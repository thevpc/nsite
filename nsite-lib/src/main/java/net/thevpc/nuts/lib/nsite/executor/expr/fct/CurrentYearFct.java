package net.thevpc.nuts.lib.nsite.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.lib.nsite.executor.expr.BaseNexprNExprFct;

import java.time.LocalDate;
import java.util.List;

public class CurrentYearFct extends BaseNexprNExprFct {
    public CurrentYearFct() {
        super("currentYear");
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
        if (args.size() != 0) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        return LocalDate.now().getYear();
    }
}
