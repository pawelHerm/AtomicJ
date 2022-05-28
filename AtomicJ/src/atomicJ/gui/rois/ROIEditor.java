
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

package atomicJ.gui.rois;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import atomicJ.gui.LabelAutomaticType;
import atomicJ.gui.AtomicJ;
import atomicJ.gui.BasicStrokeReceiver;
import atomicJ.gui.FontField;
import atomicJ.gui.FontReceiver;
import atomicJ.gui.PaintSampleFlexible;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.SubPanel;
import atomicJ.gui.editors.FontChooserDialog;



public class ROIEditor extends JDialog implements ActionListener, ItemListener
{
    private static final long serialVersionUID = 1L;

    private static final String SELECT_LABEL_FONT_UNFINISHED_STANDARD = "SELECT_LABEL_FONT_UNFINISHED_STANDARD";
    private static final String SELECT_LABEL_FONT_FINISHED_STANDARD = "SELECT_LABEL_FONT_FINISHED_STANDARD";

    private static final String EDIT_STROKE_UNFINISHED_STANDARD = "EDIT_STROKE_UNFINISHED_STANDARD";
    private static final String EDIT_STROKE_FINISHED_STANDARD = "EDIT_STROKE_FINISHED_STANDARD";

    private static final String SELECT_PAINT_LABEL_UNFINISHED_STANDARD = "SELECT_PAINT_LABEL_UNFINISHED_STANDARD";
    private static final String SELECT_PAINT_LABEL_FINISHED_STANDARD = "SELECT_PAINT_LABEL_FINISHED_STANDARD";

    private static final String SELECT_PAINT_FILL_UNFINISHED_STANDARD = "SELECT_PAINT_FILL_UNFINISHED_STANDARD";
    private static final String SELECT_PAINT_FILL_FINISHED_STANDARD = "SELECT_PAINT_FILL_FINISHED_STANDARD";

    //initial settings

    private boolean initOutlineVisible;

    private LabelAutomaticType initLabelType;
    private float initLabelOffset;
    private float initLabelLengthwisePosition;

    private boolean initIsFilledUnfinishedStandard;
    private boolean initIsFilledFinishedStandard;

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

    private Paint initPaintFillFinishedStandard;
    private Paint initPaintFillUnfinishedStandard; 


    //current settings

    private boolean outlineVisible;

    private LabelAutomaticType labelType;
    private float labelOffset;
    private float labelLengthwisePosition;

    private boolean isFilledUnfinishedStandard;
    private boolean isFilledFinishedStandard;

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

    private Paint paintFillFinishedStandard;
    private Paint paintFillUnfinishedStandard;


    //GUI components

    private final JComboBox<LabelAutomaticType> comboLabelType = new JComboBox<>(LabelAutomaticType.values());

    private final StraightStrokeSample strokeUnfinishedStandardSample = new StraightStrokeSample();
    private final StraightStrokeSample strokeFinishedStandardSample = new StraightStrokeSample();

    private final PaintSampleFlexible paintLabelUnfinishedStandardSample = new PaintSampleFlexible();
    private final PaintSampleFlexible paintLabelFinishedStandardSample = new PaintSampleFlexible();

    private final PaintSampleFlexible paintFillFinishedStandardSample = new PaintSampleFlexible();
    private final PaintSampleFlexible paintFillUnfinishedStandardSample = new PaintSampleFlexible();

    private final FontField fieldLabelFontUnfinishedStandard = new FontField();
    private final FontField fieldLabelFontFinishedStandard = new FontField();

    private final JButton buttonSelectLabelFontUnfinishedStandard = new JButton("Select");
    private final JButton buttonSelectLabelFontFinishedStandard = new JButton("Select");

    private final JButton buttonEditStrokeUnfinishedStandard = new JButton("Edit");
    private final JButton buttonEditStrokeFinishedStandard = new JButton("Edit");

    private final JButton buttonSelectPaintLabelUnfinishedStandard = new JButton("Select");
    private final JButton buttonSelectPaintLabelFinishedStandard = new JButton("Select");

    private final JButton buttonSelectPaintFillUnfinishedSandard = new JButton("Select");
    private final JButton buttonSelectPaintFillFinishedStandard = new JButton("Select");

    private final JCheckBox boxFilledUnfinishedStandard = new JCheckBox();
    private final JCheckBox boxFilledFinishedStandard = new JCheckBox();

