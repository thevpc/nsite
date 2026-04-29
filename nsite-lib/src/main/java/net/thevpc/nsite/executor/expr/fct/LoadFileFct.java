package net.thevpc.nsite.executor.expr.fct;

import net.thevpc.nuts.expr.NExprCallContext;
import net.thevpc.nuts.expr.NExprContext;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nsite.context.NSiteContext;
import net.thevpc.nsite.executor.expr.BaseNexprNExprFct;
import net.thevpc.nsite.util.FileProcessorUtils;
import net.thevpc.nsite.util.StringUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;

import java.util.List;

public class LoadFileFct extends BaseNexprNExprFct {
    public LoadFileFct() {
        super("loadFile");
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
        NLog.ofScoped(getClass()).debug(NMsg.ofC("[%s] %s(%s)","eval",name,StringUtils.toLiteralString(str)));
        return FileProcessorUtils.loadString(
                NPath.of(FileProcessorUtils.toAbsolute(str, fcontext.getWorkingDirRequired()))
        );
    }
}
