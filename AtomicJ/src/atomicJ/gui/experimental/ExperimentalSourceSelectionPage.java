
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

package atomicJ.gui.experimental;

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
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.Border;

import atomicJ.data.ChannelFilter;
import atomicJ.data.SampleCollection;
import atomicJ.gui.AbstractWizardPage;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.MultipleNumericalTableView;
import atomicJ.gui.OrderedNumericalTable;
import atomicJ.gui.RawDataTableModel;
import atomicJ.gui.ResourceSelectionModel;
import atomicJ.gui.SourceFileChooser;
import atomicJ.gui.StandardNumericalTable;
import atomicJ.gui.SubPanel;
import atomicJ.gui.WizardPage;
import atomicJ.gui.curveProcessing.ProcessingBatchModelInterface;
import atomicJ.gui.curveProcessing.ProcessingModelInterface;
import atomicJ.gui.curveProcessing.ProcessingWizardModel;
import atomicJ.readers.ConcurrentReadingTask;
import atomicJ.readers.SourceReadingModel;
import atomicJ.sources.Channel2DSource;


public class ExperimentalSourceSelectionPage extends AbstractWizardPage implements PropertyChangeListener, WizardPage
{
    private static final boolean DESKTOP_AVAILABLE = Desktop.isDesktopSupported()&&(!GraphicsEnvironment.isHeadless());

    private final Preferences pref = Preferences.userNodeForPackage(ExperimentalSourceSelectionPage.class).node(getClass().getName()).node("SourceSelection");

    private Desktop desktop;
    private boolean openSupported;

    private final Action showRawDataAction = new ShowRawDataAction();
    private final Action openAction = new OpenAction();
    private final Action previewAction = new PreviewAction();
    private final Action browseReadInAction = new BrowseReadInResourcesAction();
    private final Action browseFileSystemAction = new BrowseFileSystemAction();

    private final JButton buttonPreview = new JButton(previewAction);
    private final JButton buttonBrowseReadIn = new JButton(browseReadInAction);
    private final JButton buttonBrowseFileSystem = new JButton(browseFileSystemAction);

    private final JFormattedTextField fieldSource = new JFormattedTextField();
    private final JLabel labelBatchNumber = new JLabel();	

    private final JPanel panelControls;
    private final SourceFileChooser<? extends Channel2DSource<?>> chooser;
    private final ResourceStandAloneChooser resourceChooser;

    private ExperimentalSourceSelectionModel  model;

    private ConcurrentReadingTask<? extends Channel2DSource<?>> currentReadingTask;

    private boolean necessaryInputProvided;
    private final JPanel viewPanel = new JPanel();

    public ExperimentalSourceSelectionPage(ExperimentalSourceSelectionModel model, 
            SourceReadingModel<? extends Channel2DSource<?>> manager)
    {
        setModel(model);

        this.resourceChooser = new ResourceStandAloneChooser(model.getParent(), model.getChooserModel());
        this.chooser = new SourceFileChooser<>(manager, pref, false, JFileChooser.FILES_ONLY);      

        initDesktop();
        createAndRegisterPopupMenu();

        viewPanel.setLayout(new BorderLayout());
        viewPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        SubPanel outerPanel = new SubPanel();
        SubPanel innerPanel = new SubPanel();

        innerPanel.addComponent(new JLabel("Selected file"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(5,5,5,5));
        innerPanel.addComponent(fieldSource, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        outerPanel.addComponent(innerPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        Border border = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),BorderFactory.createEmptyBorder(12,12,12,12));
        outerPanel.setBorder(border);

        this.panelControls = buildControlPanel();		

        viewPanel.add(outerPanel,BorderLayout.CENTER);

        initInputAndActionMaps();
    }

    private void initInputAndActionMaps()
    {
        InputMap inputMap = viewPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);                
        inputMap.put((KeyStroke) showRawDataAction.getValue(Action.ACCELERATOR_KEY), showRawDataAction.getValue(Action.NAME));

        ActionMap actionMap = viewPanel.getActionMap();
        actionMap.put(showRawDataAction.getValue(Action.NAME), showRawDataAction);    
    }

    private void pullModelProperties()
    {
        List<Channel2DSource<?>> sources = model.getSources();
        Channel2DSource<?> source = sources.isEmpty() ? null : sources.get(0);
        String identifier = model.getIdentifier();
        boolean modelRestricted = model.isRestricted();
        necessaryInputProvided = model.areSourcesSelected();
        boolean readInResourcesAvailable = model.areReadInResourcesAvailable();

        setSource(source);
        labelBatchNumber.setText(identifier);

        previewAction.setEnabled(necessaryInputProvided);
        browseFileSystemAction.setEnabled(!modelRestricted);
        browseReadInAction.setEnabled(readInResourcesAvailable);        
    }

    public void setModel(ExperimentalSourceSelectionModel modelNew)
    {
        if(model != null)
        {
            model.removePropertyChangeListener(this);
        }

        this.model = modelNew;
        model.addPropertyChangeListener(this);

        pullModelProperties();
    }

    public void setTypeOfData(ChannelFilter dataTypeFilter)
    {
        chooser.setTypeOfData(dataTypeFilter);
    }

    public void setChooserCurrentDirectory(File dir)
    {
        chooser.setCurrentDirectory(dir);
    }

