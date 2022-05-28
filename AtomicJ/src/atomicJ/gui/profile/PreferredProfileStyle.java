package atomicJ.gui.profile;


import java.awt.Paint;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import atomicJ.gui.PreferredAnnotationStyle;
import atomicJ.utilities.MetaMap;

public class PreferredProfileStyle extends PreferredAnnotationStyle 
{
    public static final String PROFILE_KNOB_VISIBLE = "ProfileKnobVisible";
    public static final String PROFILE_KNOB_ORIENTATION = "ProfileKnobOrientation";

    public static final String PROFILE_KNOB_WIDTH = "ProfileKnobWidth";
    public static final String PROFILE_KNOB_HEIGHT = "ProfileKnobHeight";

    public static final String PROFILE_ARROWHEAD_VISIBLE_UNFINISHED_STANDARD = "ProfileArrowheadVisibleUnfinishedStandard";
    public static final String PROFILE_ARROWHEAD_VISIBLE_UNFINISHED_HIGHLIGHTED = "ProfileArrowheadVisibleUnfinishedHighlighted";
    public static final String PROFILE_ARROWHEAD_VISIBLE_FINISHED_STANDARD = "ProfileArrowheadVisibleFinishedStandard";
    public static final String PROFILE_ARROWHEAD_VISIBLE_FINISHED_HIGHLIGHTED = "ProfileArrowheadVisibleFinishedHighlighted";

    public static final String PROFILE_ARROWHEAD_LENGTH_UNFINISHED_STANDARD = "ProfileArrowheadLengthUnfinishedStandard";
    public static final String PROFILE_ARROWHEAD_LENGTH_UNFINISHED_HIGHLIGHTED = "ProfileArrowheadLengthUnfinishedHighlighted";
    public static final String PROFILE_ARROWHEAD_LENGTH_FINISHED_STANDARD = "ProfileArrowheadLengthFinishedStandard";
    public static final String PROFILE_ARROWHEAD_LENGTH_FINISHED_HIGHLIGHTED = "ProfileArrowheadLengthFinishedHighlighted";

    public static final String PROFILE_ARROWHEAD_WIDTH_UNFINISHED_STANDARD = "ProfileArrowheadHeightUnfinishedStandard";
    public static final String PROFILE_ARROWHEAD_WIDTH_UNFINISHED_HIGHLIGHTED = "ProfileArrowheadHeightUnfinishedHighlighted";
    public static final String PROFILE_ARROWHEAD_WIDTH_FINISHED_STANDARD = "ProfileArrowheadHeightFinishedStandard";
    public static final String PROFILE_ARROWHEAD_WIDTH_FINISHED_HIGHLIGHTED = "ProfileArrowheadHeightFinishedHighlighted";

    private static final float DEFAULT_LABEL_OFFSET = 15.0f;
    private static final float DEFAULT_LENGTHWISE_LABEL_POSITION = 0.1f;

    private boolean knobVisible;
    private KnobOrientation knobOrientation;
    private int knobWidth;
    private int knobHeight;

    private boolean isArrowheadVisibleUnfinishedStandard;
    private boolean isArrowheadVisibleUnfinishedHighlighted;
    private boolean isArrowheadVisibleFinishedStandard;
    private boolean isArrowheadVisibleFinishedHighlighted;

    private float arrowheadLengthUnfinishedStandard;
    private float arrowheadLengthUnfinishedHighlighted;
    private float arrowheadLengthFinishedStandard;
    private float arrowheadLengthFinishedHighlighted;

    private float arrowheadWidthUnfinishedStandard;
    private float arrowheadWidthUnfinishedHighlighted;
    private float arrowheadWidthFinishedStandard;
    private float arrowheadWidthFinishedHighlighted;

    private static MetaMap<String, Paint, PreferredProfileStyle> instances = new MetaMap<>();

    protected PreferredProfileStyle(Preferences pref, Paint defaultPaint)
    {
        super(pref, defaultPaint, DEFAULT_LABEL_OFFSET, DEFAULT_LENGTHWISE_LABEL_POSITION);

        setDefaultProfileStyle();
    }


