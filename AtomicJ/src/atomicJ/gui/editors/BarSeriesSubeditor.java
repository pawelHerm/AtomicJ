
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.jfree.ui.PaintSample;

import atomicJ.gui.BasicStrokeReceiver;
import atomicJ.gui.CustomizableXYBarRenderer;
import atomicJ.gui.PreferredContinuousSeriesRendererStyle;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;


public class BarSeriesSubeditor implements Subeditor, ActionListener, ItemListener, ChangeListener
{
    private static final String SELECT_BAR_PAINT_COMMAND = "SELECT_BAR_PAINT_COMMAND";
    private static final String EDIT_OUTLINE_STROKE_COMMAND = "EDIT_OUTLINE_STROKE_COMMAND";

    private Paint barPaint;
    private Paint outlinePaint;
    private Stroke outlineStroke;
    private boolean shadowVisible;
    private boolean useGradient;
    private boolean outlineVisible;
    private double barMargin;

    private final Paint initBarPaint;
    private final Paint initOutlinePaint;
    private final Stroke initOutlineStroke;
    private final boolean initShadowVisible;
    private final boolean initUseGradient;
    private final boolean initOutlineVisible;
    private final double initMargin;

    private final PaintSample paintSample;	
    private final StraightStrokeSample outlineStrokeSample = new StraightStrokeSample();
    private final JLabel labelOutlineStroke = new JLabel("Outline stroke");
    private final JButton buttonSelectBarPaint = new JButton("Select");	
    private final JButton buttonEditOutlineStroke = new JButton("Edit");		

    private final JCheckBox boxShowShadow = new JCheckBox();
    private final JCheckBox boxUseGradient = new JCheckBox();
    private final JCheckBox boxOutlineVisible = new JCheckBox();
    private final JSpinner marginsSpinner;

    private final Preferences pref;       
    private final String seriesName;

    private StrokeChooser barOutlineStrokeChooser;

    private final SubPanel editorPanel = new SubPanel();

    private final CustomizableXYBarRenderer renderer;
    private final List<CustomizableXYBarRenderer> boundededRenderers;

