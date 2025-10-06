package net.thevpc.nsite;

import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.app.NAppDefinition;
import net.thevpc.nuts.app.NAppRunner;
import net.thevpc.nuts.cmdline.NCmdLineRunner;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nsite.context.NSiteContext;

@NAppDefinition
public class NSiteMain {
    NSiteProjectConfig config = new NSiteProjectConfig();

    public static void main(String[] args) {
        NApp.builder(args).run();
    }

    @NAppRunner
    public void run() {
        NApp.of().runCmdLine(new NCmdLineRunner() {
            @Override
            public boolean next(NArg arg, NCmdLine cmdLine) {
                if(arg.isOption()){
                    if(config.configureFirst(cmdLine)){
                        return true;
                    }
                    return false;
                }else{
                    config.addSource(cmdLine.next().get().image());
                    return false;
                }
            }

            @Override
            public void run(NCmdLine cmdLine) {
                new NSiteContext().run(config);
            }
        });
    }


}
