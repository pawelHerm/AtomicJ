package atomicJ.gui;


import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import atomicJ.utilities.SerializationUtilities;


public class PreferredContinuousSeriesRendererStyle implements PreferenceChangeListener
{
    private boolean visible;
    private boolean visibleInLegend;
    private boolean joined;
    private boolean markers;
    private int markerSize;
    private int markerIndex;
    private Stroke stroke;
    private Shape shape;
    private Paint fillPaint;
    private Paint strokePaint;

    private Channel1DRendererData rendererData;

    private final Preferences pref;
    public static final String PAINT = "Paint";
    public static final String SHOWN = "Shown";
    public static final String SERIES_JOINING_LINE_PAINT = "SeriesJoiningLinePaint";
    public static final String SERIES_JOINING_LINE_STROKE = "SeriesJoiningLineStroke";
    public static final String SHAPE_SIZE = "ShapeSize";
    public static final String SHAPE_INDEX = "ShapeIndex";
    public static final String MARKERS = "Markers";
    public static final String JOINED = "Joined";
    public static final String VISIBLE_IN_LEGEND = "VisibleInLegend";

    private static final SeriesStyleSupplier DEFAULT_STYLE_SUPPLIER = DefaultSeriesStyleSupplier.getSupplier();

    private static final Map<String, PreferredContinuousSeriesRendererStyle> INSTANCES = new LinkedHashMap<>();

    public PreferredContinuousSeriesRendererStyle(Preferences pref, StyleTag tag)
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        boolean defJoined = DEFAULT_STYLE_SUPPLIER.getDefaultJoiningLineVisible(tag);
        boolean defMarkers = DEFAULT_STYLE_SUPPLIER.getDefaultMarkersVisible(tag);
        int defSize = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerSize(tag);
        int defIndex = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerIndex(tag);
        Stroke defStroke = DEFAULT_STYLE_SUPPLIER.getDefaultJoiningLineStroke(tag);
        Paint defPaint = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerPaint(tag);   

