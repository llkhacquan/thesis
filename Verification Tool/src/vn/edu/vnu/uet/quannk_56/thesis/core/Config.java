package vn.edu.vnu.uet.quannk_56.thesis.core;

import java.net.URL;

public final class Config {
	public final boolean DEBUG;

	private static Config config = null;

	private Config() {
		URL t = Config.class.getResource("Config.class");
		if (t.toString().contains(".jar!")) {
			DEBUG = false;
		} else {
			DEBUG = true;
		}
	}

	public static Config getConfig() {
		if (config == null) {
			config = new Config();
		}

		return config;
	}

	public static boolean isDebug() {
		if (Config.config != null)
			return config.DEBUG;
		else
			return getConfig().DEBUG;
	}
}
