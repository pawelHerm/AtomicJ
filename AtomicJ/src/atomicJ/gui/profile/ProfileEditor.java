
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

package atomicJ.gui.profile;


import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.AtomicJ;
import atomicJ.gui.BasicStrokeReceiver;
import atomicJ.gui.FontField;
import atomicJ.gui.FontReceiver;
import atomicJ.gui.LabelAutomaticType;
import atomicJ.gui.PaintSampleFlexible;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.SubPanel;
import atomicJ.gui.editors.FontChooserDialog;



public class ProfileEditor extends JDialog implements ActionListener, ItemListener, ChangeListener
{
    private static final long serialVersionUID = 1L;

    private static final String SELECT_LABEL_FONT_UNFINISHED_STANDARD = "SELECT_LABEL_FONT_UNFINISHED_STANDARD";
    private static final String SELECT_LABEL_FONT_FINISHED_STANDARD = "SELECT_LABEL_FONT_FINISHED_STANDARD";

    private static final String EDIT_STROKE_UNFINISHED_STANDARD = "EDIT_STROKE_UNFINISHED_STANDARD";
    private static final String EDIT_STROKE_FINISHED_STANDARD = "EDIT_STROKE_FINISHED_STANDARD";

    private static final String SELECT_PAINT_LABEL_UNFINISHED_STANDARD = "SELECT_PAINT_LABEL_UNFINISHED_STANDARD";
    private static final String SELECT_PAINT_LABEL_FINISHED_STANDARD = "SELECT_PAINT_LABEL_FINISHED_STANDARD";

    //initial settings

    private boolean initOutlineVisible;

    private boolean initKnobVisible;
    private KnobOrientation initKnobOrientation;
    private int initKnobWidth;
    private int initKnobHeight;

    private LabelAutomaticType initLabelType;
    private float initLabelOffset;
    private float initLabelLengthwisePosition;

    private boolean initIsArrowheadVisibleUnfinishedStandard;
    private boolean initIsArrowheadVisibleFinishedStandard;

    private float initArrowheadLengthUnfinishedStandard;
    private float initArrowheadLengthFinishedStandard;

    private float initArrowheadWidthUnfinishedStandard;
    private float initArrowheadWidthFinishedStandard;

    private boolean initLabelVisibleUnfinishedStandard; 
    private boolean initLabelVisibleFinishedStandard;     

    private Font initLabelFontUnfinishedStandard;
    private Font initLabelFontFinishedStandard;

    private Paint initPaintLabelFinishedStandard;
    private Paint initPaintLabelUnfinishedStandard;

    private Stroke initStrokeUnfinishedStandard;
    private Stroke initStrokeFinishedStandard;

    private Paint initPaintUnfinishedStandard;
    private Paint initPaintFinishedStandard;


    //current settings

    private boolean outlineVisible;

    private boolean knobVisible;
    private KnobOrientation knobOrientation;
    private int knobWidth;
    private int knobHeight;

    private LabelAutomaticType labelType;
    private float labelOffset;
    private float labelLengthwisePosition;

    private boolean isArrowheadVisibleUnfinishedStandard;
    private boolean isArrowheadVisibleFinishedStandard;

    private float arrowheadLengthUnfinishedStandard;
    private float arrowheadLengthFinishedStandard;

    private float arrowheadWidthUnfinishedStandard;
    private float arrowheadWidthFinishedStandard;

    private boolean labelVisibleUnfinishedStandard;    
    private boolean labelVisibleFinishedStandard;     

    private Font labelFontUnfinishedStandard;
    private Font labelFontFinishedStandard;

    private Paint paintLabelUnfinishedStandard;
    private Paint paintLabelFinishedStandard;

    private Stroke strokeUnfinishedStandard;
    private Stroke strokeFinishedStandard;

    private Paint paintUnfinishedStandard;
    private Paint paintFinishedStandard;

    private final ApplyToAllAction applyToAllAction = new ApplyToAllAction();

    //GUI components

    private final JCheckBox boxKnobVisible = new JCheckBox();
    private final JComboBox<KnobOrientation> comboKnobOrientation = new JComboBox<>(KnobOrientation.values());
    private final JSpinner spinnerKnobWidth = new JSpinner(new SpinnerNumberModel(1,1,1000,1));  
    private final JSpinner spinnerKnobHeight = new JSpinner(new SpinnerNumberModel(1,1,1000,1));  

