package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.DECIMAL_SEPARATOR;
import static atomicJ.gui.PreferenceKeys.GROUPING_SEPARATOR;
import static atomicJ.gui.PreferenceKeys.GROUPING_USED;
import static atomicJ.gui.PreferenceKeys.MAX_FRACTION_DIGITS;
import static atomicJ.gui.PreferenceKeys.TRAILING_ZEROES;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;


public class StandardNumericalFormatStyle implements NumericalFormatStyle
{
    private final PreferredNumericalFormatStyle prefStyle;
    private final DecimalFormat format = new DecimalFormat();

    private final List<FormattableNumericalDataListener> listeners = new ArrayList<>();

    private boolean showTrailingZeroes; 

    public StandardNumericalFormatStyle()
    {
        this(null);
    }

    public StandardNumericalFormatStyle(Preferences pref)
    {
        this.prefStyle = pref != null ? PreferredNumericalFormatStyle.getInstance(pref) : null;
        pullDecimalFormatPreferences(prefStyle);
    }

    private void pullDecimalFormatPreferences(PreferredNumericalFormatStyle prefStyle)
    {
        if(prefStyle != null)
        {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

            boolean groupingUsed = prefStyle.isGroupingUsed();       
            showTrailingZeroes = prefStyle.isShowTrailingZeroes();
            char groupingSeparator = prefStyle.getGroupingSeparator();               
            char decimalSeparator = prefStyle.getDecimalSeparator();             
            int maxDigit = prefStyle.getMaximumFractionDigits();
            int minDigit = showTrailingZeroes ? maxDigit : 0;

            symbols.setDecimalSeparator(decimalSeparator);
            symbols.setGroupingSeparator(groupingSeparator);

            format.setMaximumFractionDigits(maxDigit);
            format.setMinimumFractionDigits(minDigit);
            format.setGroupingUsed(groupingUsed);

            format.setDecimalFormatSymbols(symbols);
        }     
    }

    @Override
    public void saveToPreferences()
    {
        if(prefStyle != null)
        {
            Preferences pref = prefStyle.getPreferences();

            int maxDigits = getMaximumFractionDigits();        
            boolean separateThousands = isGroupingUsed();
            boolean trailingZeroes = isShowTrailingZeroes();
            char decimalSeparator = getDecimalSeparator();
            char thousandSeparator = getGroupingSeparator();

            pref.putInt(MAX_FRACTION_DIGITS, maxDigits);
            pref.putInt(DECIMAL_SEPARATOR, decimalSeparator);
            pref.putInt(GROUPING_SEPARATOR, thousandSeparator);

            pref.putBoolean(GROUPING_USED, separateThousands);
            pref.putBoolean(TRAILING_ZEROES, trailingZeroes);
        }       
    }

    @Override
    public int getMaximumFractionDigits()
    {
        return format.getMaximumFractionDigits();
    }  

    @Override
    public void setMaximumFractionDigits(int maximumFractionDigitsNew)
    {
        int maximumFractionDigitsOld = format.getMaximumFractionDigits();

        format.setMaximumFractionDigits(maximumFractionDigitsNew);
        if(showTrailingZeroes)
        {
            format.setMinimumFractionDigits(maximumFractionDigitsNew);
        }  

        if(maximumFractionDigitsOld != maximumFractionDigitsNew)
        {
            for(FormattableNumericalDataListener l : listeners) 
            {
                l.maximumFractionDigitsChanged(maximumFractionDigitsNew);
            }
        }
    }

    @Override
    public boolean isGroupingUsed()
    {
        return format.isGroupingUsed();
    }

    @Override
    public void setGroupingUsed(boolean groupingUsedNew)
    {
        boolean groupingUsedOld = format.isGroupingUsed();
        format.setGroupingUsed(groupingUsedNew);

        if(groupingUsedOld != groupingUsedNew)
        {
            for(FormattableNumericalDataListener l : listeners) 
            {
                l.groupingUsedChanged(groupingUsedNew);
            }
        }
    }

