package vn.edu.vnu.uet.quannk_56.thesis.constraint;

public class Term implements Comparable<Term> {
	enum Type {
		INTEGER, REAL, BOOLEAN
	};

	String name;
	boolean output;
	Term.Type type;

	public String toString() {
		return type + "_" + name + "_" + output;
	}

	public String getSMTDeclare() {
		// (declare-const a Int)
		String t = "(declare-const " + name + " ";
		if (type == Type.INTEGER)
			t += "Int";
		else if (type == Type.REAL)
			t += "Real";
		else if (type == Type.BOOLEAN)
			t += "Bool";
		else
			assert (false);
		return t + ")";
	}

	@Override
	public int compareTo(Term o) {
		return name.compareTo(o.name);
	}
}