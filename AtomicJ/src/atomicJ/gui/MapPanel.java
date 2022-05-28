
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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.jfree.chart.entity.ChartEntity;

public class MapPanel extends Channel2DPanel<MapChart<?>> implements ActionListener, PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private final JumpToGraphsAction jumpToGraphsAction = new JumpToGraphsAction();
    private final JumpToResultsAction jumpToResultsAction = new JumpToResultsAction();

    private final RecalculateAction recalculateAction = new RecalculateAction();


    private SpectroscopySupervisor supervisor;
    private Point2D popupPoint;	

    public MapPanel(boolean addPopup)
    {
        this(addPopup, true);
    }

    public MapPanel(boolean addPopup, boolean allowROIbasedActions)
    {
        super(false, false);

        if(addPopup)
        {
            setPopupMenu(buildMapPanelPopupMenu(true, true, true, true,true, allowROIbasedActions));
        }

        initInputAndActionMaps();
    }

    private void initInputAndActionMaps()
    {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);

        inputMap.put((KeyStroke) recalculateAction.getValue(Action.ACCELERATOR_KEY), recalculateAction.getValue(Action.NAME));

        ActionMap actionMap =  getActionMap();
        actionMap.put(recalculateAction.getValue(Action.NAME), recalculateAction);      
    }

    public void setSpectroscopySupervisor(SpectroscopySupervisor supervisor)
    {
        this.supervisor = supervisor;
    }

    @Override
    protected void handleChartAddition(MapChart<?> chart)
    {
        super.handleChartAddition(chart);

        if(chart != null)
        {
            chart.setSpectroscopySupervisor(supervisor);
        }
    }

    @Override
    protected void handleChartAddition(List<MapChart<?>> charts)
    {
        super.handleChartAddition(charts);

        for(MapChart<?> chart : charts)
        {
            if(chart != null)
            {
                chart.setSpectroscopySupervisor(supervisor);
            }
        }       
    }
    protected JPopupMenu buildMapPanelPopupMenu(boolean properties, boolean copy, boolean save, boolean print, boolean zoom, boolean roi) 
    {
        JPopupMenu popupMenu = super.buildDenistyPanelPopupMenu(properties, copy, save, print, zoom, roi, true);
        int n = popupMenu.getComponentCount();	

        popupMenu.addSeparator();

        JMenuItem jumpToGraphsItem = new JMenuItem(jumpToResultsAction);
        JMenuItem jumpToResultsItem = new JMenuItem(jumpToGraphsAction);

        JMenuItem recalculateItem = new JMenuItem(recalculateAction);

        popupMenu.insert(recalculateItem, n + 1);
        popupMenu.insert(jumpToGraphsItem,n + 2);
        popupMenu.insert(jumpToResultsItem,n + 3);      

        return popupMenu;
    }

    @Override
    protected void displayPopupMenu(int x, int y) 
    {
        boolean enabled = curveMayExistForThePoint(x, y);	 
        jumpToGraphsAction.setEnabled(enabled);
        jumpToResultsAction.setEnabled(enabled);

        popupPoint = getDataPoint(new Point(x,y));

        super.displayPopupMenu(x, y);	 
    }

    protected boolean curveMayExistForThePoint(int x, int y)
    {
        ChartEntity entity = getEntityForPoint(x, y);

        boolean mayExist = entity instanceof LightweightXYItemEntity;

        return mayExist;
    }

    public void jumpToResults(Point2D point)
    {
        supervisor.jumpToResults(point);
    }

    public void jumpToFigures(Point2D point)
    {
        supervisor.jumpToFigures(point);
    }

    private class JumpToGraphsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public JumpToGraphsAction()
        {			
            putValue(NAME,"Find graphs");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            jumpToFigures(popupPoint);
        }
    }

    private class JumpToResultsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public JumpToResultsAction()
        {			
            putValue(NAME,"Find results");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            jumpToResults(popupPoint);
        }
    }

    private class RecalculateAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public RecalculateAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Recalculate");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            supervisor.recalculateFullDialog();
        }
    }

    public static class MapPanelFactory implements AbstractChartPanelFactory<MapPanel>
    {
        private static final  MapPanelFactory INSTANCE = new MapPanelFactory();

        public static MapPanelFactory getInstance()
        {
            return INSTANCE;
        }

        @Override
        public MapPanel buildEmptyPanel() 
        {
            return new MapPanel(true);
        }
    }
}
