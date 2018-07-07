package mrriegel.hudlibrary.worldgui.message;

import io.netty.buffer.ByteBuf;
import mrriegel.hudlibrary.CommonEvents;
import mrriegel.hudlibrary.HUDLibrary;
import mrriegel.hudlibrary.worldgui.WorldGuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CloseGuiMessage implements IMessage, IMessageHandler<CloseGuiMessage, IMessage> {

	int id;

	public CloseGuiMessage() {
	}

	public CloseGuiMessage(int id) {
		super();
		this.id = id;
	}

	@Override
	public IMessage onMessage(CloseGuiMessage message, MessageContext ctx) {
		FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
			WorldGuiContainer con=CommonEvents.getData(ctx.getServerHandler().player).containers.get(message.id);
			if(con!=null)
				con.onContainerClosed(ctx.getServerHandler().player);
			CommonEvents.getData(ctx.getServerHandler().player).containers.remove(message.id);
			HUDLibrary.drop(ctx.getServerHandler().player);
		});
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
	}

}
