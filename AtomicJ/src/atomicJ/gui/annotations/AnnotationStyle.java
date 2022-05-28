
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

import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.IOException;
import java.io.Serializable;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import atomicJ.gui.AbstractModel;
import atomicJ.gui.LabelAutomaticType;
import atomicJ.gui.PreferredAnnotationStyle;
import atomicJ.utilities.SerializationUtilities;
import static atomicJ.gui.PreferredAnnotationStyle.*;



public class AnnotationStyle <E extends PreferredAnnotationStyle> extends AbstractModel implements Cloneable, Serializable 
{
    private static final long serialVersionUID = 1L;

    public static final String ANNOTATION_STYLE_COMPLETELY_CHANGED = "AnnotationStyleCompletelyChanged";

    private boolean visible = true;

    private boolean labelVisibleUnfinishedStandard;     
    private boolean labelVisibleUnfinishedHighlighted;     
    private boolean labelVisibleFinishedStandard;     
    private boolean labelVisibleFinishedHighlighted;     

    private Font labelFontUnfinishedStandard;
    private Font labelFontUnfinishedHighlighted;
    private Font labelFontFinishedStandard;
    private Font labelFontFinishedHighlighted;

    private LabelAutomaticType labelType;
    private float labelOffset;
    private float labelLengthwisePosition;

    private Paint paintLabelFinishedHighlighted;
    private Paint paintLabelUnfinishedHighlighted;
    private Paint paintLabelFinishedStandard;
    private Paint paintLabelUnfinishedStandard;

    private Stroke strokeUnfinishedStandard;
    private Stroke strokeUnfinishedHightlighted;
    private Stroke strokeFinishedStandard;
    private Stroke strokeFinishedHighlighted;

    private Paint paintUnfinishedStandard;
    private Paint paintUnfinishedHightlighted;
    private Paint paintFinishedStandard;
    private Paint paintFinishedHightlighted;	

    private final E prefStyle;

    public AnnotationStyle(E prefStyle) 
    {
        this.prefStyle = prefStyle;
        setPreferredAnnotationStyle(prefStyle);
    }  

    public E getPreferredStyle()
    {
        return prefStyle;
    }

    public Preferences getPreferences()
    {
        return prefStyle.getPreferences();
    }

    private void setPreferredAnnotationStyle(PreferredAnnotationStyle prefStyle)
    {    	
        this.labelType = prefStyle.getLabelType();
        this.labelOffset = prefStyle.getLabelOffset();
        this.labelLengthwisePosition = prefStyle.getLabelLengthwisePosition();

        this.labelFontUnfinishedStandard = prefStyle.getLabelFontUnfinished();
        this.labelFontUnfinishedHighlighted = prefStyle.getLabelFontUnfinishedHighlighted();
        this.labelFontFinishedStandard = prefStyle.getLabelFontFinished();
        this.labelFontFinishedHighlighted = prefStyle.getLabelFontFinishedHighlighted();

        this.labelVisibleUnfinishedStandard = prefStyle.isLabelVisibleUnfinished();     
        this.labelVisibleUnfinishedHighlighted = prefStyle.isLabelVisibleUnfinishedHighlighted();     
        this.labelVisibleFinishedStandard = prefStyle.isLabelVisibleFinished();
        this.labelVisibleFinishedHighlighted =  prefStyle.isLabelVisibleFinishedHighlighted();

        this.paintLabelUnfinishedStandard  = prefStyle.getPaintLabelUnfinished();
        this.paintLabelUnfinishedHighlighted  = prefStyle.getPaintLabelUnfinishedHighlighted();
        this.paintLabelFinishedStandard  = prefStyle.getPaintLabelFinished();
        this.paintLabelFinishedHighlighted  = prefStyle.getPaintLabelFinishedHighlighted();

        this.strokeUnfinishedStandard = prefStyle.getStrokeUnfinished();
        this.strokeUnfinishedHightlighted = prefStyle.getStrokeUnfinishedHighlighted();
        this.strokeFinishedStandard = prefStyle.getStrokeFinished();
        this.strokeFinishedHighlighted = prefStyle.getStrokeFinishedHighlighted();

        this.paintUnfinishedStandard = prefStyle.getPaintUnfinished();
        this.paintUnfinishedHightlighted = prefStyle.getPaintUnfinishedHighlighted();
        this.paintFinishedStandard = prefStyle.getPaintFinished();
        this.paintFinishedHightlighted = prefStyle.getPaintFinishedHighlighted();

        firePropertyChange(ANNOTATION_STYLE_COMPLETELY_CHANGED, false, true);
    }	

    public void setDefaultStyle()
    {
        setPreferredAnnotationStyle(prefStyle);
    }

