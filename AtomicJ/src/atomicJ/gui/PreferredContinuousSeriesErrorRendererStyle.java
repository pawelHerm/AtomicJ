package atomicJ.gui;


import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import atomicJ.utilities.SerializationUtilities;

public class PreferredContinuousSeriesErrorRendererStyle implements PreferenceChangeListener
{
    //whiskers properties

    public static final String BAR_PAINT = "BarPaint";
    public static final String BAR_STROKE = "BarStroke";
    public static final String CAP_VISIBLE = "CapVisible";
    public static final String CAP_WIDTH = "CapWidth";
    public static final String CAP_PAINT = "CapPaint";
    public static final String CAP_STROKE = "CapStroke";
    public static final String ERROR_BAR_DRAWING_DIRECTION = "ErrorBarDrawingDirection";

    private static final Map<String, PreferredContinuousSeriesErrorRendererStyle> INSTANCES = new HashMap<>();

    //error bars
    private Paint barPaint;
    private Stroke barStroke;
    private boolean capVisible;
    private double capWidth;
    private Paint capPaint; 
    private Stroke capStroke;

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
    private ErrorBarDirection drawingDirection;

    private Channel1DErrorRendererData rendererData;

    private final Preferences pref;

    private static final SeriesStyleSupplier DEFAULT_STYLE_SUPPLIER = DefaultSeriesStyleSupplier.getSupplier();

    private PreferredContinuousSeriesErrorRendererStyle(Preferences pref, StyleTag tag) 
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

        this.barPaint = (Paint)SerializationUtilities.getSerializableObject(pref, BAR_PAINT, defPaint);
        this.barStroke= SerializationUtilities.getStroke(pref, BAR_STROKE, defStroke);
        this.capVisible = pref.getBoolean(CAP_VISIBLE, true);
        this.capWidth = pref.getDouble(CAP_WIDTH, 20);

        this.capPaint = (Paint)SerializationUtilities.getSerializableObject(pref, CAP_PAINT, defPaint); 
        this.capStroke = SerializationUtilities.getStroke(pref, CAP_STROKE, defStroke);

        String preferredDrawingDirectionName = pref.get(ERROR_BAR_DRAWING_DIRECTION, ErrorBarDirection.BOTH_SIDES.name());
        this.drawingDirection = ErrorBarDirection.getInstance(preferredDrawingDirectionName, ErrorBarDirection.BOTH_SIDES);

