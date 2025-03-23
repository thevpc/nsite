package net.thevpc.nsite.deprecated;

public interface StringValidator {

    default String getHints() {
        return null;
    }

    StringValidatorType getType();

    String validate(String value);
}
