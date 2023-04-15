/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.back28bit.gif2petscii;

import GifUtils.GifDecoder;
import static GifUtils.GifDecoder.stripGifInPngs;
import com.sixtyfour.petscii.Petsciiator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import monoEncoder.MonochromeEncoder;

/**
 *
 * @author franc
 */
public class Gif2Petscii {

    private static String getPropFromArgs(String prop, String[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        //prop starts with minus sign
        for (int t = 0; t < args.length; t++) {
            if (args[t].toLowerCase().startsWith(prop.toLowerCase())) {
                return args[t + 1].trim();
            }
        }
        return "";
    }

    private static void getHelp(){
    
        String h="Gif2Petscii - create ASM code for monochromatic Petscii animations from a gif\n";
        h+="\nby Francesco Clementoni aka Arturo Dente aka Back to the 8 bit\n"+
        "\n--------------------------------------------------------------\n"+
        "usage: java -jar Gif2Petscii.jar <options>, where options are:\n"+
        "\n-s <source gif file> ->the gif to convert (REQUIRED)"+
        "\n-c <number> -> the number from where to start the labels for the petscii ram area in the asm code;it's useful if you want to combine more gifs together, starting from the ending of the previous one";
        
        System.out.println(h);
    }
    
    public static void main(String[] args) {

        //Petsciiator.fileList=new Vector();
        Vector datas = new Vector();
        int deltacount=0;
        //String path = args[0];
        
        String path=getPropFromArgs("-s",args);
        if (args==null || path.trim().equals("")){
            getHelp();
            System.exit(1);
        }
        
        String c=getPropFromArgs("-c", args);
        if (!c.equals("")){
            deltacount=Integer.valueOf(c);
        }
        
        GifDecoder decoder = new GifDecoder();
        Vector pngs = decoder.stripGifInPngs(path);
        //now path has given a lot of png files, I have to elaborate each one

        for (int t = 0; t < pngs.size(); t++) {
            String actualPath = (String) pngs.elementAt(t);
            File tmp = new File(actualPath);
            String[] petsciiatorArgs = {"/format=asm", "/target=" + tmp.getParent(), actualPath};
            Petsciiator petsciiator = new Petsciiator(petsciiatorArgs);
            petsciiator.run();
            try {
                tmp.delete();
            } catch (Exception del){
                
            }
            int pos = datas.indexOf(Petsciiator.stringRepresentation);//we don't waste kb if a ram portion is already there
            if (pos == -1) {
                datas.add(Petsciiator.stringRepresentation);
            } else {
                datas.add("<referrer>" + pos + "</referrer>");
            }
        }

        File forpath = new File(path);
        String gifasmName = forpath.getAbsolutePath() + "gif.asm";
        File gifasm = new File(gifasmName);
        if (gifasm.exists()) {
            try {
                gifasm.delete();
            } catch (Exception del) {
                System.out.println("error " + del.toString());
            }
        }

        try {
            gifasm.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(Gif2Petscii.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("error " + ex.toString());
            System.exit(1);
        }

        String mainloop = "                         decodestream petsciis<n>,header<n>\n";
        String loopstr = "";
        for (int t = 0; t < datas.size(); t++) {
            String dataFound = (String) datas.elementAt(t);
            if (!dataFound.startsWith("<referrer>")) {
                loopstr += mainloop.replaceAll("<n>", "" + (new Integer(t+deltacount)).toString()) + "\n";
            } else {
                loopstr += mainloop.replaceAll("<n>", "" + (int)((int)deltacount+(int)Integer.parseInt((dataFound.split("</referrer>")[0]).split("<referrer>")[1]))) + "\n";
            }
        }
        //System.out.println(MonochromeEncoder.getAsmPattern().replaceFirst("<decodestream>", loopstr));
        appendStrToFile(MonochromeEncoder.getAsmPattern().replaceFirst("<decodestream>", loopstr) + "\n", gifasmName);

        for (int t = 0; t < datas.size(); t++) {
            MonochromeEncoder.n = t+deltacount;
            //System.out.println(MonochromeEncoder.encode((String) datas.elementAt(t)));

            String dataFound = (String) datas.elementAt(t);
            if (!dataFound.startsWith("<referrer>")) {
                appendStrToFile(MonochromeEncoder.encode((String) datas.elementAt(t)) + "\n", gifasmName);
            }
        }

    }

    public static void appendStrToFile(String str, String fileName) {
        // Try block to check for exceptions
        try {

            // Open given file in append mode by creating an
            // object of BufferedWriter class
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(fileName, true));

            // Writing on output stream
            out.write(str);
            // Closing the connection
            out.close();
        } // Catch block to handle the exceptions
        catch (IOException e) {

            // Display message when exception occurs
            System.out.println("exception occurred" + e);
        }
    }
}
