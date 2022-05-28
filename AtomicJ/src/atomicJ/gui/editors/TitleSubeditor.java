
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;


import org.jfree.chart.title.TextTitle;
import org.jfree.ui.*;

import atomicJ.gui.AtomicJ;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.CustomizableXYPlot;
import atomicJ.gui.FontDisplayField;
import atomicJ.gui.FontReceiver;
import atomicJ.gui.GradientPaint;
import atomicJ.gui.PaintReceiver;
import atomicJ.gui.SkewedGradientEditionDialog;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;


public class TitleSubeditor implements PaintReceiver, Subeditor
{
    private final Preferences pref;

    private final boolean initVisible;
    private final boolean initUseGradientPaint;
    private final Font initFont;
    private final Paint initPaint;

    private boolean visible;
    private boolean useGradientPaint;
    private Font textFont;
    private Paint textPaint;

    private final JCheckBox boxTitleVisible = new JCheckBox();
    private final JCheckBox boxUseGradient = new JCheckBox();
    private final JFormattedTextField fieldTitle = new JFormattedTextField(new DefaultFormatter());
    private final FontDisplayField fieldFont;
    private final PaintSample titlePaintSample;

    private final Action selectFontAction = new SelectFontAction();
    private final Action selectPaintAction = new SelectPaintAction();

    private SkewedGradientEditionDialog gradientDialog;

    private final TextTitle titleOriginal;
    private final TextTitle titleWorking;

    private final ChannelChart<? extends CustomizableXYPlot> chart;
    private final List<? extends ChannelChart<? extends CustomizableXYPlot>> boundedCharts;

    private FontChooserDialog fontChooserDialog;
    private final JPanel editorPanel = new JPanel(new BorderLayout());

    public TitleSubeditor(List<? extends ChannelChart<? extends CustomizableXYPlot>> boundedCharts, ChannelChart<? extends CustomizableXYPlot> chart) 
    {
        this.chart = chart;
        this.titleOriginal = chart.getTitle();
        this.boundedCharts = boundedCharts;
        this.pref = chart.getPreferences();

        if(titleOriginal == null)
        {
            this.titleWorking = new TextTitle("Title");
            titleWorking.setVisible(false);
            Font titleFont = (Font)SerializationUtilities.getSerializableObject(pref, TITLE_FONT, new Font("Dialog", Font.PLAIN, 14));
            Paint titlePaint = (Paint)SerializationUtilities.getSerializableObject(pref, TITLE_PAINT, Color.black);

            titleWorking.setFont(titleFont);
            titleWorking.setPaint(titlePaint);
            chart.setTitle(titleWorking);
        }
        else
        {
            this.titleWorking = titleOriginal;
        }

        this.initVisible = titleWorking.isVisible();
        this.initFont = titleWorking.getFont();
        this.initPaint = titleWorking.getPaint();
        this.initUseGradientPaint = initPaint instanceof GradientPaint;

        setParametersToInitial();

        this.fieldFont = new FontDisplayField(initFont);

        boxTitleVisible.setSelected(initVisible);

        fieldTitle.setValue(titleWorking.getText());

        DefaultFormatter formatter = (DefaultFormatter)fieldTitle.getFormatter();
        formatter.setOverwriteMode(false);
        formatter.setCommitsOnValidEdit(true);

        this.titlePaintSample = new PaintSample(initPaint);

        JPanel mainPanel = buildMainPanel();
        JPanel buttonPanel = buildButtonPanel();
        editorPanel.add(mainPanel, BorderLayout.NORTH);
        editorPanel.add(buttonPanel, BorderLayout.SOUTH);

        initPropertyChangeListener();
        initItemListener();
    }

