package atomicJ.data.units;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import atomicJ.utilities.GeometryUtilities;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.StringUtilities;


public class Minute implements PrefixedUnit
{
    private static final String MINUTE = "min";

    private static final Minute INSTANCE = new Minute();

    public static final double PREMULTIPLIER_EXPONENT = Math.log10(60.);

    private final int exp;

    private Minute()
    {
        this.exp = 1;
    }

    private Minute(int exp)
    {
        this.exp = exp;
    }

    public static Minute getInstance()
    {
        return INSTANCE;
    }

    public static Minute getInstance(int exp)
    {
        Minute instance =(exp == 1) ? INSTANCE : new Minute(exp);
        return instance;
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
        return GeneralPrefixedUnit.getInstance(Collections.singletonList(new SimplePrefixedUnit(MINUTE, SIPrefix.Empty, exp)), false);
    }

    @Override
    public List<SimpleUnit> getSimpleUnits()
    {
        return Collections.singletonList(new SimplePrefixedUnit(MINUTE, SIPrefix.Empty, exp));
    }

    @Override
    public Minute simplify()
    {
        return this;
    }

    public int getExponent()
    {
        return exp;
    }

    public static int getTotalUnitExponent(Collection<Minute> units)
    {
        int totalUnitExponent = 0;

        for(Minute unit : units)
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
    public Minute power(int exp)
    {
        return new Minute(this.exp*exp);
    }

    public String getBareName()
    {
        return MINUTE;
    }

    @Override
    public String getFullName()
    {
        String fullName = (exp == 1 ) ? MINUTE : getNameWithExponent() ;

        return fullName;
    }

    @Override
    public String getFullPrettyName()
    {
        String fullName = (exp == 1 ) ? MINUTE : getNameWithExponentInSuperscript() ;

        return fullName;
    }

    private String getNameWithExponent()
    {
        String fullName = (exp != 0) ? MINUTE + "^" + Integer.toString(exp) : MINUTE;

        return fullName;
    }

    private String getNameWithExponentInSuperscript()
    {
        String fullName = (exp != 0) ? MINUTE + StringUtilities.toSuperScriptString(exp) : MINUTE;

        return fullName;
    }

    @Override
    public int getTotalPrefixExponent()
    {
        return 0;
    }

    @Override
    public double getTotalPremultiplierExponent()
    {
        return this.exp*PREMULTIPLIER_EXPONENT;
    }

    @Override
    public boolean hasNext()
    {
        return true;
    }

    @Override
    public Hour getNext()
    {        
        return Hour.getInstance(this.exp);
    }

    @Override
    public boolean hasPrevious()
    {
        return true;
    }

    @Override
    public Second getPrevious()
    {
        Second previous = Second.getInstance(SIPrefix.Empty, exp);
        return previous;
    }

    @Override
    public List<PrefixedUnit> deriveUnits()
    {        
        return deriveUnits(-12, 12);
    }

    @Override
    public List<PrefixedUnit> deriveUnits(int minTotalPreMultiplierChange, int maxTotalPreMultiplierChange)
    {
        if(isIdentity())
        {
            return Collections.<PrefixedUnit>singletonList(this);
        }

        List<PrefixedUnit> derivedUnits = new ArrayList<>();

        Hour hour = Hour.getInstance(this.exp);

        double totalPreMultiplierExponent = getTotalPremultiplierExponent();
        double totalPremultiplierExponentHour = hour.getTotalPremultiplierExponent();

        double lowestTotalPremultiplierExponentOfNonSecondBasedUnit = Math.min(totalPreMultiplierExponent, totalPremultiplierExponentHour);
        double highestTotalPremultiplierExponentOfNonSecondBasedUnit = Math.max(totalPreMultiplierExponent, totalPremultiplierExponentHour);


        for(int exponentChange = minTotalPreMultiplierChange;
                exponentChange <= Math.min(lowestTotalPremultiplierExponentOfNonSecondBasedUnit - totalPreMultiplierExponent, maxTotalPreMultiplierChange); exponentChange = exponentChange + exp)
        {
            double newPrefixTotalExponent =  (int)totalPreMultiplierExponent + exponentChange;

            int newPrefixExponent = (int)Math.round(newPrefixTotalExponent/this.exp);

            if(SIPrefix.prefixKnown(newPrefixExponent))
            {
                Second unit = Second.getInstance(SIPrefix.getPrefix(newPrefixExponent),this.exp);
                if(unit.isPrefixNatural() && !derivedUnits.contains(unit))
                {
                    derivedUnits.add(unit);
                }
            }
        }

        if(this.exp > 0)
        {
            if(maxTotalPreMultiplierChange > 0 && minTotalPreMultiplierChange < 0)
            {
                derivedUnits.add(this);
            }
            if(maxTotalPreMultiplierChange > (totalPremultiplierExponentHour - totalPreMultiplierExponent) && minTotalPreMultiplierChange < (totalPremultiplierExponentHour - totalPreMultiplierExponent))
            {
                derivedUnits.add(hour);
            }
        }

        if(this.exp < 0)
        {
            if(maxTotalPreMultiplierChange > (totalPremultiplierExponentHour - totalPreMultiplierExponent) && minTotalPreMultiplierChange < (totalPremultiplierExponentHour - totalPreMultiplierExponent))
            {
                derivedUnits.add(hour);
            }
            if(maxTotalPreMultiplierChange > 0 && minTotalPreMultiplierChange < 0)
            {
                derivedUnits.add(this);
            }            
        }

        for(int exponentChange = (int)Math.round(highestTotalPremultiplierExponentOfNonSecondBasedUnit - totalPreMultiplierExponent) + 3;
                exponentChange <= maxTotalPreMultiplierChange; exponentChange++)
        {
            double newPrefixTotalExponent = (int)totalPreMultiplierExponent + exponentChange;
            int newPrefixExponent = (int)Math.round(newPrefixTotalExponent/this.exp);

            if(SIPrefix.prefixKnown(newPrefixExponent))
            {
                Second unit = Second.getInstance(SIPrefix.getPrefix(newPrefixExponent),this.exp);
                if(unit.isPrefixNatural() && !derivedUnits.contains(unit))
                {
                    derivedUnits.add(Second.getInstance(SIPrefix.getPrefix(newPrefixExponent), this.exp));
                }
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

        double logOfNumericValueExpressedInSeconds = Math.log10(Math.abs(value)) + getTotalPremultiplierExponent();

        if(logOfNumericValueExpressedInSeconds > this.exp*PREMULTIPLIER_EXPONENT && logOfNumericValueExpressedInSeconds < this.exp*Hour.PREMULTIPLIER_EXPONENT)
        {
            return this;
        }

        if(logOfNumericValueExpressedInSeconds > this.exp*Hour.PREMULTIPLIER_EXPONENT && logOfNumericValueExpressedInSeconds < this.exp*Hour.PREMULTIPLIER_EXPONENT + 3)
        {
            return Hour.getInstance(this.exp);
        }

        int index = MathUtilities.roundDownToMultiple(logOfNumericValueExpressedInSeconds, 3);

        double prefixFactor = ((double)index)/exp;
        int bestPrefixExponent = SIPrefix.prefixKnown(prefixFactor) ? (int)prefixFactor: Math.max(SIPrefix.LOWEST_EXPONENT, Math.min(SIPrefix.HIGHEST_EXPONENT, MathUtilities.roundToMultiple(prefixFactor, 3)));
        SIPrefix prefixNew = SIPrefix.getPrefix(bestPrefixExponent);

        Second unit = Second.getInstance(prefixNew, this.exp);

        return unit;
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
        return DimensionElementVector.getInstance(Collections.singletonList(new DimensionElement(Second.SECOND, this.exp)));
    }

    @Override
    public boolean isCompatible(PrefixedUnit unitNew) 
    {        
        return this.getDimensionVector().equals(unitNew.getDimensionVector()); 
    }

    @Override
    public int hashCode()
    {
        int result = 31*17 + exp;

        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        boolean equal = false;

        if(other instanceof Minute)
        {
            Minute otherUnit = (Minute) other;

            boolean exponentsEqual = (this.exp == otherUnit.exp);
            equal = exponentsEqual;
        }

        return equal;       
    }

}
