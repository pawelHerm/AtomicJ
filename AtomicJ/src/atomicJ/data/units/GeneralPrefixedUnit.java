package atomicJ.data.units;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import atomicJ.utilities.GeometryUtilities;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.MultiMap;
import atomicJ.utilities.WeakCompositionGenerator;


public class GeneralPrefixedUnit implements PrefixedUnit
{
    private static final String EMPTY_UNIT_STRING_REPRESENTATION = "1";
    private static final GeneralPrefixedUnit NULL_INSTANCE = new GeneralPrefixedUnit(new MultiMap<String, SimpleUnit>());

    private static String SOLIDUS = "/";

    //should contain no SISimpleUnits with exp == 0
    private final MultiMap<String, SimpleUnit> simpleUnits;

    private GeneralPrefixedUnit(MultiMap<String, SimpleUnit> simpleUnits)
    {
        this.simpleUnits = simpleUnits;
    }

    public static PrefixedUnit getNullInstance()
    {
        return NULL_INSTANCE;
    }

    public boolean isSimple()
    {
        int unitCount = simpleUnits.getTotalSize();
        boolean isSimple = unitCount < 2;

        return isSimple;
    }

    public static GeneralPrefixedUnit getInstance(Collection<? extends SimpleUnit> units, boolean simplifyExponents)
    {
        MultiMap<String, SimpleUnit> simpleUnits = new MultiMap<>();

        for(SimpleUnit unit : units)
        {
            simpleUnits.put(unit.getBareName(), unit);
        }

        GeneralPrefixedUnit generalUnit = new GeneralPrefixedUnit(simpleUnits);
        GeneralPrefixedUnit finalUnit = simplifyExponents ? simplifyExponents(generalUnit) : generalUnit;

        return finalUnit;
    }

    @Override
    public GeneralPrefixedUnit simplify()
    {
        return simplifyExponents(this);
    }


    public static GeneralPrefixedUnit simplifyExponents(GeneralPrefixedUnit unit)
    {
        MultiMap<String, SimpleUnit> simpleCancelledUnits = new MultiMap<>();

        for(String unitName : unit.simpleUnits.keySet())
        {
            List<SimpleUnit> simpleUnitsForName = unit.simpleUnits.getCopy(unitName);

            int totalPrefixExponentNew = SimpleUnit.getTotalPrefixExponent(simpleUnitsForName);
            int totalUnitExponent = SimpleUnit.getTotalUnitExponent(simpleUnitsForName);

            if(totalUnitExponent == 0 && totalPrefixExponentNew == 0)
            {
                continue;
            }

            if(totalUnitExponent != 0)
            {
                double prefixFactor = totalPrefixExponentNew/totalUnitExponent;
                int bestPrefixExponent = SIPrefix.prefixKnown(prefixFactor) ? (int)prefixFactor: Math.max(SIPrefix.LOWEST_EXPONENT, Math.min(SIPrefix.HIGHEST_EXPONENT, MathUtilities.roundToMultiple(prefixFactor, 3)));
                boolean canBeCancelled = (totalPrefixExponentNew - bestPrefixExponent*totalUnitExponent == 0);

                if(canBeCancelled)
                {      
                    simpleUnitsForName = Collections.singletonList(new SimplePrefixedUnit(unitName, SIPrefix.getPrefix(bestPrefixExponent), totalUnitExponent));            
                }
            }


            simpleCancelledUnits.setValues(unitName, simpleUnitsForName);
        }

        return new GeneralPrefixedUnit(simpleCancelledUnits);
    }

    @Override
    public GeneralPrefixedUnit getGeneralUnit()
    {
        return this;
    }

    @Override
    public boolean isIdentity()
    {
        boolean identity = simplify().simpleUnits.isEmpty();
        return identity;
    }

    @Override
    public GeneralPrefixedUnit multiply(PrefixedUnit other)
    {
        return multiplyByGeneralUnit(other.getGeneralUnit());
    }

