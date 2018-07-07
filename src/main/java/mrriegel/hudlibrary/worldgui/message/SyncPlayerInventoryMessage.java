package mrriegel.hudlibrary.worldgui.message;

import io.netty.buffer.ByteBuf;
import mrriegel.hudlibrary.HUDLibrary;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SyncPlayerInventoryMessage implements IMessage, IMessageHandler<SyncPlayerInventoryMessage, IMessage> {

	NBTTagCompound nbt;

	public SyncPlayerInventoryMessage() {
	}

	public SyncPlayerInventoryMessage(EntityPlayer player) {
		super();
		nbt = new NBTTagCompound();
		nbt.setTag("list", player.inventory.writeToNBT(new NBTTagList()));
	}

	@Override
	public IMessage onMessage(SyncPlayerInventoryMessage message, MessageContext ctx) {
		FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
			ctx.getServerHandler().player.inventory.readFromNBT(message.nbt.getTagList("list", 10));
			ctx.getServerHandler().player.openContainer.detectAndSendChanges();
		});
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		nbt = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, nbt);
	}

}
