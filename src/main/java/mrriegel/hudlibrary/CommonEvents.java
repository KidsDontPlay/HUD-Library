package mrriegel.hudlibrary;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import mrriegel.hudlibrary.tehud.HUDSyncMessage;
import mrriegel.hudlibrary.worldgui.ContainerWG;
import mrriegel.hudlibrary.worldgui.PlayerData;
import mrriegel.hudlibrary.worldgui.PlayerSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

@EventBusSubscriber(modid = HUDLibrary.MODID)
public class CommonEvents {

	public static Set<UUID> openWorldGuis = new HashSet<>();
	private static Map<ByteList, PlayerData> playerDatas = new HashMap<>();

	public static PlayerData getData(EntityPlayer player) {
		ByteList bytes = playerID(player);
		PlayerData data = playerDatas.get(bytes);
		if (data != null)
			return data;
		playerDatas.put(bytes, data = new PlayerData());
		return data;
	}

	public static boolean hasFocusedGui(EntityPlayer player) {
		return player.world.isRemote ? PlayerSettings.INSTANCE.focusedGui != null : openWorldGuis.contains(player.getUniqueID());
	}

	private static ByteList playerID(EntityPlayer player) {
		UUID u = player.getUniqueID();
		ByteBuffer bb = ByteBuffer.wrap(new byte[17]);
		bb.putLong(u.getLeastSignificantBits());
		bb.putLong(u.getMostSignificantBits());
		bb.put((byte) (player.world.isRemote ? 0 : 1));
		return new ByteArrayList(bb.array());
	}

	@SubscribeEvent
	public static void tick(PlayerTickEvent event) {
		if (event.side.isServer() && event.phase == Phase.END) {
			if (event.player.ticksExisted % 7 == 0) {
				HUDLibrary.snw.sendTo(new HUDSyncMessage(event.player, 7), (EntityPlayerMP) event.player);
			}
			PlayerData data = getData(event.player);
			Collection<Integer> toRemove = new ArrayList<>();
			for (ContainerWG c : data.containers.values()) {
				if (c.canInteractWith(event.player))
					c.detectAndSendChanges();
				else
					toRemove.add(c.id);
			}
			//			for (int id : toRemove) {
			//				CloseGuiMessage m=new CloseGuiMessage(id);
			//				m.onMessage(m, ctx)
			//			}
		}
	}

	@SubscribeEvent
	public static void join(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof EntityPlayer && !event.getWorld().isRemote) {
			HUDLibrary.snw.sendTo(new HUDSyncMessage((EntityPlayer) event.getEntity(), 7), (EntityPlayerMP) event.getEntity());
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void interact(PlayerInteractEvent event) {
		if (hasFocusedGui(event.getEntityPlayer())) {
			if (event.isCancelable())
				event.setCanceled(true);
			event.setResult(Result.DENY);
			if (event instanceof RightClickBlock) {
				((RightClickBlock) event).setUseBlock(Result.DENY);
				((RightClickBlock) event).setUseItem(Result.DENY);
			} else if (event instanceof LeftClickBlock) {
				((LeftClickBlock) event).setUseBlock(Result.DENY);
				((LeftClickBlock) event).setUseItem(Result.DENY);
			}
		}
	}

	@SubscribeEvent
	public static void load(ChunkEvent.Load event) {
//		event.getChunk().getTileEntityMap().values().stream().filter(t -> t != null && t.hasCapability(HUDCapability.cap, null)).forEach(HUDCapability.hudTiles::add);
	}

	@SubscribeEvent
	public static void unload(ChunkEvent.Unload event) {
//		event.getChunk().getTileEntityMap().values().stream().filter(t -> t != null && t.hasCapability(HUDCapability.cap, null)).forEach(HUDCapability.hudTiles::remove);
	}

}
