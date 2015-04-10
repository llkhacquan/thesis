package vn.edu.vnu.uet.quannk_56.thesis.constraint;

import java.util.Set;
import java.util.Vector;

public interface IConstraint {
	public Set<Term> getTerms();
	
	public String convertToPrefix();
	
	// public Vector<String> getSMTAsserts();
}
