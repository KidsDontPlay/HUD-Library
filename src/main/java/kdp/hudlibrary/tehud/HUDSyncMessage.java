package kdp.hudlibrary.tehud;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.network.NetworkEvent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

public class HUDSyncMessage {

    private static final String SIZE = "S", POS = "P", FACE = "F", TAG = "T";

    private Map<DirectionPos, CompoundNBT> map = new HashMap<>();

    public HUDSyncMessage() {
    }

    public HUDSyncMessage(PlayerEntity player, int radius) {
        try {
            for (TileEntity t : player.world.loadedTileEntityList) {
                IHUDProvider hud = t.getCapability(HUDCapability.cap).orElse(null);
                if (hud != null && hud.usesServerData() && hud.needsSync() && player.getPositionVector()
                        .squareDistanceTo(new Vec3d(t.getPos())) < radius * radius) {
                    for (Direction f : Direction.values()) {
                        if (f.getAxis() == Direction.Axis.Y) {
                            continue;
                        }
                        map.put(DirectionPos.of(t.getPos(), f),
                                Objects.requireNonNull(hud.getNBTData(player, f),
                                        "IHUDProvider#getNBTData must not return null."));
                    }
                }
            }
        } catch (ConcurrentModificationException e) {
        }

    }

    public void onMessage(HUDSyncMessage message, NetworkEvent.Context ctx) {
        Map<DirectionPos, CompoundNBT> map = message.map;
        ctx.enqueueWork(() -> HUDRenderer.hudElements.putAll(map));
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
            map.put(dp, nbt.getCompound(i + TAG));
        }
    }

    public void encode(ByteBuf buf) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt(SIZE, map.size());
        List<Entry<DirectionPos, CompoundNBT>> l = new ArrayList<>(map.entrySet());
        for (int i = 0; i < map.size(); i++) {
            Entry<DirectionPos, CompoundNBT> e = l.get(i);
            nbt.putLong(i + POS, e.getKey().pos.toLong());
            nbt.putInt(i + FACE, e.getKey().face.ordinal());
            nbt.put(i + TAG, e.getValue());
        }
        ByteBufOutputStream bbos = new ByteBufOutputStream(buf);
        try {
            CompressedStreamTools.write(nbt, bbos);
        } catch (IOException e) {
        }
    }

}
