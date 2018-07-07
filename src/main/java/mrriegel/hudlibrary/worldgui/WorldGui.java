package mrriegel.hudlibrary.worldgui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Range;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import mrriegel.hudlibrary.ClientHelper;
import mrriegel.hudlibrary.CommonEvents;
import mrriegel.hudlibrary.HUDLibrary;
import mrriegel.hudlibrary.worldgui.message.CloseGuiMessage;
import mrriegel.hudlibrary.worldgui.message.NotifyServerMessage;
import mrriegel.hudlibrary.worldgui.message.SlotClickMessage;
import mrriegel.hudlibrary.worldgui.message.SyncPlayerInventoryMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiUtils;

public class WorldGui {

	private static final ResourceLocation BACKGROUND_TEX = new ResourceLocation("textures/gui/demo_background.png");
	private static final ResourceLocation SLOT_TEX = new ResourceLocation("textures/gui/container/recipe_background.png");

	public int width = 250, height = 150, id;
	public final Vec3d guiPos, playerPos;
	public Vec3d a, b, c, d;
	public final float yaw, pitch;
	public double u, v, maxU, maxV;

	private final GuiScreen screen;
	protected GuiButton selectedButton;
	private final Vec3d front;
	private final Vec3d back;
	public WorldGuiContainer container;
	@Deprecated
	protected List<Slot> slots = new ArrayList<>();

	public List<GuiButton> buttons = new ArrayList<>();

	protected final Minecraft mc = Minecraft.getMinecraft();

	public WorldGui() {
		screen = new GuiScreen() {
		};
		screen.mc = mc;
		playerPos = mc.player.getPositionEyes(0);
		guiPos = mc.player.getLook(0).add(playerPos);
		yaw = mc.player.rotationYaw;
		pitch = mc.player.rotationPitch;
		Vec3d see = guiPos.subtract(playerPos).scale(.1);
		Vec3d seeN = see.scale(-1);
		front = guiPos.add(seeN);
		back = guiPos.add(see);
	}

	public void init() {
		buttons.clear();
		slots.clear();
		double halfWidth = width / 2d, halfHeight = height / 2d;
		double scale = PlayerSettings.INSTANCE.scaleMap.getDouble(getClass());
		a = guiPos.add(getVec(halfWidth * scale, halfHeight * scale, pitch, yaw));
		b = guiPos.add(getVec(-halfWidth * scale, halfHeight * scale, pitch, yaw));
		c = guiPos.add(getVec(-halfWidth * scale, -halfHeight * scale, pitch, yaw));
		d = guiPos.add(getVec(halfWidth * scale, -halfHeight * scale, pitch, yaw));
	}

	public void draw(int mouseX, int mouseY, float partialTicks) {
		drawBackground(mouseX, mouseY, partialTicks);
		GlStateManager.disableLighting();
		for (GuiButton b : buttons)
			b.drawButton(mc, mouseX, mouseY, partialTicks);
		//		for (Slot slot : slots) {
		if (container != null)
			for (Slot slot : container.inventorySlots) {
				GlStateManager.color(1f, 1f, 1f, 1f);
				mc.getTextureManager().bindTexture(SLOT_TEX);
				GuiUtils.drawTexturedModalRect(slot.xPos - 1, slot.yPos - 1, 12, 12, 18, 18, 0);
				//			GuiUtils.drawGradientRect(0, slot.xPos, slot.yPos, slot.xPos + 16, slot.yPos + 16, 0xFF123456, 0xFF654321);
				drawItemStack(slot.getStack(), slot.xPos, slot.yPos, true);
				if (Range.between(slot.xPos, slot.xPos + 16).contains(mouseX) && Range.between(slot.yPos, slot.yPos + 16).contains(mouseY)) {
					GlStateManager.colorMask(true, true, true, false);
					GuiUtils.drawGradientRect(0, slot.xPos, slot.yPos, slot.xPos + 16, slot.yPos + 16, -2130706433, -2130706433);
					GlStateManager.colorMask(true, true, true, true);
				}
			}
		drawForeground(mouseX, mouseY, partialTicks);
		if (!mc.player.inventory.getItemStack().isEmpty()&&isFocused())
			drawItemStack(mc.player.inventory.getItemStack(), mouseX - 8, mouseY - 8, true);
		if (container != null)
			for (Slot slot : container.inventorySlots) {
				if(slot.getHasStack())
				if (Range.between(slot.xPos, slot.xPos + 16).contains(mouseX) && Range.between(slot.yPos, slot.yPos + 16).contains(mouseY)) {
					drawTooltip(slot.getStack(), mouseX, mouseY);
				}
			}
		
	}

