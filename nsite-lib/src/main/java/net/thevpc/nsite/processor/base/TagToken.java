package net.thevpc.nsite.processor.base;

public class TagToken {
    TagTokenType type;
    String value;

    public TagToken(TagTokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return type+"{" +
                value+
                '}';
    }
}
