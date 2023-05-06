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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import encoders.MonochromeEncoder;
import encoders.Screenshot;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author franc
 */
public class Gif2Petscii {

    /*private static byte[] charsRaw;
    private static byte[] coloursRaw;*/
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

    private static void getHelp() {

        String h = "Gif2Petscii - create ASM code for monochromatic Petscii animations from a gif\n";
        h += "\nby Francesco Clementoni aka Arturo Dente aka Back to the 8 bit\n"
                + "\n--------------------------------------------------------------\n"
                + "usage: java -jar Gif2Petscii.jar <options>, where options are:\n"
                + "\n-s <source gif file> ->the gif to convert (REQUIRED)"
                + "\n-c <number> -> the number from where to start the labels for the petscii ram area in the asm code;it's useful if you want to combine more gifs together, starting from the ending of the previous one";

        System.out.println(h);
    }

    public static void main(String[] args) {

        //Petsciiator.fileList=new Vector();
        Vector datas = new Vector();
        int deltacount = 0;
        //String path = args[0];

        String path = getPropFromArgs("-s", args);
        if (args == null || path.trim().equals("")) {
            getHelp();
            System.exit(1);
        }

        String c = getPropFromArgs("-c", args);
        if (!c.equals("")) {
            deltacount = Integer.valueOf(c);
        }

        String f = getPropFromArgs("-f", args);
        boolean isMono = true;
        if (!f.equals("")) {
            if (f.equals("colour")) {
                isMono = false;
            } else if (f.equals("mono")) {
                isMono = true;
            } else {
                getHelp();
                System.exit(1);
            }
        }

        GifDecoder decoder = new GifDecoder();
        Vector pngs = decoder.stripGifInPngs(path);
        //now path has given a lot of png files, I have to elaborate each one

        if (isMono) {
            pngsToMonoPetsciis(datas, pngs);
        } else {
            pngsToColourPetsciis(datas, pngs);
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

        if (isMono) {

            String mainloop = "                         decodestream petsciis<n>,header<n>\n";
            String loopstr = "";
            for (int t = 0; t < datas.size(); t++) {
                String dataFound = (String) datas.elementAt(t);
                if (!dataFound.startsWith("<referrer>")) {
                    loopstr += mainloop.replaceAll("<n>", "" + (new Integer(t + deltacount)).toString()) + "\n";
                } else {
                    loopstr += mainloop.replaceAll("<n>", "" + (int) ((int) deltacount + (int) Integer.parseInt((dataFound.split("</referrer>")[0]).split("<referrer>")[1]))) + "\n";
                }
            }
            //System.out.println(MonochromeEncoder.getAsmPattern().replaceFirst("<decodestream>", loopstr));
            appendStrToFile(MonochromeEncoder.getAsmPattern().replaceFirst("<decodestream>", loopstr) + "\n", gifasmName);

            for (int t = 0; t < datas.size(); t++) {
                MonochromeEncoder.n = t + deltacount;
                //System.out.println(MonochromeEncoder.encode((String) datas.elementAt(t)));

                String dataFound = (String) datas.elementAt(t);
                if (!dataFound.startsWith("<referrer>")) {
                    appendStrToFile(MonochromeEncoder.encode((String) datas.elementAt(t)) + "\n", gifasmName);
                }
            }
        } else {
//colour
            File crunch = new File("tscrunch.exe");
            File decrunch = new File("decrunch.asm");
           // File mainAsm = new File("main.asm");

            if (crunch.exists()) {
                try {
                    crunch.delete();
                } catch (Exception del) {
                    System.out.println("error " + del.toString());
                }
            }
            if (decrunch.exists()) {
                try {
                    decrunch.delete();
                } catch (Exception del) {
                    System.out.println("error " + del.toString());
                }
            }
          /*  if (mainAsm.exists()) {
                try {
                    mainAsm.delete();
                } catch (Exception del) {
                    System.out.println("error " + del.toString());
                }
            }*/

            try {
                crunch.createNewFile();
                decrunch.createNewFile();
                Gif2Petscii tmp = new Gif2Petscii();
                URL tscrunchUrl = tmp.getClass().getResource("/tscrunch.exe");
                URL decrunchUrl = tmp.getClass().getResource("/decrunch.asm");

                FileUtils.copyURLToFile(tscrunchUrl, crunch);//copy tscrunch.exe in the same folder of this jar
                FileUtils.copyURLToFile(decrunchUrl, decrunch);//copy decrunch.asm in the same folder of this jar

                File batchFile = new File("cruncher.bat");
                if (batchFile.exists()) {
                    batchFile.delete();
                }
                batchFile.createNewFile();

                
                appendStrToFile(encoders.ColourEncoder.getAsmPattern(datas.size(),deltacount),gifasmName);

                for (int t = 0; t < datas.size(); t++) {
                    int index=deltacount+t;
                    byte[] charsFound = ((Screenshot) datas.elementAt(t)).getCharsRaw();
                    byte[] coloursFound = ((Screenshot) datas.elementAt(t)).getColoursRaw();

                    File tocrunchChars = new File(index + "Chars.bin");
                    tocrunchChars.createNewFile();
                    Files.write(Paths.get(index + "Chars.bin"), charsFound);

                    File tocrunchColours = new File(index + "Colours.bin");
                    tocrunchColours.createNewFile();
                    Files.write(Paths.get(index + "Colours.bin"), coloursFound);

                    //updating batch file to call in the end
                    appendStrToFile("tscrunch " + index + "Chars.bin " + index + "Charscrunch.bin\n", "cruncher.bat");
                    appendStrToFile("tscrunch " + index + "Colours.bin " + index + "Colourscrunch.bin\n", "cruncher.bat");
                    appendStrToFile("del " + index + "Chars.bin\n", "cruncher.bat");
                    appendStrToFile("del " + index + "Colours.bin\n", "cruncher.bat");
                }

                Runtime runtime = Runtime.getRuntime();
                try {
                    Process process = runtime.exec("cmd /c start cruncher.bat");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (IOException ex) {
                Logger.getLogger(Gif2Petscii.class.getName()).log(Level.SEVERE, null, ex);
            } finally {

            }
        }
    }

    protected static void pngsToPetsciis(boolean mono, Vector datas, Vector pngs) {

        for (int t = 0; t < pngs.size(); t++) {
            String actualPath = (String) pngs.elementAt(t);
            File tmp = new File(actualPath);
            String[] petsciiatorArgs = {"/format=asm" + ((!mono) ? "c" : ""), "/target=" + tmp.getParent(), actualPath};
            Petsciiator petsciiator = new Petsciiator(petsciiatorArgs);
            petsciiator.run();//affects also charsRaw and coloursRaw
            try {
                tmp.delete();
            } catch (Exception del) {

            }
            if (!mono) {
                datas.add(new Screenshot(convertIntArrayToByteArray(petsciiator.charsRaw), convertIntArrayToByteArray(petsciiator.coloursRaw)));

                //coloursRaw = convertIntArrayToByteArray(petsciiator.coloursRaw);
            } else {

                int pos = datas.indexOf(Petsciiator.stringRepresentation);//we don't waste kb if a ram portion is already there
                if (pos == -1) {
                    datas.add(Petsciiator.stringRepresentation);
                } else {
                    datas.add("<referrer>" + pos + "</referrer>");
                }
            }
        }
    }

    protected static void pngsToMonoPetsciis(Vector datas, Vector pngs) {
        pngsToPetsciis(true, datas, pngs);
    }

    protected static void pngsToColourPetsciis(Vector datas, Vector pngs) {
        pngsToPetsciis(false, datas, pngs);
    }

    public static byte[] convertIntArrayToByteArray(int[] input) {
        byte[] output = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = (byte) input[i];
        }
        return output;
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
