
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

package atomicJ.gui.boxplots;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import atomicJ.gui.AbstractChartPanelFactory;
import atomicJ.gui.CustomizableXYBaseChart;
import atomicJ.gui.MultipleChartPanel;
import atomicJ.gui.histogram.BasicHistogramPanelSupervisor;


public class BasicBoxPlotPanel <E extends CustomizableXYBaseChart<BoxAndWhiskerXYPlot>> extends MultipleChartPanel<E>
{
    private static final long serialVersionUID = 1L;

    private final ShowRawDataAction showRawDataAction = new ShowRawDataAction();
    private final ShowStatisticsAction showStatisticsAction = new ShowStatisticsAction();

    private BasicHistogramPanelSupervisor supervisor;

    public BasicBoxPlotPanel(boolean addPopup)
    {
        super(null, false);

        if(addPopup)
        {
            setPopupMenu(buildBasicHistogramPanelPopupMenu(true, true, true, true,true));
        }

        initInputAndActionMaps();
    }

    private void initInputAndActionMaps()
    {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);                
        inputMap.put((KeyStroke) showRawDataAction.getValue(Action.ACCELERATOR_KEY), showRawDataAction.getValue(Action.NAME));      
        inputMap.put((KeyStroke) showStatisticsAction.getValue(Action.ACCELERATOR_KEY), showStatisticsAction.getValue(Action.NAME));

        ActionMap actionMap =  getActionMap();
        actionMap.put(showRawDataAction.getValue(Action.NAME), showRawDataAction);    
        actionMap.put(showStatisticsAction.getValue(Action.NAME), showStatisticsAction);
    }

    public void setBasicSupervisor(BasicHistogramPanelSupervisor supervisor)
    {
        this.supervisor = supervisor;
    }

    protected JPopupMenu buildBasicHistogramPanelPopupMenu(boolean properties, boolean copy, boolean save, boolean print, boolean zoom) 
    {
        JPopupMenu popupMenu = super.createPopupMenu(properties, copy, save, print, zoom);

        JMenuItem rawDataItem = new JMenuItem(showRawDataAction);
        JMenuItem statisticsItem = new JMenuItem(showStatisticsAction);  

        popupMenu.add(rawDataItem);
        popupMenu.add(statisticsItem); 

        return popupMenu;
    }


    public void setNonPositiveValuesPresent(boolean nonpositivePresent)
    {
    }

    private class ShowRawDataAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowRawDataAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Raw data");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(supervisor != null)
            {
                supervisor.showRawData();
            }
        }
    }

    private class ShowStatisticsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowStatisticsAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Statistics");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(supervisor != null)
            {
                supervisor.showStatistics();
            }
        }
    }

    public static class BasicBoxPlotPanelFactory implements 
    AbstractChartPanelFactory<BasicBoxPlotPanel<CustomizableXYBaseChart<BoxAndWhiskerXYPlot>>>
    {
        private static final BasicBoxPlotPanelFactory INSTANCE = new BasicBoxPlotPanelFactory();

        public static BasicBoxPlotPanelFactory getInstance()
        {
            return INSTANCE;
        }

        @Override
        public BasicBoxPlotPanel<CustomizableXYBaseChart<BoxAndWhiskerXYPlot>> buildEmptyPanel() 
        {
            return new BasicBoxPlotPanel<>(true);
        }
    }
}
