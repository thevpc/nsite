package net.thevpc.nsite.javadoc.java;

import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedType;
import net.thevpc.nsite.javadoc.JDType;

public class JPType implements JDType {

    private Type type;
    private String typeString;
    private ResolvedType resolvedType;

    public JPType(Type type) {
        this.type = type;
        if (type != null) {
            try {
                this.resolvedType = type.resolve();
            } catch (Throwable ex) {
                // Ignore resolution failure - fallback to AST name
            }
        }
    }

    public JPType(String typeString) {
        this.typeString = typeString;
    }

    @Override
    public boolean isPrimitive() {
        if (type != null) {
            return type.isPrimitiveType();
        }
        if (typeString != null) {
            switch (typeString) {
                case "boolean":
                case "byte":
                case "short":
                case "int":
                case "long":
                case "float":
                case "double":
                case "char":
                case "void":
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean isArray() {
        if (type != null) {
            return type.isArrayType();
        }
        return typeString != null && typeString.endsWith("[]");
    }

    @Override
    public String name() {
        return asString();
    }

    @Override
    public String simpleName() {
        String s = asString();
        int idx = s.indexOf('<');
        String base = idx > 0 ? s.substring(0, idx) : s;
        int dot = base.lastIndexOf('.');
        String simple = dot >= 0 ? base.substring(dot + 1) : base;
        if (idx > 0) {
            return simple + s.substring(idx);
        }
        return simple;
    }

    @Override
    public String qualifiedName() {
        if (resolvedType != null) {
            try {
                return resolvedType.describe();
            } catch (Throwable ex) {
                // ignore
            }
        }
        return asString();
    }

    @Override
    public String asString() {
        if (type != null) {
            return type.asString();
        }
        return typeString != null ? typeString : "";
    }

    @Override
    public String toString() {
        return asString();
    }
}
