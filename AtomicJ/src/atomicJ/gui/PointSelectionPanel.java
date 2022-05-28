
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

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PointSelectionPanel extends MultipleXYChartPanel<PointSelectionChart> implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    public Double getSelectionX()
    {
        PointSelectionChart chart = getSelectedChart();

        if(chart != null)
        {
            return chart.getSelectionX();
        }

        return Double.NaN;
    }

    public Double getSelectionY()
    {
        PointSelectionChart chart = getSelectedChart();

        if(chart != null)
        {
            return chart.getSelectionY();
        }
        return Double.NaN;
    }

    public void setSelectionX(double x)
    {
        PointSelectionChart chart = getSelectedChart();
        chart.setSelectionX(x);
    }

    public void setSelectionY(double y)
    {
        PointSelectionChart chart = getSelectedChart();
        chart.setSelectionY(y);
    }

    public Point2D getSelection()
    {
        PointSelectionChart chart = getSelectedChart();
        return chart.getSelection();
    }

    public void setSelection(double x, double y)
    {		
        PointSelectionChart chart = getSelectedChart();
        chart.setSelection(x, y);
    }

    @Override
    public void setSelectedChart(PointSelectionChart chartNew)
    {
        PointSelectionChart oldChart = getSelectedChart();
        if(oldChart != null)
        {
            oldChart.removePropertyChangeListener(this);
        }

        if(chartNew != null)
        {
            chartNew.addPropertyChangeListener(this);
        }

        super.setSelectedChart(chartNew);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        Object source = evt.getSource();

        if(source == getSelectedChart())
        {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }
}
