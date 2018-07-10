package mrriegel.hudlibrary.worldgui.message;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import mrriegel.hudlibrary.CommonEvents;
import mrriegel.hudlibrary.worldgui.ContainerWG;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncContainerToClientMessage implements IMessage, IMessageHandler<SyncContainerToClientMessage, IMessage> {

	int id;
	Map<Integer, ItemStack> map;

	public SyncContainerToClientMessage() {
	}

	public SyncContainerToClientMessage(int id, Map<Integer, ItemStack> map) {
		super();
		this.id = id;
		this.map = map;
	}

	@Override
	public IMessage onMessage(SyncContainerToClientMessage message, MessageContext ctx) {
		FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
			ContainerWG c = CommonEvents.getData(FMLClientHandler.instance().getClientPlayerEntity()).containers.get(message.id);
			if (c != null) {
				for (Entry<Integer, ItemStack> e : message.map.entrySet()) {
					c.putStackInSlot(e.getKey(), e.getValue());
				}
				//				System.out.println(CommonEvents.getData(FMLClientHandler.instance().getClientPlayerEntity()).containers);
				//				System.out.println(c.inventorySlots.get(1).getStack());
			}
		});
		return null;
	}

	@Override
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

	@Override
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

	}

}