    private final JComboBox<LabelAutomaticType> comboLabelType = new JComboBox<>(LabelAutomaticType.values());

    private final StraightStrokeSample strokeUnfinishedStandardSample = new StraightStrokeSample();
    private final StraightStrokeSample strokeFinishedStandardSample = new StraightStrokeSample();

    private final PaintSampleFlexible paintLabelUnfinishedStandardSample = new PaintSampleFlexible();
    private final PaintSampleFlexible paintLabelFinishedStandardSample = new PaintSampleFlexible();

    private final FontField fieldLabelFontUnfinishedStandard = new FontField();
    private final FontField fieldLabelFontFinishedStandard = new FontField();

    private final JButton buttonSelectLabelFontUnfinishedStandard = new JButton("Select");
    private final JButton buttonSelectLabelFontFinishedStandard = new JButton("Select");

    private final JButton buttonEditStrokeUnfinishedStandard = new JButton("Edit");
    private final JButton buttonEditStrokeFinishedStandard = new JButton("Edit");

    private final JButton buttonSelectPaintLabelUnfinishedStandard = new JButton("Select");
    private final JButton buttonSelectPaintLabelFinishedStandard = new JButton("Select");

    private final JCheckBox boxArrowheadUnfinishedStandard = new JCheckBox();
    private final JCheckBox boxArrowheadFinishedStandard = new JCheckBox();

    private final JCheckBox boxLabelVisibleUnfinishedStandard = new JCheckBox();
    private final JCheckBox boxLabelVisibleFinishedStandard = new JCheckBox();


    private final JSpinner spinnerArrowheadWidthUnfinishedStandard = new JSpinner(new SpinnerNumberModel(Float.valueOf(0),Float.valueOf(0),Float.valueOf(10000),Float.valueOf(0.2f)));  
    private final JSpinner spinnerArrowheadWidthFinishedStandard = new JSpinner(new SpinnerNumberModel(Float.valueOf(0),Float.valueOf(0),Float.valueOf(10000),Float.valueOf(0.2f)));  

    private final JSpinner spinnerArrowheadLengthUnfinishedStandard = new JSpinner(new SpinnerNumberModel(Float.valueOf(0),Float.valueOf(0),Float.valueOf(10000),Float.valueOf(0.2f)));  
    private final JSpinner spinnerArrowheadLengthFinishedStandard = new JSpinner(new SpinnerNumberModel(Float.valueOf(0),Float.valueOf(0),Float.valueOf(10000),Float.valueOf(0.2f)));  


    private StrokeChooser chooserStrokeUnfinishedStandard;
    private StrokeChooser chooserStrokeFinishedStandard;

    private FontChooserDialog fontChooserDialog;

    private final List<ProfileStyle> boundedModels = new ArrayList<>();
    private ProfileStyle model;

    public ProfileEditor(Window parent, String title) 
    {   
        super(parent, title, ModalityType.APPLICATION_MODAL);

        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        JPanel unfinishedPanel = buildUnfinishedPanel();  
        JPanel finishedPanel = buildFinishedPanel();       

        tabbedPane.add("Finished", finishedPanel);
        tabbedPane.add("Unfinished", unfinishedPanel);         

        add(tabbedPane, BorderLayout.NORTH);

        JPanel buttonPanel = buildButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        updateActionsEnability();

        initActionListener();
        initChangeListener();
        initItemListener();

        pack();
        setLocationRelativeTo(parent);
    }

