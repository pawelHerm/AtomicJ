
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

import org.jfree.chart.JFreeChart;

import atomicJ.gui.NameComponent;
import atomicJ.gui.RootedFileChooser;
import atomicJ.gui.SubPanel;
import atomicJ.gui.save.FileNamingCombo.FileNamingComboType;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


import static atomicJ.gui.save.SaveModelProperties.*;

public class BatchSaveDialog extends JDialog implements ItemListener, ChangeListener, PropertyChangeListener 
{
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_DESTINATION = "Default";
    private static final String LAST_USED_CHART_FORMAT = "LastUsedChartFormat";
    private static final String LAST_USED_ARCHIVE_TYPE = "LastUsedChartArchiveType";

    private final Preferences pref  = Preferences.userRoot().node(getClass().getName());

    private final List<String> keys = new ArrayList<>();

    private final BatchSaveModel model = new BatchSaveModel();
    private SaveableChartSource<? extends JFreeChart> chartSource;

    private final SaveAction saveAction = new SaveAction();

    private final CardLayout formatCardLayout = new CardLayout();
    private final JPanel formatCardPanel = new JPanel(formatCardLayout);

    private final JTextField fieldDestination = new JTextField();
    private final Map<String, JCheckBox> boxesSaveSeries = new LinkedHashMap<>();
    private final JFormattedTextField fieldArchive = new JFormattedTextField(new DefaultFormatter());
    private final JLabel labelArchive = new JLabel("Archive name");
    private final JCheckBox boxArchive = new JCheckBox("Archive");
    private final JComboBox<ArchiveType> comboArchiveTypes = new JComboBox<>(ArchiveType.values());

    private ChartSaveFormatType[] formatTypes;
    private final JComboBox<ChartSaveFormatType> comboFormats = new JComboBox<>();

    private final Map<String, FileNamingCombo> combosPrefix = new Hashtable<>();
    private final Map<String, FileNamingCombo> combosRoot = new Hashtable<>();
    private final Map<String, FileNamingCombo> combosSuffix = new Hashtable<>();

    private final JSpinner spinnerSerial = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
    private final JCheckBox boxExtensions = new JCheckBox("Append extensions");
    private final JFileChooser chooser;

    private final SubPanel panelNaming = new SubPanel();
    private final JPanel imagesPanel = new JPanel(new GridLayout(0, 3));

