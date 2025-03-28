package net.thevpc.nsite.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nsite.context.NDocContext;
import net.thevpc.nsite.executor.expr.BaseNexprNExprFct;
import net.thevpc.nsite.processor.html.PageToHtmlUtils;
import net.thevpc.nsite.processor.pages.MPage;
import net.thevpc.nsite.util.StringUtils;

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
