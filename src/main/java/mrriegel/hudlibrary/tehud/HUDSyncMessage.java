package mrriegel.hudlibrary.tehud;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class HUDSyncMessage implements IMessage, IMessageHandler<HUDSyncMessage, IMessage> {

	private Map<DirectionPos, NBTTagCompound> map = new HashMap<>();

	public HUDSyncMessage() {
	}

	public HUDSyncMessage(EntityPlayer player, int radius) {
		try {
			//		for (BlockPos p : BlockPos.getAllInBox(playerPos.add(-radius, -radius, -radius), playerPos.add(radius, radius, radius))) {
			//			TileEntity t = player.world.getTileEntity(p);
			for (TileEntity t : player.world.loadedTileEntityList) {
				if (t != null && t.hasCapability(HUDCapability.cap, null) && player.getPositionVector().distanceTo(new Vec3d(t.getPos()).addVector(.5, 0, .5)) < radius) {
					IHUDProvider hud = t.getCapability(HUDCapability.cap, null);
					if (hud.readingSide().isServer() && hud.needsSync()) {
						for (EnumFacing f : EnumFacing.HORIZONTALS) {
							NBTTagCompound n = new NBTTagCompound();
							NBTTagList lis = new NBTTagList();
							Map<Integer, Function<TileEntity, NBTTagCompound>> nbts = hud.getNBTData(player, f);
							for (Entry<Integer, Function<TileEntity, NBTTagCompound>> e : nbts.entrySet()) {
								int index = e.getKey();
								while (lis.tagCount() <= index)
									lis.appendTag(new NBTTagCompound());
								lis.set(index, e.getValue().apply(t));
							}
							//						for (Function<TileEntity, NBTTagCompound> e : hud.getNBTData(player, f)) {
							//							NBTTagCompound nn = e.apply(t);
							//							lis.appendTag(nn != null ? nn : new NBTTagCompound());
							//						}
							n.setTag("list", lis);
							map.put(new DirectionPos(t.getPos(), f), n);
						}
					}
				}
			}
		} catch (ConcurrentModificationException e) {
		}

	}

	@Override
	public IMessage onMessage(HUDSyncMessage message, MessageContext ctx) {
		Map<DirectionPos, NBTTagCompound> map = message.map;
		FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
			ClientEvents.hudelements.putAll(map);
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
