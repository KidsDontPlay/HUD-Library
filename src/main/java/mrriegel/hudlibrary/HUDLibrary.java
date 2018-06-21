package mrriegel.hudlibrary;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mrriegel.hudlibrary.tehud.DirectionPos;
import mrriegel.hudlibrary.tehud.HUDCapability;
import mrriegel.hudlibrary.tehud.IHUDElement;
import mrriegel.hudlibrary.tehud.IHUDProvider;
import mrriegel.hudlibrary.tehud.IHUDProvider.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		if (config.hasChanged())
			config.save();
		HUDCapability.register();
		dev = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) throws IOException {
	}

	public static Map<DirectionPos, NBTTagCompound> hudelements = new HashMap<>();

	@SubscribeEvent
	public static void attach(AttachCapabilitiesEvent<TileEntity> event) {
		if (dev && event.getObject() instanceof TileEntityFurnace) {
			event.addCapability(new ResourceLocation(MODID, "dd"), new ICapabilityProvider() {
				TileEntityFurnace tile = (TileEntityFurnace) event.getObject();
				IHUDProvider pro = new IHUDProvider() {

					@Override
					public List<IHUDElement> elements(EntityPlayer player, EnumFacing facing) {
						List<IHUDElement> lis = new ArrayList<>();
						lis.add(new IHUDElement.HUDText("KI,aIKs", false));
						lis.add(new IHUDElement.HUDText("KI!!", false));
						lis.add(new IHUDElement.HUDText("KIsandwichmaker KIgadamn", false));
						lis.add(new IHUDElement.HUDText("KIlamm", false));
						lis.add(new IHUDElement.HUDText("KIkuh", false));
						lis.add(new IHUDElement.HUDText("KIsinus", false));
						lis.add(new IHUDElement.HUDText("KIdinekl", false));
						lis.add(new IHUDElement.HUDStack(new ItemStack(Blocks.PLANKS)));
						return lis;
					}

					@Override
					public boolean requireFocus(EntityPlayer player, EnumFacing facing) {
						return false;
					}

					@Override
					public int getBackgroundColor(EntityPlayer player, EnumFacing facing) {
						return Color.RED.getRGB();
					}

					@Override
					public Side readingSide() {
						return IHUDProvider.super.readingSide();
					}

					@Override
					public double totalScale(EntityPlayer player, EnumFacing facing) {
						if (true)
							return 1.;
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
						return (int) ((MathHelper.sin(player.ticksExisted / 9f) + 2) * 30);
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
				if (t.getPos().getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) > 24 || !t.hasCapability(HUDCapability.cap, null))
					continue;

				IHUDProvider hud = t.getCapability(HUDCapability.cap, null);
				RayTraceResult rtr = mc.objectMouseOver;
				Vec3d v = new Vec3d(t.getPos().getX() + .5, mc.player.getPositionEyes(0).y, t.getPos().getZ() + .5);
				v = v.subtract(mc.player.getPositionEyes(0));
				EnumFacing face = EnumFacing.getFacingFromVector((float) v.x, (float) v.y, (float) v.z);
				EntityPlayer player = mc.player;
				if (hud.requireFocus(player, face.getOpposite()) && !(rtr != null && rtr.typeOfHit == Type.BLOCK && rtr.getBlockPos().equals(t.getPos())))
					continue;

				List<IHUDElement> elements = hud.elements(player, face.getOpposite());
				if (elements == null /*|| elements.isEmpty()*/)
					continue;

				Iterator<IHUDElement> it = elements.iterator();
				while (it.hasNext()) {
					IHUDElement e = it.next();
					if (e == null) {
						it.remove();
						continue;
					}
					NBTTagCompound nbt = null;
					if (hud.readingSide().isServer() && (nbt = hudelements.get(new DirectionPos(t.getPos(), face.getOpposite()))) != null) {
						e.readSyncTag(nbt);
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
				float f = 1f / size;
				int height = elements.stream().mapToInt(e -> e.dimension(size).height).sum();
				int xx = 2, yy = 2;
				height += yy;
				double totalScale = MathHelper.clamp(hud.totalScale(mc.player, face.getOpposite()), .1, 2.);
				GlStateManager.translate(-.5 * totalScale + hud.offset(player, Axis.HORIZONTAL, face.getOpposite()), //
						1 * totalScale + hud.offset(player, Axis.VERTICAL, face.getOpposite()), //
						0 + hud.offset(player, Axis.NORMAL, face.getOpposite()));
				GlStateManager.scale(f, -f, f);
				//				GlStateManager.glNormal3f(0.0F, 0.0F, -f);
				GlStateManager.depthMask(false);
				GlStateManager.scale(totalScale, totalScale, totalScale);
				int color = hud.getBackgroundColor(player, face.getOpposite());
				GuiUtils.drawGradientRect(0, 0, size - height, size, size, color, color);
				//				GlStateManager.translate(3, 0, 0);
				//				GlStateManager.scale(.5, 1, 1);
				//				GlStateManager.scale(2, 1, 1);
				//				GlStateManager.translate(-3, 0, 0);
				GlStateManager.translate(0, size - height, 0);
				for (int j = 0; j < elements.size(); ++j) {
					IHUDElement e = elements.get(j);
					Dimension d = e.dimension(size);
					e.draw(xx, yy, size);
					yy += d.height;

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
