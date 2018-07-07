package mrriegel.hudlibrary;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import org.apache.commons.lang3.Range;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mrriegel.hudlibrary.tehud.HUDCapability;
import mrriegel.hudlibrary.tehud.HUDSyncMessage;
import mrriegel.hudlibrary.tehud.IHUDProvider;
import mrriegel.hudlibrary.tehud.element.HUDCompound;
import mrriegel.hudlibrary.tehud.element.HUDElement;
import mrriegel.hudlibrary.tehud.element.HUDItemStack;
import mrriegel.hudlibrary.tehud.element.HUDProgressBar;
import mrriegel.hudlibrary.tehud.element.HUDText;
import mrriegel.hudlibrary.worldgui.IWorldGuiProvider;
import mrriegel.hudlibrary.worldgui.WorldGui;
import mrriegel.hudlibrary.worldgui.WorldGuiCapability;
import mrriegel.hudlibrary.worldgui.WorldGuiContainer;
import mrriegel.hudlibrary.worldgui.message.CloseGuiMessage;
import mrriegel.hudlibrary.worldgui.message.NotifyServerMessage;
import mrriegel.hudlibrary.worldgui.message.OpenGuiMessage;
import mrriegel.hudlibrary.worldgui.message.SlotClickMessage;
import mrriegel.hudlibrary.worldgui.message.SyncContainerToClientMessage;
import mrriegel.hudlibrary.worldgui.message.SyncPlayerInventoryMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
	public static boolean dev;

	public static SimpleNetworkWrapper snw;

	public static boolean useList;
	public static int maxHUDs;
	
	@SidedProxy(serverSide="mrriegel.hudlibrary.CommonProxy",clientSide="mrriegel.hudlibrary.ClientProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		useList = config.getBoolean("useGLList", Configuration.CATEGORY_CLIENT, false, "Use OpenGL Display Lists" + Configuration.NEW_LINE + //
				"Better performance but visual bugs may occur");
		maxHUDs = config.getInt("maxHUDs", Configuration.CATEGORY_CLIENT, 10, 1, 100, "Max amount of HUDs rendering simultaneously");
		if (config.hasChanged())
			config.save();
		HUDCapability.register();
		WorldGuiCapability.register();
		dev = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
		int index = 0;
		snw = new SimpleNetworkWrapper(MODID);
		snw.registerMessage(HUDSyncMessage.class, HUDSyncMessage.class, index++, Side.CLIENT);
		snw.registerMessage(NotifyServerMessage.class, NotifyServerMessage.class, index++, Side.SERVER);
		snw.registerMessage(SyncPlayerInventoryMessage.class, SyncPlayerInventoryMessage.class, index++, Side.SERVER);
		snw.registerMessage(SyncContainerToClientMessage.class, SyncContainerToClientMessage.class, index++, Side.CLIENT);
		snw.registerMessage(OpenGuiMessage.class, OpenGuiMessage.class, index++, Side.SERVER);
		snw.registerMessage(CloseGuiMessage.class, CloseGuiMessage.class, index++, Side.SERVER);
		snw.registerMessage(SlotClickMessage.class, SlotClickMessage.class, index++, Side.SERVER);
		ClientRegistry.registerKeyBinding(ClientEvents.OPENWORLDGUI);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}

	@SubscribeEvent
	public static void attach(AttachCapabilitiesEvent<TileEntity> event) {
		if (!dev)
			return;
		if (event.getObject() instanceof TileEntityFurnace) {
			event.addCapability(new ResourceLocation(MODID, "dd"), new ICapabilityProvider() {
				TileEntityFurnace tile = (TileEntityFurnace) event.getObject();
				IHUDProvider pro = new IHUDProvider() {

					@Override
					public List<HUDElement> getElements(EntityPlayer player, EnumFacing facing) {
						List<HUDElement> lis = new ArrayList<>();
						//						lis.add(new HUDText("moon", false));
						//						lis.add(new HUDCompound(!false, new HUDText("Input: kokablac", false), new HUDItemStack(ItemStack.EMPTY)));
						//						lis.add(new HUDText("sun", false));
						//						lis.add(new HUDCompound(false, new HUDText("Output: ", false), new HUDItemStack(ItemStack.EMPTY)));
						//						lis.add(new HUDProgressBar(-1, 16, 0x22111111, 0xAA777777));
						Random ran = new Random(facing.name().hashCode() ^ tile.getPos().hashCode());
						for (int i = 0; i < 5; i++) {
							HUDProgressBar bar = new HUDProgressBar(-1, 14, 0x44098765, new Color(ran.nextInt(256), ran.nextInt(256), ran.nextInt(256), 0xFF).getRGB());
							bar.setFilling(ran.nextDouble() / 2 + .5);
							//							lis.add(bar);
						}
						//						lis.add(new HUDFluidStack(new FluidStack(FluidRegistry.LAVA, 23), -1, 26));
						//						lis.add(new HUDText("geh halt weg und weg", false));
						lis.add(new HUDItemStack(new ItemStack(Blocks.CHEST, 64)).setPadding(58));
						return lis;
					}

					@Override
					public Map<Integer, Function<TileEntity, NBTTagCompound>> getNBTData(EntityPlayer player, EnumFacing facing) {
						Int2ObjectMap<Function<TileEntity, NBTTagCompound>> map = new Int2ObjectOpenHashMap<>();
						map.put(0, t -> {
							TileEntityFurnace tile = (TileEntityFurnace) t;
							NBTTagCompound nbt = new NBTTagCompound();
							NBTTagList lis = new NBTTagList();
							lis.appendTag(new NBTTagCompound());
							nbt.setTag("stack", tile.getStackInSlot(0).writeToNBT(new NBTTagCompound()));
							lis.appendTag(nbt);
							NBTTagCompound ret = new NBTTagCompound();
							ret.setTag("elements", lis);
							return ret;
						});
						map.put(2, t -> {
							TileEntityFurnace tile = (TileEntityFurnace) t;
							NBTTagCompound nbt = new NBTTagCompound();
							nbt.setDouble("filling", tile.getField(2) / (double) tile.getField(3));
							return nbt;
						});
						map.put(1, t -> {
							TileEntityFurnace tile = (TileEntityFurnace) t;
							NBTTagCompound nbt = new NBTTagCompound();
							NBTTagList lis = new NBTTagList();
							lis.appendTag(new NBTTagCompound());
							nbt.setTag("stack", tile.getStackInSlot(2).writeToNBT(new NBTTagCompound()));
							lis.appendTag(nbt);
							NBTTagCompound ret = new NBTTagCompound();
							ret.setTag("elements", lis);
							return ret;
						});
						return map;
					}

					@Override
					public int getBackgroundColor(EntityPlayer player, EnumFacing facing) {
						if (true)
							return 0x88AA1144;
						Random k = new Random(tile.getPos().toString().hashCode());
						Color c = new Color(k.nextInt(256), k.nextInt(256), k.nextInt(256), 0x88);
						return c.getRGB();
					}

					@Override
					public Side readingSide() {
						return Side.SERVER;
					}

					@Override
					public double totalScale(EntityPlayer player, EnumFacing facing) {
						if (!true)
							return player.getPositionEyes(Minecraft.getMinecraft().getRenderPartialTicks()).distanceTo(new Vec3d(tile.getPos().getX() + .5, tile.getPos().getY() + .5, tile.getPos().getZ() + .5));
						//							return player.getDistance(tile.getPos().getX() + .5, tile.getPos().getY() + .5, tile.getPos().getZ() + .5);
						if (true)
							return 1;

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
						return IHUDProvider.super.is360degrees(player) ^ !true;
					}

					@Override
					public boolean isVisible(EntityPlayer player, EnumFacing facing) {
						if (true)
							return true;
						RayTraceResult rtr = Minecraft.getMinecraft().objectMouseOver;
						if (rtr != null && rtr.typeOfHit == Type.BLOCK && rtr.getBlockPos() != null)
							return rtr.getBlockPos().equals(tile.getPos());
						return false;
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
		} else if (event.getObject() instanceof TileEntityDropper) {
			event.addCapability(new ResourceLocation(MODID, "dd"), new ICapabilityProvider() {
				TileEntityDropper tile = (TileEntityDropper) event.getObject();
				IHUDProvider pro = new IHUDProvider() {

					List<HUDElement> l = null;

					@Override
					public List<HUDElement> getElements(EntityPlayer player, EnumFacing facing) {
						List<HUDElement> lis = new ArrayList<>();
						lis.add(new HUDText("samba", false));
						//												l = null;
						if (l == null || player.world.rand.nextDouble() < .02) {
							l = new ArrayList<>();
							for (int i = 0; i < tile.getSizeInventory(); i++) {
								l.add(new HUDItemStack(ItemStack.EMPTY));
							}
						}
						lis.add(new HUDCompound(true, l));
						return lis;
					}

					@Override
					public Map<Integer, Function<TileEntity, NBTTagCompound>> getNBTData(EntityPlayer player, EnumFacing facing) {
						Int2ObjectMap<Function<TileEntity, NBTTagCompound>> map = new Int2ObjectOpenHashMap<>();
						map.put(1, t -> {
							TileEntityDropper tile = (TileEntityDropper) t;
							NBTTagList lis = new NBTTagList();
							for (int i = 0; i < tile.getSizeInventory(); i++) {
								NBTTagCompound nbt = new NBTTagCompound();
								nbt.setTag("stack", tile.getStackInSlot(i).writeToNBT(new NBTTagCompound()));
								lis.appendTag(nbt);
							}
							NBTTagCompound ret = new NBTTagCompound();
							ret.setTag("elements", lis);
							return ret;
						});
						return map;
					}

					@Override
					public int width(EntityPlayer player, EnumFacing facing) {
						return 170;
					}

					@Override
					public int getBackgroundColor(EntityPlayer player, EnumFacing facing) {
						Random k = new Random(tile.getPos().toLong());
						if (true)
							return 0x88000000 | (0x00FFFFFF & k.nextInt());
						Color c = new Color(k.nextInt(256), k.nextInt(256), k.nextInt(256), 0x88);
						if (true)
							return c.getRGB();
						return 0x88AA1144;
					}

					@Override
					public Side readingSide() {
						return Side.SERVER;
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
		} else if (event.getObject() instanceof TileEntityChest) {
			event.addCapability(new ResourceLocation(MODID, "kip"), new ICapabilityProvider() {
				TileEntity tile = event.getObject();

				IWorldGuiProvider pro = new IWorldGuiProvider() {

					@Override
					public WorldGui getGui(EntityPlayer player, BlockPos pos) {
						return new Gui((TileEntityChest) tile);
					}

					@Override
					public WorldGuiContainer getContainer(EntityPlayer player, BlockPos pos) {
						return new WorldGuiContainer(player) {
							{
//								if(player.world.isRemote)System.out.println(this);
								int h = 10;
								for (int i = 0; i < ((IInventory) tile).getSizeInventory(); i++) {
									ItemStack s = ((IInventory) tile).getStackInSlot(i);
									if(!s.isEmpty()&&false)
										System.out.println(s);
								}
								for (int j = 0; j < 3; ++j) {
									for (int k = 0; k < 9; ++k) {
										addSlotToContainer(new Slot((IInventory) tile, k + j * 9, 8 + k * 18, 5 + j * 18));
									}
								}
								for (int l = 0; l < 3; ++l) {
									for (int j1 = 0; j1 < 9; ++j1) {
										addSlotToContainer(new Slot(player.inventory, j1 + l * 9 + 9, 8 + j1 * 18, h + 55 + l * 18 + 0));
									}
								}

								for (int i1 = 0; i1 < 9; ++i1) {
									addSlotToContainer(new Slot(player.inventory, i1, 8 + i1 * 18, h + 58 + 55 + 0));
								}
							}
							
							@Override
							public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
								 ItemStack itemstack = ItemStack.EMPTY;
							        Slot slot = this.inventorySlots.get(index);

							        if (slot != null && slot.getHasStack())
							        {
							            ItemStack itemstack1 = slot.getStack();
							            itemstack = itemstack1.copy();

							            if (index < 3 * 9)
							            {
							                if (!this.mergeItemStack(itemstack1, 3 * 9, this.inventorySlots.size(), true))
							                {
							                    return ItemStack.EMPTY;
							                }
							            }
							            else if (!this.mergeItemStack(itemstack1, 0, 3 * 9, false))
							            {
							                return ItemStack.EMPTY;
							            }

							            if (itemstack1.isEmpty())
							            {
							                slot.putStack(ItemStack.EMPTY);
							            }
							            else
							            {
							                slot.onSlotChanged();
							            }
							        }

							        return itemstack;
							}
						};
					}
				};

				@Override
				public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
					return capability == WorldGuiCapability.cap;
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

	public static void drop(EntityPlayer player) {
		InventoryPlayer inventoryplayer = player.inventory;
		if (!inventoryplayer.getItemStack().isEmpty()) {
			player.dropItem(inventoryplayer.getItemStack(), false);
			inventoryplayer.setItemStack(ItemStack.EMPTY);
			player.openContainer.detectAndSendChanges();
		}
	}
}

class Gui extends WorldGui {

	TileEntityChest tile;

	public Gui(TileEntityChest tile) {
		super();
		this.tile = tile;
		this.width = 180;
	}

	@Override
	protected void drawBackground(int mouseX, int mouseY, float partialTicks) {
		//						drawBackgroundTexture();
		int color = 0xCC000000 | (0x00FFFFFF & guiPos.hashCode());
		GuiUtils.drawGradientRect(0, 0, 0, width, height, color, color);
//		drawItemStack(new ItemStack(Blocks.CHEST, 4), 120, 13, !false);
		Random ran = new Random(color);
//		GuiUtils.drawGradientRect(0, 4, 4, 130, 33, 0xFF000000 | ran.nextInt(), 0xFF000000 | ran.nextInt());
		color = ~color | 0xFF000000;
//		GuiUtils.drawGradientRect(0, 222, 14, 229, 144, color, color);
//		drawFluidStack(new FluidStack(FluidRegistry.WATER, 23), 180, 4, 12, 120);
	}

	@Override
	protected void drawForeground(int mouseX, int mouseY, float partialTicks) {
		if (Range.between(0, 40).contains(mouseX)&&false)
			drawTooltip(Arrays.asList("minus + plus"), mouseX, mouseY);
	}
	
	@Override
	public void click(int mouse, int mouseX, int mouseY) {
		super.click(mouse, mouseX, mouseY);
//		System.out.println(container.inventorySlots.get(1).getStack());
//		System.out.println(container);
//		System.out.println(CommonEvents.getData(FMLClientHandler.instance().getClientPlayerEntity()).containers);
	}

	@Override
	public void init() {
		super.init();
		//		buttons.add(new GuiButtonExt(0, 10, 60, 58, 28, "myspace"));
		//		int i=0;
		//		int h=10;
		//		for (int j = 0; j < 3; ++j)
		//        {
		//            for (int k = 0; k < 9; ++k)
		//            {
		//            	slots.add(new Slot(tile, k + j * 9, 8 + k * 18, 5 + j * 18));
		//            }
		//        }
		//		for (int l = 0; l < 3; ++l)
		//        {
		//            for (int j1 = 0; j1 < 9; ++j1)
		//            {
		//            	slots.add(new Slot(mc.player.inventory, j1 + l * 9 + 9, 8 + j1 * 18, h+55 + l * 18 + i));
		//            }
		//        }
		//
		//        for (int i1 = 0; i1 < 9; ++i1)
		//        {
		//        	slots.add(new Slot(mc.player.inventory, i1, 8 + i1 * 18, h+58+55 + i));
		//        }
	}

	@Override
	public void buttonClicked(GuiButton b, int mouse) {
	}

	
}
