package net.thevpc.nsite.javadoc;

public interface JDExecutableMemberDoc {

    JDParameter[] parameters();

    JDType[] thrownExceptions();

    JDDoc commentText();

    String qualifiedName();

    String name();

    String modifiers();

    String[] annotations();

    String typeParameters();

    JDClassDoc declaringClass();
}
