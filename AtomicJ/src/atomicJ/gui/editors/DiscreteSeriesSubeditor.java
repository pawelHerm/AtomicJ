
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


public class DiscreteSeriesSubeditor implements SeriesSubeditor, MarkerStyleReceiver
{		
    private final Channel1DRendererDataImmutable initRendererData;
    private final Channel1DRendererDataMutable<Channel1DRendererData> model;

    private final PreferredDiscreteSeriesRendererStyle prefStyle;       
    private final String seriesName;

    private final DiscreteSeriesRenderer renderer;
    private final List<DiscreteSeriesRenderer> boundedRenderers;

    private final JCheckBox showCheckBox = new JCheckBox();
    private final JCheckBox boxVisibleInLegend = new JCheckBox();
    private ShapeAndSizeChooser shapeChooser;
    private final PaintSampleFlexible markerPaintSample = new PaintSampleFlexible();		
    private final EditorShapeLabel shapeLabel = EditorShapeLabel.buildShapeLabel();		

    private final Action selectPaintAction = new SelectPaintAction();
    private final Action selectMarkerShapeAction = new SelectMarkerShapeAction();

    private final SubPanel mainPanel = new SubPanel();

    public DiscreteSeriesSubeditor(DiscreteSeriesRenderer renderer, List<DiscreteSeriesRenderer> boundedRenderes) 
    {
        this.renderer = Validation.requireNonNullParameterName(renderer, "renderer");
        this.boundedRenderers = Validation.requireNonNullParameterName(boundedRenderes, "boundedRenderes");
        this.seriesName = renderer.getName();
        this.prefStyle = PreferredDiscreteSeriesRendererStyle.getInstance(renderer.getPreferences(), renderer.getStyleKey());

        this.initRendererData = renderer.getImmutableData();
        this.model = new Channel1DRendererDataMutable<>(initRendererData);

        resetEditor();

        mainPanel.addComponent(new JLabel("Show"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(showCheckBox, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(new JLabel("Legend"), 2, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(boxVisibleInLegend, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(new JLabel("Point style"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(shapeLabel.getLabel(), 1, 1, 3, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(new JButton(selectMarkerShapeAction), 4, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(new JLabel("Color"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        mainPanel.addComponent(markerPaintSample, 1, 2, 3, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        mainPanel.addComponent(new JButton(selectPaintAction), 4, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),seriesName)));		

        initItemListeners();
    }

    @Override
    public void setNameBorder(boolean named)
    {
        Border border = named ? BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), seriesName) : BorderFactory.createEtchedBorder();
        mainPanel.setBorder(border);
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
        for(DiscreteSeriesRenderer r: boundedRenderers)
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
        showCheckBox.setSelected(model.getBaseSeriesVisible());
        boxVisibleInLegend.setSelected(model.getBaseSeriesVisibleInLegend());
        markerPaintSample.setPaint(model.getBasePaint());
        shapeLabel.update(model.getBaseMarkerIndex(), model.getBaseMarkerSize(), model.getBasePaint());
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
        shapeLabel.update(model.getBaseMarkerIndex(), model.getBaseMarkerSize(), model.getBasePaint());
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

        shapeLabel.update(model.getBaseMarkerIndex(), model.getBaseMarkerSize(), model.getBasePaint());
    }

    @Override
    public Paint getPaint() 
    {
        return model.getBasePaint();
    }

    @Override
    public Paint getMarkerFillPaint()
    {
        return model.getBasePaint();
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

    private void initItemListeners()
    {
        showCheckBox.addItemListener(new ItemListener() {            
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean visible = (e.getStateChange()== ItemEvent.SELECTED);
                model.setBaseSeriesVisible(visible);
                renderer.setBaseSeriesVisible(visible);
            }
        });

        boxVisibleInLegend.addItemListener(new ItemListener() {            
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean visibleInLegend = (e.getStateChange()== ItemEvent.SELECTED);
                model.setBaseSeriesVisibleInLegend(visibleInLegend);
                renderer.setBaseSeriesVisibleInLegend(visibleInLegend);
            }
        });
    }  

    @Override
    public boolean isApplyToAllEnabled()
    {
        return boundedRenderers.size() > 1;
    }

    private void attemptPaintSelection() 
    {
        Paint markerFillPaint = model.getBasePaint();
        Paint p = markerFillPaint;
        Color defaultColor = (p instanceof Color ? (Color) p : Color.blue);
        Color c = JColorChooser.showDialog(mainPanel, "Color", defaultColor);
        if (c != null) 
        {
            markerPaintSample.setPaint(c);
            model.setBasePaint(c);
            renderer.setBasePaint(c);
            shapeLabel.update(model.getBaseMarkerIndex(), model.getBaseMarkerSize(), model.getBasePaint());
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
}




