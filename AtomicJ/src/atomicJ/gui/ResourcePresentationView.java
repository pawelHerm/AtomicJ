
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

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import atomicJ.data.Channel1D;
import atomicJ.data.SampleCollection;
import atomicJ.gui.editors.FontChooserDialog;
import atomicJ.gui.save.SaveableChartSource;
import atomicJ.resources.DataModelResource;
import atomicJ.resources.Resource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.SerializationUtilities;


public abstract class ResourcePresentationView<R extends DataModelResource,V extends CustomizableXYBaseChart<?>,E extends MultipleChartPanel<V>> 
extends AbstractChartView<V,E> implements StyleSupervisor
{
    public static final String PRESENTATION_DIALOG_FONT = "PresentationDialogFont";
    public static final String PRESENTATION_DIALOG_USE_SHORT_NAME = "PresentationDialogUseShortName";

    private final ItemList<R> resourceList = new ItemList<>();
    private final ResourceCellRenderer resourceRenderer;

    private JPopupMenu resourceListPopup;
    private final JPanel panelResources;
    private boolean empty = true;

    private final Action clearAction = new ClearAction();
    private final Action saveAction = new SaveAction();
    private final Action saveAllAction = new SaveAllAction();
    private final Action printAction = new PrintAction();

    private final Action showRawDataAction = new ShowRawDataAction();
    private final Action useShortNamesAction = new UseShortNamesAction();
    private final Action changeListFontAction = new ChangeListFontAction();
    private final Action saveSelectedAction = new SaveSelectedAction();
    private final Action openAllInDialogsAction = new OpenAllInDialogsAction(); 
    private final Action openSelectedInDialogsAction = new OpenSelectedInDialogs();

    private final Action closeAction = new CloseAction();	
    private final EditChartAction editChartAction = new EditChartAction();

    private FontChooserDialog fontChooserDialog;

    private final JMenu chartMenu = new JMenu("Chart");
    private final JMenu fileMenu = new JMenu("File");

    private final Set<DataChangeListener<String>> dataChangeListeners = new LinkedHashSet<>();

    private final boolean allowsMultipleResources;
    private final boolean allowsMultipleTypes;

    private final ResourceViewModel<R> model;

    public ResourcePresentationView(final Window parent, AbstractChartPanelFactory<E> panelFactory, String title, Preferences pref, ModalityType modalityType)
    {
        this(parent, panelFactory, title, pref, modalityType, true, true);
    }

    public ResourcePresentationView(final Window parent, AbstractChartPanelFactory<E> panelFactory, String title, Preferences pref, ModalityType modalityType, boolean allowsMultipleResources, boolean allowsMultipleTypes)
    {
        this(parent, panelFactory, title, pref, modalityType, allowsMultipleResources, allowsMultipleTypes, new ResourceViewModel<>());
    }

    public ResourcePresentationView(final Window parent, AbstractChartPanelFactory<E> panelFactory, String title, Preferences pref, ModalityType modalityType, boolean allowsMultipleResources, boolean allowsMultipleTypes, ResourceViewModel<R> resourceModel)
    {
        super(parent, panelFactory, title, pref, modalityType);

        this.model = resourceModel;

        this.allowsMultipleResources = allowsMultipleResources;
        this.allowsMultipleTypes = allowsMultipleTypes;

        this.resourceRenderer = new ResourceCellRenderer(pref);

        this.resourceListPopup = buildResourceListPopupMenu();
        this.panelResources = buildResourcesPane();

        boolean useShortName = pref.getBoolean(PRESENTATION_DIALOG_USE_SHORT_NAME, false);
        useShortNamesAction.putValue(Action.SELECTED_KEY, useShortName);

        Font resourceListFont = (Font)SerializationUtilities.getSerializableObject(pref, PRESENTATION_DIALOG_FONT, resourceList.getFont());

        resourceRenderer.setUseShortName(useShortName);
        resourceList.setFont(resourceListFont);

        resourceList.setCellRenderer(resourceRenderer);

        initDataListener();
        initListSelectionListeners();
        initSelectionModelListener();
        initKeyListener();
        initMouseListener();
        initPropertyChangeListener();

        initInputAndActionMaps();

        buildMenuBar();

        controlForResourceEmptinessPrivate(isEmpty());	
    }

    private void initDataListener()
    {
        model.addDataModelListener(new ResourceGroupListener<R>() {

            @Override
            public void resourceCleared() 
            {
                resourceList.clear();
            }

            @Override
            public void resourceSet(int index, R resourceOld, R resourceNew) 
            {
                resourceList.set(index, resourceNew);
            }

            @Override
            public void resourceAdded(R resource) 
            {
                resourceList.addItem(resource);
            }

            @Override
            public void resourcesAdded(List<? extends R> resourcesAdded) 
            {
                resourceList.addItems(resourcesAdded);
            }

            @Override
            public void resourceRemoved(int index, R removedResource)
            {
                resourceList.deleteItemtAt(index);                
            }

            @Override
            public void resourcesRemoved(List<? extends R> resourcesRemoved)
            {
                resourceList.deleteItems(resourcesRemoved);
            }
        });
    }

    private void initPropertyChangeListener()
    {
        model.addPropertyChangeListener(ResourceViewModel.ALL_RESOURCES_EMPTY, 
                new PropertyChangeListener() 
        {                   
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                checkIfEmpty();
            }
        });
    }

    private void initSelectionModelListener()
    {
        model.addSelectionListener(new SelectionListener<R>()
        {
            @Override
            public void selectionChanged(SelectionEvent<? extends R> event) 
            {    
                int indexNew = event.getSelectedIndexNew();
                int[] indicesNew = event.getSelectedIndicesNew();

                R selectedResourceOld = event.getSelectedItemOld();
                R selectedResourceNew = event.getSelectedItemNew();
                List<R> allSelectedResources = new ArrayList<>(event.getSelectedItems());

                if(!Arrays.equals(indicesNew, resourceList.getSelectedIndices()))
                {
                    resourceList.setSelectedIndices(indicesNew);                  
                }

                if(indicesNew.length == 1)
                {
                    resourceList.ensureIndexIsVisible(indexNew);
                }

                if(!Objects.equals(selectedResourceOld, selectedResourceNew))
                {           
                    if(indexNew > -1)
                    {
                        setSelectedCharts(indexNew);               
                    }

                    revalidate();

                    handleChangeOfSelectedResource(selectedResourceOld, selectedResourceNew);   
                }   

                //the selection change may occur even if the the main selected resource
                //does not change, i.e. selectedResourceNew
                //and the selectedResourceOld are equal.
                //This is because multiple resources are often selected, so the list allSelectedResource
                //may change even if the main selected resource does not
                handleSelectionChange(selectedResourceNew, allSelectedResources);
            }
        });
    }

    private void initListSelectionListeners()
    {
        resourceList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent event)
            {               
                if(!event.getValueIsAdjusting())
                {
                    int index = resourceList.getSelectedIndex();
                    int[] indices = resourceList.getSelectedIndices();                      

                    model.setSelectedResources(index, indices);
                }
            }
        });
    }



    private JMenuBar buildMenuBar()
    {
        JMenuBar menuBar = getMenuBar();

        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem itemSave = new JMenuItem(saveAction);
        JMenuItem itemSaveAll = new JMenuItem(saveAllAction);
        JMenuItem itemPrint = new JMenuItem(printAction);

        JMenuItem itemOpenInDialog = new JMenuItem(openSelectedInDialogsAction);
        JMenuItem itemOpenAllInDialogs = new JMenuItem(openAllInDialogsAction);

        JMenuItem itemClear = new JMenuItem(clearAction);
        JMenuItem itemClose = new JMenuItem(closeAction);

        fileMenu.add(itemSave);
        if(allowsMultipleResources || allowsMultipleTypes)
        {
            fileMenu.add(itemSaveAll);
        }
        fileMenu.add(itemPrint);
        fileMenu.addSeparator();
        fileMenu.add(itemOpenInDialog);
        if(allowsMultipleTypes)
        {
            fileMenu.add(itemOpenAllInDialogs);
        }

        fileMenu.addSeparator();
        fileMenu.add(itemClear);

        fileMenu.addSeparator();
        fileMenu.add(itemClose);

        JMenuItem editChartItem = new JMenuItem(editChartAction);

        chartMenu.setMnemonic(KeyEvent.VK_C);
        chartMenu.add(editChartItem);   

        menuBar.add(fileMenu);
        menuBar.add(chartMenu);

        menuBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredSoftBevelBorder(), BorderFactory.createEmptyBorder(1,1,1,1)));

        return menuBar;
    }

    public ResourceViewModel<R> getResourceModel()
    {
        return model;
    }

    protected void handleSelectionChange(R mainSelection, List<R> allSelected)
    {}

    @Override
    protected void setDeafultStyle(Preferences pref)
    {
        super.setDeafultStyle(pref);
        try
        {
            pref.putBoolean(PRESENTATION_DIALOG_USE_SHORT_NAME, resourceRenderer.getUseShortName());
            SerializationUtilities.putSerializableObject(pref, PRESENTATION_DIALOG_FONT, resourceList.getFont());
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    protected JMenu getFileMenu()   
    {
        return fileMenu;
    }

    protected JMenu getChartMenu()
    {
        return chartMenu;
    }

    protected Action getRawDataAction()
    {
        return showRawDataAction;
    }

    protected Action getCloseAction()
    {
        return closeAction;
    }

    protected Action getSaveAllAction()
    {
        return saveAllAction;
    }

    public boolean getAllowsMultipleResources()
    {
        return allowsMultipleResources;
    }

    public boolean getAllowsMultipleTypes()
    {
        return allowsMultipleTypes;
    }

    private void initInputAndActionMaps()
    {
        InputMap inputMap = panelResources.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        inputMap.put((KeyStroke) editChartAction.getValue(Action.ACCELERATOR_KEY), editChartAction.getValue(Action.NAME));

        ActionMap actionMap =  panelResources.getActionMap();
        actionMap.put(editChartAction.getValue(Action.NAME), editChartAction);
    }

    public void addDataChangeListener(DataChangeListener<String> listener)
    {
        dataChangeListeners.add(listener);
    }

    public void removeDataChangeListener(DataChangeListener<String> listener)
    {
        dataChangeListeners.remove(listener);
    }

    public void clearDataChangeListeners()
    {
        dataChangeListeners.clear();
    }

    public void fireDataChangeEvent(DataChangeEvent<String> event)
    {	    
        for(DataChangeListener<String> listener: dataChangeListeners)
        {
            listener.dataChanged(event);
        }
    }

    private JPanel buildMultipleResourcesButtonPanel()
    {	
        JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 5, 5));	
        JPanel buttonPanelOuter = new JPanel();

        JButton buttonClear = new JButton(clearAction);
        JButton buttonSaveAll = new JButton(saveAllAction);
        JButton buttonClose = new JButton(closeAction);

        buttonPanel.add(buttonClear);
        buttonPanel.add(buttonSaveAll);
        buttonPanel.add(buttonClose);

        buttonPanelOuter.add(buttonPanel);
        buttonPanelOuter.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return buttonPanelOuter;
    }

    private JPanel buildResourcesPane()
    {
        JScrollPane resourcesPane  = new JScrollPane(resourceList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        JPanel scrollPanePanel = new JPanel(new BorderLayout());
        scrollPanePanel.add(resourcesPane,BorderLayout.CENTER);
        scrollPanePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        return scrollPanePanel;
    }

    protected JPanel getPanelResources()
    {
        return panelResources;
    }

    protected JPanel getMultipleResourcesPanelButtons()
    {
        return buildMultipleResourcesButtonPanel();
    }


    public void addResource(R resource, Map<String,V> charts)
    {
        addRowOfCharts(charts, getResourceCount(), true);

        model.addResource(resource);
    }

    public boolean updateResourceWithNewCharts(R resource, Map<String,V> charts)
    {
        boolean updated = false;

        int index = getResourceIndex(resource);

        if(index <= 0)
        {            
            setCharts(charts, index, true);
            updated = true;
        }

        return updated;
    }

    public void addOrUpdateResourceWithNewCharts(R resource, Map<String,V> charts)
    {
        boolean alreadyPresent = containsResource(resource);
        if(alreadyPresent)
        {
            updateResourceWithNewCharts(resource, charts);
        }
        else
        {
            addResource(resource, charts);
        }
    }

    public void addResources(Map<R, Map<String, V>> chartsMap)
    {             
        addCharts(chartsMap.values());
        model.addResources(chartsMap.keySet());
    }

    public void addOrUpdateResources(Map<R, Map<String, V>> chartsMap)
    {
        for(Entry<R, Map<String, V>> entry : chartsMap.entrySet())
        {
            R resource = entry.getKey();
            boolean alreadyPresent = containsResource(resource);
            if(alreadyPresent)
            {                
                int index = getResourceIndex(resource);
                setCharts(entry.getValue(), index, false); 
            }
            else
            {
                addRowOfCharts(entry.getValue(), getResourceCount(), false);
                model.addResource(resource);
            }
        }

        revalidate();
    }

    public void addOrReplaceResources(Map<R, Map<String, V>> chartsMap, Map<R, R> newVsOldResource)
    {
        Set<R> alreadyRemovedOld = new LinkedHashSet<>();

        for(Entry<R, Map<String, V>> entry : chartsMap.entrySet())
        {
            R resourceNew = entry.getKey();
            R resourceOld = newVsOldResource.get(resourceNew);

            boolean resourceToReplace = (resourceOld != null) && containsResource(resourceOld);
            if(resourceToReplace)
            {                
                int index = getResourceIndex(resourceOld);
                Map<String, V> chartsNew = entry.getValue();

                //deletes old charts
                deleteCharts(index);
                setCharts(chartsNew, index, false); 

                model.setResource(index, resourceNew);
                alreadyRemovedOld.add(resourceOld);
            }
            else
            {
                addRowOfCharts(entry.getValue(), getResourceCount(), false);
                model.addResource(resourceNew);
            }
        }

        Set<R> resourcesOldStillToRemove = new LinkedHashSet<>(newVsOldResource.values()); 
        resourcesOldStillToRemove.removeAll(alreadyRemovedOld);

        model.removeResources(resourcesOldStillToRemove);

        revalidate();
    }

    public void replaceData(Map<R, Map<String, Collection<? extends Channel1D>>> chartsMap, Map<R, R> newVsOldResource)
    {
        for(Entry<R, Map<String, Collection<? extends Channel1D>>> entry : chartsMap.entrySet())
        {
            R resourceNew = entry.getKey();
            R resourceOld = newVsOldResource.get(resourceNew);

            boolean resourceToReplace = (resourceOld != null) && containsResource(resourceOld);

            if(resourceToReplace)
            {                
                int index = getResourceIndex(resourceOld);
                Map<String, Collection<? extends Channel1D>> dataNew = entry.getValue();

                for(Entry<String, Collection<? extends Channel1D>> innerEntry : dataNew.entrySet())
                {
                    String type = innerEntry.getKey();
                    V chart = getChart(resourceOld, type);
                    chart.resetData(innerEntry.getValue());                 
                }

                model.setResource(index, resourceNew);               
                model.setSelectedResource(resourceNew);        
            }         
        }
    }

    public void selectResource(int index)
    {
        R resourceNew = model.getResource(index);
        selectResource(resourceNew);
    }

    public R getResource(String shortName)
    {
        return model.getResource(shortName);
    }

    public void selectResource(String shortName)
    {
        model.setSelectedResource(shortName);
    }

    public void selectResource(R resourceNew)
    { 
        model.setSelectedResource(resourceNew);        
    }

    public boolean containsResource(Resource resource)
    {
        boolean contains = model.containsResource(resource);
        return contains;
    }

    public boolean containsChannelsFromSource(ChannelSource source)
    {
        return model.containsChannelsFromSource(source);
    }

    public R getResourceContainingChannelsFrom(ChannelSource source)
    {
        return model.getResourceContainingChannelsFrom(source);
    }

    public List<R> getResourcesContainingChannelsFrom(ChannelSource source)
    {
        return model.getResourcesContainingChannelsFrom(source);     
    }

    public R getSelectedResource()
    {
        return model.getSelectedResource();
    }

    public List<R> getAllSelectedResources()
    {
        return model.getAllSelectedResources();
    }

    public int getSelectedIndex()
    {
        return model.getSelectedIndex();
    }

    public int[] getIndicesOfAllSelectedResources()
    {
        return model.getIndicesOfAllSelectedResources();          		
    }

    public int getResourceIndex(Object resource)
    {
        return model.getResourceIndex(resource);
    }

    public R getResource(int index)
    {
        return model.getResource(index);
    }

    public List<R> getResources()
    {
        return model.getResources();	
    }

    @Override
    public Map<String, String> getDefaultChartTitles()
    {                
        return model.getDefaultChartTitles();
    }

    public V getChart(Resource resource, String type)
    {
        int index = getResourceIndex(resource);

        return getChart(type, index);
    }

    public List<String> getNonemptyTypes(R resource)
    {
        int index = getResourceIndex(resource);
        return getNonEmptyTypes(index);
    }

    public Map<Resource, List<String>> getResourceTypeMap()
    {
        Map<Resource, List<String>> resourceTypeMap = new LinkedHashMap<>();

        int n  = resourceList.getItemCount();

        for(int i = 0; i<n; i++)
        {
            R resource = resourceList.getElementAt(i);
            List<String> types = getNonEmptyTypes(i);

            resourceTypeMap.put(resource, types);
        }

        return resourceTypeMap;
    }

    public Map<Resource, List<String>> getResourceTypeMap(List<String> retainable)
    {
        Map<Resource, List<String>> resourceTypeMap = new LinkedHashMap<>();

        int n = model.getResourceCount();

        for(int i = 0; i<n; i++)
        {
            R resource = model.getResource(i);
            List<String> types = getNonEmptyTypes(i);
            types.retainAll(retainable);

            resourceTypeMap.put(resource, types);
        }

        return resourceTypeMap;
    }

    public void handleChangeOfSelectedResource(R resourceOld, R resourceNew) 
    {
        File defaultOutputLocation = resourceNew == null ? null : resourceNew.getDefaultOutputLocation();

        for(E panel : getPanels())
        {
            panel.setDefaultDirectoryForSaveAs(defaultOutputLocation);
        }
    }

    @Override
    protected void handleChangeOfSelectedType(String typeOld, String typeNew)
    {
        super.handleChangeOfSelectedType(typeOld, typeNew);
        model.setSelectedType(typeNew);
    }

    @Override
    public void controlForSelectedChartEmptiness(boolean empty)
    {
        super.controlForSelectedChartEmptiness(empty);

        boolean enabled = !empty;

        saveAction.setEnabled(enabled);
        printAction.setEnabled(enabled);

        openSelectedInDialogsAction.setEnabled(enabled);

        editChartAction.setEnabled(enabled);
        showRawDataAction.setEnabled(enabled);
    }

    //we need private 'copy' of the controlForResourceEmptiness, because it is called in the constructor
    //we must copy private copy, so that it is not overridden
    public void controlForResourceEmptiness(boolean empty)
    {
        controlForResourceEmptinessPrivate(empty);
    }

    private void controlForResourceEmptinessPrivate(boolean empty)
    {
        boolean enabled = !empty;

        saveAction.setEnabled(enabled);
        saveAllAction.setEnabled(enabled);
        printAction.setEnabled(enabled);

        openSelectedInDialogsAction.setEnabled(enabled);
        openAllInDialogsAction.setEnabled(enabled);

        showRawDataAction.setEnabled(enabled);
        clearAction.setEnabled(enabled);
    }

    public boolean isEmpty()
    {
        return empty;
    }

    @Override
    public void handleNewChartPanel(E panel) 
    {
        super.handleNewChartPanel(panel);

        R resource = getSelectedResource();
        if(resource != null)
        {
            File defaultOutputLocation = resource.getDefaultOutputLocation();
            panel.setDefaultDirectoryForSaveAs(defaultOutputLocation);       
        }

        checkIfEmpty();     
    }   


    @Override
    protected void removeChartPanel(Object type)
    {
        super.removeChartPanel(type);

        checkIfEmpty();
    }

    @Override
    public void clear()
    {
        super.clear();
        model.clearResources();
    }

    protected void saveSelected()
    {
        SelectionSaveableChartSource<V> saveable = new SelectionSaveableChartSource<>();
        showSaveDialog(saveable);
    }

    public void openAllChartsForSelectedResourceInDialogs()
    {
        int[] selectedIndices = resourceList.getSelectedIndices();  
        openChartsInSeparateDialogs(selectedIndices);
    }

    public void openChartsOfSelectedTypeForSelectedResourceInDialogs()
    {
        int[] selectedIndices = resourceList.getSelectedIndices();  
        openChartsOfSelectedTypeInSeparateDialogs(selectedIndices);
    }

    protected void showAllRawResourceData()
    {        
        List<R> sourcesSelected = getAllSelectedResources();
        List<SampleCollection> rawData = new ArrayList<>();

        for(R resource : sourcesSelected)
        {
            List<SampleCollection> collections = resource.getSampleCollections();
            for(SampleCollection collection : collections)
            {
                collection.setKeysIncluded(true);
                rawData.add(collection);
            }				
        }

        publishRawData(rawData);
    }

    public void publishRawData(List<SampleCollection> sampleCollections) 
    {
        publishRawData(sampleCollections, false);
    }

    public void publishRawData(List<SampleCollection> sampleCollections, boolean sampleNamesInHeaders) 
    {
        if (!sampleCollections.isEmpty()) 
        {
            Map<String, StandardNumericalTable> tables = new LinkedHashMap<>();
            for (SampleCollection collection : sampleCollections) 
            {
                String collectionName = collection.getShortName();
                NumericalTableModel model = new RawDataTableModel(collection, sampleNamesInHeaders);
                StandardNumericalTable table = new OrderedNumericalTable(model, true);
                tables.put(collectionName, table);
            }

            MultipleNumericalTableView dialog = new MultipleNumericalTableView(SwingUtilities.getWindowAncestor(getAssociatedWindow()), tables, "Raw data", true);
            dialog.setVisible(true);
        }
    }

    public void setUseShortNames(boolean useShortNames)
    {
        resourceRenderer.setUseShortName(useShortNames);
        resourceList.repaint();
    }

    public void attemptListFontSelection() 
    {
        if(fontChooserDialog == null)
        {
            this.fontChooserDialog = new FontChooserDialog(SwingUtilities.getWindowAncestor(getAssociatedWindow()), "Font selection");
        }

        this.fontChooserDialog.showDialog(new FontReceiver() 
        {

            @Override
            public void setFont(Font newFont) 
            { 
                resourceList.setFont(newFont);
                resourceList.repaint();
            }

            @Override
            public Font getFont()
            {
                return resourceList.getFont();
            }
        });
    }

    @Override
    public List<File> getDefaultOutputLocations(String key) 
    {
        List<File> outputLocations = new ArrayList<>();
        List<R> resources = getResources();
        List<? extends V> charts = getAllCharts(key);

        int n = resources.size();

        for(int i = 0;i<n;i++)
        {
            Resource resource = resources.get(i);
            V chart = charts.get(i);
            if(chart != null)
            {
                outputLocations.add(resource.getDefaultOutputLocation());
            }
        }
        return outputLocations;
    }

    public List<File> getDefaultOutputLocations(String key, int[] indices) 
    {
        List<File> outputLocations = new ArrayList<>();

        for(int i : indices)
        {
            Resource resource = getResource(i);
            V chart = getChart(key, i);
            if(chart != null)
            {
                outputLocations.add(resource.getDefaultOutputLocation());
            }
        }
        return outputLocations;
    }

    @Override
    public List<String> getDefaultOutputNames(String key) 
    {
        List<String> names = new ArrayList<>();
        List<R> resources = getResources();
        List<? extends V> charts = getAllCharts(key);

        int n = resources.size();

        for(int i = 0;i<n;i++)
        {
            Resource resource = resources.get(i);
            V chart = charts.get(i);
            if(chart != null)
            {
                names.add(resource.getShortName());
            }
        }
        return names;
    }

    public List<String> getDefaultOutputNames(String key, int[] indices) 
    {
        List<String> outputNames = new ArrayList<>();

        for(int i : indices)
        {
            Resource resource = getResource(i);
            V chart = getChart(key, i);
            if(chart != null)
            {
                outputNames.add(resource.getShortName());
            }
        }
        return outputNames;
    }


    @Override
    public int getResourceCount()
    {
        return model.getResourceCount();
    }

    //returns indices in increasing order,
    //ONLY of THOSE RESOURCES THAT ARE CONTAINED IN THE RESOURCE LIST OF THIS OBJECT
    public int[] getIndicesOfPresentResources(Set<?> resources)
    {
        return model.getIndicesOfPresentResources(resources);
    }

    public void removeResources(Set<?> resources)
    {
        int[] indicesArray = getIndicesOfPresentResources(resources);
        removeItems(indicesArray);
    }

    //items must be in increasing order
    public void removeItems(int[] indices)
    {
        Arrays.sort(indices);

        deleteCharts(indices);
        model.removeResources(indices);
    }

    private void checkIfEmpty()
    {
        boolean resourceEmpty = model.isDataEmpty();
        boolean typeEmpty = (getTypeCount() == 0);
        boolean emptyNew = resourceEmpty || typeEmpty;

        boolean emptyOld = this.empty;
        this.empty = emptyNew;	

        fireDataAvailabilityChanged(!emptyNew);

        if(emptyOld != emptyNew)
        {
            controlForResourceEmptiness(emptyNew);
        }
    }

    private void initKeyListener()
    {
        resourceList.addKeyListener(new KeyAdapter() 
        {
            @Override
            public void keyPressed(KeyEvent e) 
            {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                {
                    int[] selectedIndices = resourceList.getSelectedIndices();          		
                    removeItems(selectedIndices);
                }
            }
        });
    }

    public void setResourceListPopupMenu(JPopupMenu popup)
    {
        this.resourceListPopup = popup;
    }

    protected JPopupMenu getResourceListPopupMenu()
    {
        return resourceListPopup;
    }

    private JPopupMenu buildResourceListPopupMenu()
    {
        JPopupMenu popup = new JPopupMenu(); 		

        JMenuItem itemShowRawData = new JMenuItem(showRawDataAction);
        popup.add(itemShowRawData);

        popup.addSeparator();

        JMenuItem itemSaveSelected = new JMenuItem(saveSelectedAction);
        popup.add(itemSaveSelected);    

        popup.addSeparator();

        JCheckBoxMenuItem itemUseShortNames = new JCheckBoxMenuItem(useShortNamesAction);
        popup.add(itemUseShortNames);

        JMenuItem itemChangeFont = new JMenuItem(changeListFontAction);
        popup.add(itemChangeFont);

        popup.addSeparator();

        JMenuItem itemOpenInNewDialogs = new JMenuItem(openAllInDialogsAction);
        popup.add(itemOpenInNewDialogs);

        JMenuItem itemOpenSelectedInNewDialogs = new JMenuItem(openSelectedInDialogsAction);
        popup.add(itemOpenSelectedInNewDialogs);

        popup.addSeparator();

        JMenuItem itemDelete = new JMenuItem("Delete");
        itemDelete.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                int[] selectedIndices = resourceList.getSelectedIndices();          		
                removeItems(selectedIndices);
            }
        });
        popup.add(itemDelete);

        return popup;
    }

    private void initMouseListener()
    {
        MouseListener listener = new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)  {check(e);}

            @Override
            public void mouseReleased(MouseEvent e) {check(e);}

            private void check(MouseEvent e) 
            {
                if (resourceListPopup != null && e.isPopupTrigger() && resourceList.isNonEmpty()) 
                {
                    int clicked = resourceList.locationToIndex(e.getPoint());
                    if(!resourceList.isSelectedIndex(clicked))
                    {
                        resourceList.setSelectedIndex(clicked); 
                    }        
                    resourceListPopup.show(resourceList, e.getX(), e.getY());		    	
                }
            }
        };
        resourceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        resourceList.addMouseListener(listener);
    }

    private class OpenAllInDialogsAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public OpenAllInDialogsAction()
        {          
            putValue(NAME,"Open all in dialogs");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {	
            openAllChartsForSelectedResourceInDialogs();
        };
    }

    private class OpenSelectedInDialogs extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public OpenSelectedInDialogs()
        {
            putValue(NAME,"Open in dialog");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {	
            openChartsOfSelectedTypeForSelectedResourceInDialogs();
        };
    }

    private class UseShortNamesAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public UseShortNamesAction() 
        {
            putValue(NAME, "Short names");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean useShortNames = (boolean) getValue(SELECTED_KEY);
            setUseShortNames(useShortNames);            
        }
    }

    private class ChangeListFontAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ChangeListFontAction() 
        {
            putValue(NAME, "Change font");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            attemptListFontSelection();
        }
    }

    private class CloseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CloseAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
            putValue(NAME,"Close");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            close();
        };
    }

    private class SaveAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SaveAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME,"Save");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            save();
        };
    }

    private class SaveSelectedAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SaveSelectedAction()
        {
            //            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
            //            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME,"Save selected");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            saveSelected();
        };
    }

    private class SaveAllAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SaveAllAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME,"Save all");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            saveAll();
        };
    }

    private class PrintAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public PrintAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(NAME,"Print");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            print();
        };
    }

    private class ClearAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ClearAction()
        {
            putValue(NAME,"Clear");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {			
            clear();
        };
    }


    private class EditChartAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public EditChartAction() {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L,
                    InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Live chart style");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            editChartProperties();
        }
    }

    private class ShowRawDataAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ShowRawDataAction() 
        {
            //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME, "Raw data");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            showAllRawResourceData();
        }
    }

    private class SelectionSaveableChartSource <V> implements SaveableChartSource<V>
    {

        @Override
        public int getChartWidth() 
        {
            return getChartWidth();
        }

        @Override
        public int getChartHeight() 
        {
            return getChartHeight();
        }

        @Override
        public int getDataWidth() 
        {
            return getDataWidth();
        }

        @Override
        public int getDataHeight()
        {
            return getDataHeight();
        }

        @Override
        public Rectangle2D getChartArea() 
        {
            return getChartArea();
        }

        @Override
        public Rectangle2D getDataArea() 
        {
            return getDataArea();
        }

        @Override
        public List<String> getDefaultOutputNames(String key)
        {
            int[] selectedIndices = resourceList.getSelectedIndices();  
            List<String> outputNames = ResourcePresentationView.this.getDefaultOutputNames(key, selectedIndices);
            return outputNames;
        }

        @Override
        public List<File> getDefaultOutputLocations(String key) 
        {
            int[] selectedIndices = resourceList.getSelectedIndices(); 
            List<File> outputLocations = ResourcePresentationView.this.getDefaultOutputLocations(key, selectedIndices);

            return outputLocations;
        }

        @Override
        public List<V> getAllNonemptyCharts(String key) 
        {
            int[] selectedIndices = resourceList.getSelectedIndices();  
            List<V> outputCharts = (List<V>) ResourcePresentationView.this.getAllNonemptyCharts(key, selectedIndices);

            return outputCharts;
        }

    }
}