    public void saveStyleAsDefault()
    {
        Preferences pref = prefStyle.getPreferences();

        pref.put(ANNOTATION_LABEL_TYPE, labelType.name());
        pref.putFloat(ANNOTATION_LABEL_OFFSET, labelOffset);
        pref.putFloat(ANNOTATION_LABEL_LENGTHWISE_POSITION, labelLengthwisePosition);


        pref.putBoolean(ANNOTATION_LABEL_VISIBLE_UNFINISHED, labelVisibleUnfinishedStandard);     
        pref.putBoolean(ANNOTATION_LABEL_VISIBLE_UNFINISHED_HIGHLIGHTED, labelVisibleUnfinishedHighlighted);     
        pref.putBoolean(ANNOTATION_LABEL_VISIBLE_FINISHED, labelVisibleFinishedStandard);
        pref.putBoolean(ANNOTATION_LABEL_VISIBLE_FINISHED_HIGHLIGHTED, labelVisibleFinishedHighlighted);


        try 
        {	
            SerializationUtilities.putSerializableObject(pref, ANNOTATION_LABEL_FONT_UNFINISHED, labelFontUnfinishedStandard);
            SerializationUtilities.putSerializableObject(pref, ANNOTATION_LABEL_FONT_UNFINISHED_HIGHLIGHTED, labelFontUnfinishedHighlighted);
            SerializationUtilities.putSerializableObject(pref, ANNOTATION_LABEL_FONT_FINISHED, labelFontFinishedStandard);
            SerializationUtilities.putSerializableObject(pref, ANNOTATION_LABEL_FONT_FINISHED_HIGHLIGHTED, labelFontFinishedHighlighted);

            SerializationUtilities.putSerializableObject(pref, ANNOTATION_PAINT_LABEL_UNFINISHED, paintLabelUnfinishedStandard);
            SerializationUtilities.putSerializableObject(pref, ANNOTATION_PAINT_LABEL_UNFINISHED_HEIGHLIGHTED, paintLabelUnfinishedHighlighted);
            SerializationUtilities.putSerializableObject(pref, ANNOTATION_PAINT_LABEL_FINISHED, paintLabelFinishedStandard);
            SerializationUtilities.putSerializableObject(pref, ANNOTATION_PAINT_LABEL_FINISHED_HEIGHLIGHTED, paintLabelFinishedHighlighted);

            SerializationUtilities.putStroke(pref, ANNOTATION_STROKE_UNFINISHED, strokeUnfinishedStandard);
            SerializationUtilities.putStroke(pref, ANNOTATION_STROKE_UNFINISHED_HEIGHLIGHTED, strokeUnfinishedHightlighted);
            SerializationUtilities.putStroke(pref, ANNOTATION_STROKE_FINISHED, strokeFinishedStandard);
            SerializationUtilities.putStroke(pref, ANNOTATION_STROKE_FINISHED_HEIGHLIGHTED, strokeFinishedHighlighted);

            SerializationUtilities.putSerializableObject(pref, ANNOTATION_PAINT_UNFINISHED, paintUnfinishedStandard);
            SerializationUtilities.putSerializableObject(pref, ANNOTATION_PAINT_UNFINISHED_HEIGHLIGHTED, paintUnfinishedHightlighted);
            SerializationUtilities.putSerializableObject(pref, ANNOTATION_PAINT_FINISHED, paintFinishedStandard);
            SerializationUtilities.putSerializableObject(pref, ANNOTATION_PAINT_FINISHED_HEIGHLIGHTED, paintFinishedHightlighted);
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

    public Paint getPaintFinished()
    {
        return paintFinishedStandard;
    }

    public void setPaintFinishedStandard(Paint paintFinishedStandardNew)
    {
        Paint paintFinishedStandardOld = this.paintFinishedStandard;
        this.paintFinishedStandard = paintFinishedStandardNew;

        firePropertyChange(ANNOTATION_PAINT_FINISHED, paintFinishedStandardOld, paintFinishedStandardNew);	
    }

    public Paint getPaintFinishedHighlighted()
    {
        return paintFinishedHightlighted;
    }

    public void setPaintFinishedHighlighted(Paint paintFinishedHighlightedNew)
    {
        Paint paintFinishedHighlightedOld = this.paintFinishedHightlighted;
        this.paintFinishedHightlighted = paintFinishedHighlightedNew;

        firePropertyChange(ANNOTATION_PAINT_FINISHED_HEIGHLIGHTED, paintFinishedHighlightedOld, paintFinishedHighlightedNew);
    }

    public Paint getPaintUnfinished()
    {
        return paintUnfinishedStandard;
    }

    public void setPaintUnfinishedStandard(Paint paintUnfinishedStandardNew)
    {
        Paint paintUnfinishedStandardOld = this.paintUnfinishedStandard;
        this.paintUnfinishedStandard = paintUnfinishedStandardNew;

        firePropertyChange(ANNOTATION_PAINT_UNFINISHED, paintUnfinishedStandardOld, paintUnfinishedStandardNew);	
    }

    public Paint getPaintUnfinishedHighlighted()
    {
        return paintUnfinishedHightlighted;
    }

    public void setPaintUnfinishedHighlighted(Paint paintUnfinishedHighlightedNew)
    {
        Paint paintUnfinishedHighlightedOld = this.paintUnfinishedHightlighted;
        this.paintUnfinishedHightlighted = paintUnfinishedHighlightedNew;

        firePropertyChange(ANNOTATION_PAINT_UNFINISHED_HEIGHLIGHTED, paintUnfinishedHighlightedOld, paintUnfinishedHighlightedNew);
    }	


    public Stroke getStrokeFinished()
    {
        return strokeFinishedStandard;
    }

    public void setStrokeFinishedStandard(Stroke strokeFinishedStandardNew)
    {
        Stroke strokeFinishedStandardOld = this.strokeFinishedStandard;
        this.strokeFinishedStandard = strokeFinishedStandardNew;

        firePropertyChange(ANNOTATION_STROKE_FINISHED, strokeFinishedStandardOld, strokeFinishedStandardNew);	
    }

    public Stroke getStrokeFinishedHighlighted()
    {
        return strokeFinishedHighlighted;
    }

    public void setStrokeFinishedHighlighted(Stroke strokeFinishedHighlightedNew)
    {
        Stroke strokeFinishedHighlightedOld = this.strokeFinishedHighlighted;
        this.strokeFinishedHighlighted = strokeFinishedHighlightedNew;

        firePropertyChange(ANNOTATION_STROKE_FINISHED_HEIGHLIGHTED, strokeFinishedHighlightedOld, strokeFinishedHighlightedNew);
    }

    public Stroke getStrokeUnfinished()
    {
        return strokeUnfinishedStandard;
    }

    public void setStrokeUnfinishedStandard(Stroke strokeUnfinishedStandardNew)
    {
        Stroke strokeUnfinishedStandardOld = this.strokeUnfinishedStandard;
        this.strokeUnfinishedStandard = strokeUnfinishedStandardNew;

        firePropertyChange(ANNOTATION_STROKE_UNFINISHED, strokeUnfinishedStandardOld, strokeUnfinishedStandardNew);	
    }

    public Stroke getStrokeUnfinishedHighlighted()
    {
        return strokeUnfinishedHightlighted;
    }

    public void setStrokeUnfinishedHighlighted(Stroke strokeUnfinishedHighlightedNew)
    {
        Stroke strokeUnfinishedHighlightedOld = this.strokeUnfinishedHightlighted;
        this.strokeUnfinishedHightlighted = strokeUnfinishedHighlightedNew;

        firePropertyChange(ANNOTATION_STROKE_UNFINISHED_HEIGHLIGHTED, strokeUnfinishedHighlightedOld, strokeUnfinishedHighlightedNew);
    }

    public boolean isLabelVisibleUnfinished()
    {
        return labelVisibleUnfinishedStandard;
    }

    public void setLabelVisibleUnfinishedStandard(boolean labelVisibleNew)
    {
        boolean labelVisibleOld = this.labelVisibleUnfinishedStandard;
        this.labelVisibleUnfinishedStandard = labelVisibleNew;

        firePropertyChange(ANNOTATION_LABEL_VISIBLE_UNFINISHED, labelVisibleOld, labelVisibleNew);
    }

    public boolean isLabelVisibleUnfinishedHighlighted()
    {
        return labelVisibleUnfinishedHighlighted;
    }

    public void setLabelVisibleUnfinishedHighlighted(boolean labelVisibleNew)
    {
        boolean labelVisibleOld = this.labelVisibleUnfinishedHighlighted;
        this.labelVisibleUnfinishedHighlighted = labelVisibleNew;

        firePropertyChange(ANNOTATION_LABEL_VISIBLE_UNFINISHED_HIGHLIGHTED, labelVisibleOld, labelVisibleNew);
    }

    public boolean isLabelVisibleFinished()
    {
        return labelVisibleFinishedStandard;
    }

    public void setLabelVisibleFinishedStandard(boolean labelVisibleNew)
    {
        boolean labelVisibleOld = this.labelVisibleFinishedStandard;
        this.labelVisibleFinishedStandard = labelVisibleNew;

        firePropertyChange(ANNOTATION_LABEL_VISIBLE_FINISHED, labelVisibleOld, labelVisibleNew);
    }

    public boolean isLabelVisibleFinishedHighlighted()
    {
        return labelVisibleFinishedHighlighted;
    }

    public void setLabelVisibleFinishedHighlighted(boolean labelVisibleNew)
    {
        boolean labelVisibleOld = this.labelVisibleFinishedHighlighted;
        this.labelVisibleFinishedHighlighted = labelVisibleNew;

        firePropertyChange(ANNOTATION_LABEL_VISIBLE_FINISHED_HIGHLIGHTED, labelVisibleOld, labelVisibleNew);
    }

    public Font getLabelFontUnfinished()
    {
        return labelFontUnfinishedStandard;
    }

    public void setLabelFontUnfinishedStandard(Font labelFontNew)
    {
        Font labelFontOld = this.labelFontUnfinishedStandard;
        this.labelFontUnfinishedStandard = labelFontNew;

        firePropertyChange(ANNOTATION_LABEL_FONT_UNFINISHED, labelFontOld, labelFontNew);	
    }

    public Font getLabelFontUnfinishedHighlighted()
    {
        return labelFontUnfinishedHighlighted;
    }

    public void setLabelFontUnfinishedHighlighted(Font labelFontNew)
    {
        Font labelFontOld = this.labelFontUnfinishedHighlighted;
        this.labelFontUnfinishedHighlighted = labelFontNew;

        firePropertyChange(ANNOTATION_LABEL_FONT_UNFINISHED_HIGHLIGHTED, labelFontOld, labelFontNew);	
    }


    public Font getLabelFontFinished()
    {
        return labelFontFinishedStandard;
    }

    public void setLabelFontFinishedStandard(Font labelFontNew)
    {
        Font labelFontOld = this.labelFontFinishedStandard;
        this.labelFontFinishedStandard = labelFontNew;

        firePropertyChange(ANNOTATION_LABEL_FONT_FINISHED, labelFontOld, labelFontNew);	
    }

    public Font getLabelFontFinishedHighlighted()
    {
        return labelFontFinishedHighlighted;
    }

    public void setLabelFontFinishedHighlighted(Font labelFontNew)
    {
        Font labelFontOld = this.labelFontFinishedHighlighted;
        this.labelFontFinishedHighlighted = labelFontNew;

        firePropertyChange(ANNOTATION_LABEL_FONT_FINISHED_HIGHLIGHTED, labelFontOld, labelFontNew);	
    }


    public Paint getPaintLabelFinished()
    {
        return paintLabelFinishedStandard;
    }

    public void setPaintLabelFinishedStandard(Paint paintLabelFinishedStandardNew)
    {
        Paint paintLabelFinishedStandardOld = this.paintLabelFinishedStandard;
        this.paintLabelFinishedStandard = paintLabelFinishedStandardNew;

        firePropertyChange(ANNOTATION_PAINT_LABEL_FINISHED, paintLabelFinishedStandardOld, paintLabelFinishedStandardNew);	
    }

    public Paint getPaintLabelFinishedHighlighted()
    {
        return paintLabelFinishedHighlighted;
    }

    public void setPaintLabelFinishedHighlighted(Paint paintLabelFinishedHighlightedNew)
    {
        Paint paintLabelFinishedHighlightedOld = this.paintLabelFinishedHighlighted;
        this.paintLabelFinishedHighlighted = paintLabelFinishedHighlightedNew;

        firePropertyChange(ANNOTATION_PAINT_LABEL_FINISHED_HEIGHLIGHTED, paintLabelFinishedHighlightedOld, paintLabelFinishedHighlightedNew);
    }

    public Paint getPaintLabelUnfinished()
    {
        return paintLabelUnfinishedStandard;
    }

    public void setPaintLabelUnfinishedStandard(Paint paintLabelUnfinishedStandardNew)
    {
        Paint paintLabelUnfinishedStandardOld = this.paintLabelUnfinishedStandard;
        this.paintLabelUnfinishedStandard = paintLabelUnfinishedStandardNew;

        firePropertyChange(ANNOTATION_PAINT_LABEL_UNFINISHED, paintLabelUnfinishedStandardOld, paintLabelUnfinishedStandardNew);	
    }

    public Paint getPaintLabelUnfinishedHighlighted()
    {
        return paintLabelUnfinishedHighlighted;
    }

    public void setPaintLabelUnfinishedHighlighted(Paint paintLabelUnfinishedHighlightedNew)
    {
        Paint paintLabelUnfinishedHighlightedOld = this.paintLabelUnfinishedHighlighted;
        this.paintLabelUnfinishedHighlighted = paintLabelUnfinishedHighlightedNew;

        firePropertyChange(ANNOTATION_PAINT_LABEL_UNFINISHED_HEIGHLIGHTED, paintLabelUnfinishedHighlightedOld, paintLabelUnfinishedHighlightedNew);
    }

    public LabelAutomaticType getLabelType()
    {
        return labelType;
    }

    public void setLabelType(LabelAutomaticType labelTypeNew)
    {
        LabelAutomaticType labelTypeOld = this.labelType;
        this.labelType = labelTypeNew;

        firePropertyChange(ANNOTATION_LABEL_TYPE, labelTypeOld, labelTypeNew);
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
