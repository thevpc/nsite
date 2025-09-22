package net.thevpc.nsite.processor;

import net.thevpc.nsite.processor.impl.CopyStreamProcessor;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nsite.context.DefaultNSitePathTranslator;
import net.thevpc.nsite.context.NSiteContext;
import net.thevpc.nsite.mimetype.MimeTypeConstants;
import net.thevpc.nsite.processor.base.TagStreamProcessor;
import net.thevpc.nsite.util.FileProcessorUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStream;
import net.thevpc.nuts.util.NStringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class NSiteProcessorManager {
    private static final Map<String, NSiteProcessor> globalProcessorsByMimeType = new HashMap<>();
    public static final StreamToProcessor DEFAULT_PROCESSOR = new StreamToProcessor(new CopyStreamProcessor());

    static {
        registerGlobalProcessorByMimeType(TagStreamProcessor.DOLLAR, MimeTypeConstants.PLACEHOLDER_DOLLAR);
        registerGlobalProcessorByMimeType(TagStreamProcessor.BARACKET2, MimeTypeConstants.PLACEHOLDER_BRACKET2);
        registerGlobalProcessorByMimeType(TagStreamProcessor.DOLLAR_BARACKET2, MimeTypeConstants.PLACEHOLDER_DOLLAR_BRACKET2);
        registerGlobalProcessorByMimeType(TagStreamProcessor.LT_PERCENT, MimeTypeConstants.PLACEHOLDER_LT_PERCENT);

        registerGlobalProcessorByMimeType(TagStreamProcessor.BARACKET2, "text/html", "text/markdown", "text/x-shellscript", "application/x-shellscript",
                MimeTypeConstants.ANY_TEXT
        );
        for (String mimeType : NConstants.Ntf.MIME_TYPES) {
            registerGlobalProcessorByMimeType(TagStreamProcessor.BARACKET2, mimeType);
        }
        registerGlobalProcessorByMimeType(TagStreamProcessor.DOLLAR_BARACKET2,
                "application/json",
                "application/javascript",
                "application/java"
        );
        globalProcessorsByMimeType.put(MimeTypeConstants.ANY_TYPE, DEFAULT_PROCESSOR);
    }

    private NSiteContext context;
    private final Map<String, NSiteProcessor> processorsByMimeType = new HashMap<>();
    private NSiteProcessorFactory processorFactory;

    public static void registerGlobalProcessorByMimeType(NSiteProcessor p, String... mimetypes) {
        for (String mimetype : mimetypes) {
            globalProcessorsByMimeType.put(mimetype, p);
        }
    }

    public static void registerGlobalProcessorByMimeType(NSiteStreamProcessor p, String... mimetypes) {
        for (String mimetype : mimetypes) {
            globalProcessorsByMimeType.put(mimetype, new StreamToProcessor(p));
        }
    }

    public NSiteProcessorManager(NSiteContext context) {
        this.context = context;
    }

    public NSiteProcessor getProcessorExact(String mimetype) {
        if (processorFactory != null) {
            NSiteProcessor p = processorFactory.getProcessor(mimetype);
            if (p != null) {
                return p;
            }
        }
        NSiteProcessor p = processorsByMimeType.get(mimetype);
        if (p != null) {
            return p;
        }
        if (context.getParent() != null) {
            return context.getParent().getProcessorManager().getProcessorExact(mimetype);
        }
        p = globalProcessorsByMimeType.get(mimetype);
        if (p != null) {
            return p;
        }
        //never return null!
        return null;//DEFAULT_PROCESSOR;
    }

    public NSiteProcessor getProcessor(String mimetype) {
        if (mimetype == null || mimetype.isEmpty() || mimetype.equals("*")) {
            mimetype = MimeTypeConstants.ANY_TYPE;
        }
        for (String mt : mimetype.split(";")) {
            mt = mt.trim();
            if (mt.length() > 0) {
                NSiteProcessor m = getProcessorExact(mt);
                if (m != null) {
                    return m;
                }
            }
        }
        for (String mt : mimetype.split(";")) {
            mt = mt.trim();
            if (mt.length() > 0) {
                int slash = mimetype.indexOf('/');
                if (slash > 0) {
                    String a = mimetype.substring(0, slash);
                    String b = mimetype.substring(slash + 1);
                    if (!b.equals("*")) {
                        NSiteProcessor m = getProcessorExact(a + "/*");
                        if (m != null) {
                            return m;
                        }
                    }
                }
            }
        }
        return getProcessorExact(MimeTypeConstants.ANY_TYPE);
    }


    public NSiteProcessor getDefaultProcessor(String mimetype) {
        NSiteProcessor processor = processorsByMimeType.get(mimetype);
        if (processor != null) {
            return processor;
        }
        if (context.getParent() != null) {
            return context.getParent().getProcessorManager().getDefaultProcessor(mimetype);
        }
        return null;
    }


    public NSiteProcessorManager setDefaultProcessor(String mimetype, NSiteProcessor processor) {
        if (processor == null) {
            processorsByMimeType.remove(mimetype);
        } else {
            processorsByMimeType.put(mimetype, processor);
        }
        return this;
    }


    public NSiteProcessorFactory getProcessorFactory() {
        if (processorFactory != null) {
            return processorFactory;
        }
        if (context.getParent() != null) {
            return context.getParent().getProcessorManager().getProcessorFactory();
        }
        return null;
    }

    public NSiteProcessorManager setProcessorFactory(NSiteProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
        return this;
    }

    public void processResourceTree(NPath path, Predicate<NPath> filter) {
        if (!context.getRootDir().isPresent()) {
            context.setRootDir(path.toString());
        }
        if (!path.exists()) {
            log().warn(NMsg.ofC("[%s] source file not found %s", "file", path));
            return;
        }
        NPath path0 = context.toAbsolutePath(path);
        if (path0.isRegularFile()) {
            if (filter == null || filter.test(path0)) {
                getProcessorExact(MimeTypeConstants.ANY_TYPE).processPath(path, null, context);
            }
        } else if (path.isDirectory()) {
            try (NStream<NPath> stream = path0.walk().filter(x -> x.isRegularFile())) {
                stream.forEach(x -> {
                    if (filter == null || filter.test(x)) {
                        try {
                            String p = context.getPathTranslator().translatePath(x.toString());
                            if (p != null) {
                                NCp.of().from(x).to(NPath.of(p)).setMkdirs(true).run();
                                //getProcessorExact(MimeTypeConstants.ANY_TYPE).processPath(x, null, context);
                            }
                        } catch (Exception ex) {
                            throw new NIllegalArgumentException(NMsg.ofC("error processing %s : %s", x, ex), ex);
                        }
                    }
                });
            }
        } else {
            throw new NIOException(NMsg.ofC("unsupported path %s", path));
        }
    }

    public void processSourceRegularFile(NPath path, String mimeType) {
        NPath absolutePath = context.toAbsolutePath(path);
        NPath parentPath = absolutePath.getParent();
        if (!absolutePath.isRegularFile()) {
            throw new NIllegalArgumentException(NMsg.ofC("unsupported file : %s", path.toString()));
        }
        String[] mimeTypes = mimeType == null ? FileProcessorUtils.splitMimeTypes(context.getMimeTypeResolver().resolveMimetype(path.toString()))
                : FileProcessorUtils.splitMimeTypes(mimeType);
        String contextName1 = NStringUtils.firstNonBlank(context.getContextName(), "file");
        for (String mimeType0 : mimeTypes) {
            NSiteProcessor proc = null;
            try {
                proc = getProcessor(mimeType0);
            } catch (Exception ex) {
                log().error(NMsg.ofC("[%s] error resolving processor for mimeType %s and file : %s. %s", contextName1, mimeType0, path.toString(), ex).asError(ex));
            }
            if (proc != null) {
                String s1 = path.toString();
                String s2 = absolutePath.toString();
                if (s1.equals(s2)) {
                    log().info(NMsg.ofC("[%s][%s] [%s] process path : %s", contextName1, proc, NStringUtils.firstNonBlank(mimeType, "no-mimetype"), s1));
                } else {
                    log().info(NMsg.ofC("[%s][%s] [%s] process path : %s = %s", contextName1, proc, NStringUtils.firstNonBlank(mimeType, "no-mimetype"), s1, s2));
                }
                proc.processPath(path, mimeType0,
                        context.newChild()
                                .setUserParentProperties(true)
                                .setWorkingDir(parentPath.toString())
                                .setSourcePath(absolutePath.toString())
                );
                return;
            }
        }
    }

    public void processSourceRegularFile(NPath path, String mimeType, OutputStream out) {
        NPath absolutePath = context.toAbsolutePath(path);
        NPath parentPath = absolutePath.getParent();
        if (!absolutePath.isRegularFile()) {
            throw new NIllegalArgumentException(NMsg.ofC("unsupported file : %s", path.toString()));
        }
        String[] mimeTypes = mimeType == null ? FileProcessorUtils.splitMimeTypes(context.getMimeTypeResolver().resolveMimetype(path.toString()))
                : FileProcessorUtils.splitMimeTypes(mimeType);
        String contextName1 = NStringUtils.firstNonBlank(context.getContextName(), "file");
        for (String mimeType0 : mimeTypes) {
            NSiteProcessor proc = null;
            try {
                proc = getProcessor(mimeType0);
            } catch (Exception ex) {
                log().error(NMsg.ofC("[%s] error resolving processor for mimeType %s and file : %s. %s", contextName1, mimeType0, path.toString(), ex).asError(ex));
            }
            if (proc != null) {
                String s1 = path.toString();
                String s2 = absolutePath.toString();
                String mimeTypesString = NStringUtils.firstNonBlank(mimeType, "no-mimetype");
                if (s1.equals(s2)) {
                    log().debug(NMsg.ofC("[%s] [%s] [%s] execute path : %s = %s", contextName1, proc, mimeTypesString, s1, s2));
                } else {
                    log().debug(NMsg.ofC("[%s] [%s] [%s] execute path : %s", contextName1, proc, mimeTypesString, s1));
                }

                try (InputStream in = path.getInputStream()) {
                    proc.processStream(in, out,
                            context.newChild()
                                    .setUserParentProperties(true)
                                    .setWorkingDir(parentPath.toString())
                                    .setSourcePath(absolutePath.toString())
                    );
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                return;
            }
        }
    }

    public void processSourceTree(NPath path, String targetFolder, Predicate<NPath> filter) {
        NPath opath = path.normalize();
        if (!opath.exists()) {
            log().warn(NMsg.ofC("[%s] source file not found %s", "file", opath));
            return;
        } else if (opath.isDirectory()) {
            context.setWorkingDir(opath.toString());
            if (targetFolder != null) {
                context.setPathTranslator(new DefaultNSitePathTranslator(opath, NPath.of(targetFolder)));
            }
        } else {
            NPath ppath = opath.getParent();
            context.setWorkingDir(ppath.toString());
            if (targetFolder != null) {
                context.setPathTranslator(new DefaultNSitePathTranslator(ppath, NPath.of(targetFolder)));
            }
        }
        processSourceTree(opath, filter);
    }

    public void processSourceTree(NPath path, Predicate<NPath> filter) {
        if (!context.getRootDir().isPresent()) {
            context.setRootDir(path.toString());
        }
        NPath path0 = context.toAbsolutePath(path);
        if (!path0.exists()) {
            log().warn(NMsg.ofC("[%s] source file not found %s", "file", path0));
            return;
        } else if (path0.isRegularFile()) {
            if (filter == null || filter.test(path0)) {
                processSourceRegularFile(path, null);
            }
        } else if (path.isDirectory()) {
            try (NStream<NPath> stream = path0.walk().filter(x -> x.isRegularFile())) {
                stream.forEach(x -> {
                    if (filter == null || filter.test(x)) {
                        try {
                            processSourceRegularFile(x, null);
                        } catch (Exception ex) {
                            throw new NIllegalArgumentException(NMsg.ofC("error processing %s : %s", x, ex), ex);
                        }
                    }
                });
            }
        } else {
            throw new NIOException(NMsg.ofC("unsupported path %s", path));
        }
    }

    public void processResourceTree(NPath path, String targetFolder, Predicate<NPath> filter) {
        NPath opath = path.normalize();
        if (!opath.exists()) {
            log().warn(NMsg.ofC("[%s] source file not found %s", "file", opath));
            return;
        } else if (opath.isDirectory()) {
            context.setWorkingDir(opath.toString());
            if (targetFolder != null) {
                context.setPathTranslator(new DefaultNSitePathTranslator(opath, NPath.of(targetFolder)));
            }
        } else {
            NPath ppath = opath.getParent();
            context.setWorkingDir(ppath.toString());
            if (targetFolder != null) {
                context.setPathTranslator(new DefaultNSitePathTranslator(opath, ppath));
            }
        }
        processResourceTree(opath, filter);
    }

    private static NLog log() {
        return NLog.ofScoped(NSiteProcessorManager.class);
    }

    public void processFiles(NPath path, Predicate<NPath> filter) {
        NPath path0 = context.toAbsolutePath(path);
        if (!path0.exists()) {
            log().warn(NMsg.ofC("[%s] source file not found %s", "file", path0));
            return;
        } else if (path0.isRegularFile()) {
            if (filter == null || filter.test(path0)) {
                processSourceRegularFile(path0, null);
            }
        } else if (path0.isDirectory()) {
            try (Stream<NPath> stream = path0.list().stream()) {
                stream.filter(filter)
                        .forEach(x -> {
                            if (x.isRegularFile()) {
                                if (filter == null || filter.test(x)) {
                                    processSourceRegularFile(path0, null);
                                }
                            }
                        });
            }
        } else {
            throw new NIOException(NMsg.ofC("unsupported path %s", path));
        }
    }

    public NOptional<NSiteProcessor> resolveFileProcessor(NPath path, String mimeType) {
        NPath absolutePath = context.toAbsolutePath(path);
        NPath parentPath = absolutePath.getParent();
        if (!absolutePath.isRegularFile()) {
            return NOptional.ofNamedEmpty(NMsg.ofC("processor not found for %s and %s", path, mimeType));
        }
        String[] mimeTypes = mimeType == null ? FileProcessorUtils.splitMimeTypes(context.getMimeTypeResolver().resolveMimetype(path.toString()))
                : FileProcessorUtils.splitMimeTypes(mimeType);
        String contextName1 = NStringUtils.firstNonBlank(context.getContextName(), "file");
        for (String mimeType0 : mimeTypes) {
            NSiteProcessor proc = null;
            try {
                proc = getProcessor(mimeType0);
            } catch (Exception ex) {
                log().error(NMsg.ofC("[%s] error resolving processor for mimeType %s and file : %s. %s", contextName1, mimeType0, path.toString(), ex).asError(ex));
            }
            if (proc != null) {
                String s1 = path.toString();
                String s2 = absolutePath.toString();
                String mimeTypesString = NStringUtils.firstNonBlank(mimeType, "no-mimetype");
                if (s1.equals(s2)) {
                    log().debug(NMsg.ofC("[%s] [%s] [%s] execute path : %s = %s", contextName1, proc, mimeTypesString, s1, s2));
                } else {
                    log().debug(NMsg.ofC("[%s] [%s] [%s] execute path : %s", contextName1, proc, mimeTypesString, s1));
                }

                return NOptional.of(proc);
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("processor not found for %s and %s", path, mimeType));
    }

    public String processString(String source, String mimeType) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        processStream(new ByteArrayInputStream(source.getBytes()), bos, mimeType);
        return bos.toString();
    }

    public void processStream(InputStream source, OutputStream target, String mimeType) {
        String[] mimeTypes = FileProcessorUtils.splitMimeTypes(mimeType);
        for (String mimeType0 : mimeTypes) {
            NSiteProcessor proc = null;
            try {
                proc = getProcessor(mimeType0);
            } catch (Exception ex) {
                log().error(NMsg.ofC("[%s] unsupported mimeType %s. %s", "file", mimeType0, mimeType0, ex).asError(ex));
            }
            if (proc != null) {
                try {
                    proc.processStream(source, target,
                            context.newChild()
                                    .setUserParentProperties(true)
                    );
                } catch (Exception ex) {
                    log().error(NMsg.ofC("[%s] error processing mimeType %s. %s", "file", mimeType0, mimeType0, ex).asError(ex));
                }
                return;
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported mimetype : %s", mimeType));
    }

}
