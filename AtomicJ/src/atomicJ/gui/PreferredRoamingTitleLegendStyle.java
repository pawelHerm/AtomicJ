package atomicJ.gui;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;


import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import atomicJ.utilities.SerializationUtilities;

public class PreferredRoamingTitleLegendStyle implements PreferenceChangeListener
{
    public static final String LEGEND_FRAME_PAINT = "LegendFramePaint";
    public static final String LEGEND_FRAME_VISIBLE = "LegendFrameVisible";
    public static final String LEGEND_ITEM_PAINT = "LegendItemPaint";
    public static final String LEGEND_ITEM_FONT = "LegendItemFont";
    public static final String LEGEND_OUTSIDE_POSITION = "LegendOutsidePosition";
    public static final String LEGEND_INSIDE_Y = "LegendInsideY";
    public static final String LEGEND_INSIDE_X = "LegendInsideX";
    public static final String LEGEND_INSIDE = "LegendInside";
    public static final String LEGEND_VISIBLE = "LegendVisible";
    public static final String LEGEND_MARGIN_RIGHT = "LegendMarginRight";
    public static final String LEGEND_MARGIN_LEFT = "LegendMarginLeft";
    public static final String LEGEND_MARGIN_BOTTOM = "LegendMarginBottom";
    public static final String LEGEND_MARGIN_TOP = "LegendMarginTop";
    public static final String LEGEND_PADDING_RIGHT = "LegendPaddingRight";
    public static final String LEGEND_PADDING_LEFT = "LegendPaddingLeft";
    public static final String LEGEND_PADDING_BOTTOM = "LegendPaddingBottom";
    public static final String LEGEND_PADDING_TOP = "LegendPaddingTop";
    public static final String LEGEND_FRAME_STROKE = "LegendFrameStroke";

    private boolean frameVisible;
    private Stroke frameStroke;
    private Paint framePaint;

    private boolean legendVisible;
    private boolean legendInside;
    private double legendInsideX;
    private double legendInsideY;

    private double marginTop;
    private double marginBottom;
    private double marginLeft;
    private double marginRight;

    private RectangleInsets marginInsets;
    private RectangleEdge outsidePosition;

    private final String styleKey;
    private final Preferences pref;
    public static final String LEGEND_BACKGROUND_PAINT = "LegendBackgroundPaint";

    private static final ChartStyleSupplier DEFAULT_STYLE_SUPPLIER = DefaultChartStyleSupplier.getSupplier();

    public PreferredRoamingTitleLegendStyle(Preferences pref, String styleKey)
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        this.styleKey = styleKey;

        boolean defaultFrameVisible = DEFAULT_STYLE_SUPPLIER.getDefaultLegendFrameVisible(styleKey);
        boolean defaultLegendVisible = DEFAULT_STYLE_SUPPLIER.getDefaultLegendVisible(styleKey);
        boolean defaultLegendInside = DEFAULT_STYLE_SUPPLIER.getDefaultLegendInside(styleKey);
        double defaultInsideX = DEFAULT_STYLE_SUPPLIER.getDefaultLegendInsideX(styleKey);
        double defaultInsideY = DEFAULT_STYLE_SUPPLIER.getDefaultLegendInsideY(styleKey);

