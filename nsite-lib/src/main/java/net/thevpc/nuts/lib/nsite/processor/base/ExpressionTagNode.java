package net.thevpc.nuts.lib.nsite.processor.base;

import java.io.IOException;
import java.io.UncheckedIOException;

class ExpressionTagNode extends TagNode {
    private final String exprLang;
    private String expr;

    public ExpressionTagNode(String exprLang, String expr) {
        this.exprLang = exprLang;
        this.expr = expr;
    }

    public void run(ProcessStreamContext ctx)  {
        String s = ctx.context.executeString(expr, exprLang);
        try {
            ctx.out.write(s);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        return "Expression(" +expr+ ')';
    }
}
