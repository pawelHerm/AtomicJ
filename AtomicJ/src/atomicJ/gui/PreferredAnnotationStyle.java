
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

package atomicJ.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.Serializable;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import atomicJ.utilities.SerializationUtilities;


public class PreferredAnnotationStyle extends AbstractModel implements Cloneable, Serializable, PreferenceChangeListener
{
    private static final long serialVersionUID = 1L;


    public static final String ANNOTATION_VISIBLE = "AnnotationVisible";	

    public static final String ANNOTATION_LABEL_TYPE = "AnnotationLabelType";

    public static final String ANNOTATION_LABEL_LENGTHWISE_POSITION = "LabelLengthwisePosition";
    public static final String ANNOTATION_LABEL_OFFSET = "LabelOffset";

    public static final String ANNOTATION_LABEL_VISIBLE_UNFINISHED = "LabelVisibleUnfinishedStandard";
    public static final String ANNOTATION_LABEL_VISIBLE_UNFINISHED_HIGHLIGHTED = "iLabelVisibleUnfinishedHighlighted";
    public static final String ANNOTATION_LABEL_VISIBLE_FINISHED = "LabelVisibleFinishedStandard";
    public static final String ANNOTATION_LABEL_VISIBLE_FINISHED_HIGHLIGHTED = "LabelVisibleFinishedHighlighted";

    public static final String ANNOTATION_LABEL_FONT_UNFINISHED = "LabelFontUnfinishedStandard";
    public static final String ANNOTATION_LABEL_FONT_UNFINISHED_HIGHLIGHTED = "LabelFontUnfinishedHighlighted";
    public static final String ANNOTATION_LABEL_FONT_FINISHED = "LabelFontFinishedStandard";
    public static final String ANNOTATION_LABEL_FONT_FINISHED_HIGHLIGHTED = "LabelFontFinishedHighlighted";

    public static final String ANNOTATION_PAINT_LABEL_UNFINISHED = "PaintLabelUnfinishedStandard";
    public static final String ANNOTATION_PAINT_LABEL_UNFINISHED_HEIGHLIGHTED = "PaintLabelUnfinishedHighlighted";
    public static final String ANNOTATION_PAINT_LABEL_FINISHED = "PaintLabelFinishedStandard";
    public static final String ANNOTATION_PAINT_LABEL_FINISHED_HEIGHLIGHTED = "PaintLabelFinishedHighlighted";

    public static final String ANNOTATION_PAINT_FINISHED_HEIGHLIGHTED = "PaintFinishedHighlighted";
    public static final String ANNOTATION_PAINT_UNFINISHED_HEIGHLIGHTED = "PaintUnfinishedHighlighted";
    public static final String ANNOTATION_PAINT_FINISHED = "PaintFinishedStandard";
    public static final String ANNOTATION_PAINT_UNFINISHED = "RoiPaintUnfinishedStandard";

    public static final String ANNOTATION_STROKE_FINISHED_HEIGHLIGHTED = "StrokeFinishedHighlighted";
    public static final String ANNOTATION_STROKE_UNFINISHED_HEIGHLIGHTED = "StrokeUnfinishedHighlighted";
    public static final String ANNOTATION_STROKE_FINISHED = "StrokeFinishedStandard";
    public static final String ANNOTATION_STROKE_UNFINISHED = "StrokeUnfinishedStandard";

    public static final String ANNOTATION_STYLE_COMPLETELY_CHANGED = "AnnotationStyleCompletelyChanged";

    private final boolean visible = true;

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

    private final Paint defaultPaint;
    private final float defaultLabelOffset;
    private final float defaultLengtwisePosition;

    private final Preferences pref;



