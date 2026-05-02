package net.thevpc.nsite.executor.expr.fct;

import net.thevpc.nuts.expr.NExprCallContext;
import net.thevpc.nuts.expr.NExprContext;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nsite.context.NSiteContext;
import net.thevpc.nsite.executor.expr.BaseNexprNExprFct;
import net.thevpc.nsite.util.StringUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;

import java.util.ArrayList;
import java.util.List;

public class PrintFct extends BaseNexprNExprFct {
    public PrintFct() {
        super("print");
    }

    @Override
    public Object eval(NExprCallContext callContext) {
        String name = callContext.name();
        List<NExprNodeValue> args = callContext.args();
        NExprContext context = callContext.context();
        NSiteContext fcontext = fcontext(context);

        List<String> all = new ArrayList<>();
        for (NExprNodeValue arg : args) {
            all.add(String.valueOf(arg.value().failFast().orNull()));
        }
        StringBuilder sb = new StringBuilder();
        if (!all.isEmpty()) {
            if (all.size() == 1) {
                sb.append(all.get(0));
            } else {
                sb.append(String.join(", ", all));
            }
        }
        NLog.ofScoped(getClass()).debug(NMsg.ofC("[%s] %s(%s)","eval",name,StringUtils.toLiteralString(sb.toString()) + ")"));
        return sb.toString();
    }
}
