package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;


import org.jfree.chart.title.TextTitle;
import org.jfree.util.ObjectUtilities;

import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;

public class RoamingStandardStackTextTitle extends RoamingStandardTextTitle implements RoamingStackTextTitle
{
    private boolean updateFrameTitle = true;

    private final DecimalFormat format;
    private boolean showTrailingZeroes;

    private double currentValue = 0;

    private double prefixScalingFactor;
    private PrefixedUnit dataUnit = SimplePrefixedUnit.getNullInstance();
    private PrefixedUnit displayedUnit;

    public RoamingStandardStackTextTitle(TextTitle outsideTitle, String key, Preferences pref)
    {
        super(outsideTitle, key, pref);

        this.format = new DecimalFormat();

        setPreferredStyle(key, pref);
    }

    public RoamingStandardStackTextTitle(RoamingStandardStackTextTitle that)
    {
        super(that);

        this.updateFrameTitle = that.updateFrameTitle;
        this.format = (DecimalFormat) that.format.clone();
        this.showTrailingZeroes = that.showTrailingZeroes;
        this.currentValue = that.currentValue;
        this.prefixScalingFactor = that.prefixScalingFactor;
        this.dataUnit = that.dataUnit;
        this.displayedUnit = that.displayedUnit;

        fireRoamingTitleChanged();
    }

    private void setPreferredStyle(String key, Preferences pref)
    {
        char groupingSeparator = (char) pref.getInt(TITLE_STACK_GROUPING_SEPARATOR, ' ');            
        char decimalSeparator = (char) pref.getInt(TITLE_STACK_DECIMAL_SEPARATOR, '.');          
        this.showTrailingZeroes = pref.getBoolean(TITLE_STACK_TRAILING_ZEROES, true);
        boolean groupingUsed = pref.getBoolean(TITLE_STACK_GROUPING_USED, false);    

        int maxDigit = pref.getInt(TITLE_STACK_MAX_FRACTION_DIGITS, 3);
        int minDigits = showTrailingZeroes ? maxDigit : 0;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

        symbols.setDecimalSeparator(decimalSeparator);
        symbols.setGroupingSeparator(groupingSeparator);
        format.setGroupingUsed(groupingUsed);
        format.setMaximumFractionDigits(maxDigit);
        format.setMinimumFractionDigits(minDigits);

        format.setDecimalFormatSymbols(symbols);

        fireRoamingTitleChanged();  
    }

    @Override
    public RoamingTextTitle copy()
    {
        return new RoamingStandardStackTextTitle(this);
    }

    public PrefixedUnit getDataUnit()
    {
        return dataUnit;
    }

    public double getUnitScaling()
    {
        return prefixScalingFactor;
    }

    public List<PrefixedUnit> getProposedUnits()
    {
        List<PrefixedUnit> proposedUnits = new ArrayList<>();

        PrefixedUnit nextUnit = dataUnit.getNext();
        PrefixedUnit previousUnit = dataUnit.getPrevious(); 

        proposedUnits.add(nextUnit);
        proposedUnits.add(dataUnit);
        proposedUnits.add(previousUnit);

        return proposedUnits;
    }

    public PrefixedUnit getDisplayedUnit()
    {
        return displayedUnit;
    }

    public void setDisplayedUnit(PrefixedUnit displayedUnitNew)
    {
        if(displayedUnitNew == null)
        {
            return;
        }

        String newUnitName = displayedUnitNew.getFullName();
        String oldUnitName = this.displayedUnit.getFullName();

        if(!ObjectUtilities.equal(oldUnitName, newUnitName))
        {
            this.displayedUnit = displayedUnitNew;            
            this.prefixScalingFactor = dataUnit.getConversionFactorTo(displayedUnitNew);

            updateFormattedText();

            fireRoamingTitleChanged();
        }      
    }

    @Override
    public void setFrameTitleText(double currentValue, PrefixedUnit unit)
    {
        this.dataUnit = unit;
        if(this.displayedUnit == null)
        {
            this.displayedUnit = unit;
        }

        this.prefixScalingFactor = unit.getConversionFactorTo(displayedUnit);
        this.currentValue = currentValue;

        if(updateFrameTitle)
        {
            String frameTitleText = format.format(prefixScalingFactor*currentValue) + " " + displayedUnit.getFullName();
            setText(frameTitleText);
        }
    }

    private void updateFormattedText()
    {
        if(updateFrameTitle)
        {
            String frameTitleText = format.format(prefixScalingFactor*currentValue) + " " + displayedUnit.getFullName();
            setText(frameTitleText);
        }    
    }

    @Override
    public boolean isUpdateFrameTitle()
    {
        return updateFrameTitle;
    }

    @Override
    public void setUpdateFrameTitle(boolean updateFrameTitle)
    {
        this.updateFrameTitle = updateFrameTitle;
    }

    @Override
    public boolean isTickLabelTrailingZeroes()
    {
        return showTrailingZeroes;
    }

    @Override
    public void setTickLabelShowTrailingZeroes(boolean trailingZeroes)
    {       
        this.showTrailingZeroes = trailingZeroes;
        int minDigits = trailingZeroes ? format.getMaximumFractionDigits() : 0;
        format.setMinimumFractionDigits(minDigits); 

        updateFormattedText();
        fireRoamingTitleChanged();
    }

    @Override
    public boolean isTickLabelGroupingUsed()
    {
        return format.isGroupingUsed();
    }

    @Override
    public void setTickLabelGroupingUsed(boolean used)
    {
        format.setGroupingUsed(used);

        updateFormattedText();
        fireRoamingTitleChanged();
    }

    @Override
    public char getTickLabelGroupingSeparator()
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        return symbols.getGroupingSeparator();
    }

    @Override
    public void setTickLabelGroupingSeparator(char separator)
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(separator);
        format.setDecimalFormatSymbols(symbols); 

        updateFormattedText();
        fireRoamingTitleChanged();
    }

    @Override
    public char getTickLabelDecimalSeparator()
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        return symbols.getDecimalSeparator();
    }

    @Override
    public void setTickLabelDecimalSeparator(char separator)
    {       
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        symbols.setDecimalSeparator(separator);
        format.setDecimalFormatSymbols(symbols);

        updateFormattedText();
        fireRoamingTitleChanged();
    }

    @Override
    public int getMaximumFractionDigits()
    {
        return format.getMaximumFractionDigits();
    }

    @Override
    public void setMaximumFractionDigits(int n)
    {
        format.setMaximumFractionDigits(n);

        if(showTrailingZeroes)
        {
            format.setMinimumFractionDigits(n);
        }   

        updateFormattedText();
        fireRoamingTitleChanged();
    }
}
