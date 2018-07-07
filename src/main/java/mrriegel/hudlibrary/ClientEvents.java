package mrriegel.hudlibrary;

import static mrriegel.hudlibrary.HUDLibrary.useList;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import mrriegel.hudlibrary.tehud.DirectionPos;
import mrriegel.hudlibrary.tehud.HUDCapability;
import mrriegel.hudlibrary.tehud.IHUDProvider;
import mrriegel.hudlibrary.tehud.IHUDProvider.Axis;
import mrriegel.hudlibrary.tehud.IHUDProvider.Direction;
import mrriegel.hudlibrary.tehud.element.HUDElement;
import mrriegel.hudlibrary.worldgui.IWorldGuiProvider;
import mrriegel.hudlibrary.worldgui.PlayerSettings;
import mrriegel.hudlibrary.worldgui.WorldGui;
import mrriegel.hudlibrary.worldgui.WorldGuiCapability;
import mrriegel.hudlibrary.worldgui.message.NotifyServerMessage;
import mrriegel.hudlibrary.worldgui.message.OpenGuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.util.TextTable.Alignment;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = HUDLibrary.MODID, value = Side.CLIENT)
public class ClientEvents {

	public static final KeyBinding OPENWORLDGUI = new KeyBinding("Open world gui", KeyConflictContext.IN_GAME, Keyboard.KEY_O, HUDLibrary.MODID);

