package net.thevpc.nsite.javadoc.java;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescriptionElement;
import net.thevpc.nsite.javadoc.JDBlockTag;
import net.thevpc.nsite.javadoc.JDDoc;
import net.thevpc.nsite.javadoc.util.DocReader;
import net.thevpc.nuts.lib.md.MdElement;
import net.thevpc.nuts.lib.md.MdText;

import java.util.ArrayList;
import java.util.List;

public class JPDoc implements JDDoc {

    private Javadoc jd;
    private String raw;

    public static Javadoc parseJavadoc(String rawContent) {
        if (rawContent == null) {
            return null;
        }
        String sanitized = sanitizeJavadoc(rawContent);
        try {
            return StaticJavaParser.parseJavadoc(sanitized);
        } catch (Exception e) {
            try {
                return StaticJavaParser.parseJavadoc(rawContent);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    public static String sanitizeJavadoc(String raw) {
        if (raw == null || raw.isEmpty()) {
            return raw;
        }
        String[] lines = raw.split("\r?\n");
        StringBuilder sb = new StringBuilder();
        boolean inPre = false;
        boolean inCodeBlock = false;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String lower = line.toLowerCase();

            if (inPre || inCodeBlock) {
                line = protectAnnotationsInLine(line);
            }

            if (lower.contains("<pre>")) {
                inPre = true;
            }
            if (line.contains("{@code") && !line.contains("}")) {
                inCodeBlock = true;
            }
            if (line.contains("}") && inCodeBlock) {
                inCodeBlock = false;
            }
            if (lower.contains("</pre>")) {
                inPre = false;
            }

            if (i > 0) {
                sb.append("\n");
            }
            sb.append(line);
        }
        return sb.toString();
    }

    private static String protectAnnotationsInLine(String line) {
        return line.replaceAll("([\\s*])@([a-zA-Z_])", "$1&#64;$2");
    }

    public JPDoc(Javadoc jd) {
        this.jd = jd;
    }

    public JPDoc(String raw) {
        this.raw = raw;
    }

    @Override
    public String getTag(String tag) {
        if (jd != null) {
            for (JavadocBlockTag blockTag : jd.getBlockTags()) {
                if (blockTag.getTagName().equalsIgnoreCase(tag)) {
                    return blockTag.getContent().toText().trim();
                }
            }
        }
        return null;
    }

    @Override
    public List<String> getTags(String tag) {
        List<String> list = new ArrayList<>();
        if (jd != null) {
            for (JavadocBlockTag blockTag : jd.getBlockTags()) {
                if (blockTag.getTagName().equalsIgnoreCase(tag)) {
                    list.add(blockTag.getContent().toText().trim());
                }
            }
        }
        return list;
    }

    @Override
    public List<JDBlockTag> getBlockTags() {
        List<JDBlockTag> list = new ArrayList<>();
        if (jd != null) {
            for (JavadocBlockTag bt : jd.getBlockTags()) {
                String name = bt.getName().orElse(null);
                String content = bt.getContent().toText().trim();
                list.add(new JPBlockTag(bt.getTagName(), name, content));
            }
        }
        return list;
    }

    @Override
    public MdElement getDescription() {
        if (jd != null && jd.getDescription() != null) {
            DocReader dr = new DocReader();
            for (JavadocDescriptionElement element : jd.getDescription().getElements()) {
                dr.add(element);
            }
            return dr.parse();
        }
        if (raw != null && !raw.trim().isEmpty()) {
            return MdText.phrase(raw.trim());
        }
        return null;
    }

    @Override
    public String rawText() {
        if (jd != null) {
            return jd.toText();
        }
        return raw;
    }

    @Override
    public String toString() {
        return rawText();
    }
}
