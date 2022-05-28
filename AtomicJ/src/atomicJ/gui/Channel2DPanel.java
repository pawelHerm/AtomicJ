
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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.jfree.data.Range;

import atomicJ.data.QuantitativeSample;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.editors.LiveChartEditor;
import atomicJ.gui.editors.LiveChartEditorFactory;
import atomicJ.gui.profile.Profile;
import atomicJ.gui.profile.ProfileReceiver;
import atomicJ.gui.profile.ProfileStyle;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIComposite;
import atomicJ.gui.rois.ROIDrawable;
import atomicJ.gui.rois.ROIReceiver;
import atomicJ.gui.rois.ROIRelativePosition;
import atomicJ.gui.rois.ROIStyle;
import atomicJ.imageProcessing.FixMinimumOperation;
import atomicJ.imageProcessing.Channel2DDataInROITransformation;
import atomicJ.imageProcessing.PolynomialFitCorrection;
import atomicJ.utilities.ArrayUtilities;


public class Channel2DPanel <E extends Channel2DChart<?>> extends MultipleXYChartPanel<E>
implements ProfileReceiver, MapMarkerReceiver, ROIReceiver, ActionListener, PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private final Action editGradientAction = new EditGradientAction();
    private final Action editHistogramGradientAction = new EditHistogramGradientAction();
    private final Action showHistogramsAction = new ShowHistogramsAction();
    private final Action drawHistogramsAction = new DrawHistogramsAction();
    private final Action drawHistogramsForRoisAction = new DrawHistogramsForRoisAction();
    private final Action showRawDataAction = new ShowRawDataAction();
    private final Action showRawDataForRoisAction = new ShowRawDataForRoisAction();
    private final Action showStatisticsAction = new ShowStatisticsAction();
    private final Action showStatisticsForRoisAction = new ShowStatisticsForRoisAction();
    private final Action showROIShapeFactorsAction = new ShowShapeFactorsForRois();
    private final Action subtractROIFitAction = new SubtractROIFitAction();
    private final Action modifyProfileStyleAction = new ModifyProfileStyleAction();
    private final Action modifyROIStyleAction = new ModifyROIStyleAction();
    private final Action fixZeroAction = new FixZeroAction();

    private Channel2DSupervisor densitySupervisor;

    public Channel2DPanel(boolean addPopup)
    {
        this(addPopup, true);
    }

    public Channel2DPanel(boolean addPopup, boolean allowROIbasedActions)
    {
        this(addPopup, allowROIbasedActions, true);
    }

    public Channel2DPanel(boolean addPopup, boolean allowROIbasedActions, boolean allowStaticsGroupActions)
    {
        super(null, false);

        if(addPopup)
        {
            setPopupMenu(buildDenistyPanelPopupMenu(true, true, true, true, true, allowROIbasedActions, allowStaticsGroupActions));
        }

        initInputAndActionMaps();
    }

    public void registerPopupActions(String menuName, List<Action> actions)
    {
        JPopupMenu popup = getPopupMenu();

        JMenu menu = new JMenu(menuName);

        for(Action action : actions)
        {
            JMenuItem item = new JMenuItem(action);
            menu.add(item);
        }

        popup.addSeparator();
        popup.add(menu);      
    }

    private void initInputAndActionMaps()
    {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);

        inputMap.put((KeyStroke) fixZeroAction.getValue(Action.ACCELERATOR_KEY), fixZeroAction.getValue(Action.NAME));

        inputMap.put((KeyStroke) showHistogramsAction.getValue(Action.ACCELERATOR_KEY), showHistogramsAction.getValue(Action.NAME));
        inputMap.put((KeyStroke) drawHistogramsAction.getValue(Action.ACCELERATOR_KEY), drawHistogramsAction.getValue(Action.NAME));
        inputMap.put((KeyStroke) drawHistogramsForRoisAction.getValue(Action.ACCELERATOR_KEY), drawHistogramsForRoisAction.getValue(Action.NAME));

        inputMap.put((KeyStroke) showRawDataAction.getValue(Action.ACCELERATOR_KEY), showRawDataAction.getValue(Action.NAME));
        inputMap.put((KeyStroke) showRawDataForRoisAction.getValue(Action.ACCELERATOR_KEY), showRawDataForRoisAction.getValue(Action.NAME));   

        inputMap.put((KeyStroke) showStatisticsAction.getValue(Action.ACCELERATOR_KEY), showStatisticsAction.getValue(Action.NAME));
        inputMap.put((KeyStroke) showStatisticsForRoisAction.getValue(Action.ACCELERATOR_KEY), showStatisticsForRoisAction.getValue(Action.NAME));

        ActionMap actionMap =  getActionMap();
        actionMap.put(fixZeroAction.getValue(Action.NAME), fixZeroAction);

        actionMap.put(showHistogramsAction.getValue(Action.NAME), showHistogramsAction);
        actionMap.put(drawHistogramsAction.getValue(Action.NAME), drawHistogramsAction);
        actionMap.put(drawHistogramsForRoisAction.getValue(Action.NAME), drawHistogramsForRoisAction);

        actionMap.put(showRawDataAction.getValue(Action.NAME), showRawDataAction);
        actionMap.put(showRawDataForRoisAction.getValue(Action.NAME), showRawDataForRoisAction);

        actionMap.put(showStatisticsAction.getValue(Action.NAME), showStatisticsAction);
        actionMap.put(showStatisticsForRoisAction.getValue(Action.NAME), showStatisticsForRoisAction);
    }

    public boolean areROISamplesNeeded()
    {
        Channel2DChart<? extends Channel2DPlot> chart = getSelectedChart();
        boolean needed = (chart != null) ? chart.areROISamplesNeeded() : false;

        return needed;
    }

    public void setROISample(QuantitativeSample sample)
    {
        GradientPaintReceiver receiver = getGradientPaintReceiver();

        if(receiver == null)
        {
            return;
        }

        if(sample != null && sample.size() > 1)
        {
            double[] data = sample.getMagnitudes();
            Range range = ArrayUtilities.getBoundedRange(data);

            receiver.setLowerROIBound(range.getLowerBound());
            receiver.setUpperROIBound(range.getUpperBound()); 
        }
        else
        {
            receiver.setLensToFull();
        }       
    }

    public GradientPaintReceiver getGradientPaintReceiver()
    {
        Channel2DChart<? extends Channel2DPlot> chart = getSelectedChart();
        GradientPaintReceiver receiver = (chart != null) ? chart.getGradientPaintReceiver() : null;

        return receiver;
    }



    public void setHistogramsAvialable(boolean available)
    {
        showHistogramsAction.setEnabled(available);
    }

    @Override
    protected LiveChartEditor getEditorDialog(E selectedChart, List<E> boundCharts, Window parent)
    {
        return LiveChartEditorFactory.getInstance().getEditor(selectedChart, boundCharts, parent, 1);
    }

    @Override
    public void setSelectedChart(E chartNew)
    {
        Channel2DChart<?> oldChart = getSelectedChart();
        if(oldChart != null)
        {
            oldChart.removePropertyChangeListener(this);
            removeChartMouseWheelListener(oldChart);
        }

        if(chartNew != null)
        {
            chartNew.addPropertyChangeListener(this);
            addChartMouseWheelListener(chartNew);						
        }

        super.setSelectedChart(chartNew);
    }

    public void setDensitySupervisor(Channel2DSupervisor supervisor)
    {
        this.densitySupervisor = supervisor;

        for(E chart : getCharts())
        {
            if(chart != null)
            {
                chart.setDensitySupervisor(supervisor);
            }
        }

        setHistogramsAvialable(supervisor.areHistogramsAvaialable());
    }

    @Override
    public List<E> getStyleBoundedCharts()
    {
        if(densitySupervisor == null)
        {
            return super.getStyleBoundedCharts();
        }
        else
        {
            return (List<E>) densitySupervisor.getAllNonEmptyCharts().allValues();
        }
    }

    @Override
    protected void handleChartAddition(E chart)
    {
        super.handleChartAddition(chart);

        if(chart != null)
        {
            chart.setDensitySupervisor(densitySupervisor);
        }
    }

    @Override
    protected void handleChartAddition(List<E> charts)
    {
        super.handleChartAddition(charts);

        for(E chart : charts)
        {
            if(chart != null)
            {
                chart.setDensitySupervisor(densitySupervisor);
            }
        }       
    }

    ////////////////////PROFILES///////////////////////////////

    @Override
    public void setProfiles(Map<Object, Profile> profiles)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.setProfiles(profiles);
        }	
    }


    @Override
    public void addOrReplaceProfile(Profile profile)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.addOrReplaceProfile(profile);
        }   
    }

    @Override
    public void removeProfile(Profile profile)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.removeProfile(profile);
        }
    }

    public void setProfilesAvailable(boolean available)
    {}

    @Override
    public void addProfileKnob(Object profileKey, double knobNewPosition)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.addProfileKnob(profileKey, knobNewPosition);
        }
    }

    @Override
    public void moveProfileKnob(Object profileKey, int knobIndex, double knobNewPosition)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.moveProfileKnob(profileKey, knobIndex, knobNewPosition);
        }
    }

    @Override
    public void removeProfileKnob(Object profileKey, double knobPosition)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.removeProfileKnob(profileKey, knobPosition);
        }
    }


    public void setProfileKnobPositions(int index, Object profileKey, List<Double> knobPositions)
    {
        int n = getChartCount();

        if(index >= 0 && index <n)
        {
            Channel2DChart<?> selectedChart = getChartAt(index);
            if(selectedChart != null)
            {
                selectedChart.setProfileKnobPositions(profileKey, knobPositions);
            }
        }

    }	

    public void setProfileKnobPositions(Object profileKey, List<Double> knobPositions)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.setProfileKnobPositions(profileKey, knobPositions);
        }
    }

    public List<ProfileStyle> getProfileStyles()
    {
        List<ProfileStyle> styles = new ArrayList<>();
        for(Channel2DChart<?> chart : getCharts())
        {
            if(chart != null)
            {
                ProfileStyle style = chart.getProfileStyle();
                styles.add(style);
            }
        }   

        return styles;
    }

    ////////////////////////////////////////////////////////////////////////////


    @Override
    public void setMapMarkers(Map<Object, MapMarker> mapMarkers)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.setMapMarkers(mapMarkers);
        }   
    }

    @Override
    public void addOrReplaceMapMarker(MapMarker mapMarker)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.addOrReplaceMapMarker(mapMarker);
        }   
    }

    @Override
    public void removeMapMarker(MapMarker mapMarker)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.removeMapMarker(mapMarker);
        }
    }

    public List<MapMarkerStyle> getMapMarkerStyles()
    {
        List<MapMarkerStyle> styles = new ArrayList<>();

        for(Channel2DChart<?> chart : getCharts())
        {
            if(chart != null)
            {
                MapMarkerStyle style = chart.getMapMarkerStyle();
                styles.add(style);
            }
        }     

        return styles;
    }

    ///////////////////////////////////////////////////////////////////////////

    public void addMarkerKnob(Object markerKey, double knobPosition)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.addValueMarkerKnob(markerKey, knobPosition);
        }
    }

    public void moveMarkerKnob(Object markerKey, int knobIndex, double knobNewPosition)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.moveValueMarkerKnob(markerKey, knobIndex, knobNewPosition);
        }
    }

    public void removeMarkerKnob(Object markerKey, int knobIndex)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.removeValueMarkerKnob(markerKey, knobIndex);
        }
    }

    public void setROIHoleMode(boolean roiHoleMode)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.setROIHoleMode(roiHoleMode);
        }   
    }

    public ROI getROIUnion()
    {
        if(densitySupervisor != null)
        {
            return densitySupervisor.getROIUnion();
        }	
        return ROIComposite.getROIForRois(new ArrayList<ROI>(), "All");

    }

    @Override
    public void addOrReplaceROI(ROIDrawable roi)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.addOrReplaceROI(roi);
        }	
    }

    public void addOrReplaceROI(ROIDrawable roi, ROIStyle style)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.addOrReplaceROI(roi, style);
        }   
    }

    @Override
    public void removeROI(ROIDrawable roi)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.removeROI(roi);
        }
    }

    public void setROIs(Map<Object, ROIDrawable> rois, ROIStyle style)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.setROIs(rois, style);
        }   
    }


    @Override
    public void setROIs(Map<Object, ROIDrawable> rois)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.setROIs(rois);
        }	
    }

    @Override
    public void changeROILabel(Object roiKey, String labelOld, String labelNew)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            selectedChart.changeROILabel(roiKey, labelOld, labelNew);
        }   
    }

    public void setROIBasedActionsEnabled(boolean available)
    {
        boolean isNull = (getSelectedChart() == null);
        boolean enabled = (available && !isNull);

        if(drawHistogramsForRoisAction != null)
        {
            drawHistogramsForRoisAction.setEnabled(enabled);
        }
        if(showRawDataForRoisAction != null)
        {
            showRawDataForRoisAction.setEnabled(enabled);
        }		
        if(showROIShapeFactorsAction != null)
        {
            showROIShapeFactorsAction.setEnabled(enabled);
        }
        if(showStatisticsForRoisAction != null)
        {
            showStatisticsForRoisAction.setEnabled(enabled);		
        }
        if(subtractROIFitAction != null)
        {
            subtractROIFitAction.setEnabled(enabled);		
        }	
    }

    public List<ROIStyle> getROIStyles()
    {
        List<ROIStyle> styles = new ArrayList<>();

        for(Channel2DChart<?> chart : getCharts())
        {
            if(chart != null)
            {
                ROIStyle style = chart.getROIStyle();
                styles.add(style);
            }
        }    

        return styles;
    }

    ///////////////////////////////////////////////////////////////////////////

    public List<String> getValueLabelTypes()
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            return selectedChart.getValueLabelTypes();
        }

        return Collections.emptyList();
    }

    public Map<String, String> getValueLabels(Point2D dataPoint)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            return selectedChart.getValueLabels(dataPoint);   
        }
        return Collections.emptyMap();
    }


    public PrefixedUnit getZAxisUnit()
    {
        Channel2DChart<?> selectedChart = getSelectedChart();
        if(selectedChart != null)
        {
            return selectedChart.getZDisplayedUnit();   
        }
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        Object source = evt.getSource();

        if(Channel2DSupervisor.ROI_SAMPLES_NEEDED.equals(evt.getPropertyName()))
        {
            boolean neededNew = (boolean)evt.getNewValue();
            if(neededNew)
            {
                densitySupervisor.notifyAboutROISampleNeeded();
            }
        }

        else if(source == getSelectedChart())
        {
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }

    protected final JPopupMenu buildDenistyPanelPopupMenu(boolean properties, boolean copy, boolean save, boolean print, boolean zoom, boolean roiRestrictedActions, boolean channelWideActions) 
    {
        JPopupMenu popupMenu = super.createPopupMenu(properties, copy, save, print, zoom);
        int n = popupMenu.getComponentCount();	

        popupMenu.insert(new JMenuItem(editGradientAction), 1);
        popupMenu.insert(new JMenuItem(editHistogramGradientAction), 2);
        popupMenu.insert(new JMenuItem(modifyProfileStyleAction), 3);
        popupMenu.insert(new JMenuItem(modifyROIStyleAction), 4);

        int index = n + 5;

        if(channelWideActions && roiRestrictedActions)
        {
            JMenu drawHistograms = new JMenu("Histograms");
            popupMenu.insert(drawHistograms,index++);

            drawHistograms.add(new JMenuItem(drawHistogramsAction));
            drawHistograms.add(new JMenuItem(drawHistogramsForRoisAction));
            drawHistograms.add(new JMenuItem(showHistogramsAction));              

            JMenu statistics = new JMenu("Statistics");
            popupMenu.insert(statistics,index++);  

            statistics.add(new JMenuItem(showStatisticsAction));
            statistics.add(new JMenuItem(showStatisticsForRoisAction));

            JMenu rawData = new JMenu("Raw data");
            popupMenu.insert(rawData,index++);

            rawData.add(new JMenuItem(showRawDataAction));
            rawData.add(new JMenuItem(showRawDataForRoisAction));

            popupMenu.insert(new JMenuItem(subtractROIFitAction), index++);
            popupMenu.insert(new JMenuItem(fixZeroAction), index++);
        }
        else
        {
            if(channelWideActions)
            {
                popupMenu.insert(new JMenuItem(drawHistogramsAction),index++);
                popupMenu.insert(new JMenuItem(showHistogramsAction), index++);
                popupMenu.insert(new JMenuItem(showStatisticsAction),index++);  
                popupMenu.insert(new JMenuItem(showRawDataAction),index++);
                popupMenu.insert(new JMenuItem(fixZeroAction), index++);
            }

            if(roiRestrictedActions)
            {
                popupMenu.addSeparator();

                popupMenu.insert(new JMenuItem(drawHistogramsForRoisAction), index++); 
                popupMenu.insert(new JMenuItem(showStatisticsForRoisAction), index++);
                popupMenu.insert(new JMenuItem(showRawDataForRoisAction), index++);
                popupMenu.insert(new JMenuItem(showROIShapeFactorsAction), index++);
                popupMenu.insert(new JMenuItem(subtractROIFitAction), index++);
            }
        }

        return popupMenu;
    }

    @Override
    public boolean isDomainZoomable()
    {
        ChannelChart<?> selectedChart = getSelectedChart();

        boolean zoomable = false;       

        if(selectedChart != null)
        {
            boolean zoomableSuper = super.isDomainZoomable();						
            zoomable = zoomableSuper && selectedChart.isDomainZoomable();
        }

        return zoomable;
    }

    @Override
    public boolean isRangeZoomable()
    {
        ChannelChart<?> selectedChart = getSelectedChart();

        boolean zoomable = false;       

        if(selectedChart != null)
        {
            boolean zoomableSuper = super.isRangeZoomable();
            zoomable = zoomableSuper && selectedChart.isRangeZoomable();
        }

        return zoomable;
    }

    @Override
    public boolean isPopupDisplayable(int x, int y)
    {
        Channel2DChart<?> selectedChart = getSelectedChart();

        boolean displayable = false;

        if(selectedChart != null)
        {
            Point2D dataPoint = getDataPoint(new Point(x, y));
            displayable = selectedChart.isPopupDisplayable(dataPoint);
        }

        return displayable;
    }

    private class ShowHistogramsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowHistogramsAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Show histograms");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(densitySupervisor != null)
            {
                densitySupervisor.showHistograms();
            }
        }
    }

    private class DrawHistogramsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public DrawHistogramsAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Draw histograms");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(densitySupervisor != null)
            {
                densitySupervisor.drawHistograms();
            }
        }
    }

    private class DrawHistogramsForRoisAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public DrawHistogramsForRoisAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Draw ROI histograms");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(densitySupervisor != null)
            {
                densitySupervisor.drawROIHistograms();	
            }  
        }
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
            if(densitySupervisor != null)
            {
                densitySupervisor.showRawData();
            }
        }
    }

    private class ShowRawDataForRoisAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowRawDataForRoisAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Raw data for ROIs");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(densitySupervisor != null)
            {
                densitySupervisor.showROIRawData();	
            }  
        }
    }

    private class ShowShapeFactorsForRois extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowShapeFactorsForRois()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Shape factors for ROIs");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(densitySupervisor != null)
            {
                densitySupervisor.showROIShapeFactors();
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
            if(densitySupervisor != null)
            {
                densitySupervisor.showStatistics();
            }
        }
    }

    private class ShowStatisticsForRoisAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowStatisticsForRoisAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Statistics ROIs");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(densitySupervisor != null)
            {
                densitySupervisor.showROIStatistics();     
            }
        }
    }

    private class SubtractROIFitAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SubtractROIFitAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Correct ROI plane");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(densitySupervisor != null)
            {
                Channel2DDataInROITransformation tr = new PolynomialFitCorrection(new int[][] {{0,1},{1}});
                densitySupervisor.transform(tr, ROIRelativePosition.INSIDE);
            }
        }
    }

    private class ModifyProfileStyleAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ModifyProfileStyleAction()
        {			
            //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Profile style");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            densitySupervisor.setProfileStyleEditorVisible(true);
        }
    }

    private class ModifyROIStyleAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ModifyROIStyleAction()
        {			
            //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"ROI style");
        }
        @Override
        public void actionPerformed(ActionEvent event)
        {
            densitySupervisor.setROIStyleEditorVisible(true);
        }
    }

    private class EditGradientAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public EditGradientAction()
        {			
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Edit color gradient");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            densitySupervisor.editGradient();
        }
    }

    private class EditHistogramGradientAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public EditHistogramGradientAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G,	InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Gradient histogram");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            densitySupervisor.editHistogramGradient();
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
            densitySupervisor.transform(new FixMinimumOperation(0, false));
        }
    }


    public static class DensityPanelFactory implements AbstractChartPanelFactory<Channel2DPanel<Channel2DChart<?>>>
    {
        private static final  DensityPanelFactory INSTANCE = new DensityPanelFactory();

        private final boolean addPopup;
        private final boolean allowROIbasedActions;
        private final boolean allowStaticsGroupActions;

        public static DensityPanelFactory getInstance()
        {
            return INSTANCE;
        }

        public static DensityPanelFactory getInstance(boolean addPopup, boolean allowROIbasedActions, boolean allowStaticsGroupActions)
        {
            return new DensityPanelFactory(addPopup, allowROIbasedActions, allowStaticsGroupActions);
        }

        public DensityPanelFactory()
        {
            this(true, true, true);
        }

        public DensityPanelFactory(boolean addPopup, boolean allowROIbasedActions, boolean allowStaticsGroupActions)
        {
            this.addPopup = addPopup;
            this.allowROIbasedActions = allowROIbasedActions;
            this.allowStaticsGroupActions = allowStaticsGroupActions;
        }

        @Override
        public Channel2DPanel<Channel2DChart<?>> buildEmptyPanel() 
        {
            return new Channel2DPanel<>(addPopup, allowROIbasedActions, allowStaticsGroupActions);
        }
    }

    @Override
    public void setProfileKnobPositions(Object resource, Object profileKey,
            List<Double> knobPositions) {

    }
}
