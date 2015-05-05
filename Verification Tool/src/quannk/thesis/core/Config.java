package quannk.thesis.core;

import java.net.URL;

public final class Config {
  public final boolean RUN_FROM_JAR_FILE;
  public static boolean DEVELOP_MODE = true;

  private static Config config = null;

  private Config() {
    URL t = Config.class.getProtectionDomain().getCodeSource().getLocation();
    System.out.println("Config.class = " + t.toString());
    if (!t.toString().contains("rsrc:./")) {
      System.out.println("RUN_FROM_JAR_FILE = false");
      RUN_FROM_JAR_FILE = false;
    } else {
      System.out.println("RUN_FROM_JAR_FILE = true");
      RUN_FROM_JAR_FILE = true;
    }
  }

  public static Config getConfig() {
    if (config == null) {
      config = new Config();
    }
    return config;
  }

  public static boolean runFromJarFile() {
    if (Config.config != null)
      return config.RUN_FROM_JAR_FILE;
    else
      return getConfig().RUN_FROM_JAR_FILE;
  }
}
