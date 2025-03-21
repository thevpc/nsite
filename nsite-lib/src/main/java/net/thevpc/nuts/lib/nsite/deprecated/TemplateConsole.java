package net.thevpc.nuts.lib.nsite.deprecated;

public interface TemplateConsole {

    void println(String message, Object... params);

    String ask(String propName, String propertyTitle, StringValidator validator, String defaultValue);
}
