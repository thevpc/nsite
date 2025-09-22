package net.thevpc.nsite.html;

import java.util.ArrayList;
import java.util.List;

public class NHtmlTag extends NHtmlNode {
    String name;
    List<NHtmlAttr> attrs = new ArrayList<>();
    List<NHtmlNode> body = new ArrayList<>();
    boolean noEnd;

    public NHtmlTag(String name) {
        this.name = name;
    }

    public boolean isNoEnd() {
        return noEnd;
    }

    public NHtmlTag setNoEnd(boolean noEnd) {
        this.noEnd = noEnd;
        return this;
    }

    public NHtmlTag attr(String name, String value) {
        attrs.add(new NHtmlAttr(name, value));
        return this;
    }

    public NHtmlTag body(NHtmlNode value) {
        body.add(value);
        return this;
    }

    public NHtmlTag body(String value) {
        body.add(new NHtmlRaw(value));
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(name);
        for (NHtmlAttr attr : attrs) {
            sb.append(" ").append(attr.name).append("=\"").append(attr.value).append("\"");
        }
        if (body.size() == 0 && noEnd) {
            sb.append("/>");
        } else {
            sb.append(">");
            for (NHtmlNode b : body) {
                sb.append(b);
            }
            sb.append("</").append(name).append(">");
        }
        return sb.toString();
    }
}
