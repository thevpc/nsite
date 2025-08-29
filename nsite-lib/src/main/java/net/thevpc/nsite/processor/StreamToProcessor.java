/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsite.processor;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nsite.context.NSiteContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 *
 * @author thevpc
 */
public class StreamToProcessor implements NSiteProcessor {

    private NSiteStreamProcessor streamProcessor;

    public StreamToProcessor(NSiteStreamProcessor streamProcessor) {
        this.streamProcessor = streamProcessor;
    }


    @Override
    public void processPath(NPath source, String mimeType, NSiteContext context) {
        String p = context.getPathTranslator().translatePath(source.toString());
        if (p != null) {
            NPath targetPath = NPath.of(p);
            targetPath.mkParentDirs();
            try (InputStream in = source.getInputStream();
                    OutputStream out = targetPath.getOutputStream();) {
//                context.getLog().debug(context.getContextName(), "update "+p+" (from "+source+") using "+streamProcessor);
                context.setVar("source",p);
                context.setVar("target",targetPath);
                streamProcessor.processStream(in, out, context);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    @Override
    public void processStream(InputStream source, OutputStream target, NSiteContext context) {
        streamProcessor.processStream(source, target, context);
    }

    @Override
    public String toString() {
        return String.valueOf(streamProcessor);
    }

}
