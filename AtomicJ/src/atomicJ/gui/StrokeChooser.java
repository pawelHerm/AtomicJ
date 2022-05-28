
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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.jfree.ui.PaintSample;

import atomicJ.utilities.ArrayUtilities;


public class StrokeChooser implements PaintReceiver
{
    private BasicStroke initStroke;
    private Paint initStrokePaint;
    private boolean initUseGradientPaint;

    private float initLineWidth;
    private float[] initDashArray;
    private int initEndCap;
    private int initLineJoin;
    private float initMiterLimit;
    private boolean initIsToBeDashed;
    private int initGapCount;
    private float initPatternLength;

    private BasicStroke stroke;
    private Paint strokePaint;
    private boolean useGradientPaint;

    private float lineWidth;
    private float[] dashArray;
    private int endCap;
    private int lineJoin;
    private float miterLimit;
    private boolean isToBeDashed;
    private int gapCount;
    private float patternLength;

    private final JToggleButton buttonCapRound;
    private final JToggleButton buttonCapSquare;
    private final JToggleButton buttonCapButt;

    private final ButtonGroup groupCaps = new ButtonGroup();

    private final JToggleButton buttonJoinRound;
    private final JToggleButton buttonJoinMiter;
    private final JToggleButton buttonJoinBevel;

    private final ButtonGroup groupJoins = new ButtonGroup();

    private final JSpinner spinnerLineWidth;
    private final JSpinner spinnerMiterLimit;
    private final JSpinner spinnerPatternLength;
    private final JSpinner spinnerGapCount;

    private final JLabel labelPatternLength = new JLabel("Dash length");
    private final JLabel labelDashPattern = new JLabel("Dash pattern   ");

    private final JCheckBox boxDashed = new JCheckBox();
    private final JCheckBox boxUseGradient = new JCheckBox();
    private final JButton buttonSelectPaint = new JButton(new SelectPaintAction());
    private final PaintSample paintSample;


    private final BrokenStrokeSample strokeSample = new BrokenStrokeSample();
    private final SegmentedPaintSample dashingSample = new SegmentedPaintSample();

    private final BasicStrokeReceiver strokeReceiver;
    private final JDialog mainDialog;
    private final SkewedGradientEditionDialog gradientDialog;

    public StrokeChooser(Window parent, BasicStrokeReceiver strokeReceiver)
    {
        mainDialog = new JDialog(parent, "Specify stroke style", ModalityType.MODELESS);
        gradientDialog = new SkewedGradientEditionDialog(mainDialog);

        this.strokeReceiver = strokeReceiver;		

        this.spinnerLineWidth = new JSpinner(new SpinnerNumberModel(Float.valueOf(0.f), Float.valueOf(0.f),Float.valueOf(1000f), Float.valueOf(0.5f))); 		
        this.spinnerMiterLimit = new JSpinner(new SpinnerNumberModel(Float.valueOf(1f), Float.valueOf(1f), Float.valueOf(1000f), Float.valueOf(1.f)));
        this.spinnerGapCount = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        this.spinnerPatternLength = new JSpinner(new SpinnerNumberModel(Float.valueOf(1f), Float.valueOf(1f),Float.valueOf(1000f), Float.valueOf(1.f)));

        spinnerGapCount.setEditor(dashingSample);
        spinnerGapCount.setBorder(BorderFactory.createEmptyBorder());

        this.buttonCapRound = new JToggleButton(new CapRoundAction());
        this.buttonCapSquare = new JToggleButton(new CapSquareAction());
        this.buttonCapButt = new JToggleButton(new CapButtAction());
        this.buttonJoinRound = new JToggleButton(new JoinRoundAction());
        this.buttonJoinMiter = new JToggleButton(new JoinMiterAction());
        this.buttonJoinBevel = new JToggleButton(new JoinBevelAction());

        buttonCapRound.setMargin(new Insets(0,0,0,0));
        buttonCapSquare.setMargin(new Insets(0,0,0,0));
        buttonCapButt.setMargin(new Insets(0,0,0,0));
        buttonJoinRound.setMargin(new Insets(0,0,0,0));
        buttonJoinMiter.setMargin(new Insets(0,0,0,0));
        buttonJoinBevel.setMargin(new Insets(0,0,0,0));

        groupCaps.add(buttonCapRound);
        groupCaps.add(buttonCapSquare);
        groupCaps.add(buttonCapButt);

        groupJoins.add(buttonJoinRound);
        groupJoins.add(buttonJoinMiter);
        groupJoins.add(buttonJoinBevel);

        this.paintSample = new PaintSample(Color.black);

        JPanel mainPanel = buildMainPanel();              
        JPanel buttonPanel = buildButtonPanel();

        mainDialog.add(mainPanel, BorderLayout.CENTER);
        mainDialog.add(buttonPanel, BorderLayout.SOUTH);

        pullInitialParameters();
        resetEditor();

        initChangeListeners();
        initItemListeners();

        mainDialog.pack();
        mainDialog.setLocationRelativeTo(parent);
    }


