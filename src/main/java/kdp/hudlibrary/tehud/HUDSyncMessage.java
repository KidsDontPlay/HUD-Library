package kdp.hudlibrary.tehud;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkEvent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

public class HUDSyncMessage {

    private Map<DirectionPos, CompoundNBT> map = new HashMap<>();

    public HUDSyncMessage() {
    }

    public HUDSyncMessage(PlayerEntity player, int radius) {
        try {
            for (TileEntity t : player.world.loadedTileEntityList) {
                LazyOptional<IHUDProvider> hudOp = t.getCapability(HUDCapability.cap);
                if (hudOp.isPresent() && player.getPositionVector()
                        .squareDistanceTo(new Vec3d(t.getPos())) < radius * radius) {
                    IHUDProvider hud = hudOp.orElse(null);
                    if (hud.readingSide().isServer() && hud.needsSync()) {
                        for (Direction f : Direction.values()) {
                            if (f.getAxis() == Direction.Axis.Y) {
                                continue;
                            }
                            CompoundNBT n = new CompoundNBT();
                            ListNBT lis = new ListNBT();
                            Map<Integer, Function<TileEntity, CompoundNBT>> nbts = hud.getNBTData(player, f);
                            for (Entry<Integer, Function<TileEntity, CompoundNBT>> e : nbts.entrySet()) {
                                int index = e.getKey();
                                while (lis.size() <= index)
                                    lis.add(new CompoundNBT());
                                lis.set(index, e.getValue().apply(t));
                            }
                            //						for (Function<TileEntity, CompoundNBT> e : hud.getNBTData(player, f)) {
                            //							CompoundNBT nn = e.apply(t);
                            //							lis.appendTag(nn != null ? nn : new CompoundNBT());
                            //						}
                            n.put("list", lis);
                            map.put(DirectionPos.of(t.getPos(), f), n);
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException e) {
        }

    }

    public void onMessage(HUDSyncMessage message, NetworkEvent.Context ctx) {
        Map<DirectionPos, CompoundNBT> map = message.map;
        ctx.enqueueWork(() -> HudRenderer.hudElements.putAll(map));
    }

    public void decode(ByteBuf buf) {
        map = new HashMap<>();
        ByteBufInputStream bbis = new ByteBufInputStream(buf);
        DataInputStream dis = new DataInputStream(bbis);
        CompoundNBT nbt = null;
        try {
            nbt = CompressedStreamTools.read(dis);
        } catch (IOException e) {
        }
        int size = nbt.getInt("size");
        for (int i = 0; i < size; i++) {
            DirectionPos dp = DirectionPos
                    .of(BlockPos.fromLong(nbt.getLong(i + "pos")), Direction.byIndex(nbt.getInt(i + "face")));
            map.put(dp, nbt.getCompound(i + "tag"));
        }
    }

    public void encode(ByteBuf buf) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("size", map.size());
        List<Entry<DirectionPos, CompoundNBT>> l = new ArrayList<>(map.entrySet());
        for (int i = 0; i < map.size(); i++) {
            Entry<DirectionPos, CompoundNBT> e = l.get(i);
            nbt.putLong(i + "pos", e.getKey().pos.toLong());
            nbt.putInt(i + "face", e.getKey().face.ordinal());
            nbt.put(i + "tag", e.getValue());
        }
        ByteBufOutputStream bbos = new ByteBufOutputStream(buf);
        try {
            CompressedStreamTools.write(nbt, bbos);
        } catch (IOException e) {
        }
    }

}
