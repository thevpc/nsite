/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nsite.deprecated;

import net.thevpc.nuts.text.NText;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 *
 * @author thevpc
 */
public class IOUtils {

    public static String getTextResource(String url)  {
        URL r = IOUtils.class.getResource(url);
        if (r == null) {
            throw new UncheckedIOException(new IOException("Resource not found : [" + url + "]"));
        }
        return getText(r);
    }

    public static String extractFileName(String str)  {
        int i = str.lastIndexOf('/');
        if (i < 0) {
            return str;
        }
        return str.substring(i + 1);
    }

    public static void writeStringAppend(String str, File file)  {
        File pf = file.getParentFile();
        if (pf != null) {
            pf.mkdirs();
        }
        try(FileWriter fileWriter = new FileWriter(file, true)) {
            fileWriter.append("\n" + str);
//        fileWriter.flush();
        }catch (IOException e) {
            throw new UncheckedIOException(e);
        }
//        System.out.println("####[APPEND TO]#### " + file.getPath());
    }

    public static void writeString(String str, File file, ProjectTemplate project)  {
        TemplateConsole console = project.getConsole();
        String old = file.exists() ? getText(file) : null;
        boolean isOverride = false;
        if (old != null) {
            if (old.equals(str)) {
                //do nothing...
                return;
            }
            if (!project.isNewlyCreated(file.getPath())) {
                if (!console.ask("override://" + file.getPath(), "override file " + file.getName(), new ValidatorFactory(project.getSession()).BOOLEAN, null).equals("true")) {
                    console.println("%s %s",NText.ofStyledError("[WONT OVERRIDE]"),file);
                    return;
                }
            }
            isOverride = true;
        }
        File pf = file.getParentFile();
        if (pf != null) {
            pf.mkdirs();
        }
        try(FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(str);
            fileWriter.flush();
        }catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (isOverride) {
            console.println("[OVERRIDE] %s%n", file);
        } else {
            project.setNewlyCreated(file.getPath());
            console.println("[GENERATE] %s%n",file);
        }
    }

    public static String getText(File website)  {
        try {
            return getText(website.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String getText(URL website)  {
        try {
            URLConnection connection = website.openConnection();
            StringBuilder response = new StringBuilder();
            try(BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()))) {
                String inputLine;
                boolean first = true;
                 while((inputLine =in.readLine())!=null){
                    if (first) {
                        first = false;
                    } else {
                        response.append("\n");
                    }
                    response.append(inputLine);
                }

            }

            return response.toString();
        }catch (IOException ex){
            throw new UncheckedIOException(ex);
        }
    }

    public static String toString(Properties newProperties, String comments) {
        try {
            StringWriter s = new StringWriter();
            newProperties.store(s, comments == null ? "any" : comments);
            String ss = s.getBuffer().toString();
            if (comments != null) {
                return ss;
            }
            String[] all = ss.split("\n");
            StringBuilder finalV = new StringBuilder();
            for (int i = 0; i < all.length; i++) {
                if (finalV.length() > 0) {
                    finalV.append("\n");
                }
                if (!all[i].startsWith("#")) {
                    finalV.append(all[i]);
                }
            }
            return finalV.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean isEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

}
