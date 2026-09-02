package net.thevpc.nsite.javadoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NSiteJavadocConfig {
    private String targetDir;
    private List<String> sourceRoots = new ArrayList<>();
    private List<String> packages = new ArrayList<>();
    private String title = "Nuts Core API (Javadoc)";
    private String description = "Nuts Core API reference documentation.";

    public String getTargetDir() {
        return targetDir;
    }

    public NSiteJavadocConfig setTargetDir(String targetDir) {
        this.targetDir = targetDir;
        return this;
    }

    public NSiteJavadocConfig addSourceRoot(String sourceRoot) {
        if (sourceRoot != null && !sourceRoot.trim().isEmpty()) {
            this.sourceRoots.add(sourceRoot.trim());
        }
        return this;
    }

    public NSiteJavadocConfig addSourceRoots(Collection<String> sourceRoots) {
        if (sourceRoots != null) {
            for (String s : sourceRoots) {
                addSourceRoot(s);
            }
        }
        return this;
    }

    public String[] getSourceRoots() {
        return sourceRoots.toArray(new String[0]);
    }

    public NSiteJavadocConfig addPackage(String pkg) {
        if (pkg != null && !pkg.trim().isEmpty()) {
            this.packages.add(pkg.trim());
        }
        return this;
    }

    public NSiteJavadocConfig addPackages(Collection<String> pkgs) {
        if (pkgs != null) {
            for (String p : pkgs) {
                addPackage(p);
            }
        }
        return this;
    }

    public String[] getPackages() {
        return packages.toArray(new String[0]);
    }

    public String getTitle() {
        return title;
    }

    public NSiteJavadocConfig setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public NSiteJavadocConfig setDescription(String description) {
        this.description = description;
        return this;
    }
}
