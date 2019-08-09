package kdp.hudlibrary;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = HUDLibrary.MOD_ID)
public class CommonEvents {

	/*public static Set<UUID> openWorldGuis = new HashSet<>();
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
	}*/

}
