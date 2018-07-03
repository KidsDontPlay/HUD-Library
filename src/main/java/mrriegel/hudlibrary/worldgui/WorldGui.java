package mrriegel.hudlibrary.worldgui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.google.common.base.Strings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiUtils;

public class WorldGui extends Gui {
	public int width = 250, height = 150;
	public final Vec3d guiPos, playerPos;
	public Vec3d a, b, c, d;
	public final float yaw, pitch;
	public double u, v, maxU, maxV;

	int num = 0;

	public List<GuiButton> buttons = new ArrayList<>();

	private final Minecraft mc = Minecraft.getMinecraft();

	public WorldGui() {
		playerPos = mc.player.getPositionEyes(0);
		guiPos = mc.player.getLook(0).add(playerPos);
		yaw = mc.player.rotationYaw;
		pitch = mc.player.rotationPitch;
	}

	public void init() {
		buttons.clear();
		double halfWidth = width / 2d, halfHeight = height / 2d;
		double scale = PlayerSettings.INSTANCE.scale;
		a = guiPos.add(getVec(halfWidth * scale, halfHeight * scale, pitch, yaw));
		b = guiPos.add(getVec(-halfWidth * scale, halfHeight * scale, pitch, yaw));
		c = guiPos.add(getVec(-halfWidth * scale, -halfHeight * scale, pitch, yaw));
		d = guiPos.add(getVec(halfWidth * scale, -halfHeight * scale, pitch, yaw));
		buttons.add(new GuiButtonExt(0, 13, 13, 30, 30, "+"));
		buttons.add(new GuiButtonExt(0, 53, 13, 30, 30, "-"));
	}

	public void draw(int mouseX, int mouseY) {
		GuiUtils.drawGradientRect(0, 0, 0, width, height, 0x66666666, 0x66666666);
		mc.fontRenderer.drawString(num + "", 90, 27, Color.BLACK.getRGB());
		for (GuiButton b : buttons)
			b.drawButton(mc, mouseX, mouseY, 0);
		//		GlStateManager.disableDepth();
		GuiUtils.drawGradientRect(0, 15, 10, 160, 30, 0x44100010, 0x44100010);
		//		GlStateManager.enableDepth();
		//		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.disableLighting();
		RenderHelper.enableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();
		mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Blocks.YELLOW_GLAZED_TERRACOTTA), 100, 15);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.enableLighting();
		for (GuiButton b : buttons)
			if (b.isMouseOver())
				GuiUtils.drawHoveringText(Arrays.asList("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.", Strings.repeat(b.displayString, 8), Strings.repeat(b.displayString, 18)), mouseX, mouseY, width, height, -1, mc.fontRenderer);
	}

	public void click(int mouse, int mouseX, int mouseY) {
		for (GuiButton b : buttons)
			if (b.isMouseOver()) {
				if (b.displayString.equals("+"))
					num += mc.player.isSneaking() ? 10 : 1;
				else if (b.displayString.equals("-"))
					num -= mc.player.isSneaking() ? 10 : 1;
			}
	}

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
}
