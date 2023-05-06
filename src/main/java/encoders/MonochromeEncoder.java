/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encoders;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author franc
 */
public class MonochromeEncoder {
//private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
//
//public static String bytesToHex(byte[] bytes) {
//    char[] hexChars = new char[bytes.length * 2];
//    for (int j = 0; j < bytes.length; j++) {
//        int v = bytes[j] & 0xFF;
//        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
//        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
//    }
//    //return new String(hexChars);
//    String ret="";
//    for (int t=0;t<hexChars.length;t++){
//        ret+= Character.toString(hexChars[t]) + ((t<hexChars.length-1)?",":"");
//    }
//    return ret;
//}

    private static String empty = "32";//"$20";
    private static String full = "160";// "$A0";
    
    public static void switchToExa(){
        empty="$20";
        full="$A0";
    }

    public static int n = 0;

    private static boolean notPetscii(String s) {
        return s.equals(empty) || s.equals(full);
    }

    private static boolean isPetscii(String s) {
        return !s.equals(empty) && !s.equals(full);
    }

    public static String trimFirstCarriages(String str) {
        return str.replaceFirst("\\n+", "");
    }

    private static String[] arrayTransform(String str) {
        String ret = str.replaceAll("(?i)byte", "");
        ret = ret.replaceAll("\\n", ",");
        ret = ret.replaceAll("\\r", ",");
        ret = trimFirstCarriages(ret);

        ret = ret.replaceAll("\\s", "");
        ret = ret.replaceAll(",,", ",");

        return ret.split(",");

        // return trimFirstCarriages(orig).replaceAll("\\n", ",").replaceAll("\\s","").replaceAll(",,", "").split(",");
    }

    private static int translateCtrlChar(boolean intoempties, boolean intopetsciis, boolean intofulls/*, boolean intopetsciisEquals*/) {
        if (intoempties) {
            return 0;
        }
        if (intofulls) {
            return 1;
        }
        if (intopetsciis) {
            return 2;
        }
       /* if (intopetsciisEquals) {
            return 3;
        }*/
        System.err.println("warning : not allowed values in  translateCtrlChar");
        return -1;
    }

//    public static String encodeFromFilePath(String filepath) throws IOException {
//        Path path = Paths.get(filepath);
//        byte[] origdata = Files.readAllBytes(path);
//        String data=bytesToHex(origdata);
//        
//        
//        return encode(data);
//    }
    public static String encode(String str) {
        String[] array = arrayTransform(str);
        return encode(array);

    }
    
    public static String encode(String[] array) {

        String ret = "";

        Vector petsciis = new Vector();
        Vector header = new Vector();

        //String currentchar = "";
        int l = array.length;
        int cont = 0;

        boolean intopetsciis = isPetscii(array[0]);
        boolean intoempties = array[0].equals(empty);
        boolean intofulls = array[0].equals(full);
        //boolean intopetsciisEquals = false;

        String headerstr = "";
        String petsciistr = "";

        for (int t = 0; t < l - 1; t++) {
            String currentchar = array[t];
            String nextchar = array[t + 1];

            cont++;
            boolean forceswitch = false;
            if (cont == 255) {
                forceswitch = true;
            }

            if ((forceswitch && currentchar.equals(empty)) || (nextchar.equals(empty) && !currentchar.equals(empty))) {

                //if (t==0) intoempties=true;
                header.add(translateCtrlChar(intoempties, intopetsciis, intofulls/*, intopetsciisEquals*/));//save previous state
                header.add("" + cont);

                intoempties = true;
                intopetsciis = false;
                intofulls = false;
            //    intopetsciisEquals = false;
                cont = 0;
            } else if ((forceswitch && currentchar.equals(full)) || (nextchar.equals(full) && !currentchar.equals(full))) {

                //if (t==0) intofulls=true;
                header.add(translateCtrlChar(intoempties, intopetsciis, intofulls/*, intopetsciisEquals*/));//save previous state
                header.add("" + cont);

                intoempties = false;
                intopetsciis = false;
                intofulls = true;
            //    intopetsciisEquals = false;
                cont = 0;
            } else if ((forceswitch && isPetscii(currentchar)) || (isPetscii(nextchar) && notPetscii(currentchar))) {

                //if (t==0) intopetsciis=true;
                header.add(translateCtrlChar(intoempties, intopetsciis, intofulls/*, intopetsciisEquals*/));//save previous state
                header.add("" + cont);

                intoempties = false;
                intopetsciis = true;
                intofulls = false;
          //      intopetsciisEquals = false;
                cont = 0;
            } /*else if (!intopetsciisEquals && intopetsciis && nextchar.equals(currentchar)) {
                
                header.add(translateCtrlChar(intoempties, intopetsciis, intofulls, intopetsciisEquals));//save previous state
                cont--;
                header.add("" + cont);
                //intopetsciisEquals = true;
                intopetsciis=false;

            }*/ /*else if (intopetsciisEquals && ( !nextchar.equals(currentchar))) {
                header.add(translateCtrlChar(intoempties, intopetsciis, intofulls, intopetsciisEquals));//save previous state
                cont--;
                header.add("" + cont);
                //intopetsciisEquals = false;
                cont = 0;
            }*/
            if (intopetsciis) {
                petsciis.add(nextchar);
               /* if (intopetsciisEquals) {
                    intopetsciis = false;
                }*/
            }

        }

        for (int t = 0; t < header.size(); t++) {
            headerstr += (header.elementAt(t) + (t < header.size() - 1 ? "," : ""));
        }
        for (int t = 0; t < petsciis.size(); t++) {
            petsciistr += (petsciis.elementAt(t) + (t < petsciis.size() - 1 ? "," : ""));
        }

        ret = "header" + n + "\n     byte " + headerstr + "\ncontrolbyte" + n + "\n     byte 100\npetsciis" + n + "\n     byte " + petsciistr;
        //int totalbytes = header.size() + petsciis.size() + 1;//1 is for the control byte
        //int originallength = l;

//alert('original length:'+originallength+'\ncompressed length:'+totalbytes+'\nnew length is '+((totalbytes/originallength)*100)+'% of the original one');
        return ret;
    }