    @Override
    public GeneralPrefixedUnit divide(PrefixedUnit other)
    {
        return multiplyByGeneralUnit(other.getGeneralUnit().power(-1));
    }

    private GeneralPrefixedUnit multiplyByGeneralUnit(GeneralPrefixedUnit otherUnits)
    {             
        MultiMap<String, SimpleUnit> simpleUnitsNew = new MultiMap<>(this.simpleUnits);

        for(Entry<String, List<SimpleUnit>> entry : otherUnits.simpleUnits.entrySet())
        {           
            String unitName = entry.getKey();
            List<? extends SimpleUnit> otherUnitForName = entry.getValue();

            if(otherUnitForName.isEmpty())
            {
                continue;
            }

            boolean unitTypeAlreadyPresent = !simpleUnitsNew.isEmpty(unitName);

            List<SimpleUnit> simpleUnitsForName;
            if(unitTypeAlreadyPresent)
            {
                simpleUnitsForName = simpleUnitsNew.getCopy(unitName);
                simpleUnitsForName.addAll(otherUnitForName);

                int totalPrefixExponentNew = SimpleUnit.getTotalPrefixExponent(simpleUnitsForName);
                int totalUnitExponent = SimpleUnit.getTotalUnitExponent(simpleUnitsForName);

                if(totalUnitExponent == 0 && totalPrefixExponentNew == 0)
                {
                    simpleUnitsNew.clear(unitName);
                    continue;
                }

                if(totalUnitExponent == 0)
                {
                    simpleUnitsNew.setValues(unitName, simpleUnitsForName);
                    continue;
                }

                double prefixFactor = totalPrefixExponentNew/totalUnitExponent;
                int bestPrefixExponent = SIPrefix.prefixKnown(prefixFactor) ? (int)prefixFactor: Math.max(SIPrefix.LOWEST_EXPONENT, Math.min(SIPrefix.HIGHEST_EXPONENT, MathUtilities.roundToMultiple(prefixFactor, 3)));
                int remainingPower = totalPrefixExponentNew - bestPrefixExponent*totalUnitExponent;

                Collection<SimpleUnit> simplifiedUnitsForName = (remainingPower == 0) ?  Collections.singletonList(new SimplePrefixedUnit(unitName, SIPrefix.getPrefix(bestPrefixExponent), totalUnitExponent)) : simpleUnitsForName;
                simpleUnitsNew.setValues(unitName, simplifiedUnitsForName);            
            }
            else
            {
                simpleUnitsForName = new ArrayList<>(otherUnitForName);
                simpleUnitsNew.setValues(unitName, simpleUnitsForName);
            }
        }


        return new GeneralPrefixedUnit(simpleUnitsNew);
    }

    @Override
    public GeneralPrefixedUnit power(int exp)
    {
        if(exp == 0)
        {
            return NULL_INSTANCE;
        }

        MultiMap<String, SimpleUnit> simpleUnitsNew = new MultiMap<>();

        for(Entry<String, List<SimpleUnit>> entry : this.simpleUnits.entrySet())
        {
            String unitName = entry.getKey();

            List<SimpleUnit> unitsOld = entry.getValue();
            List<SimpleUnit> unitsNew = new ArrayList<>();

            for(SimpleUnit unit : unitsOld)
            {
                unitsNew.add(unit.power(exp));
            }

            simpleUnitsNew.putAll(unitName, unitsNew);
        }

        return new GeneralPrefixedUnit(simpleUnitsNew);
    }

    @Override
    public GeneralPrefixedUnit getPreferredCompatibleUnit(double value)
    {
        return getPreferredCompatibleUnit(value, false);   
    }

