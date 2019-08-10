package kdp.hudlibrary.tehud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.ShortNBT;
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
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
                IHUDProvider pro = new VanillaHUDProvider<AbstractFurnaceTileEntity>((AbstractFurnaceTileEntity) event
                        .getObject()) {

                    @Override
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing,
                            Map<Integer, INBT> data) {
                        List<HUDElement> lis = new ArrayList<>();
                        ItemStack input = ItemStack.read((CompoundNBT) data.get(0));
                        ItemStack output = ItemStack.read((CompoundNBT) data.get(1));
                        ItemStack fuel = ItemStack.read((CompoundNBT) data.get(3));
                        double fill = ((DoubleNBT) data.get(2)).getDouble();
                        if (!input.isEmpty()) {
                            lis.add(new HUDItemStack(input));
                        }
                        List<HUDElement> list = new ArrayList<>();
                        if (fill > 0) {
                            list.add(new HUDProgressBar(80, 12, 0xEE444444, 0x77777777).setFilling(fill));
                            list.hashCode();
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
                    public Map<Integer, INBT> getNBTData(PlayerEntity player, Direction facing) {
                        Int2ObjectMap<INBT> map = new Int2ObjectOpenHashMap<>();
                        CompoundNBT written = tile.write(new CompoundNBT());
                        map.put(0, tile.getStackInSlot(0).write(new CompoundNBT()));
                        map.put(1, tile.getStackInSlot(2).write(new CompoundNBT()));
                        map.put(2,
                                new DoubleNBT(written.getInt("CookTime") / (double) written.getInt("CookTimeTotal")));
                        map.put(3, tile.getStackInSlot(1).write(new CompoundNBT()));
                        return map;
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
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing,
                            Map<Integer, INBT> data) {
                        List<HUDElement> lis = new ArrayList<>();
                        for (int i = 0; i < 4; i++) {
                            CompoundNBT nbt = (CompoundNBT) data.get(i);
                            if (nbt == null) {
                                continue;
                            }
                            lis.add(new HUDCompound(false,
                                    new HUDItemStack().read(nbt.getCompound("item")),
                                    new HUDProgressBar(-1, 8, 0xEE444444, 0xFFE79E00)
                                            .read((DoubleNBT) nbt.get("fill"))));
                        }
                        return lis;
                    }

                    @Override
                    public Map<Integer, INBT> getNBTData(PlayerEntity player, Direction facing) {
                        Int2ObjectOpenHashMap<INBT> map = new Int2ObjectOpenHashMap<>();
                        CompoundNBT written = tile.write(new CompoundNBT());
                        int[] cookingTimes = written.getIntArray("CookingTimes");
                        int[] cookingTotalTimes = written.getIntArray("CookingTotalTimes");
                        NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
                        ItemStackHelper.loadAllItems(written, items);
                        for (int i = 0; i < items.size(); i++) {
                            ItemStack s = items.get(i);
                            if (!s.isEmpty()) {
                                CompoundNBT nbt = new CompoundNBT();
                                nbt.put("item", s.write(new CompoundNBT()));
                                nbt.putDouble("fill", cookingTimes[i] / (double) cookingTotalTimes[i]);
                                map.put(i, nbt);
                            }
                        }
                        return map;
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
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing,
                            Map<Integer, INBT> data) {
                        List<HUDElement> lis = new ArrayList<>();
                        int fontColor = HUDConfig.config(tile).getFontColor();
                        CompoundNBT nbt = (CompoundNBT) data.get(0);
                        int level = nbt.getInt("l");
                        int primaryID = nbt.getInt("p");
                        int secondaryID = nbt.getInt("s");
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
                    public Map<Integer, INBT> getNBTData(PlayerEntity player, Direction facing) {
                        CompoundNBT written = tile.write(new CompoundNBT());
                        int level = tile.getLevels();
                        int primaryID = written.getInt("Primary");
                        int secondaryID = written.getInt("Secondary");
                        CompoundNBT nbt = new CompoundNBT();
                        nbt.putInt("l", level);
                        nbt.putInt("p", primaryID);
                        nbt.putInt("s", secondaryID);
                        return Collections.singletonMap(0, nbt);
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
                IHUDProvider pro = new VanillaHUDProvider<BrewingStandTileEntity>((BrewingStandTileEntity) event
                        .getObject()) {

                    @Override
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing,
                            Map<Integer, INBT> data) {
                        List<HUDElement> lis = new ArrayList<>();
                        ListNBT list = (ListNBT) data.get(0);
                        List<ItemStack> items = list.stream().map(n -> ItemStack.read((CompoundNBT) n))
                                .collect(Collectors.toList());
                        if (!items.get(3).isEmpty()) {
                            lis.add(new HUDItemStack(items.get(3)).setAlignment(TextTable.Alignment.CENTER));
                        }
                        short brewTime = ((ShortNBT) data.get(1)).getShort();
                        if (brewTime > 0) {
                            lis.add(new HUDProgressBar(-1, 12, 0xEE444444, 0x77777777).setFilling(1 - brewTime / 400.));
                        }
                        if (!items.get(0).isEmpty() || !items.get(1).isEmpty() || !items.get(2).isEmpty()) {
                            lis.add(new HUDCompound(false,
                                    new HUDItemStack(items.get(0)),
                                    new HUDItemStack(items.get(1)),
                                    new HUDItemStack(items.get(2))).setAlignment(TextTable.Alignment.CENTER));
                        }
                        List<HUDElement> fuelList = new ArrayList<>();
                        if (!items.get(4).isEmpty()) {
                            fuelList.add(new HUDItemStack(items.get(4)));
                        }
                        byte fuel = ((ByteNBT) data.get(2)).getByte();
                        if (fuel > 0) {
                            fuelList.add(new HUDProgressBar(-1, 8, 0xEE444444, 0xFFE79E00).setFilling(fuel / 20.));
                        }
                        if (!fuelList.isEmpty()) {
                            lis.add(new HUDCompound(false, fuelList));
                        }
                        return lis;
                    }

                    @Override
                    public Map<Integer, INBT> getNBTData(PlayerEntity player, Direction facing) {
                        Int2ObjectOpenHashMap<INBT> map = new Int2ObjectOpenHashMap<>();
                        CompoundNBT written = tile.write(new CompoundNBT());
                        short brewTime = written.getShort("BrewTime");
                        byte fuel = written.getByte("Fuel");
                        NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
                        ItemStackHelper.loadAllItems(written, items);
                        ListNBT lis = new ListNBT();
                        items.forEach(s -> lis.add(s.write(new CompoundNBT())));
                        map.put(0, lis);
                        map.put(1, new ShortNBT(brewTime));
                        map.put(2, new ByteNBT(fuel));
                        return map;
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
                IHUDProvider pro = new VanillaHUDProvider<LockableLootTileEntity>((LockableLootTileEntity) event
                        .getObject()) {

                    @Override
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing,
                            Map<Integer, INBT> data) {
                        List<HUDElement> lis = new ArrayList<>();
                        ListNBT list = (ListNBT) data.get(0);
                        if (list != null) {
                            lis.add(new HUDCompound(true,
                                    list.stream().map(n -> new HUDItemStack(ItemStack.read((CompoundNBT) n)))
                                            .collect(Collectors.toList())));
                        }
                        return lis;
                    }

                    @Override
                    public Map<Integer, INBT> getNBTData(PlayerEntity player, Direction facing) {
                        Int2ObjectOpenHashMap<INBT> map = new Int2ObjectOpenHashMap<>();
                        if (!tile.isEmpty()) {
                            ItemStackHandler handler = new ItemStackHandler(tile.getSizeInventory());
                            IntStream.range(0, tile.getSizeInventory()).forEach(i -> ItemHandlerHelper
                                    .insertItemStacked(handler, tile.getStackInSlot(i).copy(), false));
                            map.put(0,
                                    IntStream.range(0, handler.getSlots()).mapToObj(handler::getStackInSlot)
                                            .filter(s -> !s.isEmpty()).map(s -> s.write(new CompoundNBT()))
                                            .collect(Collectors.toCollection(ListNBT::new)));
                        }
                        return map;
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

        protected T tile;

        public VanillaHUDProvider(T tile) {
            this.tile = tile;
        }

        @Override
        public boolean is360degrees(PlayerEntity player) {
            return HUDConfig.config(tile).is360();
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
            if (rtr instanceof BlockRayTraceResult && ((BlockRayTraceResult) rtr).getPos() != null)
                return ((BlockRayTraceResult) rtr).getPos().equals(tile.getPos());
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
        public LogicalSide readingSide() {
            return LogicalSide.SERVER;
        }
    }
}
