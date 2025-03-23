package net.thevpc.nsite.processor.base;

import net.thevpc.nsite.context.NDocContext;
import net.thevpc.nuts.io.NCharReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProcessStreamContext {
    final TagStreamProcessor tagStreamProcessor;
    TagTokenReader tr;
    TagNodeReader nr;
    NDocContext context;
    Writer out;
    InputStream source;

    public ProcessStreamContext(TagStreamProcessor tagStreamProcessor, InputStream source, NDocContext context) {
        this.context = context;
        this.tagStreamProcessor = tagStreamProcessor;
        tr = new TagTokenReader(tagStreamProcessor.startTag, tagStreamProcessor.endTag, tagStreamProcessor.escape, tagStreamProcessor.exprLang,
                new NCharReader(new InputStreamReader(source)));
        nr =new TagNodeReader(tr,tagStreamProcessor.exprLang);
    }

    public TagStreamProcessor getStreamProcessor() {
        return tagStreamProcessor;
    }

    public TagNode next()  {
        List<TagNode> r = new ArrayList<>();
        while (true) {
            TagNode u = nr.next();
            if (u != null) {
                r.add(u);
            }else {
                break;
            }
        }
        return ListTagNode.of(r);
    }

}
