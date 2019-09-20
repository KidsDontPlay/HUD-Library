package kdp.hudlibrary.worldgui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiUtils;

import org.apache.commons.lang3.Range;

import kdp.hudlibrary.ClientHelper;

public class WorldGui {

    private static final ResourceLocation BACKGROUND_TEX = new ResourceLocation("textures/gui/demo_background.png");
    private static final ResourceLocation SLOT_TEX = new ResourceLocation(
            "textures/gui/container/recipe_background.png");

    public int width = 250, height = 150, id;
    public final Vec3d guiPos, playerPos, front, back;
    public Vec3d a, b, c, d;
    public final float yaw, pitch;
    public double u, v, maxU, maxV;

    private final Screen screen;
    protected Button selectedButton;
    public ContainerWG container;

    public List<Button> buttons = new ArrayList<>();

    protected final Minecraft mc = Minecraft.getInstance();

    public WorldGui() {
        screen = new Screen(new StringTextComponent("")) {
        };
        playerPos = mc.player.getEyePosition(0);
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
        double halfWidth = width / 2d, halfHeight = height / 2d;
        double scale = PlayerSettings.INSTANCE.scaleMap.getDouble(getClass());
        /*a = guiPos.add(getVec(halfWidth * scale, halfHeight * scale, pitch, yaw));
        b = guiPos.add(getVec(-halfWidth * scale, halfHeight * scale, pitch, yaw));
        c = guiPos.add(getVec(-halfWidth * scale, -halfHeight * scale, pitch, yaw));
        d = guiPos.add(getVec(halfWidth * scale, -halfHeight * scale, pitch, yaw));*/
        screen.init(mc, width, height);
    }

    public void draw(int mouseX, int mouseY, float partialTicks) {
        drawBackground(mouseX, mouseY, partialTicks);
        GlStateManager.disableLighting();
        for (Button b : buttons)
            b.render(mouseX, mouseY, partialTicks);
        if (container != null)
            for (Slot slot : container.inventorySlots) {
                GlStateManager.color4f(1f, 1f, 1f, 1f);
                mc.getTextureManager().bindTexture(SLOT_TEX);
                GuiUtils.drawTexturedModalRect(slot.xPos - 1, slot.yPos - 1, 12, 12, 18, 18, 0);
                drawItemStack(slot.getStack(), slot.xPos, slot.yPos, true);
                if (isMouseOverSlot(slot, mouseX, mouseY)) {
                    GlStateManager.colorMask(true, true, true, false);
                    GuiUtils.drawGradientRect(0, slot.xPos, slot.yPos, slot.xPos + 16, slot.yPos + 16, -2130706433,
                            -2130706433);
                    GlStateManager.colorMask(true, true, true, true);
                }
            }
        drawForeground(mouseX, mouseY, partialTicks);
        if (!mc.player.inventory.getItemStack().isEmpty()/* && isFocused()*/)
            drawItemStack(mc.player.inventory.getItemStack(), mouseX - 8, mouseY - 8, true);
        if (container != null)
            for (Slot slot : container.inventorySlots) {
                if (slot.getHasStack() && isMouseOverSlot(slot, mouseX, mouseY))
                    drawTooltip(slot.getStack(), mouseX, mouseY);
            }

    }

    protected void drawBackground(int mouseX, int mouseY, float partialTicks) {
    }

    protected void drawForeground(int mouseX, int mouseY, float partialTicks) {
    }

    /*public void click(int mouse, int mouseX, int mouseY) {
        for (Button b : buttons)
            if (b.mousePressed(mc, mouseX, mouseY)) {
                selectedButton = b;
                b.playPressSound(mc.getSoundHandler());
                buttonClicked(b, mouse);
            }
        if (container != null)
            for (Slot slot : container.inventorySlots) {
                if (!isMouseOverSlot(slot, mouseX, mouseY))
                    continue;
                container.slotClick(slot.slotNumber,
                        mouse,
                        mc.player.isSneaking() ? ClickType.QUICK_MOVE : ClickType.PICKUP,
                        mc.player);
                HUDLibrary.snw.sendToServer(new SlotClickMessage(id,
                        slot.slotNumber,
                        mouse,
                        mc.player.isSneaking() ? ClickType.QUICK_MOVE : ClickType.PICKUP));
            }
    }*/

