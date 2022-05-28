
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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.ui.PaintSample;

import atomicJ.gui.BasicStrokeReceiver;
import atomicJ.gui.ChartStyleSupplier;
import atomicJ.gui.FontDisplayField;
import atomicJ.gui.FontReceiver;
import atomicJ.gui.GradientPaint;
import atomicJ.gui.PaintReceiver;
import atomicJ.gui.PreferredRoamingTitleLegendStyle;
import atomicJ.gui.RoamingLegend;
import atomicJ.gui.SkewedGradientEditionDialog;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;

public class LegendStyleSubeditor implements Subeditor, PaintReceiver
{
    private final Preferences pref;

    private final boolean initFrameVisible;
    private final Font initTextFont;
    private final Paint initTextPaint;
    private final Stroke initFrameStroke;
    private final Paint initFramePaint;
    private final Paint initBackgroundPaint;
    private final boolean initUseGradientPaint;

    private final boolean containsText;

    private boolean frameVisible;
    private Font textFont;
    private Paint textPaint;
    private Stroke frameStroke;
    private Paint framePaint;
    private Paint backgroundPaint;
    private boolean useGradientPaint;

    private final JLabel labelTextFont = new JLabel("Text font");
    private final JLabel labelTextPaint = new JLabel("Text color");

    private final JCheckBox boxFrameVisible = new JCheckBox();
    private final JCheckBox boxUseGradient = new JCheckBox();
    private final FontDisplayField fieldFont;
    private final PaintSample textPaintSample;
    private final PaintSample backgroundPaintSample;
    private final StraightStrokeSample frameStrokeSample = new StraightStrokeSample();

    private final Action selectTextPaintAction = new SelectTextPaintAction();
    private final Action selectTextFontAction = new SelectTextFontAction();

    private SkewedGradientEditionDialog gradientDialog;

    private StrokeChooser frameStrokeChooser;
    private FontChooserDialog fontChooserDialog;

    private final JPanel editorPanel = new JPanel(new BorderLayout());

    private final RoamingLegend legend;
    private final List<RoamingLegend> boundedLegends;

    public LegendStyleSubeditor(RoamingLegend legend, List<RoamingLegend> boundedLegends)
    {
        this.boundedLegends = boundedLegends;
        this.legend = legend;
        this.pref = legend.getPreferences();

        this.initFrameVisible = legend.isFrameVisible();
        this.initTextFont = legend.getLegendItemFont();
        this.initTextPaint = legend.getLegendItemPaint();
        this.initFramePaint = legend.getFramePaint();
        this.initBackgroundPaint = legend.getBackgroundPaint();
        this.initUseGradientPaint = initBackgroundPaint instanceof GradientPaint;
        this.initFrameStroke = legend.getFrameStroke();

        setParametersToInitial();
        containsText = legend.containsText();

        editorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        this.boxFrameVisible.setSelected(initFrameVisible);
        this.boxUseGradient.setSelected(initUseGradientPaint);

        this.backgroundPaintSample = new PaintSample(initBackgroundPaint);

        this.frameStrokeSample.setStroke(initFrameStroke);
        this.frameStrokeSample.setStrokePaint(initFramePaint);

        this.fieldFont = new FontDisplayField(initTextFont);
        this.textPaintSample = new PaintSample(initTextPaint);

        if(!containsText)
        {
            fieldFont.setEnabled(false);
            textPaintSample.setEnabled(false);
            labelTextFont.setEnabled(false);
            labelTextPaint.setEnabled(false);
            selectTextFontAction.setEnabled(false);
            selectTextPaintAction.setEnabled(false);
            textPaintSample.setPaint(new Color(0f, 0f, 0f, 0f));
        }          

        addComponentsAndDoLayout();                  
        initItemListener();
    }

