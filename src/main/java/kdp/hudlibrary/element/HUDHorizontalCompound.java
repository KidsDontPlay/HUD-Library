package kdp.hudlibrary.element;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraftforge.fml.client.config.GuiUtils;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import kdp.hudlibrary.api.enums.MarginDirection;

public class HUDHorizontalCompound extends HUDElement {
    protected final HUDElement[] elements;
    protected final boolean lineBreak;

    public HUDHorizontalCompound(boolean lineBreak, HUDElement... elements) {
        super();
        this.elements = Objects.requireNonNull(elements);
        this.lineBreak = lineBreak;
        setMargin(0);
    }

    public HUDHorizontalCompound(boolean lineBreak, Collection<? extends HUDElement> lis) {
        this(lineBreak, lis.toArray(new HUDElement[lis.size()]));
    }

    private List<List<HUDElement>> getElementRows(int maxWidth) {
        List<List<HUDElement>> lines = new ArrayList<>();
        List<HUDElement> line = new ArrayList<>();
        List<HUDElement> ls = new ArrayList<>(Arrays.asList(elements));
        while (!ls.isEmpty()) {
            HUDElement el = ls.remove(0);
            int width = el.getDimension(maxWidth - el.getMarginHorizontal()).width + el.getMarginHorizontal();
            if (maxWidth < line.stream()
                    .mapToInt(e -> e.getDimension(maxWidth - el.getMarginHorizontal()).width + el.getMarginHorizontal())
                    .sum() + width) {
                lines.add(new ArrayList<>(line));
                line.clear();
                line.add(el);
            } else
                line.add(el);
        }
        if (!line.isEmpty())
            lines.add(new ArrayList<>(line));
        lines.removeIf(List::isEmpty);
        return lines;
    }

    @Override
    protected Dimension dimension(int maxWidth) {
        if (elements.length == 0) {
            return new Dimension();
        }
        Dimension d;
        if (lineBreak) {
            int totalWidth = 0, totalHeight = 0;
            for (List<HUDElement> l : getElementRows(maxWidth)) {
                totalWidth = Math.max(totalWidth, l.stream().mapToInt(
                        e -> e.getDimension(maxWidth - e.getMarginHorizontal()).width + e.getMarginHorizontal()).sum());
                totalHeight += l.stream().mapToInt(
                        e -> e.getDimension(maxWidth - e.getMarginHorizontal()).height + e.getMarginVertical()).max()
                        .getAsInt();
            }
            d = new Dimension(totalWidth, totalHeight);
        } else {
            int totalWidth = 0, totalHeight = 0;
            Reference2IntOpenHashMap<HUDElement> maxWidths = getMaxWidths(
                    maxWidth - Arrays.stream(elements).mapToInt(HUDElement::getMarginHorizontal).sum());
            for (HUDElement e : elements) {
                totalWidth += e.getDimension(maxWidths.getInt(e)).width + e.getMarginHorizontal();
                totalHeight = Math.max(totalHeight, e.getDimension(maxWidths.getInt(e)).height + e.getMarginVertical());
            }
            boolean tooLong = totalWidth > maxWidth;
            if (tooLong) {
                double fac = maxWidth / (double) totalWidth;
                totalHeight *= fac;
            }
            d = new Dimension(totalWidth, totalHeight);
        }
        return d;
    }