	public static Map<DirectionPos, NBTTagCompound> hudelements = new HashMap<>();
	private static Cache<DirectionPos, List<HUDElement>> cachedElements = CacheBuilder.newBuilder().//
			maximumSize(100).expireAfterWrite(250, TimeUnit.MILLISECONDS).build();
	private static Cache<Integer, Integer> glIndexes = CacheBuilder.newBuilder().//
			maximumSize(100).expireAfterWrite(10, TimeUnit.MINUTES).removalListener(n -> {
				GLAllocation.deleteDisplayLists((int) n.getValue());
			}).build();
	private static Cache<Integer, Boolean> glIndexes2 = CacheBuilder.newBuilder().//
			maximumSize(100).expireAfterWrite(200, TimeUnit.MILLISECONDS).build();

	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		if (true)
			return;
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;
		Vec3d pp = player.getPositionEyes(event.getPartialTicks());
		Vec3d see = player.getLook(event.getPartialTicks());
		//		useList = true;
		List<TileEntity> l = mc.world.loadedTileEntityList.stream().filter(t -> {
			return t.hasCapability(HUDCapability.cap, null) && mc.player.getDistanceSq(t.getPos()) < Math.pow(24, 2);
		}).sorted((b, a) -> {
			return Double.compare(pp.squareDistanceTo(new Vec3d(a.getPos().getX() + .5, a.getPos().getY() + 1., a.getPos().getZ() + .5)), pp.squareDistanceTo(new Vec3d(b.getPos().getX() + .5, b.getPos().getY() + 1., b.getPos().getZ() + .5)));
		}).collect(Collectors.toList());
		while (l.size() > HUDLibrary.maxHUDs)
			l.remove(0);
		l.forEach(t -> {
			IHUDProvider hud = t.getCapability(HUDCapability.cap, null);
			Vec3d v = new Vec3d(t.getPos().getX() + .5, pp.y, t.getPos().getZ() + .5).subtract(pp);
			double angle = Math.toDegrees(Math.acos(see.dotProduct(v.normalize())));
			if (angle > 100)
				return;
			EnumFacing face = EnumFacing.getFacingFromVector((float) v.x, (float) v.y, (float) v.z);
			if (!hud.isVisible(player, face.getOpposite()))
				return;

			List<HUDElement> elements;
			try {
				elements = cachedElements.get(new DirectionPos(t.getPos(), face.getOpposite()), () -> hud.getElements(player, face.getOpposite()));
			} catch (ExecutionException e1) {
				throw new RuntimeException(e1);
			}
			if (elements == null || elements.isEmpty())
				return;

			NBTTagCompound n = hud.readingSide().isServer() ? hudelements.get(new DirectionPos(t.getPos(), face.getOpposite())) : null;
			NBTTagList lis = n != null ? (NBTTagList) n.getTag("list") : null;
			if (lis != null) {
				int size = Math.min(elements.size(), lis.tagCount());
				for (int i = 0; i < size; i++) {
					elements.get(i).readSyncTag(lis.getCompoundTagAt(i));
				}
			}

			double x = t.getPos().getX() - TileEntityRendererDispatcher.staticPlayerX;
			double y = t.getPos().getY() - TileEntityRendererDispatcher.staticPlayerY;
			double z = t.getPos().getZ() - TileEntityRendererDispatcher.staticPlayerZ;

			/** render */
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + .5, y + 1, z + .5);

			double f1 = 0;
			if (hud.is360degrees(player)) {
				f1 = (180 * (Math.atan2(v.x, v.z) + Math.PI)) / Math.PI;
				//					f1 = (float) ((Math.atan2(v.x, v.z) + Math.PI) * (360 / (2 * Math.PI)));
			} else {
				f1 = face.getHorizontalIndex() * 90.;
				if (face.getAxis() == EnumFacing.Axis.Z)
					f1 += 180;
			}
			GL11.glRotated(f1, 0.0, 1.0, 0.0);
			double zDiff = .5001;

			GlStateManager.enableRescaleNormal();
			int size = hud.width(player, face.getOpposite());
			int effectiveSize = size - hud.getMargin(Direction.LEFT) - hud.getMargin(Direction.RIGHT);
			float f = 1f / size;
			int height = elements.stream().mapToInt(e -> e.dimension(effectiveSize - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).height + e.getPadding(Direction.UP) + e.getPadding(Direction.DOWN)).sum();
			height += hud.getMargin(Direction.UP) + hud.getMargin(Direction.DOWN);
			double totalScale = MathHelper.clamp(hud.totalScale(mc.player, face.getOpposite()), .1, 50.);
			GlStateManager.translate(-.5 * totalScale + hud.offset(player, face.getOpposite(), Axis.HORIZONTAL), //
					1 * totalScale + hud.offset(player, face.getOpposite(), Axis.VERTICAL), //
					0 + hud.offset(player, face.getOpposite(), Axis.NORMAL) + zDiff);
			GlStateManager.scale(f, -f, f);
			//				GlStateManager.glNormal3f(0.0F, 0.0F, -f);
			GlStateManager.depthMask(false);

			GlStateManager.scale(totalScale, totalScale, totalScale);
			int color = hud.getBackgroundColor(player, face.getOpposite());
			GuiUtils.drawGradientRect(0, 0, size - height, size, size, color, color);
			GlStateManager.translate(hud.getMargin(Direction.LEFT), hud.getMargin(Direction.UP), 0);
			GlStateManager.translate(0, size - height, 0);
			//				GlStateManager.translate(0, 0, .003);
			Integer in = -1;
			if (useList && (in = glIndexes.getIfPresent(t.hashCode())) != null && glIndexes2.getIfPresent(t.hashCode()) != null)
				GlStateManager.callList(in);
			else {
				int k = render(t, elements, effectiveSize);
				if (useList)
					GlStateManager.callList(k);
			}

			GlStateManager.scale(1. / totalScale, 1. / totalScale, 1. / totalScale);
			GlStateManager.depthMask(true);
			GlStateManager.popMatrix();
		});
	}

	private static int render(TileEntity t, List<HUDElement> elements, int effectiveSize) {
		try {
			int glIndex = -1;
			if (useList) {
				glIndex = glIndexes.get(t.hashCode(), () -> GLAllocation.generateDisplayLists(1));
				glIndexes2.put(t.hashCode(), false);
				GlStateManager.glNewList(glIndex, GL11.GL_COMPILE);
			}
			{
				for (int j = 0; j < elements.size(); ++j) {
					GlStateManager.depthMask(false);
					HUDElement e = elements.get(j);
					int padLeft = e.getPadding(Direction.LEFT), padTop = e.getPadding(Direction.UP), padRight = e.getPadding(Direction.RIGHT), padDown = e.getPadding(Direction.DOWN);
					Dimension d = e.dimension(effectiveSize - padLeft - padRight);
					int offsetX = padLeft;
					if (e.getAlignment() == Alignment.RIGHT)
						offsetX += ((effectiveSize - padLeft - padRight) - d.width);
					else if (e.getAlignment() == Alignment.CENTER) {
						offsetX += ((effectiveSize - padLeft - padRight) - d.width) / 2;
					}
					GlStateManager.translate(offsetX, padTop, 0);
					//					GuiUtils.drawGradientRect(0, 0, 0, d.width, d.height, 0xff333333, 0xff333333);
					e.draw(effectiveSize - padLeft - padRight);
					GlStateManager.translate(-offsetX, padDown + d.height, 0);
				}
			}
			if (useList) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				GlStateManager.glEndList();
			}
			return glIndex;
		} catch (ExecutionException e2) {
			throw new RuntimeException(e2);
		}

	}

	@SubscribeEvent
	public static void join(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityPlayer && event.getWorld().isRemote) {
			hudelements.clear();
			glIndexes.invalidateAll();
			List<WorldGui> l = new ArrayList<>(PlayerSettings.INSTANCE.guis);
			for (WorldGui g : l)
				g.close();
		}
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if (event.phase == Phase.END && mc != null && mc.player != null) {
			if (PlayerSettings.INSTANCE.focusedGui != null && !PlayerSettings.INSTANCE.focusedGui.isFocused()) {
				PlayerSettings.INSTANCE.focusedGui.onMouseLeave();
				//				HUDLibrary.drop(mc.player);
				int id = PlayerSettings.INSTANCE.focusedGui.id;
				PlayerSettings.INSTANCE.focusedGui = null;
				HUDLibrary.snw.sendToServer(new NotifyServerMessage(false));
				CommonEvents.openWorldGuis.remove(mc.player.getUniqueID());
			}
			for (WorldGui openGui : PlayerSettings.INSTANCE.guis) {
				openGui.update();
				if (PlayerSettings.INSTANCE.focusedGui == null && openGui.isFocused()) {
					PlayerSettings.INSTANCE.focusedGui = openGui;
					openGui.onMouseEnter();
					HUDLibrary.snw.sendToServer(new NotifyServerMessage(true));
					CommonEvents.openWorldGuis.add(mc.player.getUniqueID());
				}
			}
		}
	}

	@SubscribeEvent
	public static void click(InputEvent.MouseInputEvent event) {
		if (PlayerSettings.INSTANCE.focusedGui != null) {
			WorldGui openGui = PlayerSettings.INSTANCE.focusedGui;
			if (Mouse.getEventButton() == 0 || Mouse.getEventButton() == 1)
				if (Mouse.getEventButtonState()) {
					openGui.click(Mouse.getEventButton(), (int) ((openGui.width / openGui.maxU) * openGui.u), (int) ((openGui.height / openGui.maxV) * openGui.v));
					PlayerSettings.INSTANCE.keysLocked ^= true;
				} else
					openGui.release(Mouse.getEventButton(), (int) ((openGui.width / openGui.maxU) * openGui.u), (int) ((openGui.height / openGui.maxV) * openGui.v));
		}
	}

	@SubscribeEvent
	public static void mouse(MouseEvent event) {
		if (PlayerSettings.INSTANCE.focusedGui != null && event.getDwheel() != 0 && Minecraft.getMinecraft().player.isSneaking()) {
			WorldGui openGui = PlayerSettings.INSTANCE.focusedGui;
			double scale = PlayerSettings.INSTANCE.scaleMap.getDouble(openGui.getClass());
			if (event.getDwheel() > 0) {
				scale += .00025;
				openGui.init();
			} else if (event.getDwheel() < 0) {
				scale -= .00025;
				openGui.init();
			}
			scale = MathHelper.clamp(scale, .002, .02);
			PlayerSettings.INSTANCE.scaleMap.put(openGui.getClass(), scale);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void render2(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		Vec3d pp = mc.player.getPositionEyes(event.getPartialTicks());
		Vec3d see = mc.player.getLook(event.getPartialTicks());
		if (false) {
			WorldGui gg = new WorldGui();
			BlockPos p = new BlockPos(-785, 58, -282);
			double xx = p.getX() - TileEntityRendererDispatcher.staticPlayerX;
			double yy = p.getY() - TileEntityRendererDispatcher.staticPlayerY;
			double zz = p.getZ() - TileEntityRendererDispatcher.staticPlayerZ;
			GlStateManager.translate(xx, yy, zz);
			GlStateManager.scale(.018, .018, .018);
			GlStateManager.rotate(180f, 0, 0, 1);
			//				GlStateManager.disableAlpha();
			//				GlStateManager.disableBlend();
			//				GlStateManager.disableCull();
			//				GlStateManager.disableDepth();
			//				GlStateManager.disableLighting();
			//				GlStateManager.disableRescaleNormal();
			gg.drawItemStack(new ItemStack(Blocks.CHEST, 2), 0, 0, !false);
		}
		if (!false)
			PlayerSettings.INSTANCE.guis.stream().filter(g -> g.isInFront() && pp.distanceTo(g.guiPos) < g.maxRenderDistance()).sorted((b, a) -> {
				return Double.compare(pp.distanceTo(a.guiPos), pp.distanceTo(b.guiPos));
			}).peek(openGui -> {
				if (!openGui.isReachable()) {
					openGui.u = openGui.v = openGui.maxU = openGui.maxV = Integer.MAX_VALUE;
					return;
				}
				Vec3d d2 = openGui.b.subtract(openGui.a);
				Vec3d d3 = openGui.d.subtract(openGui.a);
				Vec3d n = d2.crossProduct(d3);
				Vec3d dr = mc.player.getLook(event.getPartialTicks());
				double ndot = n.dotProduct(dr);
				if (Math.abs(ndot) >= 1e-6d) {
					double t = -n.dotProduct(mc.player.getPositionEyes(event.getPartialTicks()).subtract(openGui.a)) / ndot;
					Vec3d m = mc.player.getPositionEyes(event.getPartialTicks()).add(dr.scale(t));
					Vec3d dm = m.subtract(openGui.a);
					openGui.u = dm.dotProduct(d2);
					openGui.v = dm.dotProduct(d3);
					openGui.maxU = d2.dotProduct(d2);
					openGui.maxV = d3.dotProduct(d3);
				}
			}).forEachOrdered(openGui -> {
				double angle = Math.toDegrees(Math.acos(see.dotProduct(openGui.guiPos.subtract(pp).normalize())));
				if (angle > 100)
					return;
				double x = openGui.guiPos.x - TileEntityRendererDispatcher.staticPlayerX;
				double y = openGui.guiPos.y - TileEntityRendererDispatcher.staticPlayerY;
				double z = openGui.guiPos.z - TileEntityRendererDispatcher.staticPlayerZ;
				GlStateManager.pushMatrix();
				GlStateManager.depthMask(false);
				GlStateManager.translate(x, y, z);
				double scale = PlayerSettings.INSTANCE.scaleMap.getDouble(openGui.getClass());
				GlStateManager.scale(scale, scale, scale);
				GlStateManager.rotate(-MathHelper.wrapDegrees(openGui.yaw), 0, 1, 0);
				GlStateManager.rotate(openGui.pitch, 1, 0, 0);
				GlStateManager.rotate(180f, 0, 0, 1);
				double halfWidth = openGui.width / 2d, halfHeight = openGui.height / 2d;
				GlStateManager.translate(-halfWidth, -halfHeight, 0);
				GlStateManager.disableLighting();
				if (openGui == PlayerSettings.INSTANCE.focusedGui && !false) {
					double k = Math.sin((mc.player.ticksExisted + event.getPartialTicks()) / 7) / 2. + .5;
					int ticks = (int) (k * 255);
					int t = ticks % 255;
					int color = (0x88 << 24) | (t << 16) | (t << 8) | (t);
					GuiUtils.drawGradientRect(0, -2, -4, openGui.width + 2, -2, color, color);
					GuiUtils.drawGradientRect(0, -2, openGui.height + 2, openGui.width + 2, openGui.height + 4, color, color);
					GuiUtils.drawGradientRect(0, -4, -2, -2, openGui.height + 2, color, color);
					GuiUtils.drawGradientRect(0, openGui.width + 2, -2, openGui.width + 4, openGui.height + 2, color, color);
				}
				int mouseX = (int) ((openGui.width / openGui.maxU) * openGui.u), mouseY = (int) ((openGui.height / openGui.maxV) * openGui.v);
				openGui.draw(mouseX, mouseY, event.getPartialTicks());
				if (openGui.isFocused() && !false) {
					mc.getTextureManager().bindTexture(crosshairTex);
					GlStateManager.enableBlend();
					GlStateManager.color(1f, 1f, 1f, .6f);
					GlStateManager.disableLighting();
					GuiUtils.drawTexturedModalRect(mouseX, mouseY, 0, 0, 16, 16, 0);
				}
				GlStateManager.depthMask(true);
				GlStateManager.popMatrix();
			});
	}

	@SubscribeEvent
	public static void openGui(GuiOpenEvent event) {
		if (event.getGui() instanceof GuiIngameMenu && Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) && PlayerSettings.INSTANCE.focusedGui != null) {
			PlayerSettings.INSTANCE.focusedGui.close();
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void open(InputEvent.KeyInputEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if (OPENWORLDGUI.isPressed() && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == Type.BLOCK && mc.objectMouseOver.getBlockPos() != null && PlayerSettings.INSTANCE.focusedGui == null) {
			TileEntity tile = mc.world.getTileEntity(mc.objectMouseOver.getBlockPos());
			if (tile != null && tile.hasCapability(WorldGuiCapability.cap, mc.objectMouseOver.sideHit)) {
				IWorldGuiProvider pro = tile.getCapability(WorldGuiCapability.cap, mc.objectMouseOver.sideHit);
				WorldGui gui = pro.getGui(mc.player, tile.getPos());
				if (gui != null) {
					WorldGui.openGui(gui);
					gui.container = pro.getContainer(mc.player, tile.getPos());
					//					System.out.println(gui.container);
					if (gui.container != null) {
						CommonEvents.getData(mc.player).containers.put(gui.id, gui.container);
						HUDLibrary.snw.sendToServer(new OpenGuiMessage(gui.id, tile.getPos(), mc.objectMouseOver.sideHit));
						//						System.out.println(CommonEvents.getData(mc.player).containers);
					}
				}
			}
		}
	}

	private static final ResourceLocation crosshairTex = new ResourceLocation(HUDLibrary.MODID, "textures/gui/crosshair.png");

	@SubscribeEvent
	public static void crosshair(RenderGameOverlayEvent event) {
		if (event instanceof Pre && event.getType() == ElementType.CROSSHAIRS && PlayerSettings.INSTANCE.focusedGui != null) {
			event.setCanceled(true);
			if (true)
				return;
			Minecraft mc = Minecraft.getMinecraft();
			mc.getTextureManager().bindTexture(crosshairTex);
			GlStateManager.enableBlend();
			ScaledResolution sr = event.getResolution();
			if (mc.gameSettings.thirdPersonView == 0) {
				int l = sr.getScaledWidth();
				int i1 = sr.getScaledHeight();
				if (!mc.gameSettings.hideGUI) {
					//					GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					GlStateManager.enableAlpha();
					GlStateManager.color(1f, 1f, 1f, Mouse.getEventButtonState() ? 1f : .6f);
					GuiUtils.drawTexturedModalRect(l / 2 - 0, i1 / 2 - 0, 0, 0, 16, 16, 0);
					GlStateManager.color(1f, 1f, 1f, 1f);
				}
			}
		}
	}

	@SubscribeEvent
	public static void highlight(DrawBlockHighlightEvent event) {
		if (PlayerSettings.INSTANCE.focusedGui != null)
			event.setCanceled(true);
	}
}