        this.frameVisible = pref.getBoolean(PreferredRoamingTitleLegendStyle.LEGEND_FRAME_VISIBLE, defaultFrameVisible);
        this.legendVisible = pref.getBoolean(PreferredRoamingTitleLegendStyle.LEGEND_VISIBLE, defaultLegendVisible);
        this.legendInside = pref.getBoolean(PreferredRoamingTitleLegendStyle.LEGEND_INSIDE, defaultLegendInside);
        this.legendInsideX = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_INSIDE_X, defaultInsideX);
        this.legendInsideY = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_INSIDE_Y, defaultInsideY);

        this.outsidePosition = (RectangleEdge)SerializationUtilities.getSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_OUTSIDE_POSITION, RectangleEdge.RIGHT);

        this.marginTop = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_TOP, 5);
        this.marginBottom = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_BOTTOM, 5);
        this.marginLeft = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_LEFT, 5);
        this.marginRight = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_RIGHT, 5);

        boolean marginsPresent = marginTop + marginBottom + marginLeft + marginRight > 0.001;
        this.marginInsets = marginsPresent ? new RectangleInsets(marginTop, marginLeft, marginBottom, marginRight) : null;

        this.framePaint = (Paint) SerializationUtilities.getSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_FRAME_PAINT, Color.black);
        this.frameStroke = SerializationUtilities.getStroke(pref, PreferredRoamingTitleLegendStyle.LEGEND_FRAME_STROKE, new BasicStroke(1.f));
    }

    public Preferences getPreferences()
    {
        return pref;
    }

    public String getStyleKey()
    {
        return styleKey;
    }

    public ChartStyleSupplier getSupplier()
    {
        return DEFAULT_STYLE_SUPPLIER;
    }

    public boolean frameVisible()
    {
        return frameVisible;
    };

    public Stroke frameStroke()
    {
        return frameStroke;
    };

    public Paint framePaint()
    {
        return framePaint;
    };    

    public boolean legendVisible()
    {
        return legendVisible;
    };

    public boolean legendInside()
    {
        return legendInside;
    };

    public double legendInsideX()
    {
        return legendInsideX;
    };

    double legendInsideY()
    {
        return legendInsideY;
    };

    public RectangleInsets marginInsets()
    {
        return marginInsets;
    };

    public RectangleEdge outsidePosition()
    {
        return outsidePosition;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        String key = evt.getKey();

        if(PreferredRoamingTitleLegendStyle.LEGEND_FRAME_VISIBLE.equals(key))
        {
            boolean defaultFrameVisible = DEFAULT_STYLE_SUPPLIER.getDefaultLegendFrameVisible(styleKey);
            this.frameVisible = pref.getBoolean(PreferredRoamingTitleLegendStyle.LEGEND_FRAME_VISIBLE, defaultFrameVisible);
        }
        else if(PreferredRoamingTitleLegendStyle.LEGEND_VISIBLE.equals(key))
        {
            boolean defaultLegendVisible = DEFAULT_STYLE_SUPPLIER.getDefaultLegendVisible(styleKey);
            this.legendVisible = pref.getBoolean(PreferredRoamingTitleLegendStyle.LEGEND_VISIBLE, defaultLegendVisible);
        }
        else if(PreferredRoamingTitleLegendStyle.LEGEND_INSIDE.equals(key))
        {
            boolean defaultLegendInside = DEFAULT_STYLE_SUPPLIER.getDefaultLegendInside(styleKey);
            this.legendInside = pref.getBoolean(PreferredRoamingTitleLegendStyle.LEGEND_INSIDE, defaultLegendInside);
        }
        else if(PreferredRoamingTitleLegendStyle.LEGEND_INSIDE_X.equals(key))
        {
            double defaultInsideX = DEFAULT_STYLE_SUPPLIER.getDefaultLegendInsideX(styleKey);
            this.legendInsideX = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_INSIDE_X, defaultInsideX);
        }
        else if(PreferredRoamingTitleLegendStyle.LEGEND_INSIDE_Y.equals(key))
        {
            double defaultInsideY = DEFAULT_STYLE_SUPPLIER.getDefaultLegendInsideY(styleKey);
            this.legendInsideY = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_INSIDE_Y, defaultInsideY);
        }
        else if(PreferredRoamingTitleLegendStyle.LEGEND_OUTSIDE_POSITION.equals(key))
        {
            this.outsidePosition = (RectangleEdge)SerializationUtilities.getSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_OUTSIDE_POSITION, RectangleEdge.RIGHT);
        }
        else if(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_TOP.equals(key))
        {
            this.marginTop = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_TOP, 5);
            boolean marginsPresent = marginTop + marginBottom + marginLeft + marginRight > 0.001;
            this.marginInsets = marginsPresent ? new RectangleInsets(marginTop, marginLeft, marginBottom, marginRight) : null;      
        }
        else if(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_BOTTOM.equals(key))
        {
            this.marginBottom = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_BOTTOM, 5);
            boolean marginsPresent = marginTop + marginBottom + marginLeft + marginRight > 0.001;
            this.marginInsets = marginsPresent ? new RectangleInsets(marginTop, marginLeft, marginBottom, marginRight) : null;     
        }
        else if(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_LEFT.equals(key))
        {
            this.marginLeft = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_LEFT, 5);
            boolean marginsPresent = marginTop + marginBottom + marginLeft + marginRight > 0.001;
            this.marginInsets = marginsPresent ? new RectangleInsets(marginTop, marginLeft, marginBottom, marginRight) : null;
        }
        else if(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_RIGHT.equals(key))
        {
            this.marginRight = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_RIGHT, 5);
            boolean marginsPresent = marginTop + marginBottom + marginLeft + marginRight > 0.001;
            this.marginInsets = marginsPresent ? new RectangleInsets(marginTop, marginLeft, marginBottom, marginRight) : null;
        }
        else if(PreferredRoamingTitleLegendStyle.LEGEND_FRAME_PAINT.equals(key))
        {
            this.framePaint = (Paint) SerializationUtilities.getSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_FRAME_PAINT, Color.black);
        }
        else if(PreferredRoamingTitleLegendStyle.LEGEND_FRAME_STROKE.equals(key))
        {
            this.frameStroke = SerializationUtilities.getStroke(pref, PreferredRoamingTitleLegendStyle.LEGEND_FRAME_STROKE, new BasicStroke(1.f));

        }
    };
}
