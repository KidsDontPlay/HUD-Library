package mrriegel.hudlibrary.worldgui.message;

import io.netty.buffer.ByteBuf;
import mrriegel.hudlibrary.CommonEvents;
import mrriegel.hudlibrary.HUDLibrary;
import mrriegel.hudlibrary.worldgui.WorldGuiContainer;
import net.minecraft.inventory.ClickType;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SlotClickMessage implements IMessage, IMessageHandler<SlotClickMessage, IMessage> {

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

	@Override
	public IMessage onMessage(SlotClickMessage message, MessageContext ctx) {
		FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
			WorldGuiContainer c = CommonEvents.getData(ctx.getServerHandler().player).containers.get(message.id);
			if (c != null) {
				c.slotClick(message.slotID, message.mouse, message.type, ctx.getServerHandler().player);
			}
		});
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readInt();
		slotID = buf.readInt();
		mouse = buf.readByte();
		type = ClickType.values()[buf.readByte()];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(slotID);
		buf.writeByte(mouse);
		buf.writeByte(type.ordinal());
	}

}
