
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

import atomicJ.data.units.PrefixedUnit;

public interface RoamingStackTextTitle extends RoamingTextTitle
{
    public void setFrameTitleText(double value, PrefixedUnit unit);
    public boolean isUpdateFrameTitle();
    public void setUpdateFrameTitle(boolean updateFrameTitle);

    public int getMaximumFractionDigits();
    public void setMaximumFractionDigits(int n);
    public boolean isTickLabelTrailingZeroes();
    public void setTickLabelShowTrailingZeroes(boolean trailingZeroes);
    public boolean isTickLabelGroupingUsed();
    public void setTickLabelGroupingUsed(boolean used);
    public char getTickLabelGroupingSeparator();
    public void setTickLabelGroupingSeparator(char separatorNew);
    public char getTickLabelDecimalSeparator();
    public void setTickLabelDecimalSeparator(char separator);
}
