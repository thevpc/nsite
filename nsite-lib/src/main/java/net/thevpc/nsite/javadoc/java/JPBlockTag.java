package net.thevpc.nsite.javadoc.java;

import net.thevpc.nsite.javadoc.JDBlockTag;

public class JPBlockTag implements JDBlockTag {
    private String tagName;
    private String name;
    private String content;

    public JPBlockTag(String tagName, String name, String content) {
        this.tagName = tagName;
        this.name = name;
        this.content = content;
    }

    @Override
    public String tagName() {
        return tagName;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String content() {
        return content;
    }

    @Override
    public String toString() {
        return "@" + tagName + (name != null ? " " + name : "") + (content != null ? " " + content : "");
    }
}
