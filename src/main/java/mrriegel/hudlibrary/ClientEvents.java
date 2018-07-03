package mrriegel.hudlibrary;

import static mrriegel.hudlibrary.HUDLibrary.useList;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import mrriegel.hudlibrary.tehud.DirectionPos;
import mrriegel.hudlibrary.tehud.HUDCapability;
import mrriegel.hudlibrary.tehud.IHUDProvider;
import mrriegel.hudlibrary.tehud.IHUDProvider.Axis;
import mrriegel.hudlibrary.tehud.IHUDProvider.Direction;
import mrriegel.hudlibrary.tehud.element.HUDElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.TextTable.Alignment;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(modid = HUDLibrary.MODID, value = Side.CLIENT)
public class ClientEvents {

	public static Map<DirectionPos, NBTTagCompound> lasthudelements = new HashMap<>();
	public static Map<DirectionPos, NBTTagCompound> hudelements = new HashMap<>();
	private static Cache<DirectionPos, List<HUDElement>> cachedElements = CacheBuilder.newBuilder().//
			maximumSize(100).expireAfterWrite(250, TimeUnit.MILLISECONDS).build();

	private static Cache<Integer, Integer> glIndexes = CacheBuilder.newBuilder().//
			maximumSize(100).expireAfterWrite(10, TimeUnit.MINUTES).removalListener(n -> {
				GLAllocation.deleteDisplayLists((int) n.getValue());
			}).build();
	private static Cache<Integer, Boolean> glIndexes2 = CacheBuilder.newBuilder().//
			maximumSize(100).expireAfterWrite(250, TimeUnit.MILLISECONDS).build();

	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;
		Vec3d pp = player.getPositionEyes(event.getPartialTicks());
		//		useList = true;
		mc.world.loadedTileEntityList.stream().filter(t -> {
			return t.hasCapability(HUDCapability.cap, null) && mc.player.getDistanceSq(t.getPos()) < Math.pow(24, 2);
		}).sorted((b, a) -> {
			return Double.compare(pp.squareDistanceTo(new Vec3d(a.getPos().getX() + .5, a.getPos().getY() + 1., a.getPos().getZ() + .5)), pp.squareDistanceTo(new Vec3d(b.getPos().getX() + .5, b.getPos().getY() + 1., b.getPos().getZ() + .5)));
		}).forEachOrdered(t -> {
			IHUDProvider hud = t.getCapability(HUDCapability.cap, null);
			Vec3d v = new Vec3d(t.getPos().getX() + .5, pp.y, t.getPos().getZ() + .5);
			v = v.subtract(pp);
			Vec3d see = player.getLook(event.getPartialTicks());
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
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			if (useList)
				GlStateManager.glEndList();
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
		}
	}
}