	protected void drawBackground(int mouseX, int mouseY, float partialTicks) {
	}

	protected void drawForeground(int mouseX, int mouseY, float partialTicks) {
	}

	public void click(int mouse, int mouseX, int mouseY) {
		for (GuiButton b : buttons)
			if (b.mousePressed(mc, mouseX, mouseY)) {
				selectedButton = b;
				b.playPressSound(mc.getSoundHandler());
				buttonClicked(b, mouse);
			}
		//		for (Slot slot : slots) {
		if (container != null)
			for (Slot slot : container.inventorySlots) {
				if (!Range.between(slot.xPos, slot.xPos + 16).contains(mouseX) || !Range.between(slot.yPos, slot.yPos + 16).contains(mouseY))
					continue;
				container.slotClick(slot.slotNumber, mouse, mc.player.isSneaking() && !false ? ClickType.QUICK_MOVE : ClickType.PICKUP, mc.player);
				HUDLibrary.snw.sendToServer(new SlotClickMessage(id, slot.slotNumber, mouse, mc.player.isSneaking() && !false ? ClickType.QUICK_MOVE : ClickType.PICKUP));
				if (true)
					continue;
				InventoryPlayer inventoryplayer = mc.player.inventory;

				ItemStack slotstack = slot.getStack();
				ItemStack heldstack = inventoryplayer.getItemStack();

				if (slotstack.isEmpty()) {
					if (!heldstack.isEmpty() && slot.isItemValid(heldstack)) {
						int i3 = mouse == 0 ? heldstack.getCount() : 1;

						if (i3 > slot.getItemStackLimit(heldstack)) {
							i3 = slot.getItemStackLimit(heldstack);
						}

						slot.putStack(heldstack.splitStack(i3));
					}
				} else if (slot.canTakeStack(mc.player)) {
					if (heldstack.isEmpty()) {
						if (slotstack.isEmpty()) {
							slot.putStack(ItemStack.EMPTY);
							inventoryplayer.setItemStack(ItemStack.EMPTY);
						} else {
							int l2 = mouse == 0 ? slotstack.getCount() : (slotstack.getCount() + 1) / 2;
							inventoryplayer.setItemStack(slot.decrStackSize(l2));

							if (slotstack.isEmpty()) {
								slot.putStack(ItemStack.EMPTY);
							}

							slot.onTake(mc.player, inventoryplayer.getItemStack());
						}
					} else if (slot.isItemValid(heldstack)) {
						if (slotstack.getItem() == heldstack.getItem() && slotstack.getMetadata() == heldstack.getMetadata() && ItemStack.areItemStackTagsEqual(slotstack, heldstack)) {
							int k2 = mouse == 0 ? heldstack.getCount() : 1;

							if (k2 > slot.getItemStackLimit(heldstack) - slotstack.getCount()) {
								k2 = slot.getItemStackLimit(heldstack) - slotstack.getCount();
							}

							if (k2 > heldstack.getMaxStackSize() - slotstack.getCount()) {
								k2 = heldstack.getMaxStackSize() - slotstack.getCount();
							}

							heldstack.shrink(k2);
							slotstack.grow(k2);
						} else if (heldstack.getCount() <= slot.getItemStackLimit(heldstack)) {
							slot.putStack(heldstack);
							inventoryplayer.setItemStack(slotstack);
						}
					} else if (slotstack.getItem() == heldstack.getItem() && heldstack.getMaxStackSize() > 1 && (!slotstack.getHasSubtypes() || slotstack.getMetadata() == heldstack.getMetadata()) && ItemStack.areItemStackTagsEqual(slotstack, heldstack) && !slotstack.isEmpty()) {
						int j2 = slotstack.getCount();

						if (j2 + heldstack.getCount() <= heldstack.getMaxStackSize()) {
							heldstack.grow(j2);
							slotstack = slot.decrStackSize(j2);

							if (slotstack.isEmpty()) {
								slot.putStack(ItemStack.EMPTY);
							}

							slot.onTake(mc.player, inventoryplayer.getItemStack());
						}
					}
				}

				slot.onSlotChanged();
				HUDLibrary.snw.sendToServer(new SyncPlayerInventoryMessage(mc.player));

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

	public boolean isInFront() {
		Vec3d p = mc.player.getPositionEyes(0);
		return p.distanceTo(front) < p.distanceTo(back);
	}

	public boolean isFocused() {
		return u >= 0 && u <= maxU && v >= 0 && v <= maxV && isReachable();
	}

	public double maxRenderDistance() {
		return 10;
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
		return mc.player.getPositionVector().distanceTo(guiPos) > 8 && false;
	}

	public final void close() {
		onClosed();
		if (container != null)
			container.onContainerClosed(mc.player);
		HUDLibrary.drop(mc.player);
		PlayerSettings.INSTANCE.guis.remove(this);
		if (PlayerSettings.INSTANCE.focusedGui == this) {
			PlayerSettings.INSTANCE.focusedGui = null;
			HUDLibrary.snw.sendToServer(new NotifyServerMessage(false));
			HUDLibrary.snw.sendToServer(new CloseGuiMessage(id));
			CommonEvents.getData(mc.player).containers.remove(id);
			CommonEvents.openWorldGuis.remove(mc.player.getUniqueID());
		}
	}

	//HELPER START
	public void drawItemStack(ItemStack stack, int x, int y, boolean overlay) {
		RenderHelper.enableGUIStandardItemLighting();
		pre();
		//		GlStateManager.rotate(180, 0, 1, 0);
		mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
		//		mc.getRenderItem().renderItem(stack, TransformType.GUI);
		if (overlay)
			mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, stack, x, y, null);
		post();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.depthMask(false);
	}

	protected void drawFluidStack(FluidStack stack, int x, int y, int w, int h) {
		GlStateManager.depthMask(false);
		ClientHelper.drawFluidStack(stack, x, y, w, h);
	}

	protected void drawTooltip(List<String> lines, int mouseX, int mouseY) {
		pre();
		GuiUtils.drawHoveringText(lines, MathHelper.clamp(mouseX, 0, width), MathHelper.clamp(mouseY, 0, height), width, height, -1, mc.fontRenderer);
		post();
	}

	protected void drawTooltip(ItemStack stack, int mouseX, int mouseY) {
		drawTooltip(screen.getItemToolTip(stack), mouseX, mouseY);
	}

	protected void drawBackgroundTexture(int x, int y, int w, int h) {
		mc.getTextureManager().bindTexture(BACKGROUND_TEX);
		GlStateManager.color(1f, 1f, 1f, 1f);
		GuiUtils.drawContinuousTexturedBox(x, y, 0, 0, w, h, 248, 166, 4, 0);
	}

	protected void drawBackgroundTexture() {
		drawBackgroundTexture(0, 0, width, height);
	}

	protected void pre() {
		GlStateManager.scale(1, 1, .001);
	}

	protected void post() {
		GlStateManager.scale(1, 1, 1000);
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

	private static int ids = 0;

	public static void openGui(WorldGui gui) {
		gui.id = ids++;
		gui.init();
		PlayerSettings.INSTANCE.guis.add(gui);
	}

}
