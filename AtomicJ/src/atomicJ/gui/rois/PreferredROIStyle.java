package atomicJ.gui.rois;

import java.awt.Color;
import java.awt.Paint;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import atomicJ.gui.PreferredAnnotationStyle;
import atomicJ.utilities.MetaMap;
import atomicJ.utilities.SerializationUtilities;

public class PreferredROIStyle extends PreferredAnnotationStyle 
{
    public static final String ROI_OUTLINE_VISIBLE = "RoiOutlineVisible";

    public static final String ROI_FILLED_UNFINISHED_STANDARD = "RoiFilledUnfinishedStandard";
    public static final String ROI_FILLED_FINISHED_STANDARD = "RoiFilledFinishedStandard";

    public static final String ROI_PAINT_FILL_FINISHED_STANDARD = "RoiPaintFillFinishedStandard";
    public static final String ROI_PAINT_FILL_UNFINISHED_STANDARD = "RoiPaintFillUnfinishedStandard";

    private static final float DEFAULT_LABEL_OFFSET =  0f;
    private static final float DEFAULT_LABEL_LENGTHWISE_POSITION = 0.5f;


    private final boolean outlineVisible = true;     

    private boolean isFilledUnfinishedStandard;
    private boolean isFilledFinishedStandard;

    private Paint paintFillFinishedStandard;
    private Paint paintFillUnfinishedStandard;


    private static MetaMap<String, Paint, PreferredROIStyle> instances = new MetaMap<>();

    protected PreferredROIStyle(Preferences pref, Paint defaultPaint)
    {
        super(pref, defaultPaint, DEFAULT_LABEL_OFFSET, DEFAULT_LABEL_LENGTHWISE_POSITION);

        setDefaultROIStyle();
    }

    public static PreferredROIStyle getInstance(Preferences pref, Paint defaultPaint) 
    {
        String key = pref.absolutePath();
        PreferredROIStyle style = instances.get(key, defaultPaint);

        if(style == null)
        {
            style = new PreferredROIStyle(pref, defaultPaint);
            instances.put(key, defaultPaint, style);
        }

        return style;    
    };

    private void setDefaultROIStyle()
    {
        boolean defaultIsFilledUnfinished = true;
        boolean defaultIsFilledFinished = true;     

        Paint defaultFillUnfinishedStandardPaint = getPaintUnfinished();
        Paint defaultFillFinishedStandardPaint = getPaintFinished();

        if(defaultFillUnfinishedStandardPaint instanceof Color)
        {
            Color c = (Color)defaultFillUnfinishedStandardPaint;
            defaultFillUnfinishedStandardPaint= new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()/4);
        }

        if(defaultFillFinishedStandardPaint instanceof Color)
        {
            Color c = (Color)defaultFillFinishedStandardPaint;
            defaultFillFinishedStandardPaint= new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()/4);
        }

        Preferences pref = getPreferences();

        this.isFilledUnfinishedStandard = pref.getBoolean(ROI_FILLED_UNFINISHED_STANDARD, defaultIsFilledUnfinished);
        this.isFilledFinishedStandard = pref.getBoolean(ROI_FILLED_FINISHED_STANDARD, defaultIsFilledFinished);

        this.paintFillUnfinishedStandard = (Paint)SerializationUtilities.getSerializableObject(pref, ROI_PAINT_FILL_UNFINISHED_STANDARD, defaultFillUnfinishedStandardPaint);
        this.paintFillFinishedStandard = (Paint)SerializationUtilities.getSerializableObject(pref, ROI_PAINT_FILL_FINISHED_STANDARD, defaultFillFinishedStandardPaint);

    }

    public Paint getPaintFillFinishedStandard()
    {
        return paintFillFinishedStandard;
    }

    public Paint getPaintFillUnfinishedStandard()
    {
        return paintFillUnfinishedStandard;
    }

    public boolean isFilledUnfinishedStandard()
    {
        return isFilledUnfinishedStandard;
    }

    public boolean isFilledFinishedStandard()
    {
        return isFilledFinishedStandard;
    }

    public boolean getOutlineVisible()
    {
        return outlineVisible;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        super.preferenceChange(evt);

        Preferences pref = getPreferences();

        String key = evt.getKey();

        if(ROI_FILLED_UNFINISHED_STANDARD.equals(key))
        {
            boolean defaultIsFilledUnfinished = true;
            this.isFilledUnfinishedStandard = pref.getBoolean(ROI_FILLED_UNFINISHED_STANDARD, defaultIsFilledUnfinished);
        }
        else if(ROI_FILLED_FINISHED_STANDARD.equals(key))
        {
            boolean defaultIsFilledFinished = true;     
            this.isFilledFinishedStandard = pref.getBoolean(ROI_FILLED_FINISHED_STANDARD, defaultIsFilledFinished);
        }

        else if(ROI_FILLED_FINISHED_STANDARD.equals(key))
        {
            Paint defaultFillUnfinishedStandardPaint = getPaintUnfinished();
            if(defaultFillUnfinishedStandardPaint instanceof Color)
            {
                Color c = (Color)defaultFillUnfinishedStandardPaint;
                defaultFillUnfinishedStandardPaint= new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()/4);
            }
            this.paintFillUnfinishedStandard = (Paint)SerializationUtilities.getSerializableObject(pref, ROI_PAINT_FILL_UNFINISHED_STANDARD, defaultFillUnfinishedStandardPaint);
        }

        else if(ROI_FILLED_FINISHED_STANDARD.equals(key))
        {
            Paint defaultFillFinishedStandardPaint = getPaintFinished();
            if(defaultFillFinishedStandardPaint instanceof Color)
            {
                Color c = (Color)defaultFillFinishedStandardPaint;
                defaultFillFinishedStandardPaint= new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()/4);
            }
            this.paintFillFinishedStandard = (Paint)SerializationUtilities.getSerializableObject(pref, ROI_PAINT_FILL_FINISHED_STANDARD, defaultFillFinishedStandardPaint);
        }
    }   
}
