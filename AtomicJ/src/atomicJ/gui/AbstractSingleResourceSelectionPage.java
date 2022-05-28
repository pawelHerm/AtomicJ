
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
import java.util.Objects;
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



public abstract class AbstractSingleResourceSelectionPage <E extends ChannelSource> extends AbstractWizardPage implements PropertyChangeListener, WizardPage
{
    private static final boolean DESKTOP_AVAILABLE = Desktop.isDesktopSupported()&&(!GraphicsEnvironment.isHeadless());

    private final Preferences pref = Preferences.userNodeForPackage(AbstractSingleResourceSelectionPage.class).node(getClass().getName()).node("SourceSelection");

    private Desktop desktop;
    private boolean openSupported;

    private final ShowRawDataAction showRawDataAction = new ShowRawDataAction();
    private final OpenAction openAction = new OpenAction();

    private final JFormattedTextField fieldSource = new JFormattedTextField();
    private final JButton buttonBrowse = new JButton(new BrowseAction());
    private final JButton buttonPreview = new JButton(new PreviewAction()); 
    private final JLabel labelBatchNumber = new JLabel();   
    private final JPanel panelControls;
    private final SourceFileChooser<E> chooser;
    private final String freeLabel;
    private ResourceSelectionModel<E>  model;

    private ConcurrentReadingTask<E> currentReadingTask;

    private final JPanel mainPanel = new JPanel(new BorderLayout());

    private boolean necessaryInputProvided;

    public AbstractSingleResourceSelectionPage(ResourceSelectionModel<E> model, SourceReadingModel<E> manager, String freeLabel)
    {
        setModel(model);
        this.freeLabel = freeLabel;

        this.chooser = new SourceFileChooser<>(manager, pref, false, JFileChooser.FILES_ONLY);      

        initDesktop();
        createAndRegisterPopupMenu();

        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        SubPanel outerPanel = new SubPanel();
        SubPanel innerPanel = new SubPanel();

        innerPanel.addComponent(new JLabel("Selected file"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(5,5,5,5));
        innerPanel.addComponent(fieldSource, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        outerPanel.addComponent(innerPanel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        Border border = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),BorderFactory.createEmptyBorder(12,12,12,12));
        outerPanel.setBorder(border);

        this.panelControls = buildControlPanel();       

        mainPanel.add(outerPanel,BorderLayout.CENTER);

        initInputAndActionMaps();
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
        E source = sources.isEmpty() ? null : sources.get(0);
        String identifier = model.getIdentifier();
        boolean modelRestricted = model.isRestricted();
        necessaryInputProvided = model.areSourcesSelected();

        setSource(source);
        labelBatchNumber.setText(identifier);

        buttonPreview.setEnabled(necessaryInputProvided);
        buttonBrowse.setEnabled(!modelRestricted);
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

    public ResourceSelectionModel<E> getModel()
    {
        return model;
    }

    private void setSource(E source)
    {
        fieldSource.setValue(source);
        fieldSource.revalidate();   
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
        String property = evt.getPropertyName();

        if(property.equals(ProcessingBatchModelInterface.SOURCES))
        {
            @SuppressWarnings("unchecked")
            List<E> sourcesNew = ((List<E>)evt.getNewValue());

            if(!sourcesNew.isEmpty())
            {
                E newVal = sourcesNew.get(0);
                E oldVal = (E) fieldSource.getValue();
                if(!Objects.equals(newVal, oldVal))
                {
                    setSource(newVal);
                }
            }
        }
        else if(property.equals(ProcessingBatchModelInterface.SOURCES_SELECTED))
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
        else if(property.equals(ProcessingModelInterface.CURRENT_BATCH_NUMBER))
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
            MultipleNumericalTableView dialog = new MultipleNumericalTableView(SwingUtilities.getWindowAncestor(mainPanel), tables, "Raw data", true);
            dialog.setVisible(true);
        }
    }

    private void showRawData()
    {
        E source = (E)fieldSource.getValue();
        List<SampleCollection> rawData = new ArrayList<>();

        List<SampleCollection> collections = source.getSampleCollections();

        for(SampleCollection collection : collections)
        {
            collection.setKeysIncluded(true);
            rawData.add(collection);
        }                       

        publishRawData(rawData);
    }

    private void open()
    {
        E source = (E)fieldSource.getValue();
        try
        {
            desktop.open(source.getCorrespondingFile());
        }
        catch(IOException e)
        {
            JOptionPane.showMessageDialog(mainPanel, "Could not found the associated application for  the source", "", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void browse()
    {
        final ConcurrentReadingTask<E> task = chooser.chooseSources(mainPanel);

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
        JLabel labelBatch = new JLabel(freeLabel);
        GroupLayout layout = new GroupLayout(panelControl);
        panelControl.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup().addComponent(labelBatch).addComponent(labelBatchNumber))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonBrowse).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPreview).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup().addComponent(labelBatch).addComponent(labelBatchNumber))
                .addComponent(buttonBrowse)

                .addComponent(buttonPreview));

        layout.linkSize(buttonBrowse, buttonPreview);

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

    private class BrowseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public BrowseAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME,"Browse");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            browse();
        }
    }

    protected void handleUnreadImages(List<File> unreadImages)
    {

    }

    protected void handleUnreadSpectroscopyFiles(List<File> unreadImages)
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
}
