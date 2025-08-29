/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsite.processor.impl;

import net.thevpc.nsite.executor.NSiteExprEvaluator;
import net.thevpc.nsite.context.NSiteContext;
import net.thevpc.nsite.processor.NSiteStreamExecutor;
import net.thevpc.nsite.util.FileProcessorUtils;

import java.io.*;

/**
 *
 * @author thevpc
 */
public class DefaultNSiteStreamExecutor implements NSiteStreamExecutor {

    private NSiteExprEvaluator evaluator;

    public DefaultNSiteStreamExecutor(NSiteExprEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public Object eval(InputStream source, NSiteContext context) {
        String text = FileProcessorUtils.loadString(source, null);
        if (text != null) {
            return evaluator.eval(text, context);
        }
        return null;
    }

    @Override
    public void processStream(InputStream source, OutputStream target, NSiteContext context) {
        String text = FileProcessorUtils.loadString(source, null);
        if (text != null) {
            Object z = evaluator.eval(text, context);
            if (z != null) {
                try {
                    Writer w = new OutputStreamWriter(target);
                    w.write(String.valueOf(z));
                    w.flush();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }

    @Override
    public Object execute(InputStream source, OutputStream target, NSiteContext context) {
        String text = FileProcessorUtils.loadString(source, null);
        if (text != null) {
            Object z = evaluator.eval(text, context);
            return z == null ? "" : z;
        }
        return "";
    }

    @Override
    public String toString() {
        return "Executor(" + evaluator + ")";
    }

}
