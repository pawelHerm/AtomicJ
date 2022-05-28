
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

package atomicJ.gui.histogram;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import atomicJ.gui.AbstractChartPanelFactory;
import atomicJ.gui.ChannelChart;


public class TransformableHistogramPanel <E extends ChannelChart<?>> extends BasicHistogramPanel<E> implements ActionListener
{
    private static final long serialVersionUID = 1L;

    private final FixZeroAction fixZeroAction = new FixZeroAction();
    private final AddConstantAction addConstantAction = new AddConstantAction();
    private final LogarithmizeAction logarithmizeAction = new LogarithmizeAction();
    private final SquareRootAction squareRootAction = new SquareRootAction();
    private final ReciprocalAction reciprocalAction = new ReciprocalAction();

    private TransformableHistogramPanelSupervisor supervisor;

    public TransformableHistogramPanel(boolean addPopup)
    {
        super(false);

        if(addPopup)
        {
            setPopupMenu(buildTransformableHistogramPanelPopupMenu(true, true, true, true,true));
        }

        initInputAndActionMaps();
    }

    private void initInputAndActionMaps()
    {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);                
        inputMap.put((KeyStroke) fixZeroAction.getValue(Action.ACCELERATOR_KEY), fixZeroAction.getValue(Action.NAME));
        inputMap.put((KeyStroke) addConstantAction.getValue(Action.ACCELERATOR_KEY), addConstantAction.getValue(Action.NAME));
        inputMap.put((KeyStroke) logarithmizeAction.getValue(Action.ACCELERATOR_KEY), logarithmizeAction.getValue(Action.NAME));
        inputMap.put((KeyStroke) squareRootAction.getValue(Action.ACCELERATOR_KEY), squareRootAction.getValue(Action.NAME));
        inputMap.put((KeyStroke) reciprocalAction.getValue(Action.ACCELERATOR_KEY), reciprocalAction.getValue(Action.NAME));

        ActionMap actionMap =  getActionMap();
        actionMap.put(fixZeroAction.getValue(Action.NAME), fixZeroAction);
        actionMap.put(addConstantAction.getValue(Action.NAME), addConstantAction);
        actionMap.put(logarithmizeAction.getValue(Action.NAME), logarithmizeAction);
        actionMap.put(squareRootAction.getValue(Action.NAME), squareRootAction);
        actionMap.put(reciprocalAction.getValue(Action.NAME), reciprocalAction);
    }

    public void setTransformableHistogramSupervisor(TransformableHistogramPanelSupervisor supervisor)
    {
        this.supervisor = supervisor;		
    }

    protected final JPopupMenu buildTransformableHistogramPanelPopupMenu(boolean properties, boolean copy, boolean save, boolean print, boolean zoom) 
    {
        JPopupMenu popupMenu = buildBasicHistogramPanelPopupMenu(properties, copy, save, print, zoom);

        int n = popupMenu.getComponentCount();
        JMenu transformationsMenu = new JMenu("Transformations");


        JMenuItem squareRootItem = new JMenuItem(squareRootAction);
        JMenuItem logarithmizeItem = new JMenuItem(logarithmizeAction);
        JMenuItem reciprocalItem = new JMenuItem(reciprocalAction);

        transformationsMenu.add(squareRootItem);
        transformationsMenu.add(logarithmizeItem);
        transformationsMenu.add(reciprocalItem);

        JMenuItem fixZeroItem = new JMenuItem(fixZeroAction);
        JMenuItem addConstantItem = new JMenuItem(addConstantAction);

        popupMenu.insert(fixZeroItem, n - 1);
        popupMenu.insert(addConstantItem, n);
        popupMenu.insert(transformationsMenu, n + 1);

        return popupMenu;
    }	

    @Override
    public void setNonPositiveValuesPresent(boolean nonpositivePresent)
    {
        boolean enabled = !nonpositivePresent;
        logarithmizeAction.setEnabled(enabled);
        squareRootAction.setEnabled(enabled);
    }

    private class AddConstantAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public AddConstantAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Add constant");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            supervisor.addConstant();
        }
    }

    private class LogarithmizeAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public LogarithmizeAction()
        {			
            putValue(NAME,"Logarithmize");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            supervisor.logarithmize();
        }
    }

    private class SquareRootAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SquareRootAction()
        {			
            putValue(NAME,"Square root");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            supervisor.squareRoot();
        }
    }

    private class ReciprocalAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ReciprocalAction()
        {			
            putValue(NAME,"Reciprocal");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            supervisor.reciprocal();
        }
    }

    private class FixZeroAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FixZeroAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Fix zero");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            supervisor.fixZero();
        }
    }

    public static class TransformableHistogramPanelFactory implements AbstractChartPanelFactory<TransformableHistogramPanel<ChannelChart<HistogramPlot>>>
    {
        private static final  TransformableHistogramPanelFactory INSTANCE = new TransformableHistogramPanelFactory();

        public static TransformableHistogramPanelFactory getInstance()
        {
            return INSTANCE;
        }

        @Override
        public TransformableHistogramPanel<ChannelChart<HistogramPlot>> buildEmptyPanel() 
        {
            return new TransformableHistogramPanel<>(true);
        }
    }
}
