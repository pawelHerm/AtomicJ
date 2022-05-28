
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

package atomicJ.readers.regularImage;

import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import atomicJ.data.units.SIPrefix;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.EditableComboBox;
import atomicJ.gui.MessageDisplayer;
import atomicJ.gui.NumericalField;
import atomicJ.gui.SubPanel;


public class ImageInterpretationDialog extends JDialog implements PropertyChangeListener, MessageDisplayer
{
    private static final long serialVersionUID = 1L;

    private static final double TOLERANCE = 1e-10;

    private final JLabel labelTaskName = new JLabel();
    private final JLabel labelTaskDescription = new JLabel();
    private final JLabel labelMessage = new JLabel();

    private final JLabel labelWidthValue = new JLabel();
    private final JLabel labelHeightValue = new JLabel();
    private final JLabel labelFrameCount = new JLabel();
    private final JLabel labelColorSpaceValue = new JLabel();

    private final NumericalField fieldImageWidth = new NumericalField("Image width must be a positive number", Double.MIN_NORMAL);
    private final NumericalField fieldImageHeight = new NumericalField("Image height must be a positive number", Double.MIN_NORMAL);
    private final JSpinner spinnerImageWidth = new JSpinner(new SpinnerNumberModel(1.,Double.MIN_NORMAL, Double.MAX_VALUE, 1.));
    private final JSpinner spinnerImageHeight = new JSpinner(new SpinnerNumberModel(1.,Double.MIN_NORMAL, Double.MAX_VALUE, 1.));

    private final JFormattedTextField fieldCombinedZQuantityName = new JFormattedTextField(new DefaultFormatter());
    private final JComboBox<SIPrefix> comboCombinedZQuantityPrefix = new JComboBox<>(SIPrefix.values());
    private final EditableComboBox comboCombinedZQuantityUnitName = new EditableComboBox(ImageInterpretationModel.getZQuantityUnitNames());

    private final Map<String, JFormattedTextField> zQuantityNameFields = new LinkedHashMap<>();
    private final Map<String, JComboBox<SIPrefix>> zQuantityPrefixCombos = new LinkedHashMap<>();
    private final Map<String, EditableComboBox> zQuantityUnitNameCombos = new LinkedHashMap<>();

    private final JComboBox<PrefixedUnit> comboImageWidthUnits = new JComboBox<>(ImageInterpretationModel.getSIUnits());
    private final JComboBox<PrefixedUnit> comboImageHeightUnits = new JComboBox<>(ImageInterpretationModel.getSIUnits());

    private final JRadioButton buttonChannelsCombined = new JRadioButton("Combined");
    private final JRadioButton buttonChannelsSeparate = new JRadioButton("Separate");
    private final ButtonGroup buttonGroupChannelCombination = new ButtonGroup();

    private final Map<String, JSpinner> coefficientSpinners = new LinkedHashMap<>();

    private final JCheckBox boxReadInROIs = new JCheckBox("Read ROIs");
    private final JCheckBox boxUseReadInColorGradients = new JCheckBox("Use read-in color gradients");

    private final Action aspectRatioAction = new AspectRatioConstantAction();
    private final Action finishAction = new FinishAction();
    private final Action cancelAction = new CancelAction();

    private final JButton buttonFinish = new JButton(finishAction);
    private final JButton buttonCancel = new JButton(cancelAction);

    private final ImageInterpretationModel model;

