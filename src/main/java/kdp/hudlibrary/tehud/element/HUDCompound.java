package kdp.hudlibrary.tehud.element;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import org.apache.commons.lang3.Validate;

import kdp.hudlibrary.tehud.IHUDProvider;

public class HUDCompound extends HUDElement {
    protected final HUDElement[] elements;
    protected final boolean lineBreak;

    public HUDCompound(boolean lineBreak, HUDElement... elements) {
        super();
        this.elements = elements;
        this.lineBreak = lineBreak;
        Validate.isTrue(elements != null && elements.length != 0);
    }

    public HUDCompound(boolean lineBreak, Collection<? extends HUDElement> lis) {
        this(lineBreak, lis.toArray(new HUDElement[lis.size()]));
    }

    public HUDElement[] getElements() {
        return elements;
    }

    @Override
    public void readSyncTag(CompoundNBT tag) {
        if (reader != null) {
            reader.accept(this, tag);
            return;
        }
        if (tag.contains("elements")) {
            ListNBT list = tag.getList("elements", 10);
            //Validate.isTrue(elements.length == list.size());
            int size = Math.min(elements.length, list.size());
            for (int i = 0; i < size; i++) {
                HUDElement h = elements[i];
                CompoundNBT nbt = list.getCompound(i);
                h.readSyncTag(nbt);
            }
        }
    }

