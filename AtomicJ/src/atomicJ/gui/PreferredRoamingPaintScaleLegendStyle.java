package atomicJ.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import atomicJ.utilities.SerializationUtilities;


public class PreferredRoamingPaintScaleLegendStyle extends PreferredRoamingTitleLegendStyle
{
    public static final String LEGEND_STRIP_WIDTH = "LegendStripWidth";
    public static final String LEGEND_STRIP_OUTLINE_PAINT = "LegendStripOutlinePaint";
    public static final String LEGEND_STRIP_OUTLINE_STROKE = "LegendStripOutlineStroke";
    public static final String LEGEND_STRIP_OUTLINE_VISIBLE = "LegendStripOutlineVisible";

    private Paint backgroundPaint;

    private Paint stripOutlinePaint;
    private Stroke stripOutlineStroke;
    private boolean stripOutlineVisible;
    private double stripWidth;

    private static final Map<String, PreferredRoamingPaintScaleLegendStyle> INSTANCES = new LinkedHashMap<>();

    public PreferredRoamingPaintScaleLegendStyle(Preferences pref, String styleKey) 
    {
        super(pref, styleKey);

        Paint defaultBackgoundPaint = new Color(0f, 0f, 0f, 0f);

        this.backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_BACKGROUND_PAINT, defaultBackgoundPaint);    
        this.stripOutlinePaint = (Paint) SerializationUtilities.getSerializableObject(pref, PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_OUTLINE_PAINT, Color.black);
        this.stripOutlineStroke = SerializationUtilities.getStroke(pref, PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_OUTLINE_STROKE, new BasicStroke(1.f));
        this.stripOutlineVisible = pref.getBoolean(PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_OUTLINE_VISIBLE, true);
        this.stripWidth = pref.getDouble(PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_WIDTH, 20);
    }

    public static PreferredRoamingPaintScaleLegendStyle getInstance(Preferences pref, String styleKey)
    {
        String key = pref.absolutePath() + styleKey;
        PreferredRoamingPaintScaleLegendStyle style = INSTANCES.get(key);

        if(style == null)
        {
            style = new PreferredRoamingPaintScaleLegendStyle(pref, styleKey);
            INSTANCES.put(key, style);
        }

        return style;
    }

    public Paint getBackgroundPaint()
    {
        return backgroundPaint;
    }

    public Paint getStripOutlinePaint()
    {
        return stripOutlinePaint;
    }

    public Stroke getStripOutlineStroke()
    {
        return stripOutlineStroke;
    }

    public boolean isStripOutlineVisible()
    {
        return stripOutlineVisible;
    }

    public double getStripWidth()
    {
        return stripWidth;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        super.preferenceChange(evt);
        String key = evt.getKey();

        Preferences pref = getPreferences();

        if(PreferredRoamingTitleLegendStyle.LEGEND_BACKGROUND_PAINT.equals(key))
        {
            Paint defaultBackgoundPaint = new Color(0f, 0f, 0f, 0f);
            this.backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(getPreferences(), PreferredRoamingTitleLegendStyle.LEGEND_BACKGROUND_PAINT, defaultBackgoundPaint);
        } 
        else if(PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_OUTLINE_PAINT.equals(key))
        {
            this.stripOutlinePaint = (Paint) SerializationUtilities.getSerializableObject(pref, PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_OUTLINE_PAINT, Color.black);
        }
        else if(PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_OUTLINE_STROKE.equals(key))
        {
            this.stripOutlineStroke = SerializationUtilities.getStroke(pref, PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_OUTLINE_STROKE, new BasicStroke(1.f));
        }
        else if(PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_OUTLINE_VISIBLE.equals(key))
        {
            this.stripOutlineVisible = pref.getBoolean(PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_OUTLINE_VISIBLE, true);
        }
        else if(PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_WIDTH.equals(key))
        {
            this.stripWidth = pref.getDouble(PreferredRoamingPaintScaleLegendStyle.LEGEND_STRIP_WIDTH, 20);
        }
    }
}
