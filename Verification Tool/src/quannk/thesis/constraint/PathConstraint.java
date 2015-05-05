package quannk.thesis.constraint;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import quannk.thesis.convert.InfixToPrefix;
import quannk.thesis.convert.ParseException;

public class PathConstraint extends ConstraintAdappter {
  public CNFClause preConditions;
  public CNFClause postConditions;

  public PathConstraint() {
    preConditions = new CNFClause();
    postConditions = new CNFClause();
  }

  @Override
  public String toString() {
    return "(" + preConditions + ") => (" + postConditions + ")";
  }

  @Override
  public String getUserFriendlyString() {
    return "(" + preConditions.getUserFriendlyString() + ") => (" + postConditions.getUserFriendlyString() + ")";
  }

  @Override
  public Set<Term> getTerms() {
    Set<Term> result = new HashSet<Term>();
    result.addAll(preConditions.getTerms());
    result.addAll(postConditions.getTerms());
    return result;
  }

  @Override
  public String convertToPrefix() {
    try {
      return InfixToPrefix.parse(getUserFriendlyString());
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  public void removeTempRealAndInt() {
    // Vector<Clause> clauses = new Vector<Clause>();
    // clauses.addAll(preConditions.clauses);
    // clauses.addAll(postConditions.clauses);
    for (Clause c : preConditions.clauses) {
      if (c.toString().contains("REAL_")) {
        String ss[] = c.toString().split(" ");
        if (ss.length == 3 && ss[1].startsWith("=")) {
          
        }
      }
    }
  }
}
