package atomicJ.data.units;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import atomicJ.utilities.GeometryUtilities;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.StringUtilities;


public class Hour implements PrefixedUnit
{
    public static final double PREMULTIPLIER_EXPONENT = Math.log10(3600.);
    private static final Hour INSTANCE = new Hour();

    private static final String HOUR = "h";
    private final int exp;

    private Hour()
    {
        this(1);
    }

    private Hour(int exp)
    {
        this.exp = exp;
    }

    public static Hour getInstance()
    {
        return INSTANCE;
    }

    public static Hour getInstance(int exp)
    {
        Hour instance = (exp == 1) ? INSTANCE : new Hour(exp);
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
        return GeneralPrefixedUnit.getInstance(Collections.singletonList(new SimplePrefixedUnit(HOUR, SIPrefix.Empty, exp)), false);
    }

    @Override
    public List<SimpleUnit> getSimpleUnits()
    {
        return Collections.singletonList(new SimplePrefixedUnit(HOUR, SIPrefix.Empty, exp));
    }

    @Override
    public Hour simplify()
    {
        return this;
    }

    public int getExponent()
    {
        return exp;
    }

    public static int getTotalUnitExponent(Collection<Hour> units)
    {
        int totalUnitExponent = 0;

        for(Hour unit : units)
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
    public Hour power(int exp)
    {
        return new Hour(this.exp*exp);
    }

    public String getBareName()
    {
        return HOUR;
    }

    @Override
    public String getFullName()
    {
        String fullName = (exp == 1 ) ? HOUR : getNameWithExponent() ;

        return fullName;
    }

    @Override
    public String getFullPrettyName()
    {
        String fullName = (exp == 1 ) ? HOUR : getNameWithExponentInSuperscript() ;

        return fullName;
    }

    private String getNameWithExponent()
    {
        String fullName = (exp != 0) ? HOUR + "^" + Integer.toString(exp) : "";

        return fullName;
    }

    private String getNameWithExponentInSuperscript()
    {
        String fullName = (exp != 0) ? HOUR + StringUtilities.toSuperScriptString(exp) : "";

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

    public static int getTotalPrefixExponent(Collection<Hour> units)
    {
        int totalPrefixExponent = 0;

        for(Hour unit : units)
        {
            totalPrefixExponent += unit.getTotalPrefixExponent();
        }

        return totalPrefixExponent;
    }

    @Override
    public boolean hasNext()
    {
        return true;
    }

    @Override
    public PrefixedUnit getNext()
    {        
        Second next = Second.getInstance(SIPrefix.M, exp);
        return next;
    }

    @Override
    public boolean hasPrevious()
    {
        return true;
    }

    @Override
    public PrefixedUnit getPrevious()
    {
        return Minute.getInstance(exp);
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

        Minute minute = Minute.getInstance(this.exp);

        double totalPreMultiplierExponentHour = getTotalPremultiplierExponent();
        double totalPremultiplierExponentMinute = minute.getTotalPremultiplierExponent();

        double lowestTotalPremultiplierExponentOfNonSecondBasedUnit = Math.min(totalPreMultiplierExponentHour, totalPremultiplierExponentMinute);
        double highestTotalPremultiplierExponentOfNonSecondBasedUnit = Math.max(totalPreMultiplierExponentHour, totalPremultiplierExponentMinute);


        for(int exponentChange = minTotalPreMultiplierChange;
                exponentChange <= Math.min(lowestTotalPremultiplierExponentOfNonSecondBasedUnit - totalPreMultiplierExponentHour, maxTotalPreMultiplierChange); exponentChange = exponentChange + exp)
        {
            double newPrefixTotalExponent =  (int)totalPreMultiplierExponentHour + exponentChange;

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
            if(maxTotalPreMultiplierChange > (totalPremultiplierExponentMinute - totalPreMultiplierExponentHour) && minTotalPreMultiplierChange < (totalPremultiplierExponentMinute - totalPreMultiplierExponentHour))
            {
                derivedUnits.add(minute);
            }
            if(maxTotalPreMultiplierChange > 0 && minTotalPreMultiplierChange < 0)
            {
                derivedUnits.add(this);
            }

        }

        if(this.exp < 0)
        {
            if(maxTotalPreMultiplierChange > 0 && minTotalPreMultiplierChange < 0)
            {
                derivedUnits.add(this);
            }   
            if(maxTotalPreMultiplierChange > (totalPremultiplierExponentMinute - totalPreMultiplierExponentHour) && minTotalPreMultiplierChange < (totalPremultiplierExponentMinute - totalPreMultiplierExponentHour))
            {
                derivedUnits.add(minute);
            }                    
        }

        for(int exponentChange = (int)Math.round(highestTotalPremultiplierExponentOfNonSecondBasedUnit - totalPreMultiplierExponentHour) + 3;
                exponentChange <= maxTotalPreMultiplierChange; exponentChange++)
        {
            double newPrefixTotalExponent = (int)totalPreMultiplierExponentHour + exponentChange;
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

        if(logOfNumericValueExpressedInSeconds > this.exp*Minute.PREMULTIPLIER_EXPONENT && logOfNumericValueExpressedInSeconds < this.exp*Hour.PREMULTIPLIER_EXPONENT)
        {
            return Minute.getInstance(this.exp);
        }

        if(logOfNumericValueExpressedInSeconds > this.exp*Hour.PREMULTIPLIER_EXPONENT && logOfNumericValueExpressedInSeconds < this.exp*Hour.PREMULTIPLIER_EXPONENT + 3)
        {
            return this;
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

        if(other instanceof Hour)
        {
            Hour otherUnit = (Hour) other;
            boolean exponentsEqual = (this.exp == otherUnit.exp);
            equal = exponentsEqual;
        }

        return equal;       
    }

}
