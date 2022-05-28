
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2014 by Pawe³ Hermanowicz
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

package atomicJ.readers.asylum;

import java.util.ArrayList;
import java.util.List;

public class IgorUtilities
{
    public static final int MAXDIMS5 = 4; //maximal number of dimensions, only in version 5, earlier versions support only 1 dimension
    public static final int MAX_WAVE_NAME2 = 18;   // Maximum length of wave name in version 1 and 2 files. Does not include the trailing null.
    public static final int MAX_WAVE_NAME5 = 31;   // Maximum length of wave name in version 5 files. Does not include the trailing null.
    public static final int MAX_UNIT_CHARS = 3;

    public static List<String> extractChannelNames(String dimensionLabel, int channelCount)
    {
        List<String> channelNames = new ArrayList<>();

        int labelLength = dimensionLabel.length();
        int expectedLabelLength = (IgorUtilities.MAX_WAVE_NAME5 + 1)*(channelCount + 1);

        if(expectedLabelLength == labelLength)
        {
            for(int i = 0; i<channelCount; i++)
            {
                int beginIndex = (i + 1)*(IgorUtilities.MAX_WAVE_NAME5 + 1);
                int endIndex = (i + 2)*(IgorUtilities.MAX_WAVE_NAME5 + 1);

                String channelLabel = dimensionLabel.substring(beginIndex, endIndex).
                        replaceAll("\u0000+$|^\u0000+", "");
                channelNames.add(channelLabel);
            }
        }

        return channelNames;
    }

    public static int getDimensionCount(int[] nDim)      
    {
        for(int i = 0; i<nDim.length; i++)
        {                
            if(nDim[i] == 0)
            {
                return i;
            }
        }

        return nDim.length;
    }
}