package net.thevpc.nsite.executor.expr.fct;

import net.thevpc.nsite.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.expr.NExprCallContext;
import net.thevpc.nuts.expr.NExprContext;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.util.NBlankable;

import java.util.List;

public class EitherFct extends BaseNexprNExprFct {
    public EitherFct() {
        super("either");
    }

    @Override
    public Object eval(NExprCallContext callContext) {
        String name = callContext.name();
        List<NExprNodeValue> args = callContext.args();
        NExprContext context = callContext.context();
        for (NExprNodeValue arg : args) {
            Object str = arg.value().orNull();
            if(!NBlankable.isBlank(str)){
                return str;
            }
        }
        return null;
    }
}
