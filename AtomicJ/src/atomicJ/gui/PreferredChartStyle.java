package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.ANTIALIASING;
import static atomicJ.gui.PreferenceKeys.ASPECT_RATIO_LOCKED;
import static atomicJ.gui.PreferenceKeys.BACKGROUND_PAINT;

import java.awt.Paint;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import org.jfree.chart.JFreeChart;
import org.jfree.ui.RectangleInsets;

import atomicJ.gui.editors.ChartStyleData;
import atomicJ.utilities.SerializationUtilities;

public class PreferredChartStyle implements PreferenceChangeListener
{
    private static final String CHART_PADDING_RIGHT = "ChartPaddingRight";
    private static final String CHART_PADDING_LEFT = "ChartPaddingLeft";
    private static final String CHART_PADDING_BOTTOM = "ChartPaddingBottom";
    private static final String CHART_PADDING_TOP = "ChartPaddingTop";

    private double paddingTop;
    private double paddingBottom;
    private double paddingLeft;
    private double paddingRight;
    private RectangleInsets paddingInsets;

    private Paint backgroundPaint;
    private boolean antialias;

    private boolean lockAspectRatio;

    private final boolean defaultLockAspect;
    private final Preferences pref;

    private static final Map<String, PreferredChartStyle> INSTANCES = new LinkedHashMap<>();

    public PreferredChartStyle(Preferences pref, boolean defaultLockAspect)
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        this.defaultLockAspect = defaultLockAspect;

        this.paddingTop = pref.getDouble(PreferredChartStyle.CHART_PADDING_TOP, 0);
        this.paddingBottom = pref.getDouble(PreferredChartStyle.CHART_PADDING_BOTTOM, 0);
        this.paddingLeft = pref.getDouble(PreferredChartStyle.CHART_PADDING_LEFT, 0);
        this.paddingRight = pref.getDouble(PreferredChartStyle.CHART_PADDING_RIGHT, 0);

        refreshPaddings();

        this.backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(pref, BACKGROUND_PAINT, JFreeChart.DEFAULT_BACKGROUND_PAINT);
        this.antialias = pref.getBoolean(ANTIALIASING, true);

        this.lockAspectRatio = pref.getBoolean(ASPECT_RATIO_LOCKED, defaultLockAspect);
    }

    public static PreferredChartStyle getInstance(Preferences pref, boolean defaultLockAspect) 
    {
        String key = pref.absolutePath() + Boolean.toString(defaultLockAspect);
        PreferredChartStyle style = INSTANCES.get(key);

        if(style == null)
        {
            style = new PreferredChartStyle(pref, defaultLockAspect);
            INSTANCES.put(key, style);
        }

        return style;    
    };


    public void saveToPreferences(ChartStyleData styleData)
    {
        pref.putDouble(PreferredChartStyle.CHART_PADDING_TOP, styleData.getPaddingTop());
        pref.putDouble(PreferredChartStyle.CHART_PADDING_BOTTOM, styleData.getPaddingBottom());
        pref.putDouble(PreferredChartStyle.CHART_PADDING_LEFT, styleData.getPaddingLeft());
        pref.putDouble(PreferredChartStyle.CHART_PADDING_RIGHT, styleData.getPaddingRight());

        pref.putBoolean(ANTIALIASING, styleData.isAntialias());
        pref.putBoolean(ASPECT_RATIO_LOCKED, styleData.isLockAspectRatio());

        try {
            SerializationUtilities.putSerializableObject(pref, BACKGROUND_PAINT, styleData.getBackgroundPaint());
        } catch (ClassNotFoundException | IOException
                | BackingStoreException e) {
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

    public double getPaddingTop()
    {
        return paddingTop;
    }

    public double getPaddingBottom()
    {
        return paddingBottom;
    }

    public double getPaddingLeft()
    {
        return paddingLeft;
    }

    public double getPaddingRight()
    {
        return paddingRight;
    }

    public RectangleInsets paddingInsets()
    {
        return paddingInsets;
    }

    public Paint getBackgroundPaint()
    {
        return backgroundPaint;
    }

    public boolean isAntialias()
    {
        return antialias;
    }

    public boolean isLockAspectRatio()
    {
        return lockAspectRatio;
    }

    private void refreshPaddings()
    {
        boolean paddingPresent = paddingTop + paddingBottom + paddingLeft + paddingRight > 0.001;
        this.paddingInsets = paddingPresent ? new RectangleInsets(paddingTop, paddingLeft, paddingBottom, paddingRight) : null;       
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt)
    {
        String key = evt.getKey();

        if(PreferredChartStyle.CHART_PADDING_TOP.equals(key))
        {
            this.paddingTop = pref.getDouble(PreferredChartStyle.CHART_PADDING_TOP, 0);
            refreshPaddings();
        }
        else if(PreferredChartStyle.CHART_PADDING_BOTTOM.equals(key))
        {
            this.paddingBottom = pref.getDouble(PreferredChartStyle.CHART_PADDING_BOTTOM, 0);
            refreshPaddings();
        }
        else if(PreferredChartStyle.CHART_PADDING_LEFT.equals(key))
        {
            this.paddingLeft = pref.getDouble(PreferredChartStyle.CHART_PADDING_LEFT, 0);
            refreshPaddings();
        }
        else if(PreferredChartStyle.CHART_PADDING_RIGHT.equals(key))
        {
            this.paddingRight = pref.getDouble(PreferredChartStyle.CHART_PADDING_RIGHT, 0);
            refreshPaddings();
        }
        else if(BACKGROUND_PAINT.equals(key))
        {
            this.backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(pref, BACKGROUND_PAINT, JFreeChart.DEFAULT_BACKGROUND_PAINT);
        }
        else if(ANTIALIASING.equals(key))
        {
            this.antialias = pref.getBoolean(ANTIALIASING, true);
        }
        else if(ASPECT_RATIO_LOCKED.equals(key))
        {
            this.lockAspectRatio = pref.getBoolean(ASPECT_RATIO_LOCKED, defaultLockAspect);
        }
    }
}
