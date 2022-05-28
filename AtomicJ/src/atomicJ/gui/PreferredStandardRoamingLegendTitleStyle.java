package atomicJ.gui;

import java.awt.Color;
import java.awt.Paint;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import atomicJ.utilities.SerializationUtilities;


public class PreferredStandardRoamingLegendTitleStyle extends PreferredRoamingTitleLegendStyle
{
    private Paint backgroundPaint;

    private static final Map<String, PreferredStandardRoamingLegendTitleStyle> INSTANCES = new LinkedHashMap<>();

    public PreferredStandardRoamingLegendTitleStyle(Preferences pref, String styleKey) 
    {
        super(pref, styleKey);

        Paint defaultBackgoundPaint = Color.white;  
        this.backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_BACKGROUND_PAINT, defaultBackgoundPaint);
    }

    public static PreferredStandardRoamingLegendTitleStyle getInstance(Preferences pref, String styleKey)
    {
        String key = pref.absolutePath() + styleKey;
        PreferredStandardRoamingLegendTitleStyle style = INSTANCES.get(key);

        if(style == null)
        {
            style = new PreferredStandardRoamingLegendTitleStyle(pref, styleKey);
            INSTANCES.put(key, style);
        }

        return style;
    }

    public Paint getBackgroundPaint()
    {
        return backgroundPaint;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        super.preferenceChange(evt);
        String key = evt.getKey();

        if(PreferredRoamingTitleLegendStyle.LEGEND_BACKGROUND_PAINT.equals(key))
        {
            Paint defaultBackgoundPaint = Color.white;  
            this.backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(getPreferences(), PreferredRoamingTitleLegendStyle.LEGEND_BACKGROUND_PAINT, defaultBackgoundPaint);
        }      
    }
}
