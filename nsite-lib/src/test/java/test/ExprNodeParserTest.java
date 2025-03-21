package test;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.lib.nsite.context.NDocContext;
import net.thevpc.nuts.lib.nsite.executor.expr.NDocExprEvaluator;

public class ExprNodeParserTest {
    public static void main(String[] args) {
        Nuts.openWorkspace(args).share();
        String expr = "a=b*2+6*1+m(-2)+1";
        NDocExprEvaluator e = new NDocExprEvaluator();
        Object d = e.eval(expr,new NDocContext());
        System.out.println(d);

    }
}
