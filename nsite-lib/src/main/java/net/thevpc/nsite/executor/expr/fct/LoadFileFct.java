package net.thevpc.nsite.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nsite.context.NDocContext;
import net.thevpc.nsite.executor.expr.BaseNexprNExprFct;
import net.thevpc.nsite.util.FileProcessorUtils;
import net.thevpc.nsite.util.StringUtils;

import java.util.List;

public class LoadFileFct extends BaseNexprNExprFct {
    public LoadFileFct() {
        super("loadFile");
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
        if (args.size() != 1) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        NDocContext fcontext = fcontext(context);
        String str = (String) args.get(0).getValue().orNull();
        fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(str) + ")");
        return FileProcessorUtils.loadString(
                NPath.of(FileProcessorUtils.toAbsolute(str, fcontext.getWorkingDirRequired()))
        );
    }
}
