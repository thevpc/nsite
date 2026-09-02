package net.thevpc.nsite.javadoc;

public interface JDFieldDoc {

    String name();

    String qualifiedName();

    JDType type();

    String constantValueExpression();

    boolean isFinal();

    boolean isStatic();

    boolean isEnumConstant();

    String modifiers();

    String[] annotations();

    JDDoc commentText();

    JDClassDoc declaringClass();
}
