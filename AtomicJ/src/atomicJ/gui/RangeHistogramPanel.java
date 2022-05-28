
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

import atomicJ.gui.histogram.BasicHistogramPanel;
import atomicJ.gui.histogram.HistogramPlot;


public class RangeHistogramPanel <E extends ChannelChart<?>> extends BasicHistogramPanel<E>
{
    private static final long serialVersionUID = 1L;

    private final RestoreFullRangeAction restoreFullRangeAction = new RestoreFullRangeAction();
    private final RestoreAutomaticRangeAction restoreAutomaticRangeAction = new RestoreAutomaticRangeAction();

    private RangeHistogramPanelSupervisor supervisor;

    public RangeHistogramPanel(boolean addPopup)
    {
        super(false);

        if(addPopup)
        {
            setPopupMenu(buildRangeHistogramPanelPopupMenu(true, true, true, true,true));
        }

        initInputAndActionMaps();
    }

    private void initInputAndActionMaps()
    {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);                
        inputMap.put((KeyStroke) restoreFullRangeAction.getValue(Action.ACCELERATOR_KEY), restoreFullRangeAction.getValue(Action.NAME));      
        inputMap.put((KeyStroke) restoreAutomaticRangeAction.getValue(Action.ACCELERATOR_KEY), restoreAutomaticRangeAction.getValue(Action.NAME));

        ActionMap actionMap =  getActionMap();
        actionMap.put(restoreFullRangeAction.getValue(Action.NAME), restoreFullRangeAction);    
        actionMap.put(restoreAutomaticRangeAction.getValue(Action.NAME), restoreAutomaticRangeAction);
    }

    public void setRangeSupervisor(RangeHistogramPanelSupervisor supervisor)
    {
        this.supervisor = supervisor;
    }

    protected JPopupMenu buildRangeHistogramPanelPopupMenu(boolean properties, boolean copy, boolean save, boolean print, boolean zoom) 
    {
        JPopupMenu popupMenu = super.buildBasicHistogramPanelPopupMenu(properties, copy, save, print, zoom);

        JMenuItem itemRestoreFullRange = new JMenuItem(restoreFullRangeAction);
        JMenuItem itemRestoreAutomaticRange = new JMenuItem(restoreAutomaticRangeAction); 

        popupMenu.addSeparator();
        popupMenu.add(itemRestoreAutomaticRange);
        popupMenu.add(itemRestoreFullRange); 

        return popupMenu;
    }

    private class RestoreAutomaticRangeAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public RestoreAutomaticRangeAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Restore automatic range");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(supervisor != null)
            {
                supervisor.setRangeSelector(GradientRangeSelector.AUTOMATIC);
            }
        }
    }


    private class RestoreFullRangeAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public RestoreFullRangeAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R,	InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Restore full range");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(supervisor != null)
            {
                supervisor.setRangeSelector(GradientRangeSelector.FULL);
            }
        }
    }

    public static class RangeHistogramPanelFactory implements AbstractChartPanelFactory<RangeHistogramPanel<ChannelChart<HistogramPlot>>>
    {
        private static final  RangeHistogramPanelFactory INSTANCE = new RangeHistogramPanelFactory();

        public static RangeHistogramPanelFactory getInstance()
        {
            return INSTANCE;
        }

        @Override
        public RangeHistogramPanel<ChannelChart<HistogramPlot>> buildEmptyPanel() 
        {
            return new RangeHistogramPanel<>(true);
        }
    }
}
