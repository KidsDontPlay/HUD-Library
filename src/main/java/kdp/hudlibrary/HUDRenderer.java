package kdp.hudlibrary;

import java.awt.*;
import java.util.ArrayList;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;

import org.apache.commons.lang3.mutable.MutableInt;

import kdp.hudlibrary.api.IHUDProvider;
import kdp.hudlibrary.api.enums.Axis;
import kdp.hudlibrary.api.enums.MarginDirection;
import kdp.hudlibrary.element.HUDElement;
import kdp.hudlibrary.element.HUDFluidStack;
import kdp.hudlibrary.element.HUDHorizontalCompound;
import kdp.hudlibrary.element.HUDItemStack;
import kdp.hudlibrary.element.HUDProgressBar;
import kdp.hudlibrary.element.HUDText;
import kdp.hudlibrary.util.DirectionPos;

@Mod.EventBusSubscriber(modid = HUDLibrary.MOD_ID, value = Dist.CLIENT)
public class HUDRenderer {
    static final Map<DirectionPos, CompoundNBT> syncedNBTs = new HashMap<>();
    static final Map<BlockPos, CompoundNBT> defaultNBTs = new HashMap<>();
    private static Cache<DirectionPos, List<HUDElement>> cachedElements = CacheBuilder.newBuilder().maximumSize(100)
            .expireAfterWrite(250, TimeUnit.MILLISECONDS).build();
    private static Set<TileEntity> tiles = Collections.newSetFromMap(new IdentityHashMap<>());
    private static LoadingCache<CapabilityProvider<?>, LazyOptional<IHUDProvider>> capaMap = CacheBuilder.newBuilder()
            .weakKeys().build(CacheLoader.from(k -> k.getCapability(HUDCapability.cap)));

    @SubscribeEvent
    public static void join(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof PlayerEntity && event.getWorld().isRemote) {
            syncedNBTs.clear();
            tiles.clear();
            capaMap.invalidateAll();
            cachedElements.invalidateAll();
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && event.phase == TickEvent.Phase.END && event.player.ticksExisted % 20 == 0) {
            tiles = event.player.world.loadedTileEntityList.stream()
                    .filter(t -> (capaMap.getUnchecked(t).isPresent() || defaultNBTs.containsKey(t.getPos())) && !t
                            .isRemoved())
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
            IHUDProvider hud = capaMap.getUnchecked(t).orElse(defaultNBTs.containsKey(t.getPos()) ?
                    getDefaultProvider(defaultNBTs.get(t.getPos())) :
                    null);
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
                CompoundNBT data = syncedNBTs.get(dp);
                if (data == null && hud.usesServerData()) {
                    elements = null;
                } else {
                    elements = cachedElements.get(dp, () -> hud.getElements(player, face.getOpposite(), data));
                }
            } catch (ExecutionException e1) {
                throw new RuntimeException(e1);
            }
            if (elements == null || elements.isEmpty()) {
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
            double f1;
            if (hud.smoothRotation(player)) {
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
                    e.getMargin(MarginDirection.LEFT) - //
                    e.getMargin(MarginDirection.RIGHT)).height + //
                    e.getMargin(MarginDirection.TOP) + //
                    e.getMargin(MarginDirection.BOTTOM)).sum() + //
                    getMargin(hud, false);
            double totalScale = MathHelper.clamp(hud.getScale(mc.player, face.getOpposite()), .1, 50.);
            GlStateManager.translated(//
                    -.5 * totalScale + hud.getOffset(player, face.getOpposite(), Axis.HORIZONTAL),//
                    1 * totalScale + hud.getOffset(player, face.getOpposite(), Axis.VERTICAL),//
                    0 + hud.getOffset(player, face.getOpposite(), Axis.NORMAL) + .5001);

            //				GlStateManager.glNormal3f(0.0F, 0.0F, -f);
            GlStateManager.depthMask(false);
            final double scaledWidth = 1. / width;
            GlStateManager.scaled(scaledWidth, -scaledWidth, .01);
            GlStateManager.translated(0, width - height, 0);