    public static String getAsmPattern() {
        return ";Temp Memory Locations in Page Zero\n"
                + "ZeroTmpMem01            = $02\n"
                + "ZeroTmpMem02            = $2a\n"
                + "ZeroTmpMem03            = $52\n"
                + "ZeroTmpMem04            = $5a\n"
                + "ZeroPtr1Low             = $03\n"
                + "ZeroPtr1High            = $04\n"
                + "ZeroPtr2Low             = $05\n"
                + "ZeroPtr2High            = $06\n"
                + "\n"
                + "ColBase                 = $d800 \n"
                + "\n"
                + "\n"
                + "Empty                    = $20\n"
                + "Full                    = $A0\n"
                + "\n"+
                " \n"+
                "*=$0801 \n"+
                " \n"+
                "        BYTE    $0E, $08, $0A, $00, $9E, $20, $28,  $38, $31, $39, $32, $29, $00, $00, $00 \n"
                + "*=$2000\n"
                + "\n                        jsr     ExcludeKernalAndBasic"
                + "\n"
                + "                        lda     #5\n"
                + "                        sta     53281\n"
                + "                        sta     53280\n"
                + "\n"
                + "                        lda     #0\n"
                + "                        jsr     colorscreen\n"
                + "\n"
                + "\n"
                + "eternalloop\n"
                + "						<decodestream>\n"
                + "						\n"
                + "                        jmp eternalloop\n"
                + "\n"
                + "\n"
                + "                        \n"
                + "\n"
                + ";--------------------------------------------------------------------------------------------\n"
                + ";decodestream\n"
                + ";--------------------------------------------------------------------------------------------\n"
                + ";uses : ZeroTmpMem01 is the petsciis ram pointer\n"
                + ";       ZeroPtr1Low/High to advance on screen memory\n"
                + ";       ZeroTmpMem02 is the screen memory pointer (where I store Y)\n"
                + ";       ZeroTmpMem03 is the header ram pointer\n"
                + ";       ZeroTmpMem04 is the counter for the actual chunk to read from petsciis\n"
                + ";       ZeroPtr2Low/High to advance on petsciis ram\n"
                + ";--------------------------------------------------------------------------------------------\n"
                + "\n"
                + "defm    decodestream \n"
                + "\n"
                + "                lda     #</1\n"
                + "                sta     ZeroPtr2Low\n"
                + "                lda     #>/1\n"
                + "                sta     ZeroPtr2High\n"
                + "\n"
                + "\n"
                + "                lda     #</2\n"
                + "                sta     $fb\n"
                + "                lda     #>/2\n"
                + "                sta     $fc\n"
                + "\n"
                + "                jsr     decodestream_func\n"
                + "\n"
                + "        endm\n"
                + "\n"
                + "decodestream_func\n"
                + "\n"
                + "\n"
                + "\n"
                + "\n"
                + "\n"
                + "                lda     #<1024\n"
                + "                sta     ZeroPtr1Low\n"
                + "                lda     #>1024\n"
                + "                sta     ZeroPtr1High\n"
                + "\n"
                + "                ldy     #0\n"
                + "                sty     ZeroTmpMem02;is the screen memory pointer (where I store Y)\n"
                + "                sty     ZeroTmpMem03;header pointer\n"
                + "                sty     ZeroTmpMem01;petscii ram pointer\n"
                + "\n"
                + "\n"
                + "\n"
                + "                \n"
                + "decodeloop\n"
                + "\n"
                + "                ldy     ZeroTmpMem03;header pointer\n"
                + "                \n"
                + "                lda     ($fb),y\n"
                + "                cmp     #100\n"
                + "                beq     gotoExitDecodestream\n"
                + "                jmp     noExitDecodestream\n"
                + "\n"
                + "\n"
                + "gotoExitDecodestream; fucking branch too long\n"
                + "                jmp     exitDecodestream\n"
                + "\n"
                + "noExitDecodestream\n"
                + "\n"
                + "                cmp     #0\n"
                + "                beq     skiproutine\n"
                + "                cmp     #1\n"
                + "                beq     fullroutine\n"
                + "\n"
                + "petsciiroutine\n"
                + "\n"
                + "                jsr     advanceHeader; increase ZeroTmpMem03 and if 255 is overcame it increments the vector $fb/$fc\n"
                + "                ldy     ZeroTmpMem03; y=header pointer\n"
                + "\n"
                + "                lda     ($fb),y; a=number of petscii to show (they have to be less than 256 for single scan)  \n"
                + "                sta     ZeroTmpMem04; ZeroTmpMem04 now is the counter of how many reads I have to do from petsciis\n"
                + "\n"
                + "petsciiroutineloop\n"
                + "                ldy     ZeroTmpMem01; petscii ram pointer\n"
                + "                lda     (ZeroPtr2Low),y; //get the petscii. petscii ram pointed by ZeroPtr2Low/High vector\n"
                + "\n"
                + "                ldy     ZeroTmpMem02; screen memory pointer\n"
                + "                sta     (ZeroPtr1Low),y\n"
                + "\n"
                + "                ;now I have to increase ZeroTmpMem01 and ZeroTmpMem02\n"
                + "                inc     ZeroTmpMem01; petscii ram pointer\n"
                + "                lda     ZeroTmpMem01\n"
                + "                cmp     #200\n"
                + "                bne     nopetscii200\n"
                + "                \n"
                + "                jsr     advance200petsciis\n"
                + "nopetscii200\n"
                + "                \n"
                + "                inc     ZeroTmpMem02; since ZeroTmpMem02 is the screen memory pointer, when >200 I increase the basis ZeroPtr1Low/High\n"
                + "                lda     ZeroTmpMem02\n"
                + "                cmp     #200\n"
                + "                bne     dontupdatescreenmemory\n"
                + "\n"
                + "\n"
                + "                jsr     advance200; if here, the screen memory pointer has gone over 200, so I update the basis vector\n"
                + "\n"
                + "dontupdatescreenmemory\n"
                + "\n"
                + "                dec     ZeroTmpMem04\n"
                + "                bne     petsciiroutineloop\n"
                + "\n"
                + "                \n"
                + "                jsr     advanceHeader;prepare header pointers for the next stage\n"
                + "\n"
                + "                jmp     decodeloop                \n"
                + "\n"
                + "\n"
                + "\n"
                + "skiproutine\n"
                + "\n"
                + "                jsr     advanceHeader\n"
                + "                ldy     ZeroTmpMem03; y=header pointer\n"
                + "\n"
                + "                lda     ($fb),y; a=number of empty spaces to show (they have to be less than 256 for single scan)  \n"
                + "                sta     ZeroTmpMem04; ZeroTmpMem04 now is the counter of how many reads I have to do from petsciis\n"
                + "\n"
                + "skiproutineloop\n"
                + "\n"
                + "                lda     #Empty     ;empty space\n"
                + "                ldy     ZeroTmpMem02; screen memory pointer\n"
                + "                sta     (ZeroPtr1Low),y;writing char on screen memory\n"
                + "\n"
                + "                inc     ZeroTmpMem02; since ZeroTmpMem02 is the screen memory pointer, when >200 I increase the basis ZeroPtr1Low/High\n"
                + "                lda     ZeroTmpMem02\n"
                + "                cmp     #200\n"
                + "                bne     notempty200\n"
                + "\n"
                + "                jsr     advance200\n"
                + "\n"
                + "notempty200\n"
                + "                dec     ZeroTmpMem04\n"
                + "                bne     skiproutineloop\n"
                + "\n"
                + "\n"
                + "                jsr     advanceHeader\n"
                + "                jmp     decodeloop\n"
                + "\n"
                + "\n"
                + "fullroutine\n"
                + "                jsr     advanceHeader\n"
                + "                ldy     ZeroTmpMem03; y=header pointer\n"
                + "\n"
                + "                lda     ($fb),y; a=number of full spaces to show (they have to be less than 256 for single scan)  \n"
                + "                sta     ZeroTmpMem04; ZeroTmpMem04 now is the counter of how many reads I have to do from petsciis\n"
                + "\n"
                + "fullroutineloop\n"
                + "\n"
                + "                lda     #Full     ;empty space\n"
                + "                ldy     ZeroTmpMem02; screen memory pointer\n"
                + "                sta     (ZeroPtr1Low),y;writing char on screen memory\n"
                + "\n"
                + "                inc     ZeroTmpMem02; since ZeroTmpMem02 is the screen memory pointer, when >200 I increase the basis ZeroPtr1Low/High\n"
                + "                lda     ZeroTmpMem02\n"
                + "                cmp     #200\n"
                + "                bne     notadvance200\n"
                + "\n"
                + "                jsr     advance200\n"
                + "notadvance200\n"
                + "\n"
                + "                dec     ZeroTmpMem04\n"
                + "                bne     fullroutineloop\n"
                + "\n"
                + "\n"
                + "                jsr     advanceHeader\n"
                + "                jmp     decodeloop\n"
                + "exitDecodestream\n"
                + "\n"
                + "                rts\n"
                + "                \n"
                + "\n"
                + ";------------------------------------\n"
                + ";advanceHeader\n"
                + ";------------------------------------\n"
                + "advanceHeader\n"
                + "                        \n"
                + "\n"
                + "                        inc     ZeroTmpMem03\n"
                + "                        lda     ZeroTmpMem03\n"
                + "                        cmp     #200\n"
                + "                        bne     exitAdvanceHeader\n"
                + "\n"
                + "                        lda     #0\n"
                + "                        sta     ZeroTmpMem03\n"
                + "\n"
                + "\n"
                + "                \n"
                + "                        clc                             ; clear carry\n"
                + "                        lda $fb\n"
                + "                        adc #200\n"
                + "                        sta $fb                       ; store sum of LSBs\n"
                + "                        lda #0\n"
                + "                        adc $fc                      ; add the MSBs using carry from\n"
                + "                        sta $fc                \n"
                + "exitAdvanceHeader\n"
                + "                        rts\n"
                + "\n"
                + "\n"
                + "              \n"
                + "\n"
                + "\n"
                + ";------------------------------------\n"
                + ";advance200 and advance200petsciis\n"
                + ";------------------------------------\n"
                + "\n"
                + "\n"
                + "advance200petsciis\n"
                + "                        lda     #0\n"
                + "                        sta     ZeroTmpMem01\n"
                + "\n"
                + "                        clc                             ; clear carry\n"
                + "                        lda ZeroPtr2Low\n"
                + "                        adc #200\n"
                + "                        sta ZeroPtr2Low                       ; store sum of LSBs\n"
                + "                        lda #0\n"
                + "                        adc ZeroPtr2High                      ; add the MSBs using carry from\n"
                + "                        sta ZeroPtr2High                \n"
                + "\n"
                + "                        rts\n"
                + "\n"
                + "\n"
                + "advance200\n"
                + "\n"
                + "\n"
                + "                        lda     #0\n"
                + "                        sta     ZeroTmpMem02\n"
                + "\n"
                + "                        clc                             ; clear carry\n"
                + "                        lda ZeroPtr1Low\n"
                + "                        adc #200\n"
                + "                        sta ZeroPtr1Low                       ; store sum of LSBs\n"
                + "                        lda #0\n"
                + "                        adc ZeroPtr1High                      ; add the MSBs using carry from\n"
                + "                        sta ZeroPtr1High                \n"
                + "\n"
                + "                        rts\n"
                + "\n"
                + "						\n"
                + ";---------------------------------------------------------------\n"
                + ";colorscreen: colors the screen of the value in a\n"
                + ";---------------------------------------------------------------\n"
                + "\n"
                + "colorscreen\n"
                + "\n"
                + ";normal colouring\n"
                + "\n"
                + "                       \n"
                + "\n"
                + "                        ldx                     #0\n"
                + "\n"
                + "colfill1\n"
                + "                        sta                     ColBase,x\n"
                + "                        sta                     Colbase+200,x\n"
                + "                        sta                     Colbase+400,x\n"
                + "                        sta                     Colbase+600,x\n"
                + "                        sta                     Colbase+800,x\n"
                + "                        inx\n"
                + "                        cpx                     #200\n"
                + "                        bne                     colfill1\n"
                + "\n"
                + "\n"
                + "                        rts"+
                " \n"+
                ";*******************  \n"+
                "; Exclude Kernal & Basic  \n"+
                "; thanks to Emanuele Bonin  \n"+                
                ";*******************  \n"+
                "ExcludeKernalAndBasic  \n"+
                "            sei  \n"+
                "            ldy #$7f                ; $7f = %01111111 maschera per disattivare  \n"+
                "                                    ; gli interrupt nei chip CIA 1 e 2  \n"+
                "            sty $dc0d               ; Disabilitazione IRQs CIA1  \n"+
                "            sty $dd0d               ; Disabilitazione IRQs CIA2  \n"+
                "            lda $dc0d               ; pulizia di eventuali IRQs in coda CIA1  \n"+
                "            lda $dd0d               ; pulizia di eventuali IRQs in coda CIA2  \n"+
                "  \n"+
                "  \n"+
                "                                    ; Disabilitazione del KERNAL e del BASIC  \n"+
                "            lda #%00110101          ; Carico la maschera ($35) per configurare come  \n"+
                "                                    ; il C=64 deve vedere la memoria RAM  \n"+
                "                                    ; mettendo 01 sui bit 0 e 1 escludiamo  \n"+
                "                                    ; le ROM BASIC e KERNAL  \n"+
                "            sta $01                 ; il byte $0001 è quello usato per la configurazione  \n"+
                "  \n"+
                "            cli  \n"+
                "            rts \n";

    }

