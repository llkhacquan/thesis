package quannk.thesis.core;


public class TestMethod {
  enum Type {
    VOID, INT, BOOLEAN, DOUBLE;

    @Override
    public String toString() {
      switch (this) {
      case VOID:
        return "void";
      case INT:
        return "int";
      case BOOLEAN:
        return "boolean";
      case DOUBLE:
        return "double";
      default:
        return null;
      }
    }

    public static Type fromString(String t) {
      if (t.compareTo("int") == 0) {
        return INT;
      } else if (t.compareTo("void") == 0) {
        return VOID;
      } else if (t.compareTo("double") == 0) {
        return DOUBLE;
      } else if (t.compareTo("boolean") == 0) {
        return BOOLEAN;
      } else
        return null;
    }
  }

  class Parameter {
    String name;
    Type type;
  }

  public Parameter parameters[];
  public Type returnType;

  public TestMethod(String line) {
    int i = line.indexOf("(");
    int j = line.indexOf(")");
    String preline = line.substring(0, i).trim(); // double testMethod
    String para = line.substring(i + 1, j).trim(); // double a, double b, double c

    { // extract returnType
      String ts[] = preline.split(" |\t");
      assert (ts.length >= 2);
      for (int i1 = 1; i1 < ts.length; i1++) {
        if (ts[i1].compareTo("testMethod") == 0) {
          returnType = Type.fromString(ts[i1 - 1]);
        }
      }
      assert (returnType != null);
    }

    { // extract parameters info
      String ts[] = para.split(",");
      parameters = new Parameter[ts.length];
      for (int i1 = 0; i1 < ts.length; i1++) {
        String ts2[] = ts[i1].trim().split(" |\t");
        assert (ts2.length == 2);
        parameters[i1] = new Parameter();
        parameters[i1].type = Type.fromString(ts2[0]);
        parameters[i1].name = ts2[1];
      }
    }
  }
}