    public BatchSaveDialog(Window parent, Preferences pref) 
    {
        super(parent, "Batch save", ModalityType.APPLICATION_MODAL);

        this.chooser = new RootedFileChooser(pref);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        model.addPropertyChangeListener(this);

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

    public void addKey(String key)
    {
        model.addKey(key);

        FileNamingCombo comboPrefix = new FileNamingCombo(new Object[] {NameComponent.PREFIX, NameComponent.SERIAL_NUMBER,NameComponent.NAME }, FileNamingComboType.PREFIX, key);
        FileNamingCombo comboRoot = new FileNamingCombo(new Object[] {NameComponent.ROOT, NameComponent.NAME,NameComponent.SERIAL_NUMBER }, FileNamingComboType.ROOT,key);
        FileNamingCombo comboSuffix = new FileNamingCombo(new Object[] {NameComponent.SUFFIX, "_" + key,NameComponent.SERIAL_NUMBER }, FileNamingComboType.SUFFIX,key);

        combosPrefix.put(key, comboPrefix);
        combosRoot.put(key, comboRoot);
        combosSuffix.put(key, comboSuffix);

        JCheckBox checkBox = new JCheckBox(key);

        boxesSaveSeries.put(key, checkBox);

        comboPrefix.addItemListener(this);
        comboRoot.addItemListener(this);
        comboSuffix.addItemListener(this);
        checkBox.addItemListener(this);

        Object prefix = model.getPrefix(key);
        Object root = model.getRoot(key);
        Object suffix = model.getSuffix(key);

        comboPrefix.setSelectedItem(prefix);
        comboRoot.setSelectedItem(root);
        comboSuffix.setSelectedItem(suffix);

        boolean saveSeries = model.isSeriesToBeSaved(key);

        checkBox.setSelected(saveSeries);

        int n = keys.size();

        panelNaming.addComponent(new JLabel(key), 0, n, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        panelNaming.addComponent(comboPrefix, 1, n, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,0, 1);
        panelNaming.addComponent(comboRoot, 2, n, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,0, 1);
        panelNaming.addComponent(comboSuffix, 3, n, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,0, 1);

        imagesPanel.add(checkBox);

        keys.add(key);
    }

    public void showDialog(SaveableChartSource<? extends JFreeChart> chartSource)
    {
        this.chartSource = chartSource;       

        Rectangle2D chartArea = chartSource.getChartArea();
        Rectangle2D dataArea = chartSource.getDataArea();

        for(ChartSaveFormatType type: formatTypes)
        {
            type.specifyInitialDimensions(chartArea, dataArea.getWidth(), dataArea.getHeight());
        }

        String format = getLastUsedFormat();
        if(format != null)
        {
            model.setSaveFormat(format);
        }

        super.setVisible(true); 
    }

    @Override
    public void setVisible(boolean visible)
    {
        if(visible)
        {
            if(chartSource == null)
            {
                throw new IllegalStateException("Chart sources is not known. Use showDialog(SaveableChartSource chartSource)");
            }
        }

        super.setVisible(visible);
    }

    private void pullModelProperties() 
    {
        File directory = model.getDirectory();
        boolean saveInArchive = model.getSaveInArchive();
        ArchiveType archiveType = model.getArchiveType();
        String archiveName = model.getArchiveName();
        boolean extensionsAppended = model.areExtensionsAppended();
        boolean multiplePagesSupported = model.multiplePagesSupported();
        ChartSaveFormatType saveFormat = model.getSaveFormat();
        Integer initSerial = model.getInitialSerialNumber();
        ChartSaveFormatType[] formatTypes = model.getFormatTypes();

        String destination = (directory == null) ? DEFAULT_DESTINATION : directory.getPath();
        fieldDestination.setText(destination);

        this.formatTypes = formatTypes;
        comboFormats.removeAllItems();

        boxArchive.setSelected(saveInArchive);
        comboArchiveTypes.setSelectedItem(archiveType);
        comboArchiveTypes.setEnabled(saveInArchive);
        fieldArchive.setEnabled(saveInArchive);
        fieldArchive.setValue(archiveName);
        boxExtensions.setSelected(extensionsAppended);
        spinnerSerial.setValue(initSerial);
        spinnerSerial.setEnabled(!multiplePagesSupported);

        formatCardPanel.removeAll();

        for(ChartSaveFormatType formatType: formatTypes)
        {
            JPanel inputPanel = formatType.getParametersInputPanel();
            formatCardPanel.add(inputPanel, formatType.toString());
            comboFormats.addItem(formatType);
        }


        comboFormats.setSelectedItem(saveFormat);

        boolean inputSpecified = model.isNecessaryInputProvided();
        saveAction.setEnabled(inputSpecified);
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

    private String getLastUsedFormat()
    {
        String typeDescription = pref.get(LAST_USED_CHART_FORMAT, null);
        return typeDescription;
    }   

    private String getLastUsedArchiveType()
    {
        String typeDescription = pref.get(LAST_USED_ARCHIVE_TYPE, null);
        return typeDescription;
    }   

    private void initItemListener() 
    {
        this.boxArchive.addItemListener(this);
        this.boxExtensions.addItemListener(this);
        this.comboFormats.addItemListener(this);
        this.comboArchiveTypes.addItemListener(this);
    }

    private void initChangeListener() 
    {
        spinnerSerial.addChangeListener(this);
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
                    model.setArchiveName(newVal);
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
            model.setSaveInArchive(saveInArchiveNew);
            fieldArchive.setEnabled(saveInArchiveNew);
            labelArchive.setEnabled(saveInArchiveNew);
            comboArchiveTypes.setEnabled(saveInArchiveNew);
        } 
        else if (source == boxExtensions) 
        {
            boolean appendExtensionsNew = boxExtensions.isSelected();
            model.setAppendExtensions(appendExtensionsNew);
        } 
        else if(source == comboFormats)
        {
            ChartSaveFormatType type = (ChartSaveFormatType) comboFormats.getSelectedItem();
            formatCardLayout.show(formatCardPanel, type.toString());
            model.setSaveFormat(type);
            updateLastUsedFormat();
        }
        else if(source == comboArchiveTypes)
        {
            ArchiveType type = (ArchiveType) comboArchiveTypes.getSelectedItem();
            model.setArchiveType(type);
            updateLastUsedArchiveType();
        }
        else if (boxesSaveSeries.containsValue(source)) 
        {
            JCheckBox box = (JCheckBox) source;
            boolean saveNew = box.isSelected();

            // find the right key

            String key = null;

            for (Entry<String, JCheckBox> entry : boxesSaveSeries.entrySet()) 
            {
                if (box.equals(entry.getValue())) 
                {
                    key = entry.getKey();
                }
            }
            if (key != null) 
            {
                model.setSaveSeries(saveNew, key);
                combosPrefix.get(key).setEnabled(saveNew);
                combosRoot.get(key).setEnabled(saveNew);
                combosSuffix.get(key).setEnabled(saveNew);
            }
        } 
        else if (source instanceof FileNamingCombo) 
        {
            FileNamingCombo combo = (FileNamingCombo) source;
            FileNamingComboType type = combo.getType();
            String key = combo.getKey();

            if (FileNamingComboType.PREFIX.equals(type)) 
            {                
                Object prefixNew = combo.getSelectedItem();
                model.setPrefix(prefixNew, key);
            } 
            else if (type.equals(FileNamingComboType.ROOT)) 
            {
                Object rootNew = combo.getSelectedItem();
                model.setRoot(rootNew, key);
            } 
            else if (type.equals(FileNamingComboType.SUFFIX)) 
            {
                Object suffixNew = combo.getSelectedItem();
                model.setSuffix(suffixNew, key);
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
            model.setInitialSerialNumber(initSerialNew);
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
        else if(MULTIPLE_PAGES_SUPPORTED.equals(property))
        {
            boolean multiplePagesSupported = (boolean)evt.getNewValue();
            spinnerSerial.setEnabled(!multiplePagesSupported);
        }
        else if (source instanceof ChannelSpecificSaveSettingsModel) 
        {
            ChannelSpecificSaveSettingsModel seriesModel = (ChannelSpecificSaveSettingsModel) source;
            String key = seriesModel.getKey();

            if (PREFIX.equals(property)) 
            {
                FileNamingCombo combo = combosPrefix.get(key);
                Object newVal = evt.getNewValue();
                Object oldVal = combo.getSelectedItem();
                if (!newVal.equals(oldVal)) 
                {
                    combo.setSelectedItem(newVal);
                }
            } 
            else if (ROOT.equals(property)) 
            {
                FileNamingCombo combo = combosRoot.get(key);

                Object newVal = evt.getNewValue();
                Object oldVal = combo.getSelectedItem();
                if (!newVal.equals(oldVal)) 
                {
                    combo.setSelectedItem(newVal);
                }
            } 
            else if (SUFFIX.equals(property)) 
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
        JLabel labelImages = new JLabel("Charts to save");
        JButton buttonSelect = new JButton(new SelectDirectoryAction());
        JButton buttonDefault = new JButton(new DefaultDirectoryAction());

        fieldDestination.setEnabled(false);

        destPanel.addComponent(labelDestDirectory, 0, 0, 2, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(4, 3, 6, 3));
        destPanel.addComponent(buttonSelect, 0, 1, 1, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(5, 3, 5, 3));
        destPanel.addComponent(fieldDestination, 1, 1, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1,new Insets(5, 3, 5, 3));
        destPanel.addComponent(buttonDefault, 2, 1, 1, 1,GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1,new Insets(5, 3, 5, 3));

        destPanel.addComponent(labelImages, 0, 2, 2, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(7, 3, 5, 3));
        destPanel.addComponent(imagesPanel, 0, 3, 3, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(5, 3, 5, 3));

        destPanel.addComponent(boxArchive, 0, 4, 2, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(7, 3, 5, 3));
        destPanel.addComponent(comboArchiveTypes, 1, 4, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,1, 1, new Insets(5, 3, 5, 3));
        destPanel.addComponent(labelArchive, 0, 5, 1, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(7, 3, 5, 3));
        destPanel.addComponent(fieldArchive, 1, 5, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,1, 1, new Insets(5, 3, 5, 3));

        destPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        generalPanel.addComponent(destPanel, 0, 0, 1, 1,GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);

        return generalPanel;
    }

    private JPanel buildFormatPanel() 
    {
        JPanel formatPanel = new JPanel(new BorderLayout());
        SubPanel southPanel = new SubPanel();
        southPanel.addComponent(new JLabel("Format"), 0, 0, 1, 1, GridBagConstraints.EAST,GridBagConstraints.NONE, 0.25, 1);
        southPanel.addComponent(comboFormats, 1, 0, 1, 1, GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL, 1, 1);
        southPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        this.formatCardPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

        formatPanel.add(formatCardPanel,BorderLayout.NORTH);
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

    private void save()
    {
        Map<String, List<? extends JFreeChart>> allCharts = new Hashtable<>();
        Map<String, List<String>> allDefaultNames = new Hashtable<>();
        Map<String, List<File>> allDefaultPaths = new Hashtable<>();

        for (String key : keys) 
        {
            if (model.isSeriesToBeSaved(key)) 
            {
                List<String> defaultNames = chartSource.getDefaultOutputNames(key);
                List<File> defaultPaths = chartSource.getDefaultOutputLocations(key);
                List<? extends JFreeChart> charts = chartSource.getAllNonemptyCharts(key);

                allDefaultNames.put(key, defaultNames);
                allDefaultPaths.put(key, defaultPaths);
                allCharts.put(key, charts);
            }
        }

        model.save(getParent(), allCharts, allDefaultNames, allDefaultPaths);
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
            save();

            BatchSaveDialog.this.setVisible(false);
        }
    }

    private class SelectDirectoryAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public SelectDirectoryAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME, "Select");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            int op = chooser.showOpenDialog(BatchSaveDialog.this);
            if (op == JFileChooser.APPROVE_OPTION) 
            {
                File directoryNew = chooser.getSelectedFile();
                String path = directoryNew.getPath();
                fieldDestination.setText(path);
                model.setDirectory(directoryNew);
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
            model.setDirectory(null);
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
            BatchSaveDialog.this.setVisible(false);
        }
    }
}
