package kdp.hudlibrary;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = HUDLibrary.MOD_ID)
public class CommonEvents {

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER && event.player.ticksExisted % 10 == 0) {
            HUDLibrary.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.player),
                    new HUDSyncMessage(event.player, 18));
        }
    }

    @SubscribeEvent
    public static void join(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof PlayerEntity && !event.getWorld().isRemote) {
            HUDLibrary.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getEntity()),
                    new HUDSyncMessage((PlayerEntity) event.getEntity(), 18));
        }
    }
}
