package atomicJ.data.units;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import atomicJ.utilities.GeometryUtilities;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.StringUtilities;


public class Second implements PrefixedUnit
{
    public static final String SECOND = "s";

    private final static Second SECOND_INSTANCE = new Second();

    private final int exp;
    private final SIPrefix prefix;

    private final static int MAXIMAL_PREFERRED_PREFIX_EXPONENT = 0;

    private Second()
    {
        this(SIPrefix.Empty);
    }

    private Second(SIPrefix prefix)
    {
        this(prefix, 1);
    }

    private Second(SIPrefix prefix, int exp)
    {
        this.exp = exp;
        this.prefix = prefix;
    }

    public static Second getInstance()
    {
        return SECOND_INSTANCE;
    }

    public static Second getInstance(SIPrefix prefix)
    {
        Second instance = new Second(prefix);
        return instance;
    }

    public static Second getInstance(SIPrefix prefix, int exp)
    {
        Second instance = new Second(prefix, exp);
        return instance;
    }

    @Override
    public boolean isIdentity()
    {
        boolean identity = (exp == 0);
        return identity;
    }

    public boolean isPrefixNatural()
    {
        return prefix.isExponentMultipleOf3();
    }

    @Override
    public GeneralPrefixedUnit getGeneralUnit()
    {
        return GeneralPrefixedUnit.getInstance(Collections.singletonList(new SimplePrefixedUnit(SECOND, prefix, exp)), false);
    }

    @Override
    public List<SimpleUnit> getSimpleUnits()
    {
        return Collections.singletonList(new SimplePrefixedUnit(SECOND, prefix, exp));
    }

    @Override
    public Second simplify()
    {
        return this;
    }

    public int getExponent()
    {
        return exp;
    }

