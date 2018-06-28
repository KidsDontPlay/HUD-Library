package mrriegel.hudlibrary.tehud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import io.netty.buffer.ByteBuf;
import mrriegel.hudlibrary.ClientEvents;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class HUDSyncMessage implements IMessage, IMessageHandler<HUDSyncMessage, IMessage> {

	private Map<DirectionPos, NBTTagCompound> map = new HashMap<>();

	public HUDSyncMessage() {
	}

	public HUDSyncMessage(EntityPlayer player) {
		BlockPos playerPos = new BlockPos(player);
		int radius = 7;
		for (BlockPos p : BlockPos.getAllInBox(playerPos.add(-radius, -radius, -radius), playerPos.add(radius, radius, radius))) {
			TileEntity t = player.world.getTileEntity(p);
			if (t != null && t.hasCapability(HUDCapability.cap, null)) {
				IHUDProvider hud = t.getCapability(HUDCapability.cap, null);
				if (hud.readingSide().isServer() && hud.needsSync()) {
					for (EnumFacing f : EnumFacing.HORIZONTALS) {
						NBTTagCompound n = new NBTTagCompound();
						NBTTagList lis = new NBTTagList();
						for (Function<TileEntity, NBTTagCompound> e : hud.getNBTData(player, f)) {
							NBTTagCompound nn = e.apply(t);
							lis.appendTag(nn != null ? nn : new NBTTagCompound());
						}
						n.setTag("list", lis);
						map.put(new DirectionPos(p, f), n);
					}
				}
			}
		}

	}

	@Override
	public IMessage onMessage(HUDSyncMessage message, MessageContext ctx) {
		Map<DirectionPos, NBTTagCompound> map = message.map;
		FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
			for (Entry<DirectionPos, NBTTagCompound> e : ClientEvents.hudelements.entrySet()) {
				ClientEvents.lasthudelements.put(e.getKey(), e.getValue().copy());
			}
			ClientEvents.hudelements = map;
		});
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		map = new HashMap<>();
		NBTTagCompound nbt = ByteBufUtils.readTag(buf);
		int size = nbt.getInteger("size");
		for (int i = 0; i < size; i++) {
			DirectionPos dp = new DirectionPos(BlockPos.fromLong(nbt.getLong(i + "pos")), EnumFacing.getFront(nbt.getInteger(i + "face")));
			map.put(dp, nbt.getCompoundTag(i + "tag"));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("size", map.size());
		List<Entry<DirectionPos, NBTTagCompound>> l = new ArrayList<>(map.entrySet());
		for (int i = 0; i < map.size(); i++) {
			Entry<DirectionPos, NBTTagCompound> e = l.get(i);
			nbt.setLong(i + "pos", e.getKey().pos.toLong());
			nbt.setInteger(i + "face", e.getKey().face.ordinal());
			nbt.setTag(i + "tag", e.getValue());
		}
		ByteBufUtils.writeTag(buf, nbt);
	}

}
