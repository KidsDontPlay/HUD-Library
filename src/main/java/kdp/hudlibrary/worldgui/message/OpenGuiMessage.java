package kdp.hudlibrary.worldgui.message;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import io.netty.buffer.ByteBuf;

public class OpenGuiMessage {

    int id;
    BlockPos pos;
    Direction face;

    public OpenGuiMessage() {
    }

    public OpenGuiMessage(int id, BlockPos pos, Direction face) {
        super();
        this.id = id;
        this.pos = pos;
        this.face = face;
    }

	/*public void onMessage(OpenGuiMessage message, MessageContext ctx) {
		FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
			TileEntity tile = ctx.getServerHandler().player.world.getTileEntity(message.pos);
			if (tile != null && tile.hasCapability(WorldGuiCapability.cap, message.face)) {
				ContainerWG con = tile.getCapability(WorldGuiCapability.cap, message.face).getContainer(ctx.getServerHandler().player, message.pos);
				if (con != null) {
					con.id = message.id;
					CommonEvents.getData(ctx.getServerHandler().player).containers.put(message.id, con);
				}
			}
		});
	}*/

    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        pos = BlockPos.fromLong(buf.readLong());
        face = Direction.byIndex(buf.readByte());
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        buf.writeLong(pos.toLong());
        buf.writeByte(face.ordinal());
    }

}