    public GeneralPrefixedUnit getPreferredCompatibleUnit(double value, boolean onlyNumerator)
    {
        if(isIdentity() || Double.isNaN(value) || GeometryUtilities.almostEqual(0, value, 1e-12))
        {
            return this;
        }

        double log = Math.log10(Math.abs(value));

        int totalPrefixExponentChange = MathUtilities.roundDownToMultiple(log, 3);

        return attemptToChangeUnitPrefices(totalPrefixExponentChange, onlyNumerator);
    }

    public GeneralPrefixedUnit attemptToChangeUnitPrefices(int totalPrefixExponentChange, boolean onlyInNumerator)
    {
        if(totalPrefixExponentChange == 0)
        {
            return this;
        }

        boolean increase = totalPrefixExponentChange > 0;

        int[][] prefixChangeRestrictions = increase ? getPrefixIncreaseRestrictions(onlyInNumerator) : MathUtilities.abs(getPrefixDecreaseRestrictions(onlyInNumerator));
        int[][] possiblePrefixChangesAbs = WeakCompositionGenerator.generateCompositions(prefixChangeRestrictions, Math.abs(totalPrefixExponentChange), prefixChangeRestrictions.length);
        int[][] possiblePrefixChanges = increase ? possiblePrefixChangesAbs : MathUtilities.multiply(possiblePrefixChangesAbs, -1);


        int[] bestPrefixChanges = getBestPrefixIncreases(possiblePrefixChanges); 

        return getCompatibleUnit(bestPrefixChanges);   
    }

    private int[] getBestPrefixIncreases(int[][] allIncreases)
    {
        int bestIndex = -1;
        double lowestCriterion = Double.POSITIVE_INFINITY;

        for(int i = 0; i<allIncreases.length; i++)
        {
            double currentCriterion = getPrefixChangeCriterion(allIncreases[i]);
            if(currentCriterion < lowestCriterion)
            {
                bestIndex = i;
                lowestCriterion = currentCriterion;
            }
        }

        int[] bestIncreases = bestIndex >= 0 ? allIncreases[bestIndex] : new int[] {};
        return bestIncreases;
    }

    private GeneralPrefixedUnit getCompatibleUnit(int[] prefixChanges)
    {
        List<SimpleUnit> changedUnits = new ArrayList<>();
        List<SimpleUnit> allUnits = simpleUnits.allValues();

        for(int i = 0; i<allUnits.size(); i++)
        {
            SimpleUnit unit = allUnits.get(i);

            if(i >= prefixChanges.length)
            {
                changedUnits.add(unit);
                continue;
            }

            SIPrefix currentPrefix = unit.getPrefix();

            int exponent = unit.getExponent();
            int newPrefixExponent = currentPrefix.getExponent() + prefixChanges[i]/exponent;
            changedUnits.add(unit.deriveUnit(SIPrefix.getPrefix(newPrefixExponent)));     
        }

        return GeneralPrefixedUnit.getInstance(changedUnits, false);
    }

    private double getPrefixChangeCriterion(int[] prefixChanges)
    {
        double criterion = 0;
        List<SimpleUnit> allUnits = simpleUnits.allValues();

        for(int i = 0; i<allUnits.size(); i++)
        {
            SimpleUnit unit = allUnits.get(i);
            int exponent = unit.getExponent();

            SIPrefix currentPrefix = unit.getPrefix();
            int newPrefixExponent = currentPrefix.getExponent() + prefixChanges[i]/exponent;
            criterion += Math.abs(newPrefixExponent);
        }

        return criterion;
    }

