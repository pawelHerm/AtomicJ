
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.gui;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.renderer.PaintScale;

public class GradientPaintScale implements PaintScale
{
    private final Color overflowColor;
    private final Color underflowColor;

    private final ColorGradient colorGradient;

    private final double lowerBound;
    private final double upperBound;
    private final double rangeLength;

    public GradientPaintScale(double lowerBound, double upperBound, ColorGradient colorGradient, Color underflowColor, Color overflowColor)
    {
        if(upperBound<lowerBound)
        {
            throw new IllegalArgumentException("LowerBound should be smaller than upperBound");
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.colorGradient = colorGradient;
        this.underflowColor = underflowColor;
        this.overflowColor= overflowColor;
        this.rangeLength = upperBound - lowerBound;
    }

    public ColorGradient getGradient()
    {
        return colorGradient;
    }

    @Override
    public double getLowerBound() 
    {
        return lowerBound;
    }

    @Override
    public Paint getPaint(double val) 
    {		
        if(val<lowerBound)
        {
            return underflowColor;
        }
        else if(val > upperBound)
        {
            return overflowColor;
        }
        else
        {
            double f = (val - lowerBound)/rangeLength;

            return colorGradient.getColor(f);
        }
    }

    public int getColorInteger(double val)
    {
        int colorInt;
        if(val<lowerBound)
        {
            colorInt = underflowColor.getRGB();
        }
        else if(val > upperBound)
        {
            colorInt =  overflowColor.getRGB();
        }
        else
        {
            double f = (val - lowerBound)/rangeLength;

            colorInt = colorGradient.getColorInt(f);
        }

        return colorInt;
    }


    public short getIndex(double val)
    {
        short colorInt = (short)colorGradient.getPaletteSize();
        if(val<lowerBound)
        {
            return colorInt;
        }
        else if(val > upperBound)
        {
            return (short)(colorInt + 1);
        }
        else
        {
            double f = (val - lowerBound)/rangeLength;

            return colorGradient.getIndex(f);
        }

    }

    public int[] getLooup()
    {
        int oldSize = colorGradient.getPaletteSize();

        int[] original = colorGradient.getColorInts();
        int[] copied = new int[oldSize + 2];

        System.arraycopy(original, 0, copied, 0, oldSize);
        copied[oldSize] = underflowColor.getRGB();
        copied[oldSize + 1] = overflowColor.getRGB();

        return copied;
    }

    @Override
    public double getUpperBound() 
    {
        return upperBound;
    }

    public int blend(int color1,int color2, int factor)
    {
        int f1=255-factor;
        int alpha = (((color1 >> 24)*f1 + (color2 >> 24)*factor)/255 <<24);

        return ((((((color1 & 0x00FF00FF)*f1 + (color2&0x00FF00FF)*factor )  &0xFF00FF00  )  | (   ( (color1&0x00FF00)*f1 + (color2&0x00FF00)*factor )  &0x00FF0000  )   ) >>8 ) & 0x00FFFFFF| alpha);
    }

    public static int blend2(int color1, int color2, int f2)
    {
        int f1 = 255-f2;


        int alpha1 = (color1 >>> 24);
        int alpha2 = (color2 >>> 24);

        int red1 = ((color1 & 0x00FF0000)>> 16);
        int red2 = ((color2 & 0x00FF0000)>> 16);

        int green1 = ((color1 & 0x0000FF00)>> 8);
        int green2 = ((color2 & 0x0000FF00)>> 8);

        int blue1 = (color1 & 0x000000FF);
        int blue2 = (color2 & 0x000000FF);

        int result = ((alpha1*f1 + alpha2*f2)/255 <<24) | ((red1*f1 + red2*f2)/255 <<16) | 
                ((green1*f1 + green2*f2)/255  << 8) | ((blue1*f1 + blue2*f2)/255);

        return result;
    }
}
