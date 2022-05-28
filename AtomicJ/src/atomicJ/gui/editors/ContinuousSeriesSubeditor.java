
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

import atomicJ.gui.*;
import atomicJ.utilities.Validation;

public class ContinuousSeriesSubeditor implements PaintReceiver, MarkerStyleReceiver, SeriesSubeditor 
{	
    private final PreferredContinuousSeriesRendererStyle prefStyle;       
    private final String seriesName;
    private final ContinuousSeriesRenderer renderer;	
    private final List<ContinuousSeriesRenderer> boundedRenderers;

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

    private final Action selectPaintAction = new SelectPaintAction();
    private final Action selectMarkerShapeAction = new SelectMarkerShapeAction();
    private final Action editStrokeAction = new EditStrokeAction();

    private final Channel1DRendererDataImmutable initRendererData;
    private final Channel1DRendererDataMutable<Channel1DRendererData> model;
    private StrokeChooser lineStrokeChooser;

    private final SubPanel mainPanel = new SubPanel();

    public ContinuousSeriesSubeditor(ContinuousSeriesRenderer renderer, List<ContinuousSeriesRenderer> boundedRenderers) 
    {
        this.renderer = Validation.requireNonNullParameterName(renderer, "renderer");
        this.boundedRenderers = Validation.requireNonNullParameterName(boundedRenderers, "boundedRenderers");
        this.seriesName = renderer.getName();	
        this.prefStyle = PreferredContinuousSeriesRendererStyle.getInstance(renderer.getPreferences(), renderer.getStyleKey());

        this.initRendererData = renderer.getImmutableData();
        this.model = new Channel1DRendererDataMutable<>(initRendererData);

        resetEditor();

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

        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), seriesName));

        initItemListeners();
    }

    @Override
    public void setNameBorder(boolean named)
    {
        Border border = named ? BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), seriesName) : BorderFactory.createEtchedBorder();
        mainPanel.setBorder(border);
    }

    private void initItemListeners()
    {       
        boxItemMarked.addItemListener(new ItemListener() 
        {            
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                model.setBaseShapesVisible(selected);
                setEditorConsistentWithMarked();
                renderer.setBaseShapesVisible(selected);
            }
        });
        boxItemsJoined.addItemListener(new ItemListener()
        {            
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                model.setBaseLinesVisible(selected);
                setEditorConsistentWithJoined();
                renderer.setBaseLinesVisible(selected);
            }
        });
        boxSeriesVisible.addItemListener(new ItemListener() 
        {            
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                model.setBaseSeriesVisible(selected);
                renderer.setBaseSeriesVisible(selected);
            }
        });
        boxVisibleInLegend.addItemListener(new ItemListener() 
        {            
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                model.setBaseSeriesVisibleInLegend(selected);
                renderer.setBaseSeriesVisibleInLegend(selected);
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
        Channel1DRendererDataImmutable modelData = model.getImmutableVersion();
        for(ContinuousSeriesRenderer r: boundedRenderers)
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

        setEditorConsistentWithJoined();
        setEditorConsistentWithMarked();
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
        return boundedRenderers.size() > 1;
    }

    private void attemptPaintSelection() 
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

    private void attemptStrokeEdition(BasicStrokeReceiver strokeReceiver)
    {
        if(lineStrokeChooser == null)
        {
            lineStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(mainPanel), strokeReceiver);
        }

        lineStrokeChooser.showDialog();
    }

    @Override
    public void setPaint(Paint paint) {
        // TODO Auto-generated method stub

    }

    private class CustomStrokeReceiver implements BasicStrokeReceiver    
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
            attemptStrokeEdition(new CustomStrokeReceiver());
        }
    }
}