    private int[][] getPrefixDecreaseRestrictions(boolean onlyNumerator)
    {
        int[][] restrictions = new int[simpleUnits.getTotalSize()][];

        List<SimpleUnit> allUnits = simpleUnits.allValues();

        for(int i = 0; i<allUnits.size();i++)
        {
            SimpleUnit unit = allUnits.get(i);
            int exponent = unit.getExponent();

            if(onlyNumerator && exponent < 0)
            {
                restrictions[i]= new int[] {0};
                continue;
            }

            SIPrefix currentPrefix = unit.getPrefix();
            int currentPrefixExponent = currentPrefix.getExponent();
            List<SIPrefix> possibleOtherPrefices = exponent > 0 ? SIPrefix.getRangeEndingAt(currentPrefix) : SIPrefix.getRangeBeginningAt(currentPrefix);

            int[] restrictionsForUnit = new int[possibleOtherPrefices.size()];

            for(int j = 0; j<possibleOtherPrefices.size(); j++)
            {
                restrictionsForUnit[j] = exponent*(possibleOtherPrefices.get(j).getExponent() - currentPrefixExponent);
            }

            restrictions[i] = restrictionsForUnit;
        }

        return restrictions;
    }

    private int[][] getPrefixIncreaseRestrictions(boolean onlyNumerator)
    {
        int[][] increaseRestrictions = new int[simpleUnits.getTotalSize()][];

        List<SimpleUnit> allUnits = simpleUnits.allValues();

        for(int i = 0; i<allUnits.size();i++)
        {
            SimpleUnit unit = allUnits.get(i);
            int exponent = unit.getExponent();

            if(onlyNumerator && exponent < 0)
            {
                increaseRestrictions[i]= new int[] {};
                continue;
            }

            SIPrefix currentPrefix = unit.getPrefix();
            int currentPrefixExponent = currentPrefix.getExponent();
            List<SIPrefix> possibleOtherPrefices = exponent > 0 ? SIPrefix.getRangeBeginningAt(currentPrefix) : SIPrefix.getRangeEndingAt(currentPrefix);

            int[] restrictions = new int[possibleOtherPrefices.size()];

            for(int j = 0; j<possibleOtherPrefices.size(); j++)
            {
                restrictions[j] = exponent*(possibleOtherPrefices.get(j).getExponent() - currentPrefixExponent);
            }

            increaseRestrictions[i] = restrictions;
        }

        return increaseRestrictions;
    }

    @Override
    public String getFullName()
    {      
        String fullName = getUnitString(getSimpleUnits(), true);

        return fullName;
    }

    @Override
    public String getFullPrettyName()
    {      
        String fullName = getUnitStringPrettyNames(getSimpleUnits(), true);

        return fullName;
    }

    public String getFullNameWithSolidus()
    {
        List<SimpleUnit> numeratorUnits = getNumeratorUnits();
        List<SimpleUnit> denominatorUnits = getDenominatorUnits();

        if(numeratorUnits.isEmpty() && denominatorUnits.isEmpty())
        {
            return "";
        }

        if(denominatorUnits.isEmpty())
        {
            return getUnitString(numeratorUnits, false);
        }

        String numeratorString = getUnitString(numeratorUnits, true);
        String denominatorString = getUnitString(denominatorUnits, true);

        String fullName = numeratorString + SOLIDUS + denominatorString;

        return fullName;
    }

    private String getUnitString(List<SimpleUnit> units, boolean bracketsMayBeNeccessary)
    {        
        if(units.isEmpty())
        {
            return EMPTY_UNIT_STRING_REPRESENTATION;
        }

        boolean multiple = units.size() > 1;

        StringBuilder unitStringBuilder = new StringBuilder(units.get(0).getFullName());

        for(int i = 1; i<units.size();i++)//the zeroth index cannot be included in the loop, this causes problems with single element units list, in which case the unitString would start with empty space  
        {
            unitStringBuilder.append(" ").append(units.get(i).getFullName());
        }

        String unitString = multiple && bracketsMayBeNeccessary ? unitStringBuilder.append(")").insert(0, "(").toString(): unitStringBuilder.toString();

        return unitString;
    }