    public void showDialog()
    {				
        pullInitialParameters();

        setWorkingParametersToInitial();
        resetEditor();

        mainDialog.setVisible(true);
    }

    private void pullInitialParameters()
    {
        this.initStroke = strokeReceiver.getStroke();
        this.initStrokePaint = strokeReceiver.getStrokePaint();
        this.initUseGradientPaint = initStrokePaint instanceof GradientPaint;

        if(initStroke!= null)
        {
            this.initLineWidth = initStroke.getLineWidth();
            this.initDashArray = initStroke.getDashArray();
            this.initEndCap = initStroke.getEndCap();
            this.initLineJoin = initStroke.getLineJoin();
            this.initMiterLimit = Math.max(1f, initStroke.getMiterLimit());
            this.initIsToBeDashed = (initDashArray != null);
            this.initGapCount =  initIsToBeDashed ? initDashArray.length/2 :0;
            this.initPatternLength = initIsToBeDashed ? Math.max(1.f,ArrayUtilities.total(dashArray)) : 1.f;
        }
        else
        {
            this.initMiterLimit = 1.f;
            this.initPatternLength = 1.f;
        }
    }

    private void setWorkingParametersToInitial()
    {
        this.stroke = initStroke;
        this.strokePaint = initStrokePaint;
        this.useGradientPaint = initUseGradientPaint;

        this.lineWidth = initLineWidth;
        this.dashArray = initDashArray;
        this.endCap = initEndCap;
        this.lineJoin = initLineJoin;
        this.miterLimit = initMiterLimit;
        this.isToBeDashed = initIsToBeDashed;
        this.gapCount = initGapCount;
        this.patternLength = initPatternLength;
    }	

    private void resetEditor()
    {		
        spinnerLineWidth.setValue(lineWidth); 		
        spinnerMiterLimit.setValue(miterLimit);
        spinnerGapCount.setValue(gapCount);
        spinnerPatternLength.setValue(patternLength);

        paintSample.setPaint(strokePaint);
        strokeSample.setStroke(stroke);
        strokeSample.setStrokePaint(strokePaint);
        dashingSample.setSolidPartPaint(strokePaint);

        boxDashed.setSelected(isToBeDashed);
        boxUseGradient.setSelected(useGradientPaint);

        groupCaps.clearSelection();
        if(endCap == BasicStroke.CAP_ROUND)
        {
            groupCaps.setSelected(buttonCapRound.getModel(), true);
        }
        else if(endCap == BasicStroke.CAP_SQUARE)
        {
            groupCaps.setSelected(buttonCapSquare.getModel(), true);			
        }
        else if(endCap == BasicStroke.CAP_BUTT)
        {
            groupCaps.setSelected(buttonCapButt.getModel(), true);			
        }

        groupJoins.clearSelection();

        if(lineJoin == BasicStroke.JOIN_ROUND)
        {
            groupJoins.setSelected(buttonJoinRound.getModel(), true);
        }
        else if(lineJoin == BasicStroke.JOIN_MITER)
        {
            groupJoins.setSelected(buttonJoinMiter.getModel(), true);			
        }
        else if(lineJoin == BasicStroke.JOIN_BEVEL)
        {
            groupJoins.setSelected(buttonJoinBevel.getModel(), true);

        }

        updateDashingSample();
        updateIfTheStrokeIsToBeDashed();
    }

