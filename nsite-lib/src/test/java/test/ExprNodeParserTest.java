package test;

import net.thevpc.nuts.Nuts;
import net.thevpc.nsite.context.NDocContext;
import net.thevpc.nsite.executor.expr.DefaultNDocExprEvaluator;

public class ExprNodeParserTest {
    public static void main(String[] args) {
        Nuts.openWorkspace(args).share();
        String expr = "a=b*2+6*1+m(-2)+1";
        DefaultNDocExprEvaluator e = new DefaultNDocExprEvaluator();
        Object d = e.eval(expr,new NDocContext());
        System.out.println(d);

    }
}
