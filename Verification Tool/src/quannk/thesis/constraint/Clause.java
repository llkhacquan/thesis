package quannk.thesis.constraint;

import static demo.Tester.CHECKIN_PREFIX;
import static demo.Tester.CHECKOUT_PREFIX;

import java.util.HashSet;
import java.util.Set;

import quannk.thesis.convert.InfixToPrefix;

// Each term in clause must begin with CHECKIN_PREFIX or CHECKOUT_PREFIX
// Identifiers must be composed of letters, numbers, the underscore _ and the dollar sign $. 
// Identifiers may only begin with a letter, the underscore or a dollar sign.
public class Clause extends ConstraintAdappter {
  public String clause;

  public Clause(String clause) {
    this.clause = clause;
  }

  @Override
  public Set<Term> getTerms() {
    Set<Term> identifiers = new HashSet<Term>();
    int currentPos = 0;
    while (currentPos < clause.length()) {
      int index = clause.indexOf("CHECK", currentPos);
      if (index < 0)
        break;
      currentPos = index;
      while (currentPos < clause.length() && Character.isJavaIdentifierPart(clause.charAt(currentPos)))
        currentPos++;
      Term i = new Term();
      String id = clause.substring(index, currentPos);
      i.name = id.substring(CHECKIN_PREFIX.length());
      if (id.charAt(6) == 'I') {
        i.output = false;
      } else if (id.charAt(6) == 'O') {
        i.output = true;
      } else
        assert (false);

      if (id.charAt(8) == 'I') {
        i.type = Term.Type.INTEGER;
      } else if (id.charAt(8) == 'R') {
        i.type = Term.Type.REAL;
      } else if (id.charAt(8) == 'B') {
        i.type = Term.Type.BOOLEAN;
      } else
        assert (false);
      identifiers.add(i);
    }

    currentPos = 0;
    while (currentPos < clause.length()) {
      int index = clause.indexOf("REAL", currentPos);
      if (index < 0)
        break;
      currentPos = index;
      while (currentPos < clause.length() && Character.isJavaIdentifierPart(clause.charAt(currentPos)))
        currentPos++;
      Term i = new Term();
      String id = clause.substring(index, currentPos);
      i.name = id;
      i.output = true;
      i.type = Term.Type.REAL;
      identifiers.add(i);
    }

    return identifiers;
  }

  public Clause() {
    this.clause = "true";
  }

  @Override
  public String toString() {
    return clause;
  }

  @Override
  public String getUserFriendlyString() {
    String trimmedClause = clause.replaceAll(CHECKIN_PREFIX, "").replaceAll(CHECKOUT_PREFIX, "");
    return trimmedClause;
  }

  @Override
  public String convertToPrefix() {
    return InfixToPrefix.parse(getUserFriendlyString());
  }
}
