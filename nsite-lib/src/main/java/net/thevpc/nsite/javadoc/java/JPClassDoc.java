package net.thevpc.nsite.javadoc.java;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import net.thevpc.nsite.javadoc.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JPClassDoc implements JDClassDoc {

    private JPRootDoc root;
    private String packageName;
    private TypeDeclaration<?> declaration;
    private List<JDMethodDoc> methods = new ArrayList<>();
    private List<JDConstructorDoc> constructors = new ArrayList<>();
    private List<JDFieldDoc> fields = new ArrayList<>();
    private List<JDFieldDoc> enumConstants = new ArrayList<>();

    public JPClassDoc(JPRootDoc root, TypeDeclaration<?> declaration, String packageName) {
        this.root = root;
        this.declaration = declaration;
        this.packageName = packageName;

        if (declaration.isEnumDeclaration()) {
            EnumDeclaration ed = declaration.asEnumDeclaration();
            for (EnumConstantDeclaration entry : ed.getEntries()) {
                enumConstants.add(new JPFieldDoc(entry, this));
            }
        }

        for (BodyDeclaration<?> member : declaration.getMembers()) {
            if (member instanceof MethodDeclaration) {
                methods.add(new JPMethodDoc((MethodDeclaration) member, this));
            } else if (member instanceof ConstructorDeclaration) {
                constructors.add(new JPConstructorDoc((ConstructorDeclaration) member, this));
            } else if (member instanceof FieldDeclaration) {
                FieldDeclaration vv = (FieldDeclaration) member;
                for (VariableDeclarator variable : vv.getVariables()) {
                    fields.add(new JPFieldDoc(vv, variable, this));
                }
            }
        }
    }

    @Override
    public JDFieldDoc[] fields() {
        return fields.toArray(new JDFieldDoc[0]);
    }

    @Override
    public JDFieldDoc[] enumConstants() {
        return enumConstants.toArray(new JDFieldDoc[0]);
    }

    @Override
    public JDConstructorDoc[] constructors() {
        return constructors.toArray(new JDConstructorDoc[0]);
    }

    @Override
    public JDMethodDoc[] methods() {
        return methods.toArray(new JDMethodDoc[0]);
    }

    @Override
    public String name() {
        return declaration.getName().asString();
    }

    @Override
    public String packageName() {
        return packageName;
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
        if (declaration.isClassOrInterfaceDeclaration()) {
            ClassOrInterfaceDeclaration cid = declaration.asClassOrInterfaceDeclaration();
            if (!cid.getTypeParameters().isEmpty()) {
                return "<" + cid.getTypeParameters().stream().map(TypeParameter::asString).collect(Collectors.joining(", ")) + ">";
            }
        }
        return "";
    }

    @Override
    public JDType superClass() {
        if (declaration.isClassOrInterfaceDeclaration()) {
            ClassOrInterfaceDeclaration cid = declaration.asClassOrInterfaceDeclaration();
            if (!cid.getExtendedTypes().isEmpty() && !cid.isInterface()) {
                return new JPType(cid.getExtendedTypes().get(0));
            }
        }
        return null;
    }

    @Override
    public JDType[] interfaces() {
        List<JDType> list = new ArrayList<>();
        if (declaration.isClassOrInterfaceDeclaration()) {
            ClassOrInterfaceDeclaration cid = declaration.asClassOrInterfaceDeclaration();
            if (cid.isInterface()) {
                for (ClassOrInterfaceType extendedType : cid.getExtendedTypes()) {
                    list.add(new JPType(extendedType));
                }
            } else {
                for (ClassOrInterfaceType implementedType : cid.getImplementedTypes()) {
                    list.add(new JPType(implementedType));
                }
            }
        } else if (declaration.isEnumDeclaration()) {
            EnumDeclaration ed = declaration.asEnumDeclaration();
            for (ClassOrInterfaceType implementedType : ed.getImplementedTypes()) {
                list.add(new JPType(implementedType));
            }
        }
        return list.toArray(new JDType[0]);
    }

    @Override
    public String qualifiedName() {
        if (packageName != null && !packageName.isEmpty()) {
            return packageName + "." + name();
        }
        return name();
    }

    @Override
    public JDDoc comments() {
        if (declaration.getComment().isPresent() && declaration.getComment().get() instanceof JavadocComment) {
            JavadocComment jc = (JavadocComment) declaration.getComment().get();
            return new JPDoc(JPDoc.parseJavadoc(jc.getContent()));
        }
        return null;
    }

    @Override
    public boolean isClass() {
        return declaration.isClassOrInterfaceDeclaration() && !declaration.asClassOrInterfaceDeclaration().isInterface();
    }

    @Override
    public boolean isInterface() {
        return declaration.isClassOrInterfaceDeclaration() && declaration.asClassOrInterfaceDeclaration().isInterface();
    }

    @Override
    public boolean isAnnotation() {
        return declaration.isAnnotationDeclaration();
    }

    @Override
    public boolean isEnum() {
        return declaration.isEnumDeclaration();
    }

    @Override
    public boolean isRecord() {
        return declaration.getClass().getSimpleName().equals("RecordDeclaration");
    }

    @Override
    public boolean isException() {
        if (!isClass()) {
            return false;
        }
        String n = name();
        if (n.endsWith("Exception") || n.endsWith("Error")) {
            return true;
        }
        JDType sup = superClass();
        if (sup != null && (sup.name().endsWith("Exception") || sup.name().endsWith("Throwable") || sup.name().endsWith("Error"))) {
            return true;
        }
        return false;
    }

    public JDDoc findInheritedMethodDoc(String methodName, String[] paramTypes) {
        if (root == null) {
            return null;
        }
        List<JDType> superTypes = new ArrayList<>();
        JDType sup = superClass();
        if (sup != null) {
            superTypes.add(sup);
        }
        superTypes.addAll(Arrays.asList(interfaces()));

        for (JDType superType : superTypes) {
            String qName = superType.qualifiedName();
            JDClassDoc parentDoc = root.findClass(qName);
            if (parentDoc == null) {
                parentDoc = root.findClass(superType.simpleName());
            }
            if (parentDoc != null) {
                for (JDMethodDoc method : parentDoc.methods()) {
                    if (method.name().equals(methodName)) {
                        JDParameter[] params = method.parameters();
                        if (paramTypes == null || params.length == paramTypes.length) {
                            JDDoc doc = method.commentText();
                            if (doc != null) {
                                return doc;
                            }
                        }
                    }
                }
                if (parentDoc instanceof JPClassDoc) {
                    JDDoc doc = ((JPClassDoc) parentDoc).findInheritedMethodDoc(methodName, paramTypes);
                    if (doc != null) {
                        return doc;
                    }
                }
            }
        }
        return null;
    }

    public JPRootDoc getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return qualifiedName();
    }
}
