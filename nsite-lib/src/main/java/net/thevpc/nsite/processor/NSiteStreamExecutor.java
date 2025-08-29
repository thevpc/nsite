/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsite.processor;

import net.thevpc.nsite.context.NSiteContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author thevpc
 */
public interface NSiteStreamExecutor {
    public Object execute(InputStream source, OutputStream target, NSiteContext context) ;
    default Object eval(String source, NSiteContext context) {
        return eval(new ByteArrayInputStream(source == null ? new byte[0] : source.getBytes()), context);
    }

    Object eval(InputStream source, NSiteContext context);

    public void processStream(InputStream source, OutputStream target, NSiteContext context);
}
