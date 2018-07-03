package mrriegel.hudlibrary.worldgui;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class WorldGuiCapability {
	@CapabilityInject(IWorldGuiProvider.class)
	public static Capability<IWorldGuiProvider> cap;

	public static void register() {
		CapabilityManager.INSTANCE.register(IWorldGuiProvider.class, new IStorage<IWorldGuiProvider>() {

			@Override
			public NBTBase writeNBT(Capability<IWorldGuiProvider> capability, IWorldGuiProvider instance, EnumFacing side) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void readNBT(Capability<IWorldGuiProvider> capability, IWorldGuiProvider instance, EnumFacing side, NBTBase nbt) {
				throw new UnsupportedOperationException();
			}
		}, () -> {
			throw new UnsupportedOperationException();
		});
	}
}
