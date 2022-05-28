
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

package atomicJ.gui.units;


import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;


import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.TickUnitSource;

import atomicJ.gui.NumberFormatReceiver;
import atomicJ.gui.PrefixedTickUnit;
import atomicJ.utilities.MathUtilities;


public class LightweightTickUnits implements TickUnitSource, NumberFormatReceiver, Cloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    private static final double[] TICK_UNIT_SIZES = new double[42];

    static
    {
        int index = 0;
        for(int maxDigit = -10; maxDigit<=10;maxDigit++)
        {	
            TICK_UNIT_SIZES[index++] = MathUtilities.intPow(10, maxDigit);
            TICK_UNIT_SIZES[index++] = 5*MathUtilities.intPow(10, maxDigit);
        }
    }

    private boolean showTrailingZeroes;
    private char groupingSeparator;			
    private char decimalSeparator;			
    private boolean groupingUsed;
    private double conversionFactor = 1;

    public void setConversionFactor(double conversionFactorNew)
    {        
        this.conversionFactor = conversionFactorNew;
    }

    public int size() 
    {
        return TICK_UNIT_SIZES.length;
    }

    public PrefixedTickUnit get(int pos) 
    {
        double size = TICK_UNIT_SIZES[pos];    	
        return getTickUnitForSize(size);
    }

    public PrefixedTickUnit getTickUnitForSize(double size)
    {        
        int maxDigit = MathUtilities.getFractionCount(conversionFactor*size);
        int minDigits = showTrailingZeroes ? maxDigit : 0;
        DecimalFormat format = new DecimalFormat();

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

        symbols.setDecimalSeparator(decimalSeparator);
        symbols.setGroupingSeparator(groupingSeparator);
        format.setGroupingUsed(groupingUsed);
        format.setMaximumFractionDigits(maxDigit);
        format.setMinimumFractionDigits(minDigits);

        format.setDecimalFormatSymbols(symbols);

        PrefixedTickUnit tickUnit = new PrefixedTickUnit(size,format, 5, conversionFactor);
        return tickUnit;
    }

    @Override
    public PrefixedTickUnit getLargerTickUnit(TickUnit unit) 
    {
        int index = Arrays.binarySearch(TICK_UNIT_SIZES, unit.getSize());
        if (index >= 0) {
            index = index + 1;
        }
        else {
            index = -index;
        }

        double size = TICK_UNIT_SIZES[Math.min(index, TICK_UNIT_SIZES.length - 1)];

        return getTickUnitForSize(size);
    }


    @Override
    public PrefixedTickUnit getCeilingTickUnit(TickUnit unit) 
    {
        return getCeilingTickUnit(unit.getSize());
    }

    @Override
    public PrefixedTickUnit getCeilingTickUnit(double size) 
    {
        int index = Arrays.binarySearch(TICK_UNIT_SIZES, size);

        if (index <0) 
        {
            index = -(index + 1);

        }

        double sizeNew =  TICK_UNIT_SIZES[Math.min(index, TICK_UNIT_SIZES.length - 1)];

        return getTickUnitForSize(sizeNew);
    }

    @Override
    public boolean isTickLabelTrailingZeroes()
    {
        return showTrailingZeroes;
    }

    @Override
    public void setTickLabelShowTrailingZeroes(boolean trailingZeroes)
    {
        this.showTrailingZeroes = trailingZeroes;
    }

    @Override
    public boolean isTickLabelGroupingUsed()
    {
        return groupingUsed;
    }

    @Override
    public void setTickLabelGroupingUsed(boolean used)
    {
        this.groupingUsed = used;
    }

    @Override
    public char getTickLabelGroupingSeparator()
    {
        return groupingSeparator;
    }

    @Override
    public void setTickLabelGroupingSeparator(char separator)
    {
        this.groupingSeparator = separator;
    }

    @Override
    public char getTickLabelDecimalSeparator()
    {
        return decimalSeparator;
    }

    @Override
    public void setTickLabelDecimalSeparator(char separator)
    {
        this.decimalSeparator = separator;

    }


    @Override
    public Object clone() throws CloneNotSupportedException 
    {
        LightweightTickUnits clone = (LightweightTickUnits) super.clone();
        return clone;
    }

    @Override
    public int hashCode()
    {
        int result = Boolean.hashCode(showTrailingZeroes);
        result = 31*result + Boolean.hashCode(groupingUsed);
        result = 31*result + Character.hashCode(decimalSeparator);
        result = 31*result + Character.hashCode(groupingSeparator);
        result = 31*result + Double.hashCode(conversionFactor);

        return result;
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LightweightTickUnits)) {
            return false;
        }
        LightweightTickUnits that = (LightweightTickUnits) obj;

        boolean equal = true;
        equal = equal && (this.showTrailingZeroes == that.showTrailingZeroes);
        equal = equal && (this.groupingUsed == that.groupingUsed);
        equal = equal && (this.decimalSeparator == that.decimalSeparator);
        equal = equal && (this.groupingSeparator == that.groupingSeparator);
        equal = equal && (this.conversionFactor == that.conversionFactor);

        return equal;
    }

}

