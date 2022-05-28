
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

package atomicJ.gui.save;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.List;

public interface SaveableChartSource <E> 
{
    public int getChartWidth();
    public int getChartHeight();
    public int getDataWidth();
    public int getDataHeight();

    public Rectangle2D getChartArea();
    public Rectangle2D getDataArea();

    public List<String> getDefaultOutputNames(String key);
    public List<File> getDefaultOutputLocations(String key);
    public List<E> getAllNonemptyCharts(String key);
}
