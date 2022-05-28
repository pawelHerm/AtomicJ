package atomicJ.gui;


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


import static atomicJ.gui.PreferenceKeys.*;

import javax.swing.table.DefaultTableCellRenderer;

import atomicJ.gui.StandardNumericalFormatStyle.FormattableNumericalDataState;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

public class DecimalStandardTableCellRenderer extends DefaultTableCellRenderer implements DecimalTableCellRenderer
{
    private static final long serialVersionUID = 1L;

    private boolean showTrailingZeroes;
    private DecimalFormat format = new DecimalFormat();
    private final List<FormattableNumericalDataListener> listeners = new ArrayList<>();

    private Preferences pref = PreferencesNull.getInstance();

    public DecimalStandardTableCellRenderer()
    {}

    public DecimalStandardTableCellRenderer(Preferences pref) 
    {
        this.pref = pref;
        pullPreferences(pref);      
    }

    public DecimalStandardTableCellRenderer(DecimalFormat format) 
    {
        this.format = format;
    }

    @Override
    public void setPreferences(Preferences pref)
    {
        this.pref = pref;
        pullPreferences(pref);
    }

    private void pullPreferences(Preferences pref)
    {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

        boolean groupingUsed = pref.getBoolean(GROUPING_USED, false);       
        showTrailingZeroes = pref.getBoolean(TRAILING_ZEROES, false);
        char groupingSeparator = (char) pref.getInt(GROUPING_SEPARATOR, ' ');               
        char decimalSeparator = (char) pref.getInt(DECIMAL_SEPARATOR, '.');             
        int maxDigit = pref.getInt(MAX_FRACTION_DIGITS, 4);
        int minDigit = showTrailingZeroes ? maxDigit : 0;

        symbols.setDecimalSeparator(decimalSeparator);
        symbols.setGroupingSeparator(groupingSeparator);

        format.setMaximumFractionDigits(maxDigit);
        format.setMinimumFractionDigits(minDigit);
        format.setGroupingUsed(groupingUsed);

        format.setDecimalFormatSymbols(symbols);
    }

    @Override
    public void saveToPreferences()
    {
        if(pref != null)
        {
            pref.putInt(MAX_FRACTION_DIGITS, getMaximumFractionDigits());
            pref.putBoolean(GROUPING_USED, isGroupingUsed());
            pref.putInt(GROUPING_SEPARATOR, getGroupingSeparator());
            pref.putInt(DECIMAL_SEPARATOR, getDecimalSeparator());
            pref.putBoolean(TRAILING_ZEROES, isShowTrailingZeroes());
        }
    }

    @Override
    public void setValue(Object entry)
    {
        if (entry instanceof Double) 
        {
            String text = entry.equals(Double.NaN) ? "IND" : format.format(entry);
            setText(text);
        }
        else
        {
            String text = (entry != null) ? entry.toString() : "";
            setText(text);
        }
    }

    @Override
    public String getValue(Object entry, int modelColumn)
    {
        String text;
        if (entry instanceof Double) 
        {
            text = entry.equals(Double.NaN) ? "IND" : format.format(entry);
        }
        else
        {
            text = (entry != null) ? entry.toString() : "";
        }

        return text;
    }

    @Override
    public int getMaximumFractionDigits()
    {
        return format.getMaximumFractionDigits();
    }  

    @Override
    public void setMaximumFractionDigits(int maxDigitsNew)
    {
        int maxDigitsOld = format.getMaximumFractionDigits();
        format.setMaximumFractionDigits(maxDigitsNew);
        if(showTrailingZeroes)
        {
            format.setMinimumFractionDigits(maxDigitsNew);
        }  

        if(maxDigitsOld != maxDigitsNew)
        {
            for(FormattableNumericalDataListener l : listeners) 
            {
                l.maximumFractionDigitsChanged(maxDigitsNew);
            }
        }
    }

    @Override
    public boolean isGroupingUsed()
    {
        return format.isGroupingUsed();
    }

    @Override
    public void setGroupingUsed(boolean groupingUsedNew)
    {
        boolean groupingUsedOld = format.isGroupingUsed();
        format.setGroupingUsed(groupingUsedNew);

        if(groupingUsedOld != groupingUsedNew)
        {
            for(FormattableNumericalDataListener l : listeners) 
            {
                l.groupingUsedChanged(groupingUsedNew);
            }
        }
    }

    @Override
    public char getGroupingSeparator()
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();

        return symbols.getGroupingSeparator();
    }

    @Override
    public void setGroupingSeparator(char separatorNew)
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();

        char separatorOld = symbols.getGroupingSeparator();
        symbols.setGroupingSeparator(separatorNew);

        format.setDecimalFormatSymbols(symbols);

        if(separatorOld != separatorNew)
        {
            for(FormattableNumericalDataListener l : listeners) 
            {
                l.groupingSeparatorChanged(separatorNew);
            }
        }
    }

    @Override
    public char getDecimalSeparator()
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        return symbols.getDecimalSeparator();
    }

    @Override
    public void setDecimalSeparator(char separatorNew)
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        char separatorOld = symbols.getDecimalSeparator();
        symbols.setDecimalSeparator(separatorNew);

        format.setDecimalFormatSymbols(symbols);

        if(separatorOld != separatorNew)
        {
            for(FormattableNumericalDataListener l : listeners) 
            {
                l.decimalSeparatorChanged(separatorNew);
            }
        }
    }

    @Override
    public boolean isShowTrailingZeroes()
    {
        return showTrailingZeroes;
    }

    @Override
    public void setShowTrailingZeroes(boolean showTrailingZeroesNew)
    {
        boolean showTrailingZeroesOld = this.showTrailingZeroes;
        this.showTrailingZeroes = showTrailingZeroesNew;

        int minDigits = showTrailingZeroesNew ? format.getMaximumFractionDigits() : 0;
        format.setMinimumFractionDigits(minDigits);

        if(showTrailingZeroesOld != showTrailingZeroesNew){
            for(FormattableNumericalDataListener l : listeners) 
            {
                l.showTrailingZeroesChanged(this.showTrailingZeroes);
            }
        }    
    }

    @Override
    public DecimalFormat getDecimalFormat()
    {
        return format;
    }

    @Override
    public FormattableNumericalDataState getState()
    {
        return new FormattableNumericalDataState(getMaximumFractionDigits(),
                showTrailingZeroes, isGroupingUsed(), getGroupingSeparator(), getDecimalSeparator());
    }

    @Override
    public void setState(FormattableNumericalDataState memento)
    {
        setMaximumFractionDigits(memento.getMaximumFractionDigits());
        setShowTrailingZeroes(memento.isShowTrailingZeroes());
        setGroupingUsed(memento.isGroupingUsed());
        setGroupingSeparator(memento.getGroupingSeparator());
        setDecimalSeparator(memento.getDecimalSeparator());
    }

    public void setDecimalFormat(DecimalFormat format)
    {
        this.format = format;
    }   

    @Override
    public void addListener(FormattableNumericalDataListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(FormattableNumericalDataListener listener) {
        listeners.remove(listener);
    }
}

