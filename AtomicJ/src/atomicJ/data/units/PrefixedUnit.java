package atomicJ.data.units;

import java.util.List;


public interface PrefixedUnit 
{ 
    public int getTotalPrefixExponent();
    public double getTotalPremultiplierExponent();

    public double getConversionFactorTo(PrefixedUnit unit);
    public String getFullName();
    public String getFullPrettyName();
    public List<PrefixedUnit> deriveUnits();
    public List<PrefixedUnit> deriveUnits(int minTotalPrefixExponentChange, int maxTotalPrefixExponentChange);
    public PrefixedUnit getPreferredCompatibleUnit(double value);
    public PrefixedUnit multiply(PrefixedUnit other);
    public PrefixedUnit divide(PrefixedUnit other);
    public PrefixedUnit power(int exp);

    public GeneralPrefixedUnit getGeneralUnit();
    public List<SimpleUnit> getSimpleUnits();
    public PrefixedUnit simplify();
    public boolean hasNext();
    public PrefixedUnit getNext();
    public boolean hasPrevious();
    public PrefixedUnit getPrevious();
    public boolean isCompatible(PrefixedUnit unitNew);
    public DimensionElementVector getDimensionVector();
    public boolean isIdentity();

    public default PrefixedUnit getInverse()
    {
        return power(-1);
    }
}
