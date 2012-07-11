/* 
 * PiPlayer.java
 * Created by Aditya Vaidya <kroq.gar78@gmail.com>
 * 
 * Plays the first 1 million digits of Pi based on a certain key (what 
 * that key is, is yet to be determined :P)
 * 
 * Don't worry, it can also play Tau, for those who believe Pi is wrong.
 * Digits of Tau obtained from http://tauday.com/tau-digits.
 * 
 * All note frequencies taken from http://en.wikipedia.org/wiki/Piano_key_frequencies
 * 
 */

import java.math.BigInteger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class PiPlayer
{
    public static float SAMPLE_RATE = 8000f;
    
    protected static byte[] buf1 = null;
    protected static ArrayList<Byte> buf2 = null;
    
    private static class SoundGetter implements Runnable
    {
        public void run()
        {
            try
            {
                Thread.sleep(2000);
                System.out.println(PiPlayer.SAMPLE_RATE);
            }
            catch( InterruptedException e ) { e.printStackTrace(); }
        }
    }
    
    public static byte[] getSound( double hz, int millis, double vol ) throws LineUnavailableException
    {
        if(hz<=0) throw new IllegalArgumentException("Frequency <= 0 Hz");
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
        
        return buf;
    }
    
    public static void playSound( byte[] buf ) throws LineUnavailableException
    {
        AudioFormat af = new AudioFormat(SAMPLE_RATE,8,1,true,false);
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af);
        sdl.start();
        sdl.write(buf,0,buf.length);
        //System.out.println(buf.length);
        sdl.drain();
        sdl.close();
    }
    public static void playSound( double hz, int millis, double vol ) throws LineUnavailableException
    {
        playSound(getSound(hz,millis,vol));
    }
    
    public static double getFrequency(int halfStepsFromConcertA)
    {
        return Math.pow(2,(double)(halfStepsFromConcertA/12))*440;
    }
    
    public static void main( String[] args ) throws LineUnavailableException, InterruptedException
    {
        // TONS of pitch definitions; all is based on A = 440Hz
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
        //double[] key = {cSharp,a,b,cSharp,d,e,fSharp,gSharp,a*2,b*2}; // D Major
        double[] key = {c*2,a,b,c,d,e,f,g,a*2,b*2}; // C Major
        //double[] key = {e,cSharp,d,e,fSharp,g,a,b,cSharp,d}; // Random Major
        
        int time = 125;
        int bufferSize = 3; // how many tones can fit into one buffer
        buf1 = new byte[(int)SAMPLE_RATE * time / 1000 * bufferSize];
        buf2 = new ArrayList<Byte>();
        
        // read pi_1mil.txt; pin = pi in ;)
        try
        {
            BufferedReader pin = new BufferedReader( new FileReader("pi_1mil.txt") );
            
            Thread t = new Thread(new SoundGetter());
            t.start();
            
            int digitChar;
            int digitCount = 0;
            while((digitChar = pin.read()) != -1)
            {
                if( (char)digitChar == '.' )
                {
                    System.out.print('.');
                    continue;
                }
                int digit = Character.getNumericValue((char)digitChar);
                System.out.print(digit);
                byte[] newSound = getSound( key[digit], time, 0.2 );
                for( int i = 0; i < newSound.length; i++ )
                {
                    buf2.add((Byte)newSound[i]);
                }
                
                if(digitCount % bufferSize == 0 && digitCount > 0)
                {
                    for( int i = 0; i < buf1.length; i++ )
                    {
                        buf1[i] = buf2.get(i).byteValue();
                    }
                    playSound(buf1);
                    buf2 = new ArrayList<Byte>();
                }
                
                digitCount++;
            }
            
            /*BigInteger pi = new BigInteger(pin.readLine().replace(".",""));
            String piBase12 = pi.toString(12);
            System.out.println(piBase12);*/
        }
        catch(Exception ex) { ex.printStackTrace(); System.exit(1); }
    }
}