    public ImageInterpretationDialog(Window parent, ImageInterpretationModel model)
    {
        super(parent, "Image interpretation assistant", ModalityType.APPLICATION_MODAL);

        this.model = model;
        this.model.addPropertyChangeListener(this);

        DefaultFormatter formatter = (DefaultFormatter)fieldCombinedZQuantityName.getFormatter();
        formatter.setOverwriteMode(false);
        formatter.setCommitsOnValidEdit(true);

        buttonGroupChannelCombination.add(buttonChannelsCombined);
        buttonGroupChannelCombination.add(buttonChannelsSeparate);  
        buttonChannelsCombined.setHorizontalTextPosition(SwingConstants.LEFT);
        buttonChannelsSeparate.setHorizontalTextPosition(SwingConstants.LEFT);

        pullModelProperties();

        JPanel panelTaskInformation = buildTaskInformationPanel();
        JPanel panelButtons = buildButtonPanel();	
        JPanel mainPanel = buildMainPanel();

        panelTaskInformation.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        panelButtons.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), 
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));       
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));


        Container content = getContentPane();
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);

        layout.setAutoCreateContainerGaps(false);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(
                        layout.createParallelGroup()
                        .addComponent(panelButtons)
                        .addComponent(mainPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelTaskInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(panelTaskInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(mainPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panelButtons, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                );

        initItemListeners();
        initFieldsListener();
        initChangeListeners();  

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                ImageInterpretationDialog.this.cancel();
            }
        });


        pack();
        setLocationRelativeTo(parent);
    }


    //has to be called before buildMainPanel() method, as it builds the GUI components
    //whose number depends on the model
    private void pullModelProperties()
    {    
        String taskName = model.getTaskName();
        labelTaskName.setText(taskName);

        String taskDescription = model.getTaskDescription();
        labelTaskDescription.setText(taskDescription);

        int imageRowCount = model.getImageRowCount();
        int imageColumnCount = model.getImageColumnCount();
        int frameCount = model.getFrameCount();
        String colorSpace = model.getColorSpace();

        double imageWidth = model.getImageWidth();
        double imageHeight = model.getImageHeight();
        PrefixedUnit imageWidthUnit = model.getImageWidthUnit();
        PrefixedUnit imageHeightUnit = model.getImageHeightUnit();
        boolean aspectRatioConstant = model.isAspectRatioConstant();
        boolean combineChannels = model.isCombineChannels();

        boolean readInROIsAvailable = model.isROIsAvailableToRead();
        boolean readInROIs = model.isReadInROIs();
        boolean useReadInColorGradient = model.isUseReadInColorGradients();

        labelWidthValue.setText(Integer.toString(imageColumnCount));
        labelHeightValue.setText(Integer.toString(imageRowCount));
        labelFrameCount.setText(Integer.toString(frameCount));
        labelColorSpaceValue.setText(colorSpace);

        fieldImageWidth.setValue(imageWidth);
        fieldImageHeight.setValue(imageHeight);
        comboImageWidthUnits.setSelectedItem(imageWidthUnit);
        comboImageHeightUnits.setSelectedItem(imageHeightUnit);
        aspectRatioAction.putValue(Action.SELECTED_KEY, aspectRatioConstant);

        String combinedZQuantityName = model.getCombinedZQuantityName();
        SIPrefix combinedZQuantityPrefix = model.getCombinedZQuantityPrefix();
        String combinedZQuantityUnitName = model.getCombinedZQuatityUnitName();

        fieldCombinedZQuantityName.setValue(combinedZQuantityName);
        comboCombinedZQuantityPrefix.setSelectedItem(combinedZQuantityPrefix);
        comboCombinedZQuantityUnitName.setSelectedItem(combinedZQuantityUnitName);

        //this group of methods both create the components and sets the values
        //those components can be created only when the model is known, as their number depends
        //on the number of color channels

        initChannelCombinationCoefficientsSpinners();
        initChannelZQuantityNameFields();
        initChannelZQuantityPrefixCombos();
        initChannelZQuantityUnitNameCombos();

        setChannelCombinationEnabled(combineChannels);

        ButtonModel selectedButtonModel = combineChannels ? buttonChannelsCombined.getModel() : buttonChannelsSeparate.getModel();
        buttonGroupChannelCombination.setSelected(selectedButtonModel, true);

        boxReadInROIs.setEnabled(readInROIsAvailable);
        boxReadInROIs.setSelected(readInROIs);

        boxUseReadInColorGradients.setSelected(useReadInColorGradient);

        boolean finishEnabled = model.isFinishEnabled();
        buttonFinish.setEnabled(finishEnabled);
    }

    private void setChannelCombinationEnabled(boolean combinationEnabled)
    {
        //channels combined

        for(JSpinner spinner : coefficientSpinners.values())
        {
            spinner.setEnabled(combinationEnabled);
        }

        comboCombinedZQuantityPrefix.setEnabled(combinationEnabled);
        comboCombinedZQuantityUnitName.setEnabled(combinationEnabled);
        fieldCombinedZQuantityName.setEnabled(combinationEnabled);

        //channels separate

        for(JFormattedTextField field : zQuantityNameFields.values())
        {
            field.setEnabled(!combinationEnabled);
        }

        for(JComboBox<SIPrefix> combo : zQuantityPrefixCombos.values())
        {
            combo.setEnabled(!combinationEnabled);
        }

        for(JComboBox<String> combo : zQuantityUnitNameCombos.values())
        {
            combo.setEnabled(!combinationEnabled);
        }
    }

    private void initItemListeners()
    {   
        boxReadInROIs.addItemListener(new ItemListener() 
        {         
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setReadInROIs(selected);  
            }
        });

        boxUseReadInColorGradients.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent evt)
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setUseReadInColorGradients(selected);
            }
        });

        buttonChannelsCombined.addItemListener(new ItemListener() {          
            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                model.setCombineChannels(selected);
                setChannelCombinationEnabled(selected);
            }
        });
        comboImageWidthUnits.addItemListener(new ItemListener() {         
            @Override
            public void itemStateChanged(ItemEvent evt) {
                PrefixedUnit unit = (PrefixedUnit) comboImageWidthUnits.getSelectedItem();        
                model.specifyImageWidthUnit(unit);                
            }
        });
        comboImageHeightUnits.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent evt) {
                PrefixedUnit unit = (PrefixedUnit) comboImageHeightUnits.getSelectedItem();        
                model.specifyImageHeightUnit(unit);                
            }
        });
        comboCombinedZQuantityPrefix.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent e) {                
                SIPrefix prefix = (SIPrefix)comboCombinedZQuantityPrefix.getSelectedItem();
                model.setCombinedZQuantityPrefix(prefix);
            }
        });
        comboCombinedZQuantityUnitName.addItemListener(new ItemListener() 
        {          
            @Override
            public void itemStateChanged(ItemEvent e) {
                String unitName = (String)comboCombinedZQuantityUnitName.getSelectedItem();
                model.setCombinedZQuatityUnitName(unitName);                
            }
        });      
    }

    private void initChannelZQuantityPrefixCombos()
    {
        for(final String channel : model.getChannelNames())
        {
            final JComboBox<SIPrefix> combo = new JComboBox<>(SIPrefix.values());

            zQuantityPrefixCombos.put(channel, combo);
            combo.setSelectedItem(model.getChannelQuantityPrefix(channel));

            combo.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    SIPrefix prefixNew = (SIPrefix)combo.getSelectedItem();
                    model.setChannelQuantityPrefix(channel, prefixNew);
                }
            });
        }
    }

    private void initChannelZQuantityUnitNameCombos()
    {
        for(final String channel : model.getChannelNames())
        {
            final EditableComboBox combo = new EditableComboBox(ImageInterpretationModel.getZQuantityUnitNames());

            zQuantityUnitNameCombos.put(channel, combo);
            combo.setSelectedItem(model.getChannelQuantityUnitName(channel));

            combo.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    String nameNew = (String)combo.getSelectedItem();
                    model.setChannelQuantityUnitName(channel, nameNew);
                }
            });
        }
    }

    private void initChannelZQuantityNameFields()
    {
        for(final String channel : model.getChannelNames())
        {
            final JFormattedTextField fieldZQuantityName = new JFormattedTextField(new DefaultFormatter());

            DefaultFormatter formatter = (DefaultFormatter)fieldZQuantityName.getFormatter();
            formatter.setOverwriteMode(false);
            formatter.setCommitsOnValidEdit(true);

            zQuantityNameFields.put(channel, fieldZQuantityName);
            fieldZQuantityName.setValue(model.getChannelQuantityName(channel));

            fieldZQuantityName.addPropertyChangeListener("value", new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String valNew = (String)evt.getNewValue();
                    model.setChannelQuantityName(channel, valNew);
                }
            }
                    );
        }
    }

    private void initChannelCombinationCoefficientsSpinners()
    {
        for(final String channel : model.getChannelNames())
        {
            final JSpinner spinner = new JSpinner(new SpinnerNumberModel(1.,-Short.MAX_VALUE, Short.MAX_VALUE, 0.1));

            coefficientSpinners.put(channel, spinner);
            spinner.setValue(model.getCombinationCoefficient(channel));

            spinner.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e)
                {
                    double fractionNew = ((SpinnerNumberModel)spinner.getModel()).getNumber().doubleValue();
                    model.setCombinationCoefficient(channel, fractionNew);
                }
            });
        }
    }

    private void initChangeListeners()
    {
        spinnerImageWidth.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) 
            {
                Double imageWidthNew = ((SpinnerNumberModel)spinnerImageWidth.getModel()).getNumber().doubleValue();
                Number imageWidthOld = fieldImageWidth.getValue();

                if(!Objects.equals(imageWidthNew, imageWidthOld))
                {
                    model.specifyImageWidth(imageWidthNew);                
                }            
            }
        });

        spinnerImageHeight.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) 
            {
                Double imageHeightNew = ((SpinnerNumberModel)spinnerImageHeight.getModel()).getNumber().doubleValue();
                Number imageHeightOld = fieldImageHeight.getValue();

                if(!Objects.equals(imageHeightNew, imageHeightOld))
                {
                    model.specifyImageHeight(imageHeightNew);
                }            
            }
        });
    }

    private void initFieldsListener()
    {
        fieldImageWidth.addPropertyChangeListener(NumericalField.VALUE_EDITED,new PropertyChangeListener() {            
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                double valNew = ((Number)evt.getNewValue()).doubleValue();

                model.specifyImageWidth(valNew);            
            }
        }); 
        fieldImageWidth.addPropertyChangeListener("value",new PropertyChangeListener() {            
            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                double valNew = ((Number)evt.getNewValue()).doubleValue();

                if(!Double.isNaN(valNew))
                {
                    spinnerImageWidth.setValue(valNew);
                }            
            }
        }); 
        fieldImageHeight.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                double valNew = ((Number)evt.getNewValue()).doubleValue();
                model.specifyImageHeight(valNew);              
            }
        });
        fieldImageHeight.addPropertyChangeListener("value", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                double valNew = ((Number)evt.getNewValue()).doubleValue();

                if(!Double.isNaN(valNew))
                {
                    spinnerImageHeight.setValue(valNew);
                }             
            }
        });
        fieldCombinedZQuantityName.addPropertyChangeListener("value", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) 
            {
                String valNew = (String)evt.getNewValue();
                model.setCombinedZQuantityName(valNew);
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String property = evt.getPropertyName();

        if(ImageInterpretationModel.COMBINE_CHANNELS.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = buttonChannelsCombined.isSelected();

            if(valNew != valOld)
            {
                buttonGroupChannelCombination.clearSelection();
                ButtonModel selectedModel = valNew ? buttonChannelsCombined.getModel() : buttonChannelsSeparate.getModel();
                buttonGroupChannelCombination.setSelected(selectedModel, true);
            }
        }
        else if(ImageInterpretationModel.IMAGE_WIDTH.equals(property))
        {            
            double valNew = (double)evt.getNewValue();
            double valOld = fieldImageWidth.getValue().doubleValue();

            boolean onlyOneNaN = Double.isNaN(valOld) != Double.isNaN(valNew);

            if(onlyOneNaN || Math.abs(valOld - valNew) > TOLERANCE)
            {
                fieldImageWidth.setValue(valNew);
            }
        }
        else if(ImageInterpretationModel.IMAGE_HEIGHT.equals(property))
        {
            double valNew = (double)evt.getNewValue();
            double valOld = fieldImageHeight.getValue().doubleValue();

            boolean onlyOneNaN = Double.isNaN(valOld) != Double.isNaN(valNew);

            if(onlyOneNaN || Math.abs(valOld - valNew) > TOLERANCE)
            {
                fieldImageHeight.setValue(valNew);
            }
        }
        else if(ImageInterpretationModel.IMAGE_WIDTH_UNIT.equals(property))
        {
            PrefixedUnit valNew = (PrefixedUnit)evt.getNewValue();
            PrefixedUnit valOld = (PrefixedUnit)comboImageWidthUnits.getSelectedItem();

            if(!Objects.equals(valNew, valOld))
            {
                comboImageWidthUnits.setSelectedItem(valNew);
            }
        }
        else if(ImageInterpretationModel.IMAGE_HEIGHT_UNIT.equals(property))
        {
            PrefixedUnit valNew = (PrefixedUnit)evt.getNewValue();
            PrefixedUnit valOld = (PrefixedUnit)comboImageHeightUnits.getSelectedItem();

            if(!Objects.equals(valNew, valOld))
            {
                comboImageHeightUnits.setSelectedItem(valNew);
            }
        }
        else if(ImageInterpretationModel.COMBINED_Z_QUANTITY_NAME.equals(property))
        {
            String valNew = (String) evt.getNewValue();
            String valOld = fieldCombinedZQuantityName.toString();

            if(!Objects.equals(valOld, valNew))
            {
                fieldCombinedZQuantityName.setValue(valNew);
            }
        }
        else if(ImageInterpretationModel.COMBINED_Z_QUANTITY_PREFIX.equals(property))
        {
            SIPrefix valNew = (SIPrefix) evt.getNewValue();
            SIPrefix valOld = (SIPrefix)comboCombinedZQuantityPrefix.getSelectedItem();

            if(!Objects.equals(valOld, valNew))
            {
                comboCombinedZQuantityPrefix.setSelectedItem(valNew);
            }
        }
        else if(ImageInterpretationModel.COMBINED_Z_QUANTITY_UNIT_NAME.equals(property))
        {
            String valNew = (String) evt.getNewValue();
            String valOld = (String)comboCombinedZQuantityUnitName.getSelectedItem();

            if(!Objects.equals(valOld, valNew))
            {
                comboCombinedZQuantityUnitName.setSelectedItem(valNew);
            }
        }
        else if(ImageInterpretationModel.ASPECT_RATIO_CONSTANT.equals(property))
        {
            boolean aspectRatioSelectedNew = (boolean)evt.getNewValue();          
            aspectRatioAction.putValue(Action.SELECTED_KEY, aspectRatioSelectedNew);
        }
        else if(ImageInterpretationModel.READ_IN_ROIS.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = boxReadInROIs.isSelected();

            if(valOld != valNew)
            {
                boxReadInROIs.setSelected(valNew);
            }
        }
        else if(ImageInterpretationModel.USE_READIN_COLOR_GRADIENTS.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = boxReadInROIs.isSelected();

            if(valOld != valNew)
            {
                boxUseReadInColorGradients.setSelected(valNew);
            }
        }
        else if(ImageInterpretationModel.FINISH_ENABLED.equals(property))
        {
            boolean valNew = (boolean)evt.getNewValue();
            boolean valOld = buttonFinish.isEnabled();
            if(valNew != valOld)
            {
                buttonFinish.setEnabled(valNew);
            }
        }

        List<String> channels = model.getChannelNames();

        //listenes to changes in channel combination coefficients
        for(String channel : channels)
        {
            String prop = ImageInterpretationModel.CHANNEL_COMBINATION_FRACTION + channel;

            if(prop.equals(property))
            {
                JSpinner spinner = coefficientSpinners.get(channel);

                double valNew = (double)evt.getNewValue();
                double valOld = ((Number)spinner.getValue()).doubleValue();

                if(Math.abs(valOld - valNew) > TOLERANCE)
                {
                    spinner.setValue(valNew);
                }

                break;
            }
        }

        //listens to changes in separate channels z quantity names

        for(String channel : channels)
        {
            String prop = ImageInterpretationModel.CHANNEL_Z_QUANTITY_NAME + channel;

            if(prop.equals(property))
            {
                JFormattedTextField field = zQuantityNameFields.get(channel);

                String valNew = (String)evt.getNewValue();
                String valOld = field.getValue().toString();

                if(!Objects.equals(valNew, valOld))
                {
                    field.setValue(valNew);
                }

                return;
            }
        }

        //listens to changes in separate channels z quantity unit names

        for(String channel : channels)
        {
            String prop = ImageInterpretationModel.CHANNEL_Z_QUANTITY_UNIT_NAME + channel;

            if(prop.equals(property))
            {
                JComboBox<String> combo = zQuantityUnitNameCombos.get(channel);

                String valNew = (String)evt.getNewValue();
                String valOld = (String)combo.getSelectedItem();

                if(!Objects.equals(valNew, valOld))
                {
                    combo.setSelectedItem(valNew);
                }

                return;
            }
        }

        //listens to changes in separate channels z quantity prefixes

        for(String channel : channels)
        {
            String prop = ImageInterpretationModel.CHANNEL_Z_QUANTITY_PREFIX + channel;

            if(prop.equals(property))
            {
                JComboBox<SIPrefix> combo = zQuantityPrefixCombos.get(channel);

                SIPrefix valNew = (SIPrefix)evt.getNewValue();
                SIPrefix valOld = (SIPrefix)combo.getSelectedItem();

                if(!Objects.equals(valNew, valOld))
                {
                    combo.setSelectedItem(valNew);
                }

                return;
            }
        }
    }

    private JPanel buildTaskInformationPanel()
    {
        SubPanel panel = new SubPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));	

        Color color = UIManager.getColor("Label.background");
        panel.setBackground(color);
        labelTaskName.setBackground(color);
        labelTaskDescription.setBackground(color);
        labelMessage.setBackground(color);

        Font font = UIManager.getFont("TextField.font");
        labelTaskName.setFont(font.deriveFont(Font.BOLD));
        labelTaskDescription.setFont(font);

        panel.addComponent(labelTaskName, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        panel.addComponent(labelTaskDescription, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        panel.addComponent(labelMessage, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);

        return panel;
    }

    private JPanel buildButtonPanel()
    {
        JPanel panelButtons = new JPanel();	

        GroupLayout layout = new GroupLayout(panelButtons);
        panelButtons.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonFinish)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonFinish)
                .addComponent(buttonCancel));

        layout.linkSize(buttonFinish,buttonCancel);

        return panelButtons;
    }

    public void finish()
    {
        model.finish();
        setVisible(false);
    }

    public void cancel()
    {
        model.cancel();
        setVisible(false);
    }

    @Override
    public void publishErrorMessage(String message) 
    {
        Icon icon = UIManager.getIcon("OptionPane.errorIcon");
        labelMessage.setIcon(icon);
        labelMessage.setText(message);
        pack();
        validate();
    }

    @Override
    public void publishWarningMessage(String message) 
    {
        Icon icon = UIManager.getIcon("OptionPane.warningIcon");
        labelMessage.setIcon(icon);
        labelMessage.setText(message);
        pack();
        validate();
    }

    @Override
    public void publishInformationMessage(String message) 
    {
        Icon icon = UIManager.getIcon("OptionPane.informationIcon");
        labelMessage.setIcon(icon);
        labelMessage.setText(message);
        pack();
        validate();
    }

    @Override
    public void clearMessage() 
    {
        labelMessage.setIcon(null);
        labelMessage.setText(null);
        pack();
        validate();
    }

    private class FinishAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FinishAction()
        {			
            putValue(NAME, "Finish");
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            finish();
        }
    }

    private class CancelAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CancelAction()
        {			
            putValue(NAME, "Cancel");
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            cancel();
        }
    }

    private class AspectRatioConstantAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public AspectRatioConstantAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon iconDeselected = new ImageIcon(
                    toolkit.getImage("Resources/chainLinksBrokenSmall.png"));

            putValue(LARGE_ICON_KEY, iconDeselected);

            putValue(NAME, "Aspect ratio constant");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean constant = (boolean) getValue(SELECTED_KEY);
            model.setAspectRatioConstant(constant);
        }
    }

    private SubPanel buildMainPanel()
    {        
        ///aspect ratio button

        JToggleButton buttonAspectRatio = new JToggleButton(aspectRatioAction);

        buttonAspectRatio.setHideActionText(true);
        buttonAspectRatio.setMargin(new Insets(3, 5, 3, 5));

        ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Resources/chainLinksSmall.png"));

        buttonAspectRatio.setSelectedIcon(icon);

        //// sets components fonts

        Font font = UIManager.getFont("TextField.font");

        SubPanel panel = new SubPanel();

        JLabel labelImageInfo = new JLabel("Image properties");

        labelImageInfo.setFont(font.deriveFont(Font.BOLD));
        buttonChannelsCombined.setFont(font.deriveFont(Font.BOLD));
        buttonChannelsSeparate.setFont(font.deriveFont(Font.BOLD));

        panel.addComponent(labelImageInfo, 0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(5,3,5,3));
        panel.addComponent(buildImageInfoPanel(), 0, 5, 4, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);

        JLabel labelScaling = new JLabel("Scaling");
        labelScaling.setFont(font.deriveFont(Font.BOLD));

        spinnerImageWidth.setEditor(fieldImageWidth);    
        spinnerImageHeight.setEditor(fieldImageHeight);

        panel.addComponent(labelScaling, 0, 6, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(5,3,5,3));
        panel.addComponent(new JLabel("Width"), 0, 7, 1, 1, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, 1, 1);
        panel.addComponent(spinnerImageWidth, 1, 7, 1, 1, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, 1, 1);
        panel.addComponent(comboImageWidthUnits, 2, 7, 1, 1, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, 0.1, 1);
        panel.addComponent(buttonAspectRatio, 3, 7, 1, 2, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, 1, 1);

        panel.addComponent(new JLabel("Height"), 0, 8, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, 1, 1);
        panel.addComponent(spinnerImageHeight, 1, 8, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 1);
        panel.addComponent(comboImageHeightUnits, 2, 8, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 0.1, 1);

        JLabel labelChannels = new JLabel("Channels");
        labelChannels.setFont(font.deriveFont(Font.BOLD));

        int controlsRowCount = 5;

        panel.addComponent(labelChannels, 0, 9, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(5,3,5,3));

        panel.addComponent(new JLabel("Coefficients"), 1, 10, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1, new Insets(5,3,5,3));
        panel.addComponent(buttonChannelsCombined, 0, 11, 1, Math.min(controlsRowCount, coefficientSpinners.size()), GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, 1, 1);


        Iterator<Entry<String, JSpinner>> spinnersIterator = coefficientSpinners.entrySet().iterator();

        for(int i = 0; i<coefficientSpinners.size();i++)
        {        
            Entry<String, JSpinner> entry = spinnersIterator.next();
            int indexY = 11 + i % controlsRowCount;
            int indexX = 1 + 2*(i/controlsRowCount);

            String channelName = entry.getKey();
            JSpinner spinner = entry.getValue();

            panel.addComponent(spinner, indexX, indexY, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);          
            panel.addComponent(new JLabel(channelName), indexX + 1, indexY, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        }

        int coefficientSpinnerLabelLastIndex = 1 + 2*((coefficientSpinners.size() - 1)/controlsRowCount) + 1;

        SubPanel panelZQuantityUnit = new SubPanel();

        panelZQuantityUnit.addComponent(comboCombinedZQuantityPrefix, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(5,1,5,1));
        panelZQuantityUnit.addComponent(comboCombinedZQuantityUnitName, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(5,1,5,1));          


        panel.addComponent(new JLabel("Combined quantity"), coefficientSpinnerLabelLastIndex + 1, 10, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1, new Insets(5,3,5,3));
        panel.addComponent(fieldCombinedZQuantityName, coefficientSpinnerLabelLastIndex + 1, 11, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);          
        panel.addComponent(panelZQuantityUnit, coefficientSpinnerLabelLastIndex + 1, 12, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);          

        int maxRowA = panel.getMaxRow();

        panel.addComponent(Box.createVerticalStrut(10), 0, maxRowA + 1, 4, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);
        panel.addComponent(buttonChannelsSeparate, 0, maxRowA + 2, 1, Math.min(controlsRowCount, coefficientSpinners.size()), GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, 1, 1);

        List<String> channels = model.getChannelNames();

        for(int i = 0; i<channels.size(); i++)
        {     
            int indexY = maxRowA + 2 + i % controlsRowCount;
            int indexX = 1 + 3*(i/controlsRowCount);

            String channel = channels.get(i);

            JFormattedTextField field = zQuantityNameFields.get(channel);
            JComboBox<SIPrefix> comboPrefix = zQuantityPrefixCombos.get(channel);
            JComboBox<String> comboUnitName = zQuantityUnitNameCombos.get(channel);

            SubPanel panelChannelZQuantityUnit = new SubPanel();

            panelChannelZQuantityUnit.addComponent(comboPrefix, 0, 0, 1, 1, 
                    GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(5,1,5,1));
            panelChannelZQuantityUnit.addComponent(comboUnitName, 1, 0, 1, 1,
                    GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(5,1,5,1));    

            panel.addComponent(field, indexX, indexY, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);          
            panel.addComponent(panelChannelZQuantityUnit, indexX + 1, indexY, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);

        }

        panel.addComponent(Box.createVerticalStrut(10), 0, panel.getMaxRow() + 1, 4, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);

        JLabel labelOtherSettings = new JLabel("Other settings");
        labelOtherSettings.setFont(font.deriveFont(Font.BOLD));

        panel.addComponent(labelOtherSettings, 0, panel.getMaxRow() + 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1, new Insets(5,3,5,3));
        panel.addComponent(boxReadInROIs, 1, panel.getMaxRow() + 1, 2, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);          
        panel.addComponent(boxUseReadInColorGradients, 1, panel.getMaxRow() + 1, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);


        return panel;
    }

    private JPanel buildImageInfoPanel()
    {
        SubPanel panelImageInfo = new SubPanel();

        panelImageInfo.addComponent(Box.createHorizontalGlue(), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        panelImageInfo.addComponent(new JLabel("Frames"), 1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panelImageInfo.addComponent(labelFrameCount, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);
        panelImageInfo.addComponent(new JLabel("Color space"), 3, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panelImageInfo.addComponent(labelColorSpaceValue, 4, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);
        panelImageInfo.addComponent(Box.createHorizontalGlue(), 5, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        panelImageInfo.addComponent(Box.createHorizontalGlue(), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        panelImageInfo.addComponent(new JLabel("Width (px)"), 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panelImageInfo.addComponent(labelWidthValue, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);
        panelImageInfo.addComponent(new JLabel("Height (px)"), 3, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);
        panelImageInfo.addComponent(labelHeightValue, 4, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);
        panelImageInfo.addComponent(Box.createHorizontalGlue(), 5, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        return panelImageInfo;
    }
}
