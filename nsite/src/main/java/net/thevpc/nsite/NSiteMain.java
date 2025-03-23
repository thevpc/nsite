package net.thevpc.nsite;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineRunner;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nsite.context.NDocContext;
import net.thevpc.nuts.util.NLiteral;

public class NSiteMain implements NApplication {
    NDocProjectConfig config = new NDocProjectConfig();

    public static void main(String[] args) {
        NApplication.main(NSiteMain.class, args);
    }

    @Override
    public void run() {
        NApp.of().processCmdLine(new NCmdLineRunner() {

            @Override
            public boolean nextOption(NArg option, NCmdLine cmdLine, NCmdLineContext context) {
                if(config.configureFirst(cmdLine)){
                    return true;
                }
                return false;
            }

            @Override
            public boolean nextNonOption(NArg nonOption, NCmdLine cmdLine, NCmdLineContext context) {
                config.addSource(cmdLine.next().flatMap(NLiteral::asString).get());
                return false;
            }


            @Override
            public void run(NCmdLine cmdLine, NCmdLineContext context) {
                new NDocContext().run(config);
            }
        });
    }


}
