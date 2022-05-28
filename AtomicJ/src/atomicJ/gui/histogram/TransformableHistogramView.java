
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


import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;

import org.apache.commons.math3.analysis.UnivariateFunction;

import atomicJ.data.units.UnitQuantity;
import atomicJ.functions.DecimalLogarithm;
import atomicJ.functions.IncrementationFunction;
import atomicJ.functions.Reciprocal;
import atomicJ.functions.SquareRoot;
import atomicJ.gui.ConstantFunctionDialog;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.UnivariateFunctionReceiver;
import atomicJ.gui.histogram.TransformableHistogramPanel.TransformableHistogramPanelFactory;



public class TransformableHistogramView extends AbstractHistogramView<TransformableHistogramPanel<ChannelChart<HistogramPlot>>> implements TransformableHistogramPanelSupervisor, HistogramDestination
{
    private static final NumberFormat DEFAULT_FORMAT = NumberFormat.getInstance(Locale.US);

    static 
    {
        DEFAULT_FORMAT.setMaximumFractionDigits(4);
    }

    private final FixZeroAction fixZeroAction = new FixZeroAction();
    private final AddConstantAction addConstantAction = new AddConstantAction();
    private final LogarithmizeAction logarithmizeAction = new LogarithmizeAction();
    private final SquareRootAction squareRootAction = new SquareRootAction();
    private final ReciprocalAction reciprocalAction = new ReciprocalAction();

    private final JMenu transformationsMenu = new JMenu("Transformations");

    private final ConstantFunctionDialog constantDialog = new ConstantFunctionDialog(getAssociatedWindow(), "Add constant");


    public TransformableHistogramView(Window parent, String title)
    {
        this(parent, title, ModalityType.MODELESS, true, true);	
    }

    public TransformableHistogramView(Window parent, String title, ModalityType modalityType, boolean allowsMultipleResources, boolean allowsMultipleTypes)
    {
        super(parent, TransformableHistogramPanelFactory.getInstance(), title, modalityType, allowsMultipleResources, allowsMultipleTypes);	

        JMenu dataMenu = getDataMenu();

        JMenuItem addConstantItem = new JMenuItem(addConstantAction);
        JMenuItem logarithmizeItem = new JMenuItem(logarithmizeAction);
        JMenuItem squareRootItem = new JMenuItem(squareRootAction);
        JMenuItem reciprocalItem = new JMenuItem(reciprocalAction);

        JMenuItem fixZeroItem = new JMenuItem(fixZeroAction);

        transformationsMenu.add(squareRootItem);
        transformationsMenu.add(logarithmizeItem);
        transformationsMenu.add(reciprocalItem);

        dataMenu.insert(fixZeroItem, 3);
        dataMenu.add(addConstantItem,4);
        dataMenu.add(transformationsMenu, 5);  

        controlForResourceEmptinessPrivate(isEmpty());
    }

    @Override
    public void controlForResourceEmptiness(boolean empty) 
    {
        super.controlForResourceEmptiness(empty);
        controlForResourceEmptinessPrivate(empty);
    }

    private void controlForResourceEmptinessPrivate(boolean empty)
    {
        boolean enabled = !empty;

        fixZeroAction.setEnabled(enabled);
        addConstantAction.setEnabled(enabled);
        logarithmizeAction.setEnabled(enabled);
        squareRootAction.setEnabled(enabled);
        reciprocalAction.setEnabled(enabled);
    }

    @Override
    public void handleNewChartPanel(TransformableHistogramPanel<ChannelChart<HistogramPlot>> panel)
    {
        super.handleNewChartPanel(panel);
        panel.setTransformableHistogramSupervisor(this);
    }

    @Override
    public void logarithmize()
    {
        HistogramResource resource = getSelectedResource();

        String key = getSelectedType();
        final HistogramSampleModel sampleModel = resource.getModel(key);

        sampleModel.applyFunction(new DecimalLogarithm(), "Log10");	
        setConsistentWithCurrentSampleModel();
    }

    @Override
    public void squareRoot()
    {
        HistogramResource resource = getSelectedResource();

        String key = getSelectedType();
        final HistogramSampleModel sampleModel = resource.getModel(key);

        sampleModel.applyFunction(new SquareRoot(), "Sqrt");	
        setConsistentWithCurrentSampleModel();
    }

    @Override
    public void reciprocal()
    {
        HistogramResource resource = getSelectedResource();

        String key = getSelectedType();
        final HistogramSampleModel sampleModel = resource.getModel(key);

        sampleModel.applyFunction(new Reciprocal(), UnitQuantity.RECIPROCAL);	
        setConsistentWithCurrentSampleModel();
    }

    @Override
    public void addConstant()
    {
        HistogramResource resource = getSelectedResource();

        String key = getSelectedType();
        final HistogramSampleModel sampleModel = resource.getModel(key);
        constantDialog.showDialog(new UnivariateFunctionReceiver() 
        {
            private double addedConstant = 0;

            @Override
            public void setFunction(UnivariateFunction f) 
            {
                IncrementationFunction incrF = (IncrementationFunction)f;
                double newConstant = incrF.getConstant();
                double diff = newConstant - addedConstant;
                addedConstant = newConstant;
                sampleModel.applyFunction(new IncrementationFunction(diff));
                setConsistentWithCurrentSampleModel();
            }

            @Override
            public void reset() 
            {
                addedConstant = 0;
                IncrementationFunction f = new IncrementationFunction(-addedConstant);
                sampleModel.applyFunction(f);
                setConsistentWithCurrentSampleModel();
            }
        });
    }

    @Override
    public void fixZero()
    {
        HistogramResource resource = getSelectedResource();

        String key = getSelectedType();
        HistogramSampleModel sampleModel = resource.getModel(key);
        sampleModel.fixZero();

        setConsistentWithCurrentSampleModel();
    }


    @Override
    protected void setNonPositiveValuesPresent(boolean nonPositve)
    {
        boolean enabled = !nonPositve;
        logarithmizeAction.setEnabled(enabled);
        squareRootAction.setEnabled(enabled);
        for(BasicHistogramPanel<?> panel : getPanels())
        {
            panel.setNonPositiveValuesPresent(nonPositve);
        }

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
            addConstant();
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
            logarithmize();
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
            squareRoot();
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
            reciprocal();
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
            fixZero();
        }
    }
}