    private final JCheckBox boxLabelVisibleUnfinishedStandard = new JCheckBox();
    private final JCheckBox boxLabelVisibleFinishedStandard = new JCheckBox();

    private StrokeChooser chooserStrokeUnfinishedStandard;
    private StrokeChooser chooserStrokeFinishedStandard;

    private FontChooserDialog fontChooserDialog;

    private final ApplyToAllAction applyToAllAction = new ApplyToAllAction();

    private ROIStyle model;
    private final List<ROIStyle> boundedModels = new ArrayList<>();

    private final boolean modelsFullyBounded;


    public ROIEditor(Window parent) 
    {   
        this(parent, true);
    }

    public ROIEditor(Window parent, boolean modelsFullyBounded) 
    {   
        super(parent, "ROI style", ModalityType.APPLICATION_MODAL);

        this.modelsFullyBounded = modelsFullyBounded;

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
        initItemListener();

        pack();
        setLocationRelativeTo(parent);
    }

    private void updateActionsEnability()
    {
        boolean applyToAllEnabled = isApplyToAllEnabled();

        applyToAllAction.setEnabled(applyToAllEnabled);
    }

    public void setModel(ROIStyle model)
    {
        this.model = model;

        this.initOutlineVisible = model.getOutlineVisible();

        this.initLabelType = model.getLabelType();
        this.initLabelOffset = model.getLabelOffset();
        this.initLabelLengthwisePosition = model.getLabelLengthwisePosition();

        this.initIsFilledUnfinishedStandard = model.isFilledUnfinishedStandard();
        this.initIsFilledFinishedStandard = model.isFilledFinishedStandard();

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

        this.initPaintFillUnfinishedStandard = model.getPaintFillUnfinishedStandard(); 
        this.initPaintFillFinishedStandard = model.getPaintFillFinishedStandard();

        setParametersToInitial();        
        resetEditor();
    }

    protected List<ROIStyle> getBoundedModels()
    {
        return boundedModels;
    }   

    public void setBoundedModels(List<ROIStyle> models)
    {
        boundedModels.clear();
        boundedModels.addAll(models);

        updateActionsEnability();
    }


    private void setParametersToInitial()
    {
        this.outlineVisible = initOutlineVisible;

        this.labelType = initLabelType;
        this.labelOffset = initLabelOffset;
        this.labelLengthwisePosition = initLabelLengthwisePosition;

        this.isFilledUnfinishedStandard = initIsFilledUnfinishedStandard;
        this.isFilledFinishedStandard = initIsFilledFinishedStandard;

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

        this.paintFillUnfinishedStandard = initPaintFillUnfinishedStandard; 
        this.paintFillFinishedStandard = initPaintFillFinishedStandard;
    }

    private void initActionListener()
    {
        buttonSelectLabelFontUnfinishedStandard.setActionCommand(SELECT_LABEL_FONT_UNFINISHED_STANDARD);
        buttonSelectLabelFontFinishedStandard.setActionCommand(SELECT_LABEL_FONT_FINISHED_STANDARD);

        buttonEditStrokeUnfinishedStandard.setActionCommand(EDIT_STROKE_UNFINISHED_STANDARD);
        buttonEditStrokeFinishedStandard.setActionCommand(EDIT_STROKE_FINISHED_STANDARD);

        buttonSelectPaintLabelUnfinishedStandard.setActionCommand(SELECT_PAINT_LABEL_UNFINISHED_STANDARD);
        buttonSelectPaintLabelFinishedStandard.setActionCommand(SELECT_PAINT_LABEL_FINISHED_STANDARD);

        buttonSelectPaintFillUnfinishedSandard.setActionCommand(SELECT_PAINT_FILL_UNFINISHED_STANDARD);
        buttonSelectPaintFillFinishedStandard.setActionCommand(SELECT_PAINT_FILL_FINISHED_STANDARD);

        buttonSelectLabelFontUnfinishedStandard.addActionListener(this);
        buttonSelectLabelFontFinishedStandard.addActionListener(this);

        buttonEditStrokeUnfinishedStandard.addActionListener(this);
        buttonEditStrokeFinishedStandard.addActionListener(this);

        buttonSelectPaintLabelUnfinishedStandard.addActionListener(this);
        buttonSelectPaintLabelFinishedStandard.addActionListener(this);

        buttonSelectPaintFillUnfinishedSandard.addActionListener(this);
        buttonSelectPaintFillFinishedStandard.addActionListener(this);
    }

