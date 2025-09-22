package net.thevpc.nsite.html;

public class NHtmlRaw extends NHtmlNode {
    private String value;

    public NHtmlRaw(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return (value == null ? "" : value.toString());
    }
}