    protected PreferredAnnotationStyle(Preferences pref, Paint defaultPaint, float defaultLabelOffset,  float defaultLengtwisePosition) 
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        this.defaultPaint = defaultPaint;
        this.defaultLabelOffset = defaultLabelOffset;
        this.defaultLengtwisePosition = defaultLengtwisePosition;
        pullAnnotationPreferences();
    }  

    public Preferences getPreferences()
    {
        return pref;
    }

    private void pullAnnotationPreferences()
    {    	
        Font defaultLabelFontUnfinished = new Font("Dialog", Font.BOLD, 14);
        Font defaultLabelFontFinished = new Font("Dialog", Font.BOLD, 14);

        boolean defaultLabelVisibleUnfinished = true;
        boolean defaultLabelVisibleFinished = true;

        Paint defaultLabelUnfinishedStandardPaint = new Color(190, 16, 33);
        Paint defaultLabelUnfinishedHighlightedPaint = new Color(190, 16, 33);		

        Paint defaultLabelFinishedStandardPaint = defaultPaint;
        Paint defaultLabelFinishedHighlightedPaint =  defaultPaint;

        Paint defaultUnfinishedStandardPaint = new Color(190, 16, 33);
        Paint defaultUnfinishedHighlightedPaint = new Color(190, 16, 33);

        Paint defaultFinishedStandardPaint = defaultPaint;
        Paint defaultFinishedHighlightedPaint =  defaultPaint;

        Stroke defaultUnfinishedStandardStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        Stroke defaultUnfinishedHighlightedStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

        Stroke defaultFinishedStandardStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        Stroke defaultFinishedHighlightedStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

        this.labelType = LabelAutomaticType.valueOf(pref.get(ANNOTATION_LABEL_TYPE, LabelAutomaticType.INTEGERS.name()));
        this.labelOffset = pref.getFloat(ANNOTATION_LABEL_OFFSET, defaultLabelOffset);
        this.labelLengthwisePosition = pref.getFloat(ANNOTATION_LABEL_LENGTHWISE_POSITION, defaultLengtwisePosition);

        this.labelFontUnfinishedStandard = (Font)SerializationUtilities.getSerializableObject(pref, ANNOTATION_LABEL_FONT_UNFINISHED, defaultLabelFontUnfinished);
        this.labelFontUnfinishedHighlighted = (Font)SerializationUtilities.getSerializableObject(pref, ANNOTATION_LABEL_FONT_UNFINISHED_HIGHLIGHTED, defaultLabelFontUnfinished);
        this.labelFontFinishedStandard = (Font)SerializationUtilities.getSerializableObject(pref, ANNOTATION_LABEL_FONT_FINISHED, defaultLabelFontFinished);
        this.labelFontFinishedHighlighted = (Font)SerializationUtilities.getSerializableObject(pref, ANNOTATION_LABEL_FONT_FINISHED_HIGHLIGHTED, defaultLabelFontFinished);

        this.labelVisibleUnfinishedStandard = pref.getBoolean(ANNOTATION_LABEL_VISIBLE_UNFINISHED, defaultLabelVisibleUnfinished);     
        this.labelVisibleUnfinishedHighlighted = pref.getBoolean(ANNOTATION_LABEL_VISIBLE_UNFINISHED_HIGHLIGHTED, defaultLabelVisibleUnfinished);     
        this.labelVisibleFinishedStandard =  pref.getBoolean(ANNOTATION_LABEL_VISIBLE_FINISHED, defaultLabelVisibleFinished);
        this.labelVisibleFinishedHighlighted =  pref.getBoolean(ANNOTATION_LABEL_VISIBLE_FINISHED_HIGHLIGHTED , defaultLabelVisibleFinished);

        this.paintLabelUnfinishedStandard  = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_LABEL_UNFINISHED, defaultLabelUnfinishedStandardPaint);
        this.paintLabelUnfinishedHighlighted  = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_LABEL_UNFINISHED_HEIGHLIGHTED, defaultLabelUnfinishedHighlightedPaint);
        this.paintLabelFinishedStandard  = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_LABEL_FINISHED, defaultLabelFinishedStandardPaint);
        this.paintLabelFinishedHighlighted  = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_LABEL_FINISHED_HEIGHLIGHTED, defaultLabelFinishedHighlightedPaint);

        this.strokeUnfinishedStandard = SerializationUtilities.getStroke(pref, ANNOTATION_STROKE_UNFINISHED, defaultUnfinishedStandardStroke);
        this.strokeUnfinishedHightlighted = SerializationUtilities.getStroke(pref, ANNOTATION_STROKE_UNFINISHED_HEIGHLIGHTED, defaultUnfinishedHighlightedStroke);
        this.strokeFinishedStandard = SerializationUtilities.getStroke(pref, ANNOTATION_STROKE_FINISHED, defaultFinishedStandardStroke);
        this.strokeFinishedHighlighted = SerializationUtilities.getStroke(pref, ANNOTATION_STROKE_FINISHED_HEIGHLIGHTED, defaultFinishedHighlightedStroke);

        this.paintUnfinishedStandard = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_UNFINISHED, defaultUnfinishedStandardPaint);
        this.paintUnfinishedHightlighted = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_UNFINISHED_HEIGHLIGHTED, defaultUnfinishedHighlightedPaint);
        this.paintFinishedStandard = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_FINISHED, defaultFinishedStandardPaint);
        this.paintFinishedHightlighted = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_FINISHED_HEIGHLIGHTED, defaultFinishedHighlightedPaint);

    }

    public boolean isVisible()
    {
        return visible;
    }

    public Paint getPaintFinished()
    {
        return paintFinishedStandard;
    }

    public Paint getPaintFinishedHighlighted()
    {
        return paintFinishedHightlighted;
    }

    public Paint getPaintUnfinished()
    {
        return paintUnfinishedStandard;
    }


    public Paint getPaintUnfinishedHighlighted()
    {
        return paintUnfinishedHightlighted;
    }


    public Stroke getStrokeFinished()
    {
        return strokeFinishedStandard;
    }

    public Stroke getStrokeFinishedHighlighted()
    {
        return strokeFinishedHighlighted;
    }

    public Stroke getStrokeUnfinished()
    {
        return strokeUnfinishedStandard;
    }

    public Stroke getStrokeUnfinishedHighlighted()
    {
        return strokeUnfinishedHightlighted;
    }

    public boolean isLabelVisibleUnfinished()
    {
        return labelVisibleUnfinishedStandard;
    }

    public boolean isLabelVisibleUnfinishedHighlighted()
    {
        return labelVisibleUnfinishedHighlighted;
    }

    public boolean isLabelVisibleFinished()
    {
        return labelVisibleFinishedStandard;
    }

    public boolean isLabelVisibleFinishedHighlighted()
    {
        return labelVisibleFinishedHighlighted;
    }

    public Font getLabelFontUnfinished()
    {
        return labelFontUnfinishedStandard;
    }

    public Font getLabelFontUnfinishedHighlighted()
    {
        return labelFontUnfinishedHighlighted;
    }

    public Font getLabelFontFinished()
    {
        return labelFontFinishedStandard;
    }

    public Font getLabelFontFinishedHighlighted()
    {
        return labelFontFinishedHighlighted;
    }

    public Paint getPaintLabelFinished()
    {
        return paintLabelFinishedStandard;
    }


    public Paint getPaintLabelFinishedHighlighted()
    {
        return paintLabelFinishedHighlighted;
    }

    public Paint getPaintLabelUnfinished()
    {
        return paintLabelUnfinishedStandard;
    }

    public Paint getPaintLabelUnfinishedHighlighted()
    {
        return paintLabelUnfinishedHighlighted;
    }

    public LabelAutomaticType getLabelType()
    {
        return labelType;
    }

    public float getLabelLengthwisePosition()
    {
        return labelLengthwisePosition;
    }


    public float getLabelOffset()
    {
        return labelOffset;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        String key = evt.getKey();

        if(ANNOTATION_LABEL_TYPE.equals(key))
        {
            this.labelType = LabelAutomaticType.valueOf(pref.get(ANNOTATION_LABEL_TYPE, LabelAutomaticType.INTEGERS.name()));
        }
        else if(ANNOTATION_LABEL_OFFSET.equals(key))
        {
            this.labelOffset = pref.getFloat(ANNOTATION_LABEL_OFFSET, defaultLabelOffset);
        }
        else if(ANNOTATION_LABEL_LENGTHWISE_POSITION.equals(key))
        {
            this.labelLengthwisePosition = pref.getFloat(ANNOTATION_LABEL_LENGTHWISE_POSITION, defaultLengtwisePosition);
        }

        else if(ANNOTATION_LABEL_FONT_UNFINISHED.equals(key))
        {
            Font defaultLabelFontUnfinished = new Font("Dialog", Font.BOLD, 14);
            this.labelFontUnfinishedStandard = (Font)SerializationUtilities.getSerializableObject(pref, ANNOTATION_LABEL_FONT_UNFINISHED, defaultLabelFontUnfinished);
        }

        else if(ANNOTATION_LABEL_FONT_UNFINISHED_HIGHLIGHTED.equals(key))
        {
            Font defaultLabelFontUnfinished = new Font("Dialog", Font.BOLD, 14);
            this.labelFontUnfinishedHighlighted = (Font)SerializationUtilities.getSerializableObject(pref, ANNOTATION_LABEL_FONT_UNFINISHED_HIGHLIGHTED, defaultLabelFontUnfinished);
        }

        else if(ANNOTATION_LABEL_FONT_FINISHED.equals(key))
        {
            Font defaultLabelFontFinished = new Font("Dialog", Font.BOLD, 14);
            this.labelFontFinishedStandard = (Font)SerializationUtilities.getSerializableObject(pref, ANNOTATION_LABEL_FONT_FINISHED, defaultLabelFontFinished);
        }

        else if(ANNOTATION_LABEL_FONT_FINISHED_HIGHLIGHTED.equals(key))
        {
            Font defaultLabelFontFinished = new Font("Dialog", Font.BOLD, 14);
            this.labelFontFinishedHighlighted = (Font)SerializationUtilities.getSerializableObject(pref, ANNOTATION_LABEL_FONT_FINISHED_HIGHLIGHTED, defaultLabelFontFinished);
        }

        else if(ANNOTATION_LABEL_VISIBLE_UNFINISHED.equals(key))
        {
            boolean defaultLabelVisibleUnfinished = true;

            this.labelVisibleUnfinishedStandard = pref.getBoolean(ANNOTATION_LABEL_VISIBLE_UNFINISHED, defaultLabelVisibleUnfinished);     
        }

        else if(ANNOTATION_LABEL_VISIBLE_UNFINISHED_HIGHLIGHTED.equals(key))
        {
            boolean defaultLabelVisibleUnfinished = true;

            this.labelVisibleUnfinishedHighlighted = pref.getBoolean(ANNOTATION_LABEL_VISIBLE_UNFINISHED_HIGHLIGHTED, defaultLabelVisibleUnfinished);     
        }

        else if(ANNOTATION_LABEL_VISIBLE_FINISHED.equals(key))
        {
            boolean defaultLabelVisibleFinished = true;
            this.labelVisibleFinishedStandard = pref.getBoolean(ANNOTATION_LABEL_VISIBLE_FINISHED, defaultLabelVisibleFinished);
        }

        else if(ANNOTATION_LABEL_VISIBLE_FINISHED_HIGHLIGHTED.equals(key))
        {
            boolean defaultLabelVisibleFinished = true;
            this.labelVisibleFinishedHighlighted = pref.getBoolean(ANNOTATION_LABEL_VISIBLE_FINISHED_HIGHLIGHTED , defaultLabelVisibleFinished);
        }

        else if(ANNOTATION_PAINT_LABEL_UNFINISHED.equals(key))
        {
            Paint defaultLabelUnfinishedStandardPaint = new Color(190, 16, 33);
            this.paintLabelUnfinishedStandard  = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_LABEL_UNFINISHED, defaultLabelUnfinishedStandardPaint);
        }

        else if(ANNOTATION_PAINT_LABEL_UNFINISHED_HEIGHLIGHTED.equals(key))
        {
            Paint defaultLabelUnfinishedHighlightedPaint = new Color(190, 16, 33);      
            this.paintLabelUnfinishedHighlighted  = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_LABEL_UNFINISHED_HEIGHLIGHTED, defaultLabelUnfinishedHighlightedPaint);
        }

        else if(ANNOTATION_PAINT_LABEL_FINISHED.equals(key))
        {
            Paint defaultLabelFinishedStandardPaint = defaultPaint;
            this.paintLabelFinishedStandard  = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_LABEL_FINISHED, defaultLabelFinishedStandardPaint);

        }

        else if(ANNOTATION_PAINT_LABEL_FINISHED_HEIGHLIGHTED.equals(key))
        {
            Paint defaultLabelFinishedHighlightedPaint =  defaultPaint;
            this.paintLabelFinishedHighlighted  = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_LABEL_FINISHED_HEIGHLIGHTED, defaultLabelFinishedHighlightedPaint);
        }

        else if(ANNOTATION_STROKE_UNFINISHED.equals(key))
        {
            Stroke defaultUnfinishedStandardStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            this.strokeUnfinishedStandard = SerializationUtilities.getStroke(pref, ANNOTATION_STROKE_UNFINISHED, defaultUnfinishedStandardStroke);
        }

        else if(ANNOTATION_STROKE_UNFINISHED_HEIGHLIGHTED.equals(key))
        {
            Stroke defaultUnfinishedHighlightedStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            this.strokeUnfinishedHightlighted = SerializationUtilities.getStroke(pref, ANNOTATION_STROKE_UNFINISHED_HEIGHLIGHTED, defaultUnfinishedHighlightedStroke);
        }

        else if(ANNOTATION_STROKE_FINISHED.equals(key))
        {
            Stroke defaultFinishedStandardStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            this.strokeFinishedStandard = SerializationUtilities.getStroke(pref, ANNOTATION_STROKE_FINISHED, defaultFinishedStandardStroke);
        }

        else if(ANNOTATION_STROKE_FINISHED_HEIGHLIGHTED.equals(key))
        {
            Stroke defaultFinishedHighlightedStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            this.strokeFinishedHighlighted = SerializationUtilities.getStroke(pref, ANNOTATION_STROKE_FINISHED_HEIGHLIGHTED, defaultFinishedHighlightedStroke);
        }
        else if(ANNOTATION_PAINT_UNFINISHED.equals(key))
        {
            Paint defaultUnfinishedStandardPaint = new Color(190, 16, 33);
            this.paintUnfinishedStandard = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_UNFINISHED, defaultUnfinishedStandardPaint);
        }

        else if(ANNOTATION_PAINT_UNFINISHED_HEIGHLIGHTED.equals(key))
        {
            Paint defaultUnfinishedHighlightedPaint = new Color(190, 16, 33);
            this.paintUnfinishedHightlighted = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_UNFINISHED_HEIGHLIGHTED, defaultUnfinishedHighlightedPaint);
        }      

        else if(ANNOTATION_PAINT_FINISHED.equals(key))
        {
            Paint defaultFinishedStandardPaint = defaultPaint;
            this.paintFinishedStandard = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_FINISHED, defaultFinishedStandardPaint);
        }

        else if(ANNOTATION_PAINT_FINISHED_HEIGHLIGHTED.equals(key))
        {
            Paint defaultFinishedHighlightedPaint =  defaultPaint;
            this.paintFinishedHightlighted = (Paint)SerializationUtilities.getSerializableObject(pref, ANNOTATION_PAINT_FINISHED_HEIGHLIGHTED, defaultFinishedHighlightedPaint);
        }

    };
}
