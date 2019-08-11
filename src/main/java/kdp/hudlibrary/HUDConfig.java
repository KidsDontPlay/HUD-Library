package kdp.hudlibrary;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import org.apache.commons.lang3.tuple.Pair;

import kdp.hudlibrary.tehud.VanillaImpl;

public class HUDConfig {

    private static ForgeConfigSpec config;
    public static ForgeConfigSpec.IntValue maxHUDs;
    public static ForgeConfigSpec.IntValue visibleDistance;

    public static TileConfig config(TileEntity t) {
        TileConfig tc = configMap.get(t.getClass());
        if (tc != null) {
            return tc;
        }
        TileConfig tc2 = configMap.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(t.getClass()))
                .map(Map.Entry::getValue).findAny().get();
        configMap.put(t.getClass(), tc2);
        return config(t);
    }

    private static final Map<Class<? extends TileEntity>, TileConfig> configMap = new HashMap<>();

    public static void init() {
        Pair<HUDConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(b -> {
            b.push("Client");
            maxHUDs = b.comment("Max amount of HUDs rendering simultaneously").defineInRange("maxHUDs", 10, 1, 100);
            visibleDistance = b.comment("Distance the HUDs are visible within")
                    .defineInRange("visibleDistance", 20, 4, 100);
            b.comment("This section affects vanilla tile entities");
            b.push("Impl");
            for (String s : (Iterable<String>) () -> VanillaImpl.tileClassMap.keySet().stream().sorted().iterator()) {
                b.push(s);
                TileConfig tc = new TileConfig();
                Class<? extends TileEntity> clazz = VanillaImpl.tileClassMap.get(s);
                tc.enabled = b.comment("HUD is visible").define("enabled", true);
                tc.focus = b.comment("Player must look at the block to see HUD")
                        .define("focus", VanillaImpl.defaultFocus.getBoolean(clazz));
                tc.sneak = b.comment("Player must sneak to see HUD")
                        .define("sneak", VanillaImpl.defaultSneak.getBoolean(clazz));
                tc.smooth = b.comment("Smooth rotation of HUD").define("smooth", false);
                tc.background = b.comment("Background color in hex")
                        .define("backgroundColor", VanillaImpl.defaultBack.get(clazz));
                tc.fontColor = b.comment("Font color in hex").define("fontColor", VanillaImpl.defaultFont.get(clazz));
                tc.scale = b.comment("Scale of HUD").defineInRange("scale", 1., .1, 10.);
                tc.width = b.comment("Width of HUD").defineInRange("width", 120, 32, 1000);
                configMap.put(clazz, tc);
                b.pop();
            }
            b.pop();
            b.pop();
            return null;
        });
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, config = pair.getValue());
    }

    public static class TileConfig {
        private ForgeConfigSpec.BooleanValue enabled;
        private ForgeConfigSpec.ConfigValue<String> background;
        private ForgeConfigSpec.ConfigValue<String> fontColor;
        private ForgeConfigSpec.DoubleValue scale;
        private ForgeConfigSpec.IntValue width;
        private ForgeConfigSpec.BooleanValue focus;
        private ForgeConfigSpec.BooleanValue sneak;
        private ForgeConfigSpec.BooleanValue smooth;

        public boolean isEnabled() {
            return enabled.get();
        }

        public int getBackground() {
            String s = background.get();
            s = s.replace("#", "");
            return (int) Long.parseLong(s, 16);
        }

        public int getFontColor() {
            String s = fontColor.get();
            s = s.replace("#", "");
            return (int) Long.parseLong(s, 16);
        }

        public double getScale() {
            return scale.get();
        }

        public int getWidth() {
            return width.get();
        }

        public boolean isFocus() {
            return focus.get();
        }

        public boolean isSneak() {
            return sneak.get();
        }

        public boolean isSmooth() {
            return smooth.get();
        }
    }
}