    public BarSeriesSubeditor(CustomizableXYBarRenderer renderer, List<CustomizableXYBarRenderer> boundedRenderers)
    {
        this.renderer = renderer;
        this.boundededRenderers = boundedRenderers;

        this.initBarPaint = renderer.getBasePaint();
        this.initOutlinePaint = renderer.getBaseOutlinePaint();
        this.initOutlineStroke = renderer.getBaseOutlineStroke();
        this.initOutlineVisible = renderer.isDrawBarOutline();
        this.initShadowVisible = renderer.getShadowsVisible();
        this.initUseGradient = renderer.isGradientPainted();
        this.initMargin = renderer.getMargin();
        this.seriesName = renderer.getName();
        this.pref = renderer.getPreferences();

        setParametersToInitial();		

        outlineStrokeSample.setStroke(initOutlineStroke);
        outlineStrokeSample.setStrokePaint(initOutlinePaint);

        boxShowShadow.setSelected(initShadowVisible);		
        boxUseGradient.setSelected(initUseGradient);	    
        boxOutlineVisible.setSelected(initOutlineVisible);

        paintSample = new PaintSample(barPaint);

        marginsSpinner = new JSpinner(new SpinnerNumberModel(100*barMargin,0,100,1));

        editorPanel.addComponent(new JLabel("Shadow"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        editorPanel.addComponent(boxShowShadow, 1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);		

        editorPanel.addComponent(new JLabel("Gradient"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        editorPanel.addComponent(boxUseGradient, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);

        editorPanel.addComponent(new JLabel("Bar margin (%)"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        editorPanel.addComponent(marginsSpinner, 1, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);

        editorPanel.addComponent(new JLabel("Bar color"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        editorPanel.addComponent(paintSample, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        editorPanel.addComponent(buttonSelectBarPaint, 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 1);


        SubPanel outlinePanel = new SubPanel();

        outlinePanel.addComponent(boxOutlineVisible, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.075, 0);     
        outlinePanel.addComponent(new JLabel("Stroke"), 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        outlinePanel.addComponent(outlineStrokeSample, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);             

        editorPanel.addComponent(new JLabel("Bar outline"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        editorPanel.addComponent(outlinePanel, 1, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        editorPanel.addComponent(buttonEditOutlineStroke, 2, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 1);

        editorPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), seriesName)));

        initActionListener();
        initChangeListener();
        initItemListener();
    }

    @Override
    public void setNameBorder(boolean b)
    {
        if(b)
        {
            editorPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), seriesName)));
        }
        else
        {
            editorPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));
        }
    }

    private void initActionListener()
    {
        buttonSelectBarPaint.setActionCommand(SELECT_BAR_PAINT_COMMAND);
        buttonEditOutlineStroke.setActionCommand(EDIT_OUTLINE_STROKE_COMMAND);

        buttonSelectBarPaint.addActionListener(this);
        buttonEditOutlineStroke.addActionListener(this);
    }

    private void initChangeListener()
    {
        marginsSpinner.addChangeListener(this);
    }

    private void initItemListener()
    {
        boxOutlineVisible.addItemListener(this);
        boxUseGradient.addItemListener(this);
        boxShowShadow.addItemListener(this);
    }

    private void setParametersToInitial()
    {
        barPaint = initBarPaint;
        outlinePaint = initOutlinePaint;
        outlineStroke = initOutlineStroke;
        shadowVisible = initShadowVisible;
        useGradient = initUseGradient;
        outlineVisible = initOutlineVisible;
        barMargin = initMargin;
    }

    @Override
    public String getSubeditorName()
    {
        return seriesName;
    }


    @Override
    public void resetToDefaults() 
    {
        Paint defBarPaint = renderer.getSupplier().getColor(renderer.getStyleKey());
        Paint defOutlinePaint = Color.black;
        Stroke defOutlineStroke = new BasicStroke(1.f);

        shadowVisible = pref.getBoolean(BAR_SHADOW_VISIBLE, true);
        useGradient = pref.getBoolean(GRADIENT, true);
        outlineVisible = pref.getBoolean(BAR_OUTLINE_VISIBLE, true);
        barMargin = pref.getDouble(BAR_MARGIN, 0);
        barPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredContinuousSeriesRendererStyle.PAINT, defBarPaint);
        outlinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, BAR_OUTLINE_PAINT, defOutlinePaint);
        outlineStroke = SerializationUtilities.getStroke(pref, BAR_OUTLINE_STROKE, defOutlineStroke);

        resetRenderer(renderer);	
        resetEditor();
    }

    private void resetRenderer(CustomizableXYBarRenderer r)
    {
        r.setBasePaint(barPaint);
        r.setBaseOutlinePaint(outlinePaint);
        r.setBaseOutlineStroke(outlineStroke);
        r.setDrawBarOutline(outlineVisible);
        r.setMargin(barMargin);
        r.setShadowVisible(shadowVisible);
        r.setGradientPainted(useGradient);
    }

    private void attemptOutlineStrokeSelection() 
    {
        if(barOutlineStrokeChooser == null)
        {
            barOutlineStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(editorPanel), new BasicStrokeReceiver()
            {

                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke)outlineStroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    outlineStroke = stroke;
                    outlineStrokeSample.setStroke(stroke);
                    renderer.setBaseOutlineStroke(stroke);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return outlinePaint;
                }

                @Override
                public void setStrokePaint(Paint paint) 
                {
                    outlinePaint = paint;
                    outlineStrokeSample.setStrokePaint(paint);
                    renderer.setBaseOutlinePaint(paint);
                }       	
            }
                    ); 
        }

        barOutlineStrokeChooser.showDialog();
    }

    @Override
    public void saveAsDefaults() 
    {
        pref.putBoolean(BAR_SHADOW_VISIBLE, shadowVisible);
        pref.putBoolean(GRADIENT, useGradient);
        pref.putDouble(BAR_MARGIN, barMargin);
        pref.putBoolean(BAR_OUTLINE_VISIBLE, outlineVisible);

        try 
        {
            SerializationUtilities.putSerializableObject(pref, PreferredContinuousSeriesRendererStyle.PAINT, barPaint);
            SerializationUtilities.putSerializableObject(pref, BAR_OUTLINE_PAINT, outlinePaint);
            SerializationUtilities.putStroke(pref, BAR_OUTLINE_STROKE, outlineStroke);
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
        for(CustomizableXYBarRenderer r : boundededRenderers)
        {
            resetRenderer(r);	
        }
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();		
        resetRenderer(renderer);			
        resetEditor();
    }

    @Override
    public Component getEditionComponent() 
    {
        return editorPanel;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return boundededRenderers.size() > 1;
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();
        if (command.equals(SELECT_BAR_PAINT_COMMAND)) 
        {
            attemptBarPaintSelection();
            renderer.setBasePaint(barPaint);
        }	
        else if(command.equals(EDIT_OUTLINE_STROKE_COMMAND))
        {
            attemptOutlineStrokeSelection();
        }
    }	

    public void attemptBarPaintSelection() 
    {
        Paint p = this.barPaint;
        Color defaultColor = (p instanceof Color ? (Color) p : Color.blue);
        Color c = JColorChooser.showDialog(editorPanel, "Color", defaultColor);
        if (c != null) 
        {
            this.barPaint = c;
            this.paintSample.setPaint(c);
        }
    }

    private void resetEditor()
    {
        boxShowShadow.setSelected(shadowVisible);
        boxUseGradient.setSelected(useGradient);
        marginsSpinner.setValue(100*barMargin);
        paintSample.setPaint(barPaint);
    }

    @Override
    public void stateChanged(ChangeEvent evt) 
    {		
        barMargin = 0.01*((SpinnerNumberModel)marginsSpinner.getModel()).getNumber().floatValue();;
        renderer.setMargin(barMargin);  					
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);
        Object source = evt.getSource();

        if(source == boxOutlineVisible)
        {
            this.outlineVisible = selected;
            renderer.setDrawBarOutline(outlineVisible);
        }
        else if(source == boxUseGradient)
        {
            this.useGradient = selected;
            renderer.setGradientPainted(useGradient);
        }
        else if(source == boxShowShadow)
        {
            shadowVisible = selected;
            renderer.setShadowVisible(shadowVisible);
        }
    }
}
