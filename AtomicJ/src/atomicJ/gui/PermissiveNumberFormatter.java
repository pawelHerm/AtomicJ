
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

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.text.NumberFormatter;

public class PermissiveNumberFormatter extends NumberFormatter
{
    private static final long serialVersionUID = 1L;

    private final boolean acceptReals;

    public PermissiveNumberFormatter(NumberFormat f)
    {
        this(f, -Double.MAX_VALUE);
    }

    public PermissiveNumberFormatter(NumberFormat f, double min)
    {
        this(f, min, Double.MAX_VALUE);
    }

    public PermissiveNumberFormatter(NumberFormat f, double min, double max)
    {
        this(f, min, max, true);
    }

    public PermissiveNumberFormatter(NumberFormat f, double min, double max, boolean acceptReals)
    {
        super(f);
        setMinimum(Double.valueOf(min));
        setMaximum(Double.valueOf(max));
        setCommitsOnValidEdit(true);
        this.acceptReals = acceptReals;
    }

    @Override
    public Object stringToValue(String s) throws ParseException
    {
        if(s == null || s.length() == 0)
        {
            return Double.NaN;
        }

        if(acceptReals)
        {
            return super.stringToValue(s);
        }

        double rawValue = ((Number)super.stringToValue(s)).doubleValue();
        double roundedValue = Math.rint(rawValue);

        if(Double.compare(rawValue, roundedValue) == 0) //we use compare, for this guarantees that NaN == NaN
        {
            return super.stringToValue(s);
        }
        else
        {
            throw new ParseException("Fraction encountered, integer excepted", 0);
        }
    }

    @Override
    public String valueToString(Object v) throws ParseException
    {
        String text = Double.valueOf(Double.NaN).equals(v) ? "" : super.valueToString(v);        
        return text;
    }	
}
