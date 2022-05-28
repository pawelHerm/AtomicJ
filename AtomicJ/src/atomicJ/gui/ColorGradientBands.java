
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

public class ColorGradientBands implements ColorGradient, Serializable
{	
    private static final long serialVersionUID = 1L;

    private final Color[] stopColors;
    private final float[] stopPositions;
    private final float[] boundaryPostions;

    public ColorGradientBands(Color[] stopColors)
    {		
        this.stopColors = stopColors;	
        this.stopPositions = buildEquidistantStopPositions();
        this.boundaryPostions = buildBoundaryPositions();
    }

    public ColorGradientBands(Color[] stopColors, float[] stopPositions)
    {
        if(stopColors.length != stopPositions.length)
        {
            throw new IllegalArgumentException("The arrays 'colorStops' and 'stopPositions' must have the same length");
        }
        this.stopColors = stopColors;	
        this.stopPositions = stopPositions;
        this.boundaryPostions = buildBoundaryPositions();
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
    public int[] getColorInts()
    {
        int[] ints = new int[stopColors.length];

        for(int i = 0; i<stopColors.length; i++)
        {
            ints[i] = stopColors[i].getRGB();
        }

        return ints;
    }

    @Override
    public Color[] getStopColors() 
    {
        return stopColors;
    }

    @Override
    public Color getColor(double fraction) 
    {
        int index = stopPositions.length - 1;
        for(int i = 0;i<stopPositions.length - 1;i++)
        {						
            float boundary = boundaryPostions[i];			
            if(fraction<boundary)
            {
                index = i;
                break;
            }		
        }

        Color color = stopColors[index];
        return color;
    }

    @Override
    public int getColorInt(double fraction) 
    {
        int index = stopPositions.length - 1;
        for(int i = 0;i<stopPositions.length - 1;i++)
        {						
            float boundary = boundaryPostions[i];			
            if(fraction<boundary)
            {
                index = i;
                break;
            }		
        }

        Color color = stopColors[index];
        return color.getRGB();
    }

    @Override
    public short getIndex(double fraction)
    {
        short index = (short)(stopPositions.length - 1);
        for(short i = 0;i<stopPositions.length - 1;i++)
        {						
            float boundary = boundaryPostions[i];			
            if(fraction<boundary)
            {
                index = i;
                break;
            }		
        }

        return index;
    }

    @Override
    public int getPaletteSize() 
    {
        int paletteSize = stopColors.length;
        return paletteSize;
    }

    @Override
    public boolean isPaletteResizable()
    {
        return false;
    }

    private float[] buildBoundaryPositions()
    {
        float[] boundaryPostions = new float[stopPositions.length -1];

        for(int i = 0;i<stopPositions.length - 1;i++)
        {

            float p1 = stopPositions[i];
            float p2 = stopPositions[i + 1];

            float boundary = p1 + 0.5f*(p2 - p1);
            boundaryPostions[i] = boundary;				
        }

        return boundaryPostions;
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
        if(ob instanceof ColorGradientBands)
        {
            ColorGradientBands that = (ColorGradientBands)ob;
            if(Arrays.equals(that.getStopColors(), this.getStopColors()) && Arrays.equals(that.getStopPositions(), this.getStopPositions()))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public float[] getStopPositions() 
    {
        return stopPositions;
    }
}
