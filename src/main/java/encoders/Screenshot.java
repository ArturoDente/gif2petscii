/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package encoders;

/**
 *
 * @author franc
 */
public class Screenshot {
    
    
    private byte[] charsRaw;
    private byte[] coloursRaw;

    public byte[] getCharsRaw() {
        return charsRaw;
    }

    public void setCharsRaw(byte[] charsRaw) {
        this.charsRaw = charsRaw;
    }

    public byte[] getColoursRaw() {
        return coloursRaw;
    }

    public void setColoursRaw(byte[] coloursRaw) {
        this.coloursRaw = coloursRaw;
    }

    public Screenshot(byte[] charsRaw, byte[] coloursRaw) {
        this.charsRaw = charsRaw;
        this.coloursRaw = coloursRaw;
    }
    
    public Screenshot(){
        
    }
    
    
}
