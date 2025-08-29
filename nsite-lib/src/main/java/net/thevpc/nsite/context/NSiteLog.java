/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsite.context;

/**
 * @author thevpc
 */
public interface NSiteLog {

    void info(String title, String message);

    void debug(String title, String message);

    void error(String title, String message);

    void warn(String title, String message);
}
