package atomicJ.data.units;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UnitUtilities 
{
    //we have to take into account that units can be logarithmized
    private static final Pattern UNITS_PATTERN = Pattern.compile( "\\s*+(G|M|k|h|da|d|c|m|Milli|\uFFFD|µ|~|u|Micro|n|Nano|p|f)?\\s*+(m|Meter|g|s|Second|sec|A|Ampere|Hz|N|Newton|Pa|Pascal|log\\(Pa\\)|J|eV|W|C|V|Volt|F|deg|rad|Arb)\\s*+");

    private static String SOLIDUS = "/";

    public static PrefixedUnit getDefaultUnit(PrefixedUnit unitNew)
    {
        if(Units.NANO_NEWTON_UNIT.isCompatible(unitNew))
        {
            return Units.NANO_NEWTON_UNIT;
        }
        else if(Units.MICRO_METER_UNIT.isCompatible(unitNew))
        {
            return Units.MICRO_METER_UNIT;
        }
        return unitNew;
    }

    public static PrefixedUnit getPreferredPrefix(double[] values, PrefixedUnit oldUnit)
    {
        if(values.length == 0)
        {
            return oldUnit;
        }

        List<PrefixedUnit> allUnits = new ArrayList<>();
        for(double val : values)
        {
            allUnits.add(oldUnit.getPreferredCompatibleUnit(val));
        }

        Collections.sort(allUnits, new Comparator<PrefixedUnit>() {

            @Override
            public int compare(PrefixedUnit u1, PrefixedUnit u2) 
            {
                return Double.compare(u1.getTotalPrefixExponent(), u2.getTotalPrefixExponent());
            }
        });

        return allUnits.get(allUnits.size()/2);
    }

    public static SIPrefix getPrefixValue(String prefixSIName)
    {
        SIPrefix prefix = SIPrefix.getPrefix(prefixSIName);
        return prefix;
    }

    public static SIPrefix getPrefixValueForUnit(String unit)
    {
        String[] elements = unit.split(SOLIDUS);
        int length = elements.length;

        String numeratorPrefixString = "";
        String denomiatorPrefixString = "";

        if(length >= 1)
        {
            String numerator = elements[0];
            numeratorPrefixString = numerator.length()>1 ? numerator.substring(0, 1) : "";
        }
        if(length >= 2)
        {

            String denominator = elements[1];
            denomiatorPrefixString = denominator.length()>1 ? denominator.substring(0, 1) : "";
        }

        SIPrefix numeratorPrefix = SIPrefix.getPrefix(numeratorPrefixString);
        SIPrefix enominatorPrefix = SIPrefix.getPrefix(denomiatorPrefixString);

        return numeratorPrefix.divide(enominatorPrefix);
    }

    public static PrefixedUnit getSIUnit(String stringRepresentation)
    {
        String[] tokens = stringRepresentation.split(SOLIDUS);
        int length = tokens.length;

        String numeratorName = tokens[0];

        SimplePrefixedUnit numeratorUnit = getSISimpleUnit(numeratorName);

        boolean isRational = length >= 2;

        if(isRational)
        {            
            String denominatorName = tokens[1];
            SimplePrefixedUnit denominatorUnit = getSISimpleUnit(denominatorName);

            PrefixedUnit unit = new RationalPrefixedUnit(numeratorUnit, denominatorUnit);

            return unit;
        }
        else
        {
            return numeratorUnit;
        }        
    }

    public static SimplePrefixedUnit getSISimpleUnit(String stringRepresentation)
    {        
        Matcher matcher = UNITS_PATTERN.matcher(stringRepresentation);

        boolean matches = matcher.matches();

        //we are unable to find what is the prefix and unitName, so we will readEverything as unit name

        String firstMatch = matches ? matcher.group(1) : null;

        String prefixString = (firstMatch != null) ? firstMatch : "";
        String unitString = matches ? BasicUnit.convertToSymbolIfNecessary(matcher.group(2)): BasicUnit.convertToSymbolIfNecessary(stringRepresentation);

        SIPrefix prefix =  SIPrefix.getPrefix(prefixString);

        SimplePrefixedUnit unit = new SimplePrefixedUnit(unitString, prefix);

        return unit;
    }

    public static String toString(double[][] data, NumberFormat format, PrefixedUnit unit) 
    {
        StringBuilder builder = new StringBuilder();

        int count = data.length;
        builder.append("{");
        for(int j = 0;j<count;j++)
        {
            double[] row = data[j];
            builder.append(UnitUtilities.toString(row, format, unit) + "\\n");                       
        }
        builder.append("}");  

        return builder.toString();
    }

    public static String toString(double[] data, NumberFormat format, PrefixedUnit unit) 
    {
        StringBuilder builder = new StringBuilder();

        int count = data.length;
        builder.append("{");
        for(int j = 0;j<count;j++)
        {
            builder.append(format.format(data[j]) + " " + unit.getFullName());
            if(j<count - 1)
            {
                builder.append(",");
            }               
        }
        builder.append("}");  

        return builder.toString();
    }

    public static List<PrefixedUnit> buildUnitList(String unitSymbol, int exp, SIPrefix minPrefix, SIPrefix maxPrefix)
    {
        List<PrefixedUnit> units = new ArrayList<>();

        for(SIPrefix prefix : SIPrefix.getRange(minPrefix, maxPrefix))
        {
            units.add(new SimplePrefixedUnit(unitSymbol, prefix, exp));
        }

        return Collections.unmodifiableList(units);
    }
}
