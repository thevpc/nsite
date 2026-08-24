package net.thevpc.nsite;

import net.thevpc.nuts.app.NAppComplete;
import net.thevpc.nuts.app.NApplication;
import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.app.NAppRun;
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
        processCmdline();
        new NSiteContext().run(config);
    }

    @NAppComplete
    public void complete() {
        processCmdline().printCompleteResult();
    }

    private NCmdLine processCmdline() {
        NCmdLine cmdLine = NApplication.of().cmdLine();
        cmdLine.matcher().with(c -> {
            NArg arg = c.peek().get();
            if (arg.isOption()) {
                return config.configureFirst(c);
            } else {
                config.addSource(c.next().get().image());
                return false;
            }
        }).requireAll();
        return cmdLine;
    }


}
