package kdp.hudlibrary.worldgui.message;

import io.netty.buffer.ByteBuf;

public class CloseGuiMessage {

    int id;

    public CloseGuiMessage() {
    }

    public CloseGuiMessage(int id) {
        super();
        this.id = id;
    }

	/*public void onMessage(CloseGuiMessage message, MessageContext ctx) {
		FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
			ContainerWG con = CommonEvents.getData(ctx.getServerHandler().player).containers.get(message.id);
			if (con != null)
				con.onContainerClosed(ctx.getServerHandler().player);
			CommonEvents.getData(ctx.getServerHandler().player).containers.remove(message.id);
			FocusGuiMessage m = new FocusGuiMessage(false);
			m.onMessage(m, ctx);
		});
	}*/

    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
    }

}
