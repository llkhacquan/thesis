package vn.edu.vnu.uet.quannk_56.thesis;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import vn.edu.vnu.uet.quannk_56.thesis.constraint.CNFClause;
import vn.edu.vnu.uet.quannk_56.thesis.constraint.Clause;
import vn.edu.vnu.uet.quannk_56.thesis.constraint.IConstraint;
import vn.edu.vnu.uet.quannk_56.thesis.constraint.PathConstraint;
import vn.edu.vnu.uet.quannk_56.thesis.constraint.Term;
import vn.edu.vnu.uet.quannk_56.thesis.convert.InfixToPrefix;
import vn.edu.vnu.uet.quannk_56.thesis.core.Config;

public class JPFDemo {
	public static void run(String fileJPF) {
		if (fileJPF == null || fileJPF == "")
			fileJPF = "src/demo/test.jpf";
		String jpfOutput = runJPFSymbc(fileJPF);

		if (Config.isDebug())
			System.out.println("JPF Output:\n" + jpfOutput);

		Vector<PathConstraint> paths = printPathsContrains(jpfOutput);

		printPathCoverageContraint(paths);

		printDeclares(paths);
	}

	public static void printPathCoverageContraint(Vector<PathConstraint> paths) {
		CNFClause errorCheckingCondition = getErrorCheckingCondition(paths);
		System.out
				.println("\terrorCheckingCondition: if this is SAT, there is error!");
		System.out.println(errorCheckingCondition);

		System.out.println(InfixToPrefix.parse(errorCheckingCondition
				.toString()));
	}

	public static Vector<PathConstraint> printPathsContrains(String jpfOutput) {
		Vector<PathConstraint> paths = extractPathConditions(jpfOutput);

		System.out.println("\tLogic function of source code");
		for (PathConstraint path : paths) {
			System.out.println(path);
		}
		return paths;
	}

	public static void printDeclares(Vector<PathConstraint> paths) {
		Set<Term> terms = new HashSet<Term>();
		for (PathConstraint path : paths) {
			terms.addAll(path.getTerms());
		}
		for (Term term : terms) {
			System.out.println(term.getSMTDeclare());
		}
	}

	public static Vector<String> printDeclares(IConstraint contraints) {
		Set<Term> terms = contraints.getTerms();
		Vector<String> result = new Vector<String>();
		for (Term t : terms) {
			result.add(t.getSMTDeclare());
		}
		return result;
	}

	public static CNFClause getErrorCheckingCondition(
			Vector<PathConstraint> paths) {
		CNFClause result = new CNFClause();
		for (PathConstraint path : paths) {
			result.clauses.add(new Clause("!(" + path.preConditions.toString()
					+ ")"));
		}
		return result;
	}

	public static Vector<PathConstraint> extractPathConditions(String jpfOutput) {
		String[] lines = jpfOutput.replaceAll("\n\r", "\n")
				.replaceAll("\r\n", "\n").split("\n");

		Vector<PathConstraint> pathConditions = new Vector<PathConstraint>();

		PathConstraint currentPath = null;
		for (int iLine = 0; iLine < lines.length; iLine++) {
			if (lines[iLine].startsWith("CHECK_O")) {
				String checkOutString = lines[iLine].replaceAll("\n", "");
				if (currentPath == null)
					currentPath = new PathConstraint();
				currentPath.postConditions.clauses.add(new Clause(
						checkOutString));
			} else if (lines[iLine].startsWith("path constraint # = ")) {
				int constraintNumber = Integer.parseInt(lines[iLine]
						.substring("path constraint # = ".length()));
				for (int iConstrain = 0; iConstrain < constraintNumber; iConstrain++) {
					iLine++;
					String constrainString = lines[iLine].replaceAll(" &&", "")
							.replaceAll("%NonLinInteger% ", "");
					// remove CONST_
					constrainString = constrainString.replaceAll("CONST_", "");
					currentPath.preConditions.clauses.add(new Clause(
							constrainString));
				}
				pathConditions.add(currentPath);
				currentPath = null;
			} else {

			}
		}
		return pathConditions;
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

				iLine = j;
				result.add(s);
			}
		}
		return result;
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