    public void setChooserSelectedFile(File file)
    {
        chooser.setSelectedFile(file);
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

    public ResourceSelectionModel<Channel2DSource<?>> getModel()
    {
        return model;
    }

    private void setSource(Channel2DSource<?> source)
    {
        fieldSource.setValue(source);
        fieldSource.revalidate();	
    }

    @Override
    public Component getView()
    {
        return viewPanel;
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
            List<Channel2DSource<?>> sourcesNew = ((List<Channel2DSource<?>>)evt.getNewValue());

            if(!sourcesNew.isEmpty())
            {
                Channel2DSource<?> newVal = sourcesNew.get(0);
                Channel2DSource<?> oldVal = (Channel2DSource<?>) fieldSource.getValue();
                if(!Objects.equals(newVal, oldVal))
                {
                    setSource(newVal);
                }
            }
        }
        else if(ProcessingBatchModelInterface.SOURCES_SELECTED.equals(name))
        {
            boolean newVal = (boolean)evt.getNewValue();
            boolean oldVal = necessaryInputProvided;
            if(newVal != oldVal)
            {
                buttonPreview.setEnabled(newVal);
                firePropertyChange(ProcessingWizardModel.INPUT_PROVIDED, oldVal, newVal);
                necessaryInputProvided = newVal;
            }
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
            MultipleNumericalTableView dialog = new MultipleNumericalTableView(SwingUtilities.getWindowAncestor(viewPanel), tables, "Raw data", true);
            dialog.setVisible(true);
        }
    }

    private void showRawData()
    {
        Channel2DSource<?> source = (Channel2DSource<?>)fieldSource.getValue();
        List<SampleCollection> rawData = new ArrayList<>();

        List<SampleCollection> collections = source.getSampleCollections();

        for(SampleCollection collection : collections)
        {
            collection.setKeysIncluded(true);
            rawData.add(collection);
        }						

        publishRawData(rawData);
    }

    private void browseReadInResources()
    {
        ResourceStandAloneChooserModel chooserModel = this.model.getChooserModel();

        boolean approved = resourceChooser.show(chooserModel);

        if(approved)
        {
            Channel2DSource<?> chosenSource = chooserModel.getChosenResource();

            List<Channel2DSource<?>> sources = new ArrayList<>();
            sources.add(chosenSource);

            this.model.setSources(sources); 
        }                       
    }

    private void open()
    {
        Channel2DSource<?> source = (Channel2DSource<?>)fieldSource.getValue();
        try
        {
            desktop.open(source.getCorrespondingFile());
        }
        catch(IOException e)
        {
            JOptionPane.showMessageDialog(viewPanel, "Could not found the associated application for  the source", "", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void browse()
    {
        final ConcurrentReadingTask<? extends Channel2DSource<?>> task = chooser.chooseSources(viewPanel);

        if(task == null)
        {
            return;
        }

        //we store the task as a instance variable, so that it can be cancelled when the user cancels the dialog containing source selection page
        currentReadingTask = task;
        task.getPropertyChangeSupport().addPropertyChangeListener("state", new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                if(SwingWorker.StateValue.DONE.equals(evt.getNewValue())) 
                {
                    try 
                    {
                        boolean cancelled = task.isCancelled();

                        List<Channel2DSource<?>> sources = null;
                        if(!cancelled)
                        {
                            sources = new ArrayList<>(task.get());
                        }

                        if(cancelled || sources == null)
                        {
                            JOptionPane.showMessageDialog(viewPanel, 
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
                                JOptionPane.showMessageDialog(viewPanel, errorMessage, AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
                            }

                            handleUnreadImages(unreadImages);
                            handleUnreadSpectroscopyFiles(unreadSpectroscopyFiles);

                            getModel().setSources(sources);                          
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

    private void createAndRegisterPopupMenu() 
    {
        final JPopupMenu popup = new JPopupMenu();

        JMenuItem itemRawData = new JMenuItem(showRawDataAction);
        popup.add(itemRawData);

        JMenuItem itemPreview = new JMenuItem("Preview");
        itemPreview.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                model.showPreview();
            }
        });
        popup.add(itemPreview);

        if(openSupported)
        {
            JMenuItem itemOpen = new JMenuItem(openAction);
            popup.add(itemOpen);
        }

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
                    if(fieldSource.getValue() != null)
                    {                      	 
                        popup.show(fieldSource, e.getX(), e.getY());
                    }		    	
                }
            }
        };
        fieldSource.addMouseListener(listener);
    }

    private JPanel buildControlPanel()
    {
        JPanel panelControl = new JPanel();	
        JLabel labelBrowse = new JLabel("Browse");
        GroupLayout layout = new GroupLayout(panelControl);
        panelControl.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(labelBrowse).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonBrowseFileSystem).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonBrowseReadIn).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonPreview).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(labelBrowse)
                .addComponent(buttonBrowseFileSystem)
                .addComponent(buttonBrowseReadIn)
                .addComponent(buttonPreview));

        layout.linkSize(buttonBrowseFileSystem, buttonBrowseReadIn, buttonPreview);

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



    private class BrowseReadInResourcesAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public BrowseReadInResourcesAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME,"Read-in resources");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            browseReadInResources();
        }
    }

    private class BrowseFileSystemAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public BrowseFileSystemAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME,"File system");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            browse();
        }
    }

    protected void handleUnreadImages(List<File> unreadImages)
    {}

    protected void handleUnreadSpectroscopyFiles(List<File> unreadImages)
    {}

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


    @Override
    public boolean isLast() 
    {
        return model.isLast();
    }

    @Override
    public boolean isFirst()
    {
        return model.isFirst();
    }


    private class ShowRawDataAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ShowRawDataAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W,InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Raw data");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            showRawData();
        };
    }

    private class OpenAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public OpenAction()
        {
            putValue(NAME,"Open");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            open();
        };
    }

    @Override
    public String getTaskName() {
        return model.getTaskName();
    }

    @Override
    public String getTaskDescription() {
        return model.getTaskDescription();
    }

    @Override
    public String getIdentifier() {
        return model.getIdentifier();
    }
}