    @Override
    public void draw(int maxWidth) {
        if (elements.length == 0) {
            return;
        }
        if (lineBreak) {
            int hei = 0;
            boolean firstH = true;
            for (List<HUDElement> l : getElementRows(maxWidth)) {
                int down = 0;
                if (firstH) {
                    firstH = false;
                    GlStateManager.translated(0,
                            down = l.stream().mapToInt(e -> e.getMargin(MarginDirection.TOP)).max().getAsInt(), 0);
                }
                int back = 0;
                boolean firstW = true;
                for (HUDElement e : l) {
                    GlStateManager.depthMask(false);
                    if (firstW) {
                        firstW = false;
                        int mar = e.getMargin(MarginDirection.LEFT);
                        back += mar;
                        GlStateManager.translated(mar, 0, 0);
                    }
                    Dimension d = e.getDimension(maxWidth - e.getMarginHorizontal());
                    int w = d.width + e.getMarginHorizontal();
                    back += w;
                    Integer color = e.getBackgroundColor();
                    if (color != null) {
                        GuiUtils.drawGradientRect(0, 0, 0, d.width, d.height, color, color);
                    }
                    e.draw(maxWidth - e.getMarginHorizontal());
                    GlStateManager.translated(w, 0, 0);
                }
                int h = l.stream().mapToInt(
                        e -> e.getDimension(maxWidth - e.getMarginHorizontal()).height + e.getMarginVertical()).max()
                        .getAsInt();
                hei += h;
                GlStateManager.translated(0, h - down, 0);
                GlStateManager.translated(-back, 0, 0);
            }
            GlStateManager.translated(0, -hei, 0);
        } else {
            int width = getDimension(maxWidth).width;
            int height = getDimension(maxWidth).height;
            boolean tooLong = width > maxWidth;
            double fac = maxWidth / (double) width;
            if (tooLong) {
                GlStateManager.scaled(fac, fac, 1);
            }
            int down = 0;
            GlStateManager.translated(0,
                    down = Arrays.stream(elements).mapToInt(e -> e.getMargin(MarginDirection.TOP)).max().getAsInt(), 0);
            int back = 0;
            boolean first = true;
            Reference2IntOpenHashMap<HUDElement> maxWidths = getMaxWidths(
                    maxWidth - Arrays.stream(elements).mapToInt(HUDElement::getMarginHorizontal).sum());
            for (HUDElement e : elements) {
                GlStateManager.depthMask(false);
                if (first) {
                    first = false;
                    int mar = e.getMargin(MarginDirection.LEFT);
                    back += mar;
                    GlStateManager.translated(mar, 0, 0);
                }
                Dimension dim = e.getDimension(maxWidths.getInt(e));
                int up = 0;
                int free = height - dim.height - e.getMarginVertical();
                if (free > 0)
                    up = (int) Math.ceil(free / (double) 2);
                GlStateManager.translated(0, up, 0);
                Integer color = e.getBackgroundColor();
                if (color != null) {
                    GuiUtils.drawGradientRect(0, 0, 0, dim.width, dim.height, color, color);
                }
                e.draw(maxWidths.getInt(e));
                int w = dim.width + e.getMarginHorizontal();
                back += w;
                GlStateManager.translated(0, -up, 0);
                GlStateManager.translated(w, 0, 0);
            }
            GlStateManager.translated(0, -down, 0);
            GlStateManager.translated(-back, 0, 0);
            if (tooLong) {
                GlStateManager.scaled(1 / fac, 1 / fac, 1);
            }
        }
    }

    private Cache<Integer, Reference2IntOpenHashMap<HUDElement>> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(500, TimeUnit.MILLISECONDS).build();

    private Reference2IntOpenHashMap<HUDElement> getMaxWidths(int maxWidth) {
        try {
            return cache.get(maxWidth, () -> {
                final int splittedWidth = maxWidth / elements.length;
                List<HUDElement> flexibles = new ArrayList<>();
                List<HUDElement> unflexibles = new ArrayList<>();
                for (HUDElement e : elements) {
                    int width1 = e.getDimension(maxWidth).width;
                    int width2 = e.getDimension(splittedWidth).width;
                    if (width1 > width2) {
                        flexibles.add(e);
                    } else if (width1 == width2) {
                        unflexibles.add(e);
                    } else {
                        throw new IllegalStateException("Unreasonable!");
                    }
                }
                Reference2IntOpenHashMap<HUDElement> result = new Reference2IntOpenHashMap<>();
                int unflexiblesWidth = 0;
                for (HUDElement e : unflexibles) {
                    int w = e.getDimension(splittedWidth).width;
                    unflexiblesWidth += w;
                    result.put(e, w);
                }
                if (!flexibles.isEmpty()) {
                    final int splittedFlexiblesWidth = (maxWidth - unflexiblesWidth) / flexibles.size();
                    for (HUDElement e : flexibles) {
                        result.put(e, splittedFlexiblesWidth);
                    }
                }

                return result;
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}