package net.thevpc.nsite.javadoc.java;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.Expression;
import net.thevpc.nsite.javadoc.JDClassDoc;
import net.thevpc.nsite.javadoc.JDDoc;
import net.thevpc.nsite.javadoc.JDFieldDoc;
import net.thevpc.nsite.javadoc.JDType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JPFieldDoc implements JDFieldDoc {

    private FieldDeclaration fieldDeclaration;
    private VariableDeclarator variableDeclarator;
    private EnumConstantDeclaration enumConstantDeclaration;
    private JDClassDoc cls;

    public JPFieldDoc(FieldDeclaration fieldDeclaration, VariableDeclarator variableDeclarator, JDClassDoc cls) {
        this.fieldDeclaration = fieldDeclaration;
        this.variableDeclarator = variableDeclarator;
        this.cls = cls;
    }

    public JPFieldDoc(EnumConstantDeclaration enumConstantDeclaration, JDClassDoc cls) {
        this.enumConstantDeclaration = enumConstantDeclaration;
        this.cls = cls;
    }

    @Override
    public boolean isStatic() {
        if (enumConstantDeclaration != null) {
            return true;
        }
        if (fieldDeclaration != null) {
            for (Modifier modifier : fieldDeclaration.getModifiers()) {
                if (modifier.getKeyword() == Modifier.Keyword.STATIC) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String name() {
        if (enumConstantDeclaration != null) {
            return enumConstantDeclaration.getName().getIdentifier();
        }
        return variableDeclarator != null ? variableDeclarator.getName().getIdentifier() : "";
    }

    @Override
    public String qualifiedName() {
        return (cls != null ? cls.qualifiedName() + "." : "") + name();
    }

    @Override
    public JDType type() {
        if (enumConstantDeclaration != null) {
            return cls != null ? new JPType(cls.name()) : new JPType("Enum");
        }
        return variableDeclarator != null ? new JPType(variableDeclarator.getType()) : new JPType("");
    }

    @Override
    public String constantValueExpression() {
        if (enumConstantDeclaration != null) {
            return null;
        }
        if (variableDeclarator != null) {
            Expression e = variableDeclarator.getInitializer().orElse(null);
            return e == null ? null : e.toString();
        }
        return null;
    }

    @Override
    public boolean isFinal() {
        if (enumConstantDeclaration != null) {
            return true;
        }
        if (fieldDeclaration != null) {
            for (Modifier modifier : fieldDeclaration.getModifiers()) {
                if (modifier.getKeyword() == Modifier.Keyword.FINAL) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isEnumConstant() {
        return enumConstantDeclaration != null;
    }

    @Override
    public String modifiers() {
        if (enumConstantDeclaration != null) {
            return "public static final";
        }
        if (fieldDeclaration != null) {
            return Arrays.stream(fieldDeclaration.getModifiers().toArray()).map(Object::toString).collect(Collectors.joining(" ")).trim();
        }
        return "";
    }

    @Override
    public String[] annotations() {
        List<String> list = new ArrayList<>();
        if (fieldDeclaration != null) {
            for (com.github.javaparser.ast.expr.AnnotationExpr annotation : fieldDeclaration.getAnnotations()) {
                list.add(annotation.toString());
            }
        }
        if (enumConstantDeclaration != null) {
            for (com.github.javaparser.ast.expr.AnnotationExpr annotation : enumConstantDeclaration.getAnnotations()) {
                list.add(annotation.toString());
            }
        }
        return list.toArray(new String[0]);
    }

    @Override
    public JDDoc commentText() {
        if (enumConstantDeclaration != null && enumConstantDeclaration.getComment().isPresent() && enumConstantDeclaration.getComment().get() instanceof JavadocComment) {
            JavadocComment jc = (JavadocComment) enumConstantDeclaration.getComment().get();
            return new JPDoc(JPDoc.parseJavadoc(jc.getContent()));
        }
        if (fieldDeclaration != null && fieldDeclaration.getComment().isPresent() && fieldDeclaration.getComment().get() instanceof JavadocComment) {
            JavadocComment jc = (JavadocComment) fieldDeclaration.getComment().get();
            return new JPDoc(JPDoc.parseJavadoc(jc.getContent()));
        }
        return null;
    }

    @Override
    public JDClassDoc declaringClass() {
        return cls;
    }

    @Override
    public String toString() {
        return modifiers() + " " + (type() != null ? type().asString() : "") + " " + name();
    }
}
