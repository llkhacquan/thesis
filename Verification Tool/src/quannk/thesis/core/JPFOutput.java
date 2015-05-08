package quannk.thesis.core;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import quannk.thesis.constraint.CNFClause;
import quannk.thesis.constraint.Clause;
import quannk.thesis.constraint.PathConstraint;
import quannk.thesis.constraint.Term;

public class JPFOutput {
  public Vector<PathConstraint> pathConstraints;

  public JPFOutput(String jpfOutput) {
    pathConstraints = extractPathConditions(jpfOutput);
  }

  public CNFClause getErrorCheckConstraint() {
    CNFClause result = new CNFClause();
    for (PathConstraint path : pathConstraints) {
      result.clauses.add(new Clause("!(" + path.preConditions.toString() + ")"));
    }
    return result;
  }

  public Clause getPreConstraintOfCode() {
    StringBuilder sb = new StringBuilder();
    for (PathConstraint path : pathConstraints) {
      sb.append(" || (" + path.preConditions.toString() + ")");
    }
    return new Clause(sb.substring(4));
  }

  public String getDeclares() {
    Set<String> declares = new HashSet<String>();
    Set<Term> terms = new HashSet<Term>();
    for (PathConstraint path : pathConstraints) {
      terms.addAll(path.getTerms());
    }
    for (Term term : terms) {
      declares.add(term.getSMTDeclare());
    }
    StringBuilder sb = new StringBuilder();
    for (String s : declares)
      sb.append(s + "\n");
    return sb.toString();
  }

  private static class RealConstant {
    int number;
    String value;

    public RealConstant(int n, String v) {
      number = n;
      value = v;
    }

    public String getName() {
      return "REAL_" + number;
    }

    public String getValue() {
      return value;
    }
  }

  private static Vector<RealConstant> reals = new Vector<RealConstant>();

  private static Vector<String> extractPreConditions(String constraints) {
    Vector<String> result = new Vector<String>();
    String[] lines = constraints.split("\n");

    for (int i = 0; i < lines.length; i++) {
      String[] ss = lines[i].split(" == ");
      if (ss.length == 2) {
        int n = -1;
        String v = null;
        if (ss[0].contains("REAL_")) {
          n = Integer.parseInt(ss[0].substring(5));
          v = ss[1];
        } else if (ss[1].contains("REAL_")) {
          n = Integer.parseInt(ss[1].substring(5));
          v = ss[0];
        }

        if (n >= 0) {
          boolean existed = false;
          for (RealConstant real : reals) {
            if (real.number == n) {
              existed = true;
              break;
            }
          }
          if (!existed) {
            reals.add(new RealConstant(n, v));
            lines[i] = "";
          } else {
            for (RealConstant real : reals) {
              lines[i] = lines[i].replace(real.getName(), real.getValue());
            }
          }
        }
      }
    }

    for (int i = 0; i < lines.length; i++) {
      for (RealConstant real : reals) {
        lines[i] = lines[i].replaceAll(real.getName(), real.getValue());
      }
      if (lines[i].length() > 0)
        result.add(lines[i]);
    }
    return result;
  }

  private static Vector<PathConstraint> extractPathConditions(String jpfOutput) {
    String[] lines = jpfOutput.replaceAll("\n\r", "\n").replaceAll("\r\n", "\n").replaceAll("CONST_", "").split("\n");
    Vector<PathConstraint> pathConditions = new Vector<PathConstraint>();
    PathConstraint currentPath = null;

    String preCondition = null;
    String postCondition = null;

    for (int iLine = 0; iLine < lines.length; iLine++) {
      if (lines[iLine].startsWith("CHECK_O")) {
        if (currentPath == null) {
          currentPath = new PathConstraint();
          pathConditions.add(currentPath);
          reals.clear();
          postCondition = "";
          preCondition = "";
        }
        String constrainString = lines[iLine].trim();
        postCondition += constrainString + "\n";
      } else if (lines[iLine].startsWith("path constraint # = ")) {
        int constraintNumber = Integer.parseInt(lines[iLine].substring("path constraint # = ".length()));
        preCondition = "";
        for (int iConstrain = 0; iConstrain < constraintNumber; iConstrain++) {
          iLine++;
          preCondition += lines[iLine].replaceAll(" &&", "").replaceAll("%NonLinInteger% ", "").trim() + "\n";
        }
        Vector<String> ss = extractPreConditions(preCondition);
        for (String s : ss)
          currentPath.preConditions.clauses.add(new Clause(s));
        for (String line : postCondition.split("\n")) {
          for (RealConstant real : reals) {
            line = line.replaceAll(real.getName(), real.getValue());
          }
          currentPath.postConditions.clauses.add(new Clause(line));
        }
        Logger.outlnInDevMode(currentPath + "\n");
        currentPath = null;
      }
    }
    return pathConditions;
  }
}