    private void setParametersToInitial()
    {
        this.visible = initVisible;
        this.useGradientPaint = initUseGradientPaint;
        this.textFont = initFont;
        this.textPaint = initPaint;
    } 
    private void initPropertyChangeListener()
    {
        fieldTitle.addPropertyChangeListener("value", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String newVal = evt.getNewValue().toString();
                titleWorking.setText(newVal);                
            }
        });
    }

    private void initItemListener()
    {
        boxTitleVisible.addItemListener(new ItemListener() {            
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                visible = selected;
                if(chart.getTitle() != titleWorking)
                {
                    chart.setTitle(titleWorking);
                }
                titleWorking.setVisible(visible); 
            }
        });

        boxUseGradient.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                useGradientPaint = selected;
            }
        });
    }	

    private void hideRoot()       
    {
        Component root = SwingUtilities.getRoot(editorPanel);
        root.setVisible(false);
    }

    public void attemptFontSelection() 
    {
        if( fontChooserDialog == null)
        {
            this.fontChooserDialog = new FontChooserDialog(SwingUtilities.getWindowAncestor(editorPanel), "Font selection");
        }

        this.fontChooserDialog.showDialog(new FontReceiver() {

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
                gradientDialog = new SkewedGradientEditionDialog(SwingUtilities.getWindowAncestor(editorPanel));
            }
            gradientDialog.showDialog(this);
        }
        else
        {
            Paint textPaintNew = JColorChooser.showDialog(editorPanel, "Title text color", Color.blue);	        
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
        textFont = (Font)SerializationUtilities.getSerializableObject(pref, TITLE_FONT, new Font("Dialog", Font.PLAIN, 14));
        textPaint = (Paint)SerializationUtilities.getSerializableObject(pref, TITLE_PAINT, Color.black);
        useGradientPaint = textPaint instanceof GradientPaint;

        resetTitle(titleWorking);		
        resetEditor();
    }

    private void resetTitle(TextTitle t)
    {
        t.setVisible(visible);
        t.setFont(textFont);
        t.setPaint(textPaint);
    }

    private void resetEditor()
    {
        boxTitleVisible.setSelected(visible);
        fieldFont.setDisplayFont(textFont);
        titlePaintSample.setPaint(textPaint);
        boxUseGradient.setSelected(useGradientPaint);
    }

    @Override
    public void saveAsDefaults() 
    {
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
        for(ChannelChart<? extends CustomizableXYPlot> chart: boundedCharts)
        {
            TextTitle currentTitle = chart.getTitle();
            if(currentTitle != null)
            {
                resetTitle(currentTitle);
            }
        }
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();

        resetTitle(titleWorking);		
        resetEditor();

        chart.setTitle(titleOriginal);
    }

    @Override
    public Component getEditionComponent() 
    {
        return editorPanel;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        boolean enabled = boundedCharts.size() > 1;
        return enabled;
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
        SubPanel mainPanel = new SubPanel();

        mainPanel.addComponent(new JLabel("Show"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 0);
        mainPanel.addComponent(boxTitleVisible, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);

        mainPanel.addComponent(new JLabel("Text"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 0);
        mainPanel.addComponent(fieldTitle, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);

        mainPanel.addComponent(new JLabel("Font"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 0);
        mainPanel.addComponent(fieldFont, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);
        mainPanel.addComponent(new JButton(selectFontAction), 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.03, 0);

        mainPanel.addComponent(new JLabel("Use gradient"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(boxUseGradient, 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        mainPanel.addComponent(new JLabel("Color"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 0);
        mainPanel.addComponent(titlePaintSample, 1, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);
        mainPanel.addComponent(new JButton(selectPaintAction), 2, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.03, 1);

        mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5),
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Title style"),BorderFactory.createEmptyBorder(8, 6, 6, 8))));

        return mainPanel;
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

    private class SelectFontAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public SelectFontAction()
        {
            putValue(NAME, "Select");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            attemptFontSelection();
        }
    }

    private class SelectPaintAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public SelectPaintAction()
        {
            putValue(NAME, "Select");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            attemptPaintSelection();
        }
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
            JOptionPane.showMessageDialog(editorPanel, "Title style was applied to all charts of the same type", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(editorPanel, "Default title style was changed", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
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
