package quannk.thesis.constraint;

import java.util.HashSet;
import java.util.Set;

import quannk.thesis.convert.InfixToPrefix;

public class ConstraintAdappter implements IConstraint {

  @Override
  public String getSMTDeclare() {
    StringBuilder sb = new StringBuilder();
    Set<Term> terms = getTerms();
    for (Term t : terms) {
      sb.append(t.getSMTDeclare() + "\n");
    }
    return sb.toString();
  }

  @Override
  public Set<Term> getTerms() {
    return new HashSet<Term>();
  }

  @Override
  public String convertToPrefix() {
    return InfixToPrefix.parse(getUserFriendlyString());
  }

  @Override
  public String getSMTAsserts() {
    String result = "(assert (" + convertToPrefix() + "))";
    return result.replaceAll("!", "not").replaceAll("\\&\\&", "and").replaceAll("\\|\\|", "or").replaceAll("==", "=");
  }

  @Override
  public String getUserFriendlyString() {
    // TODO Auto-generated method stub
    return null;
  }

}
