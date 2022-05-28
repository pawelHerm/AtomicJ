package atomicJ.data.units;

public class DimensionElement 
{
    private final String name;
    private final int exp;

    //immutable class
    public DimensionElement(String name, int exp)
    {
        this.name = name;
        this.exp = exp;
    }

    public boolean isIdentity()
    {
        boolean identity = (exp == 0);
        return identity;
    }

    public int getExponent()
    {
        return exp;
    }

    public String getName()
    {
        return name;
    }

    public DimensionElement power(int pow)
    {
        return new DimensionElement(this.name, pow*this.exp);
    }

    public DimensionElement multiply(int expOther)
    {
        return new DimensionElement(this.name, expOther + this.exp);
    }

    public String getFullName()
    {
        String fullName = (exp == 1) ? name : getNameWithExponent() ;

        return fullName;
    }

    private String getNameWithExponent()
    {
        String fullName = name + "^" + Integer.toString(exp);

        return fullName;
    }

    @Override
    public String toString()
    {
        return getFullName();
    }

    @Override
    public int hashCode()
    {
        int result = 17;
        result = 31*result + name.hashCode();
        result = 31*result + exp;

        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if(other instanceof DimensionElement)
        {
            DimensionElement otherDimensionFactor = (DimensionElement)other;
            boolean equal = this.name.equals(otherDimensionFactor.name) && (this.exp == otherDimensionFactor.exp);
            return equal;
        }
        return false;
    }
}
