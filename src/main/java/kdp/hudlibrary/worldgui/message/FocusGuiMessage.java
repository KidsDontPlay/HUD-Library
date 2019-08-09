package kdp.hudlibrary.worldgui.message;

import io.netty.buffer.ByteBuf;

public class FocusGuiMessage {

    boolean openGui;

    public FocusGuiMessage() {
    }

    public FocusGuiMessage(boolean openGui) {
        this.openGui = openGui;
    }

	/*public void onMessage(FocusGuiMessage message, MessageContext ctx) {
		FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
			if (message.openGui) {
				CommonEvents.openWorldGuis.add(ctx.getServerHandler().player.getUniqueID());
			} else {
				CommonEvents.openWorldGuis.remove(ctx.getServerHandler().player.getUniqueID());
			}
		});
	}*/

    public void fromBytes(ByteBuf buf) {
        openGui = buf.readBoolean();
    }

    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(openGui);
    }

}
