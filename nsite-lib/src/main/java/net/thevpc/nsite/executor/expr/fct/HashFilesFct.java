package net.thevpc.nsite.executor.expr.fct;

import net.thevpc.nsite.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.expr.NExprCallContext;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.expr.NGlob;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.util.NBlankable;

import java.util.ArrayList;
import java.util.List;

public class HashFilesFct extends BaseNexprNExprFct {
    public HashFilesFct() {
        super("hashFiles");
    }

    @Override
    public Object eval(NExprCallContext callContext) {
        String name = callContext.name();
        List<NExprNodeValue> args = callContext.args();
        List<NPath> allFiles = new ArrayList<>();
        NPath d = callContext.context().getVarValue("cwd").map(x -> {
            if (x instanceof String && !NBlankable.isBlank((String) x)) {
                return NPath.of((String) x);
            }
            return null;
        }).instanceOf(NPath.class).orElse(NPath.ofUserDirectory());
        for (NExprNodeValue arg : args) {
            Object str = arg.value().ifErrorThrow().orNull();
            if (!NBlankable.isBlank(str)) {
                if(str instanceof String) {
                    NPath p = NPath.of((String) str);
                    allFiles.addAll(p.toAbsolute(d).walkGlob(NPathOption.SORTED).toList());
                }else if (str instanceof NPath){
                    NPath p = (NPath) str;
                    allFiles.addAll(p.toAbsolute(d).walkGlob(NPathOption.SORTED).toList());
                }
            }
        }
        NDigest digest = NDigest.of().sha256();
        for (NPath f : allFiles) {
            digest.source(f);
        }
        return digest.computeString();
    }
}