    @Override
    public char getGroupingSeparator()
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();

        return symbols.getGroupingSeparator();
    }

    @Override
    public void setGroupingSeparator(char separatorNew)
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();

        char separatorOld = symbols.getGroupingSeparator();
        symbols.setGroupingSeparator(separatorNew);
        format.setDecimalFormatSymbols(symbols);

        if(separatorOld != separatorNew)
        {
            for(FormattableNumericalDataListener l : listeners) 
            {
                l.groupingSeparatorChanged(separatorNew);
            }
        }
    }

    @Override
    public char getDecimalSeparator()
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        return symbols.getDecimalSeparator();
    }

    @Override
    public void setDecimalSeparator(char separatorNew)
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();

        char separatorOld = symbols.getDecimalSeparator();
        symbols.setDecimalSeparator(separatorNew);

        format.setDecimalFormatSymbols(symbols);

        if(separatorOld != separatorNew)
        {
            for(FormattableNumericalDataListener l : listeners) 
            {
                l.decimalSeparatorChanged(separatorNew);
            }
        }
    }

    @Override
    public boolean isShowTrailingZeroes()
    {
        return showTrailingZeroes;
    }

    @Override
    public void setShowTrailingZeroes(boolean trailingZeroesNew)
    {
        boolean showTrailingZeroesOld = this.showTrailingZeroes;
        this.showTrailingZeroes = trailingZeroesNew;

        int minDigits = trailingZeroesNew ? format.getMaximumFractionDigits() : 0;
        format.setMinimumFractionDigits(minDigits);

        if(showTrailingZeroesOld != trailingZeroesNew){
            for(FormattableNumericalDataListener l : listeners) 
            {
                l.showTrailingZeroesChanged(this.showTrailingZeroes);
            }
        }     
    }

    @Override
    public FormattableNumericalDataState getState()
    {
        return new FormattableNumericalDataState(getMaximumFractionDigits(),
                showTrailingZeroes, isGroupingUsed(), getGroupingSeparator(), getDecimalSeparator());
    }

    @Override
    public void setState(FormattableNumericalDataState memento)
    {
        setMaximumFractionDigits(memento.getMaximumFractionDigits());
        setShowTrailingZeroes(memento.isShowTrailingZeroes());
        setGroupingUsed(memento.isGroupingUsed());
        setGroupingSeparator(memento.getGroupingSeparator());
        setDecimalSeparator(memento.getDecimalSeparator());
    }

    @Override
    public DecimalFormat getDecimalFormat()
    {
        return format;
    }


    @Override
    public void addListener(FormattableNumericalDataListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(FormattableNumericalDataListener listener) {
        listeners.remove(listener);
    }

    public static class FormattableNumericalDataState
    {
        private final boolean showTrailingZeroes;
        private final int maxDigits;
        private final boolean useGroupingSeparator;
        private final char groupingSeparator;
        private final char decimalSeparator;

        public FormattableNumericalDataState(int maxDigits, boolean showTrailingZeroes, boolean groupingUsed, char groupingSeparator, char decimalSeparator)
        {
            this.maxDigits = maxDigits;
            this.showTrailingZeroes = showTrailingZeroes;
            this.useGroupingSeparator = groupingUsed;
            this.groupingSeparator = groupingSeparator;
            this.decimalSeparator = decimalSeparator;
        }

        public int getMaximumFractionDigits()
        {
            return maxDigits;
        }

        public boolean isShowTrailingZeroes()
        {
            return showTrailingZeroes;
        }

        public boolean isGroupingUsed()
        {
            return useGroupingSeparator;
        }

        public char getGroupingSeparator()
        {
            return groupingSeparator;
        }

        public char getDecimalSeparator()
        {
            return decimalSeparator;
        }
    }
}