    public static PreferredProfileStyle getInstance(Preferences pref, Paint defaultPaint) 
    {
        String key = pref.absolutePath();
        PreferredProfileStyle style = instances.get(key, defaultPaint);

        if(style == null)
        {
            style = new PreferredProfileStyle(pref, defaultPaint);
            instances.put(key, defaultPaint, style);
        }

        return style;    
    };


    private void setDefaultProfileStyle()
    {
        Preferences pref = getPreferences();

        int defaultKnobWidth = 12;
        int defaultKnobHeight = 25;

        boolean defaultIsArrowheadVisibleUnfinished = true;
        boolean defaultIsArrowheadVisibleFinished = true;

        float defaultArrowheadLengthUnfinished = 9.0f;
        float defaultArrowheadLengthFinished= 9.0f;

        float defaultArrowheadWidthUnfinished = 8.0f;
        float defaultArrowheadWidthFinished = 8.0f;

        this.isArrowheadVisibleUnfinishedStandard = pref.getBoolean(PROFILE_ARROWHEAD_VISIBLE_UNFINISHED_STANDARD, defaultIsArrowheadVisibleUnfinished);

        this.knobVisible = pref.getBoolean(PROFILE_KNOB_VISIBLE, true);
        this.knobOrientation =  KnobOrientation.valueOf(pref.get(PROFILE_KNOB_ORIENTATION, KnobOrientation.UP.name()));

        this.knobWidth = pref.getInt(PROFILE_KNOB_WIDTH, defaultKnobWidth);
        this.knobHeight = pref.getInt(PROFILE_KNOB_HEIGHT, defaultKnobHeight);

        this.isArrowheadVisibleUnfinishedHighlighted = pref.getBoolean(PROFILE_ARROWHEAD_VISIBLE_UNFINISHED_HIGHLIGHTED, defaultIsArrowheadVisibleUnfinished);
        this.isArrowheadVisibleFinishedStandard = pref.getBoolean(PROFILE_ARROWHEAD_VISIBLE_FINISHED_STANDARD, defaultIsArrowheadVisibleFinished);
        this.isArrowheadVisibleFinishedHighlighted = pref.getBoolean(PROFILE_ARROWHEAD_VISIBLE_FINISHED_HIGHLIGHTED, defaultIsArrowheadVisibleFinished);

        this.arrowheadLengthUnfinishedStandard = pref.getFloat(PROFILE_ARROWHEAD_LENGTH_UNFINISHED_STANDARD, defaultArrowheadLengthUnfinished);
        this.arrowheadLengthUnfinishedHighlighted = pref.getFloat(PROFILE_ARROWHEAD_LENGTH_UNFINISHED_HIGHLIGHTED, defaultArrowheadLengthUnfinished);
        this.arrowheadLengthFinishedStandard = pref.getFloat(PROFILE_ARROWHEAD_LENGTH_FINISHED_STANDARD, defaultArrowheadLengthFinished);
        this.arrowheadLengthFinishedHighlighted = pref.getFloat(PROFILE_ARROWHEAD_LENGTH_FINISHED_HIGHLIGHTED, defaultArrowheadLengthFinished);

        this.arrowheadWidthUnfinishedStandard = pref.getFloat(PROFILE_ARROWHEAD_WIDTH_UNFINISHED_STANDARD, defaultArrowheadWidthUnfinished);
        this.arrowheadWidthUnfinishedHighlighted = pref.getFloat(PROFILE_ARROWHEAD_WIDTH_UNFINISHED_HIGHLIGHTED, defaultArrowheadWidthUnfinished);
        this.arrowheadWidthFinishedStandard = pref.getFloat(PROFILE_ARROWHEAD_WIDTH_FINISHED_STANDARD, defaultArrowheadWidthFinished);
        this.arrowheadWidthFinishedHighlighted = pref.getFloat(PROFILE_ARROWHEAD_WIDTH_FINISHED_HIGHLIGHTED, defaultArrowheadWidthFinished);
    }

