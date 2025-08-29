package net.thevpc.nsite.context;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nsite.executor.nsh.NshEvaluator;

public class ProjectNSiteContext extends NSiteContext {
    public ProjectNSiteContext() {
        super();
        this.getExecutorManager().setDefaultExecutor("text/nsite-nsh-project", new NshEvaluator(this));
        setProjectFileName("project.nsh");
    }

    public void executeProjectFile(NPath path, String mimeTypesString) {
        getExecutorManager().executeRegularFile(path, "text/nsite-nsh-project");
    }
}
