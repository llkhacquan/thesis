package quannk.thesis.core;

import java.util.HashMap;
import java.util.LinkedList;

import com.microsoft.z3.AST;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

public class Z3Runner {
  public static String z3Run(String input) throws Z3Exception {
    StringBuilder sb = new StringBuilder();
    String filename = null;
    System.out.println("SMT2 File test ");
    System.gc();
    {
      HashMap<String, String> cfg = new HashMap<String, String>();
      cfg.put("model", "true");
      Context ctx = new Context(cfg);
      Expr a = ctx.parseSMTLIB2File(filename, null, null, null, null);
      // Iterate over the formula.
      LinkedList<Expr> q = new LinkedList<Expr>();
      q.add(a);
      int cnt = 0;
      while (q.size() > 0) {
        AST cur = q.removeFirst();
        cnt++;
        if (cur.getClass() == Expr.class)
          if (!(cur.isVar()))
            for (Expr c : ((Expr) cur).getArgs())
              q.add(c);
      }
      System.out.println(cnt + " ASTs");
    }
    return sb.toString();
  }

  public static void main(String... args) throws Exception {
    HashMap<String, String> cfg = new HashMap<String, String>();
    cfg.put("model", "true");
    Context ctx = new Context(cfg);

    System.out.println("FindModelExample1");

    BoolExpr x = ctx.mkBoolConst("x");
    BoolExpr y = ctx.mkBoolConst("y");
    BoolExpr x_xor_y = ctx.mkXor(x, y);

    Model model = check(ctx, x_xor_y, Status.SATISFIABLE);
    System.out.println("x = " + model.evaluate(x, false) + ", y = " + model.evaluate(y, false));
  }

  static Model check(Context ctx, BoolExpr f, Status sat) throws Exception {
    Solver s = ctx.mkSolver();
    s.add(f);
    if (s.check() != sat)
      throw new Exception("not sat");
    if (sat == Status.SATISFIABLE)
      return s.getModel();
    else
      return null;
  }
}
