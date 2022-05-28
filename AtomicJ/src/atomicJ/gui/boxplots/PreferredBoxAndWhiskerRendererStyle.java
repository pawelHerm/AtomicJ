package atomicJ.gui.boxplots;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import atomicJ.gui.ColorSupplier;
import atomicJ.gui.DefaultColorSupplier;
import atomicJ.gui.StyleTag;
import atomicJ.utilities.SerializationUtilities;


public class PreferredBoxAndWhiskerRendererStyle implements PreferenceChangeListener
{
    //box properties
    public static final String BOX_FILLED = "BoxFilled";
    public static final String BOX_WIDTH = "BoxWidth";
    public static final String BOX_FILL_PAINT = "BoxFillPaint";
    public static final String BOX_OUTLINE_VISIBLE = "BoxOutlineVisible";
    public static final String BOX_OUTLINE_PAINT = "BoxOutlinePaint";
    public static final String BOX_OUTLINE_STROKE = "BoxOutlineStroke";

    //whiskers properties

    public static final String WHISKER_PAINT = "WhiskerPaint";
    public static final String WHISKER_STROKE = "WhiskerStroke";
    public static final String WHISKER_CROSSBAR_VISIBLE = "WhiskerCrossbarVisible";
    public static final String WHISKER_CROSSBAR_WIDTH = "WhiskerCrossBarWidth";
    public static final String WHISKER_CROSSBAR_PAINT = "WhiskerCrossBarPaint";
    public static final String WHISKER_CROSSBAR_STROKE = "WhiskerCrossBartStroke";

    //median properties
    public static final String MEDIAN_VISIBLE = "MedianVisible";
    public static final String MEDIAN_PAINT = "MedianPaint";
    public static final String MEDIAN_STROKE = "MedianStroke";

    //mean properties
    public static final String MEAN_VISIBLE = "MeanVisible";
    public static final String MEAN_FILLED = "MeanFilled";
    public static final String MEAN_FILL_PAINT = "MeanFillPaint";
    public static final String MEAN_OUTLINE_VISIBLE = "MeanOutlineVisible";
    public static final String MEAN_OUTLINE_PAINT = "MeanOutlinePaint";
    public static final String MEAN_OUTLINE_STROKE = "MeanOutlineStroke";

    //outlier properties
    public static final String OUTLIERS_VISIBLE = "OutliersVisible";
    public static final String OUTLIER_SIZE = "OutlierSize";
    public static final String OUTLIER_MARKER_INDEX = "OutlierMarkerIndex";
    public static final String OUTLIER_FILLED = "OutlierFilled";
    public static final String OUTLIER_FILL_PAINT = "OutlierFillPaint";
    public static final String OUTLIER_OUTLINE_VISIBLE = "OutlierOutlineVisible";
    public static final String OUTLIER_STROKE = "OutlierStroke";
    public static final String OUTLIER_STROKE_PAINT = "OutlierStrokePaint";

    //box
    private boolean boxFilled;
    private double boxWidth;
    private Paint boxFillPaint;
    private boolean boxOutlineVisible;
    private Paint boxOutlinePaint;
    private Stroke boxOutlineStroke;  

    //mean
    private boolean meanVisible;   
    private boolean meanFilled;
    private Paint meanFillPaint;
    private boolean meanOutlineVisible;
    private Paint meanOutlinePaint;
    private Stroke meanOutlineStroke;

    //median
    private boolean medianVisible;
    private Paint medianPaint;
    private Stroke medianStroke;

    //whiskers
    private Paint whiskerPaint;
    private Stroke whiskerStroke;
    private boolean whiskerCrossbarVisible;
    private double whiskerCrossBarWidth;
    private Paint whiskerCrossBarPaint;
    private Stroke whiskerCrossBarStroke;

    //outliers
    private boolean outliersVisible;
    private int outlierMarkerIndex;
    private float outlierSize;
    private boolean outlierFilled;
    private Paint outlierFillPaint;
    private boolean outlierOutlineVisible;
    private Stroke outlierStroke;
    private Paint outlierStrokePaint;


    private final Preferences pref;

