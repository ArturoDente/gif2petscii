/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package encoders;

/**
 *
 * @author franc
 */
public class ColourEncoder {

    public static String getAsmLoop() {
        String ret = " \n"
                + "	:TS_DECRUNCH(compressed_data<t>,1024)  \n"
                + "    :TS_DECRUNCH(compressed_colours<t>,55296)  \n";
        return ret;
    }

    public static String getAsmClosing() {
        String ret = "	compressed_data<t>: \n"
                + "	.import binary \"<t>Charscrunch.bin\" \n"
                + " \n"
                + "    compressed_colours<t>: \n"
                + "	.import binary \"<t>Colourscrunch.bin\" \n";
        return ret;
    }

    public static String getAsmPattern(int n,int startfrom) {
        String ret = ".pc = $1000 \"test\" \n"
                + "    lda #0 \n"
                + "    sta 53281 \n"
                + "    sta 53280 \n"
                + " \n"
                + "animationloop: \n"
                + "	//decrunches data to screen \n";
        for (int t = startfrom; t < startfrom+n; t++) {
            ret += getAsmLoop().replaceAll("<t>", "" + t);
        }
        ret += " \n"
                + "	jmp animationloop \n"
                + " \n"
                + "	.align $100 \n"
                + "	#import  \"decrunch.asm\" \n";

        for (int t = startfrom; t < startfrom+n; t++) {
            ret += getAsmClosing().replaceAll("<t>", "" + t);
        }

        return ret;
    }
}
