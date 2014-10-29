package vn.edu.vnu.uet.quannk_56.scientific_research;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

public class MyJPFLauncher {
	public static void main(String[] args) {
		SortedSet<String> codes = new TreeSet<String>();
		String fileJPF;
		fileJPF = "../jpf-symbc/src/examples/demo/NumericExample.jpf";
		String jpfOutput = runJPFSymbc(fileJPF);
		System.out.println(jpfOutput);
		Vector<String> v_Contraints = getContraints(jpfOutput, "PrePC",
				"PostPC");
		SortedSet<String> symbols = new TreeSet<String>();

		String s1, s2, t;
		Vector<String> asserts = new Vector<String>();
		for (String contraint : v_Contraints) {
			System.out.println("Contraints:\n" + contraint + "\n");
			codes.clear();
			String lines[] = contraint.split("\\r?\\n");
			int i;
			int count;
			s1 = s2 = "";
			for (count = 0, i = 0; i < lines.length; i++) {
				count++;
				if (lines[i].contains("=>"))
					break;
				t = getPrefix(lines[i], symbols);
				s1 += " (" + t + ") ";
			}
			if (count > 1)
				s1 = "(and " + s1 + ")";
			for (count = 0, i++; i < lines.length; i++) {
				count++;
				t = getPrefix(lines[i], symbols);
				s2 += " (" + t + ") ";
			}
			if (count > 1)
				s2 = "(and " + s2 + ")";
			asserts.add(" =>\n" + s1 + "\n" + s2 + "\n");
		}
		for (String symbol : symbols) {
			System.out.println(SMTConvert.getDeclaration(symbol));
		}
		for (String s : asserts)
			System.out.println(SMTConvert.getAssert(s));
		System.out.println("(check-sat)");
	}

	public static Vector<String> getContraints(String outOfSymbc, String prePC,
			String postPC) {
		Vector<String> result = new Vector<String>();
		String[] lines = outOfSymbc.split("\\r?\\n");

		for (int iLine = 0; iLine < lines.length; iLine++) {
			if (lines[iLine].contains(prePC + "constraint # = ")) {
				int j = iLine;
				while (!lines[j].contains(postPC))
					j++;
				String s = "";
				for (int t = iLine + 1; t < j; t++) {
					s += lines[t] + "\n";
				}
				// System.out.println(s);
				iLine = j;
				result.add(s);
			}
		}
		return result;
	}

	/**
	 * return the SMT codes of a constraint
	 * 
	 * @param contraint
	 * @return
	 */
	public static String getPrefix(String contraint, SortedSet<String> symbols) {
		String prefixCondition;

		Vector<String> t = new Vector<String>();
		prefixCondition = InfixToPrefix.InfixToPrefix(contraint, t);
		symbols.addAll(t);

		return prefixCondition.replace("==", "=");
	}

	/**
	 * return output when run JPFSymbc on a jpf file
	 * 
	 * @param fileJPF
	 * @return
	 */
	public static String runJPFSymbc(String fileJPF) {
		// Create a stream to hold the output
		ByteArrayOutputStream newOut = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(newOut);
		// IMPORTANT: Save the old System.out!
		PrintStream oldOut = System.out;
		// Tell Java to use your special stream
		System.setOut(ps);

		gov.nasa.jpf.JPF.main(new String[] { fileJPF });

		// Put things back
		System.out.flush();
		System.setOut(oldOut);
		return newOut.toString();
	}
}
