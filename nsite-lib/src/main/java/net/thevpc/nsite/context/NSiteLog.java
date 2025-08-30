/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsite.context;

import net.thevpc.nuts.util.NMsg;

/**
 * @author thevpc
 */
public interface NSiteLog {

    void info(NMsg msg);
    void debug(NMsg msg);
    void error(NMsg msg);
    void warn(NMsg msg);
}
