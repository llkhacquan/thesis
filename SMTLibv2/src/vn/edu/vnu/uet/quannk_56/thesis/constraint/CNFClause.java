package vn.edu.vnu.uet.quannk_56.thesis.constraint;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import vn.edu.vnu.uet.quannk_56.thesis.convert.InfixToPrefix;

public class CNFClause implements IConstraint {
	public Vector<Clause> clauses = new Vector<Clause>();

	public void setContrain(String s) throws Exception {
		throw new Exception("Not implemented.");
	}

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
			contrain.replace(0, 4, "");
		}
		return contrain.toString();
	}

	public Set<Term> getTerms() {
		Set<Term> result = new HashSet<Term>();
		for (Clause clause : clauses) {
			result.addAll(clause.getTerms());
		}
		return result;
	}

	@Override
	public String convertToPrefix() {
		return InfixToPrefix.parse(toString());
	}
}
