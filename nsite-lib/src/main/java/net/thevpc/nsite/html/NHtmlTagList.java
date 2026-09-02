package net.thevpc.nsite.html;

import java.util.List;

public class NHtmlTagList extends NHtmlNode {
    NHtmlNode[] all;
    private boolean newLine = false;

    public boolean isNewLine() {
        return newLine;
    }

    public NHtmlTagList setNewLine(boolean newLine) {
        this.newLine = newLine;
        return this;
    }

    public NHtmlTagList(List<NHtmlNode> all) {
        this(all.toArray(new NHtmlNode[0]));
    }

    public NHtmlTagList(NHtmlNode... all) {
        this.all = all;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (all != null) {
            for (NHtmlNode node : all) {
                if (node != null) {
                    sb.append(node);
                    if (newLine) {
                        sb.append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }
}