    public static int getTotalUnitExponent(Collection<Second> units)
    {
        int totalUnitExponent = 0;

        for(Second unit : units)
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
    public Second power(int exp)
    {
        return new Second(prefix, this.exp*exp);
    }

    public String getBareName()
    {
        return SECOND;
    }

    @Override
    public String getFullName()
    {
        String fullName = (exp == 1 ) ? prefix.getEquationForm() + SECOND : getNameWithExponent() ;

        return fullName;
    }

    @Override
    public String getFullPrettyName()
    {
        String fullName = (exp == 1 ) ? prefix.getEquationForm() + SECOND : getNameWithExponentInSuperscript() ;

        return fullName;
    }

    private String getNameWithExponent()
    {
        String fullName = (exp != 0) ? prefix.getEquationForm() + SECOND + "^" + Integer.toString(exp) : prefix.getEquationForm();

        return fullName;
    }

    private String getNameWithExponentInSuperscript()
    {
        String fullName = (exp != 0) ? prefix.getEquationForm() + SECOND + StringUtilities.toSuperScriptString(exp) : prefix.getEquationForm();

        return fullName;
    }

    public SIPrefix getPrefix()
    {
        return prefix;
    }

    @Override
    public int getTotalPrefixExponent()
    {
        return exp*prefix.getExponent();
    }

    public static int getTotalPrefixExponent(Collection<Second> units)
    {
        int totalPrefixExponent = 0;

        for(Second unit : units)
        {
            totalPrefixExponent += unit.getTotalPrefixExponent();
        }

        return totalPrefixExponent;
    }

    @Override
    public double getTotalPremultiplierExponent()
    {
        return exp*prefix.getExponent();
    }

    @Override
    public boolean hasNext()
    {
        return prefix.hasNext();
    }

    @Override
    public PrefixedUnit getNext()
    {        
        if(prefix.hasNext() && prefix.getExponent() < MAXIMAL_PREFERRED_PREFIX_EXPONENT)
        {
            Second next = new Second(prefix.getNext(), exp);
            return next;
        }

        if(prefix.hasNext() && prefix.getExponent() > Hour.PREMULTIPLIER_EXPONENT)
        {
            Second next = new Second(prefix.getNext(), exp);
            return next;
        }

        if(prefix.getExponent() < Minute.PREMULTIPLIER_EXPONENT)
        {
            Minute next = Minute.getInstance(exp);
            return next;
        }

        if(prefix.getExponent() < Hour.PREMULTIPLIER_EXPONENT)
        {
            Hour next = Hour.getInstance(exp);
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
    public PrefixedUnit getPrevious()
    {
        if(prefix.hasPrevious())
        {
            if(prefix.getExponent() < Minute.PREMULTIPLIER_EXPONENT || prefix.getPrevious().getExponent() > Hour.PREMULTIPLIER_EXPONENT + 4)
            {
                Second previous = new Second(prefix.getPrevious(), exp);
                return previous;
            }

            if(prefix.getExponent() > Hour.PREMULTIPLIER_EXPONENT)
            {
                Hour previous = Hour.getInstance(exp);
                return previous;
            }

            if(prefix.getExponent() > Minute.PREMULTIPLIER_EXPONENT)
            {
                Minute previous = Minute.getInstance(exp);
                return previous;
            }           
        }
        throw new IllegalStateException("There is no unit previous to " + toString());
    }

    @Override
    public List<PrefixedUnit> deriveUnits()
    {
        return deriveUnits(-12, 12);
    }

    public Second deriveUnit(SIPrefix prefixNew)
    {
        Second newUnit = new Second(prefixNew, exp);

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

    public static List<PrefixedUnit> deriveUnits(int exp, int minTotalPrefixExponentChange, int maxTotalPrefixExponentChange)
    {
        if(exp == 0)
        {
            return Collections.<PrefixedUnit>singletonList(new Second(SIPrefix.Empty, 0));
        }

        List<PrefixedUnit> derivedUnits = new ArrayList<>();


        for(int exponentChange = minTotalPrefixExponentChange; exponentChange <= maxTotalPrefixExponentChange; exponentChange++)
        {
            int newPrefixTotalExponent = exp + exponentChange;
            int newPrefixExponent = newPrefixTotalExponent/exp;

            if(SIPrefix.prefixKnown(newPrefixExponent))
            {
                derivedUnits.add(new Second(SIPrefix.getPrefix(newPrefixExponent), exp));
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

        double logOfNumericValueExpressedInSeconds = Math.log10(Math.abs(value)) + getTotalPrefixExponent();     

        if(logOfNumericValueExpressedInSeconds < Minute.PREMULTIPLIER_EXPONENT*this.exp || /*w prefer thousands of hours to smaller numbers with ks as a unit, but not millions of hours*/logOfNumericValueExpressedInSeconds > 3 + Hour.PREMULTIPLIER_EXPONENT*this.exp)
        {
            int index = MathUtilities.roundDownToMultiple(logOfNumericValueExpressedInSeconds, 3);

            double prefixFactor = ((double)index)/exp;
            int bestPrefixExponent = SIPrefix.prefixKnown(prefixFactor) ? (int)prefixFactor: Math.max(SIPrefix.LOWEST_EXPONENT, Math.min(SIPrefix.HIGHEST_EXPONENT, MathUtilities.roundToMultiple(prefixFactor, 3)));
            SIPrefix prefixNew = SIPrefix.getPrefix(bestPrefixExponent);

            Second unit = new Second(prefixNew, this.exp);

            return unit;
        }

        if(logOfNumericValueExpressedInSeconds < Hour.PREMULTIPLIER_EXPONENT*this.exp)
        {
            return Minute.getInstance(this.exp);
        }

        Hour unit = Hour.getInstance(this.exp);

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
        return DimensionElementVector.getInstance(Collections.singletonList(new DimensionElement(SECOND, this.exp)));
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
        result = 31*result + this.prefix.hashCode();

        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        boolean equal = false;

        if(other instanceof Second)
        {
            Second otherUnit = (Second) other;

            boolean exponentsEqual = (this.exp == otherUnit.exp);
            boolean preficesEqual = Objects.equals(this.prefix, otherUnit.prefix);

            equal = exponentsEqual && preficesEqual;
        }

        return equal;       
    }

}

