package net.thevpc.nsite.html;

import net.thevpc.nuts.text.NTextPlain;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.List;

public final class NHtml {
    public static NHtmlTag tag(String input) {
        return new NHtmlTag(input);
    }
    public static NHtmlNode splitLines(String input) {
        if("\n".equals(input)){
            return  tag("br");
        }
        List<NHtmlNode> all=new ArrayList<>();
        for (String s : NStringUtils.splitLines(input)) {
            if(!all.isEmpty()){
                all.add(NHtml.tag("br"));
            }
            if(!s.isEmpty()) {
                all.add(NHtml.raw(s));
            }
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return NHtml.list(all);
    }

    public static NHtmlTagList list(NHtmlNode... nodes) {
        return new NHtmlTagList(nodes);
    }
    public static NHtmlTagList list(List<NHtmlNode> nodes) {
        return new NHtmlTagList(nodes);
    }

    public static NHtmlRaw space() {
        return raw(" ");
    }

    public static NHtmlRaw raw(String plain) {
        return new NHtmlRaw(plain);
    }

    public static NHtmlRaw newLine() {
        return raw("\n");
    }

    public static String escapeString(String input) {
            if (input == null) return "";
            StringBuilder sb = new StringBuilder(input.length());
            for (char c : input.toCharArray()) {
                switch (c) {
                    case '<' :{
                        sb.append("&lt;");
                        break;
                    }
                    case '>' :{
                        sb.append("&gt;");
                        break;
                    }
                    case '&' :{
                        sb.append("&amp;");
                        break;
                    }
                    case '"' :{
                        sb.append("&quot;");
                        break;
                    }
                    case '\'' :{
                        sb.append("&#39;"); // or &apos; but it's less supported
                        break;
                    }
                    default: {
                        sb.append(c);
                        break;
                    }
                }
            }
            return sb.toString();
    }
}
