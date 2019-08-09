package kdp.hudlibrary;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = HUDLibrary.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    //public static final KeyBinding OPENWORLDGUI = new KeyBinding("Open world gui", KeyConflictContext.IN_GAME, Keyboard.KEY_O, HUDLibrary.MOD_ID);

	/*@SubscribeEvent
	public static void tick(ClientTickEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if (event.phase == Phase.END && mc != null && mc.player != null) {
			if (PlayerSettings.INSTANCE.focusedGui != null && !PlayerSettings.INSTANCE.focusedGui.isFocused()) {
				PlayerSettings.INSTANCE.focusedGui.onMouseLeave();
				PlayerSettings.INSTANCE.focusedGui = null;
				HUDLibrary.snw.sendToServer(new FocusGuiMessage(false));
			}
			for (WorldGui openGui : PlayerSettings.INSTANCE.guis) {
				openGui.update();
				if (PlayerSettings.INSTANCE.focusedGui == null && openGui.isFocused()) {
					PlayerSettings.INSTANCE.focusedGui = openGui;
					openGui.onMouseEnter();
					HUDLibrary.snw.sendToServer(new FocusGuiMessage(true));
				}
			}
		}
	}

	@SubscribeEvent
	public static void click(InputEvent.MouseInputEvent event) {
		if (PlayerSettings.INSTANCE.focusedGui != null) {
			WorldGui openGui = PlayerSettings.INSTANCE.focusedGui;
			int button = Mouse.getButtonCount();
			if (button == 0 || button == 1)
				if (Mouse.getEventButtonState())
					openGui.click(button, (int) ((openGui.width / openGui.maxU) * openGui.u), (int) ((openGui.height / openGui.maxV) * openGui.v));
				else
					openGui.release(button, (int) ((openGui.width / openGui.maxU) * openGui.u), (int) ((openGui.height / openGui.maxV) * openGui.v));
		}
	}

	@SubscribeEvent
	public static void mouse(MouseEvent event) {
		if (!event.isCanceled() && PlayerSettings.INSTANCE.focusedGui != null && event.getDwheel() != 0 && Minecraft.getMinecraft().player.isSneaking()) {
			WorldGui openGui = PlayerSettings.INSTANCE.focusedGui;
			double scale = PlayerSettings.INSTANCE.scaleMap.getDouble(openGui.getClass());
			if (event.getDwheel() > 0) {
				scale += .00025;
				openGui.init();
			} else if (event.getDwheel() < 0) {
				scale -= .00025;
				openGui.init();
			}
			scale = MathHelper.clamp(scale, .002, .02);
			PlayerSettings.INSTANCE.scaleMap.put(openGui.getClass(), scale);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void render2(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		Vec3d pp = mc.player.getPositionEyes(event.getPartialTicks());
		Vec3d see = mc.player.getLook(event.getPartialTicks());
		PlayerSettings.INSTANCE.guis.stream().filter(g -> g.isInFront() && pp.distanceTo(g.guiPos) < g.maxRenderDistance()).sorted((b, a) -> {
			return Double.compare(pp.distanceTo(a.guiPos), pp.distanceTo(b.guiPos));
		}).peek(openGui -> {
			if (!openGui.isReachable()) {
				openGui.u = openGui.v = openGui.maxU = openGui.maxV = Integer.MAX_VALUE;
				return;
			}
			Vec3d d2 = openGui.b.subtract(openGui.a);
			Vec3d d3 = openGui.d.subtract(openGui.a);
			Vec3d n = d2.crossProduct(d3);
			Vec3d dr = mc.player.getLook(event.getPartialTicks());
			double ndot = n.dotProduct(dr);
			if (Math.abs(ndot) >= 1e-6d) {
				double t = -n.dotProduct(mc.player.getPositionEyes(event.getPartialTicks()).subtract(openGui.a)) / ndot;
				Vec3d m = mc.player.getPositionEyes(event.getPartialTicks()).add(dr.scale(t));
				Vec3d dm = m.subtract(openGui.a);
				openGui.u = dm.dotProduct(d2);
				openGui.v = dm.dotProduct(d3);
				openGui.maxU = d2.dotProduct(d2);
				openGui.maxV = d3.dotProduct(d3);
			}
		}).forEachOrdered(openGui -> {
			double angle = Math.toDegrees(Math.acos(see.dotProduct(openGui.guiPos.subtract(pp).normalize())));
			if (angle > 100)
				return;
			double x = openGui.guiPos.x - TileEntityRendererDispatcher.staticPlayerX;
			double y = openGui.guiPos.y - TileEntityRendererDispatcher.staticPlayerY;
			double z = openGui.guiPos.z - TileEntityRendererDispatcher.staticPlayerZ;
			GlStateManager.pushMatrix();
			GlStateManager.depthMask(false);
			GlStateManager.translate(x, y, z);
			double scale = PlayerSettings.INSTANCE.scaleMap.getDouble(openGui.getClass());
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.rotate(-MathHelper.wrapDegrees(openGui.yaw), 0, 1, 0);
			GlStateManager.rotate(openGui.pitch, 1, 0, 0);
			GlStateManager.rotate(180f, 0, 0, 1);
			double halfWidth = openGui.width / 2d, halfHeight = openGui.height / 2d;
			GlStateManager.translate(-halfWidth, -halfHeight, 0);
			GlStateManager.disableLighting();
			if (openGui == PlayerSettings.INSTANCE.focusedGui) {
				double k = Math.sin((mc.player.ticksExisted + event.getPartialTicks()) / 7) / 2. + .5;
				int ticks = (int) (k * 255);
				int t = ticks % 255;
				int color = (0x88 << 24) | (t << 16) | (t << 8) | (t);
				GuiUtils.drawGradientRect(0, -2, -4, openGui.width + 2, -2, color, color);
				GuiUtils.drawGradientRect(0, -2, openGui.height + 2, openGui.width + 2, openGui.height + 4, color, color);
				GuiUtils.drawGradientRect(0, -4, -2, -2, openGui.height + 2, color, color);
				GuiUtils.drawGradientRect(0, openGui.width + 2, -2, openGui.width + 4, openGui.height + 2, color, color);
			}
			int mouseX = (int) ((openGui.width / openGui.maxU) * openGui.u), mouseY = (int) ((openGui.height / openGui.maxV) * openGui.v);
			openGui.draw(mouseX, mouseY, event.getPartialTicks());
			if (openGui.isFocused()) {
				mc.getTextureManager().bindTexture(crosshairTex);
				GlStateManager.enableBlend();
				GlStateManager.color(1f, 1f, 1f, .6f);
				GlStateManager.disableLighting();
				GuiUtils.drawTexturedModalRect(mouseX, mouseY, 0, 0, 16, 16, 0);
			}
			GlStateManager.depthMask(true);
			GlStateManager.popMatrix();
		});
	}

	@SubscribeEvent
	public static void openGui(GuiOpenEvent event) {
		if (!event.isCanceled() && event.getGui() instanceof GuiIngameMenu && Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) && PlayerSettings.INSTANCE.focusedGui != null) {
			PlayerSettings.INSTANCE.focusedGui.close();
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void open(InputEvent.KeyInputEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if (OPENWORLDGUI.isPressed() && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == Type.BLOCK && mc.objectMouseOver.getBlockPos() != null && PlayerSettings.INSTANCE.focusedGui == null) {
			TileEntity tile = mc.world.getTileEntity(mc.objectMouseOver.getBlockPos());
			if (tile != null && tile.hasCapability(WorldGuiCapability.cap, mc.objectMouseOver.sideHit)) {
				IWorldGuiProvider pro = tile.getCapability(WorldGuiCapability.cap, mc.objectMouseOver.sideHit);
				WorldGui gui = pro.getGui(mc.player, tile.getPos());
				if (gui != null) {
					WorldGui.openGui(gui);
					gui.container = pro.getContainer(mc.player, tile.getPos());
					if (gui.container != null) {
						CommonEvents.getData(mc.player).containers.put(gui.id, gui.container);
						HUDLibrary.snw.sendToServer(new OpenGuiMessage(gui.id, tile.getPos(), mc.objectMouseOver.sideHit));
					}
				}
			}
		}
	}

	private static final ResourceLocation crosshairTex = new ResourceLocation(HUDLibrary.MOD_ID, "textures/gui/crosshair.png");

	@SubscribeEvent
	public static void crosshair(RenderGameOverlayEvent event) {
		if (!event.isCanceled() && event instanceof Pre && event.getType() == ElementType.CROSSHAIRS && PlayerSettings.INSTANCE.focusedGui != null) {
			event.setCanceled(true);
			if (true)
				return;
			Minecraft mc = Minecraft.getMinecraft();
			mc.getTextureManager().bindTexture(crosshairTex);
			GlStateManager.enableBlend();
			ScaledResolution sr = event.getResolution();
			if (mc.gameSettings.thirdPersonView == 0) {
				int l = sr.getScaledWidth();
				int i1 = sr.getScaledHeight();
				if (!mc.gameSettings.hideGUI) {
					//					GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					GlStateManager.enableAlpha();
					GlStateManager.color(1f, 1f, 1f, Mouse.getEventButtonState() ? 1f : .6f);
					GuiUtils.drawTexturedModalRect(l / 2 - 0, i1 / 2 - 0, 0, 0, 16, 16, 0);
					GlStateManager.color(1f, 1f, 1f, 1f);
				}
			}
		}
	}

	@SubscribeEvent
	public static void highlight(DrawBlockHighlightEvent event) {
		if (!event.isCanceled() && PlayerSettings.INSTANCE.focusedGui != null)
			event.setCanceled(true);
	}*/
}
