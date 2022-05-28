package atomicJ.data.units;

import java.util.Arrays;
import java.util.List;

import atomicJ.utilities.MathUtilities;

public enum SIPrefix implements Comparable<SIPrefix>
{        
    f("f",-15), p("p",-12), n("n",-9), u("μ", new String[] {"\uFFFD","u","μ","\u00B5","~","Micro"}, -6), m("m",-3), c("c", -2), d("d", -1),Empty("", 0),
    da("da", 1), h("h", 2), k("k",3), M("M",6), G("G",9), T("T", 12);

    public static int LOWEST_EXPONENT = -15;
    public static int HIGHEST_EXPONENT = 12;

    private final int exponent;
    private final String key;
    private final String[] keys;

    SIPrefix(String key, int exponent)
    {
        this(key, new String[] {key}, exponent);
    }

    SIPrefix(String key, String[] keys, int exponent)
    {
        this.key = key;
        this.keys = keys;
        this.exponent = exponent; 
    }

    public static SIPrefix getSmallestPrefix()
    {
        return SIPrefix.f;
    }

    public static SIPrefix getGratestPrefix()
    {
        return SIPrefix.T;
    }

    public static List<SIPrefix> getRange(SIPrefix minPrefix, SIPrefix maxPrefix)
    {
        int minIndex = minPrefix.ordinal();
        int maxIndex = maxPrefix.ordinal();

        return Arrays.asList(Arrays.copyOfRange(values(), minIndex, maxIndex + 1));
    }

    public static List<SIPrefix> getRangeBeginningAt(SIPrefix minPrefix)
    {
        int minIndex = minPrefix.ordinal();
        int maxIndex = getGratestPrefix().ordinal();

        return Arrays.asList(Arrays.copyOfRange(values(), minIndex, maxIndex + 1));
    }

    public static List<SIPrefix> getRangeEndingAt(SIPrefix maxPrefix)
    {
        int minIndex = getSmallestPrefix().ordinal();
        int maxIndex = maxPrefix.ordinal();

        return Arrays.asList(Arrays.copyOfRange(values(), minIndex, maxIndex + 1));
    }

    public int compareExponents(SIPrefix other)
    {
        return Integer.compare(this.exponent, other.exponent);
    }

    public String getEquationForm()
    {
        return key;
    }

    public int getExponent()
    {
        return exponent;
    }

    public boolean isExponentMultipleOf3()
    {
        boolean multipleOfThree = (exponent % 3) == 0;

        return multipleOfThree;
    }

    public double getConversion()
    {
        double conversionFactor = MathUtilities.intPow(10, exponent);

        return conversionFactor;
    }

    public SIPrefix multiply(SIPrefix otherPrefix)
    {
        int exponentNew = this.exponent + otherPrefix.exponent;

        SIPrefix prefixNew = getPrefix(exponentNew);

        return prefixNew;
    }   

    public static SIPrefix multiply(SIPrefix firstPrefix, int firstExponent, SIPrefix secondPrefix, int secondExponent)
    {
        int exponentNew = firstExponent*firstPrefix.exponent + secondExponent*secondPrefix.exponent;
        SIPrefix prefixNew = getPrefix(exponentNew);

        return prefixNew;
    }

    public SIPrefix divide(SIPrefix denominator)
    {
        int exponentNew = this.exponent - denominator.exponent;
        SIPrefix prefixNew = getPrefix(exponentNew);

        return prefixNew;
    }

    public boolean hasNext()
    {
        boolean hasNext = this.exponent < HIGHEST_EXPONENT;
        return hasNext;
    }

    public SIPrefix getNext()
    {
        int exponentNext = this.exponent + 3;
        SIPrefix nextPrefix = getPrefix(exponentNext);

        return nextPrefix;
    }

    public boolean hasPrevious()
    {
        boolean hasPrevious = this.exponent > LOWEST_EXPONENT;
        return hasPrevious;
    }

    public SIPrefix getPrevious()
    {
        int exponentNext = this.exponent - 3;
        SIPrefix previousPrefix = getPrefix(exponentNext);

        return previousPrefix;
    }

    public double getPrefixConversion(SIPrefix prefixNew)
    {
        double absoluteConversionNew = prefixNew.getConversion();
        double relativeConversion = this.getConversion()/absoluteConversionNew;

        return relativeConversion;
    }

    public static boolean prefixKnown(double exponent)
    {
        if(exponent % 1 != 0)
        {
            return false;
        }

        int intExponent = (int)exponent;

        for (SIPrefix prefix : values()) 
        {
            if (prefix.exponent == intExponent) 
            {
                return true;
            }
        }

        return false;
    }

    public static SIPrefix getPrefix(int exponent)
    {
        for (SIPrefix prefix : values()) 
        {
            if (prefix.exponent == exponent) 
            {
                return prefix;
            }
        }

        throw new IllegalArgumentException("Invalid SIPrefix exponent: " + exponent);
    }

    public static SIPrefix getPrefix(String key) 
    {
        for (SIPrefix prefix : values()) 
        {
            for(String k : prefix.keys)
            {
                if (k.equals(key)) 
                {
                    return prefix;
                }
            }           
        }

        throw new IllegalArgumentException("Invalid SIPrefix value: " + (int)key.charAt(0));
    }

    @Override
    public String toString()
    {
        return key;
    }
}
