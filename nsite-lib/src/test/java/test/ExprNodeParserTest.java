package test;

import net.thevpc.nuts.Nuts;
import net.thevpc.nsite.context.NSiteContext;
import net.thevpc.nsite.executor.expr.DefaultNSiteExprEvaluator;

public class ExprNodeParserTest {
    public static void main(String[] args) {
        Nuts.openWorkspace(args).share();
        String expr = "a=b*2+6*1+m(-2)+1";
        DefaultNSiteExprEvaluator e = new DefaultNSiteExprEvaluator();
        Object d = e.eval(expr,new NSiteContext());
        System.out.println(d);

    }
}
