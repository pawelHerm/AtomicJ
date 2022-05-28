
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


package atomicJ.gui.editors;

import static atomicJ.gui.PreferenceKeys.*;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;


import org.jfree.ui.*;
import atomicJ.gui.CustomizableXYBaseChart;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.Channel2DChart;
import atomicJ.gui.FontDisplayField;
import atomicJ.gui.FontReceiver;
import atomicJ.gui.GradientPaint;
import atomicJ.gui.PaintReceiver;
import atomicJ.gui.RoamingTextTitle;
import atomicJ.gui.SkewedGradientEditionDialog;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;


public class RoamingTitleSubeditor extends JPanel implements PropertyChangeListener, PaintReceiver, Subeditor
{
    private static final long serialVersionUID = 1L;

    private final Preferences pref;

    private final boolean initVisible;
    private final boolean initUseGradientPaint;
    private final Font initFont;
    private final Paint initPaint;

    private final boolean initUseAutomaticTitle;   
    private final String initAutomaticTitleType;
    private final boolean automaticTitlesSupported;

    private boolean visible;
    private boolean useGradientPaint;
    private Font textFont;
    private Paint textPaint;

    private boolean useAutomaticTitle;
    private String automaticTitleType;
    private final Map<String, String> automaticTitleTypes = new LinkedHashMap<>();

    private final JCheckBox boxTitleVisible = new JCheckBox();
    private final JCheckBox boxUseGradient = new JCheckBox();

    private final JCheckBox boxUseAutomaticTitle = new JCheckBox();
    private final JComboBox<String> comboAutomaticTitles = new JComboBox<>();

    private final JFormattedTextField fieldTitle = new JFormattedTextField(new DefaultFormatter());
    private final FontDisplayField fieldFont;
    private final PaintSample titlePaintSample;
    private final JButton buttonSelectFont = new JButton("Select");
    private final JButton buttonSelectPaint = new JButton("Select");

    private SkewedGradientEditionDialog gradientDialog;

    private final RoamingTextTitle titleOriginal;
    private final RoamingTextTitle titleWorking;

    private final TitleFrameStyleSubeditor titleFrameSubeditor;
    private final TitlePositionSubeditor titlePositionSubeditor;

    private final CustomizableXYBaseChart<?> chart;
    private final List<? extends CustomizableXYBaseChart<?>> boundedCharts;

    private FontChooserDialog fontChooserDialog;

    private final JTabbedPane tabbedPane;


    public RoamingTitleSubeditor(List<? extends CustomizableXYBaseChart<?>> boundedCharts,
            CustomizableXYBaseChart<?> chart) 
    {
        this.chart = chart;
        this.titleOriginal = chart.getRoamingTitle();
        this.boundedCharts = boundedCharts;
        this.pref = chart.getPreferences();

        if(titleOriginal == null)
        {
            this.titleWorking = chart.buildNewTitle("Title");
            titleWorking.setVisible(false);       
            chart.setRoamingTitle(titleWorking);
        }
        else
        {
            this.titleWorking = titleOriginal;
        }

        this.initVisible = titleWorking.isVisible();
        this.initFont = titleWorking.getFont();
        this.initPaint = titleWorking.getPaint();
        this.initUseGradientPaint = initPaint instanceof GradientPaint;


        ///////////////////automatic titles

        this.automaticTitleTypes.putAll(chart.getAutomaticTitles());
        //for one title it is not really necessary
        this.automaticTitlesSupported = this.automaticTitleTypes.size() > 1;

        String currentAutomaticTitleType = findCurrentAutomaticTitleType();
        this.initUseAutomaticTitle = (currentAutomaticTitleType != null) && automaticTitlesSupported;

        this.initAutomaticTitleType = currentAutomaticTitleType;

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(automaticTitleTypes.keySet().toArray(new String[] {}));
        this.comboAutomaticTitles.setModel(model);
        this.comboAutomaticTitles.setEnabled(initUseAutomaticTitle);
        /////////////////////////////////  


        setParametersToInitialPrivate();

        this.fieldFont = new FontDisplayField(initFont);
        boxTitleVisible.setSelected(initVisible);
        fieldTitle.setValue(titleWorking.getText());
        fieldTitle.addPropertyChangeListener("value",this);

        DefaultFormatter formatter = (DefaultFormatter)fieldTitle.getFormatter();
        formatter.setOverwriteMode(false);
        formatter.setCommitsOnValidEdit(true);

        this.titlePaintSample = new PaintSample(initPaint);

        this.titleFrameSubeditor = new TitleFrameStyleSubeditor(titleWorking, boundedCharts);
        this.titlePositionSubeditor = new TitlePositionSubeditor(titleWorking, boundedCharts);

        setLayout(new BorderLayout());

        JPanel mainPanel = buildMainPanel();

        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        tabbedPane.add("Text", mainPanel);
        tabbedPane.add("Frame", titleFrameSubeditor.getEditionComponent());
        tabbedPane.add("Position", titlePositionSubeditor.getEditionComponent());

        JPanel buttonPanel = buildButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        add(tabbedPane, BorderLayout.CENTER);

        initActionListener();
        initItemListeners();
    }

