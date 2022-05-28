
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

import java.beans.PropertyChangeListener;

public interface RangeModel 
{
    public static final String LOWER_BOUND = "LOWER_BOUND";
    public static final String UPPER_BOUND = "UPPER_BOUND";

    public static final String LOWER_FULL_BOUND = "LOWER_FULL_BOUND";
    public static final String UPPER_FULL_BOUND = "UPPER_FULL_BOUND";

    public static final String LOWER_AUTOMATIC_BOUND = "LOWER_AUTOMATIC_BOUND";
    public static final String UPPER_AUTOMATIC_BOUND = "UPPER_AUTOMATIC_BOUND";


    public double getLowerBound();
    public double getUpperBound();

    public void setGradientRangeSelector(GradientRangeSelector selector);

    public void setLowerBound(double lowerBoundNew);
    public void setUpperBound(double upperBoundNew);

    public void addPropertyChangeListener(PropertyChangeListener listener);
    public void removePropertyChangeListener(PropertyChangeListener listener);
}
