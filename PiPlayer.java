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
import java.util.LinkedList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class PiPlayer
{
    public static float SAMPLE_RATE = 8000f;
    public static int SAMPLE_LENGTH = 500; // length of tone in milliseconds
    public static int BUFFER_SIZE = 10; // how many tones can fit into one buffer
    
    public static double[] KEY = new double[10]; // C Major
    
    public static boolean EOF = false;
    
    protected static byte[] buf1 = null;
    protected static LinkedList<Byte> buf2 = null;
    
    private static class GeneratorThread implements Runnable
    {
        public GeneratorThread(BufferedReader pin)
        {
            this.pin = pin;
        }
        
        public void run()
        {
            try
            {
                int digitChar = 0;
                while((digitChar = pin.read()) != -1)
                {
                    while(buf2.size()>=(int)SAMPLE_RATE * SAMPLE_LENGTH / 1000 * BUFFER_SIZE*5) Thread.sleep(500);
                    if( (char)digitChar == '.' )
                    {
                        System.out.print('.');
                        continue;
                    }
                    int digit = Character.getNumericValue((char)digitChar);
                    System.out.print(digit);
                    byte[] newSound = getSound( KEY[digit], SAMPLE_LENGTH, 0.2 );
                    for( int i = 0; i < newSound.length; i++ )
                    {
                        buf2.add((Byte)newSound[i]);
                    }
                }
                PiPlayer.EOF = true;
            }
            catch( Exception e ) { e.printStackTrace(); }
        }
        
        // pin = pi in ;)
        private BufferedReader pin;
    }
    
    private static class PlayerThread implements Runnable
    {
        public PlayerThread()
        {
            digitCount = 0;
        }
        
        public void run()
        {
            try
            {
                while(!PiPlayer.EOF)
                {
                    while(buf2.size()<1000)
                    {
                        Thread.sleep(250);
                    }
                    for( int i = 0; i < buf1.length; i++ )
                    {
                        buf1[i] = buf2.poll().byteValue();
                    }
                    playSound(buf1);
                    digitCount++;
                }
            }
            catch( ArrayIndexOutOfBoundsException e ) {}
            catch( Exception e ) { e.printStackTrace(); }
        }
        
        private int digitCount;
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
        //double[] key = {e,cSharp,d,e,fSharp,g,a,b,cSharp,d}; // Random Major
        //KEY = {c*2,a,b,c,d,e,f,g,a*2,b*2}; // C Major
        KEY[0] = c*2;
        KEY[1] = a;
        KEY[2] = b;
        KEY[3] = c;
        KEY[4] = d;
        KEY[5] = e;
        KEY[6] = f;
        KEY[7] = g;
        KEY[8] = a*2;
        KEY[9] = b*2;
        
        int time = 125;
        int bufferSize = 3; 
        buf1 = new byte[(int)SAMPLE_RATE * SAMPLE_LENGTH / 1000 * bufferSize];
        buf2 = new LinkedList<Byte>();
        
        
        try
        {
            // open pi_1mil.txt; pin = pi in ;)
            BufferedReader pin = new BufferedReader( new FileReader("pi_1mil.txt") );
            
            Thread generatorThread = new Thread(new GeneratorThread(pin));
            Thread playerThread = new Thread(new PlayerThread());
            
            generatorThread.start();
            playerThread.start();
            
            /*BigInteger pi = new BigInteger(pin.readLine().replace(".",""));
            String piBase12 = pi.toString(12);
            System.out.println(piBase12);*/
        }
        catch(Exception ex) { ex.printStackTrace(); System.exit(1); }
    }
}