    public boolean isKnobVisible()
    {
        return knobVisible;
    }

    public KnobOrientation getKnobOrientation()
    {
        return knobOrientation;
    }

    public int getKnobWidth()
    {
        return knobWidth;
    }

    public int getKnobHeight()
    {
        return knobHeight;
    }

    public float getArrowheadLengthUnfinishedStandard()
    {
        return arrowheadLengthUnfinishedStandard;
    }

    public float getArrowheadLengthUnfinishedHighlighted()
    {
        return arrowheadLengthUnfinishedHighlighted;
    }

    public float getArrowheadLengthFinishedStandard()
    {
        return arrowheadLengthFinishedStandard;
    }

    public float getArrowheadLengthFinishedHighlighted()
    {
        return arrowheadLengthFinishedHighlighted;
    }


    public float getArrowheadWidthUnfinishedStandard()
    {
        return arrowheadWidthUnfinishedStandard;
    }

    public float getArrowheadWidthUnfinishedHighlighted()
    {
        return arrowheadWidthUnfinishedHighlighted;
    }

    public float getArrowheadWidthFinishedStandard()
    {
        return arrowheadWidthFinishedStandard;
    }

    public float getArrowheadWidthFinishedHighlighted()
    {
        return arrowheadWidthFinishedHighlighted;
    }

    public boolean isArrowheadVisibleUnfinishedStandard()
    {
        return isArrowheadVisibleUnfinishedStandard;
    }

    public boolean isArrowheadVisibleUnfinishedHighlighted()
    {
        return isArrowheadVisibleUnfinishedHighlighted;
    }

    public boolean isArrowheadVisibleFinishedStandard()
    {
        return isArrowheadVisibleFinishedStandard;
    }

