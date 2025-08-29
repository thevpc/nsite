package net.thevpc.nsite.executor;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nsite.context.NSiteContext;

import java.io.InputStream;
import java.io.OutputStream;

class IgnoreNSiteExecutor implements NSiteExecutor {
    @Override
    public Object eval(InputStream source, NSiteContext context) {
        return null;
    }

    @Override
    public void processStream(InputStream source, OutputStream target, NSiteContext context) {

    }

    @Override
    public void processPath(NPath source, String mimeType, NSiteContext context) {

    }
}