            GlStateManager.scaled(totalScale, totalScale, totalScale);
            int color = hud.getBackgroundColor(player, face.getOpposite());
            GuiUtils.drawGradientRect(0, 0, 0, width, height, color, color);

            // translate margin
            GlStateManager.translated(hud.getMargin(MarginDirection.LEFT), hud.getMargin(MarginDirection.TOP), 0);
            //				GlStateManager.translate(0, 0, .003);
            render(elements, effectiveWidth);

            GlStateManager.scaled(1. / totalScale, 1. / totalScale, 1. / totalScale);
            GlStateManager.depthMask(true);
            GlStateManager.popMatrix();
        });
    }

    private static void render(List<HUDElement> elements, int effectiveSize) {
        for (HUDElement e : elements) {
            GlStateManager.depthMask(false);
            int marginLeft = e.getMargin(MarginDirection.LEFT), marginTop = e
                    .getMargin(MarginDirection.TOP), marginRight = e.getMargin(MarginDirection.RIGHT), marginBottom = e
                    .getMargin(MarginDirection.BOTTOM);
            Dimension d = e.getDimension(effectiveSize - marginLeft - marginRight);
            int offsetX = marginLeft;
            if (e.getAlignment() == TextTable.Alignment.RIGHT)
                offsetX += ((effectiveSize - marginLeft - marginRight) - d.width);
            else if (e.getAlignment() == TextTable.Alignment.CENTER) {
                offsetX += ((effectiveSize - marginLeft - marginRight) - d.width) / 2;
            }
            GlStateManager.translated(offsetX, marginTop, 0);
            Integer color = e.getBackgroundColor();
            if (color != null) {
                GuiUtils.drawGradientRect(0, 0, 0, d.width, d.height, color, color);
            }
            e.draw(effectiveSize - marginLeft - marginRight);
            GlStateManager.translated(-offsetX, marginBottom + d.height, 0);
        }
    }

    private static int getMargin(IHUDProvider hud, boolean horizontal) {
        return horizontal ?
                hud.getMargin(MarginDirection.LEFT) + hud.getMargin(MarginDirection.RIGHT) :
                hud.getMargin(MarginDirection.BOTTOM) + hud.getMargin(MarginDirection.TOP);
    }

    private static IHUDProvider getDefaultProvider(CompoundNBT nbt) {
        return (player, facing, data) -> {
            List<HUDElement> result = new ArrayList<>();
            if (nbt.contains("e")) {
                int e = nbt.getInt("e");
                double eMax = nbt.getInt("eMax");
                result.add(new HUDProgressBar(-1, 10, Color.gray.getRGB(), Color.red.getRGB()).setFilling(e / eMax));
                result.add(new HUDText(e + " FE", false).setAlignment(TextTable.Alignment.CENTER));
            }
            if (nbt.contains("fs")) {
                List<FluidStack> fluids = nbt.getList("fs", 10).stream()
                        .map(n -> FluidStack.loadFluidStackFromNBT(((CompoundNBT) n).getCompound("f")))
                        .collect(Collectors.toList());
                result.add(new HUDHorizontalCompound(false,
                        fluids.stream().map(f -> new HUDFluidStack(f, -1, 30)).collect(Collectors.toList())));
                result.add(new HUDHorizontalCompound(false,
                        fluids.stream().map(f -> new HUDText(f.getAmount() + " mB", false))
                                .collect(Collectors.toList())).setAlignment(TextTable.Alignment.CENTER));
                /*result.add(new HUDCompound(false,
                        nbts.stream().map(n -> new HUDText(((CompoundNBT) n).getInt("m") + " mB", false))
                                .collect(Collectors.toList())));*/
            }
            if (nbt.contains("is")) {
                result.add(new HUDHorizontalCompound(true,
                        nbt.getList("is", 10).stream().limit(player.isSneaking() ? 15 : 5)
                                .map(n -> new HUDItemStack(ItemStack.read((CompoundNBT) n)))
                                .collect(Collectors.toList())));
            }
            return result;
        };
    }
}
