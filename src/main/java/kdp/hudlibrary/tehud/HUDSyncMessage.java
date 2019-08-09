package kdp.hudlibrary.tehud;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkEvent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class HUDSyncMessage {

    private static final String SIZE = "S", POS = "P", FACE = "F", TAG = "T";

    private Map<DirectionPos, Map<Integer, INBT>> map = new HashMap<>();

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
                            map.put(DirectionPos.of(t.getPos(), f), hud.getNBTData(player, f));
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException e) {
        }

    }

    public void onMessage(HUDSyncMessage message, NetworkEvent.Context ctx) {
        Map<DirectionPos, Map<Integer, INBT>> map = message.map;
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
        int size = nbt.getInt(SIZE);
        for (int i = 0; i < size; i++) {
            DirectionPos dp = DirectionPos
                    .of(BlockPos.fromLong(nbt.getLong(i + POS)), Direction.byIndex(nbt.getInt(i + FACE)));
            Int2ObjectOpenHashMap nbts = new Int2ObjectOpenHashMap();
            CompoundNBT mapNBT = nbt.getCompound(i + TAG);
            mapNBT.keySet().forEach(s -> nbts.put(Integer.valueOf(s), mapNBT.get(s)));
            map.put(dp, nbts);
        }
    }

    public void encode(ByteBuf buf) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt(SIZE, map.size());
        List<Entry<DirectionPos, Map<Integer, INBT>>> l = new ArrayList<>(map.entrySet());
        for (int i = 0; i < map.size(); i++) {
            Entry<DirectionPos, Map<Integer, INBT>> e = l.get(i);
            nbt.putLong(i + POS, e.getKey().pos.toLong());
            nbt.putInt(i + FACE, e.getKey().face.ordinal());
            CompoundNBT mapNBT = new CompoundNBT();
            Map<Integer, INBT> nbts = e.getValue();
            for (Entry<Integer, INBT> ee : nbts.entrySet()) {
                mapNBT.put("" + ee.getKey(), ee.getValue());
            }
            nbt.put(i + TAG, mapNBT);
        }
        ByteBufOutputStream bbos = new ByteBufOutputStream(buf);
        try {
            CompressedStreamTools.write(nbt, bbos);
        } catch (IOException e) {
        }
    }

}
