/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsite.processor;

import net.thevpc.nsite.context.NSiteContext;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author thevpc
 */
public interface NSiteStreamProcessor {

    public void processStream(InputStream source, OutputStream target, NSiteContext context);
}
