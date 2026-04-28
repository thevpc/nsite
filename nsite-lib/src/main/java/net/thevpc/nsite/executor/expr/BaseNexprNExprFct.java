package net.thevpc.nsite.executor.expr;

import net.thevpc.nuts.expr.NExprContext;
import net.thevpc.nuts.expr.NExprFunctionHandler;
import net.thevpc.nuts.expr.NExprVar;
import net.thevpc.nsite.context.NSiteContext;

public abstract class BaseNexprNExprFct implements NExprFunctionHandler {
    private String name;

    public BaseNexprNExprFct(String name) {
        this.name = name;
    }

    protected static NSiteContext fcontext(NExprContext context) {
        NExprVar vd = context.getVar(DefaultNSiteExprEvaluator.NSITE_CONTEXT_VAR_NAME).get();
        return (NSiteContext) vd.get(context);
    }

    public String getName() {
        return name;
    }
}
