package net.thevpc.nsite.processor.pages;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nsite.context.NSiteContext;
import net.thevpc.nuts.lib.md.MdElement;
import net.thevpc.nuts.lib.md.MdFactory;
import net.thevpc.nuts.lib.md.MdParser;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringBuilder;
import net.thevpc.nuts.util.NStringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Instant;

public class MPageLoader {

    public static MPage loadPageFromFileOrDirectory(NPath path, NSiteContext fcontext) {
        if (path.isDirectory()) {
            NPath u = path.resolve(".folder-info.md");
            if (u.isRegularFile()) {
                return loadPageFromFile(u, fcontext);
            }
            u = path.resolve(".folder-info.ntf");
            if (u.isRegularFile()) {
                return loadPageFromFile(u, fcontext);
            }
        } else if (
                (path.getName().endsWith(".md") && !path.getName().endsWith(".folder-info.md"))
                        ||
                        (path.getName().endsWith(".ntf") && !path.getName().endsWith(".folder-info.ntf"))
        ) {
            return loadPageFromFile(path, fcontext);
        }
        return null;
    }

    private static MPage loadPageFromFile(NPath path, NSiteContext fcontext) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        fcontext.getProcessorManager().processSourceRegularFile(path, null, bos);
        String defaultContentType=null;
        if (path.getName().endsWith(".ntf") || path.getName().endsWith(".ntf.md")) {
            defaultContentType="ntf";
        }else if (path.getName().endsWith(".html.md")) {
            defaultContentType="html";
        }else if (path.getName().endsWith(".md")) {
            defaultContentType="markdown";
        }else {
            throw new IllegalArgumentException("Unsupported file type: " + path.getName());
        }
        ByteArrayInputStream is = new ByteArrayInputStream(bos.toByteArray());
        int maxRowSize = 1024 * 4;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            br.mark(maxRowSize);
            String firstLine = br.readLine();
            MPage g = new MPage()
                    .setPath(path.toString())
                    .setTitle(path.getName())
                    .setPathName(".folder-info.md".equals(path.getName())?path.getParent().getName() :  path.getName());
            if (firstLine != null) {
                NStringBuilder yamlPrefix = new NStringBuilder();
                if (firstLine.startsWith("---")) {
                    while (true) {
                        br.mark(maxRowSize);
                        String nextLine = br.readLine();
                        if (nextLine == null) {
                            break;
                        } else if (nextLine.startsWith("---")) {
                            break;
                        } else {
                            yamlPrefix.println(nextLine);
                        }
                    }
                } else {
                    br.reset();
                }
                if (!yamlPrefix.isBlank()) {
                    NElement parsed = NElementParser.ofYaml().parse(yamlPrefix.toString());
                    NListContainerElement list = parsed.toListContainer().get();
                    for (NElement child : list.children()) {
                        NPairElement np = child.asNamedPair().get();
                        String key = np.key().asStringValue().get();
                        setPageHeaderVar(g, key, np.value());
                    }
                }
                String ct = NStringUtils.trim(g.getContentType());
                if (NBlankable.isBlank(ct)) {
                    ct = NStringUtils.trim(defaultContentType);
                }
                if (ct.startsWith("text/")) {
                    ct = ct.substring("text/".length());
                } else if (ct.equals("application/json")) {
                    ct = "json";
                }
                setPageHeaderVar(g, "contentType", NElement.ofString(ct));

                String content = NIOUtils.readString(br);
                g.setStringContent(content);
                switch (ct) {
                    case "ntf":
                    case "x-ntf": {
                        try (Reader r = new StringReader(content)) {
                            NText text = NTexts.of().parser().parse(r);
                            g.setParsedContent(text);
                        }
                        break;
                    }
                    case "":
                    case "md":
                    case "markdown": {
                        try (Reader r = new StringReader(content)) {
                            MdParser p = MdFactory.createParser(r);
                            MdElement md = p.parse();
                            g.setParsedContent(md);
                        }
                        break;
                    }
                    case "html": {
                        g.setParsedContent(content);
                        break;
                    }
                }
                return g;
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return null;
    }


    private static void setPageHeaderVar(MPage g, String key, NElement value) {
        switch (NStringUtils.trim(key)) {
            case "id": {
                g.setId(value.asStringValue().orNull());
                break;
            }
            case "title": {
                g.setTitle(value.asStringValue().orNull());
                break;
            }
            case "order": {
                g.setOrder(value.asIntValue().orElse(0));
                break;
            }
            case "sort": {
                g.setSortAsc(value.asBooleanValue().orElse(!"desc".equalsIgnoreCase(value.asStringValue().orElse(""))));
                break;
            }
            case "author": {
                g.setAuthor(value.asStringValue().orNull());
                break;
            }
            case "authorTitle":
            case "author_title": {
                g.setAuthorTitle(value.asStringValue().orNull());
                break;
            }
            case "authorUrl":
            case "authorURL":
            case "author_url": {
                g.setAuthorURL(value.asStringValue().orNull());
                break;
            }
            case "authorImageUrl":
            case "authorImageURL":
            case "author_image_url":
            case "authorImage":
            case "author_image": {
                g.setAuthorImageUrl(value.asStringValue().orNull());
                break;
            }
            case "menuTitle":
            case "menu_title":
            case "sidebarLabel":
            case "sidebar_label": {
                g.setMenuTitle(value.asStringValue().orNull());
                break;
            }
            case "subTitle":
            case "sub_title": {
                g.setSubTitle(value.asStringValue().orNull());
                break;
            }
            case "website": {
                g.setWebsite(value.asStringValue().orNull());
                break;
            }
            case "hmi": {
                g.setHmi(value.asStringValue().orNull());
                break;
            }
            case "category": {
                g.setCategory(value.asStringValue().orNull());
                break;
            }
            case "installCommand": {
                g.setInstallCommand(value.asStringValue().orNull());
                break;
            }
            case "contentType": {
                g.setContentType(value.asStringValue().orNull());
                break;
            }
            case "exampleCommand": {
                g.setExampleCommand(value.asStringValue().orNull());
                break;
            }
            case "publishDate":
            case "publish_date": {
                if (value.isAnyDate()) {
                    g.setPublishDate(value.asInstantValue().get());
                } else {
                    String d = value.asStringValue().orNull();
                    if (d != null) {
                        g.setPublishDate(parseDate(d));
                    }
                }
                break;
            }
            case "tags": {
                if (value.isArray() && value.asArray().get().children().stream().allMatch(x -> x.isAnyString() || x.isNull())) {
                    g.setTags(value.asArray().get().children().stream().map(x -> x.asStringValue().orNull()).toArray(String[]::new));
                }
                break;
            }
            case "type": {
                g.setTypeInfo((NObjectElement) value);
                break;
            }
        }
    }


    private static Instant parseDate(String d) {
        for (String pattern : new String[]{
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd"
        }) {
            try {
                return (new SimpleDateFormat(pattern).parse(d).toInstant());
            } catch (Exception ex) {
                //
            }
        }
        return null;
    }
}
