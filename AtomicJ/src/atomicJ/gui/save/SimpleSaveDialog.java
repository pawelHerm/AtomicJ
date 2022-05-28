
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
import javax.swing.filechooser.FileNameExtensionFilter;

import atomicJ.gui.AtomicJ;
import atomicJ.gui.ExtensionFileChooser;
import atomicJ.gui.SubPanel;
import atomicJ.gui.UserCommunicableException;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static atomicJ.gui.save.SaveModelProperties.*;

public class SimpleSaveDialog<E extends SaveFormatType, M extends SimpleSaveModel<E>> extends JDialog implements PropertyChangeListener 
{
    private static final long serialVersionUID = 1L;

    private static final String LAST_USED_CHART_FORMAT = "LastUsedChartFormat";
    private final Preferences pref;

    private final M model;

    private final SaveAction saveAction = new SaveAction();

    private final JTextField fieldDestination = new JTextField();
    private final JComboBox<E> comboFormats = new JComboBox<>();

    private final FormatOptionsDialog optionsDialog;
    private final ExtensionFileChooser chooser;

    public SimpleSaveDialog(M model, String title, Window parent, JPanel parentPanel, String savePreferences) 
    {
        this(model, title, Collections.emptyList(), parent, parentPanel, savePreferences);
    }

    public SimpleSaveDialog(M model, String title, List<Map.Entry<JComponent, GridBagConstraints>> additionalComponents, Window parent, JPanel parentPanel, String savePreferences) 
    {
        super(parent, title, ModalityType.APPLICATION_MODAL);

        this.model = model;
        this.optionsDialog = new FormatOptionsDialog(this, model);

        this.pref = Preferences.userRoot().node(getClass().getName()).node(savePreferences);
        this.chooser = new ExtensionFileChooser(pref, true);

        Image icon = Toolkit.getDefaultToolkit().getImage("Resources/Logo.png");
        setIconImage(icon);

        chooser.setApproveButtonMnemonic('S');
        model.addPropertyChangeListener(this);

        pullModelProperties();
        initItemListener();

        setLayout(new BorderLayout());

        JPanel generalPanel = buildGeneralPanel(additionalComponents);
        JPanel buttonPanel = buildButtonPanel();

        add(generalPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parentPanel);
    }

    protected void showDialog()
    {
        String format = getLastUsedFormat();
        if(format != null)
        {
            model.setSaveFormat(format);
        }

        setVisible(true);
    }

    protected M getModel()
    {
        return model;
    }

    public void setChooserCurrentDirectory(File dir)
    {
        chooser.setCurrentDirectory(dir);
    }

    private void pullModelProperties() 
    {
        File directory = model.getOutputFile();
        SaveFormatType saveFormat = model.getSaveFormat();

        if (directory != null) 
        {
            fieldDestination.setText(directory.getPath());
        }

        List<E> formatTypes = model.getFormatTypes();
        for(E formatType: formatTypes)
        {
            comboFormats.addItem(formatType);
        }        

        comboFormats.setSelectedItem(saveFormat);
        chooser.setEnforcedExtension(saveFormat.getFileNameExtensionFilter());

        boolean inputSpecified = model.isNecessaryInputProvided();
        saveAction.setEnabled(inputSpecified);
    }

