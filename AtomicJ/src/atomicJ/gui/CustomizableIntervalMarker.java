package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.INTERVAL_MARKER_ALPHA;
import static atomicJ.gui.PreferenceKeys.INTERVAL_MARKER_FILL_PAINT;
import static atomicJ.gui.PreferenceKeys.INTERVAL_MARKER_OUTLINE_PAINT;
import static atomicJ.gui.PreferenceKeys.INTERVAL_MARKER_STROKE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.prefs.Preferences;


import org.jfree.chart.event.MarkerChangeEvent;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.data.Range;

import atomicJ.utilities.SerializationUtilities;

public class CustomizableIntervalMarker extends IntervalMarker
{
    private static final long serialVersionUID = 1L;
    public static final Stroke DEFAULT_OUTLINE_STROKE = new BasicStroke(2.5f);

    private final String name;
    private final Preferences pref;
    private boolean visible = true;

    public CustomizableIntervalMarker(String name, double start, double end, Preferences pref, float defaultAlpha) 
    {
        super(start, end);

        this.name = name;
        this.pref = pref;
        setPreferredStyle(pref, defaultAlpha);
    }

    public String getName()
    {
        return name;
    }

    private void setPreferredStyle(Preferences pref, float defaultAlpha)
    {
        float alpha = pref.getFloat(INTERVAL_MARKER_ALPHA, defaultAlpha);

        Paint fillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, INTERVAL_MARKER_FILL_PAINT, Color.blue);
        Paint outlinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, INTERVAL_MARKER_OUTLINE_PAINT, Color.black);

        Stroke stroke = SerializationUtilities.getStroke(pref, INTERVAL_MARKER_STROKE, DEFAULT_OUTLINE_STROKE);

        setAlpha(alpha);
        setPaint(fillPaint);
        setOutlinePaint(outlinePaint);
        setOutlineStroke(stroke);
    }

    public boolean isVisible()
    {
        return visible;
    }

    //The field visible should be respected be renderers
    public void setVisible(boolean visible)
    {
        this.visible = visible;

        notifyListeners(new MarkerChangeEvent(this));
    }

    public Range getRange()
    {
        double x1 = getStartValue();
        double x2 = getEndValue();

        Range range = new Range(x1,x2);
        return range;
    }

    public Preferences getPreferences()
    {
        return pref;
    }
}
