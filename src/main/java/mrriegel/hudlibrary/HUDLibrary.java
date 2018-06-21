package mrriegel.hudlibrary;

import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = HUDLibrary.MODID, name = HUDLibrary.MODNAME, version = HUDLibrary.VERSION, acceptedMinecraftVersions = "[1.12,1.13)")
@EventBusSubscriber
public class HUDLibrary {
	public static final String MODID = "hudlibrary";
	public static final String VERSION = "1.0.0";
	public static final String MODNAME = "HUD Library";

	@Instance(HUDLibrary.MODID)
	public static HUDLibrary instance;

	public static boolean all, showChance, showMinMax, multithreaded;
	public static int iteration;
	public static Set<String> blacklist;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		all = config.getBoolean("allDrops", Configuration.CATEGORY_CLIENT, false, "Show block drops of any block.");
		showChance = config.getBoolean("showChance", Configuration.CATEGORY_CLIENT, true, "Show chance of drops.");
		showMinMax = config.getBoolean("showMinMax", Configuration.CATEGORY_CLIENT, true, "Show minimum and maximum of drops.");
		multithreaded = config.getBoolean("multithreaded", Configuration.CATEGORY_CLIENT, true, "Multithreaded calculation of drops");
		iteration = config.getInt("iteration", Configuration.CATEGORY_CLIENT, 4000, 1, 99999, "Number of calculation. The higher the more precise the chance.");
		blacklist = Sets.newHashSet(config.getStringList("blacklist", Configuration.CATEGORY_CLIENT, new String[] { "flatcoloredblocks", "chisel", "xtones", "wallpapercraft", "sonarcore", "microblockcbe" }, "Mod IDs of mods that won't be scanned."));
		if (config.hasChanged())
			config.save();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) throws IOException {
	}

}
