package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.BACKGROUND_PAINT;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;

import atomicJ.utilities.MetaMap;
import atomicJ.utilities.SerializationUtilities;

public class PreferredBasePlotStyle implements PreferenceChangeListener
{
    public static final String PLOT_VERTICAL = "PlotVertical";
    public static final String PLOT_ORIENTATION = "PlotOrientation";
    public static final String PLOT_RANGE_GRIDLINE_VISIBLE = "PlotRangeGridlineVisible";
    public static final String PLOT_RANGE_GRIDLINE_PAINT = "PlotRangeGridlinePaint";
    public static final String PLOT_RANGE_GRIDLINE_STROKE = "PlotRangeGridlineStroke";
    public static final String PLOT_DOMAIN_GRIDLINE_VISIBLE = "PlotDomainGridlineVisible";
    public static final String PLOT_DOMAIN_GRIDLINE_PAINT = "PlotDomainGridlinePaint";
    public static final String PLOT_DOMAIN_GRIDLINE_STROKE = "PlotDomainGridlineStroke";
    public static final String PLOT_OUTLINE_VISIBLE = "PlotOutlineVisible";
    public static final String PLOT_OUTLINE_PAINT = "PlotOutlinePaint";
    public static final String PLOT_OUTLINE_STROKE = "PlotOutlineStroke";

    private Paint backgroundPaint;
    private Paint outlinePaint;
    private Paint domainGridlinePaint;
    private Paint rangeGridlinePaint;
    private Stroke outlineStroke;
    private Stroke domainGridlineStroke;
    private Stroke rangeGridlineStroke;
    private PlotOrientation plotOrientation;

    private boolean outlineVisible;
    private boolean domainGridlinesVisible;
    private boolean rangeGridlinesVisible;

    private final Preferences pref;

    private static final MetaMap<String, String, PreferredBasePlotStyle> INSTANCES = new MetaMap<>();

