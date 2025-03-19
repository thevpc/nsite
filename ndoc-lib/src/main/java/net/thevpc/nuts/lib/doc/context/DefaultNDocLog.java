/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.context;

import net.thevpc.nuts.NErr;
import net.thevpc.nuts.NOut;

import java.io.PrintStream;

/**
 *
 * @author thevpc
 */
public class DefaultNDocLog implements NDocLog {
    public static final NDocLog INSTANCE=new DefaultNDocLog();
    
    public DefaultNDocLog() {
    }

    @Override
    public void info(String title, String message) {
        NOut.println("[info ] "+title + ": " + message);
    }


    @Override
    public void error(String title, String message) {
        NErr.println("[error] "+title + ": " + message);
    }

    @Override
    public void warn(String title, String message) {
        NErr.println("[warn] "+title + ": " + message);
    }

    @Override
    public void debug(String title, String message) {
        NErr.println("[debug] "+title + ": " + message);
    }
    
}
