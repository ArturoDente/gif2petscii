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

    public static void main(String[] args) {

        //Petsciiator.fileList=new Vector();
        Vector datas = new Vector();
        String path = args[0];

        GifDecoder decoder = new GifDecoder();
        Vector pngs = decoder.stripGifInPngs(path);
        //now path has given a lot of png files, I have to elaborate each one

        for (int t = 0; t < pngs.size(); t++) {
            String actualPath = (String) pngs.elementAt(t);
            File tmp = new File(actualPath);
            String[] petsciiatorArgs = {"/format=asm", "/target=" + tmp.getParent(), actualPath};
            Petsciiator petsciiator = new Petsciiator(petsciiatorArgs);
            petsciiator.run();
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
        }

        String mainloop = "                         decodestream petsciis<n>,header<n>\n";
        String loopstr = "";
        for (int t = 0; t < datas.size(); t++) {
            String dataFound = (String) datas.elementAt(t);
            if (!dataFound.startsWith("<referrer>")) {
                loopstr += mainloop.replaceAll("<n>", "" + t) + "\n";
            } else {
                loopstr += mainloop.replaceAll("<n>", "" + (dataFound.split("</referrer>")[0]).split("<referrer>")[1]) + "\n";
            }
        }
        //System.out.println(MonochromeEncoder.getAsmPattern().replaceFirst("<decodestream>", loopstr));
        appendStrToFile(MonochromeEncoder.getAsmPattern().replaceFirst("<decodestream>", loopstr) + "\n", gifasmName);

        for (int t = 0; t < datas.size(); t++) {
            MonochromeEncoder.n = t;
            //System.out.println(MonochromeEncoder.encode((String) datas.elementAt(t)));

            String dataFound = (String) datas.elementAt(t);
            if(!dataFound.startsWith("<referrer>"))
                appendStrToFile(MonochromeEncoder.encode((String) datas.elementAt(t)) + "\n", gifasmName);
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
