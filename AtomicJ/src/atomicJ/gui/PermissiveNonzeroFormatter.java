
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

public class PermissiveNonzeroFormatter extends NumberFormatter
{
    private static final long serialVersionUID = 1L;

    private final boolean acceptNonIntegers;
    private final double tolerance = 1e-10;

    public PermissiveNonzeroFormatter(NumberFormat f)
    {
        this(f, -Double.MAX_VALUE);
    }

    public PermissiveNonzeroFormatter(NumberFormat f, double min)
    {
        this(f, min, Double.MAX_VALUE);
    }

    public PermissiveNonzeroFormatter(NumberFormat f, double min, double max)
    {
        this(f, min, max, true);
    }

    public PermissiveNonzeroFormatter(NumberFormat f, double min, double max, boolean acceptNonIntegers)
    {
        super(f);
        setMinimum(Double.valueOf(min));
        setMaximum(Double.valueOf(max));
        setCommitsOnValidEdit(true);
        this.acceptNonIntegers = acceptNonIntegers;
    }

    @Override
    public Object stringToValue(String s) throws ParseException
    {
        if(s == null || s.length() == 0)
        {
            return Double.valueOf(Double.NaN);
        }

        Double rawValue = ((Number)super.stringToValue(s)).doubleValue();

        if(Math.abs(rawValue) < tolerance)
        {
            throw new ParseException("Illegal zero value encountered", 0);
        }

        if(acceptNonIntegers)
        {
            return super.stringToValue(s);
        }

        Double roundedValue = Math.rint(rawValue);

        if(rawValue.equals(roundedValue))
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
        if((Double.valueOf(Double.NaN)).equals(v))
        {
            return "";
        }
        else 
        {
            return super.valueToString(v);
        }
    }	
}
