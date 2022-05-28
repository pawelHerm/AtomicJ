
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

package atomicJ.gui.measurements;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import atomicJ.gui.AtomicJ;
import atomicJ.gui.BasicStrokeReceiver;
import atomicJ.gui.FontField;
import atomicJ.gui.FontReceiver;
import atomicJ.gui.NumericFormatSelectionPanel;
import atomicJ.gui.PaintSampleFlexible;
import atomicJ.gui.StandardNumericalFormatStyle.FormattableNumericalDataState;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.SubPanel;
import atomicJ.gui.editors.FontChooserDialog;

import java.util.ArrayList;
import java.util.List;



public class DistanceMeasurementEditor extends JDialog implements ActionListener, ItemListener
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

    private boolean initDrawAbscissaUnfinished;
    private boolean initDrawAbscissaFinished;

    private boolean initDrawOrdinateUnfinished;
    private boolean initDrawOrdinateFinished;


    private float initLabelOffset;
    private float initLabelLengthwisePosition;

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

    private FormattableNumericalDataState initFormatFinished;
    private FormattableNumericalDataState initFormatUnfinished;

    //current settings

    private boolean outlineVisible;

    private boolean drawAbscissaUnfinished;
    private boolean drawAbscissaFinished;

    private boolean drawOrdinateUnfinished;
    private boolean drawOrdinateFinished;

    private float labelOffset;
    private float labelLengthwisePosition;

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


    private FormattableNumericalDataState formatFinished;
    private FormattableNumericalDataState formatUnfinished;

    //GUI components

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

    private final JCheckBox boxDrawAbscissaUnfinishedStandard = new JCheckBox();
    private final JCheckBox boxDrawAbscissaFinishedStandard = new JCheckBox();

    private final JCheckBox boxDrawOrdinateUnfinishedStandard = new JCheckBox();
    private final JCheckBox boxDrawOrdinateFinishedStandard = new JCheckBox();

    private final JCheckBox boxLabelVisibleUnfinishedStandard = new JCheckBox();
    private final JCheckBox boxLabelVisibleFinishedStandard = new JCheckBox();


    private StrokeChooser chooserStrokeUnfinishedStandard;
    private StrokeChooser chooserStrokeFinishedStandard;

    private final NumericFormatSelectionPanel panelFormatFinished = new NumericFormatSelectionPanel();
    private final NumericFormatSelectionPanel panelFormatUnfinished = new NumericFormatSelectionPanel();

    private final BatchApplyAction batchApplyAction = new BatchApplyAction();

    private FontChooserDialog fontChooserDialog;

    private DistanceMeasurementStyle model;
    private final List<DistanceMeasurementStyle> boundedModels = new ArrayList<>();

    public DistanceMeasurementEditor(Window parent) 
    {   
        super(parent, "Measurement style", ModalityType.APPLICATION_MODAL);

        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        Component unfinishedPanel = buildUnfinishedPanel();  
        Component finishedPanel = buildFinishedPanel();       

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

    public void setModel(DistanceMeasurementStyle model)
    {
        this.model = model;

        this.initDrawAbscissaUnfinished = model.isDrawAbscissaMeasurementUnfinished();
        this.initDrawAbscissaFinished = model.isDrawAbscissaMeasurementFinished();
        this.initDrawOrdinateUnfinished = model.isDrawOrdinateMeasurementUnfinished();
        this.initDrawOrdinateFinished = model.isDrawOrdinateMeasurementFinished();

        this.initLabelOffset = model.getLabelOffset();
        this.initLabelLengthwisePosition = model.getLabelLengthwisePosition();

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

        this.initFormatFinished = model.getDecimalFormatManagerFinished().getState();
        this.initFormatUnfinished = model.getDecomalFormatManagerUnfinished().getState();

        this.panelFormatFinished.setFormattableData(model.getDecimalFormatManagerFinished());
        this.panelFormatUnfinished.setFormattableData(model.getDecomalFormatManagerUnfinished());

        setParametersToInitial();        
        resetEditor();
    }    

    public void setBoundedModels(List<DistanceMeasurementStyle> models)
    {
        boundedModels.clear();
        boundedModels.addAll(models);

        updateActionsEnability();
    }

    private void setParametersToInitial()
    {
        this.outlineVisible = initOutlineVisible;

        this.labelOffset = initLabelOffset;
        this.labelLengthwisePosition = initLabelLengthwisePosition;

        this.drawAbscissaUnfinished = initDrawAbscissaUnfinished;
        this.drawAbscissaFinished = initDrawAbscissaFinished;

        this.drawOrdinateUnfinished = initDrawOrdinateUnfinished;
        this.drawOrdinateFinished = initDrawOrdinateFinished;

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

        this.formatFinished = initFormatFinished;
        this.formatUnfinished = initFormatUnfinished;
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

    private void initItemListener()
    {
        boxDrawAbscissaUnfinishedStandard.addItemListener(this);
        boxDrawAbscissaFinishedStandard.addItemListener(this);
        boxDrawOrdinateUnfinishedStandard.addItemListener(this);
        boxDrawOrdinateFinishedStandard.addItemListener(this);

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
        this.boxDrawAbscissaUnfinishedStandard.setSelected(drawAbscissaUnfinished);
        this.boxDrawAbscissaFinishedStandard.setSelected(drawAbscissaFinished);
        this.boxDrawOrdinateUnfinishedStandard.setSelected(drawOrdinateUnfinished);
        this.boxDrawOrdinateFinishedStandard.setSelected(drawOrdinateFinished);

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

    private void resetModel(DistanceMeasurementStyle model)
    {    	
        model.setDrawAbsicssaMeasurementUnfinished(drawAbscissaUnfinished);
        model.setDrawAbsicssaMeasurementFinished(drawAbscissaFinished);
        model.setDrawOrdinateMeasurementUnfinished(drawOrdinateUnfinished);
        model.setDrawOrdinateMeasurementFinished(drawOrdinateFinished);

        model.setLabelOffset(labelOffset);
        model.setLabelLengthwisePosition(labelLengthwisePosition);

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

        model.getDecimalFormatManagerFinished().setState(formatFinished);
        model.getDecomalFormatManagerUnfinished().setState(formatUnfinished);
    }

    public void resetToDefaults() 
    {
        this.drawAbscissaUnfinished = model.isDrawAbscissaMeasurementUnfinished();
        this.drawAbscissaFinished = model.isDrawAbscissaMeasurementFinished();
        this.drawOrdinateUnfinished = model.isDrawOrdinateMeasurementUnfinished();
        this.drawOrdinateFinished = model.isDrawOrdinateMeasurementFinished();

        this.labelOffset = model.getLabelOffset();
        this.labelLengthwisePosition = model.getLabelLengthwisePosition();

        model.setDefaultStyle();

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
        this.formatFinished = model.getDecimalFormatManagerFinished().getState();
        this.formatUnfinished = model.getDecomalFormatManagerUnfinished().getState();

        resetEditor();
    }

    public void saveAsDefaults() 
    {	    
        model.saveStyleAsDefault();
    }

    public void applyChangesToAll() 
    {
        for(DistanceMeasurementStyle style : boundedModels)
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
        batchApplyAction.setEnabled(applyToAllEnabled);
    }

    public boolean isApplyToAllEnabled()
    {
        return !boundedModels.isEmpty();
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
        Object source = evt.getSource();

        if(source == boxDrawAbscissaUnfinishedStandard)
        {
            this.drawAbscissaUnfinished = selected;
            model.setDrawAbsicssaMeasurementUnfinished(drawAbscissaUnfinished);
        }
        else if(source == boxDrawAbscissaFinishedStandard)
        {
            this.drawAbscissaFinished = selected;
            model.setDrawAbsicssaMeasurementFinished(drawAbscissaFinished);
        }
        else if(source == boxDrawOrdinateUnfinishedStandard)
        {
            this.drawOrdinateUnfinished = selected;
            model.setDrawOrdinateMeasurementUnfinished(drawOrdinateUnfinished);
        }
        else if(source == boxDrawOrdinateFinishedStandard)
        {
            this.drawOrdinateFinished = selected;
            model.setDrawOrdinateMeasurementFinished(drawOrdinateFinished);
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

    private Component buildUnfinishedPanel()
    {
        JTabbedPane pane = new JTabbedPane();

        JPanel generalPanel = new JPanel(new BorderLayout());
        SubPanel regularPanel = new SubPanel();

        regularPanel.addComponent(new JLabel("Labels"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(boxLabelVisibleUnfinishedStandard, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        regularPanel.addComponent(new JLabel("X distance"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(boxDrawAbscissaUnfinishedStandard, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);       

        regularPanel.addComponent(new JLabel("Y distance"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(boxDrawOrdinateUnfinishedStandard, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);       

        regularPanel.addComponent(new JLabel("Font"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(fieldLabelFontUnfinishedStandard, 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectLabelFontUnfinishedStandard, 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        regularPanel.addComponent(new JLabel("Label color"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(paintLabelUnfinishedStandardSample, 1, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectPaintLabelUnfinishedStandard, 2, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        regularPanel.addComponent(new JLabel("Stroke"), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(strokeUnfinishedStandardSample, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonEditStrokeUnfinishedStandard, 2, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        generalPanel.add(regularPanel, BorderLayout.NORTH);
        generalPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 10));

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(panelFormatUnfinished, BorderLayout.NORTH);
        labelPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 10));

        pane.add("General", generalPanel);
        pane.add("Label", labelPanel);


        return pane;
    }

    private Component buildFinishedPanel()
    {
        JTabbedPane pane = new JTabbedPane();

        JPanel generalPanel = new JPanel(new BorderLayout());

        SubPanel regularPanel = new SubPanel();

        regularPanel.addComponent(new JLabel("Label"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(boxLabelVisibleFinishedStandard, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        regularPanel.addComponent(new JLabel("X distance"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(boxDrawAbscissaFinishedStandard, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);       

        regularPanel.addComponent(new JLabel("Y distance"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(boxDrawOrdinateFinishedStandard, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);       

        regularPanel.addComponent(new JLabel("Font"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(fieldLabelFontFinishedStandard, 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectLabelFontFinishedStandard, 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        regularPanel.addComponent(new JLabel("Label color"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(paintLabelFinishedStandardSample, 1, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonSelectPaintLabelFinishedStandard, 2, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        regularPanel.addComponent(new JLabel("Stroke"), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        regularPanel.addComponent(strokeFinishedStandardSample, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        regularPanel.addComponent(buttonEditStrokeFinishedStandard, 2, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        generalPanel.add(regularPanel, BorderLayout.NORTH);
        generalPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 10));

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(panelFormatFinished, BorderLayout.NORTH);
        labelPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 10));

        pane.add("General", generalPanel);
        pane.add("Label", labelPanel);


        return pane;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        JButton buttonClose = new JButton(new CloseAction());

        JButton buttonApplyToAll = new JButton(batchApplyAction);
        buttonApplyToAll.setEnabled(isApplyToAllEnabled());
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


    private class BatchApplyAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public BatchApplyAction()
        {
            //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(NAME,"Batch apply");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            applyChangesToAll();
            JOptionPane.showMessageDialog(DistanceMeasurementEditor.this, "Distance measurement style was applied to all charts of the same type", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(DistanceMeasurementEditor.this, "Default measurement style was changed", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);        };
    }
}
