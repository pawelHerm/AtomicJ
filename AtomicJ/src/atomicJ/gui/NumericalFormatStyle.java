
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

import java.text.DecimalFormat;

import atomicJ.gui.StandardNumericalFormatStyle.FormattableNumericalDataState;

public interface NumericalFormatStyle 
{    
    public int getMaximumFractionDigits();
    public void setMaximumFractionDigits(int n);
    public boolean isGroupingUsed();
    public void setGroupingUsed(boolean used);
    public char getGroupingSeparator();
    public void setGroupingSeparator(char separator); 
    public char getDecimalSeparator();
    public void setDecimalSeparator(char separator);
    public boolean isShowTrailingZeroes();
    public void setShowTrailingZeroes(boolean shown);
    public DecimalFormat getDecimalFormat();
    public void saveToPreferences();
    public FormattableNumericalDataState getState();
    public void setState(FormattableNumericalDataState memento);
    public void addListener(FormattableNumericalDataListener listener);
    public void removeListener(FormattableNumericalDataListener listener);
}
