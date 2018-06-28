package mrriegel.hudlibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import joptsimple.internal.Strings;
import mrriegel.hudlibrary.tehud.HUDCapability;
import mrriegel.hudlibrary.tehud.HUDSyncMessage;
import mrriegel.hudlibrary.tehud.IHUDProvider;
import mrriegel.hudlibrary.tehud.element.HUDCompound;
import mrriegel.hudlibrary.tehud.element.HUDElement;
import mrriegel.hudlibrary.tehud.element.HUDItemStack;
import mrriegel.hudlibrary.tehud.element.HUDLine;
import mrriegel.hudlibrary.tehud.element.HUDProgressBar;
import mrriegel.hudlibrary.tehud.element.HUDText;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AttachCapabilitiesEvent;
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
	public void postInit(FMLPostInitializationEvent event) {
	}

	@SubscribeEvent
	public static void tick(PlayerTickEvent event) {
		if (event.side.isServer() && event.phase == Phase.END) {
			if (event.player.ticksExisted % 8 == 0) {
				snw.sendTo(new HUDSyncMessage(event.player), (EntityPlayerMP) event.player);
			}
		}
	}

	@SubscribeEvent
	public static void attach(AttachCapabilitiesEvent<TileEntity> event) {
		if (dev && event.getObject() instanceof TileEntityFurnace) {
			event.addCapability(new ResourceLocation(MODID, "dd"), new ICapabilityProvider() {
				TileEntityFurnace tile = (TileEntityFurnace) event.getObject();
				IHUDProvider pro = new IHUDProvider() {
					List<HUDElement> ls = null;
					long time = 0;

					@Override
					public List<HUDElement> getElements(EntityPlayer player, EnumFacing facing) {
						if (System.currentTimeMillis() - time > 1000 || true) {
							ls = null;
							time = System.currentTimeMillis();
						}
						if (ls != null)
							return ls;
						List<HUDElement> lis = new ArrayList<>();
						lis.add(new HUDText("Facing: " + (player.isSneaking() ? facing.toString().toUpperCase() : facing), false));
						//						lis.add(new HUDText("KI!!", false));
						//						lis.add(new HUDText("KIsandwichm salamander nuss risesn soapß", false));
						lis.add(new HUDItemStack(ItemStack.EMPTY));
						lis.add(new HUDProgressBar(78, 16, 0x4415353e, 0xe1e95bcd));
						lis.add(new HUDItemStack(ItemStack.EMPTY));
						HUDElement[] ar = new HUDElement[10 - -2];
						for (int i = 0; i < ar.length; i++) {
							ar[i] = new HUDItemStack(new ItemStack(Blocks.WOOL, 1 + i, i));
						}
						//						lis.add(new HUDItemStack(new ItemStack(Blocks.CHEST)));
						if (true)
							lis.add(new HUDCompound(true, ar));
						else
							lis.add(new HUDProgressBar(38, 16, 0x4415353e, 0xe1e95bcd));
						lis.add(new HUDLine());
						lis.add(new HUDProgressBar(50, 8, 0xff232321, 0xff9b2223));
						//						lis.add(new HUDText("KIlamm kohle rosenmann ", false));
						//						lis.add(new HUDText("KIsinus bol", false));
						List<HUDElement> list = new ArrayList<>();
						for (int i = 1; i < 9; i++) {
							list.add(new HUDText(Strings.repeat('o', i), false));
						}
						list.add(0, new HUDText("H", false));
						list.add(new HUDText("moreover far way from chinatown", false));
						list.add(new HUDText("Over nothing else than a shot rose", false));

						if (false)
							lis.add(new HUDCompound(true, list));

						//						lis.add(new HUDText("KIdinekl cool", false));
						//						lis.add(new HUDText("Wood", false));
						//						Collections.shuffle(lis, new Random(facing.getName2().hashCode()));
						return ls = lis;
					}

					@Override
					public List<Function<TileEntity, NBTTagCompound>> getNBTData(EntityPlayer player, EnumFacing facing) {
						List<Function<TileEntity, NBTTagCompound>> lis = new ArrayList<>();
						lis.add(t -> null);
						lis.add(t -> {
							TileEntityFurnace tile = (TileEntityFurnace) t;
							NBTTagCompound nbt = new NBTTagCompound();
							nbt.setTag("stack", tile.getStackInSlot(0).writeToNBT(new NBTTagCompound()));
							return nbt;
						});
						lis.add(t -> {
							TileEntityFurnace tile = (TileEntityFurnace) t;
							NBTTagCompound nbt = new NBTTagCompound();
							nbt.setDouble("filling", tile.getField(2) / (double) tile.getField(3));
							return nbt;
						});
						lis.add(t -> {
							TileEntityFurnace tile = (TileEntityFurnace) t;
							NBTTagCompound nbt = new NBTTagCompound();
							nbt.setTag("stack", tile.getStackInSlot(2).writeToNBT(new NBTTagCompound()));
							return nbt;
						});
						return lis;
					}

					@Override
					public int getBackgroundColor(EntityPlayer player, EnumFacing facing) {
						return 0x88AA1144;
					}

					@Override
					public Side readingSide() {
						return Side.SERVER;
					}

					@Override
					public double totalScale(EntityPlayer player, EnumFacing facing) {
						if (true)
							return 1.3 * 2;
						return (MathHelper.sin(player.ticksExisted / 10f) + 2.5) / 2.;
					}

					@Override
					public double offset(EntityPlayer player, EnumFacing facing, Axis axis) {
						//						if (axis == Axis.NORMAL)
						//							return Math.sin(player.ticksExisted / 9.);
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
						return (int) ((MathHelper.sin(player.ticksExisted / 19f) + 2) * 50);
					}

					@Override
					public int getMargin(Direction dir) {
						return 2;
					}

					@Override
					public boolean is360degrees(EntityPlayer player) {
						return IHUDProvider.super.is360degrees(player) ^ true;
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

}
