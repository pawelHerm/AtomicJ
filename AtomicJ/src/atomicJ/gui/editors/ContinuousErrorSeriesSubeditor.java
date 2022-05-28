
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


import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.*;
import atomicJ.utilities.Validation;

public class ContinuousErrorSeriesSubeditor implements PaintReceiver, MarkerStyleReceiver, SeriesSubeditor 
{	
    private final PreferredContinuousSeriesErrorRendererStyle prefStyle;       
    private final String seriesName;
    private final Channel1DWithErrorRenderer renderer;	
    private final List<Channel1DWithErrorRenderer> boundedRenderers;

    private final JCheckBox boxSeriesVisible = new JCheckBox();
    private final JCheckBox boxVisibleInLegend = new JCheckBox();
    private final JCheckBox boxItemsJoined = new JCheckBox();
    private final JCheckBox boxItemMarked = new JCheckBox();

    private ShapeAndSizeChooser shapeChooser;
    private final PaintSampleFlexible markerPaintSample = new PaintSampleFlexible();		
    private final EditorShapeLabel labelShape = EditorShapeLabel.buildShapeLabel();
    private final JLabel labelStroke = new JLabel("Stroke");
    private final JLabel labelMarkerStyle = new JLabel("Marker style");
    private final JLabel labelMarkerPaint = new JLabel("Marker color");


    private final StraightStrokeSample lineStrokeSample = new StraightStrokeSample();

    private final JComboBox<ErrorBarDirection> comboBarDirection = new JComboBox<>(ErrorBarDirection.values());

    private final JLabel labelBarStroke = new JLabel("Bar stroke");
    private final StraightStrokeSample barStrokeSample = new StraightStrokeSample();
    private final Action editBarStrokeAction = new EditBarStrokeAction();

    private final JCheckBox boxCapVisible = new JCheckBox();
    private final JLabel labelCapStroke = new JLabel("Cap stroke");
    private final StraightStrokeSample capStrokeSample = new StraightStrokeSample();
    private final Action editCapStrokeAction = new EditCapStrokeAction();
    private final JSpinnerNumeric spinnerCapWidth = new JSpinnerNumeric(new SpinnerDoubleModel("Cap width must be a positive number", 1, 0, 1000, 1.0));

    private final Action selectPaintAction = new SelectPaintAction();
    private final Action selectMarkerShapeAction = new SelectMarkerShapeAction();
    private final Action editStrokeAction = new EditStrokeAction();

    private final Channel1DErrorRendererDataImmutable initRendererData;
    private final Channel1DErrorRendererDataMutable model;
    private StrokeChooser lineStrokeChooser;   
    private StrokeChooser barStrokeChooser;
    private StrokeChooser capStrokeChooser;

    private final SubPanel mainPanel = new SubPanel();

