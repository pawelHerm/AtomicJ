
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

public interface ChartStyleSupplier 
{
    public boolean getDefaultTitleFrameVisible(String key);
    public boolean getDefaultTitleVisible(String key);
    public boolean getDefaultTitleInside(String key);
    public double getDefaultTitleInsideY(String key);
    public double getDefaultTitleInsideX(String key);

    public boolean getDefaultLegendFrameVisible(String key);
    public boolean getDefaultLegendVisible(String key);
    public boolean getDefaultLegendInside(String key);
    public double getDefaultLegendInsideY(String key);
    public double getDefaultLegendInsideX(String key);
}