    private List<List<HUDElement>> getElementRows(int maxWidth) {
        List<List<HUDElement>> lines = new ArrayList<>();
        List<HUDElement> line = new ArrayList<>();
        List<HUDElement> ls = Lists.newArrayList(elements);
        while (!ls.isEmpty()) {
            HUDElement el = ls.remove(0);
            int width = el.getDimension(maxWidth - el.getPadding(IHUDProvider.SpacingDirection.LEFT) - el
                    .getPadding(IHUDProvider.SpacingDirection.RIGHT)).width + el
                    .getPadding(IHUDProvider.SpacingDirection.LEFT) + el
                    .getPadding(IHUDProvider.SpacingDirection.RIGHT);
            if (maxWidth < line.stream().mapToInt(e -> e
                    .getDimension(maxWidth - e.getPadding(IHUDProvider.SpacingDirection.LEFT) - e
                            .getPadding(IHUDProvider.SpacingDirection.RIGHT)).width + e
                    .getPadding(IHUDProvider.SpacingDirection.LEFT) + e.getPadding(IHUDProvider.SpacingDirection.RIGHT))
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
        Dimension d = null;
        if (lineBreak) {
            int totalWidth = 0, totalHeight = 0;
            for (List<HUDElement> l : getElementRows(maxWidth)) {
                totalWidth = Math.max(totalWidth,
                        l.stream().mapToInt(e -> e
                                .getDimension(maxWidth - e.getPadding(IHUDProvider.SpacingDirection.LEFT) - e
                                        .getPadding(IHUDProvider.SpacingDirection.RIGHT)).width + e
                                .getPadding(IHUDProvider.SpacingDirection.LEFT) + e
                                .getPadding(IHUDProvider.SpacingDirection.RIGHT)).sum());
                totalHeight += l.stream().mapToInt(e -> e
                        .getDimension(maxWidth - e.getPadding(IHUDProvider.SpacingDirection.LEFT) - e
                                .getPadding(IHUDProvider.SpacingDirection.RIGHT)).height + e
                        .getPadding(IHUDProvider.SpacingDirection.TOP) + e
                        .getPadding(IHUDProvider.SpacingDirection.BOTTOM)).max().getAsInt();
            }
            d = new Dimension(totalWidth, totalHeight);
        } else {
            int part = maxWidth / elements.length;
            if (true)
                part = maxWidth;
            int totalWidth = 0, totalHeight = 0;
            for (HUDElement e : elements) {
                totalWidth += e.getDimension(part - e.getPadding(IHUDProvider.SpacingDirection.LEFT) - e
                        .getPadding(IHUDProvider.SpacingDirection.RIGHT)).width + e
                        .getPadding(IHUDProvider.SpacingDirection.LEFT) + e
                        .getPadding(IHUDProvider.SpacingDirection.RIGHT);
                totalHeight = Math.max(totalHeight,
                        e.getDimension(part - e.getPadding(IHUDProvider.SpacingDirection.LEFT) - e
                                .getPadding(IHUDProvider.SpacingDirection.RIGHT)).height + e
                                .getPadding(IHUDProvider.SpacingDirection.TOP) + e
                                .getPadding(IHUDProvider.SpacingDirection.BOTTOM));
            }
            //				totalWidth -= elements[0].getPadding(Direction.LEFT);
            //				totalWidth -= elements[elements.length - 1].getPadding(Direction.RIGHT);
            int width = totalWidth;
            boolean tooLong = width > maxWidth;
            if (tooLong) {
                double fac = maxWidth / (double) width;
                totalHeight *= fac;
            }
            d = new Dimension(totalWidth, totalHeight);
        }
        return d;
    }

    @Override
    public void draw(int maxWidth) {
        if (lineBreak) {
            int hei = 0;
            boolean firstH = true;
            for (List<HUDElement> l : getElementRows(maxWidth)) {
                int down = 0;
                if (firstH) {
                    firstH = false;
                    GlStateManager.translated(0,
                            down = l.stream().mapToInt(e -> e.getPadding(IHUDProvider.SpacingDirection.TOP)).max()
                                    .getAsInt(),
                            0);
                }
                int back = 0;
                boolean firstW = true;
                for (HUDElement e : l) {
                    GlStateManager.depthMask(false);
                    if (firstW) {
                        firstW = false;
                        int pad = e.getPadding(IHUDProvider.SpacingDirection.LEFT);
                        back += pad;
                        GlStateManager.translated(pad, 0, 0);
                    }
                    int w = e.getDimension(maxWidth - e.getPadding(IHUDProvider.SpacingDirection.LEFT) - e
                            .getPadding(IHUDProvider.SpacingDirection.RIGHT)).width + e
                            .getPadding(IHUDProvider.SpacingDirection.LEFT) + e
                            .getPadding(IHUDProvider.SpacingDirection.RIGHT);
                    back += w;
                    e.draw(maxWidth - e.getPadding(IHUDProvider.SpacingDirection.LEFT) - e
                            .getPadding(IHUDProvider.SpacingDirection.RIGHT));
                    GlStateManager.translated(w, 0, 0);
                }
                int h = l.stream().mapToInt(e -> e
                        .getDimension(maxWidth - e.getPadding(IHUDProvider.SpacingDirection.LEFT) - e
                                .getPadding(IHUDProvider.SpacingDirection.RIGHT)).height + e
                        .getPadding(IHUDProvider.SpacingDirection.TOP) + e
                        .getPadding(IHUDProvider.SpacingDirection.BOTTOM)).max().getAsInt();
                hei += h;
                GlStateManager.translated(0, h - down, 0);
                GlStateManager.translated(-back, 0, 0);
            }
            GlStateManager.translated(0, -hei, 0);
        } else {
            int width = dimension(maxWidth).width;
            int height = dimension(maxWidth).height;
            boolean tooLong = width > maxWidth;
            double fac = maxWidth / (double) width;
            if (tooLong) {
                GlStateManager.scaled(fac, fac, 1);
            }
            int part = maxWidth / elements.length;
            if (true)
                part = maxWidth;
            int down = 0;
            GlStateManager.translated(0,
                    down = Arrays.stream(elements).mapToInt(e -> e.getPadding(IHUDProvider.SpacingDirection.TOP)).max()
                            .getAsInt(),
                    0);
            int back = 0;
            boolean first = true;
            for (HUDElement e : elements) {
                GlStateManager.depthMask(false);
                if (first) {
                    first = false;
                    int pad = e.getPadding(IHUDProvider.SpacingDirection.LEFT);
                    back += pad;
                    GlStateManager.translated(pad, 0, 0);
                }
                Dimension dim = e.getDimension(part - e.getPadding(IHUDProvider.SpacingDirection.LEFT) - e
                        .getPadding(IHUDProvider.SpacingDirection.RIGHT));
                int up = 0;
                int free = height - dim.height - e.getPaddingVertical();
                if (free > 0)
                    up = (int) Math.ceil(free / (double) 2);
                GlStateManager.translated(0, up, 0);
                e.draw(part - e.getPadding(IHUDProvider.SpacingDirection.LEFT) - e
                        .getPadding(IHUDProvider.SpacingDirection.RIGHT));
                int w = dim.width + e.getPadding(IHUDProvider.SpacingDirection.LEFT) + e
                        .getPadding(IHUDProvider.SpacingDirection.RIGHT);
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

}