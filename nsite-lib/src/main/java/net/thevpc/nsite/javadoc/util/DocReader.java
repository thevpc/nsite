/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nsite.javadoc.util;

import com.github.javaparser.javadoc.description.JavadocDescriptionElement;
import com.github.javaparser.javadoc.description.JavadocInlineTag;
import com.github.javaparser.javadoc.description.JavadocSnippet;
import net.thevpc.nuts.lib.md.*;

import java.util.*;

/**
 * @author thevpc
 */
public class DocReader {
    private static Set<String> unclosableTags=new HashSet<>(Arrays.asList("p","img","br","hr"));

    private List<Object> all = new ArrayList<>();

    public DocReader add(JavadocDescriptionElement e) {
        if (e instanceof JavadocSnippet) {
            all.add(new CharReader(e.toText()));
        } else if (e instanceof JavadocInlineTag) {
            JavadocInlineTag ee = (JavadocInlineTag) e;
            switch (ee.getType()) {
                case CODE: {
                    String c = ee.getContent().trim();
                    if (c.contains("\n")) {
                        all.add(MdFactory.codeBacktick3("java", cleanCodeText(c), false));
                    } else {
                        all.add(MdFactory.codeBacktick1("", c.replace("&#64;", "@")));
                    }
                    break;
                }
                case LINK:
                case LINKPLAIN: {
                    String content = ee.getContent().trim();
                    String label = content;
                    String target = content;
                    int spaceIdx = content.indexOf(' ');
                    if (spaceIdx > 0) {
                        target = content.substring(0, spaceIdx).trim();
                        label = content.substring(spaceIdx + 1).trim();
                    }
                    if (isJdkOrExternal(target)) {
                        all.add(MdFactory.codeBacktick1("", label));
                    } else if (target.startsWith("#")) {
                        String mName = target.substring(1);
                        if (mName.contains("(")) {
                            mName = mName.substring(0, mName.indexOf('('));
                        }
                        all.add(new net.thevpc.nuts.lib.md.MdLink("", label, "#m-" + mName));
                    } else if (target.contains("#")) {
                        String[] parts = target.split("#", 2);
                        all.add(new net.thevpc.nuts.lib.md.MdLink("", label, "#cls-" + parts[0].replace('.', '-')));
                    } else {
                        all.add(new net.thevpc.nuts.lib.md.MdLink("", label, "#cls-" + target.replace('.', '-')));
                    }
                    break;
                }
                case LITERAL: {
                    all.add(MdFactory.codeBacktick1("", ee.getContent().trim()));
                    break;
                }
                case VALUE: {
                    all.add(MdFactory.codeBacktick1("", ee.getContent().trim()));
                    break;
                }
                case SYSTEM_PROPERTY: {
                    all.add(MdFactory.codeBacktick1("", ee.getContent().trim()));
                    break;
                }
                case DOC_ROOT: {
                    //ignore
                    break;
                }
                default: {
                    all.add(MdFactory.text(ee.getContent()));
                    break;
                }
            }
        }
        return this;
    }

    public boolean isEmpty() {
        if (all.isEmpty()) {
            return true;
        }
//        Object a = all.get(0);
//        if (a instanceof CharReader) {
//            return ((CharReader) a).isEmpty();
//        }
        return false;
    }

    public MdElement parse() {
        List<MdElement> result = new ArrayList<MdElement>();
        while (!isEmpty()) {
            MdElement a = readAny(null);
            if (a == null) {
                if (!isEmpty()) {
                    if (isCurrTag()) {
                        this.all.remove(0);
                    } else {
                        CharReader in = currText();
                        if (in.isEmpty()) {
                            this.all.remove(0);
                        } else {
                            in.read();
                        }
                    }
                } else {
                    break;
                }
            } else {
                result.add(a);
            }
        }
        if (result.size() == 0) {
            return MdText.empty();
        }
        if (result.size() == 1) {
            return result.get(0);
        }
        return MdFactory.ofListOrEmpty( result.toArray(new MdElement[0]));
    }

