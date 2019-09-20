package kdp.hudlibrary.tehud;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effect;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.BarrelTileEntity;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.BlastFurnaceTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.SmokerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.TextTable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import kdp.hudlibrary.HUDConfig;
import kdp.hudlibrary.tehud.element.HUDCompound;
import kdp.hudlibrary.tehud.element.HUDElement;
import kdp.hudlibrary.tehud.element.HUDItemStack;
import kdp.hudlibrary.tehud.element.HUDProgressBar;
import kdp.hudlibrary.tehud.element.HUDText;

import static kdp.hudlibrary.HUDLibrary.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class VanillaImpl {

    public static final BiMap<String, Class<? extends TileEntity>> tileClassMap = ImmutableBiMap.<String, Class<? extends TileEntity>>builder()
            .put("furnace", FurnaceTileEntity.class).put("blastFurnace", BlastFurnaceTileEntity.class)
            .put("smoker", SmokerTileEntity.class).put("campfire", CampfireTileEntity.class)
            .put("beacon", BeaconTileEntity.class).put("brewingStand", BrewingStandTileEntity.class)
            .put("barrel", BarrelTileEntity.class).put("dispenser", DispenserTileEntity.class)
            .put("chest", ChestTileEntity.class).put("hopper", HopperTileEntity.class).build();

    public static final Object2BooleanOpenHashMap<Class<? extends TileEntity>> defaultFocus = new Object2BooleanOpenHashMap<>();
    public static final Object2BooleanOpenHashMap<Class<? extends TileEntity>> defaultSneak = new Object2BooleanOpenHashMap<>();
    public static final Object2ObjectOpenHashMap<Class<? extends TileEntity>, String> defaultBack = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectOpenHashMap<Class<? extends TileEntity>, String> defaultFont = new Object2ObjectOpenHashMap<>();

    static {
        defaultFocus.defaultReturnValue(false);
        defaultFocus.put(BeaconTileEntity.class, true);

        defaultSneak.defaultReturnValue(false);
        defaultSneak.put(BarrelTileEntity.class, true);
        defaultSneak.put(DispenserTileEntity.class, true);
        defaultSneak.put(ChestTileEntity.class, true);
        defaultSneak.put(HopperTileEntity.class, true);

        defaultFont.defaultReturnValue("#FFCCCCCC");
        defaultFont.put(BeaconTileEntity.class, "#FF333333");

        defaultBack.defaultReturnValue("#" + Integer.toHexString(IHUDProvider.defaultBack).toUpperCase());
        defaultBack.put(FurnaceTileEntity.class, "#BB5C5C5B");
        defaultBack.put(BlastFurnaceTileEntity.class, "#BB5C5C5B");
        defaultBack.put(SmokerTileEntity.class, "#BB5C5C5B");
        defaultBack.put(CampfireTileEntity.class, "#BB7C6137");
        defaultBack.put(BarrelTileEntity.class, "#BB7C6137");
        defaultBack.put(ChestTileEntity.class, "#BB7C6137");
        defaultBack.put(BeaconTileEntity.class, "#BB428782");
        defaultBack.put(BrewingStandTileEntity.class, "#BB5C5C5B");
        defaultBack.put(DispenserTileEntity.class, "#BB5C5C5B");
        defaultBack.put(HopperTileEntity.class, "#BB5C5C5B");
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<TileEntity> event) {
        final ResourceLocation resourceLocation = new ResourceLocation(MOD_ID, "vanilla_hud");
        if (event.getObject() instanceof AbstractFurnaceTileEntity) {
            event.addCapability(resourceLocation, new ICapabilityProvider() {
                IHUDProvider pro = new VanillaHUDProvider<AbstractFurnaceTileEntity>(
                        (AbstractFurnaceTileEntity) event.getObject()) {

                    @Override
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing, CompoundNBT data) {
                        List<HUDElement> lis = new ArrayList<>();
                        ItemStack input = ItemStack.read(data.getCompound("input"));
                        ItemStack output = ItemStack.read(data.getCompound("output"));
                        ItemStack fuel = ItemStack.read(data.getCompound("fuel"));
                        double fill = data.getDouble("fill");
                        if (!input.isEmpty()) {
                            lis.add(new HUDItemStack(input));
                        }
                        List<HUDElement> list = new ArrayList<>();
                        if (fill > 0) {
                            list.add(new HUDProgressBar(-1, 10, 0xEE444444, 0x77777777).setFilling(fill));
                        }
                        if (!output.isEmpty()) {
                            list.add(new HUDItemStack(output));
                        }
                        if (!list.isEmpty()) {
                            lis.add(new HUDCompound(false, list).setAlignment(TextTable.Alignment.RIGHT));
                        }
                        if (!fuel.isEmpty()) {
                            lis.add(new HUDItemStack(fuel));
                        }
                        return lis;
                    }

                    @Override
                    public CompoundNBT getNBTData(PlayerEntity player, Direction facing) {
                        CompoundNBT nbt = new CompoundNBT();
                        nbt.put("input", tile.getStackInSlot(0).write(new CompoundNBT()));
                        nbt.put("fuel", tile.getStackInSlot(1).write(new CompoundNBT()));
                        nbt.put("output", tile.getStackInSlot(2).write(new CompoundNBT()));
                        CompoundNBT written = tile.write(new CompoundNBT());
                        nbt.putDouble("fill", written.getInt("CookTime") / (double) written.getInt("CookTimeTotal"));
                        return nbt;
                    }

                };

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
                    return HUDCapability.cap.orEmpty(cap, LazyOptional.of(() -> pro));
                }
            });
        } else if (event.getObject() instanceof CampfireTileEntity) {
            event.addCapability(resourceLocation, new ICapabilityProvider() {
                IHUDProvider pro = new VanillaHUDProvider<CampfireTileEntity>((CampfireTileEntity) event.getObject()) {

                    @Override
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing, CompoundNBT data) {
                        List<HUDElement> lis = new ArrayList<>();
                        for (int i = 0; i < 4; i++) {
                            CompoundNBT nbt = data.getCompound(i + "");
                            if (nbt.isEmpty()) {
                                continue;
                            }
                            lis.add(new HUDCompound(false, new HUDItemStack(ItemStack.read(nbt.getCompound("item"))),
                                    new HUDProgressBar(-1, 8, 0xEE444444, 0x77777777)
                                            .setFilling(nbt.getDouble("fill"))));
                        }
                        return lis;
                    }

                    @Override
                    public CompoundNBT getNBTData(PlayerEntity player, Direction facing) {
                        CompoundNBT nbt = new CompoundNBT();
                        CompoundNBT written = tile.write(new CompoundNBT());
                        int[] cookingTimes = written.getIntArray("CookingTimes");
                        int[] cookingTotalTimes = written.getIntArray("CookingTotalTimes");
                        NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
                        ItemStackHelper.loadAllItems(written, items);
                        for (int i = 0; i < items.size(); i++) {
                            ItemStack s = items.get(i);
                            if (!s.isEmpty()) {
                                CompoundNBT n = new CompoundNBT();
                                n.put("item", s.write(new CompoundNBT()));
                                n.putDouble("fill", cookingTimes[i] / (double) cookingTotalTimes[i]);
                                nbt.put(i + "", n);
                            }
                        }
                        return nbt;
                    }

                    @Override
                    public double getOffset(PlayerEntity player, Direction facing, IHUDProvider.Axis axis) {
                        return axis == Axis.NORMAL ? -.1 : super.getOffset(player, facing, axis);
                    }
                };

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
                    return HUDCapability.cap.orEmpty(cap, LazyOptional.of(() -> pro));
                }
            });
        } else if (event.getObject() instanceof BeaconTileEntity) {
            event.addCapability(resourceLocation, new ICapabilityProvider() {
                IHUDProvider pro = new VanillaHUDProvider<BeaconTileEntity>((BeaconTileEntity) event.getObject()) {

                    @Override
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing, CompoundNBT data) {
                        List<HUDElement> lis = new ArrayList<>();
                        int fontColor = HUDConfig.config(tile).getFontColor();
                        int level = data.getInt("l");
                        int primaryID = data.getInt("p");
                        int secondaryID = data.getInt("s");
                        lis.add(new HUDText("Level: " + level, false).setColor(fontColor)
                                .setAlignment(TextTable.Alignment.CENTER));
                        Effect p = Effect.get(primaryID);
                        if (p != null) {
                            lis.add(new HUDText("Primary: " + I18n.format(p.getName()), false).setColor(fontColor)
                                    .setAlignment(TextTable.Alignment.CENTER));
                        }
                        Effect s = Effect.get(secondaryID);
                        if (s != null) {
                            lis.add(new HUDText("Secondary: " + I18n.format(s.getName()), false).setColor(fontColor)
                                    .setAlignment(TextTable.Alignment.CENTER));
                        }
                        return lis;
                    }

                    @Override
                    public CompoundNBT getNBTData(PlayerEntity player, Direction facing) {
                        CompoundNBT nbt = new CompoundNBT();
                        CompoundNBT written = tile.write(new CompoundNBT());
                        int level = tile.getLevels();
                        int primaryID = written.getInt("Primary");
                        int secondaryID = written.getInt("Secondary");
                        nbt.putInt("l", level);
                        nbt.putInt("p", primaryID);
                        nbt.putInt("s", secondaryID);
                        return nbt;
                    }
                };

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
                    return HUDCapability.cap.orEmpty(cap, LazyOptional.of(() -> pro));
                }
            });
        } else if (event.getObject() instanceof BrewingStandTileEntity) {
            event.addCapability(resourceLocation, new ICapabilityProvider() {
                IHUDProvider pro = new VanillaHUDProvider<BrewingStandTileEntity>(
                        (BrewingStandTileEntity) event.getObject()) {

                    @Override
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing, CompoundNBT data) {
                        List<HUDElement> lis = new ArrayList<>();
                        ListNBT list = data.getList("items", 10);
                        List<ItemStack> items = list.stream().map(n -> ItemStack.read((CompoundNBT) n))
                                .collect(Collectors.toList());
                        if (!items.get(3).isEmpty()) {
                            lis.add(new HUDItemStack(items.get(3)).setAlignment(TextTable.Alignment.CENTER));
                        }
                        short brewTime = data.getShort("brew");
                        if (brewTime > 0) {
                            lis.add(new HUDProgressBar(-1, 10, 0xEE444444, 0x77777777).setFilling(1 - brewTime / 400.));
                        }
                        if (!items.get(0).isEmpty() || !items.get(1).isEmpty() || !items.get(2).isEmpty()) {
                            lis.add(new HUDCompound(false, new HUDItemStack(items.get(0)),
                                    new HUDItemStack(items.get(1)), new HUDItemStack(items.get(2)))
                                    .setAlignment(TextTable.Alignment.CENTER));
                        }
                        List<HUDElement> fuelList = new ArrayList<>();
                        if (!items.get(4).isEmpty()) {
                            fuelList.add(new HUDItemStack(items.get(4)));
                        }
                        byte fuel = data.getByte("fuel");
                        if (fuel > 0) {
                            fuelList.add(new HUDProgressBar(-1, 8, 0xEE444444, 0xFFE79E00).setFilling(fuel / 20.));
                        }
                        if (!fuelList.isEmpty()) {
                            lis.add(new HUDCompound(false, fuelList));
                        }
                        return lis;
                    }

                    @Override
                    public CompoundNBT getNBTData(PlayerEntity player, Direction facing) {
                        CompoundNBT nbt = new CompoundNBT();
                        CompoundNBT written = tile.write(new CompoundNBT());
                        short brewTime = written.getShort("BrewTime");
                        byte fuel = written.getByte("Fuel");
                        NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
                        ItemStackHelper.loadAllItems(written, items);
                        ListNBT lis = new ListNBT();
                        items.forEach(s -> lis.add(s.write(new CompoundNBT())));
                        nbt.put("items", lis);
                        nbt.putShort("brew", brewTime);
                        nbt.putByte("fuel", fuel);
                        return nbt;
                    }
                };

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
                    return HUDCapability.cap.orEmpty(cap, LazyOptional.of(() -> pro));
                }
            });
        } else if (event.getObject() instanceof BarrelTileEntity || event
                .getObject() instanceof DispenserTileEntity || event.getObject() instanceof ChestTileEntity || event
                .getObject() instanceof HopperTileEntity) {
            event.addCapability(resourceLocation, new ICapabilityProvider() {
                IHUDProvider pro = new VanillaHUDProvider<LockableLootTileEntity>(
                        (LockableLootTileEntity) event.getObject()) {

                    @Override
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing, CompoundNBT data) {
                        List<HUDElement> lis = new ArrayList<>();
                        ListNBT list = data.getList("items", 10);
                        if (!list.isEmpty()) {
                            lis.add(new HUDCompound(true,
                                    list.stream().map(n -> new HUDItemStack(ItemStack.read((CompoundNBT) n)))
                                            .collect(Collectors.toList())));
                        }
                        return lis;
                    }

                    @Override
                    public CompoundNBT getNBTData(PlayerEntity player, Direction facing) {
                        CompoundNBT nbt = new CompoundNBT();
                        if (!tile.isEmpty()) {
                            ItemStackHandler handler = new ItemStackHandler(tile.getSizeInventory());
                            IntStream.range(0, tile.getSizeInventory()).forEach(i -> ItemHandlerHelper
                                    .insertItemStacked(handler, tile.getStackInSlot(i).copy(), false));
                            nbt.put("items", IntStream.range(0, handler.getSlots()).mapToObj(handler::getStackInSlot)
                                    .filter(s -> !s.isEmpty()).map(s -> s.write(new CompoundNBT()))
                                    .collect(Collectors.toCollection(ListNBT::new)));
                        }
                        return nbt;
                    }
                };

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
                    return HUDCapability.cap.orEmpty(cap, LazyOptional.of(() -> pro));
                }
            });
        }
    }

    private abstract static class VanillaHUDProvider<T extends TileEntity> implements IHUDProvider {

        T tile;

        VanillaHUDProvider(T tile) {
            this.tile = tile;
        }

        @Override
        public boolean smoothRotation(PlayerEntity player) {
            return HUDConfig.config(tile).isSmooth();
        }

        @Override
        public boolean isVisible(PlayerEntity player, Direction facing) {
            if (!HUDConfig.config(tile).isEnabled()) {
                return false;
            }
            if (HUDConfig.config(tile).isSneak() && !player.isSneaking()) {
                return false;
            }
            if (!HUDConfig.config(tile).isFocus()) {
                return true;
            }
            RayTraceResult rtr = Minecraft.getInstance().objectMouseOver;
            if (rtr instanceof BlockRayTraceResult)
                return tile.getPos().equals(((BlockRayTraceResult) rtr).getPos());
            return false;
        }

        @Override
        public int getBackgroundColor(PlayerEntity player, Direction facing) {
            return HUDConfig.config(tile).getBackground();
        }

        @Override
        public double getScale(PlayerEntity player, Direction facing) {
            return HUDConfig.config(tile).getScale();
        }

        @Override
        public int getWidth(PlayerEntity player, Direction facing) {
            return HUDConfig.config(tile).getWidth();
        }

        @Override
        public boolean usesServerData() {
            return true;
        }
    }
}
