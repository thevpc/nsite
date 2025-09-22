package net.thevpc.nsite.html;

public class NHtmlBuffer {
    private StringBuilder buffer = new StringBuilder();

    public NHtmlBuffer tagPlain(String tag, String value) {
        buffer.append("<").append(tag).append(">")
                .append(value).append("</").append(tag).append(">");
        return this;
    }

    public NHtmlBuffer newLine() {
        buffer.append("\n");
        return this;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

}
