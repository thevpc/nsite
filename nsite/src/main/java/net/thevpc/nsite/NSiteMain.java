package net.thevpc.nsite;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineRunner;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nsite.context.NDocContext;

@NApp.Info
public class NSiteMain {
    NDocProjectConfig config = new NDocProjectConfig();

    public static void main(String[] args) {
        NApp.builder(args).run();
    }

    @NApp.Main
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
                    config.addSource(cmdLine.next().get().getImage());
                    return false;
                }
            }

            @Override
            public void run(NCmdLine cmdLine) {
                new NDocContext().run(config);
            }
        });
    }


}
