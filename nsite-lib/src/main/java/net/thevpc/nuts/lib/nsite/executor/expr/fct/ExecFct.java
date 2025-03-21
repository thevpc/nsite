package net.thevpc.nuts.lib.nsite.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.nsite.context.NDocContext;
import net.thevpc.nuts.lib.nsite.executor.expr.BaseNexprNExprFct;

import java.util.List;

public class ExecFct extends BaseNexprNExprFct {
    public ExecFct() {
        super("exec");
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
        if (args.size() != 1) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        NDocContext fcontext = fcontext(context);
        String pathString = (String) args.get(0).getValue().orNull();
        NPath path = NPath.of(pathString);
        return fcontext.getExecutorManager().executeRegularFile(path, null);
    }
}
