package vn.edu.vnu.uet.quannk_56.thesis.constraint;

import java.util.HashSet;
import java.util.Set;

public class PathConstraint implements IConstraint {
	public CNFClause preConditions;
	public CNFClause postConditions;

	public PathConstraint() {
		preConditions = new CNFClause();
		postConditions = new CNFClause();
	}

	public String toString() {
		return preConditions.toString() + " => " + postConditions.toString();
	}

	@Override
	public Set<Term> getTerms() {
		Set<Term> result = new HashSet<Term>();
		result.addAll(preConditions.getTerms());
		result.addAll(postConditions.getTerms());
		return result;
	}
}
