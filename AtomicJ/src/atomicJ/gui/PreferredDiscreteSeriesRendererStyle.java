package atomicJ.gui;

import java.awt.Paint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import atomicJ.utilities.SerializationUtilities;


public class PreferredDiscreteSeriesRendererStyle implements PreferenceChangeListener
{
    private boolean visible;

    private int markerSize;
    private int markerIndex;

    private boolean visibleInLegend; 
    private Paint paint;

    private Channel1DRendererData rendererData;

    private final Preferences pref;

    private static final SeriesStyleSupplier DEFAULT_STYLE_SUPPLIER = DefaultSeriesStyleSupplier.getSupplier();

    private static final Map<String, PreferredDiscreteSeriesRendererStyle> INSTANCES = new HashMap<>();

    public PreferredDiscreteSeriesRendererStyle(Preferences pref, StyleTag key)
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        int defSize = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerSize(key);
        int defIndex = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerIndex(key);
        Paint defPaint = DEFAULT_STYLE_SUPPLIER.getDefaultMarkerPaint(key);

        this.visible = pref.getBoolean(PreferredContinuousSeriesRendererStyle.SHOWN,true);
        this.markerSize = pref.getInt(PreferredContinuousSeriesRendererStyle.SHAPE_SIZE, defSize);
        this.markerIndex = pref.getInt(PreferredContinuousSeriesRendererStyle.SHAPE_INDEX, defIndex);

        this.visibleInLegend = pref.getBoolean(PreferredContinuousSeriesRendererStyle.VISIBLE_IN_LEGEND, true); 
        this.paint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredContinuousSeriesRendererStyle.PAINT, defPaint);
        this.rendererData = buildChannel1DRendererData();
    }

    public void saveToPreferences(Channel1DRendererData model)
    {
        pref.putBoolean(PreferredContinuousSeriesRendererStyle.SHOWN, model.getBaseSeriesVisible());
        pref.putBoolean(PreferredContinuousSeriesRendererStyle.VISIBLE_IN_LEGEND, model.getBaseSeriesVisibleInLegend());
        pref.putInt(PreferredContinuousSeriesRendererStyle.SHAPE_INDEX, model.getBaseMarkerIndex());
        pref.putFloat(PreferredContinuousSeriesRendererStyle.SHAPE_SIZE, model.getBaseMarkerSize());
        try 
        {
            SerializationUtilities.putSerializableObject(pref, PreferredContinuousSeriesRendererStyle.PAINT, model.getBasePaint());
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

    public Channel1DRendererData getChannel1DRendererData()
    {
        return rendererData;
    }


    private Channel1DRendererData buildChannel1DRendererData()
    {
        Channel1DRendererDataMutable<Channel1DRendererData> rendererDataMutable = new Channel1DRendererDataMutable<>();

        rendererDataMutable.setBaseMarkerIndex(this.markerIndex);
        rendererDataMutable.setBaseMarkerSize(this.markerSize);
        rendererDataMutable.setBaseSeriesVisible(this.visible);      
        rendererDataMutable.setBaseSeriesVisibleInLegend(this.visibleInLegend);
        rendererDataMutable.setBasePaint(this.paint);
        rendererDataMutable.setBaseLinesVisible(false);
        rendererDataMutable.setBaseShapesVisible(true);

        Channel1DRendererDataImmutable rendererDataImmutable = new Channel1DRendererDataImmutable(rendererDataMutable);

        return rendererDataImmutable;
    }

    public static PreferredDiscreteSeriesRendererStyle getInstance(Preferences pref, StyleTag styleKey)
    {
        String key = pref.absolutePath() + styleKey.getPreferredStyleKey();
        PreferredDiscreteSeriesRendererStyle style = INSTANCES.get(key);

        if(style == null)
        {
            style = new PreferredDiscreteSeriesRendererStyle(pref, styleKey);
            INSTANCES.put(key, style);
        }

        return style;
    }

    public int getMarkerSize()
    {
        return markerSize;
    }

    public int getMarkerIndex()
    {
        return markerIndex;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public boolean isVisibleInLegend()
    {
        return visibleInLegend;
    }

    public Paint getPaint()
    {
        return paint;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        String key = evt.getKey();

        if(PreferredContinuousSeriesRendererStyle.SHOWN.equals(key))
        {
            this.visible = pref.getBoolean(PreferredContinuousSeriesRendererStyle.SHOWN,true);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(PreferredContinuousSeriesRendererStyle.SHAPE_SIZE.equals(key))
        {
            this.markerSize = pref.getInt(PreferredContinuousSeriesRendererStyle.SHAPE_SIZE, this.markerSize);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(PreferredContinuousSeriesRendererStyle.SHAPE_INDEX.equals(key))
        {
            this.markerIndex = pref.getInt(PreferredContinuousSeriesRendererStyle.SHAPE_INDEX,  this.markerIndex);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(PreferredContinuousSeriesRendererStyle.PAINT.equals(key))
        {
            this.paint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredContinuousSeriesRendererStyle.PAINT, this.paint);
            this.rendererData = buildChannel1DRendererData();
        }
        else if(PreferredContinuousSeriesRendererStyle.VISIBLE_IN_LEGEND.equals(key))
        {
            this.visibleInLegend = pref.getBoolean(PreferredContinuousSeriesRendererStyle.VISIBLE_IN_LEGEND, true); 
            this.rendererData = buildChannel1DRendererData();
        }
    }
}