    public void setModel(ProfileStyle model)
    {
        this.model = model;

        this.initKnobVisible = model.isKnobVisible();
        this.initKnobOrientation = model.getKnobOrientation();
        this.initKnobWidth = model.getKnobWidth();
        this.initKnobHeight = model.getKnobHeight();

        this.initLabelType = model.getLabelType();
        this.initLabelOffset = model.getLabelOffset();
        this.initLabelLengthwisePosition = model.getLabelLengthwisePosition();

        this.initIsArrowheadVisibleUnfinishedStandard = model.isArrowheadVisibleUnfinishedStandard();
        this.initIsArrowheadVisibleFinishedStandard = model.isArrowheadVisibleFinishedStandard();

        this.initArrowheadLengthUnfinishedStandard = model.getArrowheadLengthUnfinishedStandard();
        this.initArrowheadLengthFinishedStandard = model.getArrowheadWidthUnfinishedStandard();

        this.initArrowheadWidthUnfinishedStandard = model.getArrowheadWidthUnfinishedStandard();
        this.initArrowheadWidthFinishedStandard = model.getArrowheadWidthFinishedStandard();

        this.initLabelVisibleUnfinishedStandard = model.isLabelVisibleUnfinished();  
        this.initLabelVisibleFinishedStandard = model.isLabelVisibleFinished();     

        this.initLabelFontUnfinishedStandard = model.getLabelFontUnfinished();
        this.initLabelFontFinishedStandard = model.getLabelFontFinished();

        this.initPaintLabelUnfinishedStandard = model.getPaintLabelUnfinished();
        this.initPaintLabelFinishedStandard = model.getPaintLabelFinished();

        this.initStrokeUnfinishedStandard = model.getStrokeUnfinished();
        this.initStrokeFinishedStandard = model.getStrokeFinished();

        this.initPaintUnfinishedStandard = model.getPaintUnfinished();
        this.initPaintFinishedStandard = model.getPaintFinished();

        setParametersToInitial();        
        resetEditor();
    }

    public void setBoundedModels(List<ProfileStyle> models)
    {
        boundedModels.clear();
        boundedModels.addAll(models);

        updateActionsEnability();
    }

    private void setParametersToInitial()
    {
        this.outlineVisible = initOutlineVisible;

        this.knobVisible = initKnobVisible;
        this.knobOrientation = initKnobOrientation;
        this.knobWidth = initKnobWidth;
        this.knobHeight = initKnobHeight;               

        this.labelType = initLabelType;
        this.labelOffset = initLabelOffset;
        this.labelLengthwisePosition = initLabelLengthwisePosition;

        this.isArrowheadVisibleUnfinishedStandard = initIsArrowheadVisibleUnfinishedStandard;
        this.isArrowheadVisibleFinishedStandard = initIsArrowheadVisibleFinishedStandard;

        this.arrowheadLengthUnfinishedStandard = initArrowheadLengthUnfinishedStandard;
        this.arrowheadLengthFinishedStandard = initArrowheadLengthFinishedStandard;

        this.arrowheadWidthUnfinishedStandard = initArrowheadWidthUnfinishedStandard;
        this.arrowheadWidthFinishedStandard = initArrowheadWidthFinishedStandard;

        this.labelVisibleUnfinishedStandard = initLabelVisibleUnfinishedStandard;
        this.labelVisibleFinishedStandard = initLabelVisibleFinishedStandard;     

        this.labelFontUnfinishedStandard = initLabelFontUnfinishedStandard;
        this.labelFontFinishedStandard = initLabelFontFinishedStandard;

        this.paintLabelUnfinishedStandard = initPaintLabelUnfinishedStandard;
        this.paintLabelFinishedStandard = initPaintLabelFinishedStandard;

        this.strokeUnfinishedStandard = initStrokeUnfinishedStandard;
        this.strokeFinishedStandard = initStrokeFinishedStandard;

        this.paintUnfinishedStandard = initPaintUnfinishedStandard;
        this.paintFinishedStandard = initPaintFinishedStandard;
    }

    private void initActionListener()
    {
        buttonSelectLabelFontUnfinishedStandard.setActionCommand(SELECT_LABEL_FONT_UNFINISHED_STANDARD);
        buttonSelectLabelFontFinishedStandard.setActionCommand(SELECT_LABEL_FONT_FINISHED_STANDARD);

        buttonEditStrokeUnfinishedStandard.setActionCommand(EDIT_STROKE_UNFINISHED_STANDARD);
        buttonEditStrokeFinishedStandard.setActionCommand(EDIT_STROKE_FINISHED_STANDARD);

        buttonSelectPaintLabelUnfinishedStandard.setActionCommand(SELECT_PAINT_LABEL_UNFINISHED_STANDARD);
        buttonSelectPaintLabelFinishedStandard.setActionCommand(SELECT_PAINT_LABEL_FINISHED_STANDARD);

        buttonSelectLabelFontUnfinishedStandard.addActionListener(this);
        buttonSelectLabelFontFinishedStandard.addActionListener(this);

        buttonEditStrokeUnfinishedStandard.addActionListener(this);
        buttonEditStrokeFinishedStandard.addActionListener(this);

        buttonSelectPaintLabelUnfinishedStandard.addActionListener(this);
        buttonSelectPaintLabelFinishedStandard.addActionListener(this);
    }

