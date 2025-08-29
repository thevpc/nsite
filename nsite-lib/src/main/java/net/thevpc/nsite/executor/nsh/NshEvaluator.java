package net.thevpc.nsite.executor.nsh;

import net.thevpc.nuts.NOut;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nsite.executor.NSiteExprEvaluator;
import net.thevpc.nsite.context.NSiteContext;
import net.thevpc.nsh.eval.NshContext;
import net.thevpc.nsh.parser.nodes.NshVar;
import net.thevpc.nsh.parser.nodes.NshVarListener;
import net.thevpc.nsh.parser.nodes.NshVariables;
import net.thevpc.nsh.Nsh;
import net.thevpc.nsh.NshConfig;
import net.thevpc.nsite.util.StringUtils;

public class NshEvaluator implements NSiteExprEvaluator {
    private final Nsh shell;
    private final NSiteContext docContext;

    public NshEvaluator(NSiteContext docContext) {
        this.docContext = docContext;
        shell = new Nsh(new NshConfig()
                .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
                .setArgs()
        );
        NshContext rootContext = shell.getRootContext();
        rootContext.setSession(rootContext.getSession().copy());
        rootContext.vars().addVarListener(
                new NshVarListener() {
                    @Override
                    public void varAdded(NshVar nshVar, NshVariables vars, NshContext context) {
                        setVar(nshVar.getName(), nshVar.getValue());
                    }

                    @Override
                    public void varValueUpdated(NshVar nshVar, String oldValue, NshVariables vars, NshContext context) {
                        setVar(nshVar.getName(), nshVar.getValue());
                    }

                    @Override
                    public void varRemoved(NshVar nshVar, NshVariables vars, NshContext context) {
                        setVar(nshVar.getName(), null);
                    }
                }
        );
        rootContext
                .builtins()
                .set(new ProcessCmd(docContext));
    }

    public void setVar(String varName, String newValue) {
        docContext.getLog().debug("eval", varName + "=" + StringUtils.toLiteralString(newValue));
        docContext.setVar(varName, newValue);
    }

    @Override
    public Object eval(String content, NSiteContext context) {
        NshContext ctx = shell.createInlineContext(shell.getRootContext(), context.getSourcePath().orElse("nsh"), new String[0]);
        NSession session = NSession.of().copy().setTerminal(NTerminal.ofMem());
        return session.callWith(()->{
            ctx.setSession(session);
            shell.executeScript(content, ctx);
            return NOut.out().toString();
        });
    }

    @Override
    public String toString() {
        return "nsh";
    }
}
