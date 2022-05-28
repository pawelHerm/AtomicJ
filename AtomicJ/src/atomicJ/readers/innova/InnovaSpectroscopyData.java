package atomicJ.readers.innova;

import java.util.Map;

import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitExpression;
import atomicJ.data.units.UnitUtilities;


public class InnovaSpectroscopyData
{
    public static final String NAME = "Point Spectroscopy Data";

    private static final String APPROACH_RATE = "Approach Rate";
    private static final String WITHDRAW_RATE = "Retreat Rate";
    private static final String Z_START = "Z Start";
    private static final String Z_END = "Z End";

    private final UnitExpression approachRate;
    private final UnitExpression withdrawRate;
    private final UnitExpression zStart;
    private final UnitExpression zEnd;

    public InnovaSpectroscopyData(InnovaINISection section)
    {
        Map<String, String> keyValuePairs = section.getKeyValuePairs();

        String approachRateKey = keyValuePairs.get(APPROACH_RATE);
        String withdrawRateKey = keyValuePairs.get(WITHDRAW_RATE);
        String zStartKey = keyValuePairs.get(Z_START);
        String zEndKey = keyValuePairs.get(Z_END);

        this.approachRate = parseUnitExpession(approachRateKey);
        this.withdrawRate = parseUnitExpession(withdrawRateKey);
        this.zStart = parseUnitExpession(zStartKey);
        this.zEnd = parseUnitExpession(zEndKey);
    }

    public UnitExpression getZStart()
    {
        return zStart;
    }

    public UnitExpression getZEnd()
    {
        return zEnd;
    }

    private UnitExpression parseUnitExpession(String key)
    {
        if(key == null)
        {
            return null;
        }

        String[] words = key.split("\\s+");   

        int wordsCount = words.length;       

        double value = Double.parseDouble(words[0]);
        PrefixedUnit unit = wordsCount > 1 ? UnitUtilities.getSIUnit(words[1]) : SimplePrefixedUnit.getNullInstance();
        UnitExpression expression = new UnitExpression(value, unit);

        return expression;
    }
}