    private MdElement readAny(String expectedClosingTag) {
        if (isEmpty()) {
            return null;
        }
        List<MdElement> result = new ArrayList<>();
        while (!isEmpty()) {
            if (isCurrTag()) {
                result.add(currTag());
                this.all.remove(0);
            } else {
                CharReader in = currText();
                if (in.isEmpty()) {
                    this.all.remove(0);
                } else {
                    if (in.peek("</")) {
                        if (expectedClosingTag != null && in.peek("</" + expectedClosingTag + ">")) {
                            break;
                        }
                        // consume closing tag
                        while (!in.isEmpty() && in.peek() != '>') {
                            in.read();
                        }
                        if (!in.isEmpty() && in.peek('>')) {
                            in.read();
                        }
                        if (expectedClosingTag != null) {
                            break;
                        }
                    } else if (in.peek("<")) {
                        String[] na = readHtmlTagStart();
                        if (na[1].endsWith("/>") || isNoClosingTag(na[0])) {
                            result.add(prepareXml(new MdXml(MdXml.XmlTagType.OPEN, na[0], (Map)null, null)));
                        } else {
                            MdElement content = readAny(na[0]);
                            if (isCurrText() && currText().peek("</" + na[0] + ">")) {
                                currText().read("</" + na[0] + ">");
                            }
                            result.add(prepareXml(new MdXml(MdXml.XmlTagType.OPEN, na[0], (Map)null, content)));
                        }
                    } else {
                        result.add(readHtmlText());
                    }
                }
            }
        }
        if (result.isEmpty()) {
            return null;
        }
        if (result.size() == 1) {
            return result.get(0);
        }
        return MdFactory.ofListOrEmpty( result.toArray(new MdElement[0]));
    }

    protected MdElement prepareXml(MdXml xml) {
        switch (xml.getTag()) {
            case "p": {
                List<MdElement> items = new ArrayList<>();
                items.add(new MdText("\n\n", false));
                if (xml.getContent() != null && !MdFactory.isBlank(xml.getContent())) {
                    items.add(xml.getContent());
                    items.add(new MdText("\n\n", false));
                }
                return MdFactory.seq(items);
            }
            case "strong":
            case "b": {
                return new MdBold(xml.getContent());
            }
            case "i":
            case "em": {
                return new MdItalic(xml.getContent());
            }
            case "pre": {
                return MdFactory.codeBacktick3("java", cleanCodeText(extractCodeText(xml.getContent())), false);
            }
            case "code":
            case "tt": {
                return MdFactory.codeBacktick1("", extractCodeText(xml.getContent()));
            }
            case "br": {
                return new MdBr();
            }
            case "hr": {
                return new MdHr();
            }
            case "ul": {
                List<MdElement> items = new ArrayList<>();
                items.add(new MdText("\n\n", false));
                for (MdElement a : MdFactory.toArray(xml.getContent())) {
                    if (MdFactory.isBlank(a)) {
                        //ignore
                    } else if (MdFactory.isXmlTag(a, "li")) {
                        MdElement liContent = trim(((MdXml) a).getContent());
                        if (liContent != null && !MdFactory.isBlank(liContent)) {
                            items.add(new MdText("- ", false));
                            items.add(liContent);
                            items.add(new MdText("\n", false));
                        }
                    } else {
                        items.add(a);
                    }
                }
                items.add(new MdText("\n", false));
                return MdFactory.seq(items);
            }
            case "ol": {
                List<MdElement> items = new ArrayList<>();
                items.add(new MdText("\n\n", false));
                int num = 1;
                for (MdElement a : MdFactory.toArray(xml.getContent())) {
                    if (MdFactory.isBlank(a)) {
                        //ignore
                    } else if (MdFactory.isXmlTag(a, "li")) {
                        MdElement liContent = trim(((MdXml) a).getContent());
                        if (liContent != null && !MdFactory.isBlank(liContent)) {
                            items.add(new MdText((num++) + ". ", false));
                            items.add(liContent);
                            items.add(new MdText("\n", false));
                        }
                    } else {
                        items.add(a);
                    }
                }
                items.add(new MdText("\n", false));
                return MdFactory.seq(items);
            }
            case "li": {
                MdElement liContent = trim(xml.getContent());
                if (liContent != null && !MdFactory.isBlank(liContent)) {
                    return MdFactory.seq(
                            new MdText("- ", false),
                            liContent,
                            new MdText("\n", false)
                    );
                }
                return MdText.empty();
            }
            default: {
                return xml;
            }
        }

    }

    private boolean isNoClosingTag(String s) {
        return s.equals("br") || s.equals("hr") || s.equals("img") || s.equals("wbr");
    }

    private MdElement readHtmlText() {
        CharReader in = currText();
        StringBuilder s = new StringBuilder();
        while (!in.isEmpty() && in.peek() != '<') {
            s.append(in.read());
        }
        return MdText.phrase(s.toString());
    }

    private String[] readHtmlTagStart() {
        StringBuilder sb = new StringBuilder();
        CharReader in = currText();
        char e = in.read();
        boolean acceptName = true;
        sb.append(e);
        StringBuilder n = new StringBuilder();
        while (!in.isEmpty()) {
            e = in.read();
            sb.append(e);
            if (e == '>') {
                return new String[]{n.toString(), sb.toString()};
            } else if (e == ' ') {
                acceptName = false;
            } else {
                if (acceptName) {
                    n.append(e);
                }
            }
        }
        return new String[]{n.toString(), sb.toString()};
    }