    private void initChangeListener()
    {
        spinnerKnobWidth.addChangeListener(this);
        spinnerKnobHeight.addChangeListener(this);

        spinnerArrowheadWidthUnfinishedStandard.addChangeListener(this);  
        spinnerArrowheadWidthFinishedStandard.addChangeListener(this); 

        spinnerArrowheadLengthUnfinishedStandard.addChangeListener(this);  
        spinnerArrowheadLengthFinishedStandard.addChangeListener(this);  
    }

    private void initItemListener()
    {
        boxKnobVisible.addItemListener(this);
        comboKnobOrientation.addItemListener(this);

        comboLabelType.addItemListener(this);
        boxArrowheadUnfinishedStandard.addItemListener(this);
        boxArrowheadFinishedStandard.addItemListener(this);

        boxLabelVisibleUnfinishedStandard.addItemListener(this);
        boxLabelVisibleFinishedStandard.addItemListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();

        if(SELECT_LABEL_FONT_UNFINISHED_STANDARD.equals(command))
        {
            if( fontChooserDialog == null)
            {
                this.fontChooserDialog = new FontChooserDialog(this, "Font selection");
            }

            this.fontChooserDialog.showDialog(new FontReceiver() 
            {

                @Override
                public void setFont(Font newFont) 
                { 
                    labelFontUnfinishedStandard = newFont;
                    fieldLabelFontUnfinishedStandard.setDisplayFont(labelFontUnfinishedStandard);
                    model.setLabelFontUnfinishedStandard(labelFontUnfinishedStandard);
                }

                @Override
                public Font getFont()
                {
                    return labelFontUnfinishedStandard;
                }
            });
        }
        else if(SELECT_LABEL_FONT_FINISHED_STANDARD.equals(command))
        {
            if( fontChooserDialog == null)
            {
                this.fontChooserDialog = new FontChooserDialog(this, "Font selection");
            }

            this.fontChooserDialog.showDialog(new FontReceiver() 
            {

                @Override
                public void setFont(Font newFont) 
                { 
                    labelFontFinishedStandard = newFont;
                    fieldLabelFontFinishedStandard.setDisplayFont(labelFontFinishedStandard);
                    model.setLabelFontFinishedStandard(labelFontFinishedStandard);
                }

                @Override
                public Font getFont()
                {
                    return labelFontFinishedStandard;
                }
            });
        }
        else if(EDIT_STROKE_UNFINISHED_STANDARD.equals(command))
        {
            attemptStrokeUnfinishedStandardSelection();
        }      
        else if(EDIT_STROKE_FINISHED_STANDARD.equals(command))
        {
            attemptStrokeFinishedStandardSelection();
        }
        else if(SELECT_PAINT_LABEL_UNFINISHED_STANDARD.equals(command))
        {
            Paint paintLabelUnfinishedStandardNew = JColorChooser.showDialog(this,"Label color", (Color)paintLabelUnfinishedStandard);	        
            if (paintLabelUnfinishedStandardNew != null) 
            {
                paintLabelUnfinishedStandard = paintLabelUnfinishedStandardNew;
                paintLabelUnfinishedStandardSample.setPaint(paintLabelUnfinishedStandardNew);
                model.setPaintLabelUnfinishedStandard(paintLabelUnfinishedStandardNew)	;		
            }
        }
        else if(SELECT_PAINT_LABEL_FINISHED_STANDARD.equals(command))
        {
            Paint paintLabelFinishedStandardNew = JColorChooser.showDialog(this, "Label color", (Color)paintLabelFinishedStandard);	        
            if (paintLabelFinishedStandardNew != null) 
            {
                paintLabelFinishedStandard = paintLabelFinishedStandardNew;
                paintLabelFinishedStandardSample.setPaint(paintLabelFinishedStandardNew);
                model.setPaintLabelFinishedStandard(paintLabelFinishedStandardNew)	;		
            }
        }
    }