    private String getUnitStringPrettyNames(List<SimpleUnit> units, boolean bracketsMayBeNeccessary)
    {        
        if(units.isEmpty())
        {
            return EMPTY_UNIT_STRING_REPRESENTATION;
        }

        boolean multiple = units.size() > 1;

        StringBuilder unitStringBuilder = new StringBuilder(units.get(0).getFullName());

        for(int i = 1; i<units.size();i++)//the zeroth index cannot be included in the loop, this causes problems with single element units list, in which case the unitString would start with empty space  
        {
            unitStringBuilder.append(" ").append(units.get(i).getFullPrettyName());
        }

        String unitString = multiple && bracketsMayBeNeccessary ? unitStringBuilder.append(")").insert(0, "(").toString(): unitStringBuilder.toString();

        return unitString;
    }

    public List<SimpleUnit> getNumeratorUnits()
    {
        List<SimpleUnit> allUnits = this.simpleUnits.allValues();

        List<SimpleUnit> numeratorUnits = new ArrayList<>();

        for(SimpleUnit unit : allUnits)
        {
            if(unit.getExponent() > 0)
            {
                numeratorUnits.add(unit);
            }
        }

        return numeratorUnits;
    }

    public List<SimpleUnit> getNumeratorUnitsRaisedTo(int exp)
    {
        List<SimpleUnit> allUnits = this.simpleUnits.allValues();

        List<SimpleUnit> numeratorUnits = new ArrayList<>();

        for(SimpleUnit unit : allUnits)
        {
            if(unit.getExponent() > 0)
            {
                numeratorUnits.add(unit.power(exp));
            }
        }

        return numeratorUnits;
    }

    public List<SimpleUnit> getDenominatorUnits()
    {
        List<SimpleUnit> allUnits = this.simpleUnits.allValues();

        List<SimpleUnit> denominatorUnits = new ArrayList<>();

        for(SimpleUnit unit : allUnits)
        {
            if(unit.getExponent() < 0)
            {
                denominatorUnits.add(unit);
            }
        }

        return denominatorUnits;
    }

    public List<SimpleUnit> getDenominatorUnitsRaisedTo(int exp)
    {
        List<SimpleUnit> allUnits = this.simpleUnits.allValues();

        List<SimpleUnit> denominatorUnits = new ArrayList<>();

        for(SimpleUnit unit : allUnits)
        {
            if(unit.getExponent() < 0)
            {
                denominatorUnits.add(unit.power(exp));
            }
        }

        return denominatorUnits;
    }

    @Override
    public List<SimpleUnit> getSimpleUnits()
    {
        return simpleUnits.allValues();
    }

    @Override
    public int getTotalPrefixExponent()
    { 
        List<SimpleUnit> allSimpleUnits = this.simpleUnits.allValues();

        return SimpleUnit.getTotalPrefixExponent(allSimpleUnits);
    }

    @Override
    public double getTotalPremultiplierExponent()
    {
        List<SimpleUnit> allSimpleUnits = this.simpleUnits.allValues();

        return SimpleUnit.getTotalPremultiplierExponent(allSimpleUnits);
    }

    @Override
    public boolean hasNext()
    {
        if(this.simpleUnits.isEmpty())
        {
            return false;
        }

        List<SimpleUnit> numeratorUnits = getNumeratorUnits();
        if(!numeratorUnits.isEmpty())
        {
            boolean numeratorHasNext = SimpleUnit.getSmallestPrefixUnit(numeratorUnits).hasNext();
            if(numeratorHasNext)
            {
                return true;
            }
        }
        List<SimpleUnit> denominatorUnits = getDenominatorUnits();
        if(!denominatorUnits.isEmpty())
        {
            boolean denominatorHasPrevious = SimpleUnit.getGreatestPrefixUnit(getDenominatorUnits()).hasPrevious();
            if(denominatorHasPrevious)
            {
                return true;
            }
        }

        return false;
    }


