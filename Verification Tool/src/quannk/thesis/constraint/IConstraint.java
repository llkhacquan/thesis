package quannk.thesis.constraint;

import java.util.Set;

public interface IConstraint extends ISMTDeclarable, ISMTAssertable {
  Set<Term> getTerms();

  String convertToPrefix();

  String getUserFriendlyString();
}