    private void initItemListener()
    {
        comboLabelType.addItemListener(this);

        boxFilledUnfinishedStandard.addItemListener(this);
        boxFilledFinishedStandard.addItemListener(this);

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
        else if(SELECT_PAINT_FILL_UNFINISHED_STANDARD.equals(command))
        {
            Paint paintFillUnfinishedStandardNew = JColorChooser.showDialog(this, "Fill color", (Color)paintFillUnfinishedStandard);	        
            if (paintFillUnfinishedStandardNew != null) 
            {
                paintFillUnfinishedStandard = paintFillUnfinishedStandardNew;
                paintFillUnfinishedStandardSample.setPaint(paintFillUnfinishedStandardNew);
                model.setPaintFillUnfinishedStandard(paintFillUnfinishedStandardNew);		
            }
        }
        else if(SELECT_PAINT_FILL_FINISHED_STANDARD.equals(command))
        {
            Paint paintFillFinishedStandardNew = JColorChooser.showDialog(this, "Fill color", (Color)paintFillFinishedStandard);	        
            if (paintFillFinishedStandardNew != null) 
            {
                paintFillFinishedStandard = paintFillFinishedStandardNew;
                paintFillFinishedStandardSample.setPaint(paintFillFinishedStandardNew);
                model.setPaintFillFinishedStandard(paintFillFinishedStandardNew);		
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
        this.comboLabelType.setSelectedItem(labelType);

        this.fieldLabelFontUnfinishedStandard.setDisplayFont(labelFontUnfinishedStandard);
        this.fieldLabelFontFinishedStandard.setDisplayFont(labelFontFinishedStandard);

        this.paintLabelUnfinishedStandardSample.setPaint(paintLabelUnfinishedStandard);
        this.paintLabelFinishedStandardSample.setPaint(paintLabelFinishedStandard);

        this.paintFillFinishedStandardSample.setPaint(paintFillFinishedStandard);
        this.paintFillUnfinishedStandardSample.setPaint(paintFillUnfinishedStandard);       

        this.strokeUnfinishedStandardSample.setStroke(strokeUnfinishedStandard);
        this.strokeFinishedStandardSample.setStroke(strokeFinishedStandard);

        this.strokeUnfinishedStandardSample.setStrokePaint(paintUnfinishedStandard);
        this.strokeFinishedStandardSample.setStrokePaint(paintFinishedStandard);

        this.boxLabelVisibleUnfinishedStandard.setSelected(labelVisibleUnfinishedStandard);
        this.boxLabelVisibleFinishedStandard.setSelected(labelVisibleFinishedStandard);      

        this.boxFilledUnfinishedStandard.setSelected(isFilledUnfinishedStandard);
        this.boxFilledFinishedStandard.setSelected(isFilledFinishedStandard);
    }

    public void resetPartiallyModel(ROIStyle model)
    {
        model.setOutlineVisible(outlineVisible);

        model.setLabelType(labelType);
        model.setLabelOffset(labelOffset);
        model.setLabelLengthwisePosition(labelLengthwisePosition);

        model.setFilledUnfinishedStandard(isFilledUnfinishedStandard);
        model.setFilledFinishedStandard(isFilledFinishedStandard);

        model.setLabelVisibleUnfinishedStandard(labelVisibleUnfinishedStandard);    
        model.setLabelVisibleFinishedStandard(labelVisibleFinishedStandard);     

        model.setLabelFontUnfinishedStandard(labelFontUnfinishedStandard);
        model.setLabelFontFinishedStandard(labelFontFinishedStandard);

        model.setStrokeUnfinishedStandard(strokeUnfinishedStandard);
        model.setStrokeFinishedStandard(strokeFinishedStandard);
    }

    public void resetFullyModel(ROIStyle model)
    {    	
        model.setOutlineVisible(outlineVisible);

        model.setLabelType(labelType);
        model.setLabelOffset(labelOffset);
        model.setLabelLengthwisePosition(labelLengthwisePosition);

        model.setFilledUnfinishedStandard(isFilledUnfinishedStandard);
        model.setFilledFinishedStandard(isFilledFinishedStandard);

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

        model.setPaintFillUnfinishedStandard(paintFillUnfinishedStandard); 
        model.setPaintFillFinishedStandard(paintFillFinishedStandard);
    }

    public void resetToDefaults() 
    {		
        this.outlineVisible = model.getOutlineVisible();

        this.labelType = model.getLabelType();
        this.labelOffset = model.getLabelOffset();
        this.labelLengthwisePosition = model.getLabelLengthwisePosition();
        model.setDefaultStyle();

        this.isFilledUnfinishedStandard = model.isFilledUnfinishedStandard();
        this.isFilledFinishedStandard = model.isFilledFinishedStandard();

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

        this.paintFillUnfinishedStandard = model.getPaintFillUnfinishedStandard(); 
        this.paintFillFinishedStandard = model.getPaintFinished();

        resetEditor();
    }

    public void saveAsDefaults() 
    {	    
        model.saveStyleAsDefault();
    }

    public void applyChangesToAll() 
    {
        if(modelsFullyBounded)
        {
            for(ROIStyle style : boundedModels)
            {
                resetFullyModel(style);
            }
        }
        else
        {
            for(ROIStyle style : boundedModels)
            {
                resetPartiallyModel(style);
            }
        }
    }

    public void undoChanges() 
    {
        setParametersToInitial();
        resetFullyModel(model);
        resetEditor();
    }

    public Component getEditionSite()
    {
        return this;
    }

    public boolean isApplyToAllEnabled()
    {
        return !boundedModels.isEmpty();
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);
        Object source = evt.getSource();

        if(source == boxFilledUnfinishedStandard)
        {
            this.isFilledUnfinishedStandard = selected;
            model.setFilledUnfinishedStandard(isFilledUnfinishedStandard);
        }
        else if(source == boxFilledFinishedStandard)
        {		    		    
            this.isFilledFinishedStandard = selected;
            model.setFilledFinishedStandard(isFilledFinishedStandard);
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
        else if(source == comboLabelType)
        {
            LabelAutomaticType selectedItem = (LabelAutomaticType) comboLabelType.getSelectedItem();        
            this.labelType = selectedItem;
            model.setLabelType(labelType);
        }
    }

    private JPanel buildUnfinishedPanel()
    {
        JPanel unfinishedPanel = new JPanel(new BorderLayout());


        SubPanel regularPanel = new SubPanel();

        regularPanel.addComponent(new JLabel("Label"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(boxLabelVisibleUnfinishedStandard, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        regularPanel.addComponent(new JLabel("Font"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(fieldLabelFontUnfinishedStandard, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectLabelFontUnfinishedStandard, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        regularPanel.addComponent(new JLabel("Label color"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(paintLabelUnfinishedStandardSample, 1, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectPaintLabelUnfinishedStandard, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        regularPanel.addComponent(new JLabel("Filled"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(boxFilledUnfinishedStandard, 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        regularPanel.addComponent(new JLabel("Fill color"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(paintFillUnfinishedStandardSample, 1, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectPaintFillUnfinishedSandard, 2, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        regularPanel.addComponent(new JLabel("Stroke"), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(strokeUnfinishedStandardSample, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonEditStrokeUnfinishedStandard, 2, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);


        unfinishedPanel.add(regularPanel, BorderLayout.NORTH);
        unfinishedPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 10));

        return unfinishedPanel;
    }

    private JPanel buildFinishedPanel()
    {
        JPanel finishedPanel = new JPanel(new BorderLayout());


        SubPanel regularPanel = new SubPanel();

        regularPanel.addComponent(new JLabel("Label"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(boxLabelVisibleFinishedStandard, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        regularPanel.addComponent(new JLabel("Label type"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 0);
        regularPanel.addComponent(comboLabelType, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);        

        regularPanel.addComponent(new JLabel("Font"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(fieldLabelFontFinishedStandard, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectLabelFontFinishedStandard, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        regularPanel.addComponent(new JLabel("Label color"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(paintLabelFinishedStandardSample, 1, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectPaintLabelFinishedStandard, 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        regularPanel.addComponent(new JLabel("Filled"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(boxFilledFinishedStandard, 1, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        regularPanel.addComponent(new JLabel("Fill color"), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(paintFillFinishedStandardSample, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectPaintFillFinishedStandard, 2, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        regularPanel.addComponent(new JLabel("Stroke"), 0, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(strokeFinishedStandardSample, 1, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonEditStrokeFinishedStandard, 2, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

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
            JOptionPane.showMessageDialog(ROIEditor.this, "ROI style was applied to all charts of the same type", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(ROIEditor.this, "ROI style was changed", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);        
        };
    }

}
