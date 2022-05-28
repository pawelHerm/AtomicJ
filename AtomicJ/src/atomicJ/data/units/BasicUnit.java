package atomicJ.data.units;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public enum BasicUnit
{
    METER("m","Meter", new String[] {"Meter","meter","Metre","metre"}),
    NEWTON("N","Newton",new String[] {"Newton","newton"}),
    PASCAL("Pa","Pascal", new String[] {"Pascal","pascal"}),
    AMPERE("A","Ampere", new String[] {"Ampere","ampere"}),
    VOLT("V","Volt", new String[] {"Volt","volt"}),
    SECOND("s","Second", new String[] {"Second","second","Sec","sec"});

    private final String symbol;
    private final String name;
    private final String[] keys;

    BasicUnit(String symbol, String name, String[] keys)
    {
        this.symbol = symbol;
        this.name = name;
        this.keys = keys;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public boolean isSymbol(String s)
    {
        return Objects.equals(this.symbol, s);
    }

    public String getName()
    {
        return name;
    }

    public List<String> getNameVariants()
    {
        return new ArrayList<>(Arrays.asList(keys));
    }

    public static String convertToSymbolIfNecessary(String key) 
    {
        for (BasicUnit unit : values()) 
        {
            for(String k : unit.keys)
            {
                if (k.equals(key)) 
                {
                    return unit.symbol;
                }
            }           
        }
        return key;
    }
}