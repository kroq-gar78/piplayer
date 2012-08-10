/* 
 * PiCalculator.java
 * Copyright 2012 Aditya Vaidya <kroq.gar78@gmail.com>
 * 
 * Calculates the digits of Pi and then converts it to base 12 (digits 0 through B).
 * 
 * This program uses the Chudnovsky algorithm (in which I take no claim
 * by implementing it in this program). The formula is taken from here:
 * http://en.wikipedia.org/wiki/Chudnovsky_algorithm
 * 
 * This file is part of PiPlayer.
 * 
 * PiPlayer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * PiPlayer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PiPlayer.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class PiCalculator
{
    static final BigDecimal FOUR = new BigDecimal("4");
    
    static final int roundingMode = BigDecimal.ROUND_HALF_EVEN;
    
    static BigDecimal atan1_5, atan1_239, pi;
    
    public static int factorial(int n) throws IllegalArgumentException
    {
        if(n==0) return 1;
        if(n<0) throw new IllegalArgumentException("n less than 0. n = " + n);
        int ans = 1;
        for(int i = 2; i <= n; i++)
        {
            ans*=i;
        }
        return ans;
    }
    
    public static class ArctanInvThread extends Thread
    {
        public ArctanInvThread(int invx, int scale)
        {
            this.invx = invx;
            this.scale = scale;
        }
        
        public void run()
        {
            this.result = atan(invx,scale);
        }
        
        int invx, scale;
        public BigDecimal result;
    }
    
    public static BigDecimal pow(BigDecimal n1, BigDecimal n2) throws Exception
    {
        int n2sign = n2.signum();
        BigDecimal result = BigDecimal.ONE;
        try
        {
            // do n1^n2 = n1^(A+B) = n1^A*n1^B (where B = remainder)
            double dn1 = n1.doubleValue();
            // compare dn1 to n1 to make sure they're the same
            if(! new BigDecimal(dn1).equals(n1)) throw new Exception();
            
            n2 = n2.multiply(new BigDecimal(n2sign));
            BigDecimal n2remainder = n2.remainder(BigDecimal.ONE);
            BigDecimal n2intPart = n2.subtract(n2remainder);
            
            BigDecimal intPow = n1.pow(n2intPart.intValueExact());
            BigDecimal doublePow = new BigDecimal(Math.pow(dn1,n2remainder.doubleValue()));
            
            result = intPow.multiply(doublePow);
        }
        catch(Exception e) { e.printStackTrace(); }
        
        if(n2sign==-1)
            result = BigDecimal.ONE.divide(result);
        
        return result;
    }
    
    public static void main(String[] args) throws Exception
    {
        int digits = 1000;
        try
        {
            if( args.length > 0 ) digits = Integer.parseInt(args[0]);
        }
        catch(NumberFormatException e)
        {
            System.err.println("Please input an integer number of base 10 digits, if any.");
            System.exit(1);
        }
        
        // use the Chudnovsky Algorithm to calculate the reciprocal of Pi
        /*int maxIter = 3;
        for(int i = 0; i < maxIter; i++)
        {
            int sign = ((i&1)==0?1:-1); // take the sign as (-1)^k, but efficiently
            BigDecimal term = new BigDecimal(sign);
            term = term.multiply( new BigDecimal(factorial(6*i)));
            term = term.multiply(new BigDecimal(13591409 + 545140134*i));
            term = term.divide(new BigDecimal(factorial(3*i)));
            term = term.divide(new BigDecimal(factorial(i)).pow(3));
            System.out.println(Math.pow(640320.0,3.0*i+1.5));
            //term = term.divide(new BigDecimal(Math.pow(640320.0,3.0*i+1.5),MathContext.DECIMAL128));
            term = term.divide(pow(new BigDecimal(640320),new BigDecimal(i*3+1.5)));
            term = term.multiply(new BigDecimal(12));
            piRecip = piRecip.add(term);
        }*/
        
        //System.out.println((atanInvInt(5).multiply(new BigDecimal(4)).subtract(atanInvInt(239))).multiply(new BigDecimal(4)));
        
        long stime = System.currentTimeMillis();
        
        /* Compute pi in base 10
         * 
         * Optimize this process by calculating atan(1/5) and atan(1/239)
         * in separate threads
         */
        ArctanInvThread atan1_5_thread = new ArctanInvThread(5, digits+5);
        ArctanInvThread atan1_239_thread = new ArctanInvThread(239, digits+5);
        atan1_5_thread.start();
        atan1_239_thread.start();
        atan1_5_thread.join();
        atan1_239_thread.join();
        atan1_5 = atan1_5_thread.result;
        atan1_239 = atan1_239_thread.result;
        /*atan1_5 = atan(5, digits+5);
        atan1_239 = atan(239, digits+5);*/
        pi = atan1_5.multiply(FOUR).subtract(atan1_239).multiply(FOUR);
        pi.setScale(digits, BigDecimal.ROUND_HALF_UP);
        //long etime = System.currentTimeMillis();
        
        //System.out.println("Base 10: " + pi);
        
        // then convert pi to base 12
        
        /* NOTE: This could be optimized by multithreading. Two (or more)
         * threads could be run at the same time, with one thread being
         * ahead of the other(s). If the ahead threads can divide without
         * the value (the base 12 digit) being over B, then force all threads
         * to skip to the point where the ahead thread is. If it can't
         * divide evenly, tell it to wait for the thread immediately behind
         * it to get to where it is possible to get that digit.
         */
        
        String pi12 = "3.";
        System.out.print("3.");
        BigDecimal piFrac = pi.subtract(new BigDecimal("3"));
        int i = -1;
        BigDecimal TWELVE = new BigDecimal("12");
        BigDecimal pow12 = BigDecimal.ONE.divide(TWELVE, digits+5, roundingMode);
        while(piFrac.compareTo(BigDecimal.ZERO) == 1)
        {
            while(pow12.compareTo(piFrac) == 1) // find the negative exponent
            {
                i--;
                pow12 = pow12.divide(TWELVE, digits+10, roundingMode);
                //System.out.println(i);
                pi12 += "0";
                System.out.print("0");
            }
            // subtract the negative exponent
            //System.out.println(piFrac);
            //System.out.println("Doing stuff");
            BigDecimal remainder = piFrac.remainder(pow12);
            String number = Integer.toString(piFrac.subtract(remainder).divide(pow12, digits+5, roundingMode).intValue(),12);
            pi12 += number;
            System.out.print(number);
            piFrac = remainder;
            i--;
            pow12 = pow12.divide(TWELVE, digits+10, roundingMode);
            
            //System.out.println(i);
        }
        
        System.out.println("\n" + Double.toString(System.currentTimeMillis()-stime) + " ms execution time");
        
        //System.out.println(pi12);
        
        //System.out.println((double)(etime - stime)/1000 + " secs for " + digits + " digits.");
        
        //System.out.println(piRecip.pow(-1));
    }
    
    public static BigDecimal atanInvInt(int x)
    {
        BigDecimal result = BigDecimal.ONE.divide(BigDecimal.valueOf(x));
        int xsq = x*x;
        
        int div = 1; // divisor
        BigDecimal term = new BigDecimal(result.doubleValue());
        BigDecimal termTmp;
        while(term.doubleValue()!=0.0)
        {
            div += 2;
            term = term.divide(new BigDecimal(xsq));
            result = result.subtract(term.divide(BigDecimal.valueOf(div)));
            
            div += 2;
            term = term.divide(new BigDecimal(xsq));
            result = result.add(term.divide(BigDecimal.valueOf(div)));
        }
        
        return result;
    }
    
    public static BigDecimal atan(int inverseX, int scale)
    {
        BigDecimal result, numer, term; // result, numerator, term
        BigDecimal invx = BigDecimal.valueOf(inverseX);
        BigDecimal invx2 = BigDecimal.valueOf(inverseX*inverseX);
        numer = BigDecimal.ONE.divide(invx, scale, roundingMode);
        
        result = numer;
        int i = 1;
        do
        {
            numer = numer.divide(invx2, scale, roundingMode);
            int denom = (i<<1)+1;
            term = numer.divide(BigDecimal.valueOf(denom), scale, roundingMode);
            result = result.add(term.multiply(BigDecimal.valueOf(((i&1)==1?-1:1))));
            i++;
        }
        while(term.compareTo(BigDecimal.ZERO) != 0);
        return result;
    }
}
