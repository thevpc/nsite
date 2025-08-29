/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsite;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.io.NPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author thevpc
 */
public class NSiteProjectConfig implements NCmdLineConfigurable ,Cloneable{

    private String targetFolder;
    private String scriptType;
    private String projectPath;
    private Predicate<NPath> pathFilter;
    private String contextName;
    private boolean clean;
    private String javadocTarget;
    private String javadocBackend;

    private List<String> javaSourcePaths = new ArrayList<>();
    private List<String> javaPackages = new ArrayList<>();
    private List<String> sourcePaths = new ArrayList<String>();
    private List<String> resourcePaths = new ArrayList<String>();
    private List<String> initScripts = new ArrayList<>();
    private Map<String, Object> vars;

    public List<String> getJavaSourcePaths() {
        return javaSourcePaths;
    }

    public List<String> getJavaPackages() {
        return javaPackages;
    }

    public String getJavadocTarget() {
        return javadocTarget;
    }

    public String getJavadocBackend() {
        return javadocBackend;
    }

    public void setJavadocTarget(String javadocTarget) {
        this.javadocTarget = javadocTarget;
    }

    public void setJavadocBackend(String javadocBackend) {
        this.javadocBackend = javadocBackend;
    }

    public boolean isClean() {
        return clean;
    }

    public NSiteProjectConfig setClean(boolean clean) {
        this.clean = clean;
        return this;
    }

    public Map<String, Object> getVars() {
        return vars;
    }

    public NSiteProjectConfig setVars(Map<String, Object> vars) {
        this.vars = vars;
        return this;
    }

    public String getContextName() {
        return contextName;
    }

    public NSiteProjectConfig setContextName(String contextName) {
        this.contextName = contextName;
        return this;
    }

    public NSiteProjectConfig addSource(String script) {
        sourcePaths.add(script);
        return this;
    }

    public NSiteProjectConfig addResourceSource(String script) {
        resourcePaths.add(script);
        return this;
    }

    public NSiteProjectConfig addInitScript(String script) {
        initScripts.add(script);
        return this;
    }

    public NSiteProjectConfig setScriptType(String value) {
        this.scriptType = value;
        return this;
    }

    public NSiteProjectConfig setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
        return this;
    }

    public NSiteProjectConfig setProjectPath(String projectPath) {
        this.projectPath = projectPath;
        return this;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public String getScriptType() {
        return scriptType;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public List<String> getSourcePaths() {
        return sourcePaths;
    }

    public List<String> getResourcePaths() {
        return resourcePaths;
    }

    public List<String> getInitScripts() {
        return initScripts;
    }

    public Predicate<NPath> getPathFilter() {
        return pathFilter;
    }

    public void setJavaSourcePaths(List<String> javaSourcePaths) {
        this.javaSourcePaths = javaSourcePaths;
    }

    public void setJavaPackages(List<String> javaPackages) {
        this.javaPackages = javaPackages;
    }

    public void setSourcePaths(List<String> sourcePaths) {
        this.sourcePaths = sourcePaths;
    }

    public void setResourcePaths(List<String> resourcePaths) {
        this.resourcePaths = resourcePaths;
    }

    public void setInitScripts(List<String> initScripts) {
        this.initScripts = initScripts;
    }

    public NSiteProjectConfig setPathFilter(Predicate<NPath> pathFilter) {
        this.pathFilter = pathFilter;
        return this;
    }

    public NSiteProjectConfig copy(){
        return clone();
    }

    @Override
    protected NSiteProjectConfig clone()  {
        try {
            NSiteProjectConfig clone = (NSiteProjectConfig) super.clone();
            if(clone.javaSourcePaths!=null){
                clone.javaSourcePaths=new ArrayList<>(clone.javaSourcePaths);
            }
            if(clone.javaPackages!=null){
                clone.javaPackages=new ArrayList<>(clone.javaPackages);
            }
            if(clone.sourcePaths!=null){
                clone.sourcePaths=new ArrayList<>(clone.sourcePaths);
            }
            if(clone.resourcePaths!=null){
                clone.resourcePaths=new ArrayList<>(clone.resourcePaths);
            }
            if(clone.initScripts!=null){
                clone.initScripts=new ArrayList<>(clone.initScripts);
            }
            if(clone.vars!=null){
                clone.vars=new HashMap<>(clone.vars);
            }

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg option = cmdLine.peek().get();
        switch (option.key()) {
            case "-i":
            case "--init": {
                return cmdLine.matcher().matchEntry((v) -> NSiteProjectConfig.this.addInitScript(v.stringValue())).anyMatch();
            }
            case "--clean": {
                return cmdLine.matcher().matchFlag((v) -> NSiteProjectConfig.this.setClean(v.booleanValue())).anyMatch();
            }
            case "--script": {
                return cmdLine.matcher().matchEntry((v) -> NSiteProjectConfig.this.setScriptType(v.stringValue())).anyMatch();
            }
            case "-t":
            case "--to": {
                return cmdLine.matcher().matchEntry((v) -> NSiteProjectConfig.this.setTargetFolder(v.stringValue())).anyMatch();
            }
            case "-s":
            case "--src": {
                return cmdLine.matcher().matchEntry((v) -> NSiteProjectConfig.this.addSource(v.stringValue())).anyMatch();
            }
            case "-p":
            case "--project": {
                return cmdLine.matcher().matchEntry((v) -> NSiteProjectConfig.this.setProjectPath(v.stringValue())).anyMatch();
            }
            case "-r":
            case "--resource": {
                return cmdLine.matcher().matchEntry((v) -> NSiteProjectConfig.this.addResourceSource(v.stringValue())).anyMatch();
            }
            ///////////////////////////

            case "--java-source": {
                return cmdLine.matcher().matchEntry((v) -> javaSourcePaths.add(v.stringValue())).anyMatch();
            }
            case "--javadoc-target": {
                return cmdLine.matcher().matchEntry((v) -> javadocTarget = v.stringValue()).anyMatch();
            }
            case "--java-package": {
                return cmdLine.matcher().matchEntry((v) -> javaPackages.add(v.stringValue())).anyMatch();
            }
            case "--javadoc-backend": {
                return cmdLine.matcher().matchEntry((v) -> javadocBackend = v.stringValue()).anyMatch();
            }
        }
        return false;
    }
}