    private boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY) {
        return Range.between(slot.xPos, slot.xPos + 16).contains(mouseX) && Range.between(slot.yPos, slot.yPos + 16)
                .contains(mouseY);
    }

    public void release(int mouse, int mouseX, int mouseY) {
        if (selectedButton != null) {
            //selectedButton.mouseReleased(mouseX, mouseY);
            selectedButton = null;
        }
    }

    public boolean isInFront() {
        Vec3d p = mc.player.getEyePosition(0);
        return p.distanceTo(front) < p.distanceTo(back);
    }

    /*public boolean isReachable() {
        return guiPos.distanceTo(mc.player.getPositionVector()) < mc.player
                .getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() && isInFront();
    }

    public boolean isFocused() {
        return u >= 0 && u <= maxU && v >= 0 && v <= maxV && isReachable();
    }*/

    public double maxRenderDistance() {
        return 10;
    }

    public void buttonClicked(Button b, int mouse) {
    }

    public void update() {
    }

    public void onClosed() {
    }

    public void onMouseEnter() {
    }

    public void onMouseLeave() {
    }

    public final void close() {
        onClosed();
        if (container != null)
            container.onContainerClosed(mc.player);
        PlayerSettings.INSTANCE.guis.remove(this);
        if (PlayerSettings.INSTANCE.focusedGui == this) {
            PlayerSettings.INSTANCE.focusedGui = null;
            //HUDLibrary.snw.sendToServer(new CloseGuiMessage(id));
            //CommonEvents.getData(mc.player).containers.remove(id);
        }
    }

    //HELPER START
    public void drawItemStack(ItemStack stack, int x, int y, boolean overlay) {
        RenderHelper.enableGUIStandardItemLighting();
        pre();
        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, x, y);
        if (overlay)
            mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, stack, x, y, null);
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
        GuiUtils.drawHoveringText(lines, MathHelper.clamp(mouseX, 0, width), MathHelper.clamp(mouseY, 0, height), width,
                height, -1, mc.fontRenderer);
        post();
    }

    protected void drawTooltip(ItemStack stack, int mouseX, int mouseY) {
        //drawTooltip(screen.getItemToolTip(stack), mouseX, mouseY);
    }

    protected void drawBackgroundTexture(int x, int y, int w, int h) {
        mc.getTextureManager().bindTexture(BACKGROUND_TEX);
        GlStateManager.color4f(1f, 1f, 1f, 1f);
        GuiUtils.drawContinuousTexturedBox(x, y, 0, 0, w, h, 248, 166, 4, 0);
    }

    protected void drawBackgroundTexture() {
        drawBackgroundTexture(0, 0, width, height);
    }

    protected void pre() {
        GlStateManager.scaled(1, 1, .001);
    }

    protected void post() {
        GlStateManager.scaled(1, 1, 1000);
    }
    //HELPER END

    /*private static Vec3d getVec(double x, double y, double pitch, double yaw) {
        Vec3d v = new Vec3d(x, y, 0);
        Matrix4f m = new Matrix4f();
        m.m03 = (float) v.x;
        m.m13 = (float) v.y;
        m.m23 = (float) v.z;
        //		m = m.rotate((float) Math.toRadians(180), new Vector3f(0, 0, 1));
        m = m.rotate((float) Math.toRadians(-pitch), new Vector3f(1, 0, 0));
        m = m.rotate((float) Math.toRadians(-MathHelper.wrapDegrees(-yaw)), new Vector3f(0, 1, 0));
        return new Vec3d(m.m03, m.m13, m.m23);
    }*/

    private static int ids = 0;

    public static void openGui(WorldGui gui) {
        gui.id = ids++;
        gui.init();
        PlayerSettings.INSTANCE.guis.add(gui);
    }

}