    public boolean isArrowheadVisibleFinishedHighlighted()
    {
        return isArrowheadVisibleFinishedHighlighted;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) 
    {
        super.preferenceChange(evt);

        Preferences pref = getPreferences();

        String key = evt.getKey();

        if(PROFILE_KNOB_VISIBLE.equals(key))
        {
            this.knobVisible = pref.getBoolean(PROFILE_KNOB_VISIBLE, true);
        }

        else if(PROFILE_KNOB_ORIENTATION.equals(key))
        {
            this.knobOrientation =  KnobOrientation.valueOf(pref.get(PROFILE_KNOB_ORIENTATION, KnobOrientation.UP.name()));
        }

        else if(PROFILE_KNOB_WIDTH.equals(key))
        {
            int defaultKnobWidth = 12;
            this.knobWidth = pref.getInt(PROFILE_KNOB_WIDTH, defaultKnobWidth);
        }

        else if(PROFILE_KNOB_HEIGHT.equals(key))
        {
            int defaultKnobHeight = 25;
            this.knobHeight = pref.getInt(PROFILE_KNOB_HEIGHT, defaultKnobHeight);
        }

        else if(PROFILE_ARROWHEAD_VISIBLE_UNFINISHED_STANDARD.equals(key))
        {
            boolean defaultIsArrowheadVisibleUnfinished = true;
            this.isArrowheadVisibleUnfinishedStandard = pref.getBoolean(PROFILE_ARROWHEAD_VISIBLE_UNFINISHED_STANDARD, defaultIsArrowheadVisibleUnfinished);
        }

        else if(PROFILE_ARROWHEAD_VISIBLE_UNFINISHED_HIGHLIGHTED.equals(key))
        {
            boolean defaultIsArrowheadVisibleUnfinished = true;
            this.isArrowheadVisibleUnfinishedHighlighted = pref.getBoolean(PROFILE_ARROWHEAD_VISIBLE_UNFINISHED_HIGHLIGHTED, defaultIsArrowheadVisibleUnfinished);
        }

        else if(PROFILE_ARROWHEAD_VISIBLE_FINISHED_STANDARD.equals(key))
        {
            boolean defaultIsArrowheadVisibleFinished = true;
            this.isArrowheadVisibleFinishedStandard = pref.getBoolean(PROFILE_ARROWHEAD_VISIBLE_FINISHED_STANDARD, defaultIsArrowheadVisibleFinished);
        }

        else if(PROFILE_ARROWHEAD_VISIBLE_FINISHED_HIGHLIGHTED.equals(key))
        {
            boolean defaultIsArrowheadVisibleFinished = true;
            this.isArrowheadVisibleFinishedHighlighted = pref.getBoolean(PROFILE_ARROWHEAD_VISIBLE_FINISHED_HIGHLIGHTED, defaultIsArrowheadVisibleFinished);
        }

        else if(PROFILE_ARROWHEAD_LENGTH_UNFINISHED_STANDARD.equals(key))
        {
            float defaultArrowheadLengthUnfinished = 9.0f;
            this.arrowheadLengthUnfinishedStandard = pref.getFloat(PROFILE_ARROWHEAD_LENGTH_UNFINISHED_STANDARD, defaultArrowheadLengthUnfinished);
        }

        else if(PROFILE_ARROWHEAD_LENGTH_UNFINISHED_HIGHLIGHTED.equals(key))
        {
            float defaultArrowheadLengthUnfinished = 9.0f;
            this.arrowheadLengthUnfinishedHighlighted = pref.getFloat(PROFILE_ARROWHEAD_LENGTH_UNFINISHED_HIGHLIGHTED, defaultArrowheadLengthUnfinished);
        }

        else if(PROFILE_ARROWHEAD_LENGTH_FINISHED_STANDARD.equals(key))
        {
            float defaultArrowheadLengthFinished= 9.0f;
            this.arrowheadLengthFinishedStandard = pref.getFloat(PROFILE_ARROWHEAD_LENGTH_FINISHED_STANDARD, defaultArrowheadLengthFinished);
        }

        else if(PROFILE_ARROWHEAD_LENGTH_FINISHED_HIGHLIGHTED.equals(key))
        {
            float defaultArrowheadLengthFinished= 9.0f;
            this.arrowheadLengthFinishedHighlighted = pref.getFloat(PROFILE_ARROWHEAD_LENGTH_FINISHED_HIGHLIGHTED, defaultArrowheadLengthFinished);
        }

        else if(PROFILE_ARROWHEAD_WIDTH_UNFINISHED_STANDARD.equals(key))
        {
            float defaultArrowheadWidthUnfinished = 8.0f;
            this.arrowheadWidthUnfinishedStandard = pref.getFloat(PROFILE_ARROWHEAD_WIDTH_UNFINISHED_STANDARD, defaultArrowheadWidthUnfinished);
        }

        else if(PROFILE_ARROWHEAD_WIDTH_UNFINISHED_HIGHLIGHTED.equals(key))
        {
            float defaultArrowheadWidthUnfinished = 8.0f;
            this.arrowheadWidthUnfinishedHighlighted = pref.getFloat(PROFILE_ARROWHEAD_WIDTH_UNFINISHED_HIGHLIGHTED, defaultArrowheadWidthUnfinished);
        }

        else if(PROFILE_ARROWHEAD_WIDTH_FINISHED_STANDARD.equals(key))
        {
            float defaultArrowheadWidthFinished = 8.0f;
            this.arrowheadWidthFinishedStandard = pref.getFloat(PROFILE_ARROWHEAD_WIDTH_FINISHED_STANDARD, defaultArrowheadWidthFinished);
        }

        else if(PROFILE_ARROWHEAD_WIDTH_FINISHED_HIGHLIGHTED.equals(key))
        {
            float defaultArrowheadWidthFinished = 8.0f;
            this.arrowheadWidthFinishedHighlighted = pref.getFloat(PROFILE_ARROWHEAD_WIDTH_FINISHED_HIGHLIGHTED, defaultArrowheadWidthFinished);
        }
    }
}
