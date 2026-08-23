package net.thevpc.nsite.processor.base;

import net.thevpc.nuts.util.NLiteral;

import java.io.IOException;
import java.io.UncheckedIOException;

class PlainTagNode extends TagNode {
    private String value;

    public PlainTagNode(String value) {
        this.value = value;
    }

    public void run(ProcessStreamContext ctx) {
        try {
            ctx.out.write(value);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String toString() {
        String a = value;
        if (a.length() > 20) {
            a = a.substring(0, 20) + "...";
        }
        return "Plain(" + NLiteral.of(a).toStringLiteral() + ')';
    }
}
