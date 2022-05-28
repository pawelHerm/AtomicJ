
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
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.BasicStrokeReceiver;
import atomicJ.gui.CustomizableNumberAxis;
import atomicJ.gui.FontField;
import atomicJ.gui.FontReceiver;
import atomicJ.gui.PaintSampleFlexible;
import atomicJ.gui.PreferredScaleBarStyle;
import atomicJ.gui.ScaleBar;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;
import atomicJ.utilities.Validation;

import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


public class ScaleBarSubeditor implements Subeditor
{
    //initial settings

    private final boolean initVisible;
    private final double initLength;
    private final boolean initLengthAutomatic;
    private final float initLabelOffset;
    private final float initLabelLengthwisePosition;

    private final int initPositionX;
    private final int initPositionY;

    private final boolean initLabelVisible; 
    private final Font initLabelFont;
    private final Paint initLabelPaint;  
    private final Stroke initStroke;	
    private final Paint initStrokePaint;


    //current settings

    private boolean visible;

    private double length;
    private boolean lengthAutomatic;

    private int positionX;
    private int positionY;

    private float labelOffset;
    private float labelLengthwisePosition;
    private boolean labelVisible;         
    private Font labelFont;
    private Paint labelPaint;	
    private Stroke stroke;	
    private Paint strokePaint;	


    //GUI components

    private final StraightStrokeSample strokeSample = new StraightStrokeSample();	
    private final PaintSampleFlexible labelPaintSample = new PaintSampleFlexible();   
    private final FontField fieldLabelFont = new FontField();   

    private final JCheckBox boxScalebarVisible = new JCheckBox(); 
    private final JCheckBox boxLabelVisible = new JCheckBox();


    private StrokeChooser chooserStrokeUnfinishedStandard;

    //GUI for length
    private final JCheckBox boxAutomaticLength = new JCheckBox("Automatic");
    private final JSpinner spinnerLength;   

    //GUI components for position
    private final JLabel labelPositionX = new JLabel("Position");
    private final JLabel labelLeft = new JLabel("Left");
    private final JLabel labelRight = new JLabel("Right");
    private final JLabel labelBottom = new JLabel("Bottom");
    private final JLabel labelTop = new JLabel("Top");

    private final JSlider sliderX;
    private final JSlider sliderY;

    private FontChooserDialog fontChooserDialog;

    private final Preferences pref;

    private final JPanel editorPanel = new JPanel(new BorderLayout());

    private final ScaleBar scaleBar;
    private final List<ScaleBar>boundedScaleBars;

    public ScaleBarSubeditor(ScaleBar scaleBar, List<ScaleBar> boundedScaleBars, CustomizableNumberAxis axis) 
    {   
        this.scaleBar = Validation.requireNonNullParameterName(scaleBar, "scaleBar");
        this.boundedScaleBars = Validation.requireNonNullParameterName(boundedScaleBars, "boundedScaleBars");

        this.pref = scaleBar.getPreferences();

        this.initVisible = scaleBar.isVisible();
        this.initLengthAutomatic = scaleBar.isLengthAutomatic();
        this.initLength =  scaleBar.getLength();

        this.initLabelOffset = scaleBar.getLabelOffset();
        this.initLabelLengthwisePosition = scaleBar.getLabelLengthwisePosition();

        this.initPositionX = Math.max(0, (int) Math.round(scaleBar.getPositionX()*100));
        this.initPositionY = Math.max(0,(int) Math.round(scaleBar.getPositionY()*100));

        this.initLabelVisible = scaleBar.isLabelVisible(); 
        this.initLabelFont = scaleBar.getLabelFont();
        this.initLabelPaint = scaleBar.getLabelPaint();  
        this.initStrokePaint = scaleBar.getStrokePaint();	
        this.initStroke = scaleBar.getStroke();

        setParametersToInitial();

        //sets the editor
        this.fieldLabelFont.setDisplayFont(initLabelFont);   	
        this.labelPaintSample.setPaint(labelPaint);        
        this.strokeSample.setStroke(stroke);      
        this.strokeSample.setStrokePaint(strokePaint);       
        this.boxScalebarVisible.setSelected(visible);
        this.boxLabelVisible.setSelected(labelVisible);       
        this.boxAutomaticLength.setSelected(lengthAutomatic);

        this.sliderX = new JSlider(0, 100, initPositionX);
        sliderX.setMajorTickSpacing(5);

        this.sliderY = new JSlider(0, 100, initPositionY);
        sliderY.setMajorTickSpacing(5);

        double rangeLength = axis.getRange().getLength();
        int exp = (int)Math.rint(Math.floor(Math.log10(rangeLength))) - 1;
        double step = Math.pow(10, exp);

        this.spinnerLength = new JSpinner(new SpinnerNumberModel(initLength, 0, Integer.MAX_VALUE, step));        
        this.spinnerLength.setEnabled(!lengthAutomatic);

        JPanel mainPanel = buildMainPanel();  
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        editorPanel.add(mainPanel, BorderLayout.NORTH);

        initChangeListener();
        initItemListener();
    }

