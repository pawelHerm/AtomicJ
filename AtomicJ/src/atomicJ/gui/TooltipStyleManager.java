package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.DECIMAL_SEPARATOR;
import static atomicJ.gui.PreferenceKeys.GROUPING_SEPARATOR;
import static atomicJ.gui.PreferenceKeys.GROUPING_USED;
import static atomicJ.gui.PreferenceKeys.TRAILING_ZEROES;
import static atomicJ.gui.PreferenceKeys.MAX_FRACTION_DIGITS;
import static atomicJ.gui.PreferenceKeys.VISIBLE;
import static atomicJ.gui.PreferenceKeys.PREFIX_VISIBLE;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.prefs.Preferences;

import atomicJ.data.units.UnitExpression;

public class TooltipStyleManager 
{
    private final DecimalFormat format = new DecimalFormat();
    private boolean showTrailingZeroes;

    private boolean visible;
    private boolean prefixVisible;

    private final String managerName;
    private final String tooltipPrefix;

    private final Preferences pref;

    public TooltipStyleManager(String name, String tooltipPrefix, Preferences pref, boolean defaultVisible)
    {
        this.managerName = name;
        this.tooltipPrefix = tooltipPrefix;
        this.pref = pref;

        setPreferredStyle(pref, defaultVisible);
    }

    private void setPreferredStyle(Preferences pref, boolean defaultVisible)
    {
        this.visible = pref.getBoolean(VISIBLE, defaultVisible);
        this.prefixVisible = pref.getBoolean(PREFIX_VISIBLE, true);

        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();

        char groupingSeparator = (char) pref.getInt(GROUPING_SEPARATOR, ' ');         
        char decimalSeparator = (char) pref.getInt(DECIMAL_SEPARATOR, '.');          
        boolean groupingUsed = pref.getBoolean(GROUPING_USED, false); 

        symbols.setGroupingSeparator(groupingSeparator);
        symbols.setDecimalSeparator(decimalSeparator);

        format.setDecimalFormatSymbols(symbols); 
        format.setGroupingUsed(groupingUsed);

        this.showTrailingZeroes = pref.getBoolean(TRAILING_ZEROES, true);
        int maxFractionDigits = pref.getInt(MAX_FRACTION_DIGITS, 3);

        format.setMaximumFractionDigits(maxFractionDigits);
        if(showTrailingZeroes)
        {
            format.setMinimumFractionDigits(maxFractionDigits);
        }    
    }

    public String getName()
    {
        return managerName;
    }

    public String getTooltip(UnitExpression expr)
    {
        if(!visible)
        {
            return "";
        }
        String prefix = prefixVisible ? tooltipPrefix : "";        
        return prefix + expr.toString(format) + " ";
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean visibleNew)
    {
        this.visible = visibleNew;
    }

    public boolean isPrefixVisible()
    {
        return prefixVisible;
    }

    public void setPrefixVisible(boolean prefixVisible)
    {
        this.prefixVisible = prefixVisible;
    }

    public boolean isTrailingZeroes()
    {
        return showTrailingZeroes;
    }

    public void setTickLabelShowTrailingZeroes(boolean trailingZeroes)
    {       
        this.showTrailingZeroes = trailingZeroes;
        int minDigits = trailingZeroes ? format.getMaximumFractionDigits() : 0;
        format.setMinimumFractionDigits(minDigits); 
    }

    public boolean isGroupingUsed()
    {
        return format.isGroupingUsed();
    }

    public void setGroupingUsed(boolean used)
    {
        format.setGroupingUsed(used);
    }

    public char getGroupingSeparator()
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        return symbols.getGroupingSeparator();
    }

    public void setGroupingSeparator(char separator)
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(separator);
        format.setDecimalFormatSymbols(symbols);  
    }

    public char getDecimalSeparator()
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        return symbols.getDecimalSeparator();
    }

    public void setDecimalSeparator(char separator)
    {       
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        symbols.setDecimalSeparator(separator);
        format.setDecimalFormatSymbols(symbols);
    }

    public int getMaximumFractionDigits()
    {
        return format.getMaximumFractionDigits();
    }

    public void setMaximumFractionDigits(int n)
    {        
        format.setMaximumFractionDigits(n);
        if(showTrailingZeroes)
        {
            format.setMinimumFractionDigits(n);
        }    
    }

    public Preferences getPreferences()
    {
        return pref;
    }
}