    public static void main(String[] args) {

        String test = "BYTE    $20,$20,$20,$20,$20,$20,$20,$21,$22,$23,$23,$23,$22,$21,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$A0,$A0,$20,$20,$A0,$A0,$A0,$20,$20,$A0,$20,$20,$20,$A0,$20,$A0,$A0,$A0,$A0,$A0,$20,$A0,$A0,$20,$20,$20,$A0,$A0,$A0,$20,$A0,$20,$20,$20,$A0,$20,$20,$A0,$20 \n"
                + "BYTE    $A0,$20,$20,$A0,$20,$A0,$20,$20,$A0,$20,$A0,$20,$20,$20,$A0,$20,$20,$20,$A0,$20,$20,$20,$A0,$20,$A0,$20,$20,$A0,$20,$20,$20,$A0,$A0,$20,$A0,$A0,$20,$A0,$20,$A0 \n"
                + "BYTE    $A0,$20,$20,$20,$20,$A0,$20,$20,$A0,$20,$A0,$20,$20,$20,$A0,$20,$20,$20,$A0,$20,$20,$20,$A0,$20,$20,$A0,$20,$A0,$20,$20,$20,$A0,$20,$A0,$20,$A0,$20,$A0,$20,$A0 \n"
                + "BYTE    $20,$A0,$A0,$20,$20,$A0,$A0,$A0,$20,$20,$A0,$20,$20,$20,$A0,$20,$20,$20,$A0,$20,$20,$20,$A0,$20,$20,$A0,$20,$A0,$A0,$20,$20,$A0,$20,$20,$20,$A0,$20,$A0,$20,$A0 \n"
                + "BYTE    $20,$20,$20,$A0,$20,$A0,$20,$20,$20,$20,$A0,$20,$20,$20,$A0,$20,$20,$20,$A0,$20,$20,$20,$A0,$20,$20,$A0,$20,$A0,$20,$20,$20,$A0,$20,$20,$20,$A0,$20,$A0,$20,$A0 \n"
                + "BYTE    $A0,$20,$20,$A0,$20,$A0,$20,$20,$20,$20,$A0,$20,$20,$20,$A0,$20,$20,$20,$A0,$20,$20,$20,$A0,$20,$A0,$20,$20,$A0,$20,$20,$20,$A0,$20,$20,$20,$A0,$20,$A0,$20,$A0 \n"
                + "BYTE    $20,$A0,$A0,$20,$20,$A0,$20,$20,$20,$20,$A0,$A0,$A0,$20,$A0,$20,$20,$20,$A0,$20,$20,$20,$A0,$A0,$20,$20,$20,$A0,$A0,$A0,$20,$A0,$20,$20,$20,$A0,$20,$20,$A0,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n"
                + "BYTE    $20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20,$20 \n";
        switchToExa();
        System.out.println(encode(test));
    }
}
