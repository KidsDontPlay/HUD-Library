package kdp.hudlibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.potion.Effect;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.TextTable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import org.apache.commons.lang3.RandomStringUtils;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import kdp.hudlibrary.tehud.HUDCapability;
import kdp.hudlibrary.tehud.HUDSyncMessage;
import kdp.hudlibrary.tehud.IHUDProvider;
import kdp.hudlibrary.tehud.element.HUDCompound;
import kdp.hudlibrary.tehud.element.HUDElement;
import kdp.hudlibrary.tehud.element.HUDItemStack;
import kdp.hudlibrary.tehud.element.HUDProgressBar;
import kdp.hudlibrary.tehud.element.HUDText;

@Mod(HUDLibrary.MOD_ID)
public class HUDLibrary {
    public static final String MOD_ID = "hudlibrary";

    private static final String VERSION = "1.0";
    public static SimpleChannel channel = NetworkRegistry
            .newSimpleChannel(new ResourceLocation(MOD_ID, "ch1"), () -> VERSION, VERSION::equals, VERSION::equals);

    public HUDLibrary() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        ModConfig.init();
        MinecraftForge.EVENT_BUS.addListener(this::attach);
        int index = 0;
        channel.registerMessage(index++, HUDSyncMessage.class, (m, b) -> m.encode(b), b -> {
            HUDSyncMessage m = new HUDSyncMessage();
            m.decode(b);
            return m;
        }, (m, s) -> {
            m.onMessage(m, s.get());
        });
    }

    private void setup(final FMLCommonSetupEvent event) {
        HUDCapability.register();
        //WorldGuiCapability.register();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        //ClientRegistry.registerKeyBinding(ClientEvents.OPENWORLDGUI);
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
                    public int getMargin(SpacingDirection dir) {
                        return IHUDProvider.super.getMargin(dir);
                    }

                    @Override
                    public boolean is360degrees(PlayerEntity player) {
                        return IHUDProvider.super.is360degrees(player);
                    }

                    @Override
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing,
                            Map<Integer, INBT> data) {
                        boolean breac = true;
                        return Collections.singletonList(new HUDCompound(breac, IntStream.range(0, 5).mapToObj(i -> {
                            return new HUDText(IntStream.range(0, 4)
                                    .mapToObj(ii -> RandomStringUtils.randomAlphabetic(4, 9))
                                    .collect(Collectors.joining(" ")), breac).setColor(0x6959CD);
                        }).collect(Collectors.toList())));
                    }

                    @Override
                    public LogicalSide readingSide() {
                        return IHUDProvider.super.readingSide();
                    }

                    @Override
                    public Map<Integer, INBT> getNBTData(PlayerEntity player, Direction facing) {
                        return IHUDProvider.super.getNBTData(player, facing);
                    }

                    @Override
                    public boolean needsSync() {
                        return IHUDProvider.super.needsSync();
                    }
                };
            });
        }
        final ResourceLocation resourceLocation = new ResourceLocation(MOD_ID, "vanilla_hud");
        if (event.getObject() instanceof AbstractFurnaceTileEntity) {
            event.addCapability(resourceLocation, new ICapabilityProvider() {
                IHUDProvider pro = new VanillaHUDProvider<AbstractFurnaceTileEntity>((AbstractFurnaceTileEntity) event
                        .getObject()) {

                    @Override
                    public List<HUDElement> getElements(PlayerEntity player, Direction facing,
                            Map<Integer, INBT> data) {
                        List<HUDElement> lis = new ArrayList<>();
                        lis.add(new HUDCompound(false,
                                new HUDText("Input: ", false),
                                new HUDItemStack().read((CompoundNBT) data.get(0))));
                        lis.add(new HUDCompound(false,
                                new HUDText("Output: ", false),
                                new HUDItemStack().read((CompoundNBT) data.get(1))));
                        lis.add(new HUDProgressBar(-1, 16, 0xEE444444, 0x77777777).read((DoubleNBT) data.get(2)));
                        lis.add(new HUDCompound(false,
                                new HUDText("Fuel: ", false),
                                new HUDItemStack().read((CompoundNBT) data.get(3)))
                                .setAlignment(TextTable.Alignment.LEFT));
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
                                    new HUDProgressBar(-1, 12, 0xEE444444, 0x777C6137)
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
                    public double getOffset(PlayerEntity player, Direction facing, Axis axis) {
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
                        CompoundNBT nbt = (CompoundNBT) data.get(0);
                        int level = nbt.getInt("l");
                        int primaryID = nbt.getInt("p");
                        int secondaryID = nbt.getInt("s");
                        lis.add(new HUDText("Level: " + level, false).setAlignment(TextTable.Alignment.CENTER));
                        Effect p = Effect.get(primaryID);
                        if (p != null) {
                            lis.add(new HUDText("Primary: " + I18n.format(p.getName()), false)
                                    .setAlignment(TextTable.Alignment.CENTER));
                        }
                        Effect s = Effect.get(secondaryID);
                        if (s != null) {
                            lis.add(new HUDText("Secondary: " + I18n.format(s.getName()), false)
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

                    @Override
                    public double getOffset(PlayerEntity player, Direction facing, Axis axis) {
                        return axis == Axis.NORMAL ? 0 : super.getOffset(player, facing, axis);
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

    private static abstract class VanillaHUDProvider<T extends TileEntity> implements IHUDProvider {

        protected T tile;

        public VanillaHUDProvider(T tile) {
            this.tile = tile;
        }

        @Override
        public boolean is360degrees(PlayerEntity player) {
            return ModConfig.config(tile).is360();
        }

        @Override
        public boolean isVisible(PlayerEntity player, Direction facing) {
            if (!ModConfig.config(tile).isEnabled()) {
                return false;
            }
            if (!ModConfig.config(tile).isFocus()) {
                return true;
            }
            RayTraceResult rtr = Minecraft.getInstance().objectMouseOver;
            if (rtr instanceof BlockRayTraceResult && ((BlockRayTraceResult) rtr).getPos() != null)
                return ((BlockRayTraceResult) rtr).getPos().equals(tile.getPos());
            return false;
        }

        @Override
        public int getBackgroundColor(PlayerEntity player, Direction facing) {
            return ModConfig.config(tile).getBackground();
        }

        @Override
        public double getScale(PlayerEntity player, Direction facing) {
            return ModConfig.config(tile).getScale();
        }

        @Override
        public int getWidth(PlayerEntity player, Direction facing) {
            return ModConfig.config(tile).getWidth();
        }

        @Override
        public LogicalSide readingSide() {
            return LogicalSide.SERVER;
        }
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
