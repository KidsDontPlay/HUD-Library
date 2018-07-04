package mrriegel.hudlibrary.worldgui;

import java.util.HashSet;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public class PlayerSettings {
	public static final PlayerSettings INSTANCE = new PlayerSettings();
	public Set<WorldGui> guis = new HashSet<>();
//	public double scale = .0065;
	public Object2DoubleMap<Class<?>> scaleMap=new Object2DoubleOpenHashMap<>();
	public WorldGui focusedGui = null;
	
	private PlayerSettings() {
		scaleMap.defaultReturnValue(.0065);
	}
}
