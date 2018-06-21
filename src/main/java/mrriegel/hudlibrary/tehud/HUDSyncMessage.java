package mrriegel.hudlibrary.tehud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import mrriegel.hudlibrary.HUDLibrary;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
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
		int radius = 6;
		for (BlockPos p : BlockPos.getAllInBox(playerPos.add(-radius, -radius, -radius), playerPos.add(radius, radius, radius))) {
			TileEntity t = player.world.getTileEntity(p);
			if (t.hasCapability(HUDCapability.cap, null)) {
				IHUDProvider hud = t.getCapability(HUDCapability.cap, null);
				if (hud.readingSide().isServer()) {
					for (EnumFacing f : EnumFacing.HORIZONTALS) {
						for (IHUDElement e : hud.elements(player, f)) {
							map.put(new DirectionPos(p, f), e.writeSyncTag());
						}
					}
				}
			}
		}

	}

	@Override
	public IMessage onMessage(HUDSyncMessage message, MessageContext ctx) {
		Map<DirectionPos, NBTTagCompound> map = message.map;
		FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
			HUDLibrary.hudelements = map;
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
