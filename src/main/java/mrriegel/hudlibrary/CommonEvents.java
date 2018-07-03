package mrriegel.hudlibrary;

import mrriegel.hudlibrary.tehud.HUDSyncMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

@EventBusSubscriber(modid = HUDLibrary.MODID)
public class CommonEvents {

	@SubscribeEvent
	public static void tick(PlayerTickEvent event) {
		if (event.side.isServer() && event.phase == Phase.END) {
			if (event.player.ticksExisted % 7 == 0) {
				HUDLibrary.snw.sendTo(new HUDSyncMessage(event.player, 7), (EntityPlayerMP) event.player);
			}
		}
	}

	@SubscribeEvent
	public static void join(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityPlayer && !event.getWorld().isRemote) {
			HUDLibrary.snw.sendTo(new HUDSyncMessage((EntityPlayer) event.getEntity(), 7), (EntityPlayerMP) event.getEntity());
		}
	}

}
