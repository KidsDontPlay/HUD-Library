package kdp.hudlibrary.worldgui;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class ContainerWG extends Container {

    public int id;
    protected PlayerEntity player;
    private final NonNullList<ItemStack> inventoryItemStacks = NonNullList.create();

    public ContainerWG(@Nullable ContainerType<?> type, int id, PlayerEntity player) {
        super(type, id);
        this.player = player;
    }

    @Override
    protected Slot addSlot(Slot slotIn) {
        inventoryItemStacks.add(ItemStack.EMPTY);
        return super.addSlot(slotIn);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public void detectAndSendChanges() {
        Int2ObjectOpenHashMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            ItemStack itemstack = this.inventorySlots.get(i).getStack();
            ItemStack itemstack1 = this.inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
                boolean clientStackChanged = !itemstack1.equals(itemstack, true);
                itemstack1 = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack.copy();
                this.inventoryItemStacks.set(i, itemstack1);

                if (clientStackChanged) {
					/*for (int j = 0; j < this.listeners.size(); ++j) {
						this.listeners.get(j).sendSlotContents(this, i, itemstack1);
					}*/
                    if (player instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity) player).sendSlotContents(this, i, itemstack1);
                    }
                }
                map.put(i, itemstack1);
            }
        }
        if (!player.world.isRemote && !map.isEmpty()) {
            //HUDLibrary.snw.sendTo(new SyncContainerToClientMessage(id, map), (PlayerEntityMP) player);
        }
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        player.openContainer.detectAndSendChanges();
    }

}
