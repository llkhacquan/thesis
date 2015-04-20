package quannk.thesis.core;

import quannk.thesis.gui.Console;
import quannk.thesis.gui.MainPage;

public class Logger {
  private static Console console = null;
  private static MainPage mainPage = null;

  public static void setGUI(MainPage main) {
    mainPage = main;
    console = main.console;
  }

  public static boolean isOutputATextBox() {
    return console != null;
  }

  public static void outln(Object content) {
    out(content + "\n");
  }

  public static void outlnInDevMode(Object content) {
    if (Config.DEVELOP_MODE)
      outln(content);
  }

  public static void out(Object content) {
    if (console == null)
      System.out.print(content);
    else
      console.write(content);
  }

  public static void out() {
    out("\n");
  }
  
  public static void messageBox(Object o){
    if (mainPage!=null){
      mainPage.createMessageBox(o);
    } else {
      outln(o);
    }
  }
}
