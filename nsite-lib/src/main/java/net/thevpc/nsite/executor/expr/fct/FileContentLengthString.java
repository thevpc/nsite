package net.thevpc.nsite.executor.expr.fct;

import net.thevpc.nuts.expr.NExprCallContext;
import net.thevpc.nuts.expr.NExprContext;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nsite.context.NSiteContext;
import net.thevpc.nsite.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.util.NMemorySize;

import java.util.List;

public class FileContentLengthString extends BaseNexprNExprFct {
    public FileContentLengthString() {
        super("fileContentLengthString");
    }

    @Override
    public Object eval(NExprCallContext callContext) {
        String name = callContext.name();
        List<NExprNodeValue> args = callContext.args();
        NExprContext context = callContext.context();
        if (args.size() != 1) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        NSiteContext fcontext = fcontext(context);
        String str = (String) args.get(0).value().orNull();
        long contentLength = NPath.of(str).getContentLength();
        if(contentLength<0){
            return "NOT_FOUND";
        }
        NMemorySize m = NMemorySize.ofBits(contentLength)
                .reduceToLargestUnit();
        //m.getLargestUnit()
        return m.toString();
    }
}
