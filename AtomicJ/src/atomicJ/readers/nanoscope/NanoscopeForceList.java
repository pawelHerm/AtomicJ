/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2018 by Pawe³ Hermanowicz
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

package atomicJ.readers.nanoscope;

//Fields \Hold Samples: 59523 i \Hold Time: 5 are read from \*Ciao force list to establish to position of hold segments and force curves, also the duration of hold segments.
//I've verified this modifying Nanoscope files in a hex editor and checking how those modifications affect the way Matlab Toolbox reads the files

public class NanoscopeForceList extends NanoscopeHeaderSection
{   
    private static final String IDENTIFIER = "\\*Ciao force list";
    private static final String HOLD_SAMPLES = "hold samples";
    private static final String HOLD_TIME = "hold time";
    private static final String HOLD_TYPE = "hold type";

    private double holdTime = Double.NaN;
    private int pointCountInSingleHoldSegment = 0;
    private String holdType = "";


    public NanoscopeForceList()
    {
        super(IDENTIFIER);
    }

    public int getPointCountInSingleHoldSegment()
    {        
        return pointCountInSingleHoldSegment;
    }

    public double getHoldTime()
    {
        return holdTime;
    }

    public String getHoldType()
    {
        return holdType;
    }

    @Override
    public void readField(String fieldRaw)
    { 
        String field = dropPrefices(fieldRaw);
        String fieldLowerCase = field.toLowerCase(); //Nanoscope fields in the text header have inconsistent case, e.g. the files use Scan Size or Scan size, depending on the file version
        //so we check against field converted to lower case

        if (fieldLowerCase.startsWith(HOLD_TIME + ":"))
        {
            this.holdTime = parseDoubleValue(field);            
        }
        else if(fieldLowerCase.startsWith(HOLD_SAMPLES + ":"))
        {
            this.pointCountInSingleHoldSegment = parseIntValue(field);
        }
        else if(fieldLowerCase.startsWith(HOLD_TYPE + ":"))
        {
            this.holdType = parseStringValue(field);
        }
    }
}
