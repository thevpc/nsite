package net.thevpc.nsite.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nsite.context.NSiteContext;
import net.thevpc.nsite.executor.expr.BaseNexprNExprFct;
import net.thevpc.nsite.util.FileProcessorUtils;
import net.thevpc.nsite.util.StringUtils;

import java.util.List;

public class ProcessFileFct extends BaseNexprNExprFct {
    public ProcessFileFct() {
        super("processFile");
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
        if (args.size() != 1) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        NSiteContext fcontext = fcontext(context);

        String str = (String) args.get(0).getValue().orNull();
        String path = FileProcessorUtils.toAbsolute(str, fcontext.getWorkingDirRequired());
        NPath opath = NPath.of(path);
        fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(opath) + ")");
        fcontext.getProcessorManager().processSourceRegularFile(opath, null);
        return "";
    }
}
