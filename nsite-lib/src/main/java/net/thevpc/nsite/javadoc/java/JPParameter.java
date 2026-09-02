package net.thevpc.nsite.javadoc.java;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import net.thevpc.nsite.javadoc.JDParameter;
import net.thevpc.nsite.javadoc.JDType;

import java.util.ArrayList;
import java.util.List;

public class JPParameter implements JDParameter {

    private Parameter parameter;
    private String name;
    private JDType type;
    private String javadocContent;
    private List<String> annotations = new ArrayList<>();

    public JPParameter(Parameter parameter, String javadocContent) {
        this.parameter = parameter;
        this.name = parameter != null ? parameter.getName().toString() : "";
        this.type = parameter != null ? new JPType(parameter.getType()) : new JPType("");
        this.javadocContent = javadocContent;
        if (parameter != null) {
            for (AnnotationExpr annotation : parameter.getAnnotations()) {
                annotations.add(annotation.toString());
            }
        }
    }

    public JPParameter(String name, JDType type, String javadocContent) {
        this.name = name;
        this.type = type;
        this.javadocContent = javadocContent;
    }

    @Override
    public String[] annotations() {
        return annotations.toArray(new String[0]);
    }

    @Override
    public String getJavadocContent() {
        return javadocContent;
    }

    public void setJavadocContent(String javadocContent) {
        this.javadocContent = javadocContent;
    }

    @Override
    public JDType type() {
        return type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return (type != null ? type.asString() : "") + " " + name;
    }
}
