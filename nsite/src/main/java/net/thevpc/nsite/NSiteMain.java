package net.thevpc.nsite;

import net.thevpc.nuts.app.NApplication;
import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.app.NAppRun;
import net.thevpc.nuts.cmdline.NCmdLineRunner;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nsite.context.NSiteContext;

@NApp
public class NSiteMain {
    NSiteProjectConfig config = new NSiteProjectConfig();

    public static void main(String[] args) {
        NApplication.builder(args).run();
    }

    @NAppRun
    public void run() {
        NApplication.of().runCmdLine(new NCmdLineRunner() {
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
