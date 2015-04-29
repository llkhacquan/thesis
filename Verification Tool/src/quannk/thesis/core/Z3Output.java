package quannk.thesis.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import com.microsoft.z3.Status;

public class Z3Output {
  Status s;
  Vector<Declare> declares = new Vector<Declare>();

  public static class Declare {
    String name;
    String type;
    String value;

    double getRealValue() {
      return 0;
    }

    int getIntValue() {
      return 0;

    }

    boolean getBooleanValue() {
      return true;
    }

    public String toString() {
      return name + " = " + value;
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Declare d : declares) {
      sb.append(d + "\n");
    }
    return sb.toString();
  }

  public static Z3Output z3OutputProcess(Vector<String> output) {
    Z3Output z3Output = new Z3Output();
    assert (output.size() >= 1);
    if (output.get(0).compareTo("unsat") == 0) {
      z3Output.s = Status.UNSATISFIABLE;
    } else if (output.get(0).compareTo("unknown") == 0) {
      z3Output.s = Status.UNKNOWN;
    } else if (output.get(0).compareTo("sat") == 0) {
      z3Output.s = Status.SATISFIABLE;
      int numberOfDefinitions = (output.size() - 3) / 2;
      for (int i = 0; i < numberOfDefinitions; i++) {
        String l1 = output.get(2 + i * 2).trim();
        String l2 = output.get(2 + i * 2 + 1).trim();

        int index = "(define-fun ".length();
        while (Character.isJavaIdentifierPart(l1.charAt(index))) {
          index++;
        }
        Z3Output.Declare d = new Z3Output.Declare();
        z3Output.declares.add(d);
        d.name = l1.substring("(define-fun ".length(), index);
        d.type = l1.substring(index + 4);
        d.value = l2.substring(0, l2.length() - 1);
      }
    } else {
      z3Output.s = null;
    }
    return z3Output;
  }

  public static Vector<String> runZ3(String filename) throws IOException {
    Vector<String> result = new Vector<String>();
    String pathToZ3 = "C:\\Users\\wind\\Desktop\\z3\\bin\\z3.exe";
    ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", pathToZ3 + " -smt2 " + filename);
    builder.redirectErrorStream(true);
    Process p = builder.start();
    BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line;
    while (true) {
      line = r.readLine();
      if (line == null) {
        break;
      }
      result.add(line);
    }
    return result;
  }
}