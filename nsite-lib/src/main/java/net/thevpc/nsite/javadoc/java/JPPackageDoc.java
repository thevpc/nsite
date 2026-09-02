package net.thevpc.nsite.javadoc.java;

import net.thevpc.nsite.javadoc.JDClassDoc;
import net.thevpc.nsite.javadoc.JDDoc;
import net.thevpc.nsite.javadoc.JDPackageDoc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JPPackageDoc implements JDPackageDoc {

    private String name;
    private JDDoc description;
    private List<JDClassDoc> types = new ArrayList<>();

    public JPPackageDoc(String name) {
        this.name = name;
    }

    public void addType(JDClassDoc type) {
        this.types.add(type);
    }

    public void setDescription(JDDoc description) {
        this.description = description;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public JDDoc description() {
        return description;
    }

    @Override
    public JDClassDoc[] allTypes() {
        List<JDClassDoc> list = new ArrayList<>(types);
        list.sort(Comparator.comparing(JDClassDoc::name));
        return list.toArray(new JDClassDoc[0]);
    }

    @Override
    public JDClassDoc[] classes() {
        return types.stream()
                .filter(x -> x.isClass() && !x.isException())
                .sorted(Comparator.comparing(JDClassDoc::name))
                .toArray(JDClassDoc[]::new);
    }

    @Override
    public JDClassDoc[] interfaces() {
        return types.stream()
                .filter(JDClassDoc::isInterface)
                .sorted(Comparator.comparing(JDClassDoc::name))
                .toArray(JDClassDoc[]::new);
    }

    @Override
    public JDClassDoc[] enums() {
        return types.stream()
                .filter(JDClassDoc::isEnum)
                .sorted(Comparator.comparing(JDClassDoc::name))
                .toArray(JDClassDoc[]::new);
    }

    @Override
    public JDClassDoc[] exceptions() {
        return types.stream()
                .filter(JDClassDoc::isException)
                .sorted(Comparator.comparing(JDClassDoc::name))
                .toArray(JDClassDoc[]::new);
    }

    @Override
    public JDClassDoc[] annotations() {
        return types.stream()
                .filter(JDClassDoc::isAnnotation)
                .sorted(Comparator.comparing(JDClassDoc::name))
                .toArray(JDClassDoc[]::new);
    }

    @Override
    public String toString() {
        return name;
    }
}
