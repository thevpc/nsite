package net.thevpc.nsite.javadoc;

public interface JDRootDoc {

    JDClassDoc[] classes();

    JDPackageDoc[] packages();

    JDClassDoc findClass(String qualifiedName);

    JDPackageDoc findPackage(String packageName);
}
