package net.thevpc.nsite.javadoc.java;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import net.thevpc.nsite.javadoc.JDClassDoc;
import net.thevpc.nsite.javadoc.JDConstructorDoc;
import net.thevpc.nsite.javadoc.JDDoc;
import net.thevpc.nsite.javadoc.JDParameter;
import net.thevpc.nsite.javadoc.JDType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JPConstructorDoc implements JDConstructorDoc {

    private ConstructorDeclaration declaration;
    private JDClassDoc cls;

    public JPConstructorDoc(ConstructorDeclaration declaration, JDClassDoc cls) {
        this.declaration = declaration;
        this.cls = cls;
    }

    @Override
    public JDParameter[] parameters() {
        List<JDParameter> param = new ArrayList<>();
        for (Parameter parameter : declaration.getParameters()) {
            String n = parameter.getName().toString();
            String javadocContent = null;
            Javadoc jd = declaration.getJavadoc().orElse(null);
            if (jd != null) {
                for (JavadocBlockTag blockTag : jd.getBlockTags()) {
                    if (blockTag.getType() == JavadocBlockTag.Type.PARAM && blockTag.getName().orElse("").equals(n)) {
                        javadocContent = blockTag.getContent().toText().trim();
                    }
                }
            }
            param.add(new JPParameter(parameter, javadocContent));
        }
        return param.toArray(new JDParameter[0]);
    }

    @Override
    public JDType[] thrownExceptions() {
        List<JDType> list = new ArrayList<>();
        for (ReferenceType thrownException : declaration.getThrownExceptions()) {
            list.add(new JPType(thrownException));
        }
        return list.toArray(new JDType[0]);
    }

    @Override
    public String name() {
        return declaration.getName().toString();
    }

    @Override
    public String qualifiedName() {
        return (cls != null ? cls.qualifiedName() + "." : "") + name();
    }

    @Override
    public String modifiers() {
        return Arrays.stream(declaration.getModifiers().toArray()).map(Object::toString).collect(Collectors.joining(" ")).trim();
    }

    @Override
    public String[] annotations() {
        List<String> list = new ArrayList<>();
        if (declaration != null) {
            for (com.github.javaparser.ast.expr.AnnotationExpr annotation : declaration.getAnnotations()) {
                list.add(annotation.toString());
            }
        }
        return list.toArray(new String[0]);
    }

    @Override
    public String typeParameters() {
        if (!declaration.getTypeParameters().isEmpty()) {
            return "<" + declaration.getTypeParameters().stream().map(TypeParameter::asString).collect(Collectors.joining(", ")) + ">";
        }
        return "";
    }

    @Override
    public JDDoc commentText() {
        if (declaration.getComment().isPresent() && declaration.getComment().get() instanceof JavadocComment) {
            JavadocComment jc = (JavadocComment) declaration.getComment().get();
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
        return modifiers() + " " + name() + "(" + Arrays.stream(parameters()).map(Object::toString).collect(Collectors.joining(", ")) + ")";
    }
}