        this.visible = pref.getBoolean(PreferredContinuousSeriesRendererStyle.SHOWN, true);
        this.visibleInLegend = pref.getBoolean(PreferredContinuousSeriesRendererStyle.VISIBLE_IN_LEGEND, true);
        this.joined = pref.getBoolean(PreferredContinuousSeriesRendererStyle.JOINED, defJoined);
        this.markers = pref.getBoolean(PreferredContinuousSeriesRendererStyle.MARKERS, defMarkers);
        this.markerSize = pref.getInt(PreferredContinuousSeriesRendererStyle.SHAPE_SIZE, defSize);
        this.markerIndex = pref.getInt(PreferredContinuousSeriesRendererStyle.SHAPE_INDEX, defIndex);
        this.stroke = SerializationUtilities.getStroke(pref, PreferredContinuousSeriesRendererStyle.SERIES_JOINING_LINE_STROKE, defStroke);
        this.shape = ShapeSupplier.createShape(markerIndex, markerSize);
        this.fillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredContinuousSeriesRendererStyle.PAINT, defPaint);
        this.strokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredContinuousSeriesRendererStyle.SERIES_JOINING_LINE_PAINT, defPaint);
        this.rendererData = buildChannel1DRendererData();
    }   

    public Channel1DRendererData getChannel1DRendererData()
    {
        return rendererData;
    }

    private Channel1DRendererData buildChannel1DRendererData()
    {
        Channel1DRendererDataMutable<Channel1DRendererData> rendererDataMutable = new Channel1DRendererDataMutable<>();
        Shape shape = ShapeSupplier.createShape(this.markerIndex, this.markerSize);

        rendererDataMutable.setUseFillPaint(true);
        rendererDataMutable.setDrawOutlines(false);
        rendererDataMutable.setBaseMarkerIndex(markerIndex);
        rendererDataMutable.setBaseMarkerSize(markerSize);
        rendererDataMutable.setBaseSeriesVisible(this.visible);    
        rendererDataMutable.setBaseSeriesVisibleInLegend(this.visibleInLegend);
        rendererDataMutable.setBaseShape(shape);
        rendererDataMutable.setBaseStroke(this.stroke);
        rendererDataMutable.setBaseFillPaint(this.fillPaint);
        rendererDataMutable.setBasePaint(this.strokePaint);
        rendererDataMutable.setBaseLinesVisible(this.joined);
        rendererDataMutable.setBaseShapesVisible(this.markers);

        Channel1DRendererDataImmutable rendererDataImmutable = new Channel1DRendererDataImmutable(rendererDataMutable);

        return rendererDataImmutable;
    }

    public void saveToPreferences(Channel1DRendererData model)
    {
        pref.putBoolean(PreferredContinuousSeriesRendererStyle.SHOWN, model.getBaseSeriesVisible());
        pref.putBoolean(PreferredContinuousSeriesRendererStyle.VISIBLE_IN_LEGEND, model.getBaseSeriesVisibleInLegend());
        pref.putBoolean(PreferredContinuousSeriesRendererStyle.JOINED, model.getBaseLinesVisible());
        pref.putBoolean(PreferredContinuousSeriesRendererStyle.MARKERS, model.getBaseShapesVisible());
        pref.putInt(PreferredContinuousSeriesRendererStyle.SHAPE_INDEX, model.getBaseMarkerIndex());
        pref.putFloat(PreferredContinuousSeriesRendererStyle.SHAPE_SIZE, model.getBaseMarkerSize());
        try 
        {
            SerializationUtilities.putSerializableObject(pref, PreferredContinuousSeriesRendererStyle.PAINT, model.getBaseFillPaint());
            SerializationUtilities.putSerializableObject(pref, PreferredContinuousSeriesRendererStyle.SERIES_JOINING_LINE_PAINT, model.getBasePaint());
            SerializationUtilities.putStroke(pref, PreferredContinuousSeriesRendererStyle.SERIES_JOINING_LINE_STROKE, model.getBaseStroke());
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
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

    public Preferences getPreferences()
    {
        return pref;
    }

    public boolean isVisible()
    {
        return visible;
    };

    public boolean isVisibleInLegend()
    {
        return visibleInLegend;
    };

    public boolean isJoined()
    {
        return joined;
    };

    public boolean isMarkerVisible()
    {
        return markers;
    };

    public int getMarkerSize()
    {
        return markerSize;
    };

    public int getMarkerIndex()
    {
        return markerIndex;
    };

    public Stroke getStroke()
    {
        return stroke;
    };

    public Shape getShape()
    {
        return shape;
    };

    public Paint getFillPaint()
    {
        return fillPaint;
    };

    public Paint getStrokePaint()
    {
        return strokePaint;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        String key = evt.getKey();

        if(SHOWN.equals(key))
        {
            this.visible = pref.getBoolean(PreferredContinuousSeriesRendererStyle.SHOWN,true);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(VISIBLE_IN_LEGEND.equals(key))
        {
            this.visibleInLegend = pref.getBoolean(PreferredContinuousSeriesRendererStyle.VISIBLE_IN_LEGEND, true);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(JOINED.equals(key))
        {
            this.joined = pref.getBoolean(PreferredContinuousSeriesRendererStyle.JOINED, this.joined);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(MARKERS.equals(key))
        {
            this.markers = pref.getBoolean(PreferredContinuousSeriesRendererStyle.MARKERS, this.markers);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(SHAPE_SIZE.equals(key))
        {
            this.markerSize = pref.getInt(PreferredContinuousSeriesRendererStyle.SHAPE_SIZE, this.markerSize);
            this.shape = ShapeSupplier.createShape(markerIndex, markerSize);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(SHAPE_INDEX.equals(key))
        {
            this.markerIndex = pref.getInt(PreferredContinuousSeriesRendererStyle.SHAPE_INDEX, this.markerIndex);
            this.shape = ShapeSupplier.createShape(markerIndex, markerSize);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(SERIES_JOINING_LINE_STROKE.equals(key))
        {
            this.stroke = SerializationUtilities.getStroke(pref, PreferredContinuousSeriesRendererStyle.SERIES_JOINING_LINE_STROKE, this.stroke);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(PAINT.equals(key))
        {
            this.fillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredContinuousSeriesRendererStyle.PAINT, this.fillPaint);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(SERIES_JOINING_LINE_PAINT.equals(key))
        {
            this.strokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredContinuousSeriesRendererStyle.SERIES_JOINING_LINE_PAINT, this.strokePaint);
            this.rendererData = buildChannel1DRendererData();
        }
    }

    public static PreferredContinuousSeriesRendererStyle getInstance(Preferences pref, StyleTag styleKey) 
    {
        String key = pref.absolutePath() + styleKey.getPreferredStyleKey();
        PreferredContinuousSeriesRendererStyle style = INSTANCES.get(key);

        if(style == null)
        {
            style = new PreferredContinuousSeriesRendererStyle(pref, styleKey);
            INSTANCES.put(key, style);
        }

        return style;    
    };

}
