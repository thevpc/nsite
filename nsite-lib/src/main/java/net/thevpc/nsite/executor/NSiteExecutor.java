package net.thevpc.nsite.executor;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nsite.context.NSiteContext;

import java.io.InputStream;
import java.io.OutputStream;

public interface NSiteExecutor {

    Object eval(InputStream source, NSiteContext context);

    void processStream(InputStream source, OutputStream target, NSiteContext context);

    void processPath(NPath source, String mimeType, NSiteContext context);
}