    private String findCurrentAutomaticTitleType()
    {
        String text = this.titleWorking.getText();
        String currentType = null;

        for(Entry<String, String> entry : automaticTitleTypes.entrySet())
        {
            String titleType = entry.getValue();

            if(Objects.equals(text, titleType))
            {
                currentType = entry.getKey();
                break;
            }
        }

        return currentType;
    }

    public static RoamingTitleSubeditor getInstance(List<? extends CustomizableXYBaseChart<?>> boundedCharts, CustomizableXYBaseChart<?> chart)
    {        
        if(chart instanceof Channel2DChart<?>)
        {
            Channel2DChart<?> densityChart = (Channel2DChart<?>)chart;

            boolean movie = densityChart.isMovie();

            if(movie)
            {
                return new RoamingStackTitleSubeditor(boundedCharts, densityChart);
            }
        }

        return new RoamingTitleSubeditor(boundedCharts, chart);
    }

    protected void insertEditorTab(Component component, String title, String tip, int index)
    {
        tabbedPane.insertTab(title, null, component, tip, index);
    }

    protected RoamingTextTitle getTitleWorking()
    {
        return titleWorking;
    }

    protected Preferences getPreferences()
    {
        return pref;
    }

    private void setParametersToInitialPrivate()
    {
        this.visible = initVisible;
        this.useGradientPaint = initUseGradientPaint;
        this.textFont = initFont;
        this.textPaint = initPaint;

        this.useAutomaticTitle = initUseAutomaticTitle;
        this.automaticTitleType = initAutomaticTitleType;
    } 

    protected void setParametersToInitial()
    {
        setParametersToInitialPrivate();
    }

