
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

import javax.swing.*;

import org.jfree.chart.JFreeChart;

import java.awt.Window;
import java.awt.geom.Rectangle2D;

public class SimpleChartSaveDialog extends SimpleSaveDialog<ChartSaveFormatType, SimpleChartSaveModel>
{
    private static final long serialVersionUID = 1L;

    public SimpleChartSaveDialog(Window parent, JPanel parentPanel, String savePreferences) 
    {
        super(new SimpleChartSaveModel(), "Save chart", parent, parentPanel, savePreferences);
    }

    public void showDialog(JFreeChart chartToSave, Rectangle2D chartArea, Rectangle2D dataArea) 
    {
        getModel().specifyChartToSave(chartToSave, chartArea, dataArea);

        showDialog();
    }
}
