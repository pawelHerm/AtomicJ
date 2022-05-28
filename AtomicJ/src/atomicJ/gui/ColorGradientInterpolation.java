
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
import java.io.Serializable;
import java.util.Arrays;

public class ColorGradientInterpolation implements ColorGradient, Serializable
{
    private static final long serialVersionUID = 1L;

    private final int paletteSize;

    private final Color[] stopColors;
    private Color[] palette;
    private int[] paletteInts;
    private final float[] stopPositions;

    public ColorGradientInterpolation(Color[] colorStops, int paletteSize)
    {		
        this.stopColors = colorStops;	
        this.stopPositions = buildEquidistantStopPositions();
        this.paletteSize = paletteSize;		
        buildPalette();
    }

    public ColorGradientInterpolation(Color[] colorStops, float[] stopPositions, int paletteSize)
    {
        if(colorStops.length != stopPositions.length)
        {
            throw new IllegalArgumentException("The arrays 'colorStops' and 'stopPositions' must have the same length");
        }
        this.stopColors = colorStops;	
        this.stopPositions = stopPositions;
        this.paletteSize = paletteSize;		
        buildPalette();
    }

    @Override
    public int getPaletteSize()
    {
        return paletteSize;
    }

    @Override
    public int[] getColorInts()
    {
        return paletteInts;
    }

    @Override
    public boolean isPaletteResizable()
    {
        return true;
    }

    @Override
    public Color getStopColor(int index) 
    {
        return stopColors[index];
    }

    @Override
    public int getStopCount() 
    {
        int count = stopColors.length;
        return count;
    }

    @Override
    public Color[] getStopColors() 
    {
        return stopColors;
    }

    @Override
    public float[] getStopPositions()
    {
        return stopPositions;
    }

    @Override
    public Color getColor(double fraction) 
    {
        int index = (int) ((paletteSize - 1)*fraction);

        Color color = palette[index];
        return color;
    }

    @Override
    public int getColorInt(double fraction)
    {
        int index = (int)((paletteSize - 1)*fraction);
        int colorInt = paletteInts[index];
        return colorInt;
    }

    @Override
    public short getIndex(double fraction)
    {
        short index = (short)((paletteSize - 1)*fraction);
        return index;
    }

    private void buildPalette()
    {
        int stopsCount = stopColors.length;

        palette = new Color[paletteSize];
        paletteInts = new int[paletteSize];

        int palettePosition = 0;

        for(int i = 1; i<stopsCount; i++)
        {
            float f1 = stopPositions[i - 1];
            float f2 = stopPositions[i];
            float length = f2 - f1;
            Color c1 = stopColors[i - 1];
            Color c2 = stopColors[i];

            int paletteCurrentPosition = (int)Math.rint(f2*paletteSize);

            for(int j = palettePosition; j<paletteCurrentPosition; j++)
            {
                float x = (j/(paletteSize - 1.f) - f1)/length;
                int colorInt = blend2(c1.getRGB(), c2.getRGB(), x);
                paletteInts[j] = colorInt;
                palette[j] =  new Color(colorInt, true);
                //int x = (int)Math.rint(255*(j/(paletteSize - 1.f) - f1)/length);
                //palette[j] =  new Color(blend(c1.getRGB(), c2.getRGB(), x), true);
            }

            palettePosition = paletteCurrentPosition;
        }

    }

    public static int blend(int color1, int color2, int f2)
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

    public static int blend2(int rgb1, int rgb2, float f2)
    {
        // color components
        int a1, r1, g1, b1, da, dr, dg, db;

        // extract color components from packed integer
        a1 = (rgb1 >> 24) & 0xff;
        r1 = (rgb1 >> 16) & 0xff;
        g1 = (rgb1 >>  8) & 0xff;
        b1 = (rgb1      ) & 0xff;

        // calculate the total change in alpha, red, green, blue
        da = ((rgb2 >> 24) & 0xff) - a1;
        dr = ((rgb2 >> 16) & 0xff) - r1;
        dg = ((rgb2 >>  8) & 0xff) - g1;
        db = ((rgb2      ) & 0xff) - b1;

        // for each step in the interval calculate the in-between color by
        // multiplying the normalized current position by the total color
        // change (0.5 is added to prevent truncation round-off error)
        int result =
                (((int) ((a1 + da * f2) + 0.5) << 24)) |
                (((int) ((r1 + dr * f2) + 0.5) << 16)) |
                (((int) ((g1 + dg * f2) + 0.5) <<  8)) |
                (((int) ((b1 + db * f2) + 0.5)      ));           
        return result; 
    }


    private float[] buildEquidistantStopPositions()
    {
        int stopCount = stopColors.length;
        float[] stopPositions = new float[stopCount];

        for(int i = 0; i<stopCount; i++)
        {
            stopPositions[i] = i/(stopCount - 1.f);
        }

        return stopPositions;
    }

    @Override
    public boolean equals(Object ob)
    {
        if(ob instanceof ColorGradientInterpolation)
        {
            ColorGradientInterpolation that = (ColorGradientInterpolation)ob;
            boolean equals = (that.getPaletteSize() == this.getPaletteSize() && Arrays.equals(that.getStopColors(), this.getStopColors()) && Arrays.equals(that.getStopPositions(), this.getStopPositions()));
            return equals;
        }
        return false;
    }

    public static void print(ColorGradient gradient)
    {
        Color[] stopColors = gradient.getStopColors();
        float[] stopPositions = gradient.getStopPositions();

        System.out.print("new Color[] {");
        for(Color c: stopColors)
        {
            System.out.print("new Color(" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + "), ");
        }
        System.out.print("},");

        System.out.println();

        System.out.print("new float[] {");
        for(float f: stopPositions)
        {
            System.out.print(f + "f, ");
        }
        System.out.print("}");
    }
}
