package quannk.thesis.gui;

import org.eclipse.swt.custom.StyledText;

public class Console {
  private StyledText destination;
  private static Console console = null;

  private Console(StyledText destination) {
    assert (destination != null);
    this.destination = destination;
    console = this;
  }

  public static Console getConsole(StyledText destination) {
    if (console == null) {
      return console = new Console(destination);
    } else {
      return console;
    }
  }

  public static Console getConsole() {
    return console;
  }

  public void writeln(Object s) {
    destination.append(s + "\n");
  }

  public void write(Object s) {
    destination.append(s.toString());
  }

  public void clear() {
    destination.setText("");
  }
}
