package kdp.hudlibrary;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import net.minecraft.tileentity.BarrelTileEntity;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.BlastFurnaceTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.SmokerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;

import org.apache.commons.lang3.tuple.Pair;

public class ModConfig {

    private static ForgeConfigSpec config;
    public static ForgeConfigSpec.IntValue maxHUDs;
    public static ForgeConfigSpec.IntValue visibleDistance;

    public static TileConfig config(TileEntity t) {
        return configMap.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(t.getClass())).findAny()
                .map(e -> e.getValue()).orElse(null);
    }

    public static final BiMap<String, Class<? extends TileEntity>> biMap = ImmutableBiMap.<String, Class<? extends TileEntity>>builder()
            .put("furnace", FurnaceTileEntity.class).put("blastFurnace", BlastFurnaceTileEntity.class)
            .put("smoker", SmokerTileEntity.class).put("campfire", CampfireTileEntity.class)
            .put("beacon", BeaconTileEntity.class).put("brewingStand", BrewingStandTileEntity.class)
            .put("barrel", BarrelTileEntity.class).put("dispenser", DispenserTileEntity.class)
            .put("chest", ChestTileEntity.class).put("hopper", HopperTileEntity.class).build();
    private static final Map<Class<? extends TileEntity>, TileConfig> configMap = new HashMap<>();

    public static void init() {
        Pair<ModConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(b -> {
            b.push("Client");
            maxHUDs = b.comment("Max amount of HUDs rendering simultaneously").defineInRange("maxHUDs", 10, 1, 100);
            visibleDistance = b.comment("Distance the HUDs are visible within")
                    .defineInRange("visibleDistance", 20, 4, 100);
            b.comment("This section affects vanilla tile entities");
            b.push("Impl");
            for (String s : (Iterable<String>) () -> biMap.keySet().stream().sorted().iterator()) {
                b.push(s);
                TileConfig tc = new TileConfig();
                tc.enabled = b.comment("HUD is visible").define("enabled", true);
                tc.focus = b.comment("HUD needs focus to be displayed").define("focus", false);
                tc._360 = b.define("360", false);
                tc.background = b.comment("Background color in hex").define("backgroundColor", "#558470FF");
                tc.scale = b.comment("Scale of HUD").defineInRange("scale", 1., .1, 10.);
                tc.width = b.comment("Width of HUD").defineInRange("width", 120, 32, 1000);
                configMap.put(biMap.get(s), tc);
                b.pop();
            }
            b.pop();
            b.pop();
            return null;
        });
        ModLoadingContext.get()
                .registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT, config = pair.getValue());
    }

    public static class TileConfig {
        private ForgeConfigSpec.BooleanValue enabled;
        private ForgeConfigSpec.ConfigValue<String> background;
        private ForgeConfigSpec.DoubleValue scale;
        private ForgeConfigSpec.IntValue width;
        private ForgeConfigSpec.BooleanValue focus;
        private ForgeConfigSpec.BooleanValue _360;

        public boolean isEnabled() {
            return enabled.get();
        }

        public int getBackground() {
            String s = background.get();
            s = s.replace("#", "");
            return Integer.valueOf(s, 16);
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

        public boolean is360() {
            return _360.get();
        }
    }
}