        this.rendererData = buildChannel1DRendererData();
    }

    public Channel1DErrorRendererData getChannel1DRendererData()
    {
        return rendererData;
    }

    private Channel1DErrorRendererData buildChannel1DRendererData()
    {
        Channel1DErrorRendererDataMutable rendererDataMutable = new Channel1DErrorRendererDataMutable();
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

        rendererDataMutable.setBarPaint(this.barPaint);
        rendererDataMutable.setBarStroke(this.barStroke);
        rendererDataMutable.setCapVisible(this.capVisible);
        rendererDataMutable.setCapWidth(this.capWidth);
        rendererDataMutable.setCapPaint(this.capPaint);
        rendererDataMutable.setCapStroke(this.capStroke);
        rendererDataMutable.setErrorBarDrawingDirection(this.drawingDirection);

        Channel1DErrorRendererDataImmutable rendererDataImmutable = new Channel1DErrorRendererDataImmutable(rendererDataMutable);

        return rendererDataImmutable;
    }


    public void saveToPreferences(Channel1DErrorRendererData model)
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
            SerializationUtilities.putStroke(pref, BAR_STROKE, model.getBarStroke());
            SerializationUtilities.putSerializableObject(pref, BAR_PAINT, model.getBarPaint());
            SerializationUtilities.putSerializableObject(pref, CAP_PAINT, model.getCapPaint()); 
            SerializationUtilities.putStroke(pref, CAP_STROKE, model.getCapStroke());
        }
        catch (ClassNotFoundException | IOException | BackingStoreException e1) 
        {
            e1.printStackTrace();
        }

        pref.putBoolean(CAP_VISIBLE, model.isCapVisible());
        pref.putDouble(CAP_WIDTH, model.getCapWidth());    
        pref.put(ERROR_BAR_DRAWING_DIRECTION, model.getErrorBarDrawingDirection().name());

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

    public Paint getBarPaint()
    {
        return barPaint;
    }

    public Stroke getBarStroke()
    {
        return barStroke;
    }

    public boolean isCapVisible()
    {
        return capVisible;
    }

    public double getCapWidth()
    {
        return capWidth;
    }

    public Paint getCapPaint()
    {
        return capPaint;
    }

    public Stroke getCapStroke()
    {
        return capStroke;
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

    public ErrorBarDirection getErrorBarDrawingDirection() {
        return drawingDirection;
    };

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        String key = evt.getKey();

        if(PreferredContinuousSeriesRendererStyle.SHOWN.equals(key))
        {
            this.visible = pref.getBoolean(PreferredContinuousSeriesRendererStyle.SHOWN,true);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(PreferredContinuousSeriesRendererStyle.VISIBLE_IN_LEGEND.equals(key))
        {
            this.visibleInLegend = pref.getBoolean(PreferredContinuousSeriesRendererStyle.VISIBLE_IN_LEGEND, true);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(PreferredContinuousSeriesRendererStyle.JOINED.equals(key))
        {
            this.joined = pref.getBoolean(PreferredContinuousSeriesRendererStyle.JOINED, this.joined);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(PreferredContinuousSeriesRendererStyle.MARKERS.equals(key))
        {
            this.markers = pref.getBoolean(PreferredContinuousSeriesRendererStyle.MARKERS, this.markers);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(PreferredContinuousSeriesRendererStyle.SHAPE_SIZE.equals(key))
        {
            this.markerSize = pref.getInt(PreferredContinuousSeriesRendererStyle.SHAPE_SIZE, this.markerSize);
            this.shape = ShapeSupplier.createShape(markerIndex, markerSize);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(PreferredContinuousSeriesRendererStyle.SHAPE_INDEX.equals(key))
        {
            this.markerIndex = pref.getInt(PreferredContinuousSeriesRendererStyle.SHAPE_INDEX, this.markerIndex);
            this.shape = ShapeSupplier.createShape(markerIndex, markerSize);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(PreferredContinuousSeriesRendererStyle.SERIES_JOINING_LINE_STROKE.equals(key))
        {
            this.stroke = SerializationUtilities.getStroke(pref, PreferredContinuousSeriesRendererStyle.SERIES_JOINING_LINE_STROKE, this.stroke);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(PreferredContinuousSeriesRendererStyle.PAINT.equals(key))
        {
            this.fillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredContinuousSeriesRendererStyle.PAINT, this.fillPaint);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(PreferredContinuousSeriesRendererStyle.SERIES_JOINING_LINE_PAINT.equals(key))
        {
            this.strokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredContinuousSeriesRendererStyle.SERIES_JOINING_LINE_PAINT, this.strokePaint);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(BAR_PAINT.equals(key))
        {
            this.barPaint = (Paint)SerializationUtilities.getSerializableObject(pref, BAR_PAINT, this.barPaint);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(BAR_STROKE.equals(key))
        {
            this.barStroke = SerializationUtilities.getStroke(pref, BAR_STROKE, this.barStroke);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(CAP_VISIBLE.equals(key))
        {
            this.capVisible = pref.getBoolean(CAP_VISIBLE, this.capVisible);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(CAP_PAINT.equals(key))
        {
            this.capPaint = (Paint)SerializationUtilities.getSerializableObject(pref, CAP_PAINT, this.capPaint);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(CAP_STROKE.equals(key))
        {
            this.capStroke = SerializationUtilities.getStroke(pref, CAP_STROKE, this.capStroke);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(CAP_WIDTH.equals(key))
        {
            this.capWidth = pref.getDouble(CAP_WIDTH, this.capWidth);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(ERROR_BAR_DRAWING_DIRECTION.equals(key))
        {
            String preferredDrawingDirectionName = pref.get(ERROR_BAR_DRAWING_DIRECTION, ErrorBarDirection.BOTH_SIDES.name());
            this.drawingDirection = ErrorBarDirection.getInstance(preferredDrawingDirectionName, ErrorBarDirection.BOTH_SIDES);        
            this.rendererData = buildChannel1DRendererData();
        }
    }

    public static PreferredContinuousSeriesErrorRendererStyle getInstance(Preferences pref, StyleTag styleKey) 
    {
        String key = pref.absolutePath() + styleKey.getPreferredStyleKey();
        PreferredContinuousSeriesErrorRendererStyle style = INSTANCES.get(key);

        if(style == null)
        {
            style = new PreferredContinuousSeriesErrorRendererStyle(pref, styleKey);
            INSTANCES.put(key, style);
        }

        return style;    
    }
}