    @Override
    public PrefixedUnit getNext()
    {
        if(this.simpleUnits.isEmpty())
        {
            return SimplePrefixedUnit.getNullInstance();
        }

        MultiMap<String, SimpleUnit> unitsNew = new MultiMap<>(this.simpleUnits);
        List<SimpleUnit> numeratorUnits = getNumeratorUnits();

        if(!numeratorUnits.isEmpty())
        {
            SimpleUnit smallestPrefixNumeratorUnit = SimpleUnit.getSmallestPrefixUnit(numeratorUnits);     

            if(smallestPrefixNumeratorUnit.hasNext())
            {
                String unitName = smallestPrefixNumeratorUnit.getBareName();
                List<SimpleUnit> unitsForName = unitsNew.get(unitName);
                int index = unitsForName.indexOf(smallestPrefixNumeratorUnit);
                unitsForName.set(index, smallestPrefixNumeratorUnit.getNext());

                unitsNew.setValues(unitName, unitsForName); //not necessary really, because we already modified original value list, not copy

                GeneralPrefixedUnit next = new GeneralPrefixedUnit(unitsNew);
                return next;

            }
        }

        List<SimpleUnit> denominatorUnits = getDenominatorUnits();
        if(!denominatorUnits.isEmpty())
        {
            SimpleUnit greatestPrefixDenominatorUnit = SimpleUnit.getGreatestPrefixUnit(denominatorUnits);

            if(greatestPrefixDenominatorUnit.hasPrevious())
            {
                String unitName = greatestPrefixDenominatorUnit.getBareName();
                List<SimpleUnit> unitsForName = unitsNew.get(unitName);
                int index = unitsForName.indexOf(greatestPrefixDenominatorUnit);
                unitsForName.set(index, greatestPrefixDenominatorUnit.getPrevious());

                unitsNew.setValues(unitName, unitsForName); //not necessary really, because we already modified original value list, not copy

                GeneralPrefixedUnit next = new GeneralPrefixedUnit(unitsNew);

                return next;
            }
        }     

        throw new IllegalStateException("There is no unit next to " + toString());
    }

