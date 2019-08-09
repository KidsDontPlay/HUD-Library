package kdp.hudlibrary.worldgui.message;

import java.util.Map;

import net.minecraft.item.ItemStack;

public class SyncContainerToClientMessage {

    int id;
    Map<Integer, ItemStack> map;

    public SyncContainerToClientMessage() {
    }

    public SyncContainerToClientMessage(int id, Map<Integer, ItemStack> map) {
        super();
        this.id = id;
        this.map = map;
    }

	/*public IMessage onMessage(SyncContainerToClientMessage message, MessageContext ctx) {
		EntityPlayer p = FMLClientHandler.instance().getClientPlayerEntity();
		FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
			ContainerWG c = CommonEvents.getData(p).containers.get(message.id);
			if (c != null) {
				for (Entry<Integer, ItemStack> e : message.map.entrySet()) {
					c.putStackInSlot(e.getKey(), e.getValue());
				}
			}
		});
		return null;
	}

	public void fromBytes(ByteBuf buf) {
		id = buf.readInt();
		NBTTagCompound nbt = ByteBufUtils.readTag(buf);
		int[] l1 = nbt.getIntArray("l1");
		NBTTagList l2 = nbt.getTagList("l2", 10);
		map = new HashMap<>();
		for (int i = 0; i < l1.length; i++) {
			map.put(l1[i], new ItemStack((NBTTagCompound) l2.get(i)));
		}
	}

	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
		IntList l1 = new IntArrayList();
		NBTTagList l2 = new NBTTagList();
		for (Entry<Integer, ItemStack> e : map.entrySet()) {
			l1.add(e.getKey());
			l2.appendTag(e.getValue().writeToNBT(new NBTTagCompound()));
		}
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setIntArray("l1", l1.toIntArray());
		nbt.setTag("l2", l2);
		ByteBufUtils.writeTag(buf, nbt);

	}*/

}
