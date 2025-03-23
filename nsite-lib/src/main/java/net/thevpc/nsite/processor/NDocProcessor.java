package net.thevpc.nsite.processor;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nsite.context.NDocContext;

import java.io.InputStream;
import java.io.OutputStream;

public interface NDocProcessor {

    void processStream(InputStream source, OutputStream target, NDocContext context);
    void processPath(NPath source, String mimeType, NDocContext context);
}
