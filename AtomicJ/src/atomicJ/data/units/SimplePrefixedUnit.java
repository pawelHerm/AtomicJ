package atomicJ.data.units;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import atomicJ.utilities.GeometryUtilities;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.StringUtilities;


public class SimplePrefixedUnit implements SimpleUnit
{
    private static final SimplePrefixedUnit NULL_INSTANCE = new SimplePrefixedUnit("", SIPrefix.Empty, 0);

    private final int exp;
    private final String unitName;
    private final SIPrefix prefix;

    public SimplePrefixedUnit(String unitName)
    {
        this(unitName, SIPrefix.Empty);
    }

    public SimplePrefixedUnit(String unitName, SIPrefix prefix)
    {
        this(unitName, prefix, 1);
    }

    public SimplePrefixedUnit(String unitName, SIPrefix prefix, int exp)
    {
        this.exp = exp;
        this.unitName = unitName;
        this.prefix = prefix;
    }

    @Override
    public boolean isIdentity()
    {
        boolean identity = (exp == 0);
        return identity;
    }

    @Override
    public GeneralPrefixedUnit getGeneralUnit()
    {
        return GeneralPrefixedUnit.getInstance(Collections.singletonList(this), false);
    }

    @Override
    public List<SimpleUnit> getSimpleUnits()
    {
        return Collections.singletonList(this);
    }

    @Override
    public SimplePrefixedUnit simplify()
    {
        return this;
    }

    public static PrefixedUnit getNullInstance()
    {
        return NULL_INSTANCE;
    }

    @Override
    public int getExponent()
    {
        return exp;
    }

    public static int getTotalUnitExponent(Collection<SimplePrefixedUnit> units)
    {
        int totalUnitExponent = 0;

        for(SimplePrefixedUnit unit : units)
        {
            totalUnitExponent += unit.getExponent();
        }

        return totalUnitExponent;
    }

    @Override
    public GeneralPrefixedUnit multiply(PrefixedUnit other)
    {
        return this.getGeneralUnit().multiply(other);
    }

    @Override
    public RationalPrefixedUnit divide(PrefixedUnit other)
    {
        PrefixedUnit numeratorNew = this.multiply(GeneralPrefixedUnit.getInstance(other.getGeneralUnit().getDenominatorUnitsRaisedTo(-1), true));
        PrefixedUnit denominatorNew = GeneralPrefixedUnit.getInstance(other.getGeneralUnit().getNumeratorUnits(), true);

        return new RationalPrefixedUnit(numeratorNew, denominatorNew);
    }

    @Override
    public SimplePrefixedUnit power(int exp)
    {
        return new SimplePrefixedUnit(unitName, prefix, this.exp*exp);
    }

    @Override
    public String getBareName()
    {
        return unitName;
    }

    @Override
    public String getFullName()
    {
        String fullName = (exp == 1 ) ? prefix.getEquationForm() + unitName : getNameWithExponent() ;

        return fullName;
    }

    @Override
    public String getFullPrettyName()
    {
        String fullName = (exp == 1 ) ? prefix.getEquationForm() + unitName : getNameWithExponentInSuperscript() ;

        return fullName;
    }

    private String getNameWithExponent()
    {
        String fullName = (exp != 0) ? prefix.getEquationForm() + unitName + "^" + Integer.toString(exp) : prefix.getEquationForm();

        return fullName;
    }

    private String getNameWithExponentInSuperscript()
    {
        String fullName = (exp != 0) ? prefix.getEquationForm() + unitName + StringUtilities.toSuperScriptString(exp) : prefix.getEquationForm();

        return fullName;
    }

    @Override
    public SIPrefix getPrefix()
    {
        return prefix;
    }

    @Override
    public int getTotalPrefixExponent()
    {
        return exp*prefix.getExponent();
    }

    @Override
    public double getTotalPremultiplierExponent()
    {
        return exp*prefix.getExponent();
    }

    public static int getTotalPrefixExponent(Collection<SimplePrefixedUnit> units)
    {
        int totalPrefixExponent = 0;

        for(SimplePrefixedUnit unit : units)
        {
            totalPrefixExponent += unit.getTotalPrefixExponent();
        }

        return totalPrefixExponent;
    }

    public static double getTotalPremultiplierExponent(Collection<SimplePrefixedUnit> units)
    {
        int totalPremultiplierExponent = 0;

        for(SimplePrefixedUnit unit : units)
        {
            totalPremultiplierExponent += unit.getTotalPremultiplierExponent();
        }

        return totalPremultiplierExponent;
    }

    @Override
    public boolean hasNext()
    {
        return prefix.hasNext();
    }

    @Override
    public SimplePrefixedUnit getNext()
    {        
        if(prefix.hasNext())
        {
            SimplePrefixedUnit next = new SimplePrefixedUnit(unitName, prefix.getNext(), exp);
            return next;
        }

        throw new IllegalStateException("There is no unit next to " + toString());
    }

    @Override
    public boolean hasPrevious()
    {
        return prefix.hasPrevious();
    }

    @Override
    public SimplePrefixedUnit getPrevious()
    {
        if(prefix.hasPrevious())
        {
            SimplePrefixedUnit previous = new SimplePrefixedUnit(unitName, prefix.getPrevious(), exp);
            return previous;
        }
        throw new IllegalStateException("There is no unit previous to " + toString());
    }

