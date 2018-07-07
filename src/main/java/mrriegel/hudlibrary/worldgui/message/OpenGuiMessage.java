package mrriegel.hudlibrary.worldgui.message;

import io.netty.buffer.ByteBuf;
import mrriegel.hudlibrary.CommonEvents;
import mrriegel.hudlibrary.worldgui.WorldGuiCapability;
import mrriegel.hudlibrary.worldgui.WorldGuiContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class OpenGuiMessage implements IMessage, IMessageHandler<OpenGuiMessage, IMessage> {

	int id;
	BlockPos pos;
	EnumFacing face;

	public OpenGuiMessage() {
	}

	public OpenGuiMessage(int id, BlockPos pos, EnumFacing face) {
		super();
		this.id = id;
		this.pos = pos;
		this.face = face;
	}

	@Override
	public IMessage onMessage(OpenGuiMessage message, MessageContext ctx) {
		FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
			TileEntity tile = ctx.getServerHandler().player.world.getTileEntity(message.pos);
			if (tile != null && tile.hasCapability(WorldGuiCapability.cap, message.face)) {
				WorldGuiContainer con = tile.getCapability(WorldGuiCapability.cap, message.face).getContainer(ctx.getServerHandler().player, message.pos);
				if (con != null) {
					con.id = message.id;
					CommonEvents.getData(ctx.getServerHandler().player).containers.put(message.id, con);
				}
			}
		});
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readInt();
		pos = BlockPos.fromLong(buf.readLong());
		face = EnumFacing.getFront(buf.readByte());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeLong(pos.toLong());
		buf.writeByte(face.ordinal());
	}

}
