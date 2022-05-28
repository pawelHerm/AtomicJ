package atomicJ.gui.measurements;

import java.awt.Paint;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import atomicJ.gui.PreferredAnnotationStyle;
import atomicJ.utilities.MetaMap;

public class PreferredDistanceMeasurementStyle extends PreferredAnnotationStyle 
{

    public static final String DRAW_ABSCISSA_MEASUREMENT_UNFINISHED = "DrawAbscissaMeasurementUnfinished";
    public static final String DRAW_ABSCISSA_MEASUREMENT_FINISHED = "DrawAbscissaMeasurementFinished";
    public static final String DRAW_ORDINATE_MEASUREMENT_UNFINISHED = "DrawOrdinateMeasurementUnfinished";
    public static final String DRAW_ORDINATE_MEASUREMENT_FINISHED = "DrawOrdinateMeasurementFinished";

    public static final String MAXIMUM_FRACTION_DIGITS_FINISHED = "MaximumFractionDigitsFinished";
    public static final String SHOW_TRAILING_DIGITS_FINISHED = "ShowTrailingDigitsFinished";
    public static final String GROUPING_SEPERATOR_USED_FINISHED = "GroupingSeparatorUsedFinished";
    public static final String GROUPING_SEPERATOR_FINISHED = "GroupingSeparatorFinished";
    public static final String DECIMAL_SEPERATOR_FINISHED = "DecimalSeparatorFinished";

    public static final String MAXIMUM_FRACTION_DIGITS_UNFINISHED = "MaximumFractionDigitsUnfinished";
    public static final String SHOW_TRAILING_DIGITS_UNFINISHED = "ShowTrailingDigitsUnfinished";
    public static final String GROUPING_SEPERATOR_USED_UNFINISHED = "GroupingSeparatorUsedUnfinished";
    public static final String GROUPING_SEPERATOR_UNFINISHED = "GroupingSeparatorUnfinished";
    public static final String DECIMAL_SEPERATOR_UNFINISHED = "DecimalSeparatorUnfinished";


    private static final float DEFAULT_LABEL_OFFSET = 4.f;
    private static final float DEFAULT_LABEL_LENGTHWISE_POSITION = 0.1f;

    private static MetaMap<String, Paint, PreferredDistanceMeasurementStyle> instances = new MetaMap<>();


    private boolean drawAbscissaMeasurementUnfinished;
    private boolean drawOrdinateMeasurementUnfinished;

    private boolean drawAbscissaMeasurementFinished;
    private boolean drawOrdinateMeasurementFinished;


    protected PreferredDistanceMeasurementStyle(Preferences pref, Paint defaultPaint)
    {
        super(pref, defaultPaint, DEFAULT_LABEL_OFFSET, DEFAULT_LABEL_LENGTHWISE_POSITION);

        setDefaultMeasurementStyleStyle();
    }

    private void setDefaultMeasurementStyleStyle()
    {
        Preferences pref = getPreferences();

        this.drawAbscissaMeasurementUnfinished = pref.getBoolean(DRAW_ABSCISSA_MEASUREMENT_UNFINISHED, false);
        this.drawOrdinateMeasurementUnfinished = pref.getBoolean(DRAW_ORDINATE_MEASUREMENT_UNFINISHED, false);

        this.drawAbscissaMeasurementFinished = pref.getBoolean(DRAW_ABSCISSA_MEASUREMENT_FINISHED, false);
        this.drawOrdinateMeasurementFinished = pref.getBoolean(DRAW_ORDINATE_MEASUREMENT_FINISHED, false);
    }


    public static PreferredDistanceMeasurementStyle getInstance(Preferences pref, Paint defaultPaint) 
    {
        String key = pref.absolutePath();
        PreferredDistanceMeasurementStyle style = instances.get(key, defaultPaint);

        if(style == null)
        {
            style = new PreferredDistanceMeasurementStyle(pref, defaultPaint);
            instances.put(key, defaultPaint, style);
        }

        return style;    
    };

    public boolean isDrawAbscissaMeasurementUnfinished()
    {
        return drawAbscissaMeasurementUnfinished;
    }

    public boolean isDrawAbscissaMeasurementFinished()
    {
        return drawAbscissaMeasurementFinished;
    }

    public boolean isDrawOrdinateMeasurementUnfinished()
    {
        return drawOrdinateMeasurementUnfinished;
    }

    public boolean isDrawOrdinateMeasurementFinished()
    {
        return drawOrdinateMeasurementFinished;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        super.preferenceChange(evt);

        Preferences pref = getPreferences();

        String key = evt.getKey();

        if(DRAW_ABSCISSA_MEASUREMENT_UNFINISHED.equals(key))
        {
            this.drawAbscissaMeasurementUnfinished = pref.getBoolean(DRAW_ABSCISSA_MEASUREMENT_UNFINISHED, false);
        }

        else if(DRAW_ORDINATE_MEASUREMENT_UNFINISHED.equals(key))
        {
            this.drawOrdinateMeasurementUnfinished = pref.getBoolean(DRAW_ORDINATE_MEASUREMENT_UNFINISHED, false);
        }

        else if(DRAW_ABSCISSA_MEASUREMENT_FINISHED.equals(key))
        {
            this.drawAbscissaMeasurementFinished = pref.getBoolean(DRAW_ABSCISSA_MEASUREMENT_FINISHED, false);
        }

        else if(DRAW_ORDINATE_MEASUREMENT_FINISHED.equals(key))
        {
            this.drawOrdinateMeasurementFinished = pref.getBoolean(DRAW_ORDINATE_MEASUREMENT_FINISHED, false);
        }
    }
}
