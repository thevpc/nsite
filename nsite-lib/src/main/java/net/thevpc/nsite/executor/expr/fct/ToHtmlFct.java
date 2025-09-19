package net.thevpc.nsite.executor.expr.fct;

import net.thevpc.nsite.context.NSiteContext;
import net.thevpc.nsite.executor.expr.BaseNexprNExprFct;
import net.thevpc.nsite.processor.html.PageToHtmlUtils;
import net.thevpc.nsite.processor.pages.MPage;
import net.thevpc.nsite.util.HtmlBuffer;
import net.thevpc.nsite.util.StringUtils;
import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.lib.md.MdElement;
import net.thevpc.nuts.lib.md.MdFactory;
import net.thevpc.nuts.lib.md.MdParser;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.io.StringReader;
import java.util.List;

public class ToHtmlFct extends BaseNexprNExprFct {
    public ToHtmlFct() {
        super("toHtml");
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
        if (args.size() != 2) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        NSiteContext fcontext = fcontext(context);
        String type = (String) args.get(0).getValue().orNull();
        String content = (String) args.get(1).getValue().orNull();
        NLog.ofScoped(getClass()).debug(NMsg.ofC("[%] %s(%s)", "eval", name, StringUtils.toLiteralString(type) + ")"));
        return fcontext.md2Html().toHtml(type,content, PageToHtmlUtils.GeneratorContext.of(fcontext));
    }


}
