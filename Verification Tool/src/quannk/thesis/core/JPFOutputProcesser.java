package quannk.thesis.core;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import quannk.thesis.constraint.CNFClause;
import quannk.thesis.constraint.Clause;
import quannk.thesis.constraint.IConstraint;
import quannk.thesis.constraint.PathConstraint;
import quannk.thesis.constraint.Term;

public class JPFOutputProcesser {
  public Vector<PathConstraint> pathConstraints;

  public JPFOutputProcesser(String jpfOutput) {
    pathConstraints = extractPathConditions(jpfOutput);
  }

  public CNFClause getErrorCheckConstraint() {
    CNFClause result = new CNFClause();
    for (PathConstraint path : pathConstraints) {
      result.clauses.add(new Clause("!(" + path.preConditions.toString() + ")"));
    }
    return result;
  }

  private static Vector<PathConstraint> getPathsContrains(String jpfOutput) {
    Vector<PathConstraint> paths = extractPathConditions(jpfOutput);
    return paths;
  }

  private static Set<String> getDeclares(Vector<PathConstraint> paths) {
    Set<String> declares = new HashSet<String>();
    Set<Term> terms = new HashSet<Term>();
    for (PathConstraint path : paths) {
      terms.addAll(path.getTerms());
    }
    for (Term term : terms) {
      declares.add(term.getSMTDeclare());
    }
    return declares;
  }

  private static Vector<String> getDeclares(IConstraint contraints) {
    Set<Term> terms = contraints.getTerms();
    Vector<String> result = new Vector<String>();
    for (Term t : terms) {
      result.add(t.getSMTDeclare());
    }
    return result;
  }

  private static Vector<PathConstraint> extractPathConditions(String jpfOutput) {
    String[] lines = jpfOutput.replaceAll("\n\r", "\n").replaceAll("\r\n", "\n").split("\n");

    Vector<PathConstraint> pathConditions = new Vector<PathConstraint>();

    PathConstraint currentPath = null;
    for (int iLine = 0; iLine < lines.length; iLine++) {
      if (lines[iLine].startsWith("CHECK_O")) {
        String checkOutString = lines[iLine].trim();
        if (currentPath == null)
          currentPath = new PathConstraint();
        currentPath.postConditions.clauses.add(new Clause(checkOutString));
      } else if (lines[iLine].startsWith("path constraint # = ")) {
        int constraintNumber = Integer.parseInt(lines[iLine].substring("path constraint # = ".length()));
        for (int iConstrain = 0; iConstrain < constraintNumber; iConstrain++) {
          iLine++;
          String constrainString = lines[iLine].replaceAll(" &&", "").replaceAll("%NonLinInteger% ", "");
          // remove CONST_
          constrainString = constrainString.replaceAll("CONST_", "");
          currentPath.preConditions.clauses.add(new Clause(constrainString));
        }
        pathConditions.add(currentPath);
        currentPath = null;
      } else {

      }
    }
    return pathConditions;
  }

  private static Vector<String> getContraints(String outOfSymbc, String prePC, String postPC) {
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
}
