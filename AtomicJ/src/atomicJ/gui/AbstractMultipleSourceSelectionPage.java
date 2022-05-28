
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

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.Border;

import atomicJ.data.ChannelFilter;
import atomicJ.data.SampleCollection;
import atomicJ.gui.curveProcessing.ProcessingBatchModelInterface;
import atomicJ.gui.curveProcessing.ProcessingModelInterface;
import atomicJ.gui.curveProcessing.ProcessingWizardModel;
import atomicJ.readers.ConcurrentReadingTask;
import atomicJ.readers.SourceReadingModel;
import atomicJ.sources.ChannelSource;



public abstract class AbstractMultipleSourceSelectionPage <E extends ChannelSource> extends AbstractWizardPage implements PropertyChangeListener, WizardPage
{
    private static final boolean DESKTOP_AVAILABLE = Desktop.isDesktopSupported()&&(!GraphicsEnvironment.isHeadless());

    private final Preferences pref = Preferences.userNodeForPackage(AbstractMultipleSourceSelectionPage.class).node(getClass().getName()).node("SourceSelection");

    private Desktop desktop;
    private boolean openSupported;

    private boolean allowForSourceFiltering;

    private final ItemList<E> sourceList = new ItemList<>();

    private final Action showRawDataAction = new ShowRawDataAction();
    private final Action openSelectedAction = new OpenSelectedExternallyAction();
    private final Action previewAction = new PreviewAction();
    private final Action previewSelectedAction = new PreviewSelectedAction();
    private final Action addAction = new AddAction();
    private final Action clearAction = new ClearAction();

    private final JButton buttonFilterSources = new JButton(new FilterSourcesAction());

    private final JLabel labelBatchNumber = new JLabel();	
    private final JPanel panelControls;
    private final SourceFileChooser<E> fileChooser;
    private final String freeLabel;
    private ResourceSelectionModel<E>  model;

    private final JPanel mainPanel = new JPanel();

    private ConcurrentReadingTask<E> currentReadingTask;

    private boolean necessaryInputProvided;

    public AbstractMultipleSourceSelectionPage(ResourceSelectionModel<E> model, SourceReadingModel<E> manager, String freeLabel)
    {
        this(model, manager, freeLabel, false);
    }

    public AbstractMultipleSourceSelectionPage(ResourceSelectionModel<E> model, SourceReadingModel<E> manager, String freeLabel, boolean sourceFilteringAllowed)
    {
        this(model, manager, freeLabel, sourceFilteringAllowed, "Preview");
    }

    public AbstractMultipleSourceSelectionPage(ResourceSelectionModel<E> model, SourceReadingModel<E> manager, String freeLabel, boolean sourceFilteringAllowed, String openLabel)
    {
        setModel(model);

        this.allowForSourceFiltering = sourceFilteringAllowed;
        buttonFilterSources.setVisible(sourceFilteringAllowed);

        this.freeLabel = freeLabel;
        this.fileChooser = new SourceFileChooser<>(manager, pref);

        previewAction.putValue(Action.NAME, openLabel);
        previewSelectedAction.putValue(Action.NAME, openLabel);

        initDesktop();
        createAndRegisterPopupMenu();
        createAndRegisterKeyListener();

        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane  = new JScrollPane(sourceList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel scrollPanePanel = new JPanel(new BorderLayout());
        scrollPanePanel.add(scrollPane,BorderLayout.CENTER);
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),BorderFactory.createEmptyBorder(8,8,8,8));
        scrollPanePanel.setBorder(border);

        panelControls = buildControlPanel();		

        mainPanel.add(scrollPanePanel,BorderLayout.CENTER);

