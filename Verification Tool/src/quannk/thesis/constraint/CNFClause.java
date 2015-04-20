package quannk.thesis.constraint;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class CNFClause extends ConstraintAdappter {
  public Vector<Clause> clauses = new Vector<Clause>();

  public void setContrain(String s) throws Exception {
    throw new Exception("Not implemented.");
  }

  @Override
  public String getSMTAsserts() {
    StringBuilder sb = new StringBuilder();
    for (Clause c : clauses) {
      sb.append(c.getSMTAsserts() + "\n");
    }
    return sb.toString();
  }

  @Override
  public String getUserFriendlyString() {
    StringBuilder contrain = new StringBuilder();
    if (clauses.size() == 0)
      return "true";
    else if (clauses.size() == 1) {
      return clauses.elementAt(0).getUserFriendlyString();
    } else {
      for (Clause c : clauses) {
        contrain.append(" && (" + c.getUserFriendlyString() + ")");
      }
      // remove " && " at the beginning of the string
      contrain.replace(0, 4, "");
    }
    return contrain.toString();
  }

  @Override
  public String toString() {
    StringBuilder contrain = new StringBuilder();
    if (clauses.size() == 0)
      return "true";
    else if (clauses.size() == 1) {
      return clauses.elementAt(0).toString();
    } else {
      for (Clause c : clauses) {
        contrain.append(" && (" + c + ")");
      }
      // remove " && " at the beginning of the string
      contrain.replace(0, 4, "");
    }
    return contrain.toString();
  }

  @Override
  public Set<Term> getTerms() {
    Set<Term> result = new HashSet<Term>();
    for (Clause clause : clauses) {
      result.addAll(clause.getTerms());
    }
    return result;
  }
}
