
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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.BasicStrokeReceiver;
import atomicJ.gui.PreferredRoamingPaintScaleLegendStyle;
import atomicJ.gui.RoamingColorGradientLegend;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;

public class LegendStripSubeditor extends SubPanel implements Subeditor, ActionListener, ChangeListener, ItemListener
{
    private static final long serialVersionUID = 1L;	

    private static final String EDIT_OUTLINE_STROKE_COMMAND = "EDIT_OUTLINE_STROKE_COMMAND";

    private final Preferences pref;

    private final boolean initStripOutlineVisible;
    private final Stroke initStripOutlineStroke;
    private final Paint initStripOutlinePaint;
    private final double initStripWidth;

    private boolean stripOutlineVisible;
    private Stroke stripOutlineStroke;
    private Paint stripOutlinePaint;
    private double stripWidth;

    private final JCheckBox boxStripVisible = new JCheckBox();
    private final StraightStrokeSample outlineStrokeSample = new StraightStrokeSample();

    private final JButton buttonEditStroke = new JButton("Edit");
    private final JSpinner spinnerStripWidth;

    private StrokeChooser frameStrokeChooser;

    private final RoamingColorGradientLegend legend;
    private final List<RoamingColorGradientLegend> boundedLegends;

    public LegendStripSubeditor(RoamingColorGradientLegend legend, List<RoamingColorGradientLegend> boundedLegends)
    {
        this.boundedLegends = boundedLegends;
        this.legend = legend;
        this.pref = legend.getPreferences();

        this.initStripOutlineVisible = legend.isStripOutlineVisible();
        this.initStripOutlinePaint = legend.getStripOutlinePaint();
        this.initStripWidth = legend.getStripWidth();
        this.initStripOutlineStroke = legend.getStripOutlineStroke();

        setParametersToInitial();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        this.boxStripVisible.setSelected(initStripOutlineVisible);

        this.outlineStrokeSample.setStroke(initStripOutlineStroke);
        this.outlineStrokeSample.setStrokePaint(initStripOutlinePaint);

        spinnerStripWidth = new JSpinner(new SpinnerNumberModel(initStripWidth,1,Short.MAX_VALUE,0.5));   		

        addComponentsAndDoLayout();  
        initChangeListener();
        initItemListener();
    }

    private void initItemListener()
    {
        boxStripVisible.addItemListener(this);
    }

    private void initChangeListener()
    {
        spinnerStripWidth.addChangeListener(this);
    }

    private void setParametersToInitial()
    {
        stripOutlineVisible = initStripOutlineVisible;
        stripWidth = initStripWidth;
        stripOutlineStroke = initStripOutlineStroke;
        stripOutlinePaint = initStripOutlinePaint;
    }

    private void addComponentsAndDoLayout()
    {
        setLayout(new BorderLayout());
        SubPanel stylePanel = new SubPanel();    

        buttonEditStroke.setActionCommand(EDIT_OUTLINE_STROKE_COMMAND);
        buttonEditStroke.addActionListener(this);

        stylePanel.addComponent(new JLabel("Strip width"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        stylePanel.addComponent(spinnerStripWidth, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);      


        SubPanel outlinePanel = new SubPanel();

        outlinePanel.addComponent(boxStripVisible, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.075, 0);     
        outlinePanel.addComponent(new JLabel("Stroke"), 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        outlinePanel.addComponent(outlineStrokeSample, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);             

        stylePanel.addComponent(new JLabel("Show outline"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        stylePanel.addComponent(outlinePanel, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        stylePanel.addComponent(buttonEditStroke, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     

        add(stylePanel, BorderLayout.NORTH);   	
        setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
    }

    @Override
    public void applyChangesToAll() 
    {
        for(RoamingColorGradientLegend leg: boundedLegends)
        {
            resetLegend(leg);
        }		
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();
        resetLegend(legend);	
        resetEditor();
    }

    private void resetLegend(RoamingColorGradientLegend legend)
    {
        legend.setStripOutlineVisible(stripOutlineVisible);
        legend.setStripOutlinePaint(stripOutlinePaint);
        legend.setStripOutlineStroke(stripOutlineStroke);
        legend.setStripWidth(stripWidth);
    }

    private void resetEditor()
    {
        outlineStrokeSample.setStroke(stripOutlineStroke);
        outlineStrokeSample.setStrokePaint(stripOutlinePaint);
        spinnerStripWidth.setValue(stripWidth);		
        boxStripVisible.setSelected(stripOutlineVisible);		
    }

    @Override
    public void resetToDefaults() 
    {		
        stripOutlinePaint = (Paint) SerializationUtilities.getSerializableObject(pref, PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_OUTLINE_PAINT, Color.black);
        stripOutlineStroke = SerializationUtilities.getStroke(pref, PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_OUTLINE_STROKE, new BasicStroke(1.f));
        stripOutlineVisible = pref.getBoolean(PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_OUTLINE_VISIBLE, true);
        stripWidth = pref.getDouble(PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_WIDTH, 20);

        resetLegend(legend);
        resetEditor();
    }

    @Override
    public void saveAsDefaults() 
    {		
        pref.putBoolean(PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_OUTLINE_VISIBLE, stripOutlineVisible);
        pref.putDouble(PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_WIDTH, stripWidth);
        try 
        {
            SerializationUtilities.putSerializableObject(pref, PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_OUTLINE_PAINT, stripOutlinePaint);
            SerializationUtilities.putStroke(pref, PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_OUTLINE_STROKE, stripOutlineStroke);
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
    public Component getEditionComponent()
    {
        return this;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return boundedLegends.size()>1;
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();

        if(command.equals(EDIT_OUTLINE_STROKE_COMMAND))
        {
            attemptModifyFrameStroke();
        }
    }   

    private void attemptModifyFrameStroke() 
    {		    
        if(frameStrokeChooser == null)
        {
            frameStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(this), new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke)stripOutlineStroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    stripOutlineStroke = stroke;
                    outlineStrokeSample.setStroke(stroke);
                    legend.setStripOutlineStroke(stripOutlineStroke);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return stripOutlinePaint;
                }

                @Override
                public void setStrokePaint(Paint paint) 
                {
                    stripOutlinePaint = paint;
                    outlineStrokeSample.setStrokePaint(paint);
                    legend.setStripOutlinePaint(stripOutlinePaint);
                }       	
            }
                    );	            	    	
        }
        frameStrokeChooser.showDialog();
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);
        Object source = evt.getSource();
        if(source == boxStripVisible)
        {
            this.stripOutlineVisible = selected;
            legend.setStripOutlineVisible(stripOutlineVisible);
        }					
    }	

    @Override
    public void stateChanged(ChangeEvent evt) 
    {		
        Object source = evt.getSource();
        if(source == spinnerStripWidth)
        {
            stripWidth = ((SpinnerNumberModel)spinnerStripWidth.getModel()).getNumber().doubleValue();
            legend.setStripWidth(stripWidth);  
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
