/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.nsite.executor;

import net.thevpc.nuts.lib.nsite.context.NDocContext;

/**
 *
 * @author thevpc
 */
public interface NDocExprEvaluator {

    Object eval(String content, NDocContext context);
    
}
