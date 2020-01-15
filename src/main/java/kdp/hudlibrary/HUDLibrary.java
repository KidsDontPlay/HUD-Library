package kdp.hudlibrary;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import kdp.hudlibrary.api.IHUDProvider;
import kdp.hudlibrary.api.enums.Axis;
import kdp.hudlibrary.api.enums.MarginDirection;
import kdp.hudlibrary.element.HUDElement;
import kdp.hudlibrary.element.HUDFluidStack;
import kdp.hudlibrary.element.HUDHorizontalCompound;
import kdp.hudlibrary.element.HUDLine;
import kdp.hudlibrary.element.HUDProgressBar;
import kdp.hudlibrary.element.HUDText;
import kdp.hudlibrary.element.HUDTexture;

@Mod(HUDLibrary.MOD_ID)
public class HUDLibrary {
    public static final String MOD_ID = "hudlibrary";

    private static final String VERSION = "1.0";
    public static SimpleChannel channel = NetworkRegistry
            .newSimpleChannel(new ResourceLocation(MOD_ID, "ch1"), () -> VERSION, VERSION::equals, VERSION::equals);

    public HUDLibrary() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        HUDConfig.init();
        MinecraftForge.EVENT_BUS.addListener(this::attach);
        int index = 0;
        channel.registerMessage(index++, HUDSyncMessage.class, HUDSyncMessage::encode, b -> {
            HUDSyncMessage m = new HUDSyncMessage();
            m.decode(b);
            return m;
        }, (m, s) -> m.onMessage(m, s.get()));
    }

    private void setup(final FMLCommonSetupEvent event) {
        HUDCapability.register();
    }

    public void attach(AttachCapabilitiesEvent<TileEntity> event) {
        boolean dev;
        try {
            Class.forName("net.minecraft.world.World");
            dev = true;
        } catch (ClassNotFoundException e) {
            dev = false;
        }
        if (event.getObject() instanceof SkullTileEntity && dev) {
            event.addCapability(new ResourceLocation(MOD_ID, "skull"), new ICapabilityProvider() {

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                    return HUDCapability.cap.orEmpty(cap, LazyOptional.of(() -> hudProvider));
                }

                final SkullTileEntity skull = (SkullTileEntity) event.getObject();
                IHUDProvider hudProvider = new IHUDProvider() {
                    @Override
                    public int getBackgroundColor(PlayerEntity player, Direction facing) {
                        return IHUDProvider.super.getBackgroundColor(player, facing);
                    }

                    @Override
                    public double getScale(PlayerEntity player, Direction facing) {
                        return IHUDProvider.super.getScale(player, facing);
                    }

                    @Override
                    public boolean isVisible(PlayerEntity player, Direction facing) {
                        return IHUDProvider.super.isVisible(player, facing);
                    }

                    @Override
                    public double getOffset(PlayerEntity player, Direction facing, Axis axis) {
                        return IHUDProvider.super.getOffset(player, facing, axis);
                    }

                    @Override
                    public int getWidth(PlayerEntity player, Direction facing) {
                        return IHUDProvider.super.getWidth(player, facing);
                    }

                    @Override
                    public int getMargin(MarginDirection dir) {
                        return IHUDProvider.super.getMargin(dir);
                    }

                    @Override
                    public boolean smoothRotation(PlayerEntity player) {
                        return IHUDProvider.super.smoothRotation(player);
                    }

                    @Override
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing, CompoundNBT data) {
                        List<HUDElement> list = new ArrayList<>();
                        list.add(new HUDTexture(new ResourceLocation("textures/gui/icons.png"), 53, 1, 7, 7));
                        list.add(new HUDText(new StringTextComponent("AlPha djn")
                                .setStyle(new Style().setBold(true).setStrikethrough(true).setObfuscated(true)),
                                false));
                        list.add(new HUDLine(Color.RED.getRGB()));
                        list.add(new HUDHorizontalCompound(false).setMargin(8));
                        list.add(new HUDHorizontalCompound(false,
                                new HUDFluidStack(new FluidStack(Fluids.WATER, 1), 30, 40).setMargin(0),
                                new HUDFluidStack(new FluidStack(Fluids.LAVA, 1), 30, 40).setMargin(0)));
                        Random ran = new Random(facing.ordinal());
                        IntStream.range(0, 5).forEach(i -> {
                            list.add(new HUDProgressBar(-1, 10, 0x00000000, 0xff000000 | ran.nextInt(),
                                    0xff000000 | ran.nextInt())
                                    .setFilling(((System.currentTimeMillis() / 40) % 100) / 100D));
                        });
                        //list.add(new HUDFluidStack(new FluidStack(Fluids.WATER, 1), 20, 60));
                        //list.add(new HUDFluidStack(new FluidStack(Fluids.LAVA, 1), 20, 60));
                        return list;
                    }

                    @Override
                    public boolean usesServerData() {
                        return IHUDProvider.super.usesServerData();
                    }

                    @Override
                    public CompoundNBT getNBTData(PlayerEntity player, Direction facing) {
                        return IHUDProvider.super.getNBTData(player, facing);
                    }
                };
            });
        }
        if (event.getObject() instanceof MobSpawnerTileEntity && dev) {
            class Ex implements IItemHandler, IFluidHandler, IEnergyStorage {

                @Override
                public int receiveEnergy(int maxReceive, boolean simulate) {
                    return 0;
                }

                @Override
                public int extractEnergy(int maxExtract, boolean simulate) {
                    return 0;
                }

                @Override
                public int getEnergyStored() {
                    return new Random(event.getObject().getPos().hashCode()).nextInt(79000);
                }

                @Override
                public int getMaxEnergyStored() {
                    return 80000;
                }

                @Override
                public boolean canExtract() {
                    return false;
                }

                @Override
                public boolean canReceive() {
                    return false;
                }

                @Override
                public int getTanks() {
                    return new Random(event.getObject().getPos().hashCode()).nextInt(6) + 1;
                }

                @Nonnull
                @Override
                public FluidStack getFluidInTank(int tank) {
                    return new FluidStack(tank % 2 == 0 ? Fluids.WATER : Fluids.LAVA,
                            new Random(tank).nextInt(38000) + 20);
                }

                @Override
                public int getTankCapacity(int tank) {
                    return 40000;
                }

                @Override
                public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
                    return false;
                }

                @Override
                public int fill(FluidStack resource, FluidAction action) {
                    return 0;
                }

                @Nonnull
                @Override
                public FluidStack drain(FluidStack resource, FluidAction action) {
                    return null;
                }

                @Nonnull
                @Override
                public FluidStack drain(int maxDrain, FluidAction action) {
                    return null;
                }

                @Override
                public int getSlots() {
                    return new Random(event.getObject().getPos().hashCode()).nextInt(18) + 1;
                }

                @Nonnull
                @Override
                public ItemStack getStackInSlot(int slot) {
                    List<Item> choice = Lists.newArrayList(ForgeRegistries.ITEMS);
                    return new ItemStack(choice.get(
                            new Random(slot + event.getObject().getPos().hashCode()).nextInt(choice.size())));
                }

                @Nonnull
                @Override
                public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                    return null;
                }

                @Nonnull
                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    return null;
                }

                @Override
                public int getSlotLimit(int slot) {
                    return 0;
                }

                @Override
                public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                    return false;
                }
            }
            event.addCapability(new ResourceLocation("da", "ma"), new ICapabilityProvider() {
                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                    if (cap == CapabilityEnergy.ENERGY || cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                        return cap.orEmpty(cap, (LazyOptional<T>) LazyOptional.of(() -> new Ex()));
                    }
                    return LazyOptional.empty();
                }
            });
        }
    }

}
