package net.thevpc.nsite.javadoc;

public interface JDType {

    boolean isPrimitive();

    boolean isArray();

    String name();

    String simpleName();

    String qualifiedName();

    String asString();
}
