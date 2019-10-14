package kdp.hudlibrary;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
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
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import kdp.hudlibrary.element.HUDCompound;
import kdp.hudlibrary.element.HUDElement;
import kdp.hudlibrary.element.HUDFluidStack;
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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
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
                        list.add(new HUDCompound(false,
                                new HUDFluidStack(new FluidStack(Fluids.WATER, 1), 30, 40).setMargin(0),
                                new HUDFluidStack(new FluidStack(Fluids.LAVA, 1), 30, 40).setMargin(0)));
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

                    @Override
                    public boolean needsSync() {
                        return IHUDProvider.super.needsSync();
                    }
                };
            });
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
