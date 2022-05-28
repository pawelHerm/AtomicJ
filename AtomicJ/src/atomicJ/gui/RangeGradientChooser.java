
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.data.QuantitativeSample;
import atomicJ.data.units.PrefixedUnit;



public class RangeGradientChooser extends JDialog implements ChangeListener, ActionListener, ColorGradientReceiver, PropertyChangeListener
{
    private static final long serialVersionUID = 1L;
    private static final double TOLERANCE = 1e-10;

    private final static String RANGE_LUT_COMMAND = "RANGE_LUT_COMMAND";
    private final static String UNDERFLOW_COLOR_COMMAND = "UNDERFLOW_COLOR_COMMAND";
    private final static String OVERFLOW_COLOR_COMMAND = "OVERFLOW_COLOR_COMMAND";
    private final static String MASK_COLOR_COMMAND = "MASK_COLOR_COMMAND";

    private final NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);

    private GradientMaskSelector initGradientMaskSelector;
    private GradientRangeSelector initGradientRangeSelector;
    private boolean initUseOutsideRangeColors;

    private double initLowerFullBound;
    private double initUpperFullBound;

    private double initLowerAutomaticBound;
    private double initUpperAutomaticBound;

    private double initLowerBound;
    private double initUpperBound;

    private PrefixedUnit initDataUnit;
    private PrefixedUnit initDisplayedUnit;

    private ColorGradient initRangeColorGradient;

    private Color initGradientUnderflowColor;
    private Color initGradientOverflowColor;

    private Color initMaskColor;

    private Color maskColor;

    private GradientMaskSelector gradientMaskSelector;
    private GradientRangeSelector gradientRangeSelector;
    private boolean useOutsideRangeColors;

    private double lowerGradientBound;
    private double upperGradientBound;

    private ColorGradient rangeColorGradient;

    private Color gradientUnderflowColor;
    private Color gradientOverflowColor;

    private final Action showHistogramsAction = new ShowHistogramAction();

    private final JRadioButton buttonFull = new JRadioButton("Full");
    private final JRadioButton buttonAutomatic = new JRadioButton("Automatic");
    private final JRadioButton buttonROILens = new JRadioButton("ROI full");

    private final ButtonGroup gradientSelectionGroup = new ButtonGroup();

    private final JCheckBox boxUseOutsideRangeColors = new JCheckBox("Use different colors");

    private final PaintSampleFlexible rangeColorGradientSample = new PaintSampleFlexible();	
    private final PaintSampleFlexible underflowColorSample = new PaintSampleFlexible();	
    private final PaintSampleFlexible overflowColorSample = new PaintSampleFlexible();
    private final PaintSampleFlexible maskColorSample = new PaintSampleFlexible();

    private final JButton buttonEditLUTTable = new JButton("Edit");	
    private final JButton buttonSelectUnderflowColor = new JButton("Select");	
    private final JButton buttonSelectOverflowColor = new JButton("Select");
    private final JButton buttonSelectMaskColor = new JButton("Select");

    private final JToggleButton buttonHistogram = new JToggleButton(showHistogramsAction);

    private final JCheckBox boxOutsideMask = new JCheckBox("Outside");
    private final JCheckBox boxInsideMask = new JCheckBox("Inside");

    private final JSpinner lowerBoundSpinner = new JSpinner();
    private final JSpinner upperBoundSpinner = new JSpinner();

    private final JLabel labelLowerBoundUnit = new JLabel();
    private final JLabel labelUpperBoundUnit = new JLabel();

    private final JLabel labelLowerFullBoundUnit = new JLabel();
    private final JLabel labelUpperFullBoundUnit = new JLabel();

    private final JLabel labelLowerFullBound = new JLabel();
    private final JLabel labelUpperFullBound = new JLabel();
    private final JLabel labelUnderflowColor = new JLabel("Underflow");
    private final JLabel labelOverflowColor = new JLabel("Overflow");
    private final JLabel labelMaskColor = new JLabel("Mask color");

    private GradientPaintReceiver receiver;

    private GradientEditionDialog gradientEditionDialog;
    private RangeHistogramView rangeHistogramView;

    private final boolean allowForROIs;


    public RangeGradientChooser(Window parent, GradientPaintReceiver receiver)
    {
        this(parent, receiver, true);
    }

    public RangeGradientChooser(Window parent, GradientPaintReceiver receiver, boolean allowForROIs)
    {
        super(parent, "Color gradient", ModalityType.MODELESS);		

        this.allowForROIs = allowForROIs;

        setReceiver(receiver);

        JPanel mainPanel = buildMainPanel();
        JPanel buttonPanel = buildButtonPanel();

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        initActionListener();
        initChangeListener();
        initItemListener();

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                cleanUp();
            }
        });

        pack();
        setLocationRelativeTo(parent);
    }

    private void resetEditor()
    {               
        //resets range selection type buttons
        boolean fullRange = GradientRangeSelector.FULL.equals(gradientRangeSelector);
        boolean automaticRange = GradientRangeSelector.AUTOMATIC.equals(gradientRangeSelector);
        boolean roiLensRange = GradientRangeSelector.ROI_FULL.equals(gradientRangeSelector);

        gradientSelectionGroup.clearSelection();
        buttonFull.setSelected(fullRange);
        buttonAutomatic.setSelected(automaticRange);
        buttonROILens.setSelected(roiLensRange);

        //resets current range
        double increment = Math.pow(10,Math.rint(Math.log10((upperGradientBound - lowerGradientBound)/100.)));

        SpinnerNumberModel lowerBoundModel = new SpinnerNumberModel(lowerGradientBound,-Integer.MAX_VALUE,Integer.MAX_VALUE,increment);         
        SpinnerNumberModel upperBoundModel = new SpinnerNumberModel(upperGradientBound,-Integer.MAX_VALUE,Integer.MAX_VALUE,increment);

        lowerBoundSpinner.setModel(lowerBoundModel);
        upperBoundSpinner.setModel(upperBoundModel); 

        //resets range minimum/maximum
        String labelFullMinimumText = numberFormat.format(initLowerFullBound);
        String labelFullMaximumText = numberFormat.format(initUpperFullBound);

        labelLowerFullBound.setText(labelFullMinimumText);
        labelUpperFullBound.setText(labelFullMaximumText);  

        //restes unit labels

        String unitName = initDisplayedUnit.getFullName();

        labelLowerBoundUnit.setText(unitName);
        labelUpperBoundUnit.setText(unitName);
        labelLowerFullBoundUnit.setText(unitName);
        labelUpperFullBoundUnit.setText(unitName);

        //resets range color samples
        boxUseOutsideRangeColors.setSelected(useOutsideRangeColors);

        rangeColorGradientSample.setPaint(new GradientPaint(rangeColorGradient));
        underflowColorSample.setPaint(gradientUnderflowColor);
        overflowColorSample.setPaint(gradientOverflowColor);

        //reset mask buttons
        boolean insideMask = GradientMaskSelector.MASK_INSIDE.equals(gradientMaskSelector);
        boolean outsideMask = GradientMaskSelector.MASK_OUTSIDE.equals(gradientMaskSelector);

        boxInsideMask.setSelected(insideMask);
        boxOutsideMask.setSelected(outsideMask);

        //resets mask color sample
        maskColorSample.setPaint(maskColor);

        setConsistentWithUseMasks();
        setConsistentWithUseOutsideColors();
        setHistogramConsistentWithReceiver();
    }

    private void setParametersToInitial()
    {
        this.gradientMaskSelector = initGradientMaskSelector;
        this.gradientRangeSelector = initGradientRangeSelector;	
        this.useOutsideRangeColors = initUseOutsideRangeColors;

        this.maskColor = initMaskColor;

        this.lowerGradientBound = initLowerBound;
        this.upperGradientBound = initUpperBound;	
        this.gradientUnderflowColor = initGradientUnderflowColor;
        this.gradientOverflowColor = initGradientOverflowColor;

        setInternalColorGradient(initRangeColorGradient);
    }

    private void pullReceiverParameters()
    {
        this.initDataUnit = receiver.getDataUnit();
        this.initDisplayedUnit = receiver.getDisplayedUnit();

        this.initGradientMaskSelector = receiver.getGradientMaskSelector();
        this.initGradientRangeSelector = receiver.getGradientRangeSelector();
        this.initUseOutsideRangeColors = receiver.getUseOutsideRangeColors();

        this.initLowerFullBound = convertValueInDataUnitToDisplayedUnits(receiver.getLowerFullBound());
        this.initUpperFullBound = convertValueInDataUnitToDisplayedUnits(receiver.getUpperFullBound());

        this.initLowerAutomaticBound = convertValueInDataUnitToDisplayedUnits(receiver.getLowerAutomaticBound());
        this.initUpperAutomaticBound = convertValueInDataUnitToDisplayedUnits(receiver.getUpperAutomaticBound());

        this.initLowerBound = convertValueInDataUnitToDisplayedUnits(receiver.getLowerBound());
        this.initUpperBound = convertValueInDataUnitToDisplayedUnits(receiver.getUpperBound());

        this.initRangeColorGradient = receiver.getColorGradient();

        this.initGradientUnderflowColor = receiver.getGradientUnderflowColor();
        this.initGradientOverflowColor = receiver.getGradientOverflowColor();



        this.initMaskColor = receiver.getMaskColor();

        setParametersToInitial();
    }

    private double convertValueInDisplayedUnitToDataUnits(double value)
    {
        double factor = initDisplayedUnit.getConversionFactorTo(initDataUnit);
        return factor*value;
    }

    private double convertValueInDataUnitToDisplayedUnits(double value)
    {
        double factor = initDataUnit.getConversionFactorTo(initDisplayedUnit);
        return factor*value;
    }

    public GradientPaintReceiver getReceiver()
    {
        return receiver;
    }

    public void setReceiver(GradientPaintReceiver receiver)
    {
        if(this.receiver != null)
        {
            this.receiver.removePropertyChangeListener(this);
        }
        this.receiver = receiver;
        this.receiver.addPropertyChangeListener(this);
        ensureConsistencyWithReceiver();

        pack();
    }

    public void showDialog(GradientPaintReceiver receiver)
    {
        if(this.receiver != null)
        {
            this.receiver.removePropertyChangeListener(this);
        }

        this.receiver = receiver;
        this.receiver.addPropertyChangeListener(this);
        ensureConsistencyWithReceiver();

        setVisible(true);
    }

    private void ensureCorrectSize()
    {

    }

    public void ensureConsistencyWithReceiver()
    {
        pullReceiverParameters();
        resetEditor();
    }

    public void cleanUp()
    {
        if(this.receiver != null)
        {
            this.receiver.removePropertyChangeListener(this);
        }

        this.receiver = null;

        if(rangeHistogramView != null)
        {
            rangeHistogramView.cleanUp();

            //it must be set to null, because after clearing it cannot be redisplayed, as it no longer contains the
            //resources. So I set it to null, and next time the user will want to open the range histogram dialog,
            //it will be built anew
            rangeHistogramView = null;
        }

        if(gradientEditionDialog != null)
        {
            gradientEditionDialog.cleanUp();
        }
    }

    private void initActionListener()
    {
        buttonEditLUTTable.setActionCommand(RANGE_LUT_COMMAND);		
        buttonSelectOverflowColor.setActionCommand(OVERFLOW_COLOR_COMMAND);
        buttonSelectUnderflowColor.setActionCommand(UNDERFLOW_COLOR_COMMAND);
        buttonSelectMaskColor.setActionCommand(MASK_COLOR_COMMAND);

        buttonEditLUTTable.addActionListener(this);
        buttonSelectUnderflowColor.addActionListener(this);
        buttonSelectOverflowColor.addActionListener(this);
        buttonSelectMaskColor.addActionListener(this);
    }

    private void initChangeListener()
    {
        lowerBoundSpinner.addChangeListener(this);
        upperBoundSpinner.addChangeListener(this);
    }

    private void initItemListener()
    {
        boxInsideMask.addItemListener(new ItemListener()
        {            
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                boolean otherSelected = boxOutsideMask.isSelected();

                if(selected)
                {
                    boxOutsideMask.setSelected(false);
                    gradientMaskSelector = GradientMaskSelector.MASK_INSIDE;
                    receiver.setGradientMaskSelector(gradientMaskSelector);
                }
                else if(!otherSelected)
                {
                    gradientMaskSelector = GradientMaskSelector.NO_MASK;
                    receiver.setGradientMaskSelector(gradientMaskSelector);
                }

                setConsistentWithUseMasks();
            }
        });
        boxOutsideMask.addItemListener(new ItemListener()
        {            
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);

                boolean otherSelected = boxInsideMask.isSelected();

                if(selected)
                {
                    boxInsideMask.setSelected(false);
                    gradientMaskSelector = GradientMaskSelector.MASK_OUTSIDE;
                    receiver.setGradientMaskSelector(gradientMaskSelector);
                }
                else if(!otherSelected)
                {
                    gradientMaskSelector = GradientMaskSelector.NO_MASK;
                    receiver.setGradientMaskSelector(gradientMaskSelector);
                }

                setConsistentWithUseMasks();
            }
        });
        buttonFull.addItemListener(new ItemListener()
        {            
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);

                if(selected)
                {
                    setGradientRangeSelector(GradientRangeSelector.FULL);       
                }
            }
        });
        buttonAutomatic.addItemListener(new ItemListener()
        {            
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                if(selected)
                {
                    setGradientRangeSelector(GradientRangeSelector.AUTOMATIC);      
                }
            }
        });
        buttonROILens.addItemListener(new ItemListener()
        {            
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                if(selected)
                {
                    setGradientRangeSelector(GradientRangeSelector.ROI_FULL);       
                }
            }
        });
        boxUseOutsideRangeColors.addItemListener(new ItemListener()
        {            
            @Override
            public void itemStateChanged(ItemEvent evt) 
            {
                boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
                useOutsideRangeColors = selected;
                receiver.setUseOutsideRangeColors(selected);
                setConsistentWithUseOutsideColors();     
            }
        });
    }

    private void resetReceiver()
    {
        if(!initMaskColor.equals(maskColor))
        {
            receiver.setMaskColor(maskColor);
        }
        if(!initGradientMaskSelector.equals(gradientMaskSelector))
        {
            receiver.setGradientMaskSelector(initGradientMaskSelector);
        }
        if(!initGradientRangeSelector.equals(gradientRangeSelector))
        {
            receiver.setGradientRangeSelector(initGradientRangeSelector);
        }
        if(!initRangeColorGradient.equals(rangeColorGradient))
        {
            receiver.setColorGradient(initRangeColorGradient);
        }
        if(!initGradientUnderflowColor.equals(gradientUnderflowColor))
        {
            receiver.setGradientUnderflowColor(initGradientUnderflowColor);
        }
        if(!initGradientOverflowColor.equals(gradientOverflowColor))
        {
            receiver.setGradientOverflowColor(initGradientOverflowColor);
        }
        if(Math.abs(initLowerBound - lowerGradientBound)>TOLERANCE)
        {
            receiver.setLowerBound(convertValueInDisplayedUnitToDataUnits(initLowerBound));
        }
        if(Math.abs(initUpperBound - upperGradientBound)>TOLERANCE)
        {
            receiver.setUpperBound(convertValueInDisplayedUnitToDataUnits(initUpperBound));
        }
        if(useOutsideRangeColors != initUseOutsideRangeColors)
        {
            receiver.setUseOutsideRangeColors(initUseOutsideRangeColors);
        }
        if(gradientRangeSelector != initGradientRangeSelector)
        {
            receiver.setGradientRangeSelector(initGradientRangeSelector);
        }
    }

    private void resetHistogramAcessibility()
    {
        QuantitativeSample sample = receiver.getPaintedSample();

        boolean hisogramRangeSelectionEnabled = (sample != null);
        buttonHistogram.setEnabled(hisogramRangeSelectionEnabled);
    }

    private void setHistogramConsistentWithReceiver()
    {
        if(rangeHistogramView != null)
        {
            rangeHistogramView.setRangeModel(receiver);
            updateHistogramSample();
        }
    }

    public void updateHistogramSample()
    {
        if(rangeHistogramView != null && rangeHistogramView.isVisible())
        {
            QuantitativeSample sample = receiver.getPaintedSample();

            boolean hisogramRangeSelectionEnabled = (sample != null);
            buttonHistogram.setEnabled(hisogramRangeSelectionEnabled);

            if(hisogramRangeSelectionEnabled)
            {
                rangeHistogramView.setNewSample(sample);
            }
            else
            {
                rangeHistogramView.setVisible(false);
            }
        }
    }


    private void setHistogramVisible(boolean visible)
    {
        if(visible)
        {
            showHistogram();
        }
        else
        {
            hideHistogram();
        }
    }

    private void showHistogram()
    {	
        QuantitativeSample sample = receiver.getPaintedSample();

        if(rangeHistogramView == null)
        {
            rangeHistogramView = new RangeHistogramView(this, sample, receiver, "Gradient range");
            rangeHistogramView.addDataViewListener(new BasicViewListener(showHistogramsAction));         
        }
        else
        {
            rangeHistogramView.setNewSample(sample);
        }

        showHistogramsAction.putValue(Action.SELECTED_KEY, true);
        rangeHistogramView.setVisible(true);
    }

    private void hideHistogram()
    {
        if(rangeHistogramView != null && rangeHistogramView.isVisible())
        {
            rangeHistogramView.setVisible(false);
        }
    }

    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object src = evt.getSource();

        if(lowerBoundSpinner.equals(src))
        {
            lowerGradientBound = ((SpinnerNumberModel)lowerBoundSpinner.getModel()).getNumber().doubleValue();

            receiver.setLowerBound(convertValueInDisplayedUnitToDataUnits(lowerGradientBound));

            setGradientRangeSelector(GradientRangeSelector.MANUAL);		

        }
        else if(upperBoundSpinner.equals(src))
        {
            upperGradientBound = ((SpinnerNumberModel)upperBoundSpinner.getModel()).getNumber().doubleValue();

            receiver.setUpperBound(convertValueInDisplayedUnitToDataUnits(upperGradientBound)); 

            gradientRangeSelector = GradientRangeSelector.MANUAL;		
            setGradientRangeSelector(GradientRangeSelector.MANUAL);		
        }
    }



    @Override
    public void actionPerformed(ActionEvent evt) 
    {
        String command = evt.getActionCommand();
        if (command.equals(RANGE_LUT_COMMAND)) 
        {
            attemptColorGradientEdition();
        }	
        else if (command.equals(UNDERFLOW_COLOR_COMMAND)) 
        {
            attemptGradientUnderflowColorSelection();
        }	
        else if (command.equals(OVERFLOW_COLOR_COMMAND)) 
        {			
            attemptGradientOverflowColorSelection();
        }	
        else if(command.equals(MASK_COLOR_COMMAND))
        {
            attemptMaskColorSelection();
        }
    }

    private void setConsistentWithUseMasks()
    {		
        boolean useMasks = !GradientMaskSelector.NO_MASK.equals(gradientMaskSelector);

        buttonSelectMaskColor.setEnabled(useMasks);
        labelMaskColor.setEnabled(useMasks);
        maskColorSample.setEnabled(useMasks);
    }

    private void setConsistentWithUseOutsideColors()
    {		
        buttonSelectOverflowColor.setEnabled(useOutsideRangeColors);
        buttonSelectUnderflowColor.setEnabled(useOutsideRangeColors);
        labelUnderflowColor.setEnabled(useOutsideRangeColors);
        labelOverflowColor.setEnabled(useOutsideRangeColors);	
        underflowColorSample.setEnabled(useOutsideRangeColors);
        overflowColorSample.setEnabled(useOutsideRangeColors);
    }

    @Override
    public ColorGradient getColorGradient() 
    {
        return rangeColorGradient;
    }

    private void setInternalColorGradient(ColorGradient colorGradientNew)
    {
        ColorGradient colorGradientOld = this.rangeColorGradient;
        this.rangeColorGradient = colorGradientNew;

        firePropertyChange(GradientPaintReceiver.GRADIENT_COLOR, colorGradientOld, this.rangeColorGradient);
    }

    @Override
    public void setColorGradient(ColorGradient colorGradientNew) 
    {
        setInternalColorGradient(colorGradientNew);
        this.rangeColorGradientSample.setPaint(new GradientPaint(rangeColorGradient));
        receiver.setColorGradient(rangeColorGradient);				
    }

    public void attemptColorGradientEdition() 
    {
        if(gradientEditionDialog == null)
        {
            gradientEditionDialog = new GradientEditionDialog(this);			
        }
        gradientEditionDialog.showDialog(this);
    }

    private void attemptGradientUnderflowColorSelection() 
    {
        Paint p = this.gradientUnderflowColor;
        Color defaultColor = (p instanceof Color ? (Color) p : Color.blue);
        Color c = JColorChooser.showDialog(this, "Underflow color", defaultColor);
        if (c != null) 
        {
            gradientUnderflowColor = c;
            this.underflowColorSample.setPaint(gradientUnderflowColor);
            receiver.setGradientUnderflowColor(gradientUnderflowColor);
        }
    }

    private void attemptGradientOverflowColorSelection() 
    {
        Paint p = this.gradientOverflowColor;
        Color defaultColor = (p instanceof Color ? (Color) p : Color.blue);
        Color c = JColorChooser.showDialog(this, "Overflow color", defaultColor);
        if (c != null) 
        {
            gradientOverflowColor = c;
            this.overflowColorSample.setPaint(gradientOverflowColor);
            receiver.setGradientOverflowColor(gradientOverflowColor);
        }
    }

    private void attemptMaskColorSelection() 
    {
        Paint p = this.maskColor;
        Color defaultColor = (p instanceof Color ? (Color) p : Color.black);
        Color c = JColorChooser.showDialog(this, "Mask color", defaultColor);
        if (c != null) 
        {
            maskColor = c;
            this.maskColorSample.setPaint(maskColor);
            receiver.setMaskColor(maskColor);
        }
    }

    private void setGradientRangeSelector(GradientRangeSelector selector)
    {
        if(GradientRangeSelector.FULL.equals(selector))
        {
            setFullRange();
        }
        else if(GradientRangeSelector.AUTOMATIC.equals(selector))
        {
            setAutomaticRange();
        }
        else if(GradientRangeSelector.ROI_FULL.equals(selector))
        {
            setROILensRange();
        }
        else 
        {
            setManualRange();
        }
    }

    private void setFullRange()
    {			
        lowerBoundSpinner.setValue(convertValueInDataUnitToDisplayedUnits(receiver.getLowerFullBound()));
        upperBoundSpinner.setValue(convertValueInDataUnitToDisplayedUnits(receiver.getUpperFullBound()));

        gradientRangeSelector =  GradientRangeSelector.FULL;
        //this is next line may seem strange, but is necessary, because above we changed the value of a spinner
        //which generated changeEvent, which caused the change of gradient paint selector to MANUAL
        //I know this is an ugly hack, but what one can do?
        gradientSelectionGroup.setSelected(buttonFull.getModel(), true);
        receiver.setGradientRangeSelector(gradientRangeSelector);

    }

    private void setAutomaticRange()
    {			
        lowerBoundSpinner.setValue(convertValueInDataUnitToDisplayedUnits(receiver.getLowerAutomaticBound()));
        upperBoundSpinner.setValue(convertValueInDataUnitToDisplayedUnits(receiver.getUpperAutomaticBound()));

        gradientRangeSelector = GradientRangeSelector.AUTOMATIC;

        //this is next line may seem strange, but is necessary, because above we changed the value of a spinner
        //which generated changeEvent, which caused the change of gradient paint selector to MANUAL
        //I know this is an ugly hack, but what one can do?
        gradientSelectionGroup.setSelected(buttonAutomatic.getModel(), true);

        receiver.setGradientRangeSelector(gradientRangeSelector);
    }

    private void setROILensRange()
    {			
        lowerBoundSpinner.setValue(convertValueInDataUnitToDisplayedUnits(receiver.getLowerROIBound()));
        upperBoundSpinner.setValue(convertValueInDataUnitToDisplayedUnits(receiver.getUpperROIBound()));

        gradientRangeSelector = GradientRangeSelector.ROI_FULL;

        //this is next line may seem strange, but is necessary, because above we changed the value of a spinner
        //which generated changeEvent, which caused the change of gradient paint selector to MANUAL
        //I know this is an ugly hack, but what one can do?
        gradientSelectionGroup.setSelected(buttonROILens.getModel(), true);

        receiver.setGradientRangeSelector(gradientRangeSelector);
    }

    private void setManualRange()
    {
        gradientRangeSelector = GradientRangeSelector.MANUAL;

        gradientSelectionGroup.clearSelection();
        receiver.setGradientRangeSelector(gradientRangeSelector);
    }


    protected void reset()
    {
        resetReceiver();		
        setParametersToInitial();
        resetEditor();
    }

    private void cancel()
    {
        //its important that resetReceiver() is called before setParametersToInitial()
        resetReceiver();	
        setParametersToInitial();
        resetEditor();

        setVisible(false);
    }

    private JPanel buildMainPanel()
    {
        SubPanel mainPanel = new SubPanel();

        JLabel labelRange = new JLabel("Range");
        JLabel labelFull = new JLabel("Full range");
        JLabel labelMask = new JLabel("ROI masks");
        JLabel labelGradientColors = new JLabel("Range colors");
        JLabel labelOutsideColors = new JLabel("Outside range colors");


        labelRange.setFont(labelRange.getFont().deriveFont(Font.BOLD));
        labelFull.setFont(labelFull.getFont().deriveFont(Font.BOLD));
        labelMask.setFont(labelMask.getFont().deriveFont(Font.BOLD));
        labelGradientColors.setFont(labelGradientColors.getFont().deriveFont(Font.BOLD));
        labelOutsideColors.setFont(labelOutsideColors.getFont().deriveFont(Font.BOLD));

        SubPanel panelGradientRange = new SubPanel();

        panelGradientRange.addComponent(buttonFull, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        panelGradientRange.addComponent(buttonAutomatic, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);

        gradientSelectionGroup.add(buttonFull);
        gradientSelectionGroup.add(buttonAutomatic);
        if(allowForROIs)
        {
            gradientSelectionGroup.add(buttonROILens);
            panelGradientRange.addComponent(buttonROILens, 0, 1, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        }


        SubPanel panelHistogram = new SubPanel(); 		
        buttonHistogram.setHideActionText(true);
        buttonHistogram.setMargin(new Insets(0,0,0,0));

        panelHistogram.addComponent(buttonHistogram, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0, 0);

        mainPanel.addComponent(labelRange, 0, 0, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(6,3,5,5));

        mainPanel.addComponent(panelGradientRange, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(new JLabel("Minimum"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(lowerBoundSpinner, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);		
        mainPanel.addComponent(labelLowerBoundUnit, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 1);

        mainPanel.addComponent(panelHistogram, 3, 2, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 1);		

        mainPanel.addComponent(new JLabel("Maximum"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(upperBoundSpinner, 1, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(labelUpperBoundUnit, 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 1);

        mainPanel.addComponent(labelFull, 0, 4, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1, new Insets(9,3,5,5));

        labelLowerFullBound.setHorizontalAlignment(SwingConstants.RIGHT);
        labelUpperFullBound.setHorizontalAlignment(SwingConstants.RIGHT);

        mainPanel.addComponent(new JLabel("Minimum"), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(labelLowerFullBound, 1, 5, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(labelLowerFullBoundUnit, 2, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 1);

        mainPanel.addComponent(new JLabel("Maximum"), 0, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(labelUpperFullBound, 1, 6, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(labelUpperFullBoundUnit, 2, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 1);

        //        mainPanel.addComponent(panelMinimum, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);      
        //        mainPanel.addComponent(panelMaximum, 1, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(labelGradientColors, 0, 7, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1, new Insets(9,3,5,5));

        mainPanel.addComponent(new JLabel("Gradient"), 0, 8, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(rangeColorGradientSample, 1, 8, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(buttonEditLUTTable, 3, 8, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .05, 1);     

        mainPanel.addComponent(labelOutsideColors, 0, 9, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1, new Insets(9,3,5,5));

        mainPanel.addComponent(boxUseOutsideRangeColors, 1, 10, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);

        mainPanel.addComponent(labelUnderflowColor, 0, 11, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(underflowColorSample, 1, 11, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(buttonSelectUnderflowColor, 3, 11, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .05, 1);     

        mainPanel.addComponent(labelOverflowColor, 0, 12, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        mainPanel.addComponent(overflowColorSample, 1, 12, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(buttonSelectOverflowColor, 3, 12, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .05, 1);     

        if(allowForROIs)
        {
            SubPanel panelMasks = new SubPanel();
            panelMasks.addComponent(boxOutsideMask, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
            panelMasks.addComponent(boxInsideMask, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);


            mainPanel.addComponent(labelMask, 0, 13, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1, new Insets(9,3,5,5));
            mainPanel.addComponent(panelMasks, 1, 14, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);


            mainPanel.addComponent(labelMaskColor, 0, 15, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
            mainPanel.addComponent(maskColorSample, 1, 15, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
            mainPanel.addComponent(buttonSelectMaskColor, 3, 15, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.05, 1);
        }

        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return mainPanel;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        JButton buttonOK = new JButton(new OKAction());
        JButton buttonReset = new JButton(new ResetAction());
        JButton buttonCancel = new JButton(new CancelAction());

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonOK).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(buttonReset).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonOK)
                .addComponent(buttonReset)
                .addComponent(buttonCancel));

        layout.linkSize(buttonOK, buttonReset, buttonCancel);

        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
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
            setVisible(false);
        };
    }

    private class ResetAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ResetAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME,"Reset");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            reset();
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

    private class ShowHistogramAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ShowHistogramAction() 
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            ImageIcon icon = new ImageIcon(toolkit.getImage("Resources/HistogramLarger.png"));

            putValue(LARGE_ICON_KEY, icon);
            putValue(Action.SELECTED_KEY, false);

            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));

            putValue(MNEMONIC_KEY, KeyEvent.VK_A);
            putValue(NAME, "Histograms");
            putValue(SHORT_DESCRIPTION,"Show graphical results");

        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean visible = (boolean) getValue(SELECTED_KEY);
            setHistogramVisible(visible);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();
        if(RangeModel.LOWER_BOUND.equals(name))
        {
            lowerGradientBound = convertValueInDataUnitToDisplayedUnits((double)evt.getNewValue());
            lowerBoundSpinner.removeChangeListener(this);

            lowerBoundSpinner.setValue(lowerGradientBound);			
            lowerBoundSpinner.addChangeListener(this);
        }
        else if(RangeModel.UPPER_BOUND.equals(name))
        {
            upperGradientBound = convertValueInDataUnitToDisplayedUnits((double)evt.getNewValue());
            upperBoundSpinner.removeChangeListener(this);

            upperBoundSpinner.setValue(upperGradientBound);			
            upperBoundSpinner.addChangeListener(this);
        }
        else if(RangeModel.LOWER_FULL_BOUND.equals(name))
        {
            double lowerFullBoundNew = convertValueInDataUnitToDisplayedUnits((double)evt.getNewValue());
            String labelLowerBoundText = numberFormat.format(lowerFullBoundNew);
            labelLowerFullBound.setText(labelLowerBoundText);
        }
        else if(RangeModel.UPPER_FULL_BOUND.equals(name))
        {
            double upperFullBoundNew = convertValueInDataUnitToDisplayedUnits((double)evt.getNewValue());
            String labelUpperBoundText = numberFormat.format(upperFullBoundNew);
            labelUpperFullBound.setText(labelUpperBoundText);
        }
        else if(GradientPaintReceiver.GRADIENT_MASK_SELECTOR.equals(name))
        {
            gradientMaskSelector = (GradientMaskSelector)evt.getNewValue();
            if(GradientMaskSelector.MASK_OUTSIDE.equals(gradientMaskSelector))
            {
                boxOutsideMask.setSelected(true);
                boxInsideMask.setSelected(false);
            }
            else if(GradientMaskSelector.MASK_INSIDE.equals(gradientMaskSelector))
            {
                boxOutsideMask.setSelected(false);
                boxInsideMask.setSelected(true);
            }
            else
            {
                boxInsideMask.setSelected(false);
                boxOutsideMask.setSelected(false);
            }

            setConsistentWithUseMasks();
        }
        else if(GradientPaintReceiver.GRADIENT_RANGE_SELECTOR.equals(name))
        {
            gradientRangeSelector = (GradientRangeSelector)evt.getNewValue();

            if(GradientRangeSelector.FULL.equals(gradientRangeSelector))
            {
                gradientSelectionGroup.setSelected(buttonFull.getModel(), true);
            }
            else if(GradientRangeSelector.AUTOMATIC.equals(gradientRangeSelector))
            {
                gradientSelectionGroup.setSelected(buttonAutomatic.getModel(), true);
            }
            else if(GradientRangeSelector.ROI_FULL.equals(gradientRangeSelector))
            {
                gradientSelectionGroup.setSelected(buttonROILens.getModel(), true);
            }
            else
            {
                gradientSelectionGroup.clearSelection();
            }
        }
        else if(GradientPaintReceiver.GRADIENT_COLOR.equals(name))
        {
            ColorGradient colorGradientNew = (ColorGradient)evt.getNewValue();
            setInternalColorGradient(colorGradientNew);
            rangeColorGradientSample.setPaint(new GradientPaint(rangeColorGradient));			
        }
        else if(GradientPaintReceiver.UNDERFLOW_COLOR.equals(name))
        {
            this.gradientUnderflowColor = (Color)evt.getNewValue();
            underflowColorSample.setPaint(gradientUnderflowColor);
        }
        else if(GradientPaintReceiver.OVERFLOW_COLOR.equals(name))
        {
            Color gradientOverflowColorNew = (Color)evt.getNewValue();
            this.gradientOverflowColor = gradientOverflowColorNew;
            overflowColorSample.setPaint(gradientOverflowColorNew);
        }
        else if(GradientPaintReceiver.USE_OUTSIDE_RANGE_COLORS.equals(name))
        {
            this.useOutsideRangeColors = (boolean)evt.getNewValue();
            boxUseOutsideRangeColors.setSelected(this.useOutsideRangeColors);

            setConsistentWithUseOutsideColors();
        }
        else if(GradientPaintReceiver.MASK_COLOR.equals(name))
        {
            Color maskColorNew = (Color)evt.getNewValue();
            this.maskColor = maskColorNew;
            maskColorSample.setPaint(maskColorNew);
        }
    }
}
