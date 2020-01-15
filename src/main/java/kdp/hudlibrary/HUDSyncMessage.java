package kdp.hudlibrary;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.CapabilityItemHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import kdp.hudlibrary.api.IHUDProvider;
import kdp.hudlibrary.util.DirectionPos;

public class HUDSyncMessage {

    private static final String SIZE = "S", POS = "P", FACE = "F", TAG = "T";

    private Map<DirectionPos, CompoundNBT> map = new HashMap<>();

    public HUDSyncMessage() {
    }

    public HUDSyncMessage(PlayerEntity player, int radius) {
        try {
            for (TileEntity t : player.world.loadedTileEntityList) {
                IHUDProvider hud = t.getCapability(HUDCapability.cap).orElse(null);
                if (hud != null && hud.usesServerData() /*&& hud.needsSync()*/ && player.getPositionVector()
                        .squareDistanceTo(new Vec3d(t.getPos())) < radius * radius) {
                    for (Direction f : Direction.values()) {
                        if (f.getAxis() == Direction.Axis.Y) {
                            continue;
                        }
                        map.put(DirectionPos.of(t.getPos(), f), Objects.requireNonNull(hud.getNBTData(player, f),
                                "IHUDProvider#getNBTData must not return null."));
                    }
                }
                if (hud == null) {
                    CompoundNBT nbt = new CompoundNBT();
                    t.getCapability(CapabilityEnergy.ENERGY).ifPresent(e -> {
                        nbt.putInt("e", e.getEnergyStored());
                        nbt.putInt("eMax", e.getMaxEnergyStored());
                    });
                    t.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(f -> {
                        ListNBT fs = new ListNBT();
                        IntStream.range(0, f.getTanks()).forEach(i -> {
                            CompoundNBT n = new CompoundNBT();
                            if (!f.getFluidInTank(i).isEmpty()) {
                                n.put("f", f.getFluidInTank(i).writeToNBT(new CompoundNBT()));
                                n.putInt("m", f.getTankCapacity(i));
                                fs.add(n);
                            }
                        });
                        if (!fs.isEmpty()) {
                            nbt.put("fs", fs);
                        }
                    });
                    t.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(i -> {
                        ListNBT is = IntStream.range(0, i.getSlots()).mapToObj(i::getStackInSlot)
                                .filter(s -> !s.isEmpty()).map(s -> s.write(new CompoundNBT()))
                                .collect(Collectors.toCollection(ListNBT::new));
                        if (!is.isEmpty()) {
                            nbt.put("is", is);
                        }
                    });

                    if (!nbt.isEmpty()) {
                        nbt.putBoolean("defauld", true);
                        map.put(DirectionPos.of(t.getPos(), Direction.UP), nbt);
                    }
                }
            }
        } catch (ConcurrentModificationException e) {
        }

    }

    public void onMessage(HUDSyncMessage message, NetworkEvent.Context ctx) {
        Map<DirectionPos, CompoundNBT> map = message.map;
        Map<DirectionPos, CompoundNBT> synced = map.entrySet().stream().filter(e -> !e.getValue().getBoolean("defauld"))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        Map<BlockPos, CompoundNBT> defaults = map.entrySet().stream().filter(e -> !synced.containsKey(e.getKey()))
                .collect(Collectors.toMap(k -> k.getKey().pos, Entry::getValue));
        ctx.enqueueWork(() -> {
            HUDRenderer.syncedNBTs.putAll(synced);
            HUDRenderer.defaultNBTs.putAll(defaults);
        });
        ctx.setPacketHandled(true);
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
