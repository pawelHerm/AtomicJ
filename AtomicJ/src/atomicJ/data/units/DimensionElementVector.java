package atomicJ.data.units;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import atomicJ.utilities.MultiMap;

//immutable class
public class DimensionElementVector 
{    
    private static final DimensionElementVector IDENTITY_INSTANCE = new DimensionElementVector(Collections.<String, DimensionElement>emptyMap());

    //should contain no dimension elements with exp == 0;
    private final Map<String, DimensionElement> dimensionElements;

    private DimensionElementVector(Map<String, DimensionElement> dimensionElements)
    {
        this.dimensionElements = Collections.unmodifiableMap(dimensionElements);
    }

    public static DimensionElementVector getIdentityInstance()
    {
        return IDENTITY_INSTANCE;
    }

    public boolean isIdentity()
    {
        boolean identity = dimensionElements.isEmpty();
        return identity;
    }

    public Set<DimensionElement> getDimensionElements()
    {
        return new LinkedHashSet<>(this.dimensionElements.values());
    }

    public int size()
    {
        return dimensionElements.size();
    }

    public static DimensionElementVector getInstance(List<DimensionElement> allElements)
    {
        MultiMap<String, DimensionElement> dimensionElementsMap = new MultiMap<>();

        for(DimensionElement el : allElements)
        {
            String elementName = el.getName();
            dimensionElementsMap.put(elementName, el);
        }

        Map<String, DimensionElement> uniqueElements = new LinkedHashMap<>();

        for(Entry<String, List<DimensionElement>> entry : dimensionElementsMap.entrySet())
        {
            List<DimensionElement> elements = entry.getValue();

            String elementName = entry.getKey();

            int totalExponent = 0;
            for(DimensionElement el : elements)
            {
                totalExponent += el.getExponent();
            }

            if(totalExponent != 0)
            {
                uniqueElements.put(elementName, new DimensionElement(elementName, totalExponent));
            }
        }     

        return new DimensionElementVector(uniqueElements);
    }

    public DimensionElementVector multiply(DimensionElement el)
    {
        if(el.isIdentity())
        {
            return this;
        }

        String dimensionName = el.getName();
        Map<String, DimensionElement> dimensionElementsNew = new LinkedHashMap<>(this.dimensionElements);

        boolean alreadyPresent = dimensionElementsNew.containsKey(dimensionName);
        if(alreadyPresent)
        {
            DimensionElement oldElement = dimensionElementsNew.get(dimensionName);

            DimensionElement multiplied = oldElement.multiply(el.getExponent());
            if(multiplied.isIdentity())
            {
                dimensionElementsNew.remove(dimensionName);
            }
            else
            {
                dimensionElementsNew.put(dimensionName, multiplied);
            }
            return new DimensionElementVector(dimensionElementsNew);
        }

        dimensionElementsNew.put(el.getName(), el);

        DimensionElementVector result = dimensionElementsNew.isEmpty() ? IDENTITY_INSTANCE : new DimensionElementVector(dimensionElementsNew);

        return result;
    }

    public DimensionElementVector power(int exp)
    {
        if(exp == 0)
        {
            return IDENTITY_INSTANCE;
        }

        Map<String, DimensionElement> dimensionElementsNew = new LinkedHashMap<>();

        for(Entry<String, DimensionElement> entry : this.dimensionElements.entrySet())
        {
            dimensionElementsNew.put(entry.getKey(), entry.getValue().power(exp));
        }

        return new DimensionElementVector(dimensionElementsNew);
    }

    public String getFullName()
    {
        String fullName = "";
        for(DimensionElement el : dimensionElements.values())
        {
            fullName += " " + el.getFullName();
        }

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
        result = 31*result + this.dimensionElements.hashCode();

        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if(other instanceof DimensionElementVector)
        {
            DimensionElementVector otherDimensionVector = (DimensionElementVector)other;
            return this.dimensionElements.equals(otherDimensionVector.dimensionElements);
        }
        return false;
    }
}
