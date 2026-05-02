package net.thevpc.nsite.executor.expr.fct;

import net.thevpc.nsite.context.NSiteContext;
import net.thevpc.nsite.executor.expr.BaseNexprNExprFct;
import net.thevpc.nsite.processor.html.PageToHtmlUtils;
import net.thevpc.nsite.util.StringUtils;
import net.thevpc.nuts.expr.NExprCallContext;
import net.thevpc.nuts.expr.NExprContext;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;

import java.util.List;

public class ToHtmlFct extends BaseNexprNExprFct {
    public ToHtmlFct() {
        super("toHtml");
    }

    @Override
    public Object eval(NExprCallContext callContext) {
        String name = callContext.name();
        List<NExprNodeValue> args = callContext.args();
        NExprContext context = callContext.context();
        if (args.size() != 2) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        NSiteContext fcontext = fcontext(context);
        String type = (String) args.get(0).value().failFast().orNull();
        String content = (String) args.get(1).value().failFast().orNull();
        NLog.ofScoped(getClass()).debug(NMsg.ofC("[%s] %s(%s)", "eval", name, StringUtils.toLiteralString(type) + ")"));
        return fcontext.md2Html().toHtml(type,content, PageToHtmlUtils.GeneratorContext.of(fcontext));
    }


}
