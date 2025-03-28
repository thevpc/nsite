package net.thevpc.nsite.executor.expr;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprFct;
import net.thevpc.nuts.expr.NExprVarDeclaration;
import net.thevpc.nsite.context.NDocContext;

public abstract class BaseNexprNExprFct implements NExprFct {
    private String name;

    public BaseNexprNExprFct(String name) {
        this.name = name;
    }

    protected static NDocContext fcontext(NExprDeclarations context) {
        NExprVarDeclaration vd = context.getVar(DefaultNDocExprEvaluator.NSITE_CONTEXT_VAR_NAME).get();
        return (NDocContext) vd.get(context);
    }

    public String getName() {
        return name;
    }
}
