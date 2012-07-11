/* PiPlayer.java
 * Created by Aditya Vaidya <kroq.gar78@gmail.com>
 * 
 * Plays the first 1 million digits of Pi based on a certain key (by 
 * default A Major).
 * 
 * All note frequencies taken from http://en.wikipedia.org/wiki/Piano_key_frequencies
 * 
 */

import java.io.*;
import java.util.*;
import javax.sound.sampled.*;

public class PiPlayer
{
    public static float SAMPLE_RATE = 8000f;
    
    public static void playSound( double hz, int millis, double vol ) throws LineUnavailableException
    {
        if(hz<=0) throw new IllegalArgumentException("Frequency <= 0 hz");
        if(millis<=0) throw new IllegalArgumentException("Duration <= 0 msecs");
        if(vol > 1.0 || vol < 0.0) throw new IllegalArgumentException("Volume not in range 0.0 - 1.0");
        
        byte[] buf = new byte[(int)SAMPLE_RATE * millis / 1000];
        
        for( int i = 0; i < buf.length; i++ )
        {
            double angle = i / (SAMPLE_RATE/hz) * 2.0 * Math.PI;
            buf[i] = (byte)(Math.sin(angle) * 127.0 * vol);
        }
        
        // shape front and back 10ms of the wave form
        for( int i = 0; i < SAMPLE_RATE/100.0 && i < buf.length / 2; i++ )
        {
            buf[i] = (byte)(buf[i] * i / (SAMPLE_RATE / 100.0));
            buf[buf.length-1-i] = (byte)(buf[buf.length-1-i] * i / (SAMPLE_RATE/100.0));
        }
        
        AudioFormat af = new AudioFormat(SAMPLE_RATE,8,1,true,false);
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af);
        sdl.start();
        sdl.write(buf,0,buf.length);
        sdl.drain();
        sdl.close();
    }
    
    public static double getFrequency(int halfStepsFromConcertA)
    {
        return Math.pow(2,(double)(halfStepsFromConcertA/12))*440;
    }
    
    public static void main( String[] args ) throws LineUnavailableException
    {
        // TONS of pitch definitions
        double[] naturals = {440.000,493.883,523.251,587.330,659.255,698.456,783.991};
        double a = naturals[0];
        double b = naturals[1];
        double c = naturals[2];
        double d = naturals[3];
        double e = naturals[4];
        double f = naturals[5];
        double g = naturals[6];
        
        double[] sharps = {466.164,c,554.365,622.254,f,739.989,830.609};
        double aSharp = sharps[0];
        double bSharp = sharps[1];
        double cSharp = sharps[2];
        double dSharp = sharps[3];
        double eSharp = sharps[4];
        double fSharp = sharps[5];
        double gSharp = sharps[6];
        
        double[] flats = {415.305,aSharp,b,cSharp,dSharp,e,fSharp};
        double aFlat = flats[0];
        double bFlat = flats[1];
        double cFlat = flats[2];
        double dFlat = flats[3];
        double eFlat = flats[4];
        double fFlat = flats[5];
        double gFlat = flats[6];
        
        // digit = index
        double[] key = {a,b,c,cSharp,d,e,f,fSharp,g,gSharp};
        
        // read pi_1mil.txt; pin = pi in ;)
        try
        {
            BufferedReader pin = new BufferedReader( new FileReader("pi_1mil.txt") );
            
            int digitChar;
            int digitCount = 0;
            while((digitChar = pin.read()) != -1)
            {
                if( (char)digitChar == '.' ) continue;
                int digit = Character.getNumericValue((char)digitChar);
                System.out.print(digit);
                playSound( key[digit] , 350 , 0.2 );
                digitCount++;
            }
        }
        catch(Exception ex) { ex.printStackTrace(); System.exit(1); }
        
        System.out.println();
        
        //System.out.println(getFrequency(12));
        //playSound(440.0,1000,0.8);
        //playSound(220,1000,0.8);
        
        /*for( int i = 0; i < naturals.length; i++ )
        {
            playSound(naturals[i],1000,0.5);
        }*/
    }
}
