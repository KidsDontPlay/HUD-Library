package mrriegel.hudlibrary.worldgui.message;

import io.netty.buffer.ByteBuf;
import mrriegel.hudlibrary.CommonEvents;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class FocusGuiMessage implements IMessage, IMessageHandler<FocusGuiMessage, IMessage> {

	boolean openGui;

	public FocusGuiMessage() {
	}

	public FocusGuiMessage(boolean openGui) {
		this.openGui = openGui;
	}

	@Override
	public IMessage onMessage(FocusGuiMessage message, MessageContext ctx) {
		FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
			if (message.openGui) {
				CommonEvents.openWorldGuis.add(ctx.getServerHandler().player.getUniqueID());
			} else {
				CommonEvents.openWorldGuis.remove(ctx.getServerHandler().player.getUniqueID());
			}
		});
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		openGui = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(openGui);
	}

}
