
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

package atomicJ.gui.curveProcessing;

import org.jfree.data.Range;

import atomicJ.gui.MultipleXYChartPanel;

public class CalibrationPanel extends MultipleXYChartPanel<CalibrationChart>
{
    private static final long serialVersionUID = 1L;

    private CalibrationSupervisor supervisor;

    private boolean cleared;

    public CalibrationPanel()
    {
        super(null);
        cleared = true;
    }

    public CalibrationPanel(CalibrationChart chart)
    {
        super(chart);
        cleared = false;
    }

    public void setCalibrationSupervisor(CalibrationSupervisor supervisor)
    {
        this.supervisor = supervisor;

        for(CalibrationChart chart : getCharts())
        {
            if(chart != null)
            {
                chart.setCalibrationSupervisor(supervisor);
            }
        }
    }

    public void setRange(Range range)
    {
        for(CalibrationChart chart : getCharts())
        {
            if(chart != null)
            {
                chart.setRange(range);
            }
        }
    }

    @Override
    public void addChart(CalibrationChart chart)
    {
        super.addChart(chart);
        if(chart != null)
        {
            chart.setCalibrationSupervisor(supervisor);
        }
    }

    public void setChart(CalibrationChart chart)
    {
        setSelectedChartAndClearOld(chart);

        cleared = false;
    }


    @Override
    public void clear()
    {
        setChart(null);
        cleared = true;
    }

    public boolean isCleared()
    {
        return cleared;
    }
}
