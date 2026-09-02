package net.thevpc.nsite.javadoc;

public interface JDParameter {

    String getJavadocContent();

    JDType type();

    String name();

    String[] annotations();
}
