///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.thevpc.nsite.context;
//
//import net.thevpc.nuts.NErr;
//import net.thevpc.nuts.NOut;
//
///**
// *
// * @author thevpc
// */
//public class DefaultNSiteLog implements NSiteLog {
//    public static final NSiteLog INSTANCE=new DefaultNSiteLog();
//
//    public DefaultNSiteLog() {
//    }
//
//    @Override
//    public void info(String title, String message) {
//        NOut.println("[info ] "+title + ": " + message);
//    }
//
//
//    @Override
//    public void error(String title, String message) {
//        NErr.println("[error] "+title + ": " + message);
//    }
//
//    @Override
//    public void warn(String title, String message) {
//        NErr.println("[warn] "+title + ": " + message);
//    }
//
//    @Override
//    public void debug(String title, String message) {
//        NErr.println("[debug] "+title + ": " + message);
//    }
//
//}
