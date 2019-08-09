package kdp.hudlibrary.worldgui.message;

import net.minecraft.inventory.container.ClickType;

import io.netty.buffer.ByteBuf;

public class SlotClickMessage {

    int id, slotID, mouse;
    ClickType type;

    public SlotClickMessage() {
    }

    public SlotClickMessage(int id, int slotID, int mouse, ClickType type) {
        super();
        this.id = id;
        this.slotID = slotID;
        this.mouse = mouse;
        this.type = type;
    }

	/*public void onMessage(SlotClickMessage message, MessageContext ctx) {
		FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
			ContainerWG c = CommonEvents.getData(ctx.getServerHandler().player).containers.get(message.id);
			if (c != null) {
				c.slotClick(message.slotID, message.mouse, message.type, ctx.getServerHandler().player);
			}
		});
	}*/

    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        slotID = buf.readInt();
        mouse = buf.readByte();
        type = ClickType.values()[buf.readByte()];
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(slotID);
        buf.writeByte(mouse);
        buf.writeByte(type.ordinal());
    }

}
