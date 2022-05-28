
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.AtomicJ;
import atomicJ.gui.CustomizableChart;
import atomicJ.gui.PaintReceiver;
import atomicJ.gui.PaintSampleFlexible;
import atomicJ.gui.PreferredChartStyle;
import atomicJ.gui.SkewedGradientEditionDialog;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.Validation;

public class ChartSubeditor implements Subeditor, PaintReceiver
{
    private final PreferredChartStyle prefStyle;

    private final ChartStyleDataImmutable initStyle;
    private final ChartStyleDataMutable styleModel;

    private final JCheckBox boxAntialias = new JCheckBox();
    private final JCheckBox boxLockAspectRatio = new JCheckBox();
    private final JCheckBox boxUseGradient = new JCheckBox();
    private final PaintSampleFlexible backgroundPaintSample = new PaintSampleFlexible();
    private final JSpinner spinnerPaddingTop = new JSpinner(new SpinnerNumberModel(1.,0,1000000,0.2));           
    private final JSpinner spinnerPaddingBottom = new JSpinner(new SpinnerNumberModel(1.,0,1000000,0.2));                     
    private final JSpinner spinnerPaddingLeft = new JSpinner(new SpinnerNumberModel(1.,0,1000000,0.2));       
    private final JSpinner spinnerPaddingRight = new JSpinner(new SpinnerNumberModel(1.,0,1000000,0.2));


    private SkewedGradientEditionDialog gradientDialog;

    private final CustomizableChart chart;
    private final List<? extends CustomizableChart> boundedCharts;

    private final JPanel editorPanel = new JPanel(new BorderLayout());

    public ChartSubeditor(List<? extends CustomizableChart> boundedCharts, CustomizableChart chart)
    {
        this.boundedCharts = Validation.requireNonNullParameterName(boundedCharts, "boundedCharts");
        this.chart = Validation.requireNonNullParameterName(chart, "chart");

        this.prefStyle = chart.getPreferredChartStyle();

        this.initStyle = chart.getStyleData();
        this.styleModel = initStyle.getMutableCopy();

        editorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        resetEditor();

        JPanel mainPanel = buildMainPanel();       
        editorPanel.add(mainPanel, BorderLayout.NORTH);

        JPanel buttonPanel = buildButtonPanel();
        editorPanel.add(buttonPanel, BorderLayout.SOUTH);

        initItemListener();
        initChangeListener();
    }

    @Override
    public void applyChangesToAll() 
    {
        ChartStyleDataImmutable style = styleModel.getImmutableVersion();
        for(CustomizableChart ch: boundedCharts)
        {
            ch.setStyleData(style);
        }		
    }

    @Override
    public void undoChanges() 
    {
        styleModel.copyState(initStyle);
        chart.setStyleData(initStyle);
        resetEditor();
    }

    @Override
    public void resetToDefaults() 
    {			
        styleModel.readPreferredStyle(prefStyle);
        chart.setStyleData(styleModel.getImmutableVersion());
        resetEditor();
    }

    @Override
    public void saveAsDefaults() 
    {
        prefStyle.saveToPreferences(styleModel);
    }

    private void resetEditor()
    {
        backgroundPaintSample.setPaint(styleModel.getBackgroundPaint());
        boxLockAspectRatio.setSelected(styleModel.isLockAspectRatio());
        boxUseGradient.setSelected(styleModel.isUseGradientPaint());
        boxAntialias.setSelected(styleModel.isAntialias());
        spinnerPaddingTop.setValue(styleModel.getPaddingTop());
        spinnerPaddingBottom.setValue(styleModel.getPaddingBottom());
        spinnerPaddingLeft.setValue(styleModel.getPaddingLeft());
        spinnerPaddingRight.setValue(styleModel.getPaddingRight());
    }

    @Override
    public Component getEditionComponent()
    {
        return editorPanel;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return boundedCharts.size() > 1;
    }

    private void hideRoot()
    {
        Component root = SwingUtilities.getRoot(editorPanel);
        root.setVisible(false);
    }

