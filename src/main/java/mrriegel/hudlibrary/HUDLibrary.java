package mrriegel.hudlibrary;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import mrriegel.hudlibrary.tehud.DirectionPos;
import mrriegel.hudlibrary.tehud.HUDCapability;
import mrriegel.hudlibrary.tehud.HUDElement;
import mrriegel.hudlibrary.tehud.HUDSyncMessage;
import mrriegel.hudlibrary.tehud.IHUDProvider;
import mrriegel.hudlibrary.tehud.IHUDProvider.Axis;
import mrriegel.hudlibrary.tehud.IHUDProvider.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.TextTable.Alignment;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = HUDLibrary.MODID, name = HUDLibrary.MODNAME, version = HUDLibrary.VERSION, acceptedMinecraftVersions = "[1.12,1.13)")
@EventBusSubscriber
public class HUDLibrary {
	public static final String MODID = "hudlibrary";
	public static final String VERSION = "1.0.0";
	public static final String MODNAME = "HUD Library";

	@Instance(HUDLibrary.MODID)
	public static HUDLibrary instance;
	static boolean dev;

	public static SimpleNetworkWrapper snw;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		if (config.hasChanged())
			config.save();
		HUDCapability.register();
		dev = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
		int index = 0;
		snw = new SimpleNetworkWrapper(MODID);
		snw.registerMessage(HUDSyncMessage.class, HUDSyncMessage.class, index++, Side.CLIENT);

	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) throws IOException {
	}

	@SubscribeEvent
	public static void tick(PlayerTickEvent event) {
		if (event.side.isServer() && event.phase == Phase.END) {
			if (event.player.ticksExisted % 8 == 0) {
				snw.sendTo(new HUDSyncMessage(event.player), (EntityPlayerMP) event.player);
			}
		}
	}

	public static Map<DirectionPos, NBTTagCompound> hudelements = new HashMap<>();

	@SubscribeEvent
	public static void attach(AttachCapabilitiesEvent<TileEntity> event) {
		if (dev && event.getObject() instanceof TileEntityFurnace) {
			event.addCapability(new ResourceLocation(MODID, "dd"), new ICapabilityProvider() {
				TileEntityFurnace tile = (TileEntityFurnace) event.getObject();
				IHUDProvider pro = new IHUDProvider() {

					@Override
					public List<HUDElement> elements(EntityPlayer player, EnumFacing facing) {
						List<HUDElement> lis = new ArrayList<>();
						lis.add(new HUDElement.HUDText("IIIIIIIIIIII", false));
						//						lis.add(new HUDElement.HUDText("KI!!", false));
						lis.add(new HUDElement.HUDText("KIsandwichm salamander nuss risesn soapß", false));
						lis.add(new HUDElement.HUDText("O0O0O0O0O0O", false));
						HUDElement[] ar = new HUDElement[3];
						for (int i = 0; i < ar.length; i++) {
							if (ar.length / 2 == i)
								ar[i] = new HUDElement.HUDBar(22, 0x4415353e, 0xe1e95bcd);
							else
								ar[i] = new HUDElement.HUDStack(new ItemStack(Blocks.WOOL, 1, i));
						}
						//						lis.add(new HUDElement.HUDStack(new ItemStack(Blocks.CHEST)));
						//						lis.add(new HUDElement.HUDStack(new ItemStack(Blocks.WOOL, 1, 3)));
						lis.add(new HUDElement.HUDCompound(ar));
						lis.add(new HUDElement.HUDText("KIkuh", false));
						lis.add(new HUDElement.HUDBar(8, 0xff13E331, 0xff9b2223));
						//						lis.add(new HUDElement.HUDText("KIlamm kohle rosenmann ", false));
						lis.add(new HUDElement.HUDText("KIsinus bol", false));
						lis.add(new HUDElement.HUDText("KIdinekl cool", false));
						lis.add(new HUDElement.HUDText("Wood", false));
						return lis;
					}

					@Override
					public int getBackgroundColor(EntityPlayer player, EnumFacing facing) {
						return 0x88AA1144;
					}

					@Override
					public Side readingSide() {
						return Side.CLIENT;
					}

					@Override
					public double totalScale(EntityPlayer player, EnumFacing facing) {
						if (true)
							return 1.3;
						return (MathHelper.sin(player.ticksExisted / 10f) + 2.5) / 2.;
					}

					@Override
					public double offset(EntityPlayer player, Axis axis, EnumFacing facing) {
						if (true)
							return 0;
						if (axis == Axis.HORIZONTAL)
							return Math.sin(player.ticksExisted / 7.);
						if (axis == Axis.VERTICAL)
							return Math.sin(player.ticksExisted / 13.);
						return 0;
					}

					@Override
					public int width(EntityPlayer player, EnumFacing facing) {
						if (true)
							return 120;
						return (int) ((MathHelper.sin(player.ticksExisted / 19f) + 2) * 40);
					}

					@Override
					public int getMargin(Direction dir) {
						return 9;
					}

				};

				@Override
				public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
					return capability == HUDCapability.cap;
				}

				@Override
				public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
					if (hasCapability(capability, facing))
						return (T) pro;
					return null;
				}

			});
		}
	}

	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		try {
			Minecraft mc = Minecraft.getMinecraft();
			for (TileEntity t : mc.world.loadedTileEntityList) {
				if (!t.hasCapability(HUDCapability.cap, null))
					continue;

				IHUDProvider hud = t.getCapability(HUDCapability.cap, null);
				Vec3d v = new Vec3d(t.getPos().getX() + .5, mc.player.getPositionEyes(0).y, t.getPos().getZ() + .5);
				v = v.subtract(mc.player.getPositionEyes(0));
				EnumFacing face = EnumFacing.getFacingFromVector((float) v.x, (float) v.y, (float) v.z);
				if (!hud.isVisible(mc.player, face.getOpposite(), t))
					continue;
				EntityPlayer player = mc.player;
				//				RayTraceResult rtr = mc.objectMouseOver;
				//				if (hud.requireFocus(player, face.getOpposite()) && !(rtr != null && rtr.typeOfHit == Type.BLOCK && rtr.getBlockPos().equals(t.getPos())))
				//					continue;
				List<HUDElement> elements = hud.elements(player, face.getOpposite());
				if (elements == null || elements.isEmpty())
					continue;
				//				elements.clear();

				NBTTagCompound n = hud.readingSide().isServer() ? hudelements.get(new DirectionPos(t.getPos(), face.getOpposite())) : null;
				NBTTagList lis = n != null ? (NBTTagList) n.getTag("list") : null;
				if (lis != null) {
					Validate.isTrue(elements.size() == lis.tagCount());
					for (int i = 0; i < elements.size(); i++) {
						elements.get(i).readSyncTag(lis.getCompoundTagAt(i));
					}
				}

				double x = t.getPos().getX() - TileEntityRendererDispatcher.staticPlayerX;
				double y = t.getPos().getY() - TileEntityRendererDispatcher.staticPlayerY;
				double z = t.getPos().getZ() - TileEntityRendererDispatcher.staticPlayerZ;
				GlStateManager.pushMatrix();
				double dx = face.getAxis() == EnumFacing.Axis.Z ? 0.5F : Math.max(-0.001, face.getAxisDirection().getOffset() * -1.001);
				double dz = face.getAxis() == EnumFacing.Axis.X ? 0.5F : Math.max(-0.001, face.getAxisDirection().getOffset() * -1.001);

				GlStateManager.translate((float) x + dx, (float) y + 1F, (float) z + dz);
				float f1 = face.getHorizontalIndex() * 90f;
				if (face.getAxis() == EnumFacing.Axis.Z)
					f1 += 180f;
				GlStateManager.rotate(f1, 0.0F, 1.0F, 0.0F);
				GlStateManager.enableRescaleNormal();
				int size = hud.width(player, face.getOpposite());
				int effectiveSize = size - hud.getMargin(Direction.LEFT) - hud.getMargin(Direction.RIGHT);
				float f = 1f / size;
				int height = elements.stream().mapToInt(e -> e.dimension(effectiveSize - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).height + e.getPadding(Direction.UP) + e.getPadding(Direction.DOWN)).sum();
				height += hud.getMargin(Direction.UP) + hud.getMargin(Direction.DOWN);
				double totalScale = MathHelper.clamp(hud.totalScale(mc.player, face.getOpposite()), .1, 5.);
				GlStateManager.translate(-.5 * totalScale + hud.offset(player, Axis.HORIZONTAL, face.getOpposite()), //
						1 * totalScale + hud.offset(player, Axis.VERTICAL, face.getOpposite()), //
						0 + hud.offset(player, Axis.NORMAL, face.getOpposite()));
				GlStateManager.scale(f, -f, f);
				//				GlStateManager.glNormal3f(0.0F, 0.0F, -f);
				GlStateManager.depthMask(false);
				GlStateManager.scale(totalScale, totalScale, totalScale);
				int color = hud.getBackgroundColor(player, face.getOpposite());
				GuiUtils.drawGradientRect(0, 0, size - height, size, size, color, color);
				GuiUtils.drawGradientRect(0, 0 + hud.getMargin(Direction.LEFT), size - height + hud.getMargin(Direction.UP), size - hud.getMargin(Direction.RIGHT), size - hud.getMargin(Direction.DOWN), 0xff5555E5, 0xff5555E5);
				GlStateManager.translate(hud.getMargin(Direction.LEFT), hud.getMargin(Direction.UP), 0);
				GlStateManager.translate(0, size - height, 0);
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
					e.draw(effectiveSize - padLeft - padRight);
					GlStateManager.translate(-offsetX, padDown, 0);
					GlStateManager.translate(0, d.height, 0);
					//					yy += d.height;
				}
				//				GlStateManager.scale(1. / factor, 1. / factor, 1. / factor);
				GlStateManager.scale(1. / totalScale, 1. / totalScale, 1. / totalScale);
				GlStateManager.depthMask(true);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.popMatrix();
			}
		} catch (ConcurrentModificationException e) {
		}
	}

}
