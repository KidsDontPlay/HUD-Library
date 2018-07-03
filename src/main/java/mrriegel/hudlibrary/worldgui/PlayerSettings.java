package mrriegel.hudlibrary.worldgui;

import java.util.HashSet;
import java.util.Set;

public class PlayerSettings {
	public static final PlayerSettings INSTANCE = new PlayerSettings();
	public Set<WorldGui> guis = new HashSet<>();
	public double scale = .0065;
}
