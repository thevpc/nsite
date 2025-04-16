package net.thevpc.nsite.executor.nsh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nsite.context.NDocContext;
import net.thevpc.nsh.cmd.NshBuiltinDefault;
import net.thevpc.nsh.eval.NshExecutionContext;
import net.thevpc.nsite.util.StringUtils;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.List;

public class ProcessCmd extends NshBuiltinDefault {

    private final NDocContext documentContext;

    public ProcessCmd(NDocContext documentContext) {
        super("process", 10, Options.class);
        this.documentContext = documentContext;
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        Options o = context.getOptions();
        NSession session = context.getSession();
        if (cmdLine.isNonOption(0)) {
            o.args.add(cmdLine.next().get().getImage());
            while (cmdLine.hasNext()) {
                o.args.add(cmdLine.next().get().getImage());
            }
            return true;
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NshExecutionContext context) {
        Options o = context.getOptions();
        if (o.args.size() == 0) {
            throw new NExecutionException(NMsg.ofC("%s : invalid arguments count", getName()), NExecutionException.ERROR_1);
        }
        for (String pathString : o.args) {
            documentContext.getLog().debug("eval", getName() + "(" + StringUtils.toLiteralString(pathString) + ")");
            documentContext.getExecutorManager().executeRegularFile(NPath.of(pathString), null);
        }
    }

    private static class Options {
        List<String> args = new ArrayList<>();
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NshExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
