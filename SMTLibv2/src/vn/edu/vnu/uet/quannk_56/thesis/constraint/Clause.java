package vn.edu.vnu.uet.quannk_56.thesis.constraint;

import static vn.edu.vnu.uet.quannk_56.thesis.VerificationTool.*;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

// Each term in clause must begin with CHECKIN_PREFIX or CHECKOUT_PREFIX
// Identifiers must be composed of letters, numbers, the underscore _ and the dollar sign $. 
// Identifiers may only begin with a letter, the underscore or a dollar sign.
public class Clause implements IConstraint{
	public Vector<Term> identifiers = new Vector<Term>();
	public String clause;

	public Clause(String clause) {
		this.clause = clause;
	}

	public Set<Term> getTerms() {
		Set<Term> identifiers = new HashSet<Term>();
		int currentPos = 0;
		while (currentPos < clause.length()) {
			int index = clause.indexOf("CHECK", currentPos);
			if (index < 0)
				break;
			currentPos = index;
			while (currentPos < clause.length()
					&& Character
							.isJavaIdentifierPart(clause.charAt(currentPos)))
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
		return identifiers;
	}

	public Clause() {
		this.clause = "true";
	}

	public String toString() {
		String trimmedClause = clause.replaceAll(CHECKIN_PREFIX, "")
				.replaceAll(CHECKOUT_PREFIX, "");
		return trimmedClause;
	}
}
