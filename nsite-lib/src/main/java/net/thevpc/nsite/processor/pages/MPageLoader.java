package net.thevpc.nsite.processor.pages;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.elem.NPairElement;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nsite.context.NDocContext;
import net.thevpc.nuts.lib.md.MdElement;
import net.thevpc.nuts.lib.md.MdFactory;
import net.thevpc.nuts.lib.md.MdParser;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NStringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class MPageLoader {

    public static MPage load(NPath path, NDocContext fcontext) {
        if (path.isDirectory()) {
            NPath u = path.resolve(".folder-info.md");
            if (u.isRegularFile()) {
                MPage mPage = loadFile(u, fcontext);
                mPage.setPath(path.toString());
                mPage.setPathName(path.getName());
                return mPage;
            }
            u = path.resolve(".folder-info.ntf");
            if (u.isRegularFile()) {
                MPage mPage = loadFile(u, fcontext);
                mPage.setPath(path.toString());
                mPage.setPathName(path.getName());
                return mPage;
            }
        } else if (
                (path.getName().endsWith(".md") && !path.getName().endsWith(".folder-info.md"))
                        ||
                        (path.getName().endsWith(".ntf") && !path.getName().endsWith(".folder-info.ntf"))
        ) {
            return loadFile(path, fcontext);
        }
        return null;
    }

    private static MPage loadFile(NPath path, NDocContext fcontext) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        fcontext.getProcessorManager().processSourceRegularFile(path, null, bos);
        if (path.getName().endsWith(".md")) {
            return loadFileMarkdown(path, fcontext, new ByteArrayInputStream(bos.toByteArray()));
        }
        if (path.getName().endsWith(".ntf")) {
            return loadFileNtf(path, fcontext, new ByteArrayInputStream(bos.toByteArray()));
        }
        throw new IllegalArgumentException("Unsupported file type: " + path.getName());
    }


    private static MPage loadFileNtf(NPath path, NDocContext fcontext, InputStream is) {
        int maxRowSize = 1024 * 4;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            br.mark(maxRowSize);
            String firstLine = br.readLine();
            MPage g = new MPage(MPageType.NTF)
                    .setPath(path.toString())
                    .setTitle(path.getName())
                    .setPathName(path.getName());
            if (firstLine != null) {
                if (firstLine.startsWith("---")) {
                    List<String> headerLines = new ArrayList<>();
                    while (true) {
                        br.mark(maxRowSize);
                        String nextLine = br.readLine();
                        if (nextLine == null) {
                            break;
                        } else if (nextLine.startsWith("---")) {
                            break;
                        } else {
                            nextLine = nextLine.trim();
                            if (!nextLine.startsWith("#")) {
                                int i = nextLine.indexOf(':');
                                if (i > 0) {
                                    setPageHeaderVar(g, nextLine.substring(0, i).trim(), NElement.ofString(nextLine.substring(i + 1).trim()));
                                }
                            }
                            headerLines.add(nextLine);
                        }
                    }
                } else {
                    br.reset();
                }
                NText text = NTexts.of().parser().parse(br);
                g.setNtfContent(text);
                return g;
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return null;
    }


    private static MPage loadFileMarkdown(NPath path, NDocContext fcontext, InputStream is) {
        MdParser p = MdFactory.createParser(is);
        MdElement md = p.parse();
        MPage g = new MPage(MPageType.MARKDOWN)
                .setPath(path.toString())
                .setTitle(path.getName())
                .setPathName(path.getName());
        NElement mdHeader = md.getPreambleHeader();
        if (mdHeader instanceof NObjectElement) {
            for (NElement fe : ((NObjectElement) mdHeader).children()) {
                if(fe.isNamedPair()){
                    NPairElement pair = fe.asPair().get();
                    setPageHeaderVar(g, pair.key().asStringValue().orNull() , pair.value());
                }
            }
        }
        return g.setMarkdownContent(md);
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
                g.setSortAsc(value.asBooleanValue().orElse(!"desc".equalsIgnoreCase(String.valueOf(value))));
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
                if(value.isArray() && value.asArray().get().children().stream().allMatch(x->x.isAnyString() || x.isNull())){
                    g.setTags(value.asArray().get().children().stream().map(x->x.asStringValue().orNull()).toArray(String[]::new));
                }
                break;
            }
            case "type": {
                g.setTypeInfo((Map) value);
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
