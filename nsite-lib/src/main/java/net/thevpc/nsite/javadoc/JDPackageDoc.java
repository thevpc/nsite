package net.thevpc.nsite.javadoc;

public interface JDPackageDoc {
    String name();

    JDDoc description();

    JDClassDoc[] classes();

    JDClassDoc[] interfaces();

    JDClassDoc[] enums();

    JDClassDoc[] exceptions();

    JDClassDoc[] annotations();

    JDClassDoc[] allTypes();
}
