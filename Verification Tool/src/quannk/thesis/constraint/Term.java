package quannk.thesis.constraint;

public class Term implements ISMTDeclarable {
  enum Type {
    INTEGER, REAL, BOOLEAN
  };

  String name;
  boolean output;
  Term.Type type;

  @Override
  public String toString() {
    return type + "_" + name + "_" + output;
  }

  public String getUserFriendlyString() {
    return name;
  }

  @Override
  public String getSMTDeclare() {
    // example of result: "(declare-const a Int)"
    String t = "(declare-const " + name + " ";
    if (type == Type.INTEGER)
      t += "Int";
    else if (type == Type.REAL)
      t += "Real";
    else if (type == Type.BOOLEAN)
      t += "Bool";
    else
      assert (false);
    return t + ")";
  }

  @Override
  public boolean equals(Object t) {
    return toString().compareTo(((Term) t).toString()) == 0;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}