    private void initItemListener()
    {
        boxFrameVisible.addItemListener(new ItemListener() 
        {            
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                frameVisible = selected;
                legend.setFrameVisible(frameVisible);
            }
        });
        boxUseGradient.addItemListener(new ItemListener()
        {          
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                useGradientPaint = selected;                
            }
        });
    }

    private void setParametersToInitial()
    {
        frameVisible = initFrameVisible;
        textFont = initTextFont;
        textPaint = initTextPaint;
        backgroundPaint = initBackgroundPaint;
        useGradientPaint = initUseGradientPaint;
        frameStroke = initFrameStroke;
        framePaint = initFramePaint;
    }

    private void addComponentsAndDoLayout()
    {
        editorPanel.setLayout(new BorderLayout());
        SubPanel stylePanel = new SubPanel();    

        JButton buttonSelectFrameStroke = new JButton(new SelectFrameStrokeAction());
        JButton buttonSelectBackgroundPaint = new JButton(new SelectBackgroundPaintAction());

        stylePanel.addComponent(new JLabel("Use gradient"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        stylePanel.addComponent(boxUseGradient, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);      

        stylePanel.addComponent(new JLabel("Background color"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        stylePanel.addComponent(backgroundPaintSample, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        stylePanel.addComponent(buttonSelectBackgroundPaint, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);           

        SubPanel frameStrokePanel = new SubPanel();

        frameStrokePanel.addComponent(boxFrameVisible, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.075, 0);     
        frameStrokePanel.addComponent(new JLabel("Stroke"), 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        frameStrokePanel.addComponent(frameStrokeSample, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);             

        stylePanel.addComponent(new JLabel("Show frame"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        stylePanel.addComponent(frameStrokePanel, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        stylePanel.addComponent(buttonSelectFrameStroke, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     

        if(containsText)
        {
            JButton buttonSelectTextPaint = new JButton(selectTextPaintAction);
            JButton buttonSelectTextFont = new JButton(selectTextFontAction);

            stylePanel.addComponent(labelTextFont, 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
            stylePanel.addComponent(fieldFont, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
            stylePanel.addComponent(buttonSelectTextFont, 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     

            stylePanel.addComponent(labelTextPaint, 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
            stylePanel.addComponent(textPaintSample, 1, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
            stylePanel.addComponent(buttonSelectTextPaint, 2, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     
        }

        editorPanel.add(stylePanel, BorderLayout.NORTH);   	
        editorPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
    }

    @Override
    public void applyChangesToAll() 
    {
        for(RoamingLegend leg: boundedLegends)
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

    private void resetLegend(RoamingLegend legend)
    {
        legend.setFrameVisible(frameVisible);
        legend.setFramePaint(framePaint);
        legend.setFrameStroke(frameStroke);
        legend.setBackgroundPaint(backgroundPaint);
        legend.setLegendItemFont(textFont);
        legend.setLegendItemPaint(textPaint);
    }

    private void resetEditor()
    {
        frameStrokeSample.setStroke(frameStroke);
        frameStrokeSample.setStrokePaint(framePaint);
        backgroundPaintSample.setPaint(backgroundPaint);

        boxFrameVisible.setSelected(frameVisible);
        boxUseGradient.setSelected(useGradientPaint);

        if(containsText)
        {
            fieldFont.setDisplayFont(textFont);
            textPaintSample.setPaint(textPaint);
        }
    }

    @Override
    public void resetToDefaults() 
    {
        ChartStyleSupplier supplier = legend.getSupplier();
        String key = legend.getKey();
        boolean defaultFrameVisible = supplier.getDefaultLegendFrameVisible(key);

        frameVisible = pref.getBoolean(PreferredRoamingTitleLegendStyle.LEGEND_FRAME_VISIBLE, defaultFrameVisible);
        textFont = (Font)SerializationUtilities.getSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_ITEM_FONT, new Font("Dialog", Font.PLAIN, 14));
        textPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_ITEM_PAINT, Color.black);
        framePaint = (Paint) SerializationUtilities.getSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_FRAME_PAINT, Color.black);
        frameStroke = SerializationUtilities.getStroke(pref, PreferredRoamingTitleLegendStyle.LEGEND_FRAME_STROKE, new BasicStroke(1.f));
        backgroundPaint = (Paint) SerializationUtilities.getSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_BACKGROUND_PAINT, new Color(255,255,255,0));
        useGradientPaint = backgroundPaint instanceof GradientPaint;

        resetLegend(legend);
        resetEditor();
    }

    @Override
    public void saveAsDefaults() 
    {		
        pref.putBoolean(PreferredRoamingTitleLegendStyle.LEGEND_FRAME_VISIBLE, frameVisible);
        try 
        {
            SerializationUtilities.putSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_ITEM_FONT, textFont);
            SerializationUtilities.putSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_ITEM_PAINT, textPaint);
            SerializationUtilities.putSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_FRAME_PAINT, framePaint);
            SerializationUtilities.putSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_BACKGROUND_PAINT, backgroundPaint);

            SerializationUtilities.putStroke(pref, PreferredRoamingTitleLegendStyle.LEGEND_FRAME_STROKE, frameStroke);
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
        return editorPanel;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return boundedLegends.size() > 1;
    }

    private void attemptModifyFrameStroke() 
    {		    
        if(frameStrokeChooser == null)
        {
            frameStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(editorPanel), new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke)frameStroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    frameStroke = stroke;
                    frameStrokeSample.setStroke(stroke);
                    legend.setFrameStroke(frameStroke);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return framePaint;
                }

                @Override
                public void setStrokePaint(Paint paint) 
                {
                    framePaint = paint;
                    frameStrokeSample.setStrokePaint(paint);
                    legend.setFramePaint(framePaint);
                }       	
            }
                    );	            	    	
        }
        frameStrokeChooser.showDialog();
    }

    private void attemptModifyTextPaint() 
    {
        Color defaultColor = (textPaint instanceof Color ? (Color) textPaint : Color.black);
        Color c = JColorChooser.showDialog(editorPanel, "Legend text color", defaultColor);
        if (c != null) 
        {
            textPaint = c;
            textPaintSample.setPaint(textPaint);
            legend.setLegendItemPaint(textPaint);
        }
    }

    private void attemptModifyTextFont() 
    {
        if( fontChooserDialog == null)
        {
            this.fontChooserDialog = new FontChooserDialog(SwingUtilities.getWindowAncestor(editorPanel), "Font selection");
        }

        this.fontChooserDialog.showDialog(new FontReceiver() 
        {

            @Override
            public void setFont(Font newFont) 
            { 
                textFont = newFont;
                fieldFont.setDisplayFont(textFont);
                legend.setLegendItemFont(textFont);
                editorPanel.revalidate();	
            }

            @Override
            public Font getFont()
            {
                return textFont;
            }
        });
    }

    private void attemptModifyBackgroundPaint() 
    {

        if(useGradientPaint)
        {
            if(gradientDialog == null)
            {
                gradientDialog = new SkewedGradientEditionDialog(SwingUtilities.getWindowAncestor(editorPanel));
            }
            gradientDialog.showDialog(this);
        }
        else
        {
            Paint backgroundPaintNew = JColorChooser.showDialog(editorPanel, "Legend background color", Color.blue);	        
            if (backgroundPaintNew != null) 
            {
                backgroundPaint = backgroundPaintNew;
                backgroundPaintSample.setPaint(backgroundPaintNew);
                legend.setBackgroundPaint(backgroundPaintNew);			
            }
        }    	
    }

    @Override
    public Paint getPaint() 
    {
        return backgroundPaint;
    }

    @Override
    public void setPaint(Paint paint) 
    {
        if(paint != null)
        {			
            backgroundPaint = paint;
            backgroundPaintSample.setPaint(backgroundPaint);
            legend.setBackgroundPaint(backgroundPaint);			
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

    private class SelectTextFontAction extends AbstractAction
    {
        private static final long serialVersionUID = 7606481700886685687L;

        private SelectTextFontAction()
        {
            putValue(NAME, "Select");
        }
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            attemptModifyTextFont();         
        }        
    }

    private class SelectTextPaintAction extends AbstractAction
    {
        private static final long serialVersionUID = -4387049857907780148L;

        private SelectTextPaintAction()
        {
            putValue(NAME, "Select");
        }
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            attemptModifyTextPaint();         
        }        
    }


    private class SelectBackgroundPaintAction extends AbstractAction
    {
        private static final long serialVersionUID = -9190776954796003250L;

        private SelectBackgroundPaintAction()
        {
            putValue(NAME, "Select");
        }
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            attemptModifyBackgroundPaint();         
        }        
    }

    private class SelectFrameStrokeAction extends AbstractAction
    {
        private static final long serialVersionUID = 7577258734332843989L;

        private SelectFrameStrokeAction()
        {
            putValue(NAME, "Select");
        }
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            attemptModifyFrameStroke();         
        }        
    }

}