    private void initItemListeners()
    {
        boxDashed.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {                
                boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
                isToBeDashed = selected;
                dashArray = isToBeDashed ? dashingSample.getPattern(patternLength): null;
                updateStroke();
                updateIfTheStrokeIsToBeDashed();                
            }
        });
        boxUseGradient.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
                useGradientPaint = selected;
            }
        });
    }

    private void initChangeListeners()
    {
        spinnerMiterLimit.addChangeListener(new ChangeListener() {            
            @Override
            public void stateChanged(ChangeEvent e) {
                float miterLimitNew = ((SpinnerNumberModel)spinnerMiterLimit.getModel()).getNumber().floatValue();
                setNewMiterLimit(miterLimitNew);                
            }
        });
        spinnerLineWidth.addChangeListener(new ChangeListener() {            
            @Override
            public void stateChanged(ChangeEvent e) {
                float lineWidthNew = ((SpinnerNumberModel)spinnerLineWidth.getModel()).getNumber().floatValue();
                setNewLineWidth(lineWidthNew);                  
            }
        });
        spinnerGapCount.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                int gapCountNew = ((SpinnerNumberModel)spinnerGapCount.getModel()).getNumber().intValue();
                setNewGapCount(gapCountNew);                
            }
        });
        spinnerPatternLength.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                float patternLengthNew = ((SpinnerNumberModel)spinnerPatternLength.getModel()).getNumber().floatValue();            
                setNewPatternLength(patternLengthNew);                 
            }
        });
        dashingSample.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                float[] dashingArrayNew = dashingSample.getPattern(patternLength);
                setNewDashArray(dashingArrayNew);                     
            }
        });
    }

    private void updateDashingSample()
    {
        if(dashArray != null)
        {
            int patternSize = dashArray.length;						
            List<Float> knobLocations = new ArrayList<>();			
            float currentPatternLength = 0.f;

            for(int i = 0; i<patternSize - 1; i++)
            {
                float node = dashArray[i];

                currentPatternLength += node;
                knobLocations.add(currentPatternLength/patternLength);
            }			

            dashingSample.setKnobs(knobLocations);
        }		
        else
        {
            dashingSample.removeKnobs();
        }
    }	

    private void updateIfTheStrokeIsToBeDashed()
    {
        dashingSample.setEnabled(isToBeDashed);
        spinnerGapCount.setEnabled(isToBeDashed);
        spinnerPatternLength.setEnabled(isToBeDashed);
        labelDashPattern.setEnabled(isToBeDashed);
        labelPatternLength.setEnabled(isToBeDashed);
    }

    private void updateStroke()
    {
        stroke = new BasicStroke(lineWidth, endCap, lineJoin, miterLimit, dashArray, 0);
        strokeSample.setStroke(stroke);
        strokeSample.repaint();

        strokeReceiver.setStroke(stroke);
    }

    private void updateStrokePaint(Paint paint)
    {
        strokePaint = paint;
        paintSample.setPaint(paint);
        strokeSample.setStrokePaint(paint);
        dashingSample.setSolidPartPaint(paint);

        strokeReceiver.setStrokePaint(paint);
    }

    private void cancel()
    {
        if(!Objects.equals(initStroke, stroke))
        {
            strokeReceiver.setStroke(initStroke);
        }	
        if(!Objects.equals(initStrokePaint, strokePaint))
        {
            strokeReceiver.setStrokePaint(initStrokePaint);
        }	
        mainDialog.setVisible(false);
    }

    private void setNewDashArray(float[] dashArrayNew)
    {
        if(!dashArray.equals(dashArrayNew))
        {
            this.dashArray = dashArrayNew;
            updateStroke();
        }
    }

    private void setNewLineWidth(float lineWidthNew)
    {
        if(lineWidthNew != lineWidth)
        {
            this.lineWidth = lineWidthNew;
            updateStroke();
            updateSize();
        }
    }

    private void setNewMiterLimit(float miterLimitNew)
    {
        if(miterLimitNew != miterLimit)
        {
            this.miterLimit = miterLimitNew;
            updateStroke();
        }
    }

    private void setNewGapCount(int gapCountNew)
    {
        if(gapCountNew != gapCount)
        {
            try
            {
                this.gapCount = gapCountNew;

                if(gapCount > 0)
                {

                    int patternSize = gapCount*2;
                    dashArray = new float[patternSize];
                    float segementLength = patternLength/patternSize;
                    Arrays.fill(dashArray, segementLength);
                }
                else
                {
                    dashArray = null;
                }

                updateDashingSample();
                updateStroke();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void setNewPatternLength(float patternLengthNew)
    {
        if(patternLength != patternLengthNew)
        {
            float patternLengthOld = patternLength;
            float ratio = patternLengthNew/patternLengthOld;

            int patternSize = dashArray.length;
            float[] dashArrayNew = new float[patternSize];

            for(int i = 0; i<patternSize; i++)
            {
                float oldNode = dashArray[i];
                dashArrayNew[i] = oldNode*ratio;
            }

            this.patternLength = patternLengthNew;
            this.dashArray = dashArrayNew;
            updateStroke();
        }		
    }	

    @Override
    public Paint getPaint() 
    {
        return strokePaint;
    }

    @Override
    public void setPaint(Paint paint) 
    {
        updateStrokePaint(paint);
    }

    private void updateSize()
    {
        Dimension preferredSize = mainDialog.getPreferredSize();
        Dimension actualSize = mainDialog.getSize();

        int newWidth = (int)Math.max(preferredSize.getWidth(), actualSize.getWidth());
        int newHeight = (int)Math.max(preferredSize.getHeight(), actualSize.getHeight());

        Dimension newSize = new Dimension(newWidth, newHeight);
        mainDialog.setSize(newSize);
    }

    private JPanel buildMainPanel()
    {
        SubPanel mainPanel = new SubPanel();

        JPanel panelCaps = new JPanel(new GridLayout(1, 0, 4, 4));
        panelCaps.add(buttonCapRound);
        panelCaps.add(buttonCapSquare);
        panelCaps.add(buttonCapButt);

        JPanel panelJoins = new JPanel(new GridLayout(1, 0, 4, 4));
        panelJoins.add(buttonJoinRound);
        panelJoins.add(buttonJoinMiter);
        panelJoins.add(buttonJoinBevel);

        SubPanel panelColor = new SubPanel();
        panelColor.addComponent(paintSample, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);
        panelColor.addComponent(buttonSelectPaint, 1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0);		


        mainPanel.addComponent(new JLabel("Width"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(spinnerLineWidth, 1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);				

        mainPanel.addComponent(Box.createVerticalStrut(10), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(Box.createVerticalStrut(10), 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		

        mainPanel.addComponent(new JLabel("Caps"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(panelCaps, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        mainPanel.addComponent(new JLabel("Joins"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(panelJoins, 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        mainPanel.addComponent(new JLabel("Miter length"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(spinnerMiterLimit, 1, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);				

        mainPanel.addComponent(Box.createVerticalStrut(10), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(Box.createVerticalStrut(10), 1, 5, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		

        mainPanel.addComponent(new JLabel("Dashed"), 0, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(boxDashed, 1, 6, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		

        mainPanel.addComponent(labelPatternLength, 0, 7, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(spinnerPatternLength, 1, 7, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		

        mainPanel.addComponent(labelDashPattern, 0, 8, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(spinnerGapCount, 1, 8, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);				

        mainPanel.addComponent(Box.createVerticalStrut(10), 0, 9, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(Box.createVerticalStrut(10), 1, 9, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);

        mainPanel.addComponent(new JLabel("Use gradient"), 0, 10, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(boxUseGradient, 1, 10, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		

        mainPanel.addComponent(new JLabel("Color"), 0, 11, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(panelColor, 1, 11, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		

        mainPanel.addComponent(Box.createVerticalStrut(10), 0, 12, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(Box.createVerticalStrut(10), 1, 12, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);

        mainPanel.addComponent(new JLabel("Preview"), 0, 13, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        mainPanel.addComponent(strokeSample, 1, 13, 1, 1, GridBagConstraints.EAST, GridBagConstraints.BOTH, 1, 1);		


        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        return mainPanel;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        JButton buttonOK = new JButton();
        buttonOK.setAction(new OKAction());

        JButton buttonCancel = new JButton();
        buttonCancel.setAction(new CancelAction());

        JPanel buttonGroupResults = new JPanel(new GridLayout(1, 0, 5, 5));

        buttonGroupResults.add(buttonOK);
        buttonGroupResults.add(buttonCancel);

        buttonPanel.add(buttonGroupResults);
        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        return buttonPanel;
    }


    private class CapRoundAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CapRoundAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/capRoundSmall.png"));

            putValue(LARGE_ICON_KEY, icon);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            StrokeChooser.this.endCap = BasicStroke.CAP_ROUND;
            updateStroke();
        }
    }

    private class CapSquareAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CapSquareAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/capSquareSmall.png"));

            putValue(LARGE_ICON_KEY, icon);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            StrokeChooser.this.endCap = BasicStroke.CAP_SQUARE;
            updateStroke();
        }
    }

    private class CapButtAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CapButtAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/capButtSmall.png"));

            putValue(LARGE_ICON_KEY, icon);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            StrokeChooser.this.endCap = BasicStroke.CAP_BUTT;
            updateStroke();
        }
    }

    private class JoinRoundAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public JoinRoundAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/joinRoundSmall.png"));

            putValue(LARGE_ICON_KEY, icon);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            StrokeChooser.this.lineJoin = BasicStroke.JOIN_ROUND;
            updateStroke();
        }
    }
    private class JoinMiterAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public JoinMiterAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/joinMiterSmall.png"));

            putValue(LARGE_ICON_KEY, icon);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            StrokeChooser.this.lineJoin = BasicStroke.JOIN_MITER;
            updateStroke();
        }
    }

    private class JoinBevelAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public JoinBevelAction()
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/joinBevelSmall.png"));

            putValue(LARGE_ICON_KEY, icon);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            StrokeChooser.this.lineJoin = BasicStroke.JOIN_BEVEL;
            updateStroke();
        }
    }

    private class SelectPaintAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SelectPaintAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME,"Select");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(useGradientPaint)
            {
                gradientDialog.showDialog(StrokeChooser.this);
            }
            else
            {
                Color initialColor = strokePaint instanceof Color ? (Color)strokePaint : Color.blue;
                Paint paint = JColorChooser.showDialog(mainDialog, "Stroke color", initialColor);
                if (paint != null) 
                {
                    updateStrokePaint(paint);
                }
            }
            updateStroke();
        }
    }

    private class OKAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public OKAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(NAME,"OK");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            mainDialog.setVisible(false);
        };
    }

    private class CancelAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CancelAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME,"Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            cancel();
        };
    }
}
