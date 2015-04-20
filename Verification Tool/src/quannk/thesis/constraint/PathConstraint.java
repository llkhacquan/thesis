package quannk.thesis.constraint;

import java.util.HashSet;
import java.util.Set;

import quannk.thesis.convert.InfixToPrefix;

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
    return InfixToPrefix.parse(getUserFriendlyString());
  }
}