    private void initActionListener()
    {		
        buttonSelectFont.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptFontSelection();                
            }
        });
        buttonSelectPaint.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                attemptPaintSelection();                
            }
        });
    }

    private void initItemListeners()
    {
        boxTitleVisible.addItemListener(new ItemListener() 
        {
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {                 
                visible = (evt.getStateChange()== ItemEvent.SELECTED);
                if(chart.getTitle() != titleWorking)
                {
                    chart.setRoamingTitle(titleWorking);
                }
                titleWorking.setVisible(visible); 
            }
        });

        boxUseGradient.addItemListener(new ItemListener() 
        {           
            @Override
            public void itemStateChanged(ItemEvent evt) {
                useGradientPaint = (evt.getStateChange()== ItemEvent.SELECTED);                
            }
        });

        boxUseAutomaticTitle.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                useAutomaticTitle = (evt.getStateChange() == ItemEvent.SELECTED);
                comboAutomaticTitles.setEnabled(useAutomaticTitle);   

                updateAutomaticTitleType();
            }
        });

        comboAutomaticTitles.addItemListener(new ItemListener()
        {            
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                updateAutomaticTitleType();              
            }
        });
    }


    private void updateAutomaticTitleType()
    {
        if(useAutomaticTitle)
        {
            automaticTitleType = comboAutomaticTitles.getSelectedItem().toString();             
            String textNew =  automaticTitleTypes.get(automaticTitleType);
            fieldTitle.setValue(textNew); 
        }
    }

    private void hideRoot()       
    {
        Component root = SwingUtilities.getRoot(this);
        root.setVisible(false);
    }



    public void attemptFontSelection() 
    {
        if( fontChooserDialog == null)
        {
            this.fontChooserDialog = new FontChooserDialog(SwingUtilities.getWindowAncestor(this), "Font selection");
        }

        this.fontChooserDialog.showDialog(new FontReceiver() 
        {

            @Override
            public void setFont(Font newFont) 
            { 
                textFont = newFont;
                fieldFont.setDisplayFont(textFont);
                titleWorking.setFont(textFont);
            }

            @Override
            public Font getFont()
            {
                return textFont;
            }
        });
    }

    public void attemptPaintSelection() 
    {
        if(useGradientPaint)
        {
            if(gradientDialog == null)
            {
                gradientDialog = new SkewedGradientEditionDialog(SwingUtilities.getWindowAncestor(this));
            }
            gradientDialog.showDialog(this);
        }
        else
        {
            Paint textPaintNew = JColorChooser.showDialog(RoamingTitleSubeditor.this, "Title text color", Color.blue);	        
            if (textPaintNew != null) 
            {
                textPaint = textPaintNew;
                titlePaintSample.setPaint(textPaint);
                titleWorking.setPaint(textPaint);			
            }
        } 
    }

    @Override
    public void resetToDefaults() 
    {
        this.titleFrameSubeditor.resetToDefaults();
        this.titlePositionSubeditor.resetToDefaults();

        this.textFont = (Font)SerializationUtilities.getSerializableObject(pref, TITLE_FONT, new Font("Dialog", Font.PLAIN, 14));
        this.textPaint = (Paint)SerializationUtilities.getSerializableObject(pref, TITLE_PAINT, Color.black);
        this.useGradientPaint = textPaint instanceof GradientPaint;

        this.useAutomaticTitle = pref.getBoolean(TITLE_TEXT_AUTOMATIC, useAutomaticTitle);
        String defaultAutomaticTitleType = pref.get(TITLE_AUTOMATIC_TEXT_TYPE, automaticTitleType);

        if(automaticTitleTypes.containsKey(defaultAutomaticTitleType))
        {
            this.automaticTitleType = defaultAutomaticTitleType;
        }

        resetTitle(titleWorking);		
        resetEditor();
    }

    protected void resetTitle(RoamingTextTitle title)
    {
        title.setVisible(visible);
        title.setFont(textFont);
        title.setPaint(textPaint);
        title.setUseAutomaticTitle(useAutomaticTitle);
        title.setAutomaticTitleType(automaticTitleType);
    }

    protected void resetEditor()
    {
        boxTitleVisible.setSelected(visible);
        fieldFont.setDisplayFont(textFont);
        titlePaintSample.setPaint(textPaint);
        boxUseGradient.setSelected(useGradientPaint);
        boxUseAutomaticTitle.setSelected(useAutomaticTitle);

        comboAutomaticTitles.setSelectedItem(automaticTitleType);
        comboAutomaticTitles.setEnabled(useAutomaticTitle);
    }

    @Override
    public void saveAsDefaults() 
    {
        titleFrameSubeditor.saveAsDefaults();
        titlePositionSubeditor.saveAsDefaults();

        pref.putBoolean(TITLE_TEXT_AUTOMATIC, useAutomaticTitle);
        pref.put(TITLE_AUTOMATIC_TEXT_TYPE, automaticTitleType);

        try
        {
            SerializationUtilities.putSerializableObject(pref, TITLE_FONT, textFont);
            SerializationUtilities.putSerializableObject(pref, TITLE_PAINT,textPaint);
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }

        try 
        {
            pref.flush();
        } 
        catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    @Override
    public void applyChangesToAll() 
    {			
        titleFrameSubeditor.applyChangesToAll();
        titlePositionSubeditor.applyChangesToAll();

        for(CustomizableXYBaseChart<?> chart: boundedCharts)
        {
            RoamingTextTitle currentTitle = chart.getRoamingTitle();
            if(currentTitle == null && visible)
            {
                currentTitle = chart.buildNewTitle("");
                chart.setRoamingTitle(currentTitle);
            }
            if(currentTitle != null)
            {                
                resetTitle(currentTitle);
            }
        }
    }

    @Override
    public void undoChanges() 
    {
        titleFrameSubeditor.undoChanges();
        titlePositionSubeditor.undoChanges();

        setParametersToInitial();

        resetTitle(titleWorking);		
        resetEditor();

        chart.setRoamingTitle(titleOriginal);
    }

    @Override
    public Component getEditionComponent() 
    {
        return this;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        boolean enabled = boundedCharts.size() > 1;
        return enabled;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        Object source = evt.getSource();
        if(source == fieldTitle)
        {
            String valNew = evt.getNewValue().toString();
            titleWorking.setText(valNew);
        }		
    }

    @Override
    public Paint getPaint() 
    {
        return textPaint;
    }

    @Override
    public void setPaint(Paint paint) 
    {		
        if(paint != null)
        {		
            this.textPaint = paint;
            titlePaintSample.setPaint(paint);
            titleWorking.setPaint(paint);
        }		
    }

    private JPanel buildMainPanel()
    {
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout());

        SubPanel innerPanel = new SubPanel();

        innerPanel.addComponent(new JLabel("Show"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 0);
        innerPanel.addComponent(boxTitleVisible, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .1, 0);

        innerPanel.addComponent(new JLabel("Text"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 0);
        innerPanel.addComponent(fieldTitle, 1, 1, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);

        if(automaticTitlesSupported)
        {
            innerPanel.addComponent(new JLabel("Automatic text"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
            innerPanel.addComponent(boxUseAutomaticTitle, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);

            innerPanel.addComponent(comboAutomaticTitles, 2, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        }

        innerPanel.addComponent(new JLabel("Font"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        innerPanel.addComponent(fieldFont, 1, 3, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        innerPanel.addComponent(buttonSelectFont, 3, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.05, 1);

        innerPanel.addComponent(new JLabel("Use gradient"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        innerPanel.addComponent(boxUseGradient, 1, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);

        innerPanel.addComponent(new JLabel("Color"), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        innerPanel.addComponent(titlePaintSample, 1, 5, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        innerPanel.addComponent(buttonSelectPaint, 3, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.03, 1);

        outerPanel.add(innerPanel, BorderLayout.NORTH);      

        outerPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return outerPanel;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        BatchApplyAction batchApplyAction = new BatchApplyAction();
        JButton buttonBatchApplyAll = new JButton(batchApplyAction);
        batchApplyAction.setEnabled(isApplyToAllEnabled());

        JButton buttonClose = new JButton(new CloseAction());
        JButton buttonSave = new JButton(new SaveAsDefaultsAction());
        JButton buttonReset = new JButton(new ResetToDefaultsAction());
        JButton buttonUndo = new JButton(new UndoAction());

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonBatchApplyAll).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSave).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonReset).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonClose).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonUndo));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonBatchApplyAll)
                .addComponent(buttonSave)
                .addComponent(buttonReset)
                .addComponent(buttonClose)
                .addComponent(buttonUndo));

        layout.linkSize(buttonClose,buttonBatchApplyAll, buttonSave, buttonReset, buttonUndo);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return buttonPanel;
    }

    @Override
    public String getSubeditorName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setNameBorder(boolean b) {
        // TODO Auto-generated method stub

    }


    private class BatchApplyAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public BatchApplyAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(NAME, "Batch apply");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            applyChangesToAll();
            JOptionPane.showMessageDialog(RoamingTitleSubeditor.this, "Title style was applied to all charts of the same type", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class UndoAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public UndoAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(NAME, "Undo");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            undoChanges();
        }
    }

    private class SaveAsDefaultsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public SaveAsDefaultsAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME, "Use as defaults");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            saveAsDefaults();
            JOptionPane.showMessageDialog(RoamingTitleSubeditor.this, "Default title style was changed", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class ResetToDefaultsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ResetToDefaultsAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME, "Reset to defaults");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            resetToDefaults();
        }
    }

    private class CloseAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public CloseAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
            putValue(NAME, "Close");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            hideRoot();
        }
    }
}
