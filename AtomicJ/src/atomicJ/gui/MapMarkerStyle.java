package atomicJ.gui;

import java.awt.Color;
import java.awt.Paint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import atomicJ.gui.annotations.AnnotationSimpleStyle;
import atomicJ.utilities.SerializationUtilities;


public class MapMarkerStyle extends AnnotationSimpleStyle
{
    private static final long serialVersionUID = 1L;

    public static final String MAP_MARKER_INDEX = "MapMarkerIndex";
    public static final String MAP_MARKER_SIZE = "MapMarkerSize";
    public static final String MAP_MARKER_FILLED = "MapMarkerFilled";
    public static final String MAP_MARKER_FILL_PAINT = "MapMarkerFillPaint";  
    public static final String MAP_MARKER_LABEL_TYPE = "MapMarkerLabelType";
    public static final String MAP_MARKER_STYLE_COMPLETELY_CHANGED = "MapMarkerStyleCompletelyChanged";

    private boolean filled;

    private int markerIndex;
    private float markerSize;
    private Paint fillPaint;

    private final Set<String> valueLabels = new LinkedHashSet<>();
    private final List<MapMarkerLabelType> customLabelTypes = new ArrayList<>();

    private MapMarkerLabelType labelType;

    public MapMarkerStyle(Preferences pref, Paint defaultPaint) 
    {
        super(pref, defaultPaint, 5.f,  0.5f);
        setDefaultMapMarkerStyle();
    }

    public void setValueLabelTypes(Collection<? extends String> valueLabelsNew)
    {
        this.valueLabels.clear();
        this.valueLabels.addAll(valueLabelsNew);
        updateCustomLabelTypes();
    }

    private void updateCustomLabelTypes()
    {
        this.customLabelTypes.clear();

        for(String labelTypeName : valueLabels)
        {
            MapMarkerChannelLabelType channelLabel = new MapMarkerChannelLabelType(labelTypeName);
            customLabelTypes.add(channelLabel);
        }
    }

    public List<MapMarkerLabelType> getSupportedLabelTypes()
    {
        List<MapMarkerLabelType> labelTypes = new ArrayList<>();

        labelTypes.addAll(Arrays.asList(BasicMapMarkerLabelType.values()));
        labelTypes.addAll(customLabelTypes);

        return labelTypes;
    }

    private void setDefaultMapMarkerStyle()
    {
        Preferences pref = getPreferences();

        this.filled = pref.getBoolean(MAP_MARKER_FILLED, false);
        this.markerIndex = pref.getInt(MAP_MARKER_INDEX, 13);
        this.markerSize = pref.getFloat(MAP_MARKER_SIZE, 12.f);
        this.fillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, MAP_MARKER_FILL_PAINT, Color.white);

        this.labelType = BasicMapMarkerLabelType.valueOf(pref.get(MAP_MARKER_LABEL_TYPE, BasicMapMarkerLabelType.POSITION.name()));

        firePropertyChange(MAP_MARKER_STYLE_COMPLETELY_CHANGED, false, true);
    }

    @Override
    public void saveStyleAsDefault()
    {
        super.saveStyleAsDefault();

        Preferences pref = getPreferences();

        pref.putBoolean(MAP_MARKER_FILLED, filled);
        pref.putInt(MAP_MARKER_INDEX, markerIndex);
        pref.putFloat(MAP_MARKER_SIZE, markerSize);

        pref.put(MAP_MARKER_LABEL_TYPE, labelType.name());

        try 
        {   
            SerializationUtilities.putSerializableObject(pref, MAP_MARKER_FILL_PAINT, fillPaint);
        }
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }   
    }

    public int getMarkerIndex()
    {
        return markerIndex;
    }

    public void setMarkerIndex(int markerIndexNew)
    {
        int markerIndexOld = this.markerIndex;
        this.markerIndex = markerIndexNew;

        firePropertyChange(MAP_MARKER_INDEX, markerIndexOld, markerIndexNew);
    }

    public float getMarkerSize()
    {
        return markerSize;
    }

    public void setMarkerSize(float markerSizeNew)
    {
        float markerSizeOld = this.markerSize;
        this.markerSize = markerSizeNew;

        firePropertyChange(MAP_MARKER_SIZE, markerSizeOld, markerSizeNew);
    }

    public boolean isFilled()
    {
        return filled;
    }

    public void setFilled(boolean filledNew)
    {
        boolean filledOld = this.filled;
        this.filled = filledNew;

        firePropertyChange(MAP_MARKER_FILLED, filledOld, filledNew);
    }

    public Paint getFillPaint()
    {
        return fillPaint;
    }

    public void setFillPaint(Paint fillPaintNew)
    {
        Paint fillPaintOld = this.fillPaint;
        this.fillPaint = fillPaintNew;

        firePropertyChange(MAP_MARKER_FILL_PAINT, fillPaintOld, fillPaintNew);
    }

    public MapMarkerLabelType getLabelType()
    {
        return labelType;
    }

    public void setLabelType(MapMarkerLabelType labelTypeNew)
    {
        MapMarkerLabelType labelTypeOld = this.labelType;
        this.labelType = labelTypeNew;

        firePropertyChange(MAP_MARKER_LABEL_TYPE, labelTypeOld, labelTypeNew);
    }
}
