package net.thevpc.nsite.javadoc.java;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import net.thevpc.nsite.javadoc.JDClassDoc;
import net.thevpc.nsite.javadoc.JDPackageDoc;
import net.thevpc.nsite.javadoc.JDRootDoc;
import net.thevpc.nuts.io.NPath;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JPRootDoc implements JDRootDoc {

    private Map<String, JDClassDoc> classes = new HashMap<>();
    private Map<String, JPPackageDoc> packages = new HashMap<>();
    private JavaParser parser;

    public void initParser(List<NPath> sourceRoots) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        for (NPath srcRoot : sourceRoots) {
            NPath s = srcRoot;
            if (s.resolve("pom.xml").isRegularFile() && s.resolve("src/main/java").isDirectory()) {
                s = s.resolve("src/main/java");
            }
            File f = s.toFile().orNull();
            if (f != null && f.isDirectory()) {
                try {
                    combinedTypeSolver.add(new JavaParserTypeSolver(f));
                } catch (Exception ex) {
                    // ignore
                }
            }
        }
        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));
        this.parser = new JavaParser(configuration);
    }

    public void parseSrcFolder(NPath path, Predicate<String> packageFilter) {
        if (path.resolve("pom.xml").isRegularFile() && path.resolve("src/main/java").isDirectory()) {
            path = path.resolve("src/main/java");
        }
        NPath path0 = path;
        path0.walk().filter(x -> x.isRegularFile() && x.name().toString().endsWith(".java")).forEach(file -> {
            String pck = StreamSupport.stream(file.subpath(path0.nameCount(), file.nameCount() - 1).names().spliterator(), false)
                    .collect(Collectors.joining("."));
            if (packageFilter == null || packageFilter.test(pck)) {
                parseFile(file);
            }
        });
    }

    public void parseFile(NPath path) {
        try {
            File f = path.toFile().orNull();
            if (f == null || !f.exists()) {
                return;
            }
            CompilationUnit cu = null;
            if (parser != null) {
                ParseResult<CompilationUnit> result = parser.parse(f);
                cu = result.getResult().orElse(null);
            }
            if (cu == null) {
                cu = StaticJavaParser.parse(f);
            }
            if (cu == null) {
                return;
            }

            String pckName = cu.getPackageDeclaration().map(PackageDeclaration::getNameAsString).orElse("");
            JPPackageDoc pkgDoc = packages.computeIfAbsent(pckName, JPPackageDoc::new);

            if (path.name().toString().equals("package-info.java")) {
                JavadocComment jc = null;
                if (cu.getPackageDeclaration().isPresent()) {
                    PackageDeclaration pd = cu.getPackageDeclaration().get();
                    if (pd.getComment().isPresent() && pd.getComment().get() instanceof JavadocComment) {
                        jc = (JavadocComment) pd.getComment().get();
                    }
                }
                if (jc == null && cu.getComment().isPresent() && cu.getComment().get() instanceof JavadocComment) {
                    jc = (JavadocComment) cu.getComment().get();
                }
                if (jc == null) {
                    for (com.github.javaparser.ast.comments.Comment c : cu.getAllComments()) {
                        if (c instanceof JavadocComment) {
                            jc = (JavadocComment) c;
                            // continue to pick the last one if multiple exist (e.g. license + javadoc)
                        }
                    }
                }
                if (jc != null) {
                    pkgDoc.setDescription(new JPDoc(JPDoc.parseJavadoc(jc.getContent())));
                }
                return;
            }

            for (TypeDeclaration<?> type : cu.getTypes()) {
                if (type.isPublic() || type.isProtected() || (!type.isPrivate() && !type.isProtected() && !type.isPublic())) {
                    JPClassDoc classDoc = new JPClassDoc(this, type, pckName);
                    add(classDoc);
                    pkgDoc.addType(classDoc);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error parsing " + path, e);
        }
    }

    @Override
    public JDClassDoc findClass(String qualifiedName) {
        if (qualifiedName == null) {
            return null;
        }
        JDClassDoc doc = classes.get(qualifiedName);
        if (doc != null) {
            return doc;
        }
        for (JDClassDoc c : classes.values()) {
            if (c.name().equals(qualifiedName) || c.qualifiedName().equals(qualifiedName)) {
                return c;
            }
        }
        return null;
    }

    @Override
    public JDPackageDoc findPackage(String packageName) {
        return packages.get(packageName);
    }

    public JPRootDoc add(JDClassDoc c) {
        classes.put(c.qualifiedName(), c);
        return this;
    }

    @Override
    public JDClassDoc[] classes() {
        List<JDClassDoc> list = new ArrayList<>(classes.values());
        list.sort(Comparator.comparing(JDClassDoc::qualifiedName));
        return list.toArray(new JDClassDoc[0]);
    }

    @Override
    public JDPackageDoc[] packages() {
        List<JDPackageDoc> list = new ArrayList<>(packages.values());
        list.sort(Comparator.comparing(JDPackageDoc::name));
        return list.toArray(new JDPackageDoc[0]);
    }
}
