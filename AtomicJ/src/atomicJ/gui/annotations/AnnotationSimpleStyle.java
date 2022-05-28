
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

package atomicJ.gui.annotations;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.IOException;
import java.io.Serializable;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import atomicJ.gui.AbstractModel;
import atomicJ.utilities.SerializationUtilities;



public class AnnotationSimpleStyle extends AbstractModel implements Cloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    public static final String ANNOTATION_VISIBLE = "AnnotationVisible";	

    public static final String ANNOTATION_LABEL_LENGTHWISE_POSITION = "AnnotationLabelLengthwisePosition";
    public static final String ANNOTATION_LABEL_OFFSET = "AnnotationLabelOffset";
    public static final String ANNOTATION_LABEL_VISIBLE = "AnnotationLabelVisible";
    public static final String ANNOTATION_LABEL_FONT = "AnnotationLabelFont";
    public static final String ANNOTATION_LABEL_PAINT = "AnnotationLabelPaint";

    public static final String ANNOTATION_STROKE_VISIBLE = "AnnotationStrokeVisible";
    public static final String ANNOTATION_STROKE = "AnnotationStroke";
    public static final String ANNOTATION_STROKE_PAINT = "AnnotationStrokePaint";

    public static final String ANNOTATION_STYLE_COMPLETELY_CHANGED = "AnnotationStyleCompletelyChanged";

    private boolean visible = true;

    private boolean strokeVisible;
    private Stroke stroke;
    private Paint strokePaint;

    private boolean labelVisible;     
    private float labelOffset;
    private float labelLengthwisePosition;
    private Paint labelPaint;
    private Font labelFont;

    private final Preferences pref;
    private final Paint defaultPaint;

    public AnnotationSimpleStyle(Preferences pref, Paint defaultPaint, float defaultLabelOffset,  float defaultLengtwisePosition) 
    {
        this.pref = pref;
        this.defaultPaint = defaultPaint;
        setDefaultAnnotationStyle(defaultLabelOffset, defaultLengtwisePosition);
    }  

    public Preferences getPreferences()
    {
        return pref;
    }

    private void setDefaultAnnotationStyle(float defaultLabelOffset, float defaultLabelLengthwisePosition)
    {    	
        Font defaultLabelFont = new Font("Dialog", Font.BOLD, 14);
        Stroke defaultStroke = new BasicStroke(2.0f);

        this.labelOffset = pref.getFloat(ANNOTATION_LABEL_OFFSET, defaultLabelOffset);
        this.labelLengthwisePosition = pref.getFloat(ANNOTATION_LABEL_LENGTHWISE_POSITION, defaultLabelLengthwisePosition);

        this.labelFont = (Font)SerializationUtilities.getSerializableObject(pref, ANNOTATION_LABEL_FONT, defaultLabelFont);
        this.labelVisible =  pref.getBoolean(ANNOTATION_LABEL_VISIBLE, true);
        this.labelPaint  = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_LABEL_PAINT, defaultPaint);

        this.strokeVisible = pref.getBoolean(ANNOTATION_STROKE_VISIBLE, true);
        this.stroke = SerializationUtilities.getStroke(pref, ANNOTATION_STROKE, defaultStroke);
        this.strokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_STROKE_PAINT, defaultPaint);

        firePropertyChange(ANNOTATION_STYLE_COMPLETELY_CHANGED, false, true);
    }

    public void setDefaultStyle(float defaultLabelOffset, float defaultLabelLengthwisePosition)
    {
        setDefaultAnnotationStyle(defaultLabelOffset, defaultLabelLengthwisePosition);
    }	

    public void saveStyleAsDefault()
    {
        pref.putFloat(ANNOTATION_LABEL_OFFSET, labelOffset);
        pref.putFloat(ANNOTATION_LABEL_LENGTHWISE_POSITION, labelLengthwisePosition);

        pref.putBoolean(ANNOTATION_LABEL_VISIBLE, labelVisible);

        try 
        {	
            SerializationUtilities.putSerializableObject(pref, ANNOTATION_LABEL_FONT, labelFont);
            SerializationUtilities.putSerializableObject(pref, ANNOTATION_LABEL_PAINT, labelPaint);
            SerializationUtilities.putStroke(pref, ANNOTATION_STROKE, stroke);
            SerializationUtilities.putSerializableObject(pref, ANNOTATION_STROKE_PAINT, strokePaint);
        }
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }	
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean visibleNew)
    {
        boolean visibleOld = visible;
        this.visible = visibleNew;

        firePropertyChange(ANNOTATION_VISIBLE, visibleOld, visibleNew);
    }

    public Paint getPaint()
    {
        return strokePaint;
    }

    public void setPaint(Paint paintNew)
    {
        Paint paintOld = this.strokePaint;
        this.strokePaint = paintNew;

        firePropertyChange(ANNOTATION_STROKE_PAINT, paintOld, paintNew);	
    }

    public boolean isStrokeVisible()
    {
        return strokeVisible;
    }

    public void setStrokeVisible(boolean strokeVisibleNew)
    {
        boolean strokeVisibleOld = this.strokeVisible;
        this.strokeVisible = strokeVisibleNew;

        firePropertyChange(ANNOTATION_STROKE_VISIBLE, strokeVisibleOld, strokeVisibleNew);
    }

    public Stroke getStroke()
    {
        return stroke;
    }

    public void setStroke(Stroke strokeNew)
    {
        Stroke strokeOld = this.stroke;
        this.stroke = strokeNew;

        firePropertyChange(ANNOTATION_STROKE, strokeOld, strokeNew);	
    }

    public boolean isLabelVisible()
    {
        return labelVisible;
    }

    public void setLabelVisible(boolean labelVisibleNew)
    {
        boolean labelVisibleOld = this.labelVisible;
        this.labelVisible = labelVisibleNew;

        firePropertyChange(ANNOTATION_LABEL_VISIBLE, labelVisibleOld, labelVisibleNew);
    }

    public Font getLabelFont()
    {
        return labelFont;
    }

    public void setLabelFont(Font labelFontNew)
    {
        Font labelFontOld = this.labelFont;
        this.labelFont = labelFontNew;

        firePropertyChange(ANNOTATION_LABEL_FONT, labelFontOld, labelFontNew);	
    }

    public Paint getLabelPaint()
    {
        return labelPaint;
    }

    public void setPaintLabel(Paint labelPaintNew)
    {
        Paint labelPaintOld = this.labelPaint;
        this.labelPaint = labelPaintNew;

        firePropertyChange(ANNOTATION_LABEL_PAINT, labelPaintOld, labelPaintNew);	
    }

    public float getLabelLengthwisePosition()
    {
        return labelLengthwisePosition;
    }

    public void setLabelLengthwisePosition(float labelLengthwisePositionNew)
    {
        float labelLengthwisePositionOld = this.labelLengthwisePosition;
        this.labelLengthwisePosition = labelLengthwisePositionNew;

        firePropertyChange(ANNOTATION_LABEL_LENGTHWISE_POSITION, labelLengthwisePositionOld, labelLengthwisePositionNew);	
    }

    public float getLabelOffset()
    {
        return labelOffset;
    }

    public void setLabelOffset(float labelOffsetNew)
    {
        float labelOffsetOld = labelOffset;
        this.labelOffset = labelOffsetNew;

        firePropertyChange(ANNOTATION_LABEL_OFFSET, labelOffsetOld, labelOffsetNew);
    }
}
