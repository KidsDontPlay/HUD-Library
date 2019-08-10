package kdp.hudlibrary.tehud;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.TextTable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;

import org.apache.commons.lang3.mutable.MutableInt;

import kdp.hudlibrary.HUDConfig;
import kdp.hudlibrary.HUDLibrary;
import kdp.hudlibrary.tehud.element.HUDElement;

@Mod.EventBusSubscriber(modid = HUDLibrary.MOD_ID, value = Dist.CLIENT)
public class HUDRenderer {
    protected static final Map<DirectionPos, Map<Integer, INBT>> hudElements = new HashMap<>();
    private static Cache<DirectionPos, List<HUDElement>> cachedElements = CacheBuilder.newBuilder().maximumSize(100)
            .expireAfterWrite(250, TimeUnit.MILLISECONDS).build();
    private static Set<TileEntity> tiles = Collections.newSetFromMap(new IdentityHashMap<>());
    private static LoadingCache<CapabilityProvider<?>, LazyOptional<IHUDProvider>> capaMap = CacheBuilder.newBuilder()
            .weakKeys().build(CacheLoader.from(k -> k.getCapability(HUDCapability.cap)));

    @SubscribeEvent
    public static void join(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof PlayerEntity && event.getWorld().isRemote) {
            hudElements.clear();
            tiles.clear();
            capaMap.invalidateAll();
            cachedElements.invalidateAll();
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && event.phase == TickEvent.Phase.END && event.player.ticksExisted % 30 == 0) {
            tiles = event.player.world.loadedTileEntityList.stream()
                    .filter(t -> capaMap.getUnchecked(t).isPresent() && !t.isRemoved())
                    .collect(Collectors.toCollection(() -> Collections.newSetFromMap(new IdentityHashMap<>())));

        }
    }

    @SubscribeEvent
    public static void render(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        Vec3d playerPosition = player.getEyePosition(event.getPartialTicks());
        Vec3d see = player.getLook(event.getPartialTicks());
        List<TileEntity> l = tiles.stream().filter(t -> t.getPos()
                .distanceSq(playerPosition.x, playerPosition.y, playerPosition.z, false) < HUDConfig.visibleDistance
                .get() * HUDConfig.visibleDistance.get()).sorted((b, a) -> {
            Vec3d va = new Vec3d(a.getPos()).add(.5, 1, .5);
            Vec3d vb = new Vec3d(b.getPos()).add(.5, 1, .5);
            return Double.compare(playerPosition.squareDistanceTo(va), playerPosition.squareDistanceTo(vb));
        }).collect(Collectors.toList());

        MutableInt counter = new MutableInt(l.size() - HUDConfig.maxHUDs.get());
        l.forEach(t -> {
            IHUDProvider hud = capaMap.getUnchecked(t).orElse(null);
            if (hud == null || counter.getAndAdd(-1) > 0) {
                return;
            }
            final BlockPos blockPos = t.getPos();
            final Vec3d v = new Vec3d(//
                    blockPos.getX() + .5,//
                    playerPosition.y,//
                    blockPos.getZ() + .5).subtract(playerPosition);
            Direction face = Direction.getFacingFromVector((float) v.x, (float) v.y, (float) v.z);
            double angle = Math.toDegrees(Math.acos(see.dotProduct(v.normalize())));
            if (angle > 100 || !hud.isVisible(player, face.getOpposite())) {
                return;
            }

            List<HUDElement> elements;
            try {
                DirectionPos dp = DirectionPos.of(blockPos, face.getOpposite());
                Map<Integer, INBT> data = hudElements.get(dp);
                if (data == null && hud.readingSide().isServer()) {
                    elements = null;
                } else {
                    elements = cachedElements.get(dp, () -> hud.getElements(player, face.getOpposite(), data));
                }
            } catch (ExecutionException e1) {
                throw new RuntimeException(e1);
            } if (elements == null || elements.isEmpty()) {
                return;
            }

            double x = blockPos.getX() - TileEntityRendererDispatcher.staticPlayerX;
            double y = blockPos.getY() - TileEntityRendererDispatcher.staticPlayerY;
            double z = blockPos.getZ() - TileEntityRendererDispatcher.staticPlayerZ;

            // render
            GlStateManager.pushMatrix();
            // translate to TE
            GlStateManager.translated(x + .5, y + 1, z + .5);

            // rotate to the player
            double f1 = 0;
            if (hud.is360degrees(player)) {
                f1 = (180 * (Math.atan2(v.x, v.z) + Math.PI)) / Math.PI;
            } else {
                f1 = face.getHorizontalIndex() * 90.;
                if (face.getAxis() == Direction.Axis.Z)
                    f1 += 180;
            }
            GlStateManager.rotated(f1, 0.0, 1.0, 0.0);

            GlStateManager.enableRescaleNormal();
            // translate to correct position
            final int width = hud.getWidth(player, face.getOpposite());
            final int effectiveWidth = width - getMargin(hud, true);
            final int height = elements.stream().mapToInt(e -> e.getDimension(effectiveWidth - //
                    e.getMargin(IHUDProvider.MarginDirection.LEFT) - //
                    e.getMargin(IHUDProvider.MarginDirection.RIGHT)).height + //
                    e.getMargin(IHUDProvider.MarginDirection.TOP) + //
                    e.getMargin(IHUDProvider.MarginDirection.BOTTOM)).sum() + //
                    getMargin(hud, false);
            double totalScale = MathHelper.clamp(hud.getScale(mc.player, face.getOpposite()), .1, 50.);
            GlStateManager.translated(//
                    -.5 * totalScale + hud.getOffset(player, face.getOpposite(), IHUDProvider.Axis.HORIZONTAL),//
                    1 * totalScale + hud.getOffset(player, face.getOpposite(), IHUDProvider.Axis.VERTICAL),//
                    0 + hud.getOffset(player, face.getOpposite(), IHUDProvider.Axis.NORMAL) + .5001);

            //				GlStateManager.glNormal3f(0.0F, 0.0F, -f);
            GlStateManager.depthMask(false);
            final double scaledWidth = 1. / width;
            GlStateManager.scaled(scaledWidth, -scaledWidth, .01);
            GlStateManager.translated(0, width - height, 0);

            GlStateManager.scaled(totalScale, totalScale, totalScale);
            int color = hud.getBackgroundColor(player, face.getOpposite());
            GuiUtils.drawGradientRect(0, 0, 0, width, height, color, color);

            // translate margin
            GlStateManager.translated(hud.getMargin(IHUDProvider.MarginDirection.LEFT),
                    hud.getMargin(IHUDProvider.MarginDirection.TOP),
                    0);
            //				GlStateManager.translate(0, 0, .003);
            render(elements, effectiveWidth);

            GlStateManager.scaled(1. / totalScale, 1. / totalScale, 1. / totalScale);
            GlStateManager.depthMask(true);
            GlStateManager.popMatrix();
        });
    }

    private static void render(List<HUDElement> elements, int effectiveSize) {
        for (int j = 0; j < elements.size(); ++j) {
            GlStateManager.depthMask(false);
            HUDElement e = elements.get(j);
            int marginLeft = e.getMargin(IHUDProvider.MarginDirection.LEFT), marginTop = e
                    .getMargin(IHUDProvider.MarginDirection.TOP), marginRight = e
                    .getMargin(IHUDProvider.MarginDirection.RIGHT), marginBottom = e
                    .getMargin(IHUDProvider.MarginDirection.BOTTOM);
            Dimension d = e.getDimension(effectiveSize - marginLeft - marginRight);
            int offsetX = marginLeft;
            if (e.getAlignment() == TextTable.Alignment.RIGHT)
                offsetX += ((effectiveSize - marginLeft - marginRight) - d.width);
            else if (e.getAlignment() == TextTable.Alignment.CENTER) {
                offsetX += ((effectiveSize - marginLeft - marginRight) - d.width) / 2;
            }
            GlStateManager.translated(offsetX, marginTop, 0);
            Integer color = e.getBackgroundColor();
            //color=0xff000000|e.hashCode();
            if (color != null) {
                GuiUtils.drawGradientRect(0, 0, 0, d.width, d.height, color, color);
            }
            e.draw(effectiveSize - marginLeft - marginRight);
            GlStateManager.translated(-offsetX, marginBottom + d.height, 0);
        }
    }

    private static int getMargin(IHUDProvider hud, boolean horizontal) {
        return horizontal ?
                hud.getMargin(IHUDProvider.MarginDirection.LEFT) + hud.getMargin(IHUDProvider.MarginDirection.RIGHT) :
                hud.getMargin(IHUDProvider.MarginDirection.BOTTOM) + hud.getMargin(IHUDProvider.MarginDirection.TOP);
    }
}