    public ContinuousErrorSeriesSubeditor(Channel1DWithErrorRenderer renderer, List<Channel1DWithErrorRenderer> boundedRenderers) 
    {
        this.renderer = Validation.requireNonNullParameterName(renderer, "renderer");
        this.boundedRenderers = Validation.requireNonNullParameterName(boundedRenderers, "boundedRenderers");
        this.seriesName = renderer.getName();	
        this.prefStyle = PreferredContinuousSeriesErrorRendererStyle.getInstance(renderer.getPreferences(), renderer.getStyleKey());

        this.initRendererData = renderer.getImmutableData();
        this.model = new Channel1DErrorRendererDataMutable(initRendererData);  

        mainPanel.addComponent(new JLabel("Show"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(boxSeriesVisible, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.25, 1);

        mainPanel.addComponent(new JLabel("Legend"), 2, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);
        mainPanel.addComponent(boxVisibleInLegend, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(new JLabel("Markers"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(boxItemMarked, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.25, 1);

        mainPanel.addComponent(new JLabel("Joined"), 2, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);
        mainPanel.addComponent(boxItemsJoined, 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(labelStroke, 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(lineStrokeSample, 1, 2, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(new JButton(editStrokeAction), 4, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.05, 1);

        mainPanel.addComponent(labelMarkerStyle, 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(labelShape.getLabel(), 1, 3, 3, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(new JButton(selectMarkerShapeAction), 4, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.05, 1);

        mainPanel.addComponent(labelMarkerPaint, 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(markerPaintSample, 1, 4, 3, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(new JButton(selectPaintAction), 4, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.05, 1);

        mainPanel.addComponent(Box.createVerticalStrut(20), 0, 5, 5, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);

        mainPanel.addComponent(new JLabel("Bar direction"), 0, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(comboBarDirection, 1, 6, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);       

        mainPanel.addComponent(labelBarStroke, 0, 7, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(barStrokeSample, 1, 7, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(new JButton(editBarStrokeAction), 4, 7, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.05, 1);

        mainPanel.addComponent(new JLabel("Show cap"), 0, 8, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(boxCapVisible, 1, 8, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.25, 1);

        mainPanel.addComponent(new JLabel("Cap width"), 0, 9, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(spinnerCapWidth, 1, 9, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        mainPanel.addComponent(labelCapStroke, 0, 10, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.05, 1);
        mainPanel.addComponent(capStrokeSample, 1, 10, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(new JButton(editCapStrokeAction), 4,10, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0.05, 1);

        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), seriesName));

        initChangeListeners();
        initItemListeners();

        resetEditor();
    }

    @Override
    public void setNameBorder(boolean named)
    {
        Border border = named ? BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), seriesName) : BorderFactory.createEtchedBorder();
        mainPanel.setBorder(border);
    }

    private void initChangeListeners()
    {
        spinnerCapWidth.addChangeListener(new ChangeListener() 
        {          
            @Override
            public void stateChanged(ChangeEvent evt)
            {
                double capWidthNew = ((Number)spinnerCapWidth.getValue()).doubleValue();

                model.setCapWidth(capWidthNew);
                renderer.setCapWidth(capWidthNew);
            }
        });
    }

    private void initItemListeners()
    {       
        boxItemMarked.addItemListener(new ItemListener() {            
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                model.setBaseShapesVisible(selected);
                setEditorConsistentWithMarked();
                renderer.setBaseShapesVisible(selected);
            }
        });
        boxItemsJoined.addItemListener(new ItemListener() {            
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                model.setBaseLinesVisible(selected);
                setEditorConsistentWithJoined();
                renderer.setBaseLinesVisible(selected);
            }
        });
        boxSeriesVisible.addItemListener(new ItemListener() {            
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                model.setBaseSeriesVisible(selected);
                renderer.setBaseSeriesVisible(selected);
            }
        });
        boxVisibleInLegend.addItemListener(new ItemListener() {            
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                model.setBaseSeriesVisibleInLegend(selected);
                renderer.setBaseSeriesVisibleInLegend(selected);
            }
        });

        boxCapVisible.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                model.setCapVisible(selected);
                setEditorConsistentWithCapVisible();
                renderer.setCapVisible(selected);
            }
        });

        comboBarDirection.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                ErrorBarDirection directionNew = comboBarDirection.getItemAt(comboBarDirection.getSelectedIndex());
                model.setErrorBarDrawingDirection(directionNew);
                renderer.setErrorBarDrawingDirection(directionNew);
            }
        });
    }	


    private void setEditorConsistentWithMarked()
    {
        boolean markersVisible = model.getBaseShapesVisible();

        labelMarkerStyle.setEnabled(markersVisible);
        labelShape.setEnabled(markersVisible);
        labelMarkerPaint.setEnabled(markersVisible);
        selectMarkerShapeAction.setEnabled(markersVisible);
        selectPaintAction.setEnabled(markersVisible);
        markerPaintSample.setEnabled(markersVisible);
    }

    private void setEditorConsistentWithJoined()
    {
        boolean lineVisible = model.getBaseLinesVisible(); 

        labelStroke.setEnabled(lineVisible);
        lineStrokeSample.setEnabled(lineVisible);
        editStrokeAction.setEnabled(lineVisible);
    }

    private void setEditorConsistentWithCapVisible()
    {
        boolean capVisible = model.isCapVisible(); 

        labelCapStroke.setEnabled(capVisible);
        capStrokeSample.setEnabled(capVisible);
        editCapStrokeAction.setEnabled(capVisible);
    }

    @Override
    public String getSubeditorName()
    {
        return seriesName;
    }

    @Override
    public void resetToDefaults()
    {
        model.readPreferredStyle(prefStyle, false);       
        renderer.setData(model.getImmutableVersion());

        resetEditor();
    }

    @Override
    public void saveAsDefaults()
    {
        prefStyle.saveToPreferences(model.getImmutableVersion());
    }

    @Override
    public void applyChangesToAll() 
    {
        Channel1DErrorRendererDataImmutable modelData = model.getImmutableVersion();
        for(Channel1DWithErrorRenderer r: boundedRenderers)
        {
            r.setData(modelData);
        }
    }

    @Override
    public void undoChanges() 
    {
        model.copyState(initRendererData, false);
        renderer.setData(model.getImmutableVersion());
        resetEditor();
    }

    @Override
    public Component getEditionComponent()
    {
        return mainPanel;
    }

    private void resetEditor()
    {
        boxSeriesVisible.setSelected(model.getBaseSeriesVisible());
        boxVisibleInLegend.setSelected(model.getBaseSeriesVisibleInLegend());
        boxItemsJoined.setSelected(model.getBaseLinesVisible());
        boxItemMarked.setSelected(model.getBaseShapesVisible());
        lineStrokeSample.setStroke(model.getBaseStroke());
        lineStrokeSample.setStrokePaint(model.getBasePaint());
        markerPaintSample.setPaint(model.getBaseFillPaint());
        labelShape.update(model.getBaseMarkerIndex(), model.getBaseMarkerSize(), model.getBaseFillPaint());

        comboBarDirection.setSelectedItem(model.getErrorBarDrawingDirection());
        boxCapVisible.setSelected(model.isCapVisible());
        barStrokeSample.setStroke(model.getBarStroke());
        barStrokeSample.setStrokePaint(model.getBarPaint());
        capStrokeSample.setStroke(model.getCapStroke());
        capStrokeSample.setStrokePaint(model.getCapPaint());

        spinnerCapWidth.setValue(Double.valueOf(model.getCapWidth()));

        setEditorConsistentWithJoined();
        setEditorConsistentWithMarked();
        setEditorConsistentWithCapVisible();
    }

    @Override
    public int getMarkerIndex()
    {
        return model.getBaseMarkerIndex();
    }

    @Override
    public void setMarkerIndex(int markerIndex) 
    {
        model.setBaseMarkerIndex(markerIndex);	        	
        renderer.setBaseMarkerIndex(markerIndex);
        labelShape.update(model.getBaseMarkerIndex(), model.getBaseMarkerSize(), model.getBaseFillPaint());
    }

    @Override
    public float getMarkerSize()
    {
        return model.getBaseMarkerSize();
    }

    @Override
    public void setMarkerSize(float markerSize) 
    {
        model.setBaseMarkerSize(markerSize);
        renderer.setBaseMarkerSize(markerSize);

        labelShape.update(model.getBaseMarkerIndex(), model.getBaseMarkerSize(), model.getBaseFillPaint());
    }

    @Override
    public Paint getPaint() 
    {
        return model.getBaseFillPaint();
    }

    @Override
    public Paint getMarkerFillPaint()
    {
        return model.getBaseFillPaint();
    }

    @Override
    public boolean getDrawMarkerOutline()
    {
        return false;
    }

    @Override
    public Stroke getMarkerOutlineStroke()
    {
        return null;
    } 

    @Override
    public Paint getMarkerOutlinePaint()
    {
        return null;
    }   

    @Override
    public boolean isApplyToAllEnabled()
    {        
        return boundedRenderers.size()>1;
    }

    private void attemptMarkerPaintSelection() 
    {
        Paint markerFillPaint = model.getBaseFillPaint();
        Paint p = markerFillPaint;
        Color defaultColor = (p instanceof Color ? (Color) p : Color.blue);
        Color c = JColorChooser.showDialog(mainPanel, "Color", defaultColor);
        if (c != null) 
        {
            markerPaintSample.setPaint(c);
            model.setBaseFillPaint(c);
            renderer.setBaseFillPaint(c);
            labelShape.update(model.getBaseMarkerIndex(), model.getBaseMarkerSize(), model.getBaseFillPaint());
        }
    }

    private void attemptShapeSelection() 
    {
        if(shapeChooser == null)
        {
            shapeChooser = new ShapeAndSizeChooser(SwingUtilities.getWindowAncestor(mainPanel), this, PlotStyleUtilities.getNonZeroAreaShapes());
        }
        shapeChooser.setVisible(true);
    }

    private void attemptStrokeEdition()
    {
        if(lineStrokeChooser == null)
        {
            lineStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(mainPanel), new CustomBaseStrokeReceiver());
        }

        lineStrokeChooser.showDialog();
    }


    private void attemptCapStrokeEdition()
    {
        if(capStrokeChooser == null)
        {
            capStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(mainPanel), new CapStrokeReceiver());
        }

        capStrokeChooser.showDialog();
    }

    private void attemptBarStrokeEdition()
    {
        if(barStrokeChooser == null)
        {
            barStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(mainPanel), new BarStrokeReceiver());
        }

        barStrokeChooser.showDialog();
    }


    @Override
    public void setPaint(Paint paint) {
        // TODO Auto-generated method stub

    }

    private class CustomBaseStrokeReceiver implements BasicStrokeReceiver    
    {
        @Override
        public BasicStroke getStroke() 
        {
            return (BasicStroke)model.getBaseStroke();
        }

        @Override
        public void setStroke(BasicStroke stroke) 
        {
            model.setBaseStroke(stroke);
            lineStrokeSample.setStroke(stroke);
            renderer.setBaseStroke(stroke);
        }

        @Override
        public Paint getStrokePaint() 
        {
            return model.getBasePaint();
        }

        @Override
        public void setStrokePaint(Paint paint) 
        {
            model.setBasePaint(paint);
            lineStrokeSample.setStrokePaint(paint);
            renderer.setBasePaint(paint);
        }           
    }


    private class BarStrokeReceiver implements BasicStrokeReceiver    
    {
        @Override
        public BasicStroke getStroke() 
        {
            return (BasicStroke)model.getBarStroke();
        }

        @Override
        public void setStroke(BasicStroke stroke) 
        {
            model.setBarStroke(stroke);
            barStrokeSample.setStroke(stroke);
            renderer.setBarStroke(stroke);
        }

        @Override
        public Paint getStrokePaint() 
        {
            return model.getBarPaint();
        }

        @Override
        public void setStrokePaint(Paint paint) 
        {
            model.setBarPaint(paint);
            barStrokeSample.setStrokePaint(paint);
            renderer.setBarPaint(paint);
        }           
    }

    private class CapStrokeReceiver implements BasicStrokeReceiver    
    {
        @Override
        public BasicStroke getStroke() 
        {
            return (BasicStroke)model.getCapStroke();
        }

        @Override
        public void setStroke(BasicStroke stroke) 
        {            
            model.setCapStroke(stroke);
            capStrokeSample.setStroke(stroke);
            renderer.setCapStroke(stroke);
        }

        @Override
        public Paint getStrokePaint() 
        {
            return model.getCapPaint();
        }

        @Override
        public void setStrokePaint(Paint paint) 
        {
            model.setCapPaint(paint);
            capStrokeSample.setStrokePaint(paint);
            renderer.setCapPaint(paint);
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
            attemptMarkerPaintSelection();
        }
    }

    private class SelectMarkerShapeAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public SelectMarkerShapeAction()
        {
            putValue(NAME, "Select");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            attemptShapeSelection();
        }
    }

    private class EditStrokeAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public EditStrokeAction()
        {
            putValue(NAME, "Edit");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            attemptStrokeEdition();
        }
    }

    private class EditCapStrokeAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public EditCapStrokeAction()
        {
            putValue(NAME, "Edit");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            attemptCapStrokeEdition();
        }
    }

    private class EditBarStrokeAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public EditBarStrokeAction()
        {
            putValue(NAME, "Edit");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            attemptBarStrokeEdition();
        }
    }
}
