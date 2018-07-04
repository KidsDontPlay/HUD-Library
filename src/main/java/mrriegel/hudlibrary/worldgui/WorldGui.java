package mrriegel.hudlibrary.worldgui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import mrriegel.hudlibrary.CommonEvents;
import mrriegel.hudlibrary.HUDLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.config.GuiUtils;

public class WorldGui extends Gui {

	private static final ResourceLocation TEX = new ResourceLocation("textures/gui/demo_background.png");

	public int width = 250, height = 150;
	public final Vec3d guiPos, playerPos;
	public Vec3d a, b, c, d;
	public final float yaw, pitch;
	public double u, v, maxU, maxV;

	private GuiScreen screen;
	protected GuiButton selectedButton;

	public List<GuiButton> buttons = new ArrayList<>();

	private final Minecraft mc = Minecraft.getMinecraft();

	public WorldGui() {
		screen = new GuiScreen() {
		};
		screen.mc = mc;
		playerPos = mc.player.getPositionEyes(0);
		guiPos = mc.player.getLook(0).add(playerPos);
		yaw = mc.player.rotationYaw;
		pitch = mc.player.rotationPitch;
	}

	public void init() {
		buttons.clear();
		double halfWidth = width / 2d, halfHeight = height / 2d;
		double scale = PlayerSettings.INSTANCE.scaleMap.getDouble(getClass());
		a = guiPos.add(getVec(halfWidth * scale, halfHeight * scale, pitch, yaw));
		b = guiPos.add(getVec(-halfWidth * scale, halfHeight * scale, pitch, yaw));
		c = guiPos.add(getVec(-halfWidth * scale, -halfHeight * scale, pitch, yaw));
		d = guiPos.add(getVec(halfWidth * scale, -halfHeight * scale, pitch, yaw));
	}

	public void draw(int mouseX, int mouseY, float partialTicks) {
		for (GuiButton b : buttons)
			b.drawButton(mc, mouseX, mouseY, partialTicks);
	}

	public void click(int mouse, int mouseX, int mouseY) {
		for (GuiButton b : buttons)
			if (b.mousePressed(mc, mouseX, mouseY)) {
				selectedButton = b;
				b.playPressSound(mc.getSoundHandler());
				buttonClicked(b, mouse);
			}
	}

	public void release(int mouse, int mouseX, int mouseY) {
		if (selectedButton != null) {
			selectedButton.mouseReleased(mouseX, mouseY);
			selectedButton = null;
		}
	}

	public boolean isReachable() {
		return guiPos.distanceTo(mc.player.getPositionVector()) < mc.player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() && isInFront();
	}

	private boolean isInFront() {
		Vec3d see = guiPos.subtract(playerPos).scale(.1);
		Vec3d seeN = see.scale(-1);
		Vec3d front = guiPos.add(seeN);
		Vec3d back = guiPos.add(see);
		Vec3d p = mc.player.getPositionEyes(0);
		return p.distanceTo(front) < p.distanceTo(back);
	}

	public boolean isFocused() {
		return u >= 0 && u <= maxU && v >= 0 && v <= maxV && isReachable();
	}

	public void buttonClicked(GuiButton b, int mouse) {
	}

	public void update() {
	}

	public void onClosed() {
	}

	public void onMouseEnter() {
	}

	public void onMouseLeave() {
	}

	public boolean tooFarAway() {
		return mc.player.getPositionVector().distanceTo(guiPos) > 8;
	}

	public final void close() {
		onClosed();
		PlayerSettings.INSTANCE.guis.remove(this);
		if (PlayerSettings.INSTANCE.focusedGui == this) {
			PlayerSettings.INSTANCE.focusedGui = null;
			HUDLibrary.snw.sendToServer(new NotifyServerMessage(false));
			CommonEvents.openWorldGuis.put(mc.player.getUniqueID(), false);
		}
	}

	//HELPER START
	protected void drawItemstack(ItemStack stack, int x, int y, boolean overlay) {
		RenderHelper.enableGUIStandardItemLighting();
		int s = 1000;
		GlStateManager.scale(1, 1, 1. / s);
		mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
		if (overlay)
			mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, stack, x, y, null);
		GlStateManager.scale(1, 1, s);
		RenderHelper.disableStandardItemLighting();
	}

	protected void drawTooltip(List<String> lines, int mouseX, int mouseY) {
		int s = 1000;
		GlStateManager.scale(1, 1, 1. / s);
		GuiUtils.drawHoveringText(lines, MathHelper.clamp(mouseX, 0, width), MathHelper.clamp(mouseY, 0, height), width, height, -1, mc.fontRenderer);
		GlStateManager.scale(1, 1, s);
	}

	protected void drawTooltip(ItemStack stack, int mouseX, int mouseY) {
		drawTooltip(screen.getItemToolTip(stack), mouseX, mouseY);
	}

	protected void drawBackgroundTexture(int x, int y, int w, int h) {
		mc.getTextureManager().bindTexture(TEX);
		GlStateManager.color(1f, 1f, 1f, 1f);
		GuiUtils.drawContinuousTexturedBox(x, y, 0, 0, w, h, 248, 166, 4, zLevel);
	}

	protected void drawBackgroundTexture() {
		drawBackgroundTexture(0, 0, width, height);
	}
	//HELPER END

	private static Vec3d getVec(double x, double y, double pitch, double yaw) {
		Vec3d v = new Vec3d(x, y, 0);
		Matrix4f m = new Matrix4f();
		m.m03 = (float) v.x;
		m.m13 = (float) v.y;
		m.m23 = (float) v.z;
		//		m = m.rotate((float) Math.toRadians(180), new Vector3f(0, 0, 1));
		m = m.rotate((float) Math.toRadians(-pitch), new Vector3f(1, 0, 0));
		m = m.rotate((float) Math.toRadians(-MathHelper.wrapDegrees(-yaw)), new Vector3f(0, 1, 0));
		return new Vec3d(m.m03, m.m13, m.m23);
	}

	public static void openGui(WorldGui gui) {
		gui.init();
		PlayerSettings.INSTANCE.guis.add(gui);
	}

}