    protected void updateLastUsedFormat()
    {
        SaveFormatType type = (SaveFormatType)comboFormats.getSelectedItem();

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

    protected String getLastUsedFormat()
    {
        String typeDescription = pref.get(LAST_USED_CHART_FORMAT, null);
        return typeDescription;
    }

    private void browse()
    {
        int op = chooser.showDialog(SimpleSaveDialog.this, "Select");

        if (op == JFileChooser.APPROVE_OPTION) 
        {
            File directoryNew = chooser.getSelectedFile();
            String path = directoryNew.getPath();
            fieldDestination.setText(path);
            model.setFilePath(directoryNew);
        }
    }

    private void initItemListener() 
    {
        comboFormats.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                E type = comboFormats.getItemAt(comboFormats.getSelectedIndex());
                model.setSaveFormat(type);
                chooser.setEnforcedExtension(type.getFileNameExtensionFilter());
                updateLastUsedFormat();                
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if (FILE_PATH.equals(property)) 
        {
            File newVal = (File) evt.getNewValue();

            chooser.setSelectedFile(newVal);

            String text = (newVal == null) ? "" : newVal.getAbsolutePath();
            fieldDestination.setText(text);
        } 
        else if(SAVE_FORMAT.equals(property)) 
        {
            E newVal = (E) evt.getNewValue();
            E oldVal = comboFormats.getItemAt(comboFormats.getSelectedIndex());

            if (!oldVal.equals(newVal)) 
            {
                comboFormats.setSelectedItem(newVal);
                chooser.setEnforcedExtension(newVal.getFileNameExtensionFilter());
                updateLastUsedFormat();
            }
        } 
        else if (INPUT_PROVIDED.equals(property)) 
        {
            boolean newVal = (Boolean) evt.getNewValue();
            saveAction.setEnabled(newVal);
        } 
        else if(FILE_FILTER.equals(property))
        {
            FileNameExtensionFilter fileFilter = (FileNameExtensionFilter)evt.getNewValue();
            chooser.setEnforcedExtension(fileFilter);
        }  
    }

    private SubPanel buildGeneralPanel(List<Map.Entry<JComponent, GridBagConstraints>> additionalComponents) 
    {
        SubPanel generalPanel = new SubPanel();
        SubPanel destPanel = new SubPanel();

        JLabel labelDestDirectory = new JLabel("Destination");
        JButton buttonBrowse = new JButton(new BrowseAction());
        JButton buttonOptions = new JButton(new OptionsAction());

        JLabel labelFormat = new JLabel("Format");

        fieldDestination.setEnabled(false);

        for(Map.Entry<JComponent, GridBagConstraints> entry: additionalComponents)
        {
            destPanel.add(entry.getKey(), entry.getValue());
        }

        int firstRow = additionalComponents.isEmpty() ? 0 : destPanel.getMaxRow() + 1;

        destPanel.addComponent(labelDestDirectory, 0, firstRow, 1, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(4, 3, 6, 3));
        destPanel.addComponent(fieldDestination, 1, firstRow, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1,new Insets(5, 3, 5, 3));
        destPanel.addComponent(buttonBrowse, 2, firstRow, 1, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(5, 3, 5, 3));
        destPanel.addComponent(labelFormat, 0, firstRow + 1, 1, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(4, 3, 6, 3));
        destPanel.addComponent(comboFormats, 1, firstRow + 1, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1,new Insets(5, 3, 5, 3));
        destPanel.addComponent(buttonOptions, 2, firstRow +  1, 1, 1,GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1,new Insets(5, 3, 5, 3));

        destPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        generalPanel.addComponent(destPanel, 0, 0, 1, 1,GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);

        return generalPanel;
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
        try 
        {
            if(!model.isSavePathFree())
            {
                int result = JOptionPane.showConfirmDialog(SimpleSaveDialog.this,"The file exists, overwrite?", AtomicJ.APPLICATION_NAME,JOptionPane.YES_NO_CANCEL_OPTION);
                switch(result)
                {
                case JOptionPane.YES_OPTION: break;
                case JOptionPane.NO_OPTION: return;
                case JOptionPane.CANCEL_OPTION: 
                    setVisible(false);
                    return;
                }
            }

            model.save();
        } 
        catch (UserCommunicableException e) 
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errors occured during saving of the files", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
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
            save();
            SimpleSaveDialog.this.setVisible(false);
        }
    }

    private class BrowseAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public BrowseAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(NAME, "Browse");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            browse();
        }
    }

    private class OptionsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public OptionsAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(NAME, "Options");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            optionsDialog.showInputPanel(model.getSaveFormat());		
            optionsDialog.setVisible(true);
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
            SimpleSaveDialog.this.setVisible(false);
        }
    }
}
