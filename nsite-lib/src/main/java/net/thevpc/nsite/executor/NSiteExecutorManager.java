package net.thevpc.nsite.executor;

import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nsite.context.NSiteContext;
import net.thevpc.nsite.executor.expr.DefaultNSiteExprEvaluator;
import net.thevpc.nsite.mimetype.MimeTypeConstants;
import net.thevpc.nsite.processor.impl.DefaultNSiteStreamExecutor;
import net.thevpc.nsite.util.FileProcessorUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class NSiteExecutorManager {
    private static final NSiteExecutor NEXPR_EXECUTOR = new StreamToExecutor(new DefaultNSiteStreamExecutor(new DefaultNSiteExprEvaluator()));
    public static final NSiteExecutor DEFAULT_EXECUTOR = NEXPR_EXECUTOR;
    private static final NSiteExecutor IGNORE_EXECUTOR = new IgnoreNSiteExecutor();

    private static final Map<String, NSiteExecutor> globalExecProcessorsByMimeType = new HashMap<>();

    static {
        globalExecProcessorsByMimeType.put(MimeTypeConstants.NEXPR, NEXPR_EXECUTOR);
        globalExecProcessorsByMimeType.put(MimeTypeConstants.IGNORE, IGNORE_EXECUTOR);
    }

    private NSiteExecutorFactory executorFactory;
    private final Map<String, NSiteExecutor> execProcessorsByMimeType = new HashMap<>();
    private NSiteContext context;

    public NSiteExecutorManager(NSiteContext context) {
        this.context = context;
    }

    public NOptional<NSiteExecutor> getExecutor(NPath path, String mimeTypesString) {
        String[] mimeTypesArray = mimeTypesString == null ? FileProcessorUtils.splitMimeTypes(context.getMimeTypeResolver().resolveMimetype(path.toString()))
                : FileProcessorUtils.splitMimeTypes(mimeTypesString);
        for (String mimeType : mimeTypesArray) {
            NSiteExecutor proc = null;
            try {
                proc = getExecutor(mimeType);
                if (proc != null) {
                    return NOptional.of(proc);
                }
            } catch (Exception ex) {
                NLog.ofScoped(getClass()).error(NMsg.ofC("[%s] error resolving executor for mimeType %s and file : %s. %s", "file", mimeType,path.toString(), ex).asError(ex));
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("executor for %s %s", mimeTypesString, path.toString()));
    }

    public NSiteExecutor getExecutor(String mimetypes) {
        String[] mimeTypes = FileProcessorUtils.splitMimeTypes(mimetypes);
        for (String mimeType : mimeTypes) {
            if (mimeType == null || mimeType.isEmpty() || mimeType.equals("*")) {
                mimeType = MimeTypeConstants.ANY_TYPE;
            }
            String[] mts = Stream.of(mimeType.split(";")).map(String::trim).filter(x -> x.length() > 0).toArray(String[]::new);
            for (String mt : mts) {
                NSiteExecutor m = getExecutorExact(mt);
                if (m != null) {
                    return m;
                }
            }
            for (String mt : mts) {
                int slash = mt.indexOf('/');
                if (slash > 0) {
                    String a = mt.substring(0, slash);
                    String b = mt.substring(slash + 1);
                    if (!b.equals("*")) {
                        NSiteExecutor m = getExecutorExact(a + "/*");
                        if (m != null) {
                            return m;
                        }
                    }
                }
            }
            NSiteExecutor z = getExecutorExact(MimeTypeConstants.ANY_TYPE);
            if (z != null) {
                return z;
            }
        }
        return DEFAULT_EXECUTOR;
    }

    public NSiteExecutor getExecutorExact(String mimetype) {
        NSiteExecutorFactory executorFactory = getExecutorFactory();
        if (executorFactory != null) {
            NSiteExecutor p = executorFactory.getExecutor(mimetype);
            if (p != null) {
                return p;
            }
        }
        NSiteExecutor p = execProcessorsByMimeType.get(mimetype);
        if (p != null) {
            return p;
        }
        if (context.getParent() != null) {
            return context.getParent().getExecutorManager().getExecutorExact(mimetype);
        }
        p = globalExecProcessorsByMimeType.get(mimetype);
        if (p != null) {
            return p;
        }
        return null;
    }

    public NSiteExecutor getDefaultExecutor(String mimetype) {
        NSiteExecutor processor = execProcessorsByMimeType.get(mimetype);
        if (processor != null) {
            return processor;
        }
        if (context.getParent() != null) {
            return context.getParent().getExecutorManager().getDefaultExecutor(mimetype);
        }
        return null;
    }

    public NSiteExecutorManager setDefaultExecutor(String mimetype, NSiteExprEvaluator executor) {
        return setDefaultExecutor(mimetype, executor == null ? null : new StreamToExecutor(new DefaultNSiteStreamExecutor(executor)));
    }

    public NSiteExecutorManager setDefaultExecutor(String mimetype, NSiteExecutor executor) {
        if (executor == null) {
            execProcessorsByMimeType.remove(mimetype);
        } else {
            execProcessorsByMimeType.put(mimetype, executor);
        }
        return this;
    }

    public NSiteExecutorFactory getExecutorFactory() {
        if (executorFactory != null) {
            return executorFactory;
        }
        if (context.getParent() != null) {
            return context.getParent().getExecutorManager().getExecutorFactory();
        }
        return null;
    }

    public NSiteExecutorManager setExecutorFactory(NSiteExecutorFactory processorFactory) {
        this.executorFactory = processorFactory;
        return this;
    }

    public String executeRegularFile(NPath path, String mimeTypesString) {
        NPath absolutePath = context.toAbsolutePath(path);
        NPath parentPath = absolutePath.getParent();
        if (!absolutePath.isRegularFile()) {
            throw new NIllegalArgumentException(NMsg.ofC("no a file : %s", path));
        }
        NSiteExecutor proc = getExecutor(path, mimeTypesString).get();

        try {
            String s1 = path.toString();
            String s2 = absolutePath.toString();
            if (s1.equals(s2)) {
                NLog.ofScoped(getClass()).debug(NMsg.ofC("[%s] [%s] [%s] execute path : %s = %s", "file", proc, mimeTypesString, s1,s2));
            } else {
                NLog.ofScoped(getClass()).debug(NMsg.ofC("[%s] [%s] [%s] execute path : %s", "file", proc, mimeTypesString, s1));
            }
            try (InputStream in = absolutePath.getInputStream()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                proc.processStream(in, out,
                        context.newChild()
                                .setUserParentProperties(true)
                                .setWorkingDir(parentPath.toString())
                                .setSourcePath(absolutePath.toString())
                                .setVar("source", absolutePath.toString())
                                .setVar("dir", parentPath.toString())
                                .setVar("cwd", System.getProperty("user.dir"))
                                .setVar("projectRoot", context.getProjectRoot())
                );
                return out.toString();
            }
        } catch (IOException ex) {
            throw new NIOException(NMsg.ofC("error executing file : %s", path), ex);
        }
    }

}
