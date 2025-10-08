package net.thevpc.nsite.executor.expr.fct;

import net.thevpc.nsite.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.util.NBlankable;

import java.util.List;

public class EitherFct extends BaseNexprNExprFct {
    public EitherFct() {
        super("either");
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
        for (NExprNodeValue arg : args) {
            Object str = arg.getValue().orNull();
            if(!NBlankable.isBlank(str)){
                return str;
            }
        }
        return null;
    }
}