    public boolean isCurrTag() {
        return all.size() > 0 && all.get(0) instanceof MdElement;
    }

    public boolean isCurrText() {
        return all.size() > 0 && all.get(0) instanceof CharReader;
    }

    public CharReader currText() {
        return (CharReader) this.all.get(0);
    }

    public MdElement currTag() {
        return (MdElement) this.all.get(0);
    }

    private String extractCodeText(MdElement e) {
        if (e == null) {
            return "";
        }
        if (e instanceof MdCode) {
            return ((MdCode) e).getValue();
        }
        if (e instanceof MdParent) {
            StringBuilder sb = new StringBuilder();
            for (MdElement child : ((MdParent) e).getChildren()) {
                sb.append(extractCodeText(child));
            }
            return sb.toString();
        }
        String s = e.toString();
        if (s.startsWith("`") && s.endsWith("`") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("&#64;", "@");
    }

    public static String cleanCodeText(String code) {
        if (code == null) {
            return "";
        }
        String[] lines = code.split("\r?\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            line = line.replaceFirst("^[\\t ]*\\*[\\t ]?", "");
            if (i > 0) {
                sb.append("\n");
            }
            sb.append(line);
        }
        return sb.toString().replace("&#64;", "@").trim();
    }

    private MdElement trimLeading(MdElement e) {
        if (e == null) {
            return null;
        }
        if (e.isText()) {
            String t = e.asText().getText();
            String trimmed = t.replaceAll("^[\\r\\n\\t ]+", "");
            if (trimmed.isEmpty()) {
                return null;
            }
            return new MdText(trimmed, e.asText().isInline());
        }
        if (e instanceof MdParent) {
            MdElement[] children = ((MdParent) e).getChildren();
            List<MdElement> newChildren = new ArrayList<>();
            boolean foundFirst = false;
            for (MdElement child : children) {
                if (!foundFirst) {
                    MdElement trimmed = trimLeading(child);
                    if (trimmed != null) {
                        newChildren.add(trimmed);
                        foundFirst = true;
                    }
                } else {
                    newChildren.add(child);
                }
            }
            return MdFactory.seq(newChildren);
        }
        return e;
    }

    private MdElement trimTrailing(MdElement e) {
        if (e == null) {
            return null;
        }
        if (e.isText()) {
            String t = e.asText().getText();
            String trimmed = t.replaceAll("[\\r\\n\\t ]+$", "");
            if (trimmed.isEmpty()) {
                return null;
            }
            return new MdText(trimmed, e.asText().isInline());
        }
        if (e instanceof MdParent) {
            MdElement[] children = ((MdParent) e).getChildren();
            List<MdElement> newChildren = new ArrayList<>(Arrays.asList(children));
            while (!newChildren.isEmpty()) {
                int lastIdx = newChildren.size() - 1;
                MdElement trimmed = trimTrailing(newChildren.get(lastIdx));
                if (trimmed == null) {
                    newChildren.remove(lastIdx);
                } else {
                    newChildren.set(lastIdx, trimmed);
                    break;
                }
            }
            return MdFactory.seq(newChildren);
        }
        return e;
    }

    private MdElement trim(MdElement e) {
        return trimTrailing(trimLeading(e));
    }

    private static boolean isJdkOrExternal(String target) {
        if (target == null || target.isEmpty()) {
            return false;
        }
        if (target.startsWith("java.") || target.startsWith("javax.") || target.startsWith("org.w3c.") || target.startsWith("org.xml.")) {
            return true;
        }
        String clean = target;
        if (clean.contains("#")) {
            clean = clean.substring(0, clean.indexOf('#'));
        }
        switch (clean) {
            case "System":
            case "String":
            case "Object":
            case "Class":
            case "Boolean":
            case "Integer":
            case "Long":
            case "Double":
            case "Float":
            case "Number":
            case "Throwable":
            case "Exception":
            case "RuntimeException":
            case "Error":
            case "Thread":
            case "Runnable":
            case "Callable":
            case "Iterable":
            case "Iterator":
            case "Collection":
            case "List":
            case "Set":
            case "Map":
            case "Queue":
            case "Deque":
            case "Comparator":
            case "Comparable":
            case "AutoCloseable":
            case "Closeable":
            case "Serializable":
            case "File":
            case "Path":
            case "InputStream":
            case "OutputStream":
            case "Reader":
            case "Writer":
            case "URL":
            case "URI":
            case "Optional":
            case "Predicate":
            case "Function":
            case "Consumer":
            case "Supplier":
            case "BiFunction":
            case "BiConsumer":
            case "BiPredicate":
            case "Stream":
                return true;
            default:
                return false;
        }
    }
}