    private void attemptModifyBackgroundPaint() 
    {    	
        if(styleModel.isUseGradientPaint())
        {
            if(gradientDialog == null)
            {
                gradientDialog = new SkewedGradientEditionDialog(SwingUtilities.getWindowAncestor(editorPanel));
            }
            gradientDialog.showDialog(this);
        }
        else
        {
            Paint backgroundPaintNew = JColorChooser.showDialog(editorPanel, "Chart background color", Color.blue);	        
            if (backgroundPaintNew != null) 
            {
                styleModel.setBackgroundPaint(backgroundPaintNew);
                backgroundPaintSample.setPaint(backgroundPaintNew);
                chart.setBackgroundPaint(backgroundPaintNew);			
            }
        }    	
    }

    private void initItemListener()
    {
        boxAntialias.addItemListener(new ItemListener() 
        {           
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
                styleModel.setAntialias(selected);
                chart.setAntiAlias(selected);
            }
        });
        boxLockAspectRatio.addItemListener(new ItemListener() 
        {
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
                styleModel.setLockAspectRatio(selected);
                chart.setUseFixedChartAreaSize(selected);
            }
        });
        boxUseGradient.addItemListener(new ItemListener() {          
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
                styleModel.setUseGradientPaint(selected);
            }
        });
    }

    private void initChangeListener()
    {
        spinnerPaddingTop.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e) {
                double paddingTop = ((SpinnerNumberModel)spinnerPaddingTop.getModel()).getNumber().doubleValue();
                styleModel.setPaddingTop(paddingTop);
                chart.setTopPadding(paddingTop);  
            }
        });  
        spinnerPaddingBottom.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                double paddingBottom = ((SpinnerNumberModel)spinnerPaddingBottom.getModel()).getNumber().doubleValue();
                styleModel.setPaddingBottom(paddingBottom);
                chart.setBottomPadding(paddingBottom); 
            }
        });                      
        spinnerPaddingLeft.addChangeListener(new ChangeListener() 
        {           
            @Override
            public void stateChanged(ChangeEvent e)
            {
                double paddingLeft = ((SpinnerNumberModel)spinnerPaddingLeft.getModel()).getNumber().doubleValue();
                styleModel.setPaddingLeft(paddingLeft);
                chart.setLeftPadding(paddingLeft);
            }
        });        
        spinnerPaddingRight.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                double paddingRight = ((SpinnerNumberModel)spinnerPaddingRight.getModel()).getNumber().doubleValue();
                styleModel.setPaddingRight(paddingRight);
                chart.setRightPadding(paddingRight);  
            }
        });  
    }


    @Override
    public Paint getPaint() 
    {
        return styleModel.getBackgroundPaint();
    }

    @Override
    public void setPaint(Paint paint) 
    {		
        if(paint != null)
        {		
            styleModel.setBackgroundPaint(paint);
            backgroundPaintSample.setPaint(paint);
            chart.setBackgroundPaint(paint);						
        }		
    }

    private JPanel buildMainPanel()
    {
        SubPanel mainPanel = new SubPanel();

        JButton buttonSelectBackgroundPaint = new JButton(new SelectBackgroundPaintAction());

        mainPanel.addComponent(new JLabel("Draw anti-aliased"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(boxAntialias, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        mainPanel.addComponent(new JLabel("Lock aspect ratio"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(boxLockAspectRatio, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        mainPanel.addComponent(new JLabel("Padding"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(spinnerPaddingTop, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);        
        mainPanel.addComponent(new JLabel("from above"), 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(spinnerPaddingBottom, 3, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(new JLabel("from below"), 4, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);

        mainPanel.addComponent(spinnerPaddingLeft, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);        
        mainPanel.addComponent(new JLabel("from left"), 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(spinnerPaddingRight, 3, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(new JLabel("from right"), 4, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);       

        mainPanel.addComponent(new JLabel("Use gradient"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(boxUseGradient, 1, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        mainPanel.addComponent(new JLabel("Background color"), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(backgroundPaintSample, 1, 5, 4, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(buttonSelectBackgroundPaint, 5, 5, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, .05, 1);    

        mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5),
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Chart style"),BorderFactory.createEmptyBorder(8, 6, 6, 8))));

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

    private class SelectBackgroundPaintAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SelectBackgroundPaintAction()
        {
            putValue(NAME, "Select");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            attemptModifyBackgroundPaint();            
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
            JOptionPane.showMessageDialog(editorPanel, "Chart style was applied to all charts of the same type", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(editorPanel, "Default chart style was changed", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
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

    @Override
    public String getSubeditorName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setNameBorder(boolean b) {
        // TODO Auto-generated method stub

    }
}
