package kdp.hudlibrary;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import kdp.hudlibrary.api.IHUDProvider;

public class HUDCapability {

    @CapabilityInject(IHUDProvider.class)
    public static final Capability<IHUDProvider> cap = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(IHUDProvider.class, new IStorage<IHUDProvider>() {

            @Override
            public INBT writeNBT(Capability<IHUDProvider> capability, IHUDProvider instance, Direction side) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void readNBT(Capability<IHUDProvider> capability, IHUDProvider instance, Direction side, INBT nbt) {
                throw new UnsupportedOperationException();
            }
        }, () -> {
            throw new UnsupportedOperationException();
        });
    }

}
