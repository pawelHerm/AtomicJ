package atomicJ.data.units;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RationalPrefixedUnit implements PrefixedUnit
{
    private static String SOLIDUS = "/";

    private final GeneralPrefixedUnit numerator;
    private final GeneralPrefixedUnit denominator;

    public RationalPrefixedUnit(PrefixedUnit numerator, PrefixedUnit denominator)
    {
        this.numerator = numerator.getGeneralUnit();
        this.denominator = denominator.getGeneralUnit();
    }

    public RationalPrefixedUnit(List<? extends SimpleUnit> numerator, List<? extends SimpleUnit> denominator)
    {
        this.numerator = GeneralPrefixedUnit.getInstance(numerator, false);
        this.denominator = GeneralPrefixedUnit.getInstance(denominator, false);
    }

    private static RationalPrefixedUnit getInstance(GeneralPrefixedUnit unit)
    {
        GeneralPrefixedUnit numeratorNew = GeneralPrefixedUnit.getInstance(unit.getNumeratorUnits(), false);
        GeneralPrefixedUnit denominatorNew = GeneralPrefixedUnit.getInstance(unit.getDenominatorUnits(), false).power(-1);

        return new RationalPrefixedUnit(numeratorNew, denominatorNew);
    }

    @Override
    public GeneralPrefixedUnit getGeneralUnit()
    {
        return GeneralPrefixedUnit.getInstance(getSimpleUnits(), false);
    }

    @Override
    public boolean isIdentity()
    {
        return getGeneralUnit().isIdentity();
    }

    @Override
    public List<SimpleUnit> getSimpleUnits()
    {
        List<SimpleUnit> units = new ArrayList<>();

        units.addAll(numerator.getSimpleUnits());
        units.addAll(denominator.power(-1).getSimpleUnits());

        return units;
    }

    @Override
    public PrefixedUnit simplify()
    {
        GeneralPrefixedUnit simplified = GeneralPrefixedUnit.simplifyExponents(getGeneralUnit());

        List<SimpleUnit> simpleUnits = simplified.getSimpleUnits();

        if(simpleUnits.size() == 1)
        {
            return simpleUnits.get(0);
        }

        List<SimpleUnit> numeratorUnits = simplified.getNumeratorUnits();
        List<SimpleUnit> denominatorUnits = simplified.getDenominatorUnitsRaisedTo(-1);

        if(denominatorUnits.isEmpty())
        {
            return simplified;
        }

        return new RationalPrefixedUnit(numeratorUnits, denominatorUnits);
    }

    @Override
    public PrefixedUnit multiply(PrefixedUnit other)
    {
        PrefixedUnit numeratorNew = numerator.multiply(GeneralPrefixedUnit.getInstance(other.getGeneralUnit().getNumeratorUnits(), true));
        PrefixedUnit denominatorNew = denominator.multiply(GeneralPrefixedUnit.getInstance(other.getGeneralUnit().getDenominatorUnitsRaisedTo(-1), true));

        return new RationalPrefixedUnit(numeratorNew, denominatorNew);
    }

    @Override
    public PrefixedUnit divide(PrefixedUnit other)
    {
        PrefixedUnit numeratorNew = numerator.multiply(GeneralPrefixedUnit.getInstance(other.getGeneralUnit().getDenominatorUnitsRaisedTo(-1), true));
        PrefixedUnit denominatorNew = denominator.multiply(GeneralPrefixedUnit.getInstance(other.getGeneralUnit().getNumeratorUnits(), true));

        return new RationalPrefixedUnit(numeratorNew, denominatorNew);
    }

    @Override
    public RationalPrefixedUnit power(int exp)
    {
        return new RationalPrefixedUnit(numerator.power(exp), denominator.power(exp));

    }

    @Override
    public boolean hasNext()
    {
        boolean hasNext = numerator.hasNext() || denominator.hasPrevious();
        return hasNext;
    }

    @Override
    public RationalPrefixedUnit getNext()
    {
        if(numerator.hasNext())
        {
            RationalPrefixedUnit next = new RationalPrefixedUnit(numerator.getNext(), denominator);
            return next;
        }
        if(denominator.hasPrevious())
        {
            RationalPrefixedUnit next = new RationalPrefixedUnit(numerator, denominator.getPrevious());
            return next;
        }

        throw new IllegalStateException("There is no unit next to " + toString());
    }

    @Override
    public boolean hasPrevious()
    {
        boolean hasNext = numerator.hasPrevious() || denominator.hasNext();
        return hasNext;
    }

    @Override
    public RationalPrefixedUnit getPrevious()
    {
        if(numerator.hasPrevious())
        {
            RationalPrefixedUnit previous = new RationalPrefixedUnit(numerator.getPrevious(), denominator);
            return previous;
        }
        if(denominator.hasNext())
        {
            RationalPrefixedUnit previous = new RationalPrefixedUnit(numerator, denominator.getNext());
            return previous;
        }
        throw new IllegalStateException("There is no unit previous to " + toString());
    }


    @Override
    public String getFullName()
    {
        String numeratorName = numerator.getFullName();
        String denominatorName = denominator.getFullName();
        String rationalName = numeratorName + SOLIDUS + denominatorName;

        return rationalName;
    }

    @Override
    public String getFullPrettyName()
    {
        String numeratorName = numerator.getFullPrettyName();
        String denominatorName = denominator.getFullPrettyName();
        String rationalName = numeratorName + SOLIDUS + denominatorName;

        return rationalName;
    }

    @Override
    public int getTotalPrefixExponent()
    {
        int numeratorExp = numerator.getTotalPrefixExponent();
        int denominatorExp = denominator.getTotalPrefixExponent();

        int totalPrefixExponent = numeratorExp - denominatorExp;

        return totalPrefixExponent;

    }

    @Override
    public double getTotalPremultiplierExponent()
    {
        double numeratorExp = numerator.getTotalPremultiplierExponent();
        double denominatorExp = denominator.getTotalPremultiplierExponent();

        double totalPrefixExponent = numeratorExp - denominatorExp;

        return totalPrefixExponent;
    }


    @Override
    public List<PrefixedUnit> deriveUnits()
    {
        return deriveUnits(-9, 9, true);
    }

    @Override
    public List<PrefixedUnit> deriveUnits(int minTotalPrefixExponentChange, int maxTotalPrefixExponentChange)
    {
        return deriveUnits(minTotalPrefixExponentChange, maxTotalPrefixExponentChange, true);
    }

    public List<PrefixedUnit> deriveUnits(int minTotalPrefixExponentChange, int maxTotalPrefixExponentChange, boolean onlyInNumerator)
    {
        List<PrefixedUnit> derivedUnits = new ArrayList<>();

        for(int i = -minTotalPrefixExponentChange; i<maxTotalPrefixExponentChange; i++)
        {                        
            derivedUnits.add(deriveUnit(i, onlyInNumerator));
        }

        return derivedUnits;
    }

    private PrefixedUnit deriveUnit(int totalPrefixExponentChange, boolean onlyInNumerator)
    {
        GeneralPrefixedUnit compatibleUnit = getGeneralUnit().attemptToChangeUnitPrefices(totalPrefixExponentChange, onlyInNumerator);
        return RationalPrefixedUnit.getInstance(compatibleUnit);
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
        List<DimensionElement> dimensionElements = new ArrayList<>();

        dimensionElements.addAll(this.numerator.getDimensionVector().getDimensionElements());
        dimensionElements.addAll(this.denominator.getDimensionVector().power(-1).getDimensionElements());

        return DimensionElementVector.getInstance(dimensionElements);
    }

    @Override
    public boolean isCompatible(PrefixedUnit unitNew) 
    {
        return this.getDimensionVector().equals(unitNew.getDimensionVector()); 
    }

    @Override
    public RationalPrefixedUnit getPreferredCompatibleUnit(double value)
    {        
        GeneralPrefixedUnit numeratorCompatibleUnit = numerator.getPreferredCompatibleUnit(value, true);
        return new RationalPrefixedUnit(numeratorCompatibleUnit, denominator);
    }

    @Override
    public int hashCode()
    {
        int hashCode = 17;

        hashCode += 31*hashCode + this.numerator.hashCode();
        hashCode += 31*hashCode + this.denominator.hashCode();

        return hashCode;
    }

    @Override
    public boolean equals(Object other)
    {
        boolean equal = false;

        if(other instanceof RationalPrefixedUnit)
        {
            RationalPrefixedUnit otherUnit = (RationalPrefixedUnit) other;
            boolean numeratorEqual = Objects.equals(this.numerator, otherUnit.numerator);
            boolean denominatorEqual = Objects.equals(this.denominator, otherUnit.denominator);

            equal = numeratorEqual && denominatorEqual;
        }

        return equal;
    }
}
