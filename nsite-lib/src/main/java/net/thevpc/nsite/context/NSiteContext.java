/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsite.context;

import net.thevpc.nsite.processor.NSiteProcessorManager;
import net.thevpc.nsite.util.FileProcessorUtils;
import net.thevpc.nsite.util.StringUtils;
import net.thevpc.nuts.io.NErr;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nsite.NSiteProjectConfig;
import net.thevpc.nsite.executor.NSiteExecutor;
import net.thevpc.nsite.executor.NSiteExecutorManager;
import net.thevpc.nsite.javadoc.MdDoclet;
import net.thevpc.nsite.javadoc.MdDocletConfig;
import net.thevpc.nsite.mimetype.DefaultNSiteMimeTypeResolver;
import net.thevpc.nsite.mimetype.NSiteMimeTypeResolver;
import net.thevpc.nsite.processor.html.PageToHtmlUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogContext;
import net.thevpc.nuts.log.NLogs;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class NSiteContext {

    public static final String ROOT_DIR = "rootDir";
    public static final String WORKING_DIR = "workingDir";
    public static final String SOURCE_PATH = "sourcePath";
    public static final String PROJECT_FILENAME = "project.nexpr";
    private PageToHtmlUtils rootPageToHtmlUtils = new PageToHtmlUtils();

    private final Map<String, Object> vars = new HashMap<>();
    private NSiteContext parent;
    private PageToHtmlUtils pageToHtmlUtils;
    private Function<String, Object> customVarEvaluator;
    private NSiteMimeTypeResolver mimeTypeResolver;
    private String sourcePath;
    private String rootDir;
    private String workingDir;
    private boolean userParentProperties;
    private NSitePathTranslator pathTranslator;
    private String projectFileName = PROJECT_FILENAME;
    private String contextName;
    private String currentMimeType;
    private NSiteExecutorManager executorManager;
    private NSiteProcessorManager processorManager;
    private String projectRoot;

    public NSiteContext() {
        executorManager = new NSiteExecutorManager(this);
        processorManager = new NSiteProcessorManager(this);
    }

    public NSiteContext(NSiteContext parent) {
        this();
        this.parent = parent;
    }

    public PageToHtmlUtils md2Html() {
        if (pageToHtmlUtils != null) {
            return pageToHtmlUtils;
        }
        if (parent != null) {
            parent.md2Html();
        }
        return rootPageToHtmlUtils;
    }

    public NSiteProcessorManager getProcessorManager() {
        return processorManager;
    }

    private static NPath workingPath(NPath p) {
        if (p.isDirectory()) {
            return p;
        }
        NPath pp = p.getParent();
        if (pp == null) {
            throw new IllegalArgumentException("Unsupported");
        }
        return pp;
    }

    public boolean isUserParentProperties() {
        return userParentProperties;
    }

    public NSiteContext setUserParentProperties(boolean userParentProperties) {
        this.userParentProperties = userParentProperties;
        return this;
    }

    public String getProjectFileName() {
        return projectFileName;
    }

    public NSiteContext setProjectFileName(String projectFileName) {
        if (projectFileName == null || projectFileName.isEmpty()) {
            projectFileName = PROJECT_FILENAME;
        }
        this.projectFileName = projectFileName;
        return this;
    }

    public NSiteContext newChild() {
        return new NSiteContext(this);
    }

    public String getContextName() {
        if (contextName != null) {
            return contextName;
        }
        if (parent != null) {
            return parent.getContextName();
        }
        return null;
    }

    public String getProjectRoot() {
        if (projectRoot != null) {
            return projectRoot;
        }
        if (parent != null) {
            return parent.getProjectRoot();
        }
        return null;
    }

    public void setProjectRoot(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    public NSiteExecutorManager getExecutorManager() {
        return executorManager;
    }


    public Function<String, Object> getCustomVarEvaluator() {
        if (customVarEvaluator != null) {
            return customVarEvaluator;
        }
        if (parent != null) {
            return parent.getCustomVarEvaluator();
        }
        return null;
    }

    public NSiteContext setCustomVarEvaluator(Function<String, Object> customVarEvaluator) {
        this.customVarEvaluator = customVarEvaluator;
        return this;
    }


    public NSiteContext getParent() {
        return parent;
    }

    public NSiteContext setParent(NSiteContext parent) {
        this.parent = parent;
        return this;
    }

    public String getWorkingDirRequired() {
        return (String) getVar(WORKING_DIR).get();
    }

    public String getRootDirRequired() {
        return (String) getVar(ROOT_DIR).get();
    }

    public NOptional<String> getWorkingDir() {
        return (NOptional) getVar(WORKING_DIR);
    }

    public NSiteContext setWorkingDir(String workingDir) {
        return setVar(WORKING_DIR, workingDir);
    }

    public NOptional<String> getRootDir() {
        return (NOptional) getVar(ROOT_DIR);
    }

    public NSiteContext setRootDir(String rootDir) {
        return setVar(ROOT_DIR, rootDir);
    }

    public NSiteContext removeWorkingDir() {
        return remove(WORKING_DIR);
    }

    public NSiteContext removeRootDir() {
        return remove(ROOT_DIR);
    }

    public String getSourcePathRequired() {
        return (String) getVar(SOURCE_PATH).get();
    }

    public NOptional<String> getSourcePath() {
        return getVar(SOURCE_PATH);
    }

    public NSiteContext setSourcePath(String workingDir) {
        return setVar(SOURCE_PATH, workingDir);
    }

    public boolean isSet(String name) {
        switch (name) {
            case ROOT_DIR:
            case SOURCE_PATH:
            case WORKING_DIR: {
                return true;
            }
        }
        return vars.containsKey(name);
    }

    public void setVars(Map<String, Object> vars) {
        if (vars != null) {
            for (Map.Entry<String, Object> e : vars.entrySet()) {
                setVar(e.getKey(), e.getValue());
            }
        }
    }

    public NSiteContext setVar(String name, Object value) {
        switch (name) {
            case SOURCE_PATH: {
                this.sourcePath = (String) value;
                return this;
            }
            case WORKING_DIR: {
                this.workingDir = (String) value;
                return this;
            }
            case ROOT_DIR: {
                this.rootDir = (String) value;
                return this;
            }
        }
        if (parent != null && userParentProperties) {
            parent.setVar(name, value);
            return this;
        }
        vars.put(name, value);
        return this;
    }

    public NSiteContext remove(String name) {
        switch (name) {
            case ROOT_DIR:
            case SOURCE_PATH:
            case WORKING_DIR: {
                setVar(name, null);
                return this;
            }
        }
        if (parent != null && userParentProperties) {
            parent.remove(name);
            return this;
        }
        vars.remove(name);
        return this;
    }

    public Map<String, Object> getVars() {
        Map<String, Object> result = new HashMap<>(vars);
        if (parent != null) {
            for (Map.Entry<String, Object> e : parent.getVars().entrySet()) {
                if (!result.containsKey(e.getKey())) {
                    result.put(e.getKey(), e.getValue());
                }
            }
        }
        return result;
    }

    public Object getVar(String name, Object defaultValue) {
        return getVar(name).orElse(defaultValue);
    }

    public <T> NOptional<T> getVar(String name) {
        switch (name) {
            case SOURCE_PATH: {
                if (this.sourcePath != null) {
                    return (NOptional<T>) NOptional.of(this.sourcePath);
                }
                break;
            }
            case WORKING_DIR: {
                if (this.workingDir != null) {
                    return (NOptional<T>) NOptional.of(this.workingDir);
                }
                break;
            }
            case ROOT_DIR: {
                if (this.rootDir != null) {
                    return (NOptional<T>) NOptional.of(this.rootDir);
                }
                break;
            }
        }
        T r = (T) vars.get(name);
        if (r != null) {
            return NOptional.of(r);
        }
        if (vars.containsKey(name)) {
            return NOptional.ofNull();
        }
        if (customVarEvaluator != null) {
            r = (T) customVarEvaluator.apply(name);
            if (r != null) {
                return NOptional.of(r);
            }
        }
        if (parent != null) {
            return parent.getVar(name);
        }
        return NOptional.ofEmpty(() -> {
            String source = getSourcePath().orElse(null);
            if (source == null) {
                return NMsg.ofC("not found : %s", StringUtils.escapeString(name));
            } else {
                return NMsg.ofC("not found : %s in %s", StringUtils.escapeString(name), source);
            }
        });
    }


//    public void processResourceTree(Path path, String targetFolder, Predicate<Path> filter) {
//        getProcessorManager().
//    }

    public void executeProjectFile(NPath path, String mimeTypesString) {
        getExecutorManager().executeRegularFile(path, mimeTypesString);
    }


    public NPath toAbsolutePath(NPath path) {
        return FileProcessorUtils.toRealPath(path, NPath.of(getWorkingDirRequired()));
    }

    public String executeString(String source, String mimeType) {
        return executeStream(new ByteArrayInputStream(source.getBytes()), mimeType);
    }

    public Object eval(String source, String mimeType) {
        return eval(new ByteArrayInputStream(source == null ? new byte[0] : source.getBytes()), mimeType);
    }

    public Object eval(InputStream source, String mimeType) {
        String[] mimeTypes = FileProcessorUtils.splitMimeTypes(mimeType);
        NMsg lastError = null;
        for (String mimeType0 : mimeTypes) {
            NSiteExecutor proc = null;
            proc = getExecutorManager().getExecutor(mimeType0);
            if (proc != null) {
                try {
                    return proc.eval(source,
                            newChild()
                                    .setUserParentProperties(true)
                    );
                } catch (Exception ex) {
                    lastError = NMsg.ofC("[%s] error processing mimeType : %s. : %s", "file", mimeType, ex).asError(ex);
                    NLog.of(NSiteContext.class).scoped().error(lastError);
                }
            }
        }
        if (lastError != null) {
            throw new NIllegalArgumentException(lastError);
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported mimetype : %s", mimeType));
    }

    public String executeStream(InputStream source, String mimeType) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            String[] mimeTypes = FileProcessorUtils.splitMimeTypes(mimeType);
            for (String mimeType0 : mimeTypes) {
                NSiteExecutor proc = null;
                proc = getExecutorManager().getExecutor(mimeType0);
                if (proc != null) {
                    try {
                        proc.processStream(source, bos,
                                newChild()
                                        .setUserParentProperties(true)
                        );
                    } catch (Exception ex) {
                        NLog log = NLog.of(NSiteContext.class).scoped();
                        NMsg msg = NMsg.ofC("[%s] error processing mimeType : %s. %s", "file", mimeType, ex);
                        if(log.isLoggable(Level.FINEST)) {
                            msg=msg.asError(ex);
                        }
                        log.error(msg);
                    }
                    return bos.toString();
                }
            }
            throw new NIllegalArgumentException(NMsg.ofC("unsupported mimetype : %s", mimeType));
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }


    public NSiteMimeTypeResolver getMimeTypeResolver() {
        if (mimeTypeResolver != null) {
            return mimeTypeResolver;
        }
        if (parent != null) {
            return parent.getMimeTypeResolver();
        }
        return DefaultNSiteMimeTypeResolver.DEFAULT;
    }

    public NSiteContext setMimeTypeResolver(NSiteMimeTypeResolver mimeTypeResolver) {
        this.mimeTypeResolver = mimeTypeResolver;
        return this;
    }

    public NSitePathTranslator getPathTranslator() {
        if (pathTranslator != null) {
            return pathTranslator;
        }
        if (parent != null) {
            return parent.getPathTranslator();
        }
        return null;
    }

    public NSiteContext setPathTranslator(NSitePathTranslator pathTranslator) {
        this.pathTranslator = pathTranslator;
        return this;
    }

    public NSiteContext setPathTranslator(NPath from, NPath to) {
        return setPathTranslator(new DefaultNSitePathTranslator(from, to));
    }

    public NSiteContext setTargetPath(NPath to) {
        return setPathTranslator(new DefaultNSitePathTranslator(NPath.of(getWorkingDirRequired()), to));
    }

    public String getCurrentMimeType() {
        return currentMimeType;
    }

    public void setCurrentMimeType(String currentMimeType) {
        this.currentMimeType = currentMimeType;
    }

    public void run(NSiteProjectConfig config0) {
        NLogs.of().runWith(NLogContext.ofLog(NLog.of(NSiteContext.class))
                        .withLog(m->{
                            if(m.isError()){
                                NErr.println(m);
                            }else{
                                NOut.println(m);
                            }
                        })
                        .withMessagePrefix(a->NMsg.ofC("[%s] [%s]", Instant.now(),a.getLevel()))
                , ()-> {
            NSiteProjectConfig config = config0.copy();
            String scriptType = config.getScriptType();
            String targetFolder = config.getTargetFolder();
            setVars(config.getVars());
            this.contextName = NStringUtils.trimToNull(config.getContextName());
            String projectPath = config.getProjectPath();
            boolean projectFolderSpecified = !NBlankable.isBlank(projectPath);
            List<String> initScripts = new ArrayList<>();
            List<String> sourcePaths = new ArrayList<>();
            List<String> resourcePaths = new ArrayList<>();
            NPath oProjectDirPath = null;
            if (!projectFolderSpecified) {
                setProjectRoot(System.getProperty("user.dir"));
            } else {
                oProjectDirPath = NPath.of(projectPath).normalize();
                setProjectRoot(oProjectDirPath.toString());
                NPath oProjectFile = oProjectDirPath.resolve(getProjectFileName());
                if (oProjectFile.isRegularFile()) {
                    initScripts.add(oProjectFile.toString());
                } else {
                    //throw new NIllegalArgumentException(NMsg.ofC("invalid project, missing project.nexpr : %s", oProjectDirPath));
                }
                for (NPath script : oProjectDirPath.resolve("scripts").list().stream().sorted(Comparator.comparing(x -> x.getName())).collect(Collectors.toList())) {
                    if (!Objects.equals(getProjectFileName(), script.getName())) {
                        initScripts.add(oProjectFile.toString());
                    }
                }
                sourcePaths.add(0, oProjectDirPath.resolve("src/main").toString());
                resourcePaths.add(0, oProjectDirPath.resolve("src/resources").toString());
                if (NBlankable.isBlank(targetFolder)) {
                    targetFolder = oProjectDirPath.resolve("dist").toString();
                }
            }

            if (NBlankable.isBlank(targetFolder)) {
                throw new NIllegalArgumentException(NMsg.ofPlain("missing target folder"));
            }

            if (config.getInitScripts() != null) {
                for (String path : config.getInitScripts()) {
                    initScripts.add(toAbsolutePath(path, oProjectDirPath).toString());
                }
            }
            initScripts = new ArrayList<>(new LinkedHashSet<>(initScripts));

            if (config.getSourcePaths() != null) {
                for (String path : config.getSourcePaths()) {
                    sourcePaths.add(toAbsolutePath(path, oProjectDirPath).toString());
                }
            }
            sourcePaths = new ArrayList<>(new LinkedHashSet<>(sourcePaths));

            if (config.getResourcePaths() != null) {
                for (String path : config.getResourcePaths()) {
                    resourcePaths.add(toAbsolutePath(path, oProjectDirPath).toString());
                }
            }
            resourcePaths = new ArrayList<>(new LinkedHashSet<>(resourcePaths));


            if (sourcePaths.isEmpty()) {
                throw new NIllegalArgumentException(NMsg.ofPlain("missing path to process"));
            }

            if (scriptType != null) {
                if (scriptType.indexOf('/') < 0) {
                    scriptType = "text/" + scriptType;
                }
            } else {
                //scriptType = MimeTypeConstants.APPLICATION_SIMPLE_EXPR;
            }


            if (projectFolderSpecified) {
                //&& targetFolder==null
                String tf = (String) this.getVar("targetFolder", null);
                if (tf != null && targetFolder == null) {
                    targetFolder = tf;
                }
            }
            if (targetFolder == null) {
                throw new NIllegalArgumentException(NMsg.ofPlain("missing target folder"));
            }

            config.setInitScripts(initScripts);
            config.setSourcePaths(sourcePaths);
            config.setResourcePaths(resourcePaths);
            config.setTargetFolder(targetFolder);
            config.setTargetFolder(resolvePath(targetFolder));


            if (config.isClean()) {
                cleanTargetFolder(config);
            }

            for (String initScript : initScripts) {
                NPath fpath = NPath.of(initScript).toAbsolute();
                this.setWorkingDir(workingPath(fpath).toString());
                this.executeProjectFile(fpath, scriptType);
            }


            FileProcessorUtils.mkdirs(NPath.of(targetFolder));

            if (config.getResourcePaths() != null) {
                for (String path : new LinkedHashSet<>(config.getResourcePaths())) {
                    getProcessorManager().processResourceTree(NPath.of(path), targetFolder, config.getPathFilter());
                }
            }

            runJavadoc(config);

            for (String path : sourcePaths) {
                getProcessorManager().processSourceTree(NPath.of(path), targetFolder, config.getPathFilter());
            }
        });
    }

    private void runJavadoc(NSiteProjectConfig config) {
        if (config.getJavaSourcePaths().isEmpty()) {
            return;
        }
        String javadocTarget = config.getJavadocTarget();
        if (javadocTarget == null) {
            javadocTarget = ".";
        }
        new MdDoclet().start(new MdDocletConfig()
                .addSources(config.getJavaSourcePaths())
                .addPackages(config.getJavaPackages())
                .setTarget(javadocTarget)
                .setBackend(config.getJavadocBackend())
        );
    }

    private void cleanTargetFolder(NSiteProjectConfig config) {
        boolean clean = true;
        String targetFolder = config.getTargetFolder();
        NPath p2 = NPath.of(resolvePath(targetFolder)).toAbsolute();
        for (String path : config.getSourcePaths()) {
            NPath p1 = NPath.of(resolvePath(path)).toAbsolute();
            if (p1.isEqOrDeepChildOf(p2)) {
                clean = false;
            }
            if (p2.isEqOrDeepChildOf(p1)) {
                clean = false;
            }
        }
        for (String path : config.getResourcePaths()) {
            NPath p1 = NPath.of(resolvePath(path)).toAbsolute();
            if (p1.isEqOrDeepChildOf(p2)) {
                clean = false;
            }
            if (p2.isEqOrDeepChildOf(p1)) {
                clean = false;
            }
        }
        if (clean && p2.exists()) {
            for (NPath nPath : p2.list()) {
                nPath.deleteTree();
            }
        }
    }

    private NPath toAbsolutePath(String path, NPath base) {
        if (NPath.of(path).isAbsolute()) {
            return NPath.of(path).normalize();
        }
        if (base == null) {
            return NPath.ofUserDirectory().resolve(path);
        }
        return base.normalize().resolve(path);
    }

    private String resolvePath(String path) {
        return NPath.of(path).toAbsolute().normalize().toString();
    }

    public void processSourceTree(NPath path, Predicate<NPath> filter) {
        getProcessorManager().processSourceTree(path, filter);
    }
}
