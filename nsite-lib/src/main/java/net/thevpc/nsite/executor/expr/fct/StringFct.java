package net.thevpc.nsite.executor.expr.fct;

import net.thevpc.nuts.expr.NExprCallContext;
import net.thevpc.nuts.expr.NExprContext;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nsite.executor.expr.BaseNexprNExprFct;
import net.thevpc.nsite.util.StringUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.text.NMsg;

import java.util.List;

public class StringFct extends BaseNexprNExprFct {
    public StringFct() {
        super("string");
    }

    @Override
    public Object eval(NExprCallContext callContext) {
        String name = callContext.name();
        List<NExprNodeValue> args = callContext.args();
        NExprContext context = callContext.context();
        if (args.size() != 1) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        String str = (String) args.get(0).value().orNull();
        NLog.ofScoped(getClass()).debug(NMsg.ofC("[%s] %s(%s)","eval",name,StringUtils.toLiteralString(str)));
        return NLiteral.of(str).toStringLiteral();
    }
}
