
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

package atomicJ.gui.save;


import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import atomicJ.gui.Channel2DChart;
import atomicJ.gui.NameComponent;
import atomicJ.gui.RootedFileChooser;
import atomicJ.gui.SubPanel;
import atomicJ.gui.save.FileNamingCombo.FileNamingComboType;
import atomicJ.gui.stack.StackModel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


import static atomicJ.gui.save.SaveModelProperties.*;

public class MultipleFrameSaveDialog extends JDialog implements ItemListener, ChangeListener, PropertyChangeListener 
{
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_DESTINATION = "Default";
    private static final String LAST_USED_CHART_FORMAT = "LastUsedChartFormat";
    private static final String LAST_USED_ARCHIVE_TYPE = "LastUsedChartArchiveType";

    private final Preferences pref  = Preferences.userRoot().node(getClass().getName());

    private final List<String> keys = new ArrayList<>();

    private final MultipleFramesSaveModel saveModel;
    private final SaveableChartSource<Channel2DChart<?>> saveableChartSource;

    private final SaveAction saveAction = new SaveAction();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    private final JTextField fieldDestination = new JTextField();
    private final JFormattedTextField fieldArchive = new JFormattedTextField(new DefaultFormatter());
    private final JLabel labelArchive = new JLabel("Archive name");
    private final JCheckBox boxArchive = new JCheckBox("Archive");
    private final JComboBox<ArchiveType> comboArchiveTypes = new JComboBox<>(ArchiveType.values());

    private final JSpinner spinnerFirstFrame = new JSpinner();
    private final JSpinner spinnerLastFrame = new JSpinner();

    private ChartSaveFormatType[] formatTypes;
    private final JComboBox<ChartSaveFormatType> comboFormats = new JComboBox<>();

    private final Map<String, FileNamingCombo> combosPrefix = new Hashtable<>();
    private final Map<String, FileNamingCombo> combosRoot = new Hashtable<>();
    private final Map<String, FileNamingCombo> combosSuffix = new Hashtable<>();;

    private final JSpinner spinnerSerial = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
    private final JCheckBox boxExtensions = new JCheckBox("Append extensions");
    private final JFileChooser chooser;

    private final SubPanel panelNaming = new SubPanel();

