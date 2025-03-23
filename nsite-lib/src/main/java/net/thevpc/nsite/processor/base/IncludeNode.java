package net.thevpc.nsite.processor.base;

import net.thevpc.nuts.io.NPath;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

class IncludeNode extends TagNode {
    private final String exprLang;
    private String expr;

    public IncludeNode(String exprLang, String expr) {
        this.exprLang = exprLang;
        this.expr = expr;
    }

    public void run(ProcessStreamContext ctx) {
        Object eval = ctx.context.eval(expr, exprLang);
        try (InputStream in = NPath.of((String) eval).getInputStream()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ctx.getStreamProcessor().processStream(in, bos, ctx.context);
            String s = new String(bos.toByteArray());
            ctx.out.write(s);

//            WriterOutputStream wout = new WriterOutputStream(ctx.out);
//            ctx.getStreamProcessor().processStream(in, wout,ctx.context);
//            wout.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String toString() {
        return "Include(" + expr + ")";
    }
}
