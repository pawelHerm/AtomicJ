package atomicJ.gui;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import atomicJ.utilities.SerializationUtilities;


public class PreferredScaleBarStyle implements PreferenceChangeListener
{
    public static final String SCALEBAR_LABEL_DECIMAL_SEPARATOR = "ScalebarLabelDecimalSeparator";
    public static final String SCALEBAR_LABEL_GROUPING_SEPARATOR = "ScalebarLabelGroupSeparator";
    public static final String SCALEBAR_LABEL_GROUPING_USED = "ScalebarLabelGroupingUsed";
    public static final String SCALEBAR_LABEL_POSITION = "ScalebarLabelPosition";
    public static final String SCALEBAR_LABEL_OFFSET = "ScaleBarLabelOffset";
    public static final String SCALEBAR_LABEL_PAINT = "ScaleBarLabelPaint";
    public static final String SCALEBAR_LABEL_FONT = "ScaleBarLabelFont";
    public static final String SCALEBAR_LABEL_VISIBLE = "ScaleBarLabelVisible";
    public static final String SCALEBAR_POSITION_Y = "ScaleBarPositionY";
    public static final String SCALEBAR_POSITION_X = "ScaleBarPositionX";
    public static final String SCALEBAR_STROKE = "ScaleBarStroke";
    public static final String SCALEBAR_STROKE_PAINT = "ScaleBarStrokePaint";
    public static final String SCALEBAR_LENGTH_AUTOMATIC = "ScaleBarLengthAutomatic";
    public static final String SCALEBAR_VISIBLE = "ScaleBarVisible";

    private boolean visible;

    private float labelOffset;
    private float lengthwisePosition;

    private boolean labelVisible;   
    private Stroke stroke;
    private Paint strokePaint;      
    private Paint labelPaint;   
    private Font labelFont;

    private double x;
    private double y;

    private boolean lengthAutomatic;
    private double length;

    private final Preferences pref;

    public PreferredScaleBarStyle(Preferences pref)
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        this.visible = pref.getBoolean(PreferredScaleBarStyle.SCALEBAR_VISIBLE, false);
        this.lengthAutomatic = pref.getBoolean(PreferredScaleBarStyle.SCALEBAR_LENGTH_AUTOMATIC, true);

        this.strokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredScaleBarStyle.SCALEBAR_STROKE_PAINT, Color.black);
        this.stroke = SerializationUtilities.getStroke(pref, PreferredScaleBarStyle.SCALEBAR_STROKE, new BasicStroke(3.f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        this.labelPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredScaleBarStyle.SCALEBAR_LABEL_PAINT, Color.black);
        this.labelFont = (Font)SerializationUtilities.getSerializableObject(pref, PreferredScaleBarStyle.SCALEBAR_LABEL_FONT, new Font("Dialog", Font.PLAIN, 14));

        this.x = pref.getDouble(PreferredScaleBarStyle.SCALEBAR_POSITION_X, 0.8);
        this.y = pref.getDouble(PreferredScaleBarStyle.SCALEBAR_POSITION_Y, 0.1);

        this.labelVisible = pref.getBoolean(PreferredScaleBarStyle.SCALEBAR_LABEL_VISIBLE, true);
        this.labelOffset = pref.getFloat(PreferredScaleBarStyle.SCALEBAR_LABEL_OFFSET, 0.f);
        this.lengthwisePosition = pref.getFloat(PreferredScaleBarStyle.SCALEBAR_LABEL_POSITION, 0.5f);
    }

    public Preferences getPreferences()
    {
        return pref;
    }

    public double getPositionX()
    {
        return x;
    }

    public double getPositionY()
    {
        return y;
    }

    public double getLength()
    {
        return length;
    }

    public boolean isLengthAutomatic()
    {
        return lengthAutomatic;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public boolean isLabelVisible()
    {
        return labelVisible;
    }   

    public Paint getLabelPaint()
    {       
        return labelPaint;
    }

    public Paint getStrokePaint()
    {
        return strokePaint;
    }

    public Stroke getStroke()
    {
        return stroke;
    }

    public Font getLabelFont()
    {
        return labelFont;
    }

    public float getLabelLengthwisePosition()
    {
        return lengthwisePosition;
    }

    public float getLabelOffset()
    {
        return labelOffset;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        String key = evt.getKey();

        if(PreferredScaleBarStyle.SCALEBAR_VISIBLE.equals(key))
        {
            this.visible = pref.getBoolean(PreferredScaleBarStyle.SCALEBAR_VISIBLE, false);
        }
        else if(PreferredScaleBarStyle.SCALEBAR_LENGTH_AUTOMATIC.equals(key))
        {
            this.lengthAutomatic = pref.getBoolean(PreferredScaleBarStyle.SCALEBAR_LENGTH_AUTOMATIC, true);
        }
        else if(PreferredScaleBarStyle.SCALEBAR_STROKE_PAINT.equals(key))
        {
            this.strokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredScaleBarStyle.SCALEBAR_STROKE_PAINT, Color.black);
        }
        else if(PreferredScaleBarStyle.SCALEBAR_STROKE.equals(key))
        {
            this.stroke = SerializationUtilities.getStroke(pref, PreferredScaleBarStyle.SCALEBAR_STROKE, new BasicStroke(3.f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        }
        else if(PreferredScaleBarStyle.SCALEBAR_LABEL_PAINT.equals(key))
        {
            this.labelPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredScaleBarStyle.SCALEBAR_LABEL_PAINT, Color.black);
        }
        else if(PreferredScaleBarStyle.SCALEBAR_LABEL_FONT.equals(key))
        {
            this.labelFont = (Font)SerializationUtilities.getSerializableObject(pref, PreferredScaleBarStyle.SCALEBAR_LABEL_FONT, new Font("Dialog", Font.PLAIN, 14));
        }
        else if(PreferredScaleBarStyle.SCALEBAR_POSITION_X.equals(key))
        {
            this.x = pref.getDouble(PreferredScaleBarStyle.SCALEBAR_POSITION_X, 0.8);
        }
        else if(PreferredScaleBarStyle.SCALEBAR_POSITION_Y.equals(key))
        {
            this.y = pref.getDouble(PreferredScaleBarStyle.SCALEBAR_POSITION_Y, 0.1);
        }
        else if(PreferredScaleBarStyle.SCALEBAR_LABEL_VISIBLE.equals(key))
        {
            this.labelVisible = pref.getBoolean(PreferredScaleBarStyle.SCALEBAR_LABEL_VISIBLE, true);
        }
        else if(PreferredScaleBarStyle.SCALEBAR_LABEL_OFFSET.equals(key))
        {
            this.labelOffset = pref.getFloat(PreferredScaleBarStyle.SCALEBAR_LABEL_OFFSET, 0.f);
        }
        else if(PreferredScaleBarStyle.SCALEBAR_LABEL_POSITION.equals(key))
        {
            this.lengthwisePosition = pref.getFloat(PreferredScaleBarStyle.SCALEBAR_LABEL_POSITION, 0.5f);
        }
    }
}