    private void hideRoot()
    {
        Component root = SwingUtilities.getRoot(this);
        root.setVisible(false);
    }

    private void attemptStrokeUnfinishedStandardSelection() 
    {
        if(chooserStrokeUnfinishedStandard == null)
        {
            chooserStrokeUnfinishedStandard = new StrokeChooser(this, new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke) strokeUnfinishedStandard;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    strokeUnfinishedStandard = stroke;
                    strokeUnfinishedStandardSample.setStroke(stroke);
                    model.setStrokeUnfinishedStandard(stroke);
                }
                @Override
                public void setStrokePaint(Paint paint)
                {
                    paintUnfinishedStandard = paint;
                    strokeUnfinishedStandardSample.setStrokePaint(paint);
                    model.setPaintUnfinishedStandard(paint);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return paintUnfinishedStandard;
                }     	
            }
                    );
        }        
        chooserStrokeUnfinishedStandard.showDialog();
    }

    private void attemptStrokeFinishedStandardSelection() 
    {
        if(chooserStrokeFinishedStandard == null)
        {
            chooserStrokeFinishedStandard = new StrokeChooser(this, new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke) strokeFinishedStandard;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    strokeFinishedStandard = stroke;
                    strokeFinishedStandardSample.setStroke(stroke);
                    model.setStrokeFinishedStandard(stroke);
                }
                @Override
                public void setStrokePaint(Paint paint)
                {
                    paintFinishedStandard = paint;
                    strokeFinishedStandardSample.setStrokePaint(paint);
                    model.setPaintFinishedStandard(paint);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return paintFinishedStandard;
                }     	
            }
                    );
        }        
        chooserStrokeFinishedStandard.showDialog();
    }

    private void resetEditor()
    {
        this.boxKnobVisible.setSelected(knobVisible);
        this.comboKnobOrientation.setSelectedItem(knobOrientation);

        this.comboLabelType.setSelectedItem(labelType);

        this.spinnerKnobWidth.setValue(knobWidth);
        this.spinnerKnobHeight.setValue(knobHeight);

        this.boxArrowheadUnfinishedStandard.setSelected(isArrowheadVisibleUnfinishedStandard);
        this.boxArrowheadFinishedStandard.setSelected(isArrowheadVisibleFinishedStandard);        

        this.spinnerArrowheadWidthUnfinishedStandard.setValue(arrowheadWidthUnfinishedStandard);  
        this.spinnerArrowheadWidthFinishedStandard.setValue(arrowheadWidthFinishedStandard);  

        this.spinnerArrowheadLengthUnfinishedStandard.setValue(arrowheadLengthUnfinishedStandard);  
        this.spinnerArrowheadLengthFinishedStandard.setValue(arrowheadLengthFinishedStandard);    

        this.fieldLabelFontUnfinishedStandard.setDisplayFont(initLabelFontUnfinishedStandard);
        this.fieldLabelFontFinishedStandard.setDisplayFont(initLabelFontFinishedStandard);

        this.paintLabelUnfinishedStandardSample.setPaint(paintLabelUnfinishedStandard);
        this.paintLabelFinishedStandardSample.setPaint(paintLabelFinishedStandard);

        this.strokeUnfinishedStandardSample.setStroke(strokeUnfinishedStandard);
        this.strokeFinishedStandardSample.setStroke(strokeFinishedStandard);

        this.strokeUnfinishedStandardSample.setStrokePaint(paintUnfinishedStandard);
        this.strokeFinishedStandardSample.setStrokePaint(paintFinishedStandard);

        this.boxLabelVisibleUnfinishedStandard.setSelected(labelVisibleUnfinishedStandard);
        this.boxLabelVisibleFinishedStandard.setSelected(labelVisibleFinishedStandard);      
    }

    private void resetModel(ProfileStyle model)
    {    	
        model.setKnobVisible(knobVisible);
        model.setKnobOrientation(knobOrientation);
        model.setKnobWidth(knobWidth);
        model.setKnobHeight(knobHeight);

        model.setLabelType(labelType);
        model.setLabelOffset(labelOffset);
        model.setLabelLengthwisePosition(labelLengthwisePosition);

        model.setArrowheadVisibleUnfinishedStandard(isArrowheadVisibleUnfinishedStandard);
        model.setArrowheadVisibleFinishedStandard(isArrowheadVisibleFinishedStandard);

        model.setLabelVisibleUnfinishedStandard(labelVisibleUnfinishedStandard);    
        model.setLabelVisibleFinishedStandard(labelVisibleFinishedStandard);     

        model.setLabelFontUnfinishedStandard(labelFontUnfinishedStandard);
        model.setLabelFontFinishedStandard(labelFontFinishedStandard);

        model.setPaintLabelUnfinishedStandard(paintLabelUnfinishedStandard);
        model.setPaintLabelFinishedStandard(paintLabelFinishedStandard);

        model.setStrokeUnfinishedStandard(strokeUnfinishedStandard);
        model.setStrokeFinishedStandard(strokeFinishedStandard);

        model.setPaintUnfinishedStandard(paintUnfinishedStandard);
        model.setPaintFinishedStandard(paintFinishedStandard);
    }

    public void resetToDefaults() 
    {
        this.knobVisible = model.isKnobVisible();
        this.knobOrientation = model.getKnobOrientation();
        this.knobWidth = model.getKnobWidth();
        this.knobHeight = model.getKnobHeight();

        this.labelType = model.getLabelType();
        this.labelOffset = model.getLabelOffset();
        this.labelLengthwisePosition = model.getLabelLengthwisePosition();

        model.setDefaultStyle();

        this.isArrowheadVisibleUnfinishedStandard = model.isArrowheadVisibleUnfinishedStandard();
        this.isArrowheadVisibleFinishedStandard = model.isArrowheadVisibleFinishedStandard();

        this.arrowheadLengthUnfinishedStandard = model.getArrowheadLengthUnfinishedStandard();
        this.arrowheadLengthFinishedStandard = model.getArrowheadLengthFinishedStandard();

        this.arrowheadWidthUnfinishedStandard = model.getArrowheadWidthUnfinishedStandard();
        this.arrowheadWidthFinishedStandard = model.getArrowheadWidthFinishedStandard();

        this.labelVisibleUnfinishedStandard = model.isLabelVisibleUnfinished();     
        this.labelVisibleFinishedStandard = model.isLabelVisibleFinished();     

        this.labelFontUnfinishedStandard = model.getLabelFontUnfinished();
        this.labelFontFinishedStandard = model.getLabelFontFinished();

        this.paintLabelUnfinishedStandard = model.getPaintLabelUnfinished();
        this.paintLabelFinishedStandard = model.getPaintLabelFinished();

        this.strokeUnfinishedStandard = model.getStrokeUnfinished();
        this.strokeFinishedStandard = model.getStrokeFinished();

        this.paintUnfinishedStandard = model.getPaintUnfinished();
        this.paintFinishedStandard = model.getPaintFinished();

        resetEditor();
    }

    public void saveAsDefaults() 
    {	    
        model.saveStyleAsDefault();
    }

    public void applyChangesToAll() 
    {
        for(ProfileStyle style : boundedModels)
        {
            resetModel(style);
        }
    }

    public void undoChanges() 
    {
        setParametersToInitial();
        resetModel(model);
        resetEditor();
    }

    public Component getEditionSite()
    {
        return this;
    }

    private void updateActionsEnability()
    {
        boolean applyToAllEnabled = isApplyToAllEnabled();
        applyToAllAction.setEnabled(applyToAllEnabled);
    }

    public boolean isApplyToAllEnabled()
    {
        boolean enabled = !boundedModels.isEmpty();
        return enabled;
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {  
        Object source = evt.getSource();

        if(source == comboKnobOrientation)
        {
            KnobOrientation selectedItem = (KnobOrientation) comboKnobOrientation.getSelectedItem();        
            this.knobOrientation = selectedItem;
            model.setKnobOrientation(knobOrientation);
        }
        else if(source == comboLabelType)
        {
            LabelAutomaticType selectedItem = (LabelAutomaticType) comboLabelType.getSelectedItem();        
            this.labelType = selectedItem;
            model.setLabelType(labelType);
        }
        else
        {
            boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);

            if(source == boxKnobVisible)
            {
                this.knobVisible = selected;
                model.setKnobVisible(knobVisible);
            }
            else if(source == boxArrowheadUnfinishedStandard)
            {
                this.isArrowheadVisibleUnfinishedStandard = selected;
                model.setArrowheadVisibleUnfinishedStandard(isArrowheadVisibleUnfinishedStandard);
            }
            else if(source == boxArrowheadFinishedStandard)
            {
                this.isArrowheadVisibleFinishedStandard = selected;
                model.setArrowheadVisibleFinishedStandard(isArrowheadVisibleFinishedStandard);
            }
            else if(source == boxLabelVisibleUnfinishedStandard)
            {
                this.labelVisibleUnfinishedStandard = selected;
                model.setLabelVisibleUnfinishedStandard(labelVisibleUnfinishedStandard);
            }       
            else if(source == boxLabelVisibleFinishedStandard)
            {
                this.labelVisibleFinishedStandard = selected;
                model.setLabelVisibleFinishedStandard(labelVisibleFinishedStandard);        
            }
        }     
    }

    private JPanel buildUnfinishedPanel()
    {
        JPanel unfinishedPanel = new JPanel(new BorderLayout());
        SubPanel regularPanel = new SubPanel();

        regularPanel.addComponent(new JLabel("Label"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(boxLabelVisibleUnfinishedStandard, 1, 0, 6, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        regularPanel.addComponent(new JLabel("Font"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(fieldLabelFontUnfinishedStandard, 1, 1, 6, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectLabelFontUnfinishedStandard, 7, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        regularPanel.addComponent(new JLabel("Label color"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(paintLabelUnfinishedStandardSample, 1, 2, 6, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectPaintLabelUnfinishedStandard, 7, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        regularPanel.addComponent(new JLabel("Stroke"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(strokeUnfinishedStandardSample, 1, 3, 6, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonEditStrokeUnfinishedStandard, 7, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);


        regularPanel.addComponent(new JLabel("Arrowheads"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);

        regularPanel.addComponent(new JLabel(""), 1, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(boxArrowheadUnfinishedStandard, 2, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);      

        regularPanel.addComponent(new JLabel("Width"), 3, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(spinnerArrowheadWidthUnfinishedStandard, 4, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);          

        regularPanel.addComponent(new JLabel("Length"), 5, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(spinnerArrowheadLengthUnfinishedStandard, 6, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);                         


        unfinishedPanel.add(regularPanel, BorderLayout.NORTH);
        unfinishedPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 10));

        return unfinishedPanel;
    }

    private JPanel buildFinishedPanel()
    {
        JPanel finishedPanel = new JPanel(new BorderLayout());

        SubPanel regularPanel = new SubPanel();


        regularPanel.addComponent(new JLabel("Label"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(boxLabelVisibleFinishedStandard, 1, 0, 6, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        regularPanel.addComponent(new JLabel("Label type"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 0);
        regularPanel.addComponent(comboLabelType, 1, 1, 2, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);        

        regularPanel.addComponent(new JLabel("Font"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(fieldLabelFontFinishedStandard, 1, 2, 6, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectLabelFontFinishedStandard, 7, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        regularPanel.addComponent(new JLabel("Label color"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(paintLabelFinishedStandardSample, 1, 3, 6, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectPaintLabelFinishedStandard, 7, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        regularPanel.addComponent(new JLabel("Stroke"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(strokeFinishedStandardSample, 1, 4, 6, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonEditStrokeFinishedStandard, 7, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        regularPanel.addComponent(new JLabel("Arrowheads"), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);

        regularPanel.addComponent(new JLabel(""), 1, 5, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(boxArrowheadUnfinishedStandard, 2, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);      

        regularPanel.addComponent(new JLabel("Width"), 3, 5, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(spinnerArrowheadWidthFinishedStandard, 4, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);          

        regularPanel.addComponent(new JLabel("Length"), 5, 5, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.5, 0);
        regularPanel.addComponent(spinnerArrowheadLengthFinishedStandard, 6, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);                         


        regularPanel.addComponent(new JLabel("Knobs"), 0, 6, 1, 2, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);

        regularPanel.addComponent(new JLabel(""), 1, 6, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(boxKnobVisible, 2, 6, 1, 2, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);      

        regularPanel.addComponent(new JLabel("Width"), 3, 6, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(spinnerKnobWidth, 4, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);          

        regularPanel.addComponent(new JLabel("Length"), 5, 6, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.5, 0);
        regularPanel.addComponent(spinnerKnobHeight, 6, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);    

        regularPanel.addComponent(new JLabel("Position"), 1, 7, 3, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(comboKnobOrientation, 4, 7, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);

        finishedPanel.add(regularPanel, BorderLayout.NORTH);
        finishedPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 10));

        return finishedPanel;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        JButton buttonClose = new JButton(new CloseAction());

        JButton buttonApplyToAll = new JButton(applyToAllAction);

        JButton buttonSave = new JButton(new SaveAction());
        JButton buttonReset = new JButton(new ResetAction());
        JButton buttonUndo = new JButton(new UndoAction());

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonApplyToAll).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSave).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonReset).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonClose).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonUndo));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonApplyToAll)
                .addComponent(buttonSave)
                .addComponent(buttonReset)
                .addComponent(buttonClose)
                .addComponent(buttonUndo));

        layout.linkSize(buttonClose,buttonApplyToAll, buttonSave, buttonReset, buttonUndo);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return buttonPanel;
    }

    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object source = evt.getSource();

        if(source == spinnerKnobWidth)
        {
            knobWidth = ((SpinnerNumberModel)spinnerKnobWidth.getModel()).getNumber().intValue();
            model.setKnobWidth(knobWidth);
        }
        else if(source == spinnerKnobHeight)
        {
            knobHeight = ((SpinnerNumberModel)spinnerKnobHeight.getModel()).getNumber().intValue();
            model.setKnobHeight(knobHeight);
        }
        else if(source == spinnerArrowheadWidthUnfinishedStandard)
        {
            arrowheadWidthUnfinishedStandard = ((SpinnerNumberModel)spinnerArrowheadWidthUnfinishedStandard.getModel()).getNumber().floatValue();
            model.setArrowheadWidthUnfinishedStandard(arrowheadWidthUnfinishedStandard);
        }
        else if(source == spinnerArrowheadWidthFinishedStandard)
        {
            arrowheadWidthFinishedStandard = ((SpinnerNumberModel)spinnerArrowheadWidthFinishedStandard.getModel()).getNumber().floatValue();
            model.setArrowheadWidthFinishedStandard(arrowheadWidthFinishedStandard);
        }
        else if(source == spinnerArrowheadLengthUnfinishedStandard)
        {
            arrowheadLengthUnfinishedStandard = ((SpinnerNumberModel)spinnerArrowheadLengthUnfinishedStandard.getModel()).getNumber().floatValue();
            model.setArrowheadLengthUnfinishedStandard(arrowheadLengthUnfinishedStandard);
        }
        else if(source == spinnerArrowheadLengthFinishedStandard)
        {
            arrowheadLengthFinishedStandard = ((SpinnerNumberModel)spinnerArrowheadLengthFinishedStandard.getModel()).getNumber().floatValue();
            model.setArrowheadLengthFinishedStandard(arrowheadLengthFinishedStandard);
        }
    }


    private class ApplyToAllAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ApplyToAllAction()
        {
            //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(NAME,"Batch apply");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            applyChangesToAll();
            JOptionPane.showMessageDialog(ProfileEditor.this, "Profile style was applied to all charts of the same type", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class CloseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CloseAction()
        {
            //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME,"Close");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            hideRoot();
        }
    }

    private class UndoAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public UndoAction()
        {
            //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(NAME,"Undo");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            undoChanges();
        }
    }

    private class ResetAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ResetAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME,"Reset");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            resetToDefaults();
        }
    }

    private class SaveAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SaveAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME,"Save");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            saveAsDefaults();
            JOptionPane.showMessageDialog(ProfileEditor.this, "Profile style was changed", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);        
        };
    }

}
