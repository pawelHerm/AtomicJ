
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

import java.awt.Dialog.ModalityType;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import atomicJ.data.Channel1D;
import atomicJ.data.ChannelGroupTag;
import atomicJ.gui.measurements.DistanceMeasurementStyle;
import atomicJ.gui.measurements.DistanceMeasurementSupervisor;
import atomicJ.resources.DataModelResource;


public abstract class ResourceXYView<R extends DataModelResource, V extends ChannelChart<?>, E extends MultipleXYChartPanel<V>> 
extends ResourcePresentationView<R, V,E> implements DistanceMeasurementSupervisor, Channel1DModificationSupervisor
{
    private final Action modifyMeasurementStyle = new ModifyDistanceMeasurementStyleAction();
    private final Action showMeasurementsAction = new ShowDistanceMeasurementsAction();
    private final Action measureLineAction = new MeasureLineAction();

    private final JMenu measurementMenu;

    public ResourceXYView(final Window parent, AbstractChartPanelFactory<E> panelFactory, String title, Preferences pref, ModalityType modalityType)
    {
        this(parent, panelFactory, title, pref, modalityType, true, true, new ResourceViewModel<R>());
    }

    public ResourceXYView(Window parent, AbstractChartPanelFactory<E> panelFactory, String title, Preferences pref, ModalityType modalityType, ResourceViewModel<R> model)
    {
        this(parent, panelFactory, title, pref, modalityType, true, true, model);
    }

    public ResourceXYView(Window parent, AbstractChartPanelFactory<E> panelFactory, String title, Preferences pref, ModalityType modalityType, boolean allowsMultipleResources, boolean allowsMultipleTypes)
    {
        this(parent, panelFactory, title, pref, modalityType, new ResourceViewModel<R>());
    }

    public ResourceXYView(Window parent, AbstractChartPanelFactory<E> panelFactory, String title, Preferences pref, ModalityType modalityType, boolean allowsMultipleResources, boolean allowsMultipleTypes, ResourceViewModel<R> model)
    {
        super(parent, panelFactory, title, pref, modalityType, allowsMultipleResources, allowsMultipleTypes, model); 

        JMenuItem modifyMeasurementsStyleItem = new JMenuItem(modifyMeasurementStyle);
        JMenuItem showMeasurementsItem = new JMenuItem(showMeasurementsAction);
        JMenuItem measureLineItem = new JCheckBoxMenuItem(measureLineAction);

        this.measurementMenu = new JMenu("Measure");
        measurementMenu.add(modifyMeasurementsStyleItem);
        measurementMenu.add(showMeasurementsItem);

        measurementMenu.addSeparator();
        measurementMenu.add(measureLineItem);

        JMenuBar menuBar = getMenuBar();
        menuBar.add(measurementMenu);

        controlForResourceEmptinessPrivate(isEmpty());  
    }

    protected void drawingChartsFinished()
    {
        setVisible(true);
    }

    protected abstract void showDistanceMeasurements();

    protected abstract void showMeasurementEditor();

    protected JMenu getMeasurementMenu()
    {
        return measurementMenu;
    }

    protected Action getMeasureAction()
    {
        return measureLineAction;
    }

    protected List<DistanceMeasurementStyle> getDistanceMeasurementStylesForCurrentType()
    {
        String currentType = getSelectedType();

        List<DistanceMeasurementStyle> styles = getDistanceMeasurementStyles(currentType);   
        return styles;
    }

    protected List<DistanceMeasurementStyle> getDistanceMeasurementStyles(String type)
    {
        List<DistanceMeasurementStyle> styles = new ArrayList<>();

        MultipleXYChartPanel<? extends ChannelChart<?>> panel = getPanel(type);

        if(panel != null)
        {
            for(ChannelChart<?> chart : panel.getCharts())
            {
                if(chart != null)
                {
                    DistanceMeasurementStyle style = chart.getDistanceMeasurementStyle();
                    styles.add(style);
                }
            }           
        }

        return styles;
    }

    /////// DATA ITEM MOVEMENT //////

    @Override
    public void itemMoved(Channel1D channel, int itemIndex, double[] newValue)
    {}

    @Override
    public void channelTranslated(Channel1D channel)
    {}

    @Override
    public boolean isValidValue(Channel1D channel, int itemIndex, double[] newValue)
    {
        return true;
    }

    @Override
    public Point2D correctPosition(Channel1D channel, int itemIndex, Point2D dataPoint)
    {
        return dataPoint;
    }

    @Override
    public void itemAdded(Channel1D chanel, double[] itemNew)
    {}

    @Override
    public ChannelGroupTag getNextGroupMemberTag(Object groupKey)
    {
        return new ChannelGroupTag(groupKey, 0);
    }

    @Override
    public void channelAdded(Channel1D channel)
    {}

    @Override
    public void channelRemoved(Channel1D channel)
    {}

    /////////////////////////////////

    @Override
    public void handleNewChartPanel(E panel)
    {
        super.handleNewChartPanel(panel);
        panel.setDistanceMeasurementSupervisor(this);
        panel.setDataModificationSupervisor(this);
    }

    protected void enableAllActions(boolean enabled) 
    {
        modifyMeasurementStyle.setEnabled(enabled);
        measureLineAction.setEnabled(enabled);
    }

    protected void updateMeasurementsAvailability(boolean measurementsAvailable) 
    {
        showMeasurementsAction.setEnabled(measurementsAvailable);

        for (E panel : getPanels())
        {
            panel.setDistanceMeasurementsAvailable(measurementsAvailable);
        }        
    }

    @Override
    protected void setConsistentWithMode(MouseInputMode modeOld, MouseInputMode modeNew) 
    {
        super.setConsistentWithMode(modeOld, modeNew);

        boolean isLineDistanceMeasurement = MouseInputModeStandard.DISTANCE_MEASUREMENT_LINE.equals(modeNew);
        measureLineAction.putValue(Action.SELECTED_KEY, isLineDistanceMeasurement);

        boolean isAnyMeasurement = modeNew.isMeasurement();
        if(isAnyMeasurement)
        {
            showDistanceMeasurements();
        }
    }

    @Override
    public void controlForSelectedChartEmptiness(boolean empty)
    {
        super.controlForSelectedChartEmptiness(empty);
        boolean enabled = !empty;
        enableAllActions(enabled); 
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
        enableAllActions(enabled);
    }

    private class MeasureLineAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public MeasureLineAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/Compass.png"));

            putValue(LARGE_ICON_KEY, icon);
            //putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME, "Line measurement");
            putValue(SHORT_DESCRIPTION, "Line measurement");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean selected = (boolean) getValue(SELECTED_KEY);
            MouseInputMode mode = selected ?  MouseInputModeStandard.DISTANCE_MEASUREMENT_LINE : MouseInputModeStandard.NORMAL;
            setMode(mode);
        }
    }


    private class ShowDistanceMeasurementsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowDistanceMeasurementsAction() 
        {           
            putValue(NAME, "Show measurements");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            showDistanceMeasurements();
        }
    }

    private class ModifyDistanceMeasurementStyleAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ModifyDistanceMeasurementStyleAction()
        {
            putValue(NAME, "Measurement style");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            showMeasurementEditor();
        }
    }
}
