package net.thevpc.nsite.executor.expr;

import net.thevpc.nsite.executor.NSiteExprEvaluator;
import net.thevpc.nsite.executor.expr.fct.*;
import net.thevpc.nuts.expr.*;
import net.thevpc.nsite.context.NSiteContext;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

public class DefaultNSiteExprEvaluator implements NSiteExprEvaluator {
    public static final String NSITE_CONTEXT_VAR_NAME = "nsite";
    private NExprMutableContext rootDecls;
    private NExprs nExprs;

    public DefaultNSiteExprEvaluator() {

        nExprs = NExprs.of();
        NSiteNExprVar v = new NSiteNExprVar();
        rootDecls = NExprContextBuilder.of()
                .setAutoDeclareVariables(true)
                .buildMutable();
        declareFunction(new ExecFct());
        declareFunction(new CurrentYearFct());
        declareFunction(new PrintlnFct());
        declareFunction(new PrintFct());
        declareFunction(new StringFct());
        declareFunction(new ToHtmlFct());
        declareFunction(new ProcessFileFct());
        declareFunction(new LoadFileFct());
        declareFunction(new IncludeFct());
        declareFunction(new LoadPagesFct());
        declareFunction(new PageToHtmlFct());
        declareFunction(new PageContentToHtmlFct());
        declareFunction(new FormatDateFct());
        declareFunction(new FileContentLengthString());
        declareFunction(new EitherFct());
    }

    @Override
    public Object eval(String content, NSiteContext fcontext) {
        content = content.trim();
        NExprMutableContext decl = rootDecls.childContext().buildMutable();
        decl.declareConstant(NSITE_CONTEXT_VAR_NAME, fcontext);
        decl.declareConstant("cwd", System.getProperty("user.dir"));
        decl.declareConstant("projectRoot", fcontext.getProjectRoot());
        decl.declareConstant("dir", fcontext.getWorkingDir().orNull());
        NExprContext decl2 = decl.childContext()
                .declareVars((String varName, NExprContext context)->{
                    NOptional<Object> var = fcontext.getVar(varName);
                    if (var.isPresent()) {
                        return NOptional.of(new NExprVar() {
                            @Override
                            public Object get(String name, NExprContext context) {
                                return var.get();
                            }

                            @Override
                            public Object set(String name, Object value, NExprContext context) {
                                return fcontext.setVar(name, value);
                            }
                        });
                    }
                    return NOptional.ofNamedEmpty(varName);
                })
                .build();
        NExprNode nExprNode = decl2.parse(content).get();
        NOptional<Object> eval = nExprNode.eval(decl2);
//        if (!eval.isPresent()) {
//            eval = nExprNode.eval(decl2);
//        }
        if(!eval.isPresent()) {
            NLog.ofScoped(DefaultNSiteExprEvaluator.class).log(NMsg.ofC("unable to evaluate %s : %s", nExprNode,eval.getMessage().get()).asError());
        }
        return eval.get();
    }

    @Override
    public String toString() {
        return "NExpr";
    }

    protected void declareFunction(BaseNexprNExprFct d) {
        rootDecls.declareFunction(d.getName(), d);
    }

    private static NSiteContext fcontext(NExprContext context) {
        NExprVarDeclaration vd = context.getVar(NSITE_CONTEXT_VAR_NAME).get();
        return (NSiteContext) vd.get(context);
    }

    private static class NSiteNExprVar implements NExprVar {
        public NSiteNExprVar() {
        }

        @Override
        public Object get(String name, NExprContext context) {
            return fcontext(context).getVar(name).orNull();
        }

        @Override
        public Object set(String name, Object value, NExprContext context) {
            return fcontext(context).setVar(name, value);
        }
    }
}
