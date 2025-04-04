package net.thevpc.nsite.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nsite.context.NDocContext;
import net.thevpc.nsite.executor.expr.BaseNexprNExprFct;
import net.thevpc.nsite.util.StringUtils;
import net.thevpc.nuts.util.NLiteral;

import java.util.List;

public class StringFct extends BaseNexprNExprFct {
    public StringFct() {
        super("string");
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
        if (args.size() != 1) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        NDocContext fcontext = fcontext(context);

        String str = (String) args.get(0).getValue().orNull();
        fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(str) + ")");
        return NLiteral.of(str).toStringLiteral();
    }
}