    @Override
    public List<PrefixedUnit> deriveUnits()
    {
        return deriveUnits(-12, 12);
    }

    @Override
    public SimplePrefixedUnit deriveUnit(SIPrefix prefixNew)
    {
        SimplePrefixedUnit newUnit = new SimplePrefixedUnit(unitName, prefixNew, exp);

        return newUnit;
    }

    @Override
    public List<PrefixedUnit> deriveUnits(int minTotalPrefixExponentChange, int maxTotalPrefixExponentChange)
    {
        if(isIdentity())
        {
            return Collections.<PrefixedUnit>singletonList(this);
        }

        List<PrefixedUnit> derivedUnits = new ArrayList<>();

        int totalPrefixExponent = getTotalPrefixExponent();

        for(int exponentChange = minTotalPrefixExponentChange; exponentChange <= maxTotalPrefixExponentChange; exponentChange++)
        {
            int newPrefixTotalExponent = totalPrefixExponent + exponentChange;
            int newPrefixExponent = newPrefixTotalExponent/this.exp;

            if(SIPrefix.prefixKnown(newPrefixExponent))
            {
                derivedUnits.add(deriveUnit(SIPrefix.getPrefix(newPrefixExponent)));
            }
        }

        return derivedUnits;
    }

    @Override
    public PrefixedUnit getPreferredCompatibleUnit(double value)
    {
        if(isIdentity() || Double.isNaN(value) || GeometryUtilities.almostEqual(0, value, 1e-12))
        {
            return this;
        }

        double log = Math.log10(Math.abs(value)) + getTotalPrefixExponent();

        int index = MathUtilities.roundDownToMultiple(log, 3);

        double prefixFactor = ((double)index)/exp;
        int bestPrefixExponent = SIPrefix.prefixKnown(prefixFactor) ? (int)prefixFactor: Math.max(SIPrefix.LOWEST_EXPONENT, Math.min(SIPrefix.HIGHEST_EXPONENT, MathUtilities.roundToMultiple(prefixFactor, 3)));

        SIPrefix prefixNew = SIPrefix.getPrefix(bestPrefixExponent);

        SimplePrefixedUnit unit = new SimplePrefixedUnit(unitName, prefixNew, this.exp);

        return unit;
    }

    public static SimplePrefixedUnit getSmallestPrefixUnit(List<SimplePrefixedUnit> units)
    {
        if(units.isEmpty())
        {
            return null;
        }

        SIPrefix smallestPrefixExp = SIPrefix.getGratestPrefix();
        SimplePrefixedUnit smallestPrefixUnit = null;

        for(SimplePrefixedUnit unit : units)
        {
            SIPrefix currentPrefix = unit.getPrefix();

            if(currentPrefix.compareTo(smallestPrefixExp) <= 0)
            {
                smallestPrefixExp = currentPrefix;
                smallestPrefixUnit = unit;
            }
        }

        return smallestPrefixUnit;
    }

    public static SimplePrefixedUnit getGreatestPrefixUnit(List<SimplePrefixedUnit> units)
    {
        if(units.isEmpty())
        {
            return null;
        }

        SIPrefix greatestPrefixExp = SIPrefix.getSmallestPrefix();
        SimplePrefixedUnit greatestPrefixUnit = null;

        for(SimplePrefixedUnit unit : units)
        {
            SIPrefix currentPrefix = unit.getPrefix();

            if(currentPrefix.compareTo(greatestPrefixExp) >= 0)
            {
                greatestPrefixExp = currentPrefix;
                greatestPrefixUnit = unit;
            }
        }

        return greatestPrefixUnit;
    }

    @Override
    public double getConversionFactorTo(PrefixedUnit otherUnit)
    {
        double conversionFactor = 1;

        if(isCompatible(otherUnit))
        {
            double totalPrefixExponentsThis = getTotalPremultiplierExponent();
            double totalPrefixExponentOther = otherUnit.getTotalPremultiplierExponent();

            double converExp = totalPrefixExponentsThis - totalPrefixExponentOther;

            conversionFactor = Math.pow(10, converExp);
        }

        return conversionFactor;
    }

    @Override
    public String toString()
    {
        return getFullName();
    }

    @Override
    public DimensionElementVector getDimensionVector()
    {
        return DimensionElementVector.getInstance(Collections.singletonList(new DimensionElement(this.unitName, this.exp)));
    }

    @Override
    public boolean isCompatible(PrefixedUnit unitNew) 
    {        
        if(unitNew instanceof SimplePrefixedUnit)
        {
            return this.getDimensionVector().equals(unitNew.getDimensionVector()); 
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int result = 31*17 + exp;
        result = 31*result + this.unitName.hashCode();
        result = 31*result + this.prefix.hashCode();

        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        boolean equal = false;

        if(other instanceof SimplePrefixedUnit)
        {
            SimplePrefixedUnit otherUnit = (SimplePrefixedUnit) other;

            boolean exponentsEqual = (this.exp == otherUnit.exp);
            boolean namesEqual = Objects.equals(this.unitName, otherUnit.getBareName());
            boolean preficesEqual = Objects.equals(this.prefix, otherUnit.prefix);

            equal = exponentsEqual && namesEqual && preficesEqual;
        }

        return equal;       
    }

}