    private void setParametersToInitial()
    {
        this.visible = initVisible;

        this.lengthAutomatic = initLengthAutomatic;
        this.length = initLength;
        this.positionX = initPositionX;
        this.positionY = initPositionY;

        this.labelOffset = initLabelOffset;
        this.labelLengthwisePosition = initLabelLengthwisePosition;

        this.labelVisible = initLabelVisible;

        this.labelFont = initLabelFont;     
        this.labelPaint = initLabelPaint; 
        this.stroke = initStroke;    
        this.strokePaint = initStrokePaint;
    }

    private void initChangeListener()
    {
        spinnerLength.addChangeListener(new ChangeListener() 
        {           
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                length = ((SpinnerNumberModel)spinnerLength.getModel()).getNumber().doubleValue();
                scaleBar.setLength(length); 
            }
        });
        sliderX.addChangeListener(new ChangeListener() 
        {           
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                positionX = sliderX.getValue();
                double x = positionX/100.;
                scaleBar.setPositionX(x);
            }
        });
        sliderY.addChangeListener(new ChangeListener() 
        {           
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                positionY = sliderY.getValue();
                double y = positionY/100.;
                scaleBar.setPositionY(y);
            }
        });
    }

    private void initItemListener()
    {
        boxScalebarVisible.addItemListener(new ItemListener() 
        {          
            @Override
            public void itemStateChanged(ItemEvent e) 
            {    
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                visible = selected;
                scaleBar.setVisible(selected);
            }
        });  	
        boxLabelVisible.addItemListener(new ItemListener() 
        {          
            @Override
            public void itemStateChanged(ItemEvent e) 
            {        
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                labelVisible = selected;
                scaleBar.setLabelVisible(selected);
            }
        });
        boxAutomaticLength.addItemListener(new ItemListener() 
        {          
            @Override
            public void itemStateChanged(ItemEvent e) 
            {      
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                lengthAutomatic = selected;
                scaleBar.setLengthAutomatic(selected);
                spinnerLength.setEnabled(!selected);
            }
        });
    }

    private void attemptLabelPaintSelection()
    {
        Paint paintLabelUnfinishedStandardNew = JColorChooser.showDialog(editorPanel,"Label color", (Color)labelPaint);         
        if (paintLabelUnfinishedStandardNew != null) 
        {
            labelPaint = paintLabelUnfinishedStandardNew;
            labelPaintSample.setPaint(paintLabelUnfinishedStandardNew);
            scaleBar.setLabelPaint(labelPaint);     
        }
    }

    private void attemptFontSelection()
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
                labelFont = newFont;
                fieldLabelFont.setDisplayFont(labelFont);
                scaleBar.setLabelFont(labelFont);   
            }

            @Override
            public Font getFont()
            {
                return labelFont;
            }
        });

    }

    private void attemptStrokeSelection() 
    {
        if(chooserStrokeUnfinishedStandard == null)
        {
            chooserStrokeUnfinishedStandard = new StrokeChooser(SwingUtilities.getWindowAncestor(editorPanel), new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke) stroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    ScaleBarSubeditor.this.stroke = stroke;
                    strokeSample.setStroke(stroke);
                    scaleBar.setStroke(stroke);
                }
                @Override
                public void setStrokePaint(Paint paint)
                {
                    strokePaint = paint;
                    strokeSample.setStrokePaint(paint);
                    scaleBar.setStrokePaint(paint);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return strokePaint;
                }     	
            }
                    );
        }        
        chooserStrokeUnfinishedStandard.showDialog();
    }



    private void resetEditor()
    {
        this.sliderX.setValue(positionX);
        this.sliderY.setValue(positionY);
        this.fieldLabelFont.setDisplayFont(initLabelFont);   	
        this.labelPaintSample.setPaint(labelPaint);

        this.strokeSample.setStroke(stroke);      
        this.strokeSample.setStrokePaint(strokePaint);

        this.boxScalebarVisible.setSelected(visible);
        this.boxLabelVisible.setSelected(labelVisible);

        this.boxAutomaticLength.setSelected(lengthAutomatic);
        this.spinnerLength.setValue(length);

        this.spinnerLength.setEnabled(!lengthAutomatic);
    }

    @Override
    public void resetToDefaults() 
    {

        this.visible = pref.getBoolean(PreferredScaleBarStyle.SCALEBAR_VISIBLE, false);
        this.lengthAutomatic = pref.getBoolean(PreferredScaleBarStyle.SCALEBAR_LENGTH_AUTOMATIC, true);

        this.strokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredScaleBarStyle.SCALEBAR_STROKE_PAINT, Color.black);
        this.stroke = SerializationUtilities.getStroke(pref, PreferredScaleBarStyle.SCALEBAR_STROKE, new BasicStroke(1.f));

        this.labelPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredScaleBarStyle.SCALEBAR_LABEL_PAINT, Color.black);
        this.labelFont = (Font)SerializationUtilities.getSerializableObject(pref, PreferredScaleBarStyle.SCALEBAR_LABEL_FONT, new Font("Dialog", Font.PLAIN, 14));

        this.positionX = (int) (Math.round(pref.getDouble(PreferredScaleBarStyle.SCALEBAR_POSITION_X, 0.8)*100));
        this.positionY = (int) (Math.round(pref.getDouble(PreferredScaleBarStyle.SCALEBAR_POSITION_Y, 0.1)*100));

        this.labelVisible = pref.getBoolean(PreferredScaleBarStyle.SCALEBAR_LABEL_VISIBLE, true);
        this.labelOffset = pref.getFloat(PreferredScaleBarStyle.SCALEBAR_LABEL_OFFSET, 1.f);

        resetScaleBar(scaleBar);
        resetEditor();
    }

    private void resetScaleBar(ScaleBar s)
    {
        s.setVisible(visible, false);

        s.setLength(length, false);
        s.setLengthAutomatic(lengthAutomatic, false);

        s.setPositionX(positionX/100., false);
        s.setPositionY(positionY/100., false);

        s.setLabelOffset(labelOffset,false);
        s.setLabelLengthwisePosition(labelLengthwisePosition, false);
        s.setLabelVisible(labelVisible, false);         
        s.setLabelFont(labelFont, false);
        s.setLabelPaint(labelPaint, false);	
        s.setStroke(stroke, false);

        s.setStrokePaint(strokePaint, true);
    }

    @Override
    public void saveAsDefaults() 
    {	    
        pref.putBoolean(PreferredScaleBarStyle.SCALEBAR_VISIBLE, visible);
        pref.getBoolean(PreferredScaleBarStyle.SCALEBAR_LENGTH_AUTOMATIC, lengthAutomatic);

        pref.putDouble(PreferredScaleBarStyle.SCALEBAR_POSITION_X, positionX/100.);
        pref.putDouble(PreferredScaleBarStyle.SCALEBAR_POSITION_Y, positionY/100.);

        pref.putBoolean(PreferredScaleBarStyle.SCALEBAR_LABEL_VISIBLE, labelVisible);
        pref.putFloat(PreferredScaleBarStyle.SCALEBAR_LABEL_OFFSET, labelOffset);
        pref.putFloat(PreferredScaleBarStyle.SCALEBAR_LABEL_POSITION, labelLengthwisePosition);

        try 
        {
            SerializationUtilities.putSerializableObject(pref, PreferredScaleBarStyle.SCALEBAR_STROKE_PAINT, strokePaint);
            SerializationUtilities.putStroke(pref, PreferredScaleBarStyle.SCALEBAR_STROKE, stroke);

            SerializationUtilities.putSerializableObject(pref, PreferredScaleBarStyle.SCALEBAR_LABEL_PAINT, labelPaint);
            SerializationUtilities.putSerializableObject(pref, PreferredScaleBarStyle.SCALEBAR_LABEL_FONT, labelFont);
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
        for(ScaleBar s : boundedScaleBars)
        {
            resetScaleBar(s);
        }
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();
        resetScaleBar(scaleBar);
        resetEditor();
    }

    @Override
    public Component getEditionComponent()
    {
        return editorPanel;
    }

    public void setEditionComponetBorder(Border border)
    {
        editorPanel.setBorder(border);
    }  

    @Override
    public boolean isApplyToAllEnabled()
    {
        return false;
    }

    private JPanel buildMainPanel()
    {
        JPanel outerPanel = new JPanel(new BorderLayout());
        SubPanel innerPanel = new SubPanel();

        JButton buttonSelectLabelFont = new JButton(new SelectLabelFontAction());   
        JButton buttonEditStroke = new JButton(new EditStrokeAction()); 
        JButton buttonSelectLabelPaint = new JButton(new SelectLabelPaintAction());

        innerPanel.addComponent(new JLabel("Show"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(boxScalebarVisible, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        innerPanel.addComponent(new JLabel("Label"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(boxLabelVisible, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        innerPanel.addComponent(new JLabel("Font"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(fieldLabelFont, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        innerPanel.addComponent(buttonSelectLabelFont, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        innerPanel.addComponent(new JLabel("Label color"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(labelPaintSample, 1, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		
        innerPanel.addComponent(buttonSelectLabelPaint, 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        innerPanel.addComponent(new JLabel("Stroke"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(strokeSample, 1, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        innerPanel.addComponent(buttonEditStroke, 2, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        innerPanel.addComponent(new JLabel("Length"), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);

        SubPanel panelLength = new SubPanel();
        panelLength.addComponent(boxAutomaticLength, 1, 5, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelLength.addComponent(new JLabel(""), 2, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     
        panelLength.addComponent(spinnerLength, 3, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);     
        innerPanel.addComponent(panelLength, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);

        SubPanel panelPosition = new SubPanel();

        panelPosition.addComponent(labelLeft, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);
        panelPosition.addComponent(sliderX, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,1, 1);
        panelPosition.addComponent(labelRight, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);

        panelPosition.addComponent(labelBottom, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);
        panelPosition.addComponent(sliderY, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelPosition.addComponent(labelTop, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);

        innerPanel.addComponent(labelPositionX, 0, 6, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,.05, 1);
        innerPanel.addComponent(panelPosition, 1, 6, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,1, 1);

        outerPanel.add(innerPanel, BorderLayout.NORTH);
        outerPanel.setBorder(BorderFactory.createEmptyBorder(8, 6, 6, 8));

        return outerPanel;
    }

    @Override
    public String getSubeditorName() 
    {
        return null;
    }

    @Override
    public void setNameBorder(boolean b) 
    {}

    private class EditStrokeAction extends AbstractAction
    {
        private static final long serialVersionUID = 4062159908259158952L;

        public EditStrokeAction()
        {
            putValue(NAME, "Edit");
        }

        @Override
        public void actionPerformed(ActionEvent e) 
        {
            attemptStrokeSelection();
        }       
    }

    private class SelectLabelFontAction extends AbstractAction
    {
        private static final long serialVersionUID = -8467706805301153140L;

        public SelectLabelFontAction()
        {
            putValue(NAME, "Select");
        }
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            attemptFontSelection();            
        }        
    }

    private class SelectLabelPaintAction extends AbstractAction
    {
        private static final long serialVersionUID = 5123374537632827289L;

        public SelectLabelPaintAction()
        {
            putValue(NAME, "Select");
        }
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            attemptLabelPaintSelection();            
        }        
    }
}