    private static final ColorSupplier DEFAULT_SUPPLIER = DefaultColorSupplier.getSupplier();

    private static final Map<String, PreferredBoxAndWhiskerRendererStyle> INSTANCES = new LinkedHashMap<>();

    public PreferredBoxAndWhiskerRendererStyle(Preferences pref, StyleTag styleKey)
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        Color defPaint = DEFAULT_SUPPLIER.getColor(styleKey).brighter().brighter();   

        //box
        this.boxFilled = pref.getBoolean(BOX_FILLED, true);
        this.boxWidth = pref.getDouble(BOX_WIDTH, 20.);
        this.boxFillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, BOX_FILL_PAINT, defPaint);

        this.boxOutlineVisible = pref.getBoolean(OUTLIERS_VISIBLE, true);
        this.boxOutlinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, BOX_OUTLINE_PAINT, Color.black);
        this.boxOutlineStroke = SerializationUtilities.getStroke(pref, BOX_OUTLINE_STROKE, new BasicStroke(2.f));

        //whiskers
        this.whiskerPaint = (Paint)SerializationUtilities.getSerializableObject(pref, WHISKER_PAINT, Color.black);
        this.whiskerStroke = SerializationUtilities.getStroke(pref, WHISKER_STROKE, new BasicStroke(2.f));
        this.whiskerCrossbarVisible = pref.getBoolean(WHISKER_CROSSBAR_VISIBLE, true);
        this.whiskerCrossBarWidth = pref.getDouble(WHISKER_CROSSBAR_WIDTH, 100);
        this.whiskerCrossBarPaint = (Paint)SerializationUtilities.getSerializableObject(pref, WHISKER_CROSSBAR_PAINT, Color.black);
        this.whiskerCrossBarStroke = SerializationUtilities.getStroke(pref, WHISKER_CROSSBAR_STROKE, new BasicStroke(2.f));

        //mean
        this.meanVisible = pref.getBoolean(MEAN_VISIBLE, true);
        this.meanFilled = pref.getBoolean(MEAN_FILLED, true);
        this.meanFillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, MEAN_FILL_PAINT, Color.black);
        this.meanOutlineVisible = pref.getBoolean(MEAN_OUTLINE_VISIBLE, false);
        this.meanOutlineStroke = SerializationUtilities.getStroke(pref, MEAN_OUTLINE_STROKE, new BasicStroke(2.f));
        this.meanOutlinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, MEAN_OUTLINE_PAINT, Color.black);

        //median
        this.medianVisible = pref.getBoolean(MEDIAN_VISIBLE,true);
        this.medianPaint = (Paint)SerializationUtilities.getSerializableObject(pref, MEDIAN_PAINT, Color.black);
        this.medianStroke = SerializationUtilities.getStroke(pref, MEDIAN_STROKE, new BasicStroke(2.f));

        //outliers
        this.outliersVisible = pref.getBoolean(OUTLIERS_VISIBLE, true);
        this.outlierMarkerIndex = pref.getInt(OUTLIER_MARKER_INDEX, 1);
        this.outlierSize = pref.getFloat(OUTLIER_SIZE, 7.f);
        this.outlierFilled = pref.getBoolean(OUTLIER_FILLED, false);
        this.outlierFillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, OUTLIER_FILL_PAINT, Color.black);
        this.outlierOutlineVisible = pref.getBoolean(OUTLIER_OUTLINE_VISIBLE, true);
        this.outlierStrokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, OUTLIER_STROKE_PAINT, Color.black);
        this.outlierStroke = SerializationUtilities.getStroke(pref, OUTLIER_STROKE, new BasicStroke(1.5f));
    }   

    public Preferences getPreferences()
    {
        return pref;
    }

    private void flushPreferences()
    {
        try 
        {
            pref.flush();
        } 
        catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public boolean isMedianVisible()
    {
        return medianVisible;
    }

    public void setMedianVisible(boolean medianVisible)
    {
        this.medianVisible = medianVisible;

        pref.putBoolean(MEDIAN_VISIBLE, medianVisible);
        flushPreferences();
    }

    public boolean isMeanVisible()
    {
        return meanVisible;
    }

    public void setMeanVisible(boolean meanVisible)
    {
        this.meanVisible = meanVisible;

        pref.putBoolean(MEAN_VISIBLE, meanVisible);
        flushPreferences();
    }

    public boolean isOutliersVisible()
    {
        return outliersVisible;
    }

    public void setOutliersVisible(boolean outliersVisible)
    {
        this.outliersVisible = outliersVisible;

        pref.putBoolean(OUTLIERS_VISIBLE, outliersVisible);
        flushPreferences();
    }

    public int getOutlierMarkerIndex()
    {
        return outlierMarkerIndex;
    }

    public void setOutlierMarkerIndex(int outlierMarkerIndex)
    {
        this.outlierMarkerIndex = outlierMarkerIndex;

        pref.putInt(OUTLIER_MARKER_INDEX, outlierMarkerIndex);
        flushPreferences();
    }

    public float getOutlierSize()
    {
        return outlierSize;
    }

    public void setOutlierMarkerSize(float outlierMarkerSize)
    {
        this.outlierSize = outlierMarkerSize;

        pref.putFloat(OUTLIER_SIZE, outlierMarkerSize);
        flushPreferences();
    }

    public boolean isOutlierFilled()
    {
        return outlierFilled;
    }

    public void setOutlierFilled(boolean outlierFilled)
    {
        this.outlierFilled = outlierFilled;

        pref.putBoolean(OUTLIER_FILLED, outlierFilled);
        flushPreferences();
    }

    public Paint getOutlierFillPaint()
    {
        return outlierFillPaint;
    }

    public void setOutlierFillPaint(Paint outlierFillPaint)
    {
        this.outlierFillPaint = outlierFillPaint;

        try 
        {
            SerializationUtilities.putSerializableObject(pref, OUTLIER_FILL_PAINT, outlierFillPaint);
            flushPreferences();
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public boolean isOutlierOutlineVisible()
    {
        return outlierOutlineVisible;
    }

    public void setOutlierOutlineVisible(boolean outlierOutlineVisible)
    {
        this.outlierOutlineVisible = outlierOutlineVisible;

        pref.putBoolean(OUTLIER_OUTLINE_VISIBLE, outlierOutlineVisible);
        flushPreferences();
    }

    public Stroke getOutlierStroke()
    {
        return outlierStroke;
    }

    public void setOutlierStroke(Stroke outlierStroke)
    {
        this.outlierStroke = outlierStroke;

        try 
        {
            SerializationUtilities.putStroke(pref, OUTLIER_STROKE, outlierStroke);
            flushPreferences();
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public Paint getOutlierStrokePaint()
    {
        return outlierStrokePaint;
    }

    public void setOutlierStrokePaint(Paint outlierStrokePaint)
    {
        this.outlierStrokePaint = outlierStrokePaint;

        try 
        {
            SerializationUtilities.putSerializableObject(pref, 
                    OUTLIER_STROKE_PAINT, outlierStrokePaint);
            flushPreferences();
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public double getBoxWidth() 
    {
        return this.boxWidth;
    }

    public void setBoxWidth(double boxWidth)
    {
        this.boxWidth = boxWidth;

        pref.putDouble(BOX_WIDTH, boxWidth);
        flushPreferences();
    }

    public Paint getMeanPaint()
    {
        return meanFillPaint;
    }

    public void setMeanPaint(Paint meanPaint)
    {
        this.meanFillPaint = meanPaint;

        try 
        {
            SerializationUtilities.putSerializableObject(pref, MEAN_FILL_PAINT, meanPaint);
            flushPreferences();
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public boolean isMeanFilled()
    {
        return meanFilled;
    }

    public void setMeanFilled(boolean meanFilled)
    {
        this.meanFilled = meanFilled;

        pref.putBoolean(MEAN_FILLED, meanFilled);
        flushPreferences();
    }

    public boolean isMeanOutlineVisible()
    {
        return meanOutlineVisible;
    }

    public void setMeanOutlineVisible(boolean meanOutlineVisible)
    {
        this.meanOutlineVisible = meanOutlineVisible;

        pref.putBoolean(MEAN_OUTLINE_VISIBLE, meanOutlineVisible);
        flushPreferences();
    }

    public Paint getMeanOutlinePaint()
    {
        return meanOutlinePaint;
    }

    public void setMeanOutlinePaint(Paint meanOutlinePaint)
    {
        this.meanOutlinePaint = meanOutlinePaint;

        try 
        {
            SerializationUtilities.putSerializableObject(pref, MEAN_OUTLINE_PAINT, meanOutlinePaint);
            flushPreferences();
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public Stroke getMeanOutlineStroke()
    {
        return meanOutlineStroke;
    }

    public void setMeanOutlineStroke(Stroke meanOutlineStroke)
    {
        this.meanOutlineStroke = meanOutlineStroke;

        try 
        {
            SerializationUtilities.putStroke(pref, MEAN_OUTLINE_STROKE, meanOutlineStroke);
            flushPreferences();
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public Paint getMedianPaint()
    {
        return medianPaint;
    }

    public void setMedianPaint(Paint medianPaint)
    {
        this.medianPaint = medianPaint;

        try 
        {
            SerializationUtilities.putSerializableObject(pref, MEDIAN_PAINT, medianPaint);
            flushPreferences();
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public Stroke getMedianStroke()
    {
        return medianStroke;
    }

    public void setMedianStroke(Stroke medianStroke)
    {
        this.medianStroke = medianStroke;

        try 
        {
            SerializationUtilities.putStroke(pref, MEDIAN_STROKE, medianStroke);
            flushPreferences();
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public Paint getWhiskerPaint()
    {
        return whiskerPaint;
    }

    public void setWhiskerPaint(Paint whiskerPaint)
    {
        this.whiskerPaint = whiskerPaint;

        try 
        {
            SerializationUtilities.putSerializableObject(pref, WHISKER_PAINT, whiskerPaint);
            flushPreferences();
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public Stroke getWhiskerStroke()
    {
        return whiskerStroke;
    }

    public void setWhiskerStroke(Stroke whiskerStroke)
    {
        this.whiskerStroke = whiskerStroke;

        try 
        {
            SerializationUtilities.putStroke(pref, WHISKER_STROKE, whiskerStroke);
            flushPreferences();
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public boolean isWhiskerCrossBarVisible()
    {
        return whiskerCrossbarVisible;
    }

    public void setWhiskerCrossBarVisible(boolean whiskerCrossbarVisible)
    {
        this.whiskerCrossbarVisible = whiskerCrossbarVisible;

        pref.putBoolean(WHISKER_CROSSBAR_VISIBLE, whiskerCrossbarVisible);     
        flushPreferences();
    }

    public double getWhiskerCrossBarWidth()
    {
        return whiskerCrossBarWidth;
    }

    public void setWhiskerCrossBarWidth(double whiskerCrossBarWidth)
    {
        this.whiskerCrossBarWidth = whiskerCrossBarWidth;

        pref.putDouble(WHISKER_CROSSBAR_WIDTH, whiskerCrossBarWidth);
        flushPreferences();
    }

    public Paint getWhiskerCrossBarPaint()
    {
        return whiskerCrossBarPaint;
    }

    public void setWhiskerCrossBarPaint(Paint whiskerCrossBarPaint)
    {
        this.whiskerCrossBarPaint = whiskerCrossBarPaint;

        try 
        {
            SerializationUtilities.putSerializableObject(pref, WHISKER_CROSSBAR_PAINT,
                    whiskerCrossBarPaint);
            flushPreferences();
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public Stroke getWhiskerCrossBarStroke()
    {
        return whiskerCrossBarStroke;
    }

    public void setWhiskerCrossBarStroke(Stroke whiskerCrossBarStroke)
    {
        this.whiskerCrossBarStroke = whiskerCrossBarStroke;

        try 
        {
            SerializationUtilities.putSerializableObject(pref, WHISKER_CROSSBAR_STROKE,
                    whiskerCrossBarStroke);
            flushPreferences();
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public boolean isBoxOutlineVisible()
    {
        return boxOutlineVisible;
    }

    public void setBoxOutlineVisible(boolean boxOutlineVisible)
    {
        this.boxOutlineVisible = boxOutlineVisible;

        pref.putBoolean(BOX_OUTLINE_VISIBLE, boxOutlineVisible);
        flushPreferences();
    }

    public Paint getBoxOutlinePaint()    
    {
        return boxOutlinePaint;
    }

    public void setBoxOutlinePaint(Paint boxOutlinePaint)
    {
        this.boxOutlinePaint = boxOutlinePaint;

        try 
        {
            SerializationUtilities.putSerializableObject(pref, BOX_OUTLINE_PAINT, boxOutlinePaint);
            flushPreferences();
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public Stroke getBoxOutlineStroke()
    {
        return boxOutlineStroke;
    }

    public void setBoxOutlineStroke(Stroke boxOutlineStroke)
    {
        this.boxOutlineStroke = boxOutlineStroke;

        try 
        {
            SerializationUtilities.putStroke(pref, BOX_OUTLINE_STROKE, boxOutlineStroke);
            flushPreferences();
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }


    public Paint getBoxFillPaint()
    {
        return boxFillPaint;
    }

    public void setBoxFillPaint(Paint boxFillPaint)
    {
        this.boxFillPaint = boxFillPaint;

        try 
        {
            SerializationUtilities.putSerializableObject(pref, BOX_FILL_PAINT, boxFillPaint);
            flushPreferences();
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    public boolean getFillBox() 
    {
        return this.boxFilled;
    }

    public void setFillBox(boolean fillBox)
    {
        this.boxFilled = fillBox;

        pref.putBoolean(BOX_FILLED, fillBox);
        flushPreferences();
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        String key = evt.getKey();


        //outliers 8 properties
        if(OUTLIERS_VISIBLE.equals(key))
        {
            this.outliersVisible = pref.getBoolean(OUTLIERS_VISIBLE, this.outliersVisible);
        }
        else if(OUTLIER_MARKER_INDEX.equals(key))
        {
            this.outlierMarkerIndex = pref.getInt(OUTLIER_MARKER_INDEX, this.outlierMarkerIndex);
        }
        else if(OUTLIER_SIZE.equals(key))
        {
            this.outlierSize = pref.getFloat(OUTLIER_SIZE, this.outlierSize);
        }
        else if(OUTLIER_FILLED.equals(key))
        {
            this.outlierFilled = pref.getBoolean(OUTLIER_FILLED, this.outlierFilled);
        }
        else if(OUTLIER_FILL_PAINT.equals(key))
        {
            this.outlierFillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, OUTLIER_FILL_PAINT, this.outlierFillPaint);
        }
        else if(OUTLIER_OUTLINE_VISIBLE.equals(key))
        {
            this.outlierOutlineVisible = pref.getBoolean(OUTLIER_OUTLINE_VISIBLE, this.outlierOutlineVisible);
        }
        else if(OUTLIER_STROKE.equals(key))
        {
            this.outlierStroke = SerializationUtilities.getStroke(pref, OUTLIER_STROKE, this.outlierStroke);
        }
        else if(OUTLIER_STROKE_PAINT.equals(key))
        {
            this.outlierStrokePaint = (Paint)SerializationUtilities.getSerializableObject(pref, OUTLIER_STROKE_PAINT, this.outlierStrokePaint);
        }

        //median 3 properties
        else if(MEDIAN_VISIBLE.equals(key))
        {
            this.medianVisible = pref.getBoolean(MEDIAN_VISIBLE, this.medianVisible);
        }
        else if(MEDIAN_PAINT.equals(key))
        {
            this.medianPaint = (Paint)SerializationUtilities.getSerializableObject(pref, MEDIAN_PAINT, this.medianPaint);
        }
        else if(MEDIAN_STROKE.equals(key))
        {
            this.medianStroke = SerializationUtilities.getStroke(pref, MEDIAN_STROKE, this.medianStroke);
        }

        //mean 6 properties
        else if(MEAN_VISIBLE.equals(key))
        {
            this.meanVisible = pref.getBoolean(MEAN_VISIBLE, this.meanVisible);
        }
        else if(MEAN_FILLED.equals(key))
        {
            this.meanFilled = pref.getBoolean(MEAN_FILLED, this.meanFilled);
        }
        else if(MEAN_FILL_PAINT.equals(key))
        {
            this.meanFillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, MEAN_FILL_PAINT, this.meanFillPaint);
        }
        else if(MEAN_OUTLINE_VISIBLE.equals(key))
        {
            this.meanOutlineVisible = pref.getBoolean(MEAN_OUTLINE_VISIBLE, this.meanOutlineVisible);
        }
        else if(MEAN_OUTLINE_PAINT.equals(key))
        {
            this.meanOutlinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, MEAN_OUTLINE_PAINT, this.meanOutlinePaint);
        }
        else if(MEAN_OUTLINE_STROKE.equals(key))
        {
            this.meanOutlineStroke = SerializationUtilities.getStroke(pref, MEAN_OUTLINE_STROKE, this.meanOutlineStroke);
        }


        //whiskers 6 properties
        else if(WHISKER_PAINT.equals(key))
        {
            this.whiskerPaint = (Paint)SerializationUtilities.getSerializableObject(pref, WHISKER_PAINT, this.whiskerPaint);
        }
        else if(WHISKER_STROKE.equals(key))
        {
            this.whiskerStroke = SerializationUtilities.getStroke(pref, WHISKER_STROKE, this.whiskerStroke);
        }
        else if(WHISKER_CROSSBAR_VISIBLE.equals(key))
        {
            this.whiskerCrossbarVisible = pref.getBoolean(WHISKER_CROSSBAR_VISIBLE, this.whiskerCrossbarVisible);
        }   
        else if(WHISKER_CROSSBAR_WIDTH.equals(key))
        {
            this.whiskerCrossBarWidth = pref.getDouble(WHISKER_CROSSBAR_WIDTH, this.whiskerCrossBarWidth);
        }
        else if(WHISKER_CROSSBAR_PAINT.equals(key))
        {
            this.whiskerCrossBarPaint = (Paint)SerializationUtilities.getSerializableObject(pref, WHISKER_CROSSBAR_PAINT, this.whiskerCrossBarPaint);
        }
        else if(WHISKER_CROSSBAR_STROKE.equals(key))
        {
            this.whiskerCrossBarStroke = SerializationUtilities.getStroke(pref, WHISKER_CROSSBAR_STROKE, this.whiskerCrossBarStroke);
        }

        //box 6 properties
        else if(BOX_FILLED.equals(key))
        {
            this.boxFilled = pref.getBoolean(BOX_FILLED, this.boxFilled);
        }  
        else if(BOX_WIDTH.equals(key))
        {
            this.boxWidth = pref.getDouble(BOX_WIDTH, this.boxWidth);
        }
        else if(BOX_FILL_PAINT.equals(key))
        {
            this.boxFillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, BOX_FILL_PAINT, this.boxFillPaint);
        }
        else if(BOX_OUTLINE_VISIBLE.equals(key))
        {
            this.boxOutlineVisible = pref.getBoolean(BOX_OUTLINE_VISIBLE, this.boxOutlineVisible);
        }
        else if(BOX_OUTLINE_PAINT.equals(key))
        {
            this.boxOutlinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, BOX_OUTLINE_PAINT, this.boxOutlinePaint);
        }
        else if(BOX_OUTLINE_STROKE.equals(key))
        {
            this.boxOutlineStroke = SerializationUtilities.getStroke(pref, BOX_OUTLINE_STROKE, this.boxOutlineStroke);
        }
    }

    public static PreferredBoxAndWhiskerRendererStyle getInstance(Preferences pref, StyleTag styleKey) 
    {
        String key = pref.absolutePath() + styleKey.getPreferredStyleKey();
        PreferredBoxAndWhiskerRendererStyle style = INSTANCES.get(key);

        if(style == null)
        {
            style = new PreferredBoxAndWhiskerRendererStyle(pref, styleKey);
            INSTANCES.put(key, style);
        }

        return style;    
    };

}
