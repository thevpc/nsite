package net.thevpc.nsite.javadoc;

import net.thevpc.nsite.javadoc.java.JPRootDoc;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.md.MdElement;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NSiteJavadoc {

    public static void generate(NSiteJavadocConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config is null");
        }
        if (config.getTargetDir() == null || config.getTargetDir().trim().isEmpty()) {
            throw new IllegalArgumentException("targetDir is required");
        }
        if (config.getSourceRoots().length == 0) {
            throw new IllegalArgumentException("at least one sourceRoot is required");
        }

        NPath targetDir = NPath.of(config.getTargetDir());
        targetDir.mkdirs();

        List<NPath> srcPaths = Arrays.stream(config.getSourceRoots())
                .map(NPath::of)
                .collect(Collectors.toList());

        JPRootDoc rootDoc = new JPRootDoc();
        rootDoc.initParser(srcPaths);

        String[] packages = config.getPackages();
        Predicate<String> packageFilter = packages.length == 0 ? (x -> true) : x -> {
            for (String p : packages) {
                if (p.equals("*") || p.equals("**")) {
                    return true;
                }
                if (p.endsWith(".*")) {
                    String sp = p.substring(0, p.length() - 2);
                    return x.equals(sp) || x.startsWith(sp + ".");
                }
                if (p.endsWith(".**")) {
                    String sp = p.substring(0, p.length() - 3);
                    return x.equals(sp) || x.startsWith(sp + ".");
                }
                if (x.equals(p)) {
                    return true;
                }
            }
            return false;
        };

        for (NPath srcPath : srcPaths) {
            rootDoc.parseSrcFolder(srcPath, packageFilter);
        }

        // Write .folder-info.md
        NPath folderInfo = targetDir.resolve(".folder-info.md");
        folderInfo.writeString("---\ntitle: " + escapeYaml(config.getTitle()) + "\n---\n" + (config.getDescription() != null ? config.getDescription() : "") + "\n");

        // Write 000-overview.md
        writeOverview(targetDir, rootDoc, config);

        // Write each package
        JDPackageDoc[] packageDocs = rootDoc.packages();
        int order = 10;
        for (JDPackageDoc pkg : packageDocs) {
            if (pkg.allTypes().length == 0 && pkg.description() == null) {
                continue;
            }
            writePackage(targetDir, rootDoc, pkg, order);
            order += 10;
        }
    }

    private static void writeOverview(NPath targetDir, JPRootDoc rootDoc, NSiteJavadocConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append("id: api-overview\n");
        sb.append("title: Overview\n");
        sb.append("order: 0\n");
        sb.append("---\n\n");

        sb.append("# ").append(config.getTitle()).append("\n\n");
        if (config.getDescription() != null && !config.getDescription().isEmpty()) {
            sb.append(config.getDescription()).append("\n\n");
        }

        sb.append("## Packages Summary\n\n");
        sb.append("| Package | Description |\n");
        sb.append("| :--- | :--- |\n");

        for (JDPackageDoc pkg : rootDoc.packages()) {
            if (pkg.allTypes().length == 0 && pkg.description() == null) {
                continue;
            }
            String pkgAnchor = "pkg-" + pkg.name().replace('.', '-');
            String desc = getFirstSentence(pkg.description());
            if (desc == null || desc.isEmpty()) {
                desc = "Types and interfaces for `" + pkg.name() + "`";
            }
            sb.append("| [").append(pkg.name()).append("](#").append(pkgAnchor).append(") | ");
            sb.append(escapeTableCell(desc)).append(" |\n");
        }
        sb.append("\n");

        targetDir.resolve("000-overview.md").writeString(sb.toString());
    }

    private static void writePackage(NPath targetDir, JPRootDoc rootDoc, JDPackageDoc pkg, int order) {
        String fileName = String.format("%03d-%s.md", order, pkg.name());
        String pkgAnchor = "pkg-" + pkg.name().replace('.', '-');

        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append("id: ").append(pkgAnchor).append("\n");
        sb.append("title: ").append(pkg.name()).append("\n");
        sb.append("order: ").append(order).append("\n");
        sb.append("---\n\n");

        sb.append("# Package ").append(pkg.name()).append("\n\n");
        sb.append("<a id=\"").append(pkgAnchor).append("\"></a>\n\n");

        if (pkg.description() != null) {
            String desc = renderDocDescription(pkg.description());
            if (!desc.isEmpty()) {
                sb.append(desc).append("\n\n");
            }
        }

        // Summary tables
        if (pkg.interfaces().length > 0) {
            sb.append("## Interfaces Summary\n\n");
            sb.append("| Interface | Description |\n");
            sb.append("| :--- | :--- |\n");
            for (JDClassDoc cls : pkg.interfaces()) {
                String clsAnchor = "cls-" + cls.qualifiedName().replace('.', '-');
                String desc = getFirstSentence(cls.comments());
                sb.append("| [").append(cls.name()).append("](#").append(clsAnchor).append(") | ");
                sb.append(escapeTableCell(desc)).append(" |\n");
            }
            sb.append("\n");
        }

        if (pkg.classes().length > 0) {
            sb.append("## Classes Summary\n\n");
            sb.append("| Class | Description |\n");
            sb.append("| :--- | :--- |\n");
            for (JDClassDoc cls : pkg.classes()) {
                String clsAnchor = "cls-" + cls.qualifiedName().replace('.', '-');
                String desc = getFirstSentence(cls.comments());
                sb.append("| [").append(cls.name()).append("](#").append(clsAnchor).append(") | ");
                sb.append(escapeTableCell(desc)).append(" |\n");
            }
            sb.append("\n");
        }

        if (pkg.enums().length > 0) {
            sb.append("## Enums Summary\n\n");
            sb.append("| Enum | Description |\n");
            sb.append("| :--- | :--- |\n");
            for (JDClassDoc cls : pkg.enums()) {
                String clsAnchor = "cls-" + cls.qualifiedName().replace('.', '-');
                String desc = getFirstSentence(cls.comments());
                sb.append("| [").append(cls.name()).append("](#").append(clsAnchor).append(") | ");
                sb.append(escapeTableCell(desc)).append(" |\n");
            }
            sb.append("\n");
        }

        if (pkg.exceptions().length > 0) {
            sb.append("## Exceptions Summary\n\n");
            sb.append("| Exception | Description |\n");
            sb.append("| :--- | :--- |\n");
            for (JDClassDoc cls : pkg.exceptions()) {
                String clsAnchor = "cls-" + cls.qualifiedName().replace('.', '-');
                String desc = getFirstSentence(cls.comments());
                sb.append("| [").append(cls.name()).append("](#").append(clsAnchor).append(") | ");
                sb.append(escapeTableCell(desc)).append(" |\n");
            }
            sb.append("\n");
        }

        if (pkg.annotations().length > 0) {
            sb.append("## Annotation Types Summary\n\n");
            sb.append("| Annotation Type | Description |\n");
            sb.append("| :--- | :--- |\n");
            for (JDClassDoc cls : pkg.annotations()) {
                String clsAnchor = "cls-" + cls.qualifiedName().replace('.', '-');
                String desc = getFirstSentence(cls.comments());
                sb.append("| [").append(cls.name()).append("](#").append(clsAnchor).append(") | ");
                sb.append(escapeTableCell(desc)).append(" |\n");
            }
            sb.append("\n");
        }

        // Detailed documentation for each type
        for (JDClassDoc cls : pkg.allTypes()) {
            writeClassDetails(sb, rootDoc, cls);
        }

        targetDir.resolve(fileName).writeString(sb.toString());
    }

    private static void writeClassDetails(StringBuilder sb, JPRootDoc rootDoc, JDClassDoc cls) {
        String clsAnchor = "cls-" + cls.qualifiedName().replace('.', '-');

        sb.append("\n---\n\n");
        sb.append("<a id=\"").append(clsAnchor).append("\"></a>\n\n");
        sb.append("## ").append(cls.name()).append("\n\n");

        // Code signature
        sb.append("```java\n");
        if (cls.annotations() != null && cls.annotations().length > 0) {
            for (String ann : cls.annotations()) {
                sb.append(ann).append("\n");
            }
        }
        String kind = cls.isInterface() ? "interface"
                : cls.isAnnotation() ? "@interface"
                : cls.isEnum() ? "enum"
                : cls.isRecord() ? "record"
                : "class";

        String mods = cls.modifiers();
        if (!mods.isEmpty()) {
            sb.append(mods).append(" ");
        }
        sb.append(kind).append(" ").append(cls.name()).append(cls.typeParameters());

        if (cls.superClass() != null && !cls.isInterface() && !cls.isEnum()) {
            sb.append(" extends ").append(cls.superClass().asString());
        }
        if (cls.interfaces().length > 0) {
            String ifClause = cls.isInterface() ? " extends " : " implements ";
            sb.append(ifClause).append(Arrays.stream(cls.interfaces()).map(JDType::asString).collect(Collectors.joining(", ")));
        }
        sb.append("\n```\n\n");

        // Annotations notes/badges
        renderAnnotationNotes(sb, cls.annotations());

        // Class Description
        if (cls.comments() != null) {
            String desc = renderDocDescription(cls.comments());
            if (!desc.isEmpty()) {
                sb.append(desc).append("\n\n");
            }
            // Block tags
            writeBlockTags(sb, cls.comments());
        }

        // Enum constants
        if (cls.isEnum() && cls.enumConstants().length > 0) {
            sb.append("### Enum Constants\n\n");
            sb.append("| Constant | Description |\n");
            sb.append("| :--- | :--- |\n");
            for (JDFieldDoc f : cls.enumConstants()) {
                String desc = getFirstSentence(f.commentText());
                sb.append("| `").append(f.name()).append("` | ");
                sb.append(escapeTableCell(desc)).append(" |\n");
            }
            sb.append("\n");
        }

        // Fields / Constants
        if (cls.fields().length > 0) {
            sb.append("### Fields\n\n");
            sb.append("| Modifier and Type | Field | Description |\n");
            sb.append("| :--- | :--- | :--- |\n");
            for (JDFieldDoc f : cls.fields()) {
                String desc = getFirstSentence(f.commentText());
                String fMods = formatSummaryModifiers(f.modifiers(), cls.isInterface());
                String modAndType = fMods.isEmpty() ? escapeXml(f.type().asString()) : fMods + " " + escapeXml(f.type().asString());
                sb.append("| `").append(modAndType).append("` | ");
                sb.append("`").append(f.name()).append("` | ");
                sb.append(escapeTableCell(desc)).append(" |\n");
            }
            sb.append("\n");
        }

        // Constructors
        if (cls.constructors().length > 0) {
            sb.append("### Constructors\n\n");
            for (JDConstructorDoc ctor : cls.constructors()) {
                sb.append("#### `").append(ctor.name()).append("(");
                sb.append(Arrays.stream(ctor.parameters()).map(p -> p.type().simpleName() + " " + p.name()).collect(Collectors.joining(", ")));
                sb.append(")`\n\n");

                sb.append("```java\n");
                if (ctor.annotations() != null && ctor.annotations().length > 0) {
                    for (String ann : ctor.annotations()) {
                        sb.append(ann).append("\n");
                    }
                }
                String cMods = ctor.modifiers().replaceAll("\\s+", " ").trim();
                sb.append(cMods).append(cMods.isEmpty() ? "" : " ").append(ctor.name()).append("(");
                sb.append(Arrays.stream(ctor.parameters()).map(p -> p.type().asString() + " " + p.name()).collect(Collectors.joining(", ")));
                sb.append(")");
                if (ctor.thrownExceptions().length > 0) {
                    sb.append(" throws ").append(Arrays.stream(ctor.thrownExceptions()).map(JDType::asString).collect(Collectors.joining(", ")));
                }
                sb.append("\n```\n\n");

                renderAnnotationNotes(sb, ctor.annotations());

                if (ctor.commentText() != null) {
                    String desc = renderDocDescription(ctor.commentText()).trim();
                    if (!desc.isEmpty()) {
                        sb.append(desc).append("\n\n");
                    }
                    writeParamAndReturnTags(sb, ctor.commentText(), ctor.parameters());
                }
                sb.append("\n");
            }
        }

        // Method Summary
        if (cls.methods().length > 0) {
            sb.append("### Method Summary\n\n");
            sb.append("| Modifier and Type | Method | Description |\n");
            sb.append("| :--- | :--- | :--- |\n");
            for (JDMethodDoc method : cls.methods()) {
                String mAnchor = "m-" + cls.name() + "-" + method.name();
                String desc = getFirstSentence(method.commentText());
                String retType = method.returnType() != null ? method.returnType().asString() : "void";
                String methodMods = formatSummaryModifiers(method.modifiers(), cls.isInterface());
                String modAndRet = methodMods.isEmpty() ? escapeXml(retType) : methodMods + " " + escapeXml(retType);

                sb.append("| `").append(modAndRet).append("` | ");
                sb.append("[").append(method.name()).append("(");
                sb.append(Arrays.stream(method.parameters()).map(p -> escapeXml(p.type().simpleName())).collect(Collectors.joining(", ")));
                sb.append(")](").append("#").append(mAnchor).append(") | ");
                sb.append(escapeTableCell(desc)).append(" |\n");
            }
            sb.append("\n");

            // Method Details
            sb.append("### Method Details\n\n");
            for (JDMethodDoc method : cls.methods()) {
                String mAnchor = "m-" + cls.name() + "-" + method.name();
                sb.append("\n---\n\n");
                sb.append("<a id=\"").append(mAnchor).append("\"></a>\n\n");
                sb.append("#### ").append(method.name()).append("\n\n");

                sb.append("```java\n");
                if (method.annotations() != null && method.annotations().length > 0) {
                    for (String ann : method.annotations()) {
                        sb.append(ann).append("\n");
                    }
                }
                String mMods = method.modifiers().replaceAll("\\s+", " ").trim();
                if (!mMods.isEmpty()) {
                    sb.append(mMods).append(" ");
                }
                if (!method.typeParameters().isEmpty()) {
                    sb.append(method.typeParameters()).append(" ");
                }
                sb.append(method.returnType() != null ? method.returnType().asString() : "void").append(" ");
                sb.append(method.name()).append("(");
                sb.append(Arrays.stream(method.parameters()).map(p -> p.type().asString() + " " + p.name()).collect(Collectors.joining(", ")));
                sb.append(")");
                if (method.thrownExceptions().length > 0) {
                    sb.append(" throws ").append(Arrays.stream(method.thrownExceptions()).map(JDType::asString).collect(Collectors.joining(", ")));
                }
                sb.append("\n```\n\n");

                renderAnnotationNotes(sb, method.annotations());

                if (method.commentText() != null) {
                    String desc = renderDocDescription(method.commentText()).trim();
                    if (!desc.isEmpty()) {
                        sb.append(desc).append("\n\n");
                    }
                    writeParamAndReturnTags(sb, method.commentText(), method.parameters());
                }
                sb.append("\n");
            }
        }
    }

    private static void renderAnnotationNotes(StringBuilder sb, String[] annotations) {
        if (annotations == null || annotations.length == 0) {
            return;
        }
        for (String ann : annotations) {
            String trimmed = ann.trim();
            if (trimmed.startsWith("@")) {
                trimmed = trimmed.substring(1).trim();
            }
            if (trimmed.startsWith("NRenamed")) {
                String val = extractAnnotationAttr(trimmed, "value");
                String since = extractAnnotationAttr(trimmed, "since");
                sb.append("> [!NOTE]\n> **Renamed:** previously `").append(val).append("`");
                if (!since.isEmpty()) {
                    sb.append(" (since ").append(since).append(")");
                }
                sb.append("\n\n");
            } else if (trimmed.startsWith("NSince")) {
                String val = extractAnnotationAttr(trimmed, "value");
                sb.append("**Since:** ").append(val).append("\n\n");
            } else if (trimmed.startsWith("NImmutable")) {
                sb.append("**Immutable:** Instances of this type are immutable and thread-safe.\n\n");
            } else if (trimmed.startsWith("NJdkExtension")) {
                String val = extractAnnotationAttr(trimmed, "value");
                sb.append("**JDK Extension:** ").append(val.isEmpty() ? "Extends standard JDK capabilities." : val).append("\n\n");
            } else if (trimmed.startsWith("NUseDefault")) {
                sb.append("> [!NOTE]\n> **Default Implementation:** Delegates to default runtime implementation when not overridden.\n\n");
            } else if (trimmed.startsWith("NInclude")) {
                sb.append("**Property:** Explicitly included in Nuts reflection and serialization.\n\n");
            } else if (trimmed.startsWith("NExclude")) {
                sb.append("**Property:** Excluded from Nuts reflection and serialization.\n\n");
            } else if (trimmed.startsWith("NUnused")) {
                sb.append("**Note:** Intentionally unused in current implementation; preserved for interface compatibility.\n\n");
            } else if (trimmed.startsWith("NGetter")) {
                sb.append("**Accessor:** Property getter.\n\n");
            } else if (trimmed.startsWith("NSetter")) {
                sb.append("**Mutator:** Property setter.\n\n");
            }
        }
    }

    private static String extractAnnotationAttr(String ann, String attr) {
        int op = ann.indexOf('(');
        int cl = ann.lastIndexOf(')');
        if (op < 0 || cl <= op) {
            return "";
        }
        String body = ann.substring(op + 1, cl).trim();
        if (!body.contains("=") && (attr.equals("value") || attr.isEmpty())) {
            return body.replace("\"", "").trim();
        }
        String[] pairs = body.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].trim().equals(attr)) {
                return kv[1].replace("\"", "").trim();
            }
        }
        return "";
    }

    private static void writeParamAndReturnTags(StringBuilder sb, JDDoc doc, JDParameter[] parameters) {
        if (doc == null && (parameters == null || parameters.length == 0)) {
            return;
        }
        List<JDBlockTag> blockTags = doc != null ? doc.getBlockTags() : Collections.emptyList();

        // Parameters
        List<JDBlockTag> paramTags = blockTags.stream().filter(t -> t.tagName().equalsIgnoreCase("param")).collect(Collectors.toList());
        if (!paramTags.isEmpty() || (parameters != null && parameters.length > 0)) {
            sb.append("**Parameters:**\n\n");
            if (parameters != null && parameters.length > 0) {
                for (JDParameter param : parameters) {
                    String pDoc = param.getJavadocContent();
                    if (pDoc == null) {
                        for (JDBlockTag pt : paramTags) {
                            if (param.name().equals(pt.name())) {
                                pDoc = pt.content();
                                break;
                            }
                        }
                    }
                    String annBadge = "";
                    if (param.annotations() != null && param.annotations().length > 0) {
                        for (String a : param.annotations()) {
                            if (a.contains("NNonNull")) {
                                annBadge = " (`@NNonNull`)";
                            } else if (a.contains("NNullable")) {
                                annBadge = " (`@NNullable`)";
                            }
                        }
                    }
                    sb.append("- `").append(param.name()).append("`").append(annBadge).append(" - ").append(pDoc != null ? renderInlineJavadoc(pDoc) : "").append("\n");
                }
            }
            sb.append("\n");
        }

        // Returns
        List<JDBlockTag> returnTags = blockTags.stream().filter(t -> t.tagName().equalsIgnoreCase("return")).collect(Collectors.toList());
        if (!returnTags.isEmpty()) {
            sb.append("**Returns:**\n\n");
            for (JDBlockTag rt : returnTags) {
                sb.append(renderInlineJavadoc(rt.content())).append("\n\n");
            }
        }

        // Throws
        List<JDBlockTag> throwsTags = blockTags.stream().filter(t -> t.tagName().equalsIgnoreCase("throws") || t.tagName().equalsIgnoreCase("exception")).collect(Collectors.toList());
        if (!throwsTags.isEmpty()) {
            sb.append("**Throws:**\n\n");
            for (JDBlockTag tt : throwsTags) {
                sb.append("- `").append(tt.name() != null ? tt.name() : "").append("` - ").append(renderInlineJavadoc(tt.content())).append("\n");
            }
            sb.append("\n");
        }

        // See also
        List<JDBlockTag> seeTags = blockTags.stream().filter(t -> t.tagName().equalsIgnoreCase("see")).collect(Collectors.toList());
        if (!seeTags.isEmpty()) {
            sb.append("**See Also:** ");
            sb.append(seeTags.stream().map(t -> renderInlineJavadoc(t.content())).collect(Collectors.joining(", "))).append("\n\n");
        }
    }

    private static void writeBlockTags(StringBuilder sb, JDDoc doc) {
        if (doc == null) {
            return;
        }
        for (JDBlockTag tag : doc.getBlockTags()) {
            if (tag.tagName().equalsIgnoreCase("deprecated")) {
                sb.append("> [!WARNING]\n> **Deprecated:** ").append(renderInlineJavadoc(tag.content())).append("\n\n");
            } else if (tag.tagName().equalsIgnoreCase("since")) {
                sb.append("**Since:** ").append(renderInlineJavadoc(tag.content())).append("\n\n");
            } else if (tag.tagName().equalsIgnoreCase("author")) {
                sb.append("**Author:** ").append(renderInlineJavadoc(tag.content())).append("\n\n");
            } else if (tag.tagName().equalsIgnoreCase("version")) {
                sb.append("**Version:** ").append(renderInlineJavadoc(tag.content())).append("\n\n");
            }
        }
    }

    private static String renderInlineJavadoc(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        String s = text.trim();
        // Replace {@code ...}
        Matcher codeMatcher = Pattern.compile("\\{@code\\s+([^\\}]+)\\}").matcher(s);
        StringBuffer sb = new StringBuffer();
        while (codeMatcher.find()) {
            String codeContent = codeMatcher.group(1).trim();
            codeMatcher.appendReplacement(sb, Matcher.quoteReplacement("`" + codeContent + "`"));
        }
        codeMatcher.appendTail(sb);
        s = sb.toString();

        // Replace {@literal ...}
        Matcher litMatcher = Pattern.compile("\\{@literal\\s+([^\\}]+)\\}").matcher(s);
        sb = new StringBuffer();
        while (litMatcher.find()) {
            String litContent = litMatcher.group(1).trim();
            litMatcher.appendReplacement(sb, Matcher.quoteReplacement("`" + litContent + "`"));
        }
        litMatcher.appendTail(sb);
        s = sb.toString();

        // Replace {@link ...} and {@linkplain ...}
        Matcher linkMatcher = Pattern.compile("\\{@link(?:plain)?\\s+([^\\}]+)\\}").matcher(s);
        sb = new StringBuffer();
        while (linkMatcher.find()) {
            String content = linkMatcher.group(1).trim();
            String target = content;
            String label = content;
            int spaceIdx = content.indexOf(' ');
            if (spaceIdx > 0) {
                target = content.substring(0, spaceIdx).trim();
                label = content.substring(spaceIdx + 1).trim();
            }
            if (isJdkOrExternal(target)) {
                linkMatcher.appendReplacement(sb, Matcher.quoteReplacement("`" + label + "`"));
            } else if (target.startsWith("#")) {
                String mName = target.substring(1);
                if (mName.contains("(")) {
                    mName = mName.substring(0, mName.indexOf('('));
                }
                linkMatcher.appendReplacement(sb, Matcher.quoteReplacement("[" + label + "](#m-" + mName + ")"));
            } else if (target.contains("#")) {
                String[] parts = target.split("#", 2);
                linkMatcher.appendReplacement(sb, Matcher.quoteReplacement("[" + label + "](#cls-" + parts[0].replace('.', '-') + ")"));
            } else {
                linkMatcher.appendReplacement(sb, Matcher.quoteReplacement("[" + label + "](#cls-" + target.replace('.', '-') + ")"));
            }
        }
        linkMatcher.appendTail(sb);
        s = sb.toString();

        s = s.replace("&#64;", "@");
        return s;
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

    private static String formatSummaryModifiers(String rawMods, boolean isInterface) {
        if (rawMods == null || rawMods.trim().isEmpty()) {
            return "";
        }
        String[] tokens = rawMods.trim().split("\\s+");
        List<String> validMods = new ArrayList<>();
        for (String m : tokens) {
            if (m.equals("public")) {
                continue;
            }
            if (isInterface && m.equals("abstract")) {
                continue;
            }
            validMods.add(m);
        }
        return String.join(" ", validMods);
    }

    private static String cleanAutogeneratedStub(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        s = s.replaceAll("(?i)Creates a new instance of of handled\\.?", "Creates a new instance in handled mode.");
        s = s.replaceAll("(?i)Creates a new instance of of propagated\\.?", "Creates a new instance in propagated mode.");
        s = s.replaceAll("(?i)Creates a new instance of of exit\\.?", "Creates a new instance in exit mode.");
        s = s.replaceAll("(?i)Creates a new instance of of template\\.?", "Creates a new template instance.");
        s = s.replaceAll("(?i)Creates a new instance of of empty\\.?", "Creates a new empty instance.");
        s = s.replaceAll("(?i)Creates a new instance of of null\\.?", "Creates a new null instance.");
        s = s.replaceAll("(?i)Creates a new instance of of inherit\\.?", "Creates a new inherit instance.");
        s = s.replaceAll("(?i)Creates a new instance of of stream\\.?", "Creates a new stream instance.");
        s = s.replaceAll("(?i)Creates a new instance of of bytes\\.?", "Creates a new bytes instance.");
        s = s.replaceAll("(?i)Creates a new instance of of string\\.?", "Creates a new string instance.");
        s = s.replaceAll("(?i)Creates a new instance of of pipe\\.?", "Creates a new pipe instance.");
        s = s.replaceAll("(?i)Creates a new instance of of path\\.?", "Creates a new path instance.");
        s = s.replaceAll("(?i)Creates a new instance of of mem\\.?", "Creates a new memory instance.");
        s = s.replaceAll("(?i)Creates a new instance of of short\\.?", "Creates a new short parts instance.");
        s = s.replaceAll("(?i)Creates a new instance of of long\\.?", "Creates a new long parts instance.");
        s = s.replaceAll("(?i)Creates a new instance of of smart\\.?", "Creates a new smart parts instance.");
        s = s.replaceAll("(?i)Creates a new instance of of multi read\\.?", "Creates a new multi-read instance.");
        s = s.replaceAll("(?i)Creates a new instance of of http bearer\\.?", "Creates a new HTTP bearer instance.");
        s = s.replaceAll("(?i)Creates a new instance of of ([a-zA-Z0-9_]+)\\.?", "Creates a new instance of $1.");
        s = s.replaceAll("(?i)Creates a new instance of of\\.?", "Creates a new instance.");
        s = s.replaceAll("(?i)Creates a new instance of create application instance from annotated instance\\.?", "Creates an application instance from an annotated instance.");
        s = s.replaceAll("(?i)Checks if is ([a-zA-Z0-9_]+)\\.?", "Checks if this is $1.");
        s = s.replaceAll("(?i)Adds add\\.?", "Adds the given element.");
        s = s.replaceAll("(?i)Converts to list\\.?", "Converts to a list.");
        return s;
    }

    private static String renderDocDescription(JDDoc doc) {
        if (doc == null) {
            return "";
        }
        MdElement desc = doc.getDescription();
        String str = desc != null ? desc.toString() : (doc.rawText() != null ? doc.rawText() : "");
        return cleanAutogeneratedStub(str);
    }

    private static String getFirstSentence(JDDoc doc) {
        if (doc == null) {
            return "";
        }
        String s = renderDocDescription(doc).trim();
        if (s.isEmpty()) {
            return "";
        }
        // Strip markdown code blocks, headers, blockquotes
        s = s.replaceAll("(?m)^#+.*$", "")
                .replaceAll("```[\\s\\S]*?```", "")
                .replaceAll("(?m)^>.*$", "")
                .trim();

        // Strip markdown links: [text](url) -> text
        s = s.replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1");

        // Strip HTML tags: <tag> -> ""
        s = s.replaceAll("<[^>]+>", "");

        // Strip inline backticks: `code` -> code
        s = s.replace("`", "");

        // Normalize all whitespace to single spaces
        s = s.replaceAll("[\\r\\n\\t]+", " ").replaceAll("\\s+", " ").trim();

        if (s.isEmpty()) {
            return "";
        }

        // Find end of first sentence (. or ? or ! followed by space or end)
        Matcher matcher = Pattern.compile("(?<=[.?!])(\\s|$)").matcher(s);
        if (matcher.find()) {
            s = s.substring(0, matcher.start()).trim();
        }

        if (s.length() > 160) {
            // Cut at last word boundary before 150
            int lastSpace = s.lastIndexOf(' ', 150);
            if (lastSpace > 50) {
                s = s.substring(0, lastSpace).trim() + "...";
            } else {
                s = s.substring(0, 150).trim() + "...";
            }
        }
        return s.trim();
    }

    private static String escapeTableCell(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("|", "\\|").replace("\n", " ").trim();
    }

    private static String escapeXml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String escapeYaml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\"", "\\\"");
    }
}
