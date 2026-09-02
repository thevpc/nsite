package net.thevpc.nsite.javadoc;

public interface JDClassDoc {

    JDFieldDoc[] fields();

    JDConstructorDoc[] constructors();

    JDMethodDoc[] methods();

    JDFieldDoc[] enumConstants();

    String name();

    String packageName();

    String qualifiedName();

    String modifiers();

    String[] annotations();

    String typeParameters();

    JDType superClass();

    JDType[] interfaces();

    JDDoc comments();

    boolean isClass();

    boolean isInterface();

    boolean isAnnotation();

    boolean isEnum();

    boolean isRecord();

    boolean isException();
}
