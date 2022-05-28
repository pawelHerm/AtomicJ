package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.DECIMAL_SEPARATOR;
import static atomicJ.gui.PreferenceKeys.GROUPING_SEPARATOR;
import static atomicJ.gui.PreferenceKeys.GROUPING_USED;
import static atomicJ.gui.PreferenceKeys.MAX_FRACTION_DIGITS;
import static atomicJ.gui.PreferenceKeys.TRAILING_ZEROES;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;


public class PreferredNumericalFormatStyle implements PreferenceChangeListener
{
    private final Preferences pref;

    private int maxDigits;   
    private char decimalSeparator;
    private char groupingSeparator;
    private boolean showTrailingZeroes; 
    private boolean groupingUsed;

    private static Map<String, PreferredNumericalFormatStyle> instances = new LinkedHashMap<>();


    public PreferredNumericalFormatStyle(Preferences pref)
    {
        this.pref = pref;
        this.pref.addPreferenceChangeListener(this);

        pullDecimalFormatPreferences(pref);
    }


    public static PreferredNumericalFormatStyle getInstance(Preferences pref) 
    {
        String key = pref.absolutePath();
        PreferredNumericalFormatStyle style = instances.get(key);

        if(style == null)
        {
            style = new PreferredNumericalFormatStyle(pref);
            instances.put(key, style);
        }

        return style;    
    };

    public Preferences getPreferences() 
    {
        return pref;
    }

    private void pullDecimalFormatPreferences(Preferences pref)
    {
        this.groupingUsed = pref.getBoolean(GROUPING_USED, false);       
        this.showTrailingZeroes = pref.getBoolean(TRAILING_ZEROES, false);
        this.groupingSeparator = (char) pref.getInt(GROUPING_SEPARATOR, ' ');               
        this.decimalSeparator = (char) pref.getInt(DECIMAL_SEPARATOR, '.');             
        this.maxDigits = pref.getInt(MAX_FRACTION_DIGITS, 4);
    }

    public int getMaximumFractionDigits()
    {
        return maxDigits;
    }  

    public boolean isGroupingUsed()
    {
        return groupingUsed;
    }

    public char getGroupingSeparator()
    {
        return groupingSeparator;
    }

    public char getDecimalSeparator()
    {
        return decimalSeparator;
    }


    public boolean isShowTrailingZeroes()
    {
        return showTrailingZeroes;
    }


    @Override
    public void preferenceChange(PreferenceChangeEvent evt)
    {
        String key = evt.getKey();

        if(GROUPING_USED.equals(key))
        {
            this.groupingUsed = pref.getBoolean(GROUPING_USED, false);       
        }
        else if(TRAILING_ZEROES.equals(pref))
        {
            this.showTrailingZeroes = pref.getBoolean(TRAILING_ZEROES, false);
        }
        else if(GROUPING_SEPARATOR.equals(pref))
        {
            this.groupingSeparator = (char) pref.getInt(GROUPING_SEPARATOR, ' ');               
        }
        else if(DECIMAL_SEPARATOR.equals(pref))
        {
            this.decimalSeparator = (char) pref.getInt(DECIMAL_SEPARATOR, '.');             
        }
        else if(MAX_FRACTION_DIGITS.equals(pref))
        {
            this.maxDigits = pref.getInt(MAX_FRACTION_DIGITS, 4);
        }
    }
}