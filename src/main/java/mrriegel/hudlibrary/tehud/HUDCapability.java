package mrriegel.hudlibrary.tehud;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class HUDCapability {

	@CapabilityInject(IHUDProvider.class)
	public static Capability<IHUDProvider> cap;

	public static void register() {
		CapabilityManager.INSTANCE.register(IHUDProvider.class, new IStorage<IHUDProvider>() {

			@Override
			public NBTBase writeNBT(Capability<IHUDProvider> capability, IHUDProvider instance, EnumFacing side) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void readNBT(Capability<IHUDProvider> capability, IHUDProvider instance, EnumFacing side, NBTBase nbt) {
				throw new UnsupportedOperationException();
			}
		}, () -> {
			throw new UnsupportedOperationException();
		});
	}

}
