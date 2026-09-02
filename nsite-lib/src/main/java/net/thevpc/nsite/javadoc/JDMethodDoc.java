package net.thevpc.nsite.javadoc;

public interface JDMethodDoc extends JDExecutableMemberDoc {

    boolean isStatic();

    boolean isAbstract();

    boolean isDefault();

    JDType returnType();
}
