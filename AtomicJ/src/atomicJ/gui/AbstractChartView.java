
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

import static atomicJ.gui.PreferenceKeys.WINDOW_HEIGHT;
import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_X;
import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_Y;
import static atomicJ.gui.PreferenceKeys.WINDOW_WIDTH;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;

import atomicJ.gui.save.BatchSaveDialog;
import atomicJ.gui.save.SaveableChartSource;
import atomicJ.utilities.GUIUtilities;
import atomicJ.utilities.MultiMap;

public abstract class AbstractChartView <V extends CustomizableXYBaseChart<?>, 
E extends MultipleChartPanel<V>> implements ChangeListener, SaveableChartSource<V>
{
    private static final int DEFAULT_HEIGHT = (int)Math.round(0.92*Toolkit.getDefaultToolkit().getScreenSize().height);
    private static final int DEFAULT_WIDTH = (int)Math.min(1.2*DEFAULT_HEIGHT, Toolkit.getDefaultToolkit().getScreenSize().width);	

    private static final int DEFAULT_LOCATION_X = Math.round((Toolkit.getDefaultToolkit().getScreenSize().width - DEFAULT_WIDTH)/2);
    private static final int DEFAULT_LOCATION_Y = Math.round((Toolkit.getDefaultToolkit().getScreenSize().height - DEFAULT_HEIGHT)/2);

    private String selectedType;

    protected final Map<String, E> panels = new LinkedHashMap<>();
    private final JTabbedPane tabPane = new JTabbedPane();
    private final AbstractChartPanelFactory<E> panelFactory;
    private final BatchSaveDialog batchSaveDialog;

    private boolean chartEmpty;

    private MouseInputMode mode = MouseInputModeStandard.NORMAL;
    private Map<MouseInputType, MouseInputMode> accessoryModes = new HashMap<>();

    private final DataViewSupport dataViewSupport = new DataViewSupport();

    private final JDialog viewDialog;

    public AbstractChartView(final Window parent, AbstractChartPanelFactory<E> panelFactory, String title, final Preferences pref, ModalityType modalityType)
    {
        this.viewDialog = new JDialog(parent, title, modalityType);

        JMenuBar menuBar = new JMenuBar();
        viewDialog.setJMenuBar(menuBar);
        viewDialog.setLayout(new BorderLayout());

        this.batchSaveDialog = new BatchSaveDialog(viewDialog, pref);
        this.panelFactory = panelFactory;

        viewDialog.addComponentListener(new ComponentAdapter()
        {          
            @Override
            public void componentShown(ComponentEvent evt)
            {
                dataViewSupport.fireDataViewVisiblityChanged(true);
            }
        });

        //we use WindowListener to detect closing of the viewDialog
        //instead of ComponentListener. The componentHidden method of ComponentListener
        //is not called when a window is disposed on closing (i.e if we called viewDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //thus, using WindowListener is more reliable

        viewDialog.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {                   
                dataViewSupport.fireDataViewVisiblityChanged(false);
                setDeafultStyle(pref);
            }
        });

        int height = pref.getInt(WINDOW_HEIGHT, getDefaultWidth());
        int width = pref.getInt(WINDOW_WIDTH, getDefaultHeight());
        int locationX = pref.getInt(WINDOW_LOCATION_X, DEFAULT_LOCATION_X);
        int locationY = pref.getInt(WINDOW_LOCATION_Y, DEFAULT_LOCATION_Y);

        if(GUIUtilities.areWindowSizeAndLocationWellSpecified(width, height, locationX, locationY))
        {            
            viewDialog.setSize(width, height); 
            viewDialog.setLocation(locationX,locationY);
        }   
        else
        {
            viewDialog.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            viewDialog.setLocation(DEFAULT_LOCATION_X, DEFAULT_LOCATION_Y);
        }

        this.selectedType = findSelectedType();
        this.chartEmpty = (getSelectedChart() == null);

        initChangeListener();
    }

    public void dispose()
    {
        viewDialog.dispose();
    }

    protected JMenuBar getMenuBar()
    {
        return viewDialog.getJMenuBar();
    }

    public void setCenterComponent(Component component)
    {
        viewDialog.add(component, BorderLayout.CENTER);
    }

    public void setEastComponent(Component component)
    {
        viewDialog.add(component, BorderLayout.EAST);
    }

    public void setNorthComponent(Component component)
    {
        viewDialog.add(component, BorderLayout.NORTH);
    }

    public void setSouthComponent(Component component)
    {
        viewDialog.add(component, BorderLayout.SOUTH);
    }

    protected void registerActionAcceleratorKeysInInputMaps(List<Action> actions)
    {        
        InputMap inputMap = viewDialog.getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW );                

        ActionMap actionMap = viewDialog.getRootPane().getActionMap();

        for(Action action : actions)
        {
            inputMap.put((KeyStroke) action.getValue(Action.ACCELERATOR_KEY), action.getValue(Action.NAME));  
            actionMap.put(action.getValue(Action.NAME), action); 
        }
    }

    public boolean isVisible()
    {
        boolean visible = viewDialog.isVisible();
        return visible;
    }

    public void revalidate()
    {
        viewDialog.revalidate();
        viewDialog.repaint();
    }

    public void setVisible(boolean visible)
    {
        viewDialog.setVisible(visible);
    }

    public void addDataViewListener(DataViewListener listener)
    {
        dataViewSupport.addDataViewListener(listener);
    }

    public void removeDataViewListener(DataViewListener listener)
    {
        dataViewSupport.removeDataViewListener(listener);
    }

    protected void fireDataAvailabilityChanged(boolean dataAvailableNew)
    {
        dataViewSupport.fireDataAvailabilityChanged(dataAvailableNew);
    }

    public int getTypePosition(String type)
    {
        return tabPane.indexOfTab(type);
    }

    public Window getAssociatedWindow()
    {
        return viewDialog;
    }

    public void showErrorMessage(String message)
    {
        JOptionPane.showMessageDialog(viewDialog, message, AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
    }

    public void showInformationMessage(String message)
    {
        JOptionPane.showMessageDialog(viewDialog, message, AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    protected void setDeafultStyle(Preferences pref)
    {
        pref.putInt(WINDOW_HEIGHT, viewDialog.getHeight());
        pref.putInt(WINDOW_WIDTH, viewDialog.getWidth());
        pref.putInt(WINDOW_LOCATION_X, (int) viewDialog.getLocation().getX());			
        pref.putInt(WINDOW_LOCATION_Y, (int) viewDialog.getLocation().getY());
    }

    private void initChangeListener()
    {
        tabPane.addChangeListener(this);
    }

    @Override
    public Rectangle2D getChartArea()
    {
        List<E> panelList = new ArrayList<>(panels.values());
        MultipleChartPanel<?> firstPanel = panelList.get(0);

        if(firstPanel != null)
        {
            ChartRenderingInfo info = firstPanel.getChartRenderingInfo();       
            Rectangle2D chartArea = info.getChartArea();

            return chartArea;
        }
        return new Rectangle2D.Double();
    }

    @Override
    public Rectangle2D getDataArea()
    {
        List<E> panelList = new ArrayList<>(panels.values());
        MultipleChartPanel<?> firstPanel = panelList.get(0);

        if(firstPanel != null)
        {
            PlotRenderingInfo info = firstPanel.getChartRenderingInfo().getPlotInfo();      
            Rectangle2D dataArea = info.getDataArea();

            return dataArea;
        }
        return new Rectangle2D.Double();
    }

    @Override
    public int getChartWidth()
    {
        int width = 0;

        List<E> panelList = new ArrayList<>(panels.values());
        MultipleChartPanel<?> firstPanel = panelList.get(0);

        if(firstPanel != null)
        {
            ChartRenderingInfo info = firstPanel.getChartRenderingInfo();

            Rectangle2D chartArea = info.getChartArea();
            width = (int)Math.rint(chartArea.getWidth());
        }

        return width;       
    }

    @Override
    public int getChartHeight()
    {
        int height = 0;

        List<E> panelList = new ArrayList<>(panels.values());
        MultipleChartPanel<?> firstPanel = panelList.get(0);

        if(firstPanel != null)
        {
            ChartRenderingInfo info = firstPanel.getChartRenderingInfo();

            Rectangle2D chartArea = info.getChartArea();
            height = (int)Math.rint(chartArea.getHeight());
        }

        return height;      
    }

    @Override
    public int getDataWidth()
    {
        int width = 0;

        List<E> panelList = new ArrayList<>(panels.values());
        MultipleChartPanel<?> firstPanel = panelList.get(0);

        if(firstPanel != null)
        {        
            ChartRenderingInfo info = firstPanel.getChartRenderingInfo();

            Rectangle2D dataArea = info.getPlotInfo().getDataArea();
            width = (int)Math.rint(dataArea.getWidth());
        }

        return width;      
    }

    @Override
    public int getDataHeight()
    {
        int height = 0;

        List<E> panelList = new ArrayList<>(panels.values());
        MultipleChartPanel<?> firstPanel = panelList.get(0);

        if(firstPanel != null)
        {       
            ChartRenderingInfo info = firstPanel.getChartRenderingInfo();

            Rectangle2D dataArea = info.getPlotInfo().getDataArea();
            height = (int)Math.rint(dataArea.getHeight());
        }

        return height;          
    }

    public List<V> getAllCharts(String key)
    {
        MultipleChartPanel<V> panel = panels.get(key);
        List<V> charts = panel.getCharts();	
        return charts;
    } 

    @Override
    public List<V> getAllNonemptyCharts(String key)
    {
        MultipleChartPanel<V> panel = panels.get(key);

        List<V> charts = (panel != null) ? new ArrayList<>(panel.getCharts()) : new ArrayList<>();

        charts.removeAll(Collections.singletonList(null));	
        return charts;
    }

    public MultiMap<String, V> getAllNonEmptyCharts()
    {
        MultiMap<String, V> allCharts = new MultiMap<>();

        for(Entry<String, E> entry : panels.entrySet())
        {
            String key = entry.getKey();

            MultipleChartPanel<V> panel = entry.getValue();

            List<V> charts = (panel != null) ? new ArrayList<>(panel.getCharts()) : new ArrayList<>();

            charts.removeAll(Collections.singletonList(null));  

            allCharts.putAll(key, charts);
        }


        return allCharts;
    }

    public List<V> getAllNonemptyCharts(String key, int[] indices)
    {
        MultipleChartPanel<V> panel = panels.get(key);

        List<V> charts = new ArrayList<>();

        if(panel != null)
        {
            for(int i : indices)
            {
                V chart = panel.getChartAt(i);
                if(chart != null)
                {
                    charts.add(chart);
                }
            }
        }

        return charts;
    }

    public List<V> getAllNonEmptyCharts(int index)
    {
        List<V> charts = new ArrayList<>();

        for(E panel: panels.values())
        {
            int n = panel.getChartCount();
            if(index < n)
            {
                V chart = panel.getChartAt(index);

                if(chart != null)
                {
                    charts.add(chart);
                }
            }
        }

        return charts;
    }

    public List<String> getNonEmptyTypes(int index)
    {
        List<String> types = new ArrayList<>();

        for(Entry<String,E> entry: panels.entrySet())
        {
            String type = entry.getKey();
            E panel = entry.getValue();

            int n = panel.getChartCount();
            if(index<n)
            {
                V chart = panel.getChartAt(index);

                if(chart != null)
                {
                    types.add(type);
                }
            }
        }

        return types;
    }

    protected void deleteChart(String key, int index)
    {
        MultipleChartPanel<?> panel = panels.get(key);
        if(panel != null)
        {
            panel.deleteChart(index);
        }
        checkIfSelectedChartIsEmpty();
    }

    protected void deleteCharts(int index)
    {
        for(MultipleChartPanel<?> panel: panels.values())
        {
            if(panel.getChartCount() > index)
            {
                panel.deleteChart(index);
            }		
        }
        checkIfSelectedChartIsEmpty();
    }

    protected void deleteCharts(int[] indices)
    {
        for(MultipleChartPanel<?> panel: panels.values())
        {          
            panel.deleteCharts(indices);                  
        }
        checkIfSelectedChartIsEmpty();
    }

    protected void deleteCharts(String key)
    {
        MultipleChartPanel<?> panel = panels.get(key);
        if(panel != null)
        {
            panel.clear();
        }
        checkIfSelectedChartIsEmpty();
    }

    protected void deleteAllCharts()
    {
        for(MultipleChartPanel<?> panel: panels.values())
        {
            panel.clear();
        }
        checkIfSelectedChartIsEmpty();
    }

    protected void addChartPanelIfAbsent(String type)
    {
        boolean panelPresent = panels.containsKey(type);
        if(!panelPresent)
        {
            E panel = panelFactory.buildEmptyPanel(); 

            panels.put(type, panel);
            tabPane.add(panel, type);

            batchSaveDialog.addKey(type);
            batchSaveDialog.pack();

            panel.setMode(mode);

            handleNewChartPanel(panel);
        }	
    }

    protected void insertChartPanel(String type, int index)
    {
        E panel = panelFactory.buildEmptyPanel();       
        insertChartPanel(type, panel, index);
    }

    protected void insertChartPanel(String type, E panel, int index)
    {
        panels.put(type, panel);
        tabPane.insertTab(type, null, panel, "", index);

        batchSaveDialog.addKey(type);
        batchSaveDialog.pack();

        panel.setMode(mode);

        handleNewChartPanel(panel);
    }

    protected void handleNewChartPanel(E panel)
    {}

    protected void removeChartPanel(Object type)
    {
        E panel = panels.get(type);

        if(panel != null)
        {
            panel.clear();
            panels.remove(type);
            tabPane.remove(panel);
        }
    }

    protected void addRowOfCharts(Map<String,V> valueMap, int index, boolean revalidate)
    {
        Set<String> keysNotAdded = new HashSet<>(getTypes());

        for(Entry<String, V> innerEntry: valueMap.entrySet())
        {
            String key = innerEntry.getKey();
            V chart = innerEntry.getValue();

            boolean containsKey = containsType(key);
            if(!containsKey)
            {
                addChartPanelIfAbsent(key);

                E panel = panels.get(key);           

                for(int i = 0 ;i<index;i++)
                {
                    panel.addChart(null);                 
                }
            }

            addChart(key, chart);	
            keysNotAdded.remove(key);
        }

        for(String key: keysNotAdded)
        {
            addChart(key, null);
        }

        if(revalidate)
        {
            viewDialog.revalidate();
        }
    }

    public void setCharts(Map<String,V> charts, int index, boolean revalidate)
    { 
        for(Entry<String, V> entry : charts.entrySet())
        {
            String type = entry.getKey();
            V chart = entry.getValue();

            addChartPanelIfAbsent(type);                              
            setSelectedChart(type, chart, index);
        }   

        if(revalidate)
        {
            viewDialog.revalidate();
        }
    }


    public void addCharts(Collection<? extends Map<String, V>> chartsMap)
    {        
        int index = getResourceCount();

        for(Map<String, V> entry : chartsMap)
        {
            addRowOfCharts(entry, index++, false);
        }

        viewDialog.revalidate();
    }

    private void addChart(String type, V chart)
    {
        addChartPanelIfAbsent(type);

        E panel =  panels.get(type);			
        panel.addChart(chart);	
    }

    public List<V> getSelectedCharts()
    {
        List<V> charts = new ArrayList<>();

        for(String type: panels.keySet())
        {
            MultipleChartPanel<V> panel = panels.get(type);		
            V chart = panel.getSelectedChart();
            charts.add(chart);		
        }
        return charts;
    }

    public void setSelectedCharts(int index)
    {
        List<String> visibleKeys = getNonEmptyTypes(index);


        for(String key: visibleKeys)
        {
            MultipleChartPanel<?> panel = panels.get(key);
            panel.setSelectedChart(index);
        }

        setVisiblePanels(visibleKeys);

        checkIfSelectedChartIsEmpty();
    }

    public V getSelectedChart()
    {
        String selectedKey = getSelectedType();	
        E panel = panels.get(selectedKey);       
        V chart = (panel != null) ? panel.getSelectedChart() : null;

        return chart;
    }

    public V getSelectedChart(String key)
    {
        E panel = panels.get(key);
        V chart = (panel != null) ? panel.getSelectedChart() : null;

        return chart;
    }

    public V getSelectedChart(int index)
    {
        E panel = panels.get(getSelectedType());
        return panel.getChartAt(index);
    }

    public V getChart(String type, int index)
    {
        E panel = panels.get(type);
        V chart = (panel != null) ? panel.getChartAt(index) : null;

        return chart;
    }

    public void setSelectedChart(String type, int index)
    {
        MultipleChartPanel<?> panel = panels.get(type);
        if(panel != null && panel.getChartCount()>index)
        {
            panel.setSelectedChart(index);
            boolean visible = (panel.getSelectedChart() != null);
            setPanelVisible(type, visible);
        }
        checkIfSelectedChartIsEmpty();
    }


    //sets chart 'chart' in the position 'index' in the panel
    //corresponding to the string 'type'. If the panel for
    //'type' cannot be found, the panel is created and added to the dialog

    public void setSelectedChart(String type, V chart, int index)
    {                
        E panel =  panels.get(type);            

        if(panel != null)
        {
            panel.setChartAt(chart, index);           
            boolean visible = (panel.getSelectedChart() != null);       
            setPanelVisible(type, visible);

            checkIfSelectedChartIsEmpty();
        }
    }


    public String getSelectedType()
    {
        return selectedType;
    }

    private String findSelectedType()
    {
        String type = null;

        int index = tabPane.getSelectedIndex();        
        if(index >= 0)
        {
            type = tabPane.getTitleAt(index);
        }

        return type;
    }


    public E getSelectedPanel()
    {
        E panel = null;

        String type = getSelectedType();
        if(type != null)
        {
            panel = panels.get(type);
        }

        return panel;
    }

    public void setSelectedType(String type)
    {
        int tabCount = tabPane.getTabCount();
        for(int i = 0; i <tabCount; i++) 
        {
            String title = tabPane.getTitleAt(i);
            if(title.equals(type)) 
            {
                tabPane.setSelectedIndex(i);
                return;
            }
        }
        checkIfSelectedChartIsEmpty();
    }

    private void ensureThatPanelIsVisible(String type)
    {
        boolean visible = false;
        int tabCount = tabPane.getTabCount();
        for(int i = tabCount - 1; i >= 0; i--) 
        {
            String title = tabPane.getTitleAt(i);
            if(title.equals(type)) 
            {
                visible = true;
                break;
            }
        }
        if(!visible)
        {
            MultipleChartPanel<?> panel = panels.get(type);
            tabPane.addTab(type, panel);
        }
    }

    private void ensureThatPanelIsHidden(String type)
    {
        int tabCount = tabPane.getTabCount();

        for(int i = tabCount - 1; i >= 0; i--) 
        {
            String title = tabPane.getTitleAt(i);
            if(title.equals(type)) 
            {
                tabPane.removeTabAt(i);
                break;
            }
        }
    }	

    public void setPanelVisible(String type, boolean visible)
    {
        if(visible)
        {
            ensureThatPanelIsVisible(type);
        }
        else
        {
            ensureThatPanelIsHidden(type);
        }
    }

    public void setVisiblePanels(List<String> visibleTypes)
    {         
        tabPane.removeChangeListener(this);

        int indexOld = tabPane.indexOfTab(selectedType);

        tabPane.removeAll();

        for(String type: visibleTypes)
        {
            MultipleChartPanel<?> panel = panels.get(type);
            tabPane.addTab(type, panel);
        }

        int oldTypeIndex = tabPane.indexOfTab(selectedType);
        int indexNew = oldTypeIndex > - 1 ? oldTypeIndex : Math.min(indexOld, tabPane.getTabCount() - 1);

        tabPane.setSelectedIndex(indexNew);      
        tabPane.addChangeListener(this);

        handleChangeOfSelectedType();       
    }

    protected Map<String, V> getCharts(int index)
    {
        Map<String, V> charts = new LinkedHashMap<>();

        for(Entry<String,E> entry: panels.entrySet())
        {
            String key = entry.getKey();
            MultipleChartPanel<V> panel = entry.getValue();

            if(panel.getChartCount()>index)
            {
                V chart = panel.getChartAt(index);
                if(chart != null)
                {
                    charts.put(key, chart);
                }
            }
        }

        return charts;
    }

    protected void clear()
    {
        deleteAllCharts();
        panels.clear();
        tabPane.removeAll();
    }

    protected Collection<E> getPanels()
    {
        return panels.values();
    }

    protected E getPanel(Object profileKey)
    {
        return panels.get(profileKey);
    }

    protected void openChartsInSeparateDialogs(int[] selectedIndices)
    {
        for(int index: selectedIndices)
        {
            Map<String, V> charts = getCharts(index);
            for(Entry<String, V> entry : charts.entrySet())
            {
                String key = entry.getKey();

                String name = getDefaultOutputNames(key).get(index);
                String title = name + " " + key;
                V chart = entry.getValue();
                JDialog dialog = new SingleChartPresentationDialog(viewDialog, chart, title);
                dialog.setVisible(true);
            }
        }
    }

    protected void openChartsOfSelectedTypeInSeparateDialogs(int[] selectedIndices)
    {		
        for(int index: selectedIndices)
        {
            String selectedType = getSelectedType();
            V chart = getChart(selectedType, index);

            if(chart != null)
            {
                String name = getDefaultOutputNames(selectedType).get(index);
                String title = name + " " + selectedType;
                JDialog dialog = new SingleChartPresentationDialog(viewDialog, chart, title);
                dialog.setVisible(true);
            }
        }	
    }

    protected JTabbedPane getTabbedPane()
    {			
        return tabPane;
    }

    protected void showSaveDialog()
    {
        batchSaveDialog.showDialog(this);
    }

    protected void showSaveDialog(SaveableChartSource<V> chartSource)
    {
        batchSaveDialog.showDialog(chartSource);
    }

    public Set<String> getTypes()
    {
        Set<String> keys = panels.keySet();
        return keys;
    }

    public int getTypeCount()
    {
        int count = panels.size();
        return count;
    }

    protected boolean containsType(String key)
    {
        Set<String> keys = getTypes();
        return keys.contains(key);
    }

    protected void handleChangeOfSelectedType(String typeOld, String typeNew)
    {}

    protected void controlForSelectedChartEmptiness(boolean empty)
    {}

    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        handleChangeOfSelectedType();
    }

    private void handleChangeOfSelectedType()
    {
        int index = tabPane.getSelectedIndex();

        String typeOld = selectedType;
        String typeNew = index >= 0 ? tabPane.getTitleAt(index) : null;

        this.selectedType = typeNew;

        handleChangeOfSelectedType(typeOld, typeNew);

        checkIfSelectedChartIsEmpty();
    }

    @Override
    public abstract List<File> getDefaultOutputLocations(String key);

    @Override
    public abstract List<String> getDefaultOutputNames(String key);

    protected abstract int getResourceCount();

    private void checkIfSelectedChartIsEmpty()
    {
        boolean chartEmptyOld = chartEmpty;
        chartEmpty = (getSelectedChart() == null);

        if(chartEmptyOld != chartEmpty)
        {
            controlForSelectedChartEmptiness(chartEmpty);
        }
    }

    public void requestCursorChange(Cursor cursor) 
    {        
        MultipleChartPanel<V> panel = getSelectedPanel();
        if(panel != null)
        {
            panel.setCursor(cursor);
        }
    }

    public void requestCursorChange(Cursor horizontalCursor, Cursor verticalCursor)
    {
        MultipleChartPanel<V> panel = getSelectedPanel();
        if(panel != null)
        {
            V chart = panel.getSelectedChart();
            boolean isVertical = (chart.getCustomizablePlot().getOrientation() == PlotOrientation.VERTICAL);
            Cursor cursor = isVertical ? verticalCursor : horizontalCursor;
            panel.setCursor(cursor);
        }
    }   

    public MouseInputMode getMode()
    {
        return mode;
    }

    public void setMode(MouseInputMode modeNew)
    {
        setMode(modeNew, true);
    }

    public void setMode(MouseInputMode modeNew, boolean clearAcessoryModels)
    {
        MouseInputMode modeOld = this.mode;
        this.mode = modeNew;
        setConsistentWithMode(modeOld, modeNew);
        if(clearAcessoryModels)
        {
            clearAccessoryModels();
        }
    }

    public MouseInputMode getMode(MouseInputType inputType)
    {
        MouseInputMode accessoryMode = this.accessoryModes.get(inputType);

        if(accessoryMode != null)
        {
            return accessoryMode;
        }

        return this.mode;
    }

    public void setAccessoryMode(MouseInputType inputType, MouseInputMode modeNew)
    {        
        MouseInputMode modeOld = getMode(inputType);

        this.accessoryModes.put(inputType, modeNew);     
        setConsistentWithAccessoryMode(inputType, modeOld, modeNew);
    }

    public Set<MouseInputType> getMouseInputTypesForAccessoryMode(MouseInputMode mode)
    {
        Set<MouseInputType> mouseInputTypes = new LinkedHashSet<>();

        for(Entry<MouseInputType, MouseInputMode> entry : accessoryModes.entrySet())
        {
            if(Objects.equals(mode, entry.getValue()))
            {
                mouseInputTypes.add(entry.getKey());
            }
        }

        return mouseInputTypes;
    }

    private void clearAccessoryModels()
    {
        Map<MouseInputType, MouseInputMode> acessoryModesOld = new HashMap<>(accessoryModes);
        this.accessoryModes = new HashMap<>();

        for(Entry<MouseInputType, MouseInputMode> entry : acessoryModesOld.entrySet())
        {
            setConsistentWithAccessoryMode(entry.getKey(), entry.getValue(), null);
        }
    }

    protected void setConsistentWithAccessoryMode(MouseInputType inputType, MouseInputMode modeOld, MouseInputMode modeNew)
    {
        for (E panel : getPanels()) {
            panel.setAccessoryMode(inputType, modeNew);
        }
    }

    protected void setConsistentWithMode(MouseInputMode modeOld, MouseInputMode modeNew) 
    {
        for (E panel : getPanels()) {
            panel.setMode(modeNew);
        }
    }

    public void setMouseInputsReservedForTools(Set<MouseInputType> types)
    {        
        Set<MouseInputType> previouslyReservedTyps = getMouseInputTypesForAccessoryMode(MouseInputModeStandard.TOOL_MODE);

        for(MouseInputType mouseInputType : types)
        {
            setAccessoryMode(mouseInputType, MouseInputModeStandard.TOOL_MODE);
        }

        Set<MouseInputType> previouslyButNotNowReserved = new LinkedHashSet<>(previouslyReservedTyps);
        previouslyButNotNowReserved.removeAll(types);

        for(MouseInputType mouseInputType : previouslyButNotNowReserved)
        {
            setAccessoryMode(mouseInputType, null);
        }
    }

    protected int getDefaultWidth() {
        return DEFAULT_WIDTH;
    }

    protected int getDefaultHeight()
    {
        return DEFAULT_HEIGHT;
    }

    protected void print()
    {
        E panel = getSelectedPanel();
        if(panel != null)
        {
            panel.createChartPrintJob();
        }
    }

    protected void editChartProperties()
    {
        E panel = getSelectedPanel();
        if(panel != null)
        {
            panel.doEditChartProperties();
        }
    }


    protected abstract void close();

    protected void save()
    {
        E panel = getSelectedPanel();
        if(panel != null)
        {
            try 
            {
                panel.doSaveAs();
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }
    }

    protected void saveAll()
    {
        showSaveDialog();       
    }

}
