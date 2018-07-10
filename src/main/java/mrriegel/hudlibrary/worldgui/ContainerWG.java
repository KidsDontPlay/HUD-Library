package mrriegel.hudlibrary.worldgui;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mrriegel.hudlibrary.HUDLibrary;
import mrriegel.hudlibrary.worldgui.message.SyncContainerToClientMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public class ContainerWG extends Container {

	public int id;
	protected EntityPlayer player;

	public ContainerWG(EntityPlayer player) {
		super();
		this.player = player;
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

	@Override
	public void detectAndSendChanges() {
		Int2ObjectOpenHashMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		for (int i = 0; i < this.inventorySlots.size(); ++i) {
			ItemStack itemstack = this.inventorySlots.get(i).getStack();
			ItemStack itemstack1 = this.inventoryItemStacks.get(i);

			if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
				boolean clientStackChanged = !ItemStack.areItemStacksEqualUsingNBTShareTag(itemstack1, itemstack);
				itemstack1 = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack.copy();
				this.inventoryItemStacks.set(i, itemstack1);

				if (clientStackChanged) {
					for (int j = 0; j < this.listeners.size(); ++j) {
						this.listeners.get(j).sendSlotContents(this, i, itemstack1);
					}
				}
				map.put(i, itemstack1);
			}
		}
		if (!player.world.isRemote && !map.isEmpty()) {
			HUDLibrary.snw.sendTo(new SyncContainerToClientMessage(id, map), (EntityPlayerMP) player);
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		player.openContainer.detectAndSendChanges();
	}

}
