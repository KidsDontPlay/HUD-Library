package kdp.hudlibrary;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.TextTable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.GameData;

import org.apache.commons.lang3.tuple.Pair;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import kdp.hudlibrary.tehud.HUDCapability;
import kdp.hudlibrary.tehud.HUDSyncMessage;
import kdp.hudlibrary.tehud.IHUDProvider;
import kdp.hudlibrary.tehud.element.HUDCompound;
import kdp.hudlibrary.tehud.element.HUDElement;
import kdp.hudlibrary.tehud.element.HUDItemStack;
import kdp.hudlibrary.tehud.element.HUDLine;
import kdp.hudlibrary.tehud.element.HUDProgressBar;
import kdp.hudlibrary.tehud.element.HUDText;

@Mod(HUDLibrary.MOD_ID)
public class HUDLibrary {
    public static final String MOD_ID = "hudlibrary";

    private static final String VERSION = "1.0";
    public static SimpleChannel channel = NetworkRegistry
            .newSimpleChannel(GameData.checkPrefix("ch1", false), () -> VERSION, VERSION::equals, VERSION::equals);

    private static ForgeConfigSpec config;
    public static ForgeConfigSpec.IntValue maxHUDs;

    public HUDLibrary() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        Pair<HUDLibrary, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(b -> {
            b.push("Client");
            maxHUDs = b.comment("Max amount of HUDs rendering simultaneously").defineInRange("maxHUDS", 10, 1, 100);
            b.pop();
            return HUDLibrary.this;
        });
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, config = pair.getValue());
        MinecraftForge.EVENT_BUS.addListener(this::attach);
        MinecraftForge.EVENT_BUS.addListener(this::attach2);
    }

    private void setup(final FMLCommonSetupEvent event) {
        HUDCapability.register();
        //WorldGuiCapability.register();
        int index = 0;
        channel.registerMessage(index++, HUDSyncMessage.class, (m, b) -> m.encode(b), b -> {
            HUDSyncMessage m = new HUDSyncMessage();
            m.decode(b);
            return m;
        }, (m, s) -> {
            m.onMessage(m, s.get());
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        //ClientRegistry.registerKeyBinding(ClientEvents.OPENWORLDGUI);
    }

    public void attach2(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof CreeperEntity) {
            event.addCapability(new ResourceLocation(MOD_ID, "creep"), new ICapabilityProvider() {

                IHUDProvider<FurnaceTileEntity> pro = new IHUDProvider<FurnaceTileEntity>() {
                    @Override
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing) {
                        return Arrays.asList(new HUDText("Keks", false));
                    }

                    @Override
                    public Map<Integer, Function<FurnaceTileEntity, CompoundNBT>> getNBTData(PlayerEntity player,
                            Direction facing) {
                        return null;
                    }
                };

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                    return HUDCapability.cap.orEmpty(cap, LazyOptional.of(() -> pro));
                }
            });
        }
    }

    public void attach(AttachCapabilitiesEvent<TileEntity> event) {
        if (event.getObject() instanceof FurnaceTileEntity) {
            event.addCapability(new ResourceLocation(MOD_ID, "dd"), new ICapabilityProvider() {
                FurnaceTileEntity tile = (FurnaceTileEntity) event.getObject();
                IHUDProvider<FurnaceTileEntity> pro = new IHUDProvider<FurnaceTileEntity>() {

                    @Override
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing) {
                        List<HUDElement> lis = new ArrayList<>();
                        lis.add(new HUDCompound(false,
                                new HUDText("Input: ", false),
                                new HUDItemStack(ItemStack.EMPTY)));
                        lis.add(new HUDCompound(false,
                                new HUDText("Output: ", false),
                                new HUDItemStack(ItemStack.EMPTY)));
                        lis.add(new HUDProgressBar(-1, 16, 0xEE444444, 0x77777777)
                                .setBackgroundColor(Color.YELLOW.getRGB()));
                        lis.add(new HUDCompound(false,
                                new HUDText("Fuel: ", false),
                                new HUDItemStack(ItemStack.EMPTY),
                                new HUDText("", false)).setAlignment(TextTable.Alignment.LEFT));
                        lis.add(new HUDText(TextFormatting.ITALIC
                                .toString() + "Hello kids, what do you know about crocodiles in arizona? #DONTKILLTHEMESSENGERUNLESSYOUHAVETO",
                                !false).setColor(Color.GREEN.getRGB()));
                        lis.add(new HUDText("selectWorldqg", false).setColor(0xffff0000));
                        lis.add(new HUDLine());
                        lis.add(new HUDItemStack(new ItemStack(Items.CYAN_STAINED_GLASS_PANE)).setAlignment(TextTable.Alignment.CENTER));
                        lis.add(new HUDCompound(!false,
                                new HUDProgressBar(60, 10, 0xff000000, 0xffeeeeee),
                                new HUDProgressBar(60, 10, 0xff000000, 0xffeeeeee)));
                        lis.add(new HUDText("Volzotan", false).setColor(0xff000000 | tile.hashCode())
                                .setAlignment(TextTable.Alignment.CENTER));
                        return lis;
                    }

                    @Override
                    public Map<Integer, Function<FurnaceTileEntity, CompoundNBT>> getNBTData(PlayerEntity player,
                            Direction facing) {
                        Int2ObjectMap<Function<FurnaceTileEntity, CompoundNBT>> map = new Int2ObjectOpenHashMap<>();
                        CompoundNBT written = tile.write(new CompoundNBT());
                        map.put(0, t -> {
                            FurnaceTileEntity tile = (FurnaceTileEntity) t;
                            CompoundNBT nbt = new CompoundNBT();
                            ListNBT lis = new ListNBT();
                            lis.add(new CompoundNBT());
                            nbt.put("stack", tile.getStackInSlot(0).write(new CompoundNBT()));
                            lis.add(nbt);
                            CompoundNBT ret = new CompoundNBT();
                            ret.put("elements", lis);
                            return ret;
                        });
                        map.put(2, t -> {
                            FurnaceTileEntity tile = (FurnaceTileEntity) t;
                            CompoundNBT nbt = new CompoundNBT();
                            nbt.putDouble("filling",
                                    written.getInt("CookTime") / (double) written.getInt("CookTimeTotal"));
                            return nbt;
                        });
                        map.put(1, t -> {
                            FurnaceTileEntity tile = (FurnaceTileEntity) t;
                            CompoundNBT nbt = new CompoundNBT();
                            ListNBT lis = new ListNBT();
                            lis.add(new CompoundNBT());
                            nbt.put("stack", tile.getStackInSlot(2).write(new CompoundNBT()));
                            lis.add(nbt);
                            CompoundNBT ret = new CompoundNBT();
                            ret.put("elements", lis);
                            return ret;
                        });
                        map.put(3, t -> {
                            FurnaceTileEntity tile = (FurnaceTileEntity) t;
                            CompoundNBT nbt = new CompoundNBT();
                            ListNBT lis = new ListNBT();
                            lis.add(new CompoundNBT());
                            nbt.put("stack", tile.getStackInSlot(1).write(new CompoundNBT()));
                            lis.add(nbt);
                            nbt = new CompoundNBT();
                            nbt.putString("text", " " + written.getString("BurnTime"));
                            lis.add(nbt);
                            CompoundNBT ret = new CompoundNBT();
                            ret.put("elements", lis);
                            return ret;
                        });
                        return map;
                    }

                    @Override
                    public int getBackgroundColor(PlayerEntity player, Direction facing) {
                        if (true)
                            return 0x88444444;
                        Random k = new Random(tile.getPos().toString().hashCode());
                        Color c = new Color(k.nextInt(256), k.nextInt(256), k.nextInt(256), 0x88);
                        return c.getRGB();
                    }

                    @Override
                    public LogicalSide readingSide() {
                        return LogicalSide.SERVER;
                    }

                    @Override
                    public double totalScale(PlayerEntity player, Direction facing) {
                        if (!true)
                            return player.getEyePosition(Minecraft.getInstance().getRenderPartialTicks())
                                    .distanceTo(new Vec3d(tile.getPos().getX() + .5,
                                            tile.getPos().getY() + .5,
                                            tile.getPos().getZ() + .5));
                        //							return player.getDistance(tile.getPos().getX() + .5, tile.getPos().getY() + .5, tile.getPos().getZ() + .5);
                        if (true)
                            return 1.5;

                        return (MathHelper.sin(player.ticksExisted / 10f) + 2.5) / 2.;
                    }

                    @Override
                    public double offset(PlayerEntity player, Direction facing, Axis axis) {
                        //												if (axis == Axis.NORMAL)
                        //													return Math.sin(player.ticksExisted / 9.);
                        if(axis==Axis.NORMAL)
                            return -1.;
                        if (true)
                            return 0;
                        if (axis == Axis.HORIZONTAL)
                            return Math.sin(player.ticksExisted / 7.);
                        if (axis == Axis.VERTICAL)
                            return Math.sin(player.ticksExisted / 13.);
                        return 0;
                    }

                    @Override
                    public int width(PlayerEntity player, Direction facing) {
                        if (true)
                            return 128;
                        return (int) ((MathHelper.sin(player.ticksExisted / 19f) + 2) * 50);
                    }

                    @Override
                    public int getMargin(SpacingDirection dir) {
                        return 0;
                    }

                    @Override
                    public boolean is360degrees(PlayerEntity player) {
                        return false;
                    }

                    @Override
                    public boolean isVisible(PlayerEntity player, Direction facing) {
                        if (true)
                            return true;
                        RayTraceResult rtr = Minecraft.getInstance().objectMouseOver;
                        if (rtr instanceof BlockRayTraceResult && ((BlockRayTraceResult) rtr).getPos() != null)
                            return ((BlockRayTraceResult) rtr).getPos().equals(tile.getPos());
                        return false;
                    }
                };

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
                    return HUDCapability.cap.orEmpty(cap, LazyOptional.of(() -> pro));
                }
            });
        } /*else if (event.getObject() instanceof TileEntityChest) {
            event.addCapability(new ResourceLocation(MOD_ID, "dd"), new ICapabilityProvider() {
                TileEntityChest tile = (TileEntityChest) event.getObject();
                IHUDProvider<TileEntityChest> pro = new IHUDProvider<TileEntityChest>() {

                    List<HUDElement> l = null;

                    @Override
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing) {
                        List<HUDElement> lis = new ArrayList<>();
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
                    public Map<Integer, Function<TileEntityChest, CompoundNBT>> getNBTData(PlayerEntity player,
                            Direction facing) {
                        Int2ObjectMap<Function<TileEntityChest, CompoundNBT>> map = new Int2ObjectOpenHashMap<>();
                        map.put(0, t -> {
                            TileEntityChest tile = (TileEntityChest) t;
                            ListNBT lis = new ListNBT();
                            for (int i = 0; i < tile.getSizeInventory(); i++) {
                                CompoundNBT nbt = new CompoundNBT();
                                nbt.put("stack", tile.getStackInSlot(i).writeToNBT(new CompoundNBT()));
                                lis.add(nbt);
                            }
                            CompoundNBT ret = new CompoundNBT();
                            ret.put("elements", lis);
                            return ret;
                        });
                        return map;
                    }

                    @Override
                    public int width(PlayerEntity player, Direction facing) {
                        return 170;
                    }

                    @Override
                    public int getBackgroundColor(PlayerEntity player, Direction facing) {
                        if (!true)
                            return 0x99000000 | (0x00FFFFFF & (tile.getPos().hashCode() * facing.ordinal()));
                        return 0x8811AA44;
                    }

                    @Override
                    public Side readingSide() {
                        return Side.SERVER;
                    }

                };

                @Override
                public boolean hasCapability(Capability<?> capability, Direction facing) {
                    return capability == HUDCapability.cap;
                }

                @Override
                public <T> T getCapability(Capability<T> capability, Direction facing) {
                    if (hasCapability(capability, facing))
                        return (T) pro;
                    return null;
                }

            });
        } else if (event.getObject() instanceof TileEntityBrewingStand) {
            event.addCapability(new ResourceLocation(MOD_ID, "dd"), new ICapabilityProvider() {
                TileEntityBrewingStand tile = (TileEntityBrewingStand) event.getObject();
                IHUDProvider<TileEntityBrewingStand> pro = new IHUDProvider<TileEntityBrewingStand>() {

                    @Override
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing) {
                        List<HUDElement> lis = new ArrayList<>();
                        lis.add(new HUDCompound(false,
                                new HUDText("Ingredient: ", false),
                                new HUDItemStack(ItemStack.EMPTY),
                                new HUDItemStack(ItemStack.EMPTY),
                                new HUDItemStack(ItemStack.EMPTY)));
                        lis.add(new HUDText("", false));
                        return lis;
                    }

                    @Override
                    public Map<Integer, Function<TileEntityBrewingStand, CompoundNBT>> getNBTData(
                            PlayerEntity player, Direction facing) {
                        Int2ObjectMap<Function<TileEntityBrewingStand, CompoundNBT>> map = new Int2ObjectOpenHashMap<>();
                        map.put(0, t -> {
                            TileEntityBrewingStand tile = (TileEntityBrewingStand) t;
                            CompoundNBT ret = new CompoundNBT();
                            ListNBT list = new ListNBT();
                            list.add(new CompoundNBT());
                            ret.put("elements", list);
                            return ret;
                        });
                        return map;
                    }

                    @Override
                    public int width(PlayerEntity player, Direction facing) {
                        return 120;
                    }

                    @Override
                    public int getBackgroundColor(PlayerEntity player, Direction facing) {
                        if (!true)
                            return 0x99000000 | (0x00FFFFFF & (tile.getPos().hashCode() * facing.ordinal()));
                        return 0x881144AA;
                    }

                    @Override
                    public Side readingSide() {
                        return Side.SERVER;
                    }

                };

                @Override
                public boolean hasCapability(Capability<?> capability, Direction facing) {
                    return capability == HUDCapability.cap;
                }

                @Override
                public <T> T getCapability(Capability<T> capability, Direction facing) {
                    if (hasCapability(capability, facing))
                        return (T) pro;
                    return null;
                }

            });
        }*/
        /*if (event.getObject() instanceof TileEntityChest) {
            event.addCapability(new ResourceLocation(MOD_ID, "kip"), new ICapabilityProvider() {
                TileEntity tile = event.getObject();

                IWorldGuiProvider pro = new IWorldGuiProvider() {

                    @Override
                    public WorldGui getGui(PlayerEntity player, BlockPos pos) {
                        return new Gui((TileEntityChest) tile);
                    }

                    @Override
                    public ContainerWG getContainer(PlayerEntity player, BlockPos pos) {
                        return new ContainerWG(player) {
                            {
                                //								if(player.world.isRemote)System.out.println(this);
                                int h = 10;
                                for (int i = 0; i < ((IInventory) tile).getSizeInventory(); i++) {
                                    ItemStack s = ((IInventory) tile).getStackInSlot(i);
                                    if (!s.isEmpty() && false)
                                        System.out.println(s);
                                }
                                for (int j = 0; j < 3; ++j) {
                                    for (int k = 0; k < 9; ++k) {
                                        addSlotToContainer(new Slot((IInventory) tile,
                                                k + j * 9,
                                                8 + k * 18,
                                                5 + j * 18));
                                    }
                                }
                                for (int l = 0; l < 3; ++l) {
                                    for (int j1 = 0; j1 < 9; ++j1) {
                                        addSlotToContainer(new Slot(player.inventory,
                                                j1 + l * 9 + 9,
                                                8 + j1 * 18,
                                                h + 55 + l * 18 + 0));
                                    }
                                }

                                for (int i1 = 0; i1 < 9; ++i1) {
                                    addSlotToContainer(new Slot(player.inventory, i1, 8 + i1 * 18, h + 58 + 55 + 0));
                                }
                            }

                            @Override
                            public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
                                ItemStack itemstack = ItemStack.EMPTY;
                                Slot slot = this.inventorySlots.get(index);

                                if (slot != null && slot.getHasStack()) {
                                    ItemStack itemstack1 = slot.getStack();
                                    itemstack = itemstack1.copy();

                                    if (index < 3 * 9) {
                                        if (!this.mergeItemStack(itemstack1, 3 * 9, this.inventorySlots.size(), true)) {
                                            return ItemStack.EMPTY;
                                        }
                                    } else if (!this.mergeItemStack(itemstack1, 0, 3 * 9, false)) {
                                        return ItemStack.EMPTY;
                                    }

                                    if (itemstack1.isEmpty()) {
                                        slot.putStack(ItemStack.EMPTY);
                                    } else {
                                        slot.onSlotChanged();
                                    }
                                }

                                return itemstack;
                            }
                        };
                    }
                };

                @Override
                public boolean hasCapability(Capability<?> capability, Direction facing) {
                    return capability == WorldGuiCapability.cap;
                }

                @Override
                public <T> T getCapability(Capability<T> capability, Direction facing) {
                    if (hasCapability(capability, facing))
                        return (T) pro;
                    return null;
                }
            });
        }*/
    }

}

/*class Gui extends WorldGui {

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
		if (Range.between(0, 40).contains(mouseX) && false)
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
	public void buttonClicked(GuiButton b, int mouse) {
	}

}*/