    public MultipleFrameSaveDialog(Window parent, SaveableChartSource<Channel2DChart<?>> chartSource, StackModel<?> stackModel, Preferences pref) 
    {
        super(parent, "Save all frames", ModalityType.APPLICATION_MODAL);
        this.saveableChartSource = chartSource;

        this.chooser = new RootedFileChooser(pref);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        this.saveModel = new MultipleFramesSaveModel(stackModel.getFrameCount());
        saveModel.addPropertyChangeListener(this);

        pullModelProperties();
        initItemListener();
        initChangeListener();
        initFieldsListener();

        setLayout(new BorderLayout());

        JTabbedPane tabPane = new JTabbedPane();

        JPanel generalPanel = buildGeneralPanel();
        JPanel formatPanel = buildFormatPanel();
        JPanel fileNamingPanel = buildFileNamingPanel();

        tabPane.add(generalPanel, "General");
        tabPane.add(formatPanel, "Format");
        tabPane.add(fileNamingPanel, "File naming");

        JPanel buttonPanel = buildButtonPanel();

        add(tabPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    public void setConsistentWithStackModel(StackModel<?> model)
    {
        int frameCount = model.getFrameCount();
        saveModel.setFrameCount(frameCount);
    }

    public void setKey(String key)
    {
        saveModel.addKey(key);

        FileNamingCombo comboPrefix = new FileNamingCombo(new Object[] {NameComponent.PREFIX, NameComponent.SERIAL_NUMBER,NameComponent.NAME }, FileNamingComboType.PREFIX, key);
        FileNamingCombo comboRoot = new FileNamingCombo(new Object[] {NameComponent.ROOT, NameComponent.NAME,NameComponent.SERIAL_NUMBER }, FileNamingComboType.ROOT,key);
        FileNamingCombo comboSuffix = new FileNamingCombo(new Object[] {NameComponent.SUFFIX, "_" + key,NameComponent.SERIAL_NUMBER }, FileNamingComboType.SUFFIX,key);

        combosPrefix.put(key, comboPrefix);
        combosRoot.put(key, comboRoot);
        combosSuffix.put(key, comboSuffix);

        comboPrefix.addItemListener(this);
        comboRoot.addItemListener(this);
        comboSuffix.addItemListener(this);

        Object prefix = saveModel.getPrefix(key);
        Object root = saveModel.getRoot(key);
        Object suffix = saveModel.getSuffix(key);

        comboPrefix.setSelectedItem(prefix);
        comboRoot.setSelectedItem(root);
        comboSuffix.setSelectedItem(suffix);

        int n = keys.size();

        panelNaming.addComponent(new JLabel(key), 0, n, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        panelNaming.addComponent(comboPrefix, 1, n, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,0, 1);
        panelNaming.addComponent(comboRoot, 2, n, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,0, 1);
        panelNaming.addComponent(comboSuffix, 3, n, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,0, 1);

        keys.add(key);
    }

    @Override
    public void setVisible(boolean visible)
    {
        if(visible)
        {
            Rectangle2D chartArea = saveableChartSource.getChartArea();
            Rectangle2D dataArea = saveableChartSource.getDataArea();

            for(ChartSaveFormatType type: formatTypes)
            {
                type.specifyInitialDimensions(chartArea, dataArea.getWidth(), dataArea.getHeight());
            }

            String format = getLastUsedFormat();
            if(format != null)
            {
                saveModel.setSaveFormat(format);
            }
        }

        super.setVisible(visible);
    }

    private void pullModelProperties() 
    {
        File directory = saveModel.getDirectory();
        boolean saveInArchive = saveModel.getSaveInArchive();
        String archiveName = saveModel.getArchiveName();
        boolean extensionsAppended = saveModel.areExtensionsAppended();
        ChartSaveFormatType saveFormat = saveModel.getSaveFormat();
        Integer initSerial = saveModel.getInitialSerialNumber();
        ChartSaveFormatType[] formatTypes = saveModel.getFormatTypes();
        ArchiveType archiveType = saveModel.getArchiveType();

        if (directory == null) 
        {
            fieldDestination.setText(DEFAULT_DESTINATION);
        } 
        else 
        {
            fieldDestination.setText(directory.getPath());
        }
        this.formatTypes = formatTypes;
        comboFormats.removeAllItems();

        boxArchive.setSelected(saveInArchive);
        comboArchiveTypes.setSelectedItem(archiveType);
        comboArchiveTypes.setEnabled(saveInArchive);
        fieldArchive.setEnabled(saveInArchive);
        fieldArchive.setValue(archiveName);
        boxExtensions.setSelected(extensionsAppended);
        spinnerSerial.setValue(initSerial);

        cardPanel.removeAll();

        for(ChartSaveFormatType formatType: formatTypes)
        {
            JPanel inputPanel = formatType.getParametersInputPanel();
            cardPanel.add(inputPanel, formatType.toString());
            comboFormats.addItem(formatType);
        }

        comboFormats.setSelectedItem(saveFormat);

        boolean inputSpecified = saveModel.isNecessaryInputProvided();
        saveAction.setEnabled(inputSpecified);

        int frameCount = saveModel.getFrameCount();
        int firstFrame = saveModel.getFirstFrame();
        int lastFrame = saveModel.getLastFrame();

        SpinnerNumberModel spinnerFirstFrameModel = new SpinnerNumberModel(firstFrame, 1, lastFrame, 1);
        SpinnerNumberModel spinnerLastFrameModel = new SpinnerNumberModel(lastFrame, firstFrame, frameCount, 1);

        spinnerFirstFrame.setModel(spinnerFirstFrameModel);
        spinnerLastFrame.setModel(spinnerLastFrameModel);
    }

    private void initItemListener() 
    {
        boxArchive.addItemListener(this);
        boxExtensions.addItemListener(this);
        comboFormats.addItemListener(this);
        comboArchiveTypes.addItemListener(this);
    }

    private void initChangeListener() 
    {
        spinnerSerial.addChangeListener(this);
        spinnerFirstFrame.addChangeListener(this);
        spinnerLastFrame.addChangeListener(this);
    }

    private void initFieldsListener() 
    {
        final PropertyChangeListener fieldsListener = new PropertyChangeListener() 
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                Object source = evt.getSource();
                if (source == fieldArchive) 
                {
                    String newVal = evt.getNewValue().toString();
                    saveModel.setArchiveName(newVal);
                }

            }
        };

        fieldArchive.addPropertyChangeListener("value", fieldsListener);
    }

    @Override
    public void itemStateChanged(ItemEvent event) 
    {
        Object source = event.getSource();

        if (source == boxArchive) 
        {
            boolean saveInArchiveNew = boxArchive.isSelected();
            saveModel.setSaveInArchive(saveInArchiveNew);
            fieldArchive.setEnabled(saveInArchiveNew);
            labelArchive.setEnabled(saveInArchiveNew);
            comboArchiveTypes.setEnabled(saveInArchiveNew);
        } 
        else if (source == boxExtensions) 
        {
            boolean appendExtensionsNew = boxExtensions.isSelected();
            saveModel.setAppendExtensions(appendExtensionsNew);
        } 
        else if (source == comboFormats)
        {
            ChartSaveFormatType type = (ChartSaveFormatType) comboFormats.getSelectedItem();
            cardLayout.show(cardPanel, type.toString());
            saveModel.setSaveFormat(type);
            updateLastUsedFormat();
        }
        else if(source == comboArchiveTypes)
        {
            ArchiveType type = (ArchiveType) comboArchiveTypes.getSelectedItem();
            saveModel.setArchiveType(type);
            updateLastUsedArchiveType();
        }
        else if (source instanceof FileNamingCombo) 
        {
            FileNamingCombo combo = (FileNamingCombo) source;
            FileNamingComboType type = combo.getType();
            String key = combo.getKey();

            if (type.equals(FileNamingComboType.PREFIX)) 
            {
                Object prefixNew = combo.getSelectedItem();
                saveModel.setPrefix(prefixNew, key);
            } 
            else if (type.equals(FileNamingComboType.ROOT)) 
            {
                Object rootNew = combo.getSelectedItem();
                saveModel.setRoot(rootNew, key);
            } 
            else if (type.equals(FileNamingComboType.SUFFIX)) 
            {
                Object suffixNew = combo.getSelectedItem();
                saveModel.setSuffix(suffixNew, key);
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object source = evt.getSource();
        if (source == spinnerSerial) 
        {
            int initSerialNew = ((SpinnerNumberModel) spinnerSerial.getModel()).getNumber().intValue();
            saveModel.setInitialSerialNumber(initSerialNew);
        }		
        else if(source == spinnerFirstFrame)
        {
            int firstFrame = ((SpinnerNumberModel)spinnerFirstFrame.getModel()).getNumber().intValue();
            ((SpinnerNumberModel)spinnerLastFrame.getModel()).setMinimum(firstFrame);	
            saveModel.setFirstFrame(firstFrame);
        }
        else if(source == spinnerLastFrame)
        {
            int lastFrame = ((SpinnerNumberModel)spinnerLastFrame.getModel()).getNumber().intValue();
            ((SpinnerNumberModel)spinnerFirstFrame.getModel()).setMaximum(lastFrame);
            saveModel.setLastFrame(lastFrame);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        Object source = evt.getSource();
        String property = evt.getPropertyName();

        if (DIRECTORY.equals(property)) 
        {
            String newVal = (String) evt.getNewValue();

            if (newVal == null) 
            {
                fieldDestination.setText(DEFAULT_DESTINATION);
            } 
            else 
            {
                fieldDestination.setText(newVal);
            }
        } 
        else if (ARCHIVE_NAME.equals(property)) 
        {
            String newVal = (String) evt.getNewValue();
            String oldVal = (String) fieldArchive.getValue();
            if (!(newVal.equals(oldVal))) 
            {
                fieldArchive.setValue(newVal);
            }
        } 
        else if (SAVE_IN_ARCHIVE.equals(property)) 
        {
            boolean newVal = (Boolean) evt.getNewValue();
            boolean oldVal = boxArchive.isSelected();

            if (oldVal != newVal) 
            {
                boxArchive.setSelected(newVal);
                comboArchiveTypes.setEnabled(newVal);
            }
        } 
        else if (ARCHIVE_TYPE.equals(property)) 
        {
            ArchiveType newVal = (ArchiveType) evt.getNewValue();
            ArchiveType oldVal = (ArchiveType) comboArchiveTypes.getSelectedItem();

            if (!oldVal.equals(newVal)) 
            {
                comboArchiveTypes.setSelectedItem(newVal);
                updateLastUsedArchiveType();
            }
        } 
        else if (INITIAL_SERIAL.equals(property)) 
        {
            Integer newVal = ((Number) evt.getNewValue()).intValue();
            Integer oldVal = ((Number) spinnerSerial.getValue()).intValue();
            if (!(newVal.equals(oldVal))) 
            {
                spinnerSerial.setValue(newVal);
            }
        } 
        else if (SAVE_FORMAT.equals(property)) 
        {
            ChartSaveFormatType newVal = (ChartSaveFormatType) evt.getNewValue();
            ChartSaveFormatType oldVal = (ChartSaveFormatType) comboFormats.getSelectedItem();

            if (!oldVal.equals(newVal)) 
            {
                comboFormats.setSelectedItem(newVal);
                updateLastUsedFormat();
            }
        } 
        else if (EXTENSIONS_APPENDED.equals(property)) 
        {
            boolean newVal = (Boolean) evt.getNewValue();
            boolean oldVal = boxExtensions.isSelected();
            if (oldVal != newVal) {
                boxExtensions.setSelected(newVal);
            }
        } 
        else if (INPUT_PROVIDED.equals(property)) 
        {
            boolean newVal = (Boolean) evt.getNewValue();
            saveAction.setEnabled(newVal);
        } 
        else if(FIRST_FRAME.equals(property))
        {
            int newVal = ((Number)evt.getNewValue()).intValue();

            spinnerFirstFrame.setValue(newVal);
            ((SpinnerNumberModel)spinnerLastFrame.getModel()).setMinimum(newVal);
        }
        else if(LAST_FRAME.equals(property))
        {
            int newVal = ((Number)evt.getNewValue()).intValue();

            spinnerLastFrame.setValue(newVal);
            ((SpinnerNumberModel)spinnerFirstFrame.getModel()).setMaximum(newVal);
        }
        else if (source instanceof ChannelSpecificSaveSettingsModel) 
        {
            ChannelSpecificSaveSettingsModel seriesModel = (ChannelSpecificSaveSettingsModel) source;
            String key = seriesModel.getKey();

            if (property.equals(PREFIX)) 
            {
                FileNamingCombo combo = combosPrefix.get(key);
                Object newVal = evt.getNewValue();
                Object oldVal = combo.getSelectedItem();
                if (!newVal.equals(oldVal)) 
                {
                    combo.setSelectedItem(newVal);
                }
            } 
            else if (property.equals(ROOT)) 
            {
                FileNamingCombo combo = combosRoot.get(key);

                Object newVal = evt.getNewValue();
                Object oldVal = combo.getSelectedItem();
                if (!newVal.equals(oldVal)) 
                {
                    combo.setSelectedItem(newVal);
                }
            } 
            else if (property.equals(SUFFIX)) 
            {
                FileNamingCombo combo = combosSuffix.get(key);

                Object newVal = evt.getNewValue();
                Object oldVal = combo.getSelectedItem();
                if (!newVal.equals(oldVal)) 
                {
                    combo.setSelectedItem(newVal);
                }
            }
        }
    }

    private SubPanel buildGeneralPanel() 
    {
        DefaultFormatter formatter = (DefaultFormatter) fieldArchive.getFormatter();
        formatter.setOverwriteMode(false);
        formatter.setCommitsOnValidEdit(true);

        SubPanel generalPanel = new SubPanel();
        SubPanel destPanel = new SubPanel();

        JLabel labelDestDirectory = new JLabel("Destination directory");
        JButton buttonSelect = new JButton(new SelectDestinationAction());
        JButton buttonDefault = new JButton(new DefaultDirectoryAction());

        JLabel labelFrameRange = new JLabel("Frame range"); 
        JLabel labelFirstFrame = new JLabel("From");
        JLabel labelLastFrame = new JLabel("to");

        fieldDestination.setEnabled(false);

        destPanel.addComponent(labelDestDirectory, 0, 0, 2, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(4, 3, 6, 3));
        destPanel.addComponent(buttonSelect, 0, 1, 1, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(5, 3, 5, 3));
        destPanel.addComponent(fieldDestination, 1, 1, 4, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1,new Insets(5, 3, 5, 3));
        destPanel.addComponent(buttonDefault, 5, 1, 1, 1,GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1,new Insets(5, 3, 5, 3));

        destPanel.addComponent(labelFrameRange, 0, 2, 1, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(10, 3, 5, 3));
        destPanel.addComponent(labelFirstFrame, 1, 2, 1, 1,GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1, new Insets(10, 3, 5, 3));		
        destPanel.addComponent(spinnerFirstFrame, 2, 2, 1, 1,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(10, 3, 5, 3));		
        destPanel.addComponent(labelLastFrame, 3, 2, 1, 1,GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1, new Insets(10, 3, 5, 3));		
        destPanel.addComponent(spinnerLastFrame, 4, 2, 1, 1,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(10, 3, 5, 3));		

        destPanel.addComponent(boxArchive, 0, 3, 2, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(10, 3, 5, 3));
        destPanel.addComponent(comboArchiveTypes, 1, 3, 4, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,1, 1, new Insets(5, 3, 5, 3));
        destPanel.addComponent(labelArchive, 0, 4, 1, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(4, 3, 5, 3));
        destPanel.addComponent(fieldArchive, 1, 4, 4, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,1, 1, new Insets(4, 5, 5, 3));

        destPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        generalPanel.addComponent(destPanel, 0, 0, 1, 1,GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);

        return generalPanel;
    }

    private JPanel buildFrameRangePanel()
    {
        JPanel frameRangePanel = new JPanel();

        GroupLayout layout = new GroupLayout(frameRangePanel);
        frameRangePanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(false);

        JLabel labelFirstFrame = new JLabel("From");
        JLabel labelLastFrame = new JLabel("to");

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(labelFirstFrame).
                addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(spinnerFirstFrame)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(labelLastFrame)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(spinnerLastFrame)
                );

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(labelFirstFrame)
                .addComponent(spinnerFirstFrame)
                .addComponent(labelLastFrame)
                .addComponent(spinnerLastFrame));

        layout.linkSize(labelFirstFrame,  labelLastFrame);
        layout.linkSize(spinnerFirstFrame,  spinnerLastFrame);

        return frameRangePanel;
    }

    private JPanel buildFormatPanel() 
    {
        JPanel formatPanel = new JPanel(new BorderLayout());
        SubPanel southPanel = new SubPanel();
        southPanel.addComponent(new JLabel("Format"), 0, 0, 1, 1, GridBagConstraints.EAST,GridBagConstraints.NONE, 0.25, 1);
        southPanel.addComponent(comboFormats, 1, 0, 1, 1, GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL, 1, 1);
        southPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        cardPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

        formatPanel.add(cardPanel,BorderLayout.NORTH);
        formatPanel.add(southPanel,BorderLayout.SOUTH);

        return formatPanel;
    }

    private SubPanel buildFileNamingPanel() 
    {
        SubPanel fileNamingPanel = new SubPanel();

        fileNamingPanel.setLayout(new GridBagLayout());
        JLabel labelSerial = new JLabel("Initial serial # ");
        JPanel panelSerial = new JPanel();
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinnerSerial.getEditor();
        JFormattedTextField ftf = editor.getTextField();
        ftf.setColumns(3);

        panelSerial.add(labelSerial);
        panelSerial.add(spinnerSerial);
        panelSerial.add(boxExtensions);

        fileNamingPanel.addComponent(panelNaming, 0, 0, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 1);
        fileNamingPanel.addComponent(panelSerial, 0, 1, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 1);

        return fileNamingPanel;
    }

    private JPanel buildButtonPanel() 
    {
        JPanel buttonsPanel = new JPanel();

        JButton buttonOK = new JButton(saveAction);
        JButton buttonCancel = new JButton(new CancelAction());

        JPanel innerButtons = new JPanel(new GridLayout(1, 0, 10, 10));

        innerButtons.add(buttonOK);
        innerButtons.add(buttonCancel);
        innerButtons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        buttonsPanel.add(innerButtons);
        buttonsPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        return buttonsPanel;
    }

    private void updateLastUsedFormat()
    {
        ChartSaveFormatType type = (ChartSaveFormatType)comboFormats.getSelectedItem();

        if(type != null)
        {
            String typeDescription = type.getDescription();

            pref.put(LAST_USED_CHART_FORMAT, typeDescription);
            try 
            {
                pref.flush();
            } catch (BackingStoreException e)
            {
                e.printStackTrace();
            }
        }
    }

    private String getLastUsedFormat()
    {
        String typeDescription = pref.get(LAST_USED_CHART_FORMAT, null);
        return typeDescription;
    }   

    private void updateLastUsedArchiveType()
    {
        ArchiveType type = (ArchiveType)comboArchiveTypes.getSelectedItem();

        if(type != null)
        {
            String typeDescription = type.toString();

            pref.put(LAST_USED_ARCHIVE_TYPE, typeDescription);
            try 
            {
                pref.flush();
            } catch (BackingStoreException e)
            {
                e.printStackTrace();
            }
        }
    }

    private class SaveAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public SaveAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(NAME, "OK");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            Map<String, Channel2DChart<?>> allCharts = new Hashtable<>();
            Map<String, List<String>> allDefaultNames = new Hashtable<>();
            Map<String, List<File>> allDefaultPaths = new Hashtable<>();

            for (String key : keys) 
            {
                if (saveModel.getSaveSeries(key)) 
                {

                    List<String> defaultNames = saveableChartSource.getDefaultOutputNames(key);
                    List<File> defaultPaths = saveableChartSource.getDefaultOutputLocations(key);
                    List<Channel2DChart<?>> charts = saveableChartSource.getAllNonemptyCharts(key);

                    allDefaultNames.put(key, defaultNames);
                    allDefaultPaths.put(key, defaultPaths);
                    allCharts.put(key, charts.get(0));
                }
            }

            saveModel.save(getParent(), allCharts, allDefaultNames, allDefaultPaths);

            MultipleFrameSaveDialog.this.setVisible(false);
        }
    }

    private class SelectDestinationAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public SelectDestinationAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME, "Select");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            int op = chooser.showOpenDialog(MultipleFrameSaveDialog.this);
            if (op == JFileChooser.APPROVE_OPTION) 
            {
                File directoryNew = chooser.getSelectedFile();
                String path = directoryNew.getPath();
                fieldDestination.setText(path);
                saveModel.setDirectory(directoryNew);
            }
        }
    }

    private class DefaultDirectoryAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public DefaultDirectoryAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_D);
            putValue(NAME, "Default");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            fieldDestination.setText(DEFAULT_DESTINATION);
            saveModel.setDirectory(null);
        }
    }

    private class CancelAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public CancelAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME, "Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            MultipleFrameSaveDialog.this.setVisible(false);
        }
    }
}