    private PreferredBasePlotStyle(Preferences pref, String key)
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        pullPreferredStyle(pref, key);
    }

    public Preferences getPreferences()
    {
        return pref;
    }

    public static PreferredBasePlotStyle getInstance(Preferences pref, String key) 
    {
        String prefKey = pref.absolutePath();
        PreferredBasePlotStyle style = INSTANCES.get(prefKey, key);

        if(style == null)
        {
            style = new PreferredBasePlotStyle(pref, key);
            INSTANCES.put(prefKey, key, style);
        }

        return style;    
    };

    private void pullPreferredStyle(Preferences pref, String key)
    {
        PlotStyleSupplier supplier = DefaultPlotStyleSupplier.getInstance();
        Stroke defaultDomainGridline = (key == null) ? XYPlot.DEFAULT_GRIDLINE_STROKE : supplier.getDefaultDomainGridlineStroke(key);
        Stroke defaultRangeGridline = (key == null) ? XYPlot.DEFAULT_GRIDLINE_STROKE : supplier.getDefaultRangeGridlineStroke(key);

        boolean defaultDomainGridlineVisible = (key == null) ? true : supplier.getDefaultDomainGridlineVisible(key);
        boolean defaultRangeGridlineVisible = (key == null) ? true : supplier.getDefaultRangeGridlineVisible(key);

        Paint defaultDomainGridlinePaint = (key == null) ? Color.red : supplier.getDefaultDomainGridlinePaint(key);
        Paint defaultRangeGridlinePaint = (key == null) ? Color.red : supplier.getDefaultRangeGridlinePaint(key);

        Paint defaultBackgroundPaint = (key == null) ? Color.white : supplier.getDefaultBackgroundPaint(key);

        this.backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(pref, BACKGROUND_PAINT, defaultBackgroundPaint);
        this.outlinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredBasePlotStyle.PLOT_OUTLINE_PAINT, Color.black);
        this.domainGridlinePaint = (Paint) SerializationUtilities.getSerializableObject(pref, PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_PAINT, defaultDomainGridlinePaint);
        this.rangeGridlinePaint = (Paint) SerializationUtilities.getSerializableObject(pref, PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_PAINT, defaultRangeGridlinePaint);
        this.outlineStroke = SerializationUtilities.getStroke(pref, PreferredBasePlotStyle.PLOT_OUTLINE_STROKE, Plot.DEFAULT_OUTLINE_STROKE);
        this.domainGridlineStroke = SerializationUtilities.getStroke(pref, PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_STROKE, defaultDomainGridline);
        this.rangeGridlineStroke = SerializationUtilities.getStroke(pref, PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_STROKE, defaultRangeGridline);
        this.plotOrientation = pref.getBoolean(PreferredBasePlotStyle.PLOT_VERTICAL,true) ? PlotOrientation.VERTICAL: PlotOrientation.HORIZONTAL;

        this.outlineVisible = pref.getBoolean(PreferredBasePlotStyle.PLOT_OUTLINE_VISIBLE, true);
        this.domainGridlinesVisible = pref.getBoolean(PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_VISIBLE, defaultDomainGridlineVisible);
        this.rangeGridlinesVisible = pref.getBoolean(PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_VISIBLE, defaultRangeGridlineVisible);
    }


    public Paint getBackgroundPaint()
    {
        return backgroundPaint;
    }

    public Paint getOutlinePaint()
    {
        return outlinePaint;
    }

    public Paint getDomainGridlinePaint()
    {
        return domainGridlinePaint;
    }

    public Paint getRangeGridlinePaint()
    {
        return rangeGridlinePaint;
    }

    public Stroke getOutlineStroke()
    {
        return outlineStroke;
    }

    public Stroke getDomainGridlineStroke()
    {
        return domainGridlineStroke;
    }

    public Stroke getRangeGridlineStroke()
    {
        return rangeGridlineStroke;
    }

    public PlotOrientation getPlotOrientation()
    {
        return plotOrientation;
    }

    public boolean isOutlineVisible()
    {
        return outlineVisible;
    }

    public boolean isDomainGridlinesVisible()
    {
        return domainGridlinesVisible;
    }

    public boolean isRangeGridlinesVisible()
    {
        return rangeGridlinesVisible;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt)
    {
        String key = evt.getKey();

        PlotStyleSupplier supplier = DefaultPlotStyleSupplier.getInstance();

        if(BACKGROUND_PAINT.equals(key))
        {
            Paint defaultBackgroundPaint = (key == null) ? Color.white : supplier.getDefaultBackgroundPaint(key);
            this.backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(pref, BACKGROUND_PAINT, defaultBackgroundPaint);
        }
        else if(PreferredBasePlotStyle.PLOT_OUTLINE_PAINT.equals(key))
        {
            this.outlinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredBasePlotStyle.PLOT_OUTLINE_PAINT, Color.black);
        }       
        else if(PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_PAINT.equals(key))
        {
            Paint defaultDomainGridlinePaint = (key == null) ? Color.red : supplier.getDefaultDomainGridlinePaint(key);
            this.domainGridlinePaint = (Paint) SerializationUtilities.getSerializableObject(pref, PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_PAINT, defaultDomainGridlinePaint);
        }

        else if(PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_PAINT.equals(key))
        {
            Paint defaultRangeGridlinePaint = (key == null) ? Color.red : supplier.getDefaultRangeGridlinePaint(key);
            this.rangeGridlinePaint = (Paint) SerializationUtilities.getSerializableObject(pref, PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_PAINT, defaultRangeGridlinePaint);
        }      
        else if(PreferredBasePlotStyle.PLOT_OUTLINE_STROKE.equals(key))
        {
            this.outlineStroke = SerializationUtilities.getStroke(pref, PreferredBasePlotStyle.PLOT_OUTLINE_STROKE, Plot.DEFAULT_OUTLINE_STROKE);
        }        
        else if(PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_STROKE.equals(key))
        {
            Stroke defaultDomainGridline = (key == null) ? XYPlot.DEFAULT_GRIDLINE_STROKE : supplier.getDefaultDomainGridlineStroke(key);
            this.domainGridlineStroke = SerializationUtilities.getStroke(pref, PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_STROKE, defaultDomainGridline);
        }

        else if(PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_STROKE.equals(key))
        {
            Stroke defaultRangeGridline = (key == null) ? XYPlot.DEFAULT_GRIDLINE_STROKE : supplier.getDefaultRangeGridlineStroke(key);
            this.rangeGridlineStroke = SerializationUtilities.getStroke(pref, PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_STROKE, defaultRangeGridline);
        }

        else if(PreferredBasePlotStyle.PLOT_VERTICAL.equals(key))
        {
            this.plotOrientation = pref.getBoolean(PreferredBasePlotStyle.PLOT_VERTICAL,true) ? PlotOrientation.VERTICAL: PlotOrientation.HORIZONTAL;
        }

        else if(PreferredBasePlotStyle.PLOT_OUTLINE_VISIBLE.equals(key))
        {
            this.outlineVisible = pref.getBoolean(PreferredBasePlotStyle.PLOT_OUTLINE_VISIBLE, true);
        }

        else if(PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_VISIBLE.equals(key))
        {
            boolean defaultDomainGridlineVisible = (key == null) ? true : supplier.getDefaultDomainGridlineVisible(key);
            this.domainGridlinesVisible = pref.getBoolean(PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_VISIBLE, defaultDomainGridlineVisible);
        }

        else if(PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_VISIBLE.equals(key))
        {
            boolean defaultRangeGridlineVisible = (key == null) ? true : supplier.getDefaultRangeGridlineVisible(key);
            this.rangeGridlinesVisible = pref.getBoolean(PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_VISIBLE, defaultRangeGridlineVisible);
        }
    }
}
