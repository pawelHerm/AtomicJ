
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

package atomicJ.gui.profile;

import atomicJ.gui.AbstractChartPanelFactory;
import atomicJ.gui.MultipleXYChartPanel;

public class CrossSectionPanel <E extends CrossSectionsChart> extends MultipleXYChartPanel<E>
{
    private static final long serialVersionUID = 1L;

    private CrossSectionsSupervisor supervisor;

    public CrossSectionPanel(boolean addPopup)
    {
        super(null, false);
        if(addPopup)
        {
            setPopupMenu(super.createPopupMenu(true, true, true, true, true));
        }
    }

    public CrossSectionsSupervisor getCrossSectionsSupervisor()
    {
        return supervisor;
    }

    public void setCrossSectionsSupervisor(CrossSectionsSupervisor supervisorNew)
    {
        this.supervisor = supervisorNew;
    }

    public void addMarker(CrossSectionMarkerParameters markerParameters)
    {	
        E selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.addMarker(markerParameters);
        }	
    }

    public void moveMarker(CrossSectionMarkerParameters markerParameters, int markerIndex)
    {
        E selectedChart = getSelectedChart();

        if(selectedChart != null)
        {
            selectedChart.moveMarker(markerParameters, markerIndex);
        }	
    }

    public void removeMarker(Object profileKey, double markerPosition)
    {
        E selectedChart = getSelectedChart();

        if(selectedChart != null)
        {
            selectedChart.removeMarker(profileKey, markerPosition);
        }	
    }

    public void removeAllMarkers(Object profileKey)
    {
        E selectedChart = getSelectedChart();

        if(selectedChart != null)
        {
            selectedChart.removeAllMarkers(profileKey);
        }	
    }

    @Override
    public void addChart(E chart)
    {
        super.addChart(chart);
        if(chart != null)
        {
            chart.setCrossSectionsSupervisor(supervisor);
        }
    }

    public static class ProfilePanelFactory implements AbstractChartPanelFactory<CrossSectionPanel<CrossSectionsChart>>
    {
        private static final  ProfilePanelFactory INSTANCE = new ProfilePanelFactory();

        public static ProfilePanelFactory getInstance()
        {
            return INSTANCE;
        }

        @Override
        public CrossSectionPanel<CrossSectionsChart> buildEmptyPanel() 
        {
            return new CrossSectionPanel<>(true);
        }
    }
}
