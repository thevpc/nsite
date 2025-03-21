package net.thevpc.nuts.lib.nsite.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.lib.nsite.context.NDocContext;
import net.thevpc.nuts.lib.nsite.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.lib.nsite.processor.html.PageToHtmlUtils;
import net.thevpc.nuts.lib.nsite.processor.pages.MPage;
import net.thevpc.nuts.lib.nsite.util.StringUtils;

import java.util.List;

public class PageContentToHtmlFct extends BaseNexprNExprFct {
    public PageContentToHtmlFct() {
        super("pageContentToHtml");
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
        if (args.size() != 1 && args.size() != 2) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        NDocContext fcontext = fcontext(context);
        MPage page = (MPage) args.get(0).getValue().orNull();
        fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(page) + ")");
        if (page == null) {
            return "";
        }
        return fcontext.md2Html().pageContent2html(page, PageToHtmlUtils.GeneratorContext.of(fcontext));
    }


}