    @Override
    public boolean hasPrevious()
    {
        if(this.simpleUnits.isEmpty())
        {
            return false;
        }

        List<SimpleUnit> numeratorUnits = getNumeratorUnits();
        if(!numeratorUnits.isEmpty())
        {
            boolean numeratorHasPrevious = SimpleUnit.getGreatestPrefixUnit(numeratorUnits).hasPrevious();
            if(numeratorHasPrevious)
            {
                return true;
            }
        }

        List<SimpleUnit> denominatorUnits = getDenominatorUnits();
        if(!denominatorUnits.isEmpty())
        {
            boolean denominatorHasNext = SimpleUnit.getSmallestPrefixUnit(getDenominatorUnits()).hasNext();  

            if(denominatorHasNext)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public PrefixedUnit getPrevious()
    {
        if(this.simpleUnits.isEmpty())
        {
            return SimplePrefixedUnit.getNullInstance();
        }

        MultiMap<String, SimpleUnit> unitsNew = new MultiMap<>(this.simpleUnits);
        List<SimpleUnit> numeratorUnits = getNumeratorUnits();

        if(!numeratorUnits.isEmpty())
        {
            SimpleUnit greatestPrefixNumeratorUnit = SimpleUnit.getGreatestPrefixUnit(numeratorUnits);     
            if(greatestPrefixNumeratorUnit.hasPrevious())
            {
                String unitName = greatestPrefixNumeratorUnit.getBareName();

                List<SimpleUnit> unitsForName = unitsNew.get(unitName);
                int index = unitsForName.indexOf(greatestPrefixNumeratorUnit);
                unitsForName.set(index, greatestPrefixNumeratorUnit.getPrevious());

                unitsNew.setValues(unitName, unitsForName); //not necessary really, because we already modified original value list, not copy

                GeneralPrefixedUnit next = new GeneralPrefixedUnit(unitsNew);
                return next;
            }         
        }

        List<SimpleUnit> denominatorUnits = getDenominatorUnits();
        if(!denominatorUnits.isEmpty())
        {
            SimpleUnit smallestPrefixDenominatorUnit = SimpleUnit.getSmallestPrefixUnit(denominatorUnits);
            if(smallestPrefixDenominatorUnit.hasNext())
            {
                String unitName = smallestPrefixDenominatorUnit.getBareName();
                List<SimpleUnit> unitsForName = unitsNew.get(unitName);
                int index = unitsForName.indexOf(smallestPrefixDenominatorUnit);
                unitsForName.set(index, smallestPrefixDenominatorUnit.getNext());

                unitsNew.setValues(unitName, unitsForName); //not necessary really, because we already modified original value list, not copy
                GeneralPrefixedUnit next = new GeneralPrefixedUnit(unitsNew);
                return next;
            }
        }      

        throw new IllegalStateException("There is no unit previous to " + toString());
    }

    @Override
    public List<PrefixedUnit> deriveUnits()
    {
        List<PrefixedUnit> derivedUnits = new ArrayList<>();

        derivedUnits.add(this);
        PrefixedUnit lastDerivedUnit = this;

        for(int i = 1; i<10; i++)
        {
            if(lastDerivedUnit.hasNext())
            {
                lastDerivedUnit = lastDerivedUnit.getNext(); 
                derivedUnits.add(lastDerivedUnit);
            }
            else
            {
                break;
            }
        }

        lastDerivedUnit = this;

        for(int i = 1; i<10; i++)
        {
            if(lastDerivedUnit.hasPrevious())
            {
                lastDerivedUnit = lastDerivedUnit.getPrevious(); 
                derivedUnits.add(lastDerivedUnit);
            }
            else
            {
                break;
            }
        }

        Collections.sort(derivedUnits, new TotalPremultiplierExponentComparator());

        return derivedUnits;
    }

    @Override
    public List<PrefixedUnit> deriveUnits(int minTotalPrefixExponentChange, int maxTotalPrefixExponentChange)
    {
        return this.deriveUnits(minTotalPrefixExponentChange, maxTotalPrefixExponentChange, false);
    }

    public List<PrefixedUnit> deriveUnits(int minTotalPrefixExponentChange, int maxTotalPrefixExponentChange, boolean onlyInNumerator)
    {
        List<PrefixedUnit> derivedUnits = new ArrayList<>();

        for(int exponentChange = minTotalPrefixExponentChange; exponentChange <= maxTotalPrefixExponentChange; exponentChange++)
        {
            derivedUnits.add(attemptToChangeUnitPrefices(exponentChange, onlyInNumerator));
        }

        return derivedUnits;
    }

    @Override
    public double getConversionFactorTo(PrefixedUnit otherUnit)
    {
        double conversionFactor = 1;

        if(isCompatible(otherUnit))
        {
            double totalPremultiplierExponentsThis = getTotalPremultiplierExponent();
            double totalPremultiplierExponentOther = otherUnit.getTotalPremultiplierExponent();

            double converExp = totalPremultiplierExponentsThis - totalPremultiplierExponentOther;

            conversionFactor = Math.pow(10, converExp);
        }

        return conversionFactor;
    }

    @Override
    public int hashCode()
    {
        int result = 17;
        result = 31*result + simpleUnits.hashCode();

        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if(!(other instanceof GeneralPrefixedUnit))
        {
            return false;
        }

        GeneralPrefixedUnit otherUnit = (GeneralPrefixedUnit)other;

        return this.simpleUnits.equals(otherUnit.simpleUnits);       
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

        for(SimpleUnit unit : simpleUnits.allValues())
        {
            dimensionElements.addAll(unit.getDimensionVector().getDimensionElements());
        }

        return DimensionElementVector.getInstance(dimensionElements);
    }

    @Override
    public boolean isCompatible(PrefixedUnit unitNew) 
    {        
        return this.getDimensionVector().equals(unitNew.getDimensionVector()); 
    }
}
