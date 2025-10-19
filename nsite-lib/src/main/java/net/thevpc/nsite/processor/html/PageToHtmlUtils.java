package net.thevpc.nsite.processor.html;

import net.thevpc.nsite.context.NSiteContext;
import net.thevpc.nsite.html.*;
import net.thevpc.nsite.processor.pages.MPage;
import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;

import java.awt.*;
import java.io.StringReader;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PageToHtmlUtils {
    public interface GeneratorContext {
        String nextId();

        static GeneratorContext of(NSiteContext fcontext) {
            NOptional<GeneratorContext> v = fcontext.getVar(GeneratorContext.class.getSimpleName());
            if (!v.isPresent()) {
                MyGeneratorContext r = new MyGeneratorContext();
                fcontext.setVar(GeneratorContext.class.getSimpleName(), r);
                return r;
            }
            return v.get();
        }

    }

    public NHtmlNode pageContent2html(MPage page, GeneratorContext generatorContext) {
        return toHtml(page.getContentType(), NUtils.firstNonNull(page.getParsedContent(), page.getStringContent()), generatorContext);
    }

    public NNormalizedText normalizeText(NText text) {
        return NTexts.of().normalize(text, new NTextTransformConfig()
                .setFlatten(true)
                .setNormalize(true)
                .setApplyTheme(true)
                .setThemeName(null)//perhaps override
                .setBasicTrueStyles(true)
                .setThemeName("whiteboard")
        );
    }

    public NHtmlNode ntf2html(NText elem) {
        switch (elem.type()) {
            case PLAIN: {
                return NHtml.raw(((NTextPlain) elem).getValue());
            }
            case LINK: {
                NTextLink lnk = (NTextLink) elem;
                return new NHtmlTag("a")
                        .attr("href", lnk.getValue())
                        .attr("class", "md-link")
                        .body(lnk.getValue());
            }
            case TITLE: {
                NTextTitle title = (NTextTitle) elem;
                NHtmlTag t = new NHtmlTag("H4")
                        .attr("class", "md-title-" + title.getLevel())
                        .body(
                                ntf2html(title.getChild())
                        );
                return t;
            }
            case LIST: {
                List<NHtmlNode> nnn = new ArrayList<>();
                NTextList ll = (NTextList) elem;
                for (NText child : ll.getChildren()) {
                    nnn.add(ntf2html(child));
                }
                return new NHtmlTagList(nnn.toArray(new NHtmlNode[0]));
            }
            case CODE: {
                NTextCode c = (NTextCode) elem;
                String type = "default";
                String language = c.getQualifier();
                String text = c.getValue();
                boolean inline = false;
                if (inline) {
                    String value = text;
                    if (value.matches("[a-zA-Z0-9_-]+")) {
                        return (new NHtmlTag("mark")
                                .attr("class", "md-code md-code-" + type + " " + language)
                                .body(escapeCode(value)));
                    }
                    return (new NHtmlTag("code")
                            .attr("class", "md-code md-code-" + type + " " + language)
                            .body(escapeCode(value)));
                }
                return (new NHtmlTag("pre")
                        .attr("class", "md-code md-code-" + type + " " + language)
                        .body(
                                (new NHtmlTag("code").attr("class", language).body(escapeCode(text)))
                        ));
            }
            case STYLED: {
                NTextStyled style = (NTextStyled) elem;
                NTextStyles styles = style.getStyles();
                NText c = style.getChild();
                Set<String> hstyles = new HashSet<>();
                Set<String> hclasses = new HashSet<>();
                NHtmlTag t = new NHtmlTag("span");
//                hstyles.add("display: inline");
                boolean blink = false;
                for (NTextStyle st : styles) {
                    switch (st.getType()) {
                        case BOLD: {
                            hstyles.add("font-weight: bold");
                            break;
                        }
                        case PLAIN: {
                            break;
                        }
                        case ITALIC: {
                            hstyles.add("font-style: italic");
                        }
                        case BLINK: {
                            blink = true;
                            break;
                        }
                        case UNDERLINED: {
                            hstyles.add("text-decoration: underline");
                            break;
                        }
                        case REVERSED: {
                            hstyles.add("-webkit-filter: invert(100%)");
                            hstyles.add("filter: invert(100%)");
                            break;
                        }
                        case STRIKED: {
                            hstyles.add("text-decoration:line-through");
                            break;
                        }
                        case BACK_TRUE_COLOR: {
                            Color cl = new Color(st.getVariant());
                            hstyles.add("background-color: " + NColor.toHtmlHex(cl));
                            break;
                        }
                        case FORE_TRUE_COLOR: {
                            Color cl = new Color(st.getVariant());
                            hstyles.add("color: " + NColor.toHtmlHex(cl));
                            break;
                        }
                        default: {
                            NText ee = normalizeText(elem);
                            hstyles.add("color: red");
                        }
                    }
                }
                t.attr("style", String.join(";", hstyles));
                t.attr("class", String.join(" ", hclasses));
                t.body(ntf2html(style.getChild()));
                if (blink) {
                    t = new NHtmlTag("blink").body(t);
                }
                return t;
            }
        }
        return NHtml.raw(elem.toString());
    }


    public NHtmlNode toHtml(String type, Object content, GeneratorContext generatorContext) {
        switch (NStringUtils.trim(type)) {
            case "markdown": {
                if (content instanceof MdElement) {
                    return md2html((MdElement) content, generatorContext);
                } else if (content instanceof String) {
                    try (StringReader reader = new StringReader((String) content)) {
                        MdParser p = MdFactory.createParser(reader);
                        MdElement md = p.parse();
                        return md2html(md, generatorContext);
                    }
                } else {
                    throw new IllegalArgumentException("unsupported type: " + type);
                }
            }
            case "ntf": {
                if (content instanceof NText) {
                    NText nnormalized = normalizeText((NText) content);
//                    return new NHtmlBuffer.NHtmlTag("pre").body(ntf2html(nnormalized));
                    return ntf2html(nnormalized);
                } else if (content instanceof String) {
                    NText ntfContent = NText.of((String) content);
                    NText nnormalized = normalizeText(ntfContent);
//                    return new NHtmlBuffer.NHtmlTag("pre").body(ntf2html(nnormalized));
                    return ntf2html(nnormalized);
                } else {
                    throw new IllegalArgumentException("unsupported type: " + type);
                }
            }
            default: {
                if (content instanceof String) {
                    return NHtml.tag("pre").body(
                            //<code class="language-xml">
                            NHtml.tag("code").attr("class", "language-" + NStringUtils.trim(type)).body(
                                    NHtml.escapeString((String) content)
                            )
                    );
                } else {
                    throw new IllegalArgumentException("unsupported type: " + type);
                }
            }
        }
    }

    public NHtmlNode md2html(MdElement markdown, GeneratorContext generatorContext) {
        if (markdown == null) {
            return null;
        }
        switch (markdown.type().group()) {
            case TEXT: {
                MdText text = markdown.asText();
                return NHtml.raw(text.getText());
            }
            case PHRASE: {
                NHtmlTag p = new NHtmlTag("p")
                        .attr("class", "md-phrase");
                for (MdElement child : markdown.asPhrase().getChildren()) {
                    p.body(md2html(child, generatorContext));
                }
                return p;
            }
            case BODY: {
                return new NHtmlTagList(
                        Arrays.stream(markdown.asBody().getChildren())
                                .map(x -> md2html(x, generatorContext))
                                .toArray(NHtmlNode[]::new)
                );
            }
            case TITLE: {
                MdTitle title = markdown.asTitle();
                NHtmlTag t = new NHtmlTag("H4")
                        .attr("class", "md-title-" + title.getDepth())
                        .body(
                                md2html(title.getValue(), generatorContext)
                        );
                List<NHtmlNode> nnn = new ArrayList<>();
                nnn.add(t);
                for (MdElement child : title.getChildren()) {
                    nnn.add(md2html(child, generatorContext));
                }
                return new NHtmlTagList(nnn.toArray(new NHtmlNode[0]));
            }
            case ITALIC: {
                return (new NHtmlTag("i")
                        .attr("class", "md-italic")
                        .body(md2html(markdown.asItalic().getContent(), generatorContext)));
            }
            case BOLD: {
                return (new NHtmlTag("b")
                        .attr("class", "md-bold")
                        .body(md2html(markdown.asBold().getContent(), generatorContext)));
            }
            case CODE: {
                MdCode code = markdown.asCode();
                String type = code.getType();
                String language = code.getLanguage();
                if (code.isInline()) {
                    String value = code.getValue();
                    if (value.matches("[a-zA-Z0-9_-]+")) {
                        return (new NHtmlTag("mark")
                                .attr("class", "md-code md-code-" + type + " " + language)
                                .body(escapeCode(value)));
                    }
                    return (new NHtmlTag("code")
                            .attr("class", "md-code md-code-" + type + " " + language)
                            .body(escapeCode(value)));
                }
                return (new NHtmlTag("pre")
                        .attr("class", "md-code md-code-" + type + " " + language)
                        .body(
                                (new NHtmlTag("code").attr("class", language).body(escapeCode(code.getValue())))
                        ));
            }
            case UNNUMBERED_ITEM: {
                NHtmlTag li = new NHtmlTag("li")
                        .attr("class", "md-uli")
                        .body(md2html(markdown.asUnNumItem().getValue(), generatorContext));
                for (MdElement child : markdown.asUnNumItem().getChildren()) {
                    li.body(md2html(child, generatorContext));
                }
                return li;
            }
            case NUMBERED_ITEM: {
                NHtmlTag li = new NHtmlTag("li")
                        .attr("class", "md-oli")
                        .body(md2html(markdown.asNumItem().getValue(), generatorContext));
                for (MdElement child : markdown.asUnNumItem().getChildren()) {
                    li.body(md2html(child, generatorContext));
                }
                return li;
            }
            case UNNUMBERED_LIST: {
                MdUnNumberedList li = markdown.asUnNumList();
                NHtmlTag hli = new NHtmlTag("ul")
                        .attr("class", "md-ul");
                for (MdUnNumberedItem child : li.getChildren()) {
                    hli.body(md2html(child, generatorContext));
                }
                return hli;
            }
            case NUMBERED_LIST: {
                MdUnNumberedList li = markdown.asUnNumList();
                NHtmlTag hli = new NHtmlTag("ol")
                        .attr("class", "md-ol");
                for (MdUnNumberedItem child : li.getChildren()) {
                    hli.body(new NHtmlTag("li")
                            .attr("class", "md-oli")
                            .body(md2html(child, generatorContext)));
                }
                return hli;
            }
            case IMAGE: {
                MdImage li = markdown.asImage();
                return new NHtmlTag("p").body(
                        new NHtmlTag("img")
                                .attr("src", li.getImageUrl())
                                .attr("class", "md-img img-fluid border")
                                .attr("alt", li.getImageTitle())

                )
                        ;
            }
            case LINK: {
                MdLink li = markdown.asLink();
                return new NHtmlTag("a")
                        .attr("href", li.getLinkUrl())
                        .attr("class", "md-link")
                        .body(li.getLinkTitle());
            }
            case HORIZONTAL_RULE: {
                return new NHtmlTag("hr").attr("class", "divider")
                        .attr("class", "md-hr")
                        .setNoEnd(true);
            }
            case LINE_BREAK: {
                return new NHtmlTag("hr").attr("class", "divider")
                        .attr("class", "md-br")
                        .setNoEnd(true);
            }
            case ADMONITION: {
                MdAdmonition li = markdown.asAdmonition();
                switch (li.getType()) {
                    case WARNING: {
                        return new NHtmlTag("div").attr("class", "alert alert-warning mb-4")
                                .body(new NHtmlTagList(
                                        NHtml.tag("span").attr("class", "badge badge-danger text-uppercase").body(NHtml.raw("NOTE:")),
                                        NHtml.space(),
                                        md2html(li.getContent(), generatorContext)
                                ));
                    }
                    case DANGER: {
                        return NHtml.tag("div").attr("class", "alert alert-danger mb-4")
                                .body(NHtml.list(
                                        NHtml.tag("span").attr("class", "badge badge-danger text-uppercase").body(new NHtmlRaw("NOTE:")),
                                        new NHtmlRaw(" "),
                                        md2html(li.getContent(), generatorContext)
                                ));
                    }
                    case INFO: {
                        return NHtml.tag("div").attr("class", "alert alert-info mb-4")
                                .body(NHtml.list(
//                                        new NHtmlBuffer.NHtmlTag("span").attr("class","badge badge-danger text-uppercase").body(new NHtmlBuffer.NHtmlRaw("NOTE:")),
//                                        NHtml.space(),
                                        md2html(li.getContent(), generatorContext)
                                ));
                    }
                    case TIP: {
                        return NHtml.tag("div").attr("class", "alert alert-success mb-4")
                                .body(NHtml.list(
//                                        new NHtmlBuffer.NHtmlTag("span").attr("class","badge badge-danger text-uppercase").body(new NHtmlBuffer.NHtmlRaw("NOTE:")),
//                                        NHtml.space(),
                                        md2html(li.getContent(), generatorContext)
                                ));
                    }
                    case IMPORTANT: {
                        return NHtml.tag("div").attr("class", "alert alert-success mb-4")
                                .body(NHtml.list(
//                                        new NHtmlBuffer.NHtmlTag("span").attr("class","badge badge-danger text-uppercase").body(new NHtmlBuffer.NHtmlRaw("NOTE:")),
//                                        NHtml.space(),
                                        md2html(li.getContent(), generatorContext)
                                ));
                    }
                    case NOTE: {
                        return NHtml.tag("div").attr("class", "alert alert-info mb-4")
                                .body(NHtml.list(
                                        NHtml.tag("span").attr("class", "badge badge-success text-uppercase").body(new NHtmlRaw("NOTE:")),
                                        new NHtmlRaw(" "),
                                        md2html(li.getContent(), generatorContext)
                                ));
                    }
                    case CAUTION: {
                        return NHtml.tag("div").attr("class", "alert alert-info mb-4")
                                .body(NHtml.list(
                                        NHtml.tag("span").attr("class", "badge badge-danger text-uppercase").body(new NHtmlRaw("NOTE:")),
                                        new NHtmlRaw(" "),
                                        md2html(li.getContent(), generatorContext)
                                ));
                    }
                    default: {
                        return NHtml.tag("div").attr("class", "alert alert-info mb-4")
                                .body(NHtml.list(
                                        NHtml.tag("span").attr("class", "badge badge-danger text-uppercase").body(new NHtmlRaw("NOTE:")),
                                        new NHtmlRaw(" "),
                                        md2html(li.getContent(), generatorContext)
                                ));
                    }
                }

            }
            case TABLE: {
                MdTable t = markdown.asTable();
                NHtmlTag table = NHtml.tag("table").attr("class", "table table-bordered table-striped");
                MdColumn[] columns = t.getColumns();
                List<NHtmlNode> rows = new ArrayList<>();

                NHtmlTag th = NHtml.tag("tr");
                th.body(NHtml.list(
                        Arrays.stream(columns).map(x -> NHtml.tag("th").body(md2html(x.getName(), generatorContext))).collect(Collectors.toList())
                ));
                rows.add(th);
                for (MdRow row : t.getRows()) {
                    NHtmlTag tr = NHtml.tag("tr");
                    tr.body(NHtml.list(
                            Arrays.stream(row.getCells()).map(x -> NHtml.tag("td").body(md2html(x, generatorContext))).collect(Collectors.toList())
                    ));
                    rows.add(tr);
                }
                table.body(NHtml.list(rows));
                return table;
            }
            case XML: {
                MdXml xml = markdown.asXml();
                switch (xml.getTag().toLowerCase()) {
                    case "tabs": {
                        return md2htmlXmlTabs(xml, generatorContext);
                    }
                }
                return null;
            }
        }
        return null;
    }

    private NHtmlNode md2htmlXmlTabs(MdXml xml, GeneratorContext generatorContext) {
        String dv = xml.getProperties().get("defaultValue");
        String valuesString = xml.getProperties().get("values");
        //Map<String, String> map = valuesString == null ? null : NElementParser.ofJson().parse(valuesString, Map.class);
        List<NHtmlNode> allHeader = new ArrayList<>();
        List<NHtmlNode> allContent = new ArrayList<>();
        String newUuid = "id" + generatorContext.nextId().replace("-", "");
        MdElement[] children = xml.getContent().asBody().getChildren();
        for (int i = 0; i < children.length; i++) {
            MdElement c = children[i];
            MdXml cx = c.asXml();
            String tabValue = cx.getProperties().get("value");
            String tabLabel = NStringUtils.firstNonBlank(tabValue, cx.getProperties().get("label"));
            String active = Objects.equals(dv, tabValue) ? "active" : "";
            String currId = newUuid + i;
            {
                NHtmlTag h = NHtml.tag("li").attr("class", "nav-item").attr("role", "presentation");
                NHtmlTag a = NHtml.tag("a").attr("class", "nav-link " + active).attr("id", currId + "-tab").attr("data-toggle", "tab")
                        .attr("href", "#" + newUuid + i).attr("role", "tab").attr("aria-controls", currId).attr("aria-selected", active.equals("active") ? "true" : "false")
                        .body(new NHtmlRaw(tabLabel));
                h.body(a);
                allHeader.add(h);
            }
            {
                NHtmlTag h = NHtml.tag("div").attr("class", "tab-pane fade show " + active).attr("id", currId).attr("role", "tabpanel").attr("aria-labelledby", currId + "-tab");
                h.body(md2html(cx.getContent(), generatorContext));
                allContent.add(h);
            }

        }
        return NHtml.list(
                NHtml.tag("ul").attr("class", "nav nav-tabs").attr("role", "tablist")
                        .body(NHtml.list(allHeader)),
                NHtml.tag("div").attr("class", "tab-content my-3")
                        .body(NHtml.list(allContent))
        );
    }

    private static String escapeCode(String value) {
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    static class MyGeneratorContext implements GeneratorContext {
        int index = 1;

        @Override
        public String nextId() {
            int i = index;
            index++;
            return "i" + i;
        }
    }
}