        initInputAndActionMaps();
    }

    protected void setAllowForSourceFiltering(boolean allowForSourceFiltering)
    {
        this.allowForSourceFiltering = allowForSourceFiltering;        
        buttonFilterSources.setVisible(allowForSourceFiltering);
    }

    private void initInputAndActionMaps()
    {
        InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);                
        inputMap.put((KeyStroke) showRawDataAction.getValue(Action.ACCELERATOR_KEY), showRawDataAction.getValue(Action.NAME));

        ActionMap actionMap = mainPanel.getActionMap();
        actionMap.put(showRawDataAction.getValue(Action.NAME), showRawDataAction);    
    }

    private void pullModelProperties()
    {
        List<E> sources = model.getSources();
        String identifier = model.getIdentifier();
        boolean modelRestricted = model.isRestricted();
        boolean filteringPossible = model.isSourceFilteringPossible();

        necessaryInputProvided = model.areSourcesSelected();

        setSourceListElements(sources);
        labelBatchNumber.setText(identifier);

        buttonFilterSources.setEnabled(filteringPossible);
        previewAction.setEnabled(necessaryInputProvided);
        addAction.setEnabled(!modelRestricted);
        clearAction.setEnabled(necessaryInputProvided && !modelRestricted);
    }

    public void setModel(ResourceSelectionModel<E> modelNew)
    {
        if(model != null)
        {
            model.removePropertyChangeListener(this);
        }

        this.model = modelNew;
        model.addPropertyChangeListener(this);

        pullModelProperties();
    }

    public void setTypeOfDataToRead(ChannelFilter typeOfData)
    {
        fileChooser.setTypeOfData(typeOfData);
    }

    public void setChooserCurrentDirectory(File dir)
    {
        fileChooser.setCurrentDirectory(dir);
    }

    public void setChooserSelectedFile(File file)
    {
        fileChooser.setSelectedFile(file);
    }

    public void cancel()
    {
        if(currentReadingTask != null)
        {
            if(!(currentReadingTask.isCancelled() || currentReadingTask.isDone()))
            {
                currentReadingTask.cancelAllTasks();
            }
        }
    }

    public ResourceSelectionModel<E> getModel()
    {
        return model;
    }

    private void setSourceListElements(List<E> sources)
    {
        sourceList.setItems(sources);
        sourceList.revalidate();	
    }

    @Override
    public Component getView()
    {
        return mainPanel;
    }

    @Override
    public Component getControls()
    {
        return panelControls;
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return necessaryInputProvided;
    }	

    @Override
    public void propertyChange(final PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if(ProcessingBatchModelInterface.SOURCES.equals(name))
        {
            @SuppressWarnings("unchecked")
            List<E> newVal = (List<E>)evt.getNewValue();
            List<E> oldVal = sourceList.getItems();

            if(!oldVal.equals(newVal))
            {
                setSourceListElements(newVal);
            }
        }
        else if(ProcessingBatchModelInterface.SOURCES_SELECTED.equals(name))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = necessaryInputProvided;
            if(newVal != oldVal)
            {
                previewAction.setEnabled(newVal);
                clearAction.setEnabled(newVal);
                firePropertyChange(ProcessingWizardModel.INPUT_PROVIDED, oldVal, newVal);
                necessaryInputProvided = newVal;
            }
        }
        else if(ProcessingBatchModelInterface.FILTERING_POSSIBLE.equals(name))
        {            
            boolean newVal = (boolean)evt.getNewValue();           
            buttonFilterSources.setEnabled(newVal);
        }
        else if(ProcessingModelInterface.CURRENT_BATCH_NUMBER.equals(name))
        {
            pullModelProperties();
        }		
    }

    private void initDesktop()
    {
        if(DESKTOP_AVAILABLE)
        {
            desktop = Desktop.getDesktop();
            openSupported = desktop.isSupported(Desktop.Action.OPEN);
        }
    }

    private void deleteSelectedItems()
    {
        List<E> sourcesSelected = sourceList.getSelectedValuesList();
        List<E> sourcesOld = sourceList.getItems();
        List<E> sourcesNew = new ArrayList<>(sourcesOld);
        sourcesNew.removeAll(sourcesSelected);

        model.setSources(sourcesNew);
    }

    private void createAndRegisterKeyListener()
    {
        sourceList.addKeyListener(new KeyListener() 
        {
            @Override
            public void keyPressed(KeyEvent e) 
            {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                {
                    deleteSelectedItems();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}

            @Override
            public void keyTyped(KeyEvent e) {}
        });
    }

    public void publishRawData(List<SampleCollection> rawData) 
    {
        if (!rawData.isEmpty()) 
        {
            Map<String, StandardNumericalTable> tables = new LinkedHashMap<>();
            for (SampleCollection collection : rawData) 
            {
                String collectionName = collection.getShortName();
                RawDataTableModel model = new RawDataTableModel(collection, false);
                StandardNumericalTable table = new OrderedNumericalTable(model,true);
                tables.put(collectionName, table);
            }

            MultipleNumericalTableView dialog = new MultipleNumericalTableView(SwingUtilities.getWindowAncestor(mainPanel), tables, "Raw data", true);
            dialog.setVisible(true);
        }
    }

    private void showRawData()
    {
        List<E> sourcesSelected = sourceList.getSelectedValuesList();
        List<SampleCollection> rawData = new ArrayList<>();

        for(E source : sourcesSelected)
        {
            List<SampleCollection> collections = source.getSampleCollections();

            for(SampleCollection collection : collections)
            {
                collection.setKeysIncluded(true);
                rawData.add(collection);
            }						
        }

        publishRawData(rawData);
    }

    private void openSelectedInExternalApplication()
    {
        List<E> sourcesSelected = sourceList.getSelectedValuesList();

        int failures = 0;
        for(E source: sourcesSelected)
        {
            try
            {
                desktop.open(source.getCorrespondingFile());
            }
            catch(IOException e)
            {
                failures++;
            }
        }

        if(failures>0)
        {
            JOptionPane.showMessageDialog(mainPanel, "Could not found the associated application for " + failures + " sources", "", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createAndRegisterPopupMenu() 
    {
        final JPopupMenu popup = new JPopupMenu();

        JMenuItem itemRawData = new JMenuItem(showRawDataAction);
        popup.add(itemRawData);

        JMenuItem itemPreview = new JMenuItem(previewSelectedAction);
        popup.add(itemPreview);

        if(openSupported)
        {
            JMenuItem itemOpen = new JMenuItem(openSelectedAction);
            popup.add(itemOpen);
        }

        JMenuItem itemDelete = new JMenuItem("Delete");
        itemDelete.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                deleteSelectedItems();
            }
        });

        popup.addSeparator();
        popup.add(itemDelete);

        //Add listener to the text area so the popup menu can come up.

        MouseListener listener = new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)  {check(e);}

            @Override
            public void mouseReleased(MouseEvent e) {check(e);}

            public void check(MouseEvent e) 
            {
                if (e.isPopupTrigger()) 
                {
                    if(sourceList.isNonEmpty())
                    {
                        int clicked = sourceList.locationToIndex(e.getPoint());
                        if(!sourceList.isSelectedIndex(clicked))
                        {
                            sourceList.setSelectedIndex(clicked); 
                        }		 
                        popup.show(sourceList, e.getX(), e.getY());
                    }		    	
                }
            }
        };

        sourceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sourceList.addMouseListener(listener);
    }

    private JPanel buildControlPanel()
    {
        JButton buttonAdd = new JButton(addAction);
        JButton buttonClear = new JButton(clearAction);
        JButton buttonPreview = new JButton(previewAction);   

        JPanel panelControl = new JPanel();	
        JLabel labelBatch = new JLabel(freeLabel);
        GroupLayout layout = new GroupLayout(panelControl);
        layout.setHonorsVisibility(true);
        panelControl.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup().addComponent(labelBatch).addComponent(labelBatchNumber))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonAdd).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonClear).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPreview).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonFilterSources)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup().addComponent(labelBatch).addComponent(labelBatchNumber))
                .addComponent(buttonAdd)
                .addComponent(buttonClear)
                .addComponent(buttonPreview)
                .addComponent(buttonFilterSources));

        layout.linkSize(buttonAdd, buttonClear, buttonPreview, buttonFilterSources);

        return panelControl;
    }

    private class PreviewAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;
        public PreviewAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(NAME, "Preview");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {	
            model.showPreview();
        }
    }

    private class PreviewSelectedAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;
        public PreviewSelectedAction()
        {
            putValue(NAME, "Preview");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            List<E> sourcesSelected = sourceList.getSelectedValuesList();
            model.showPreview(sourcesSelected);
        }
    }

    private class ClearAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ClearAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME,"Clear");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            model.setSources(new ArrayList<E>());
        }
    }

    private class AddAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public AddAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_A);
            putValue(NAME,"Add");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            final ConcurrentReadingTask<E> task = fileChooser.chooseSources(mainPanel);

            if(task == null)
            {
                return;
            }

            //we store the task as a instance variable, so that it can be cancelled when the user cancels the dialog containing source selection page
            currentReadingTask = task;

            task.addPropertyChangeListener("state", new PropertyChangeListener()
            {
                @Override
                public void propertyChange(PropertyChangeEvent evt) 
                {
                    if(SwingWorker.StateValue.DONE.equals(evt.getNewValue())) 
                    {
                        try 
                        {
                            boolean cancelled = task.isCancelled();

                            List<E> sources = null;
                            if(!cancelled)
                            {
                                sources = task.get();				   			
                            }


                            if(cancelled || sources == null)
                            {
                                JOptionPane.showMessageDialog(mainPanel, 
                                        "Reading terminated", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
                            }
                            else
                            {
                                int failureCount = task.getFailuresCount();
                                List<File> unreadImages = task.getIllegalImageFiles();
                                List<File> unreadSpectroscopyFiles = task.getIllegalSpectroscopyFiles();

                                if(failureCount > 0)
                                {
                                    String errorMessage = "Errors occured during reading of " + failureCount + " files";
                                    JOptionPane.showMessageDialog(mainPanel, errorMessage, AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
                                }

                                handleUnreadImages(unreadImages);
                                handleUnreadSpectroscopyFiles(unreadSpectroscopyFiles);

                                getModel().addSources(sources);
                            }
                        }
                        catch (InterruptedException | ExecutionException e) 
                        {
                            e.printStackTrace();
                        } 
                        finally
                        {
                            //we set currentReadingTask to null to avoid memory waste, as this object holds references to the read-in sources
                            currentReadingTask = null;
                        }
                    }					
                }		
            });
            task.execute();
        }
    }

    protected void handleUnreadImages(List<File> unreadImages)
    {

    }

    protected void handleUnreadSpectroscopyFiles(List<File> unreadImages)
    {

    }

    protected void filterSources()
    {

    }

    @Override
    public boolean isBackEnabled() 
    {
        return model.isBackEnabled();
    }

    @Override
    public boolean isNextEnabled() 
    {
        return model.isNextEnabled();
    }

    @Override
    public boolean isSkipEnabled() 
    {
        return model.isSkipEnabled();
    }

    @Override
    public boolean isFinishEnabled() 
    {
        return model.isFinishEnabled();
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
            showRawData();
        };
    }

    private class OpenSelectedExternallyAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public OpenSelectedExternallyAction()
        {
            putValue(NAME,"Open in default application");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            openSelectedInExternalApplication();
        };
    }

    private class FilterSourcesAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FilterSourcesAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_K);
            putValue(NAME,"Patchwork");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            filterSources();
        };
    }
}
