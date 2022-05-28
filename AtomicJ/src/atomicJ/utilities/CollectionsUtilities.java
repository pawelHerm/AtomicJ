package atomicJ.utilities;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CollectionsUtilities 
{    
    public static <E, K, V>  Map<K, Map<E, V>> swapNestedHierarchy( Map<E, Map<K, V>>  mapInput)
    {
        Map<K, Map<E, V>> mapSwapped = new LinkedHashMap<>();

        for(Entry<E, Map<K, V>> entry : mapInput.entrySet())
        {
            E inputOuterKey = entry.getKey();
            Map<K, V> inputOuterValue = entry.getValue();

            for(Entry<K, V>  entryInner : inputOuterValue.entrySet())
            {
                K inputInnerKey = entryInner.getKey();
                V inputInnerValue = entryInner.getValue();

                Map<E, V> swappedOuterValue = mapSwapped.get(inputInnerKey);
                if(swappedOuterValue == null)
                {
                    swappedOuterValue = new LinkedHashMap<>();
                    mapSwapped.put(inputInnerKey, swappedOuterValue);
                }
                swappedOuterValue.put(inputOuterKey, inputInnerValue);    
            }
        }

        return mapSwapped;
    }

    public static <K, E, V> Map<K, Map<E, V>> deepCopy(Map<K, Map<E, V>> that)
    {
        Map<K, Map<E, V>> copy = new LinkedHashMap<>();

        for(Entry<K, Map<E, V>> entry: that.entrySet())
        {
            K type = entry.getKey();
            Map<E, V> samplesForType = new LinkedHashMap<>(entry.getValue());

            copy.put(type, samplesForType);
        }

        return copy;
    }

    //this method is designed for LinkedHashMap
    //such maps preserve insertion order, however, when put(key, value) is called with already existing
    //key, the key not only gets replaced, but also moved to the end of the mapo
    //
    //this method allows to replace a value in a map, without changing the insertion order
    //it return the copy of the map with the value replaced
    //if the value is not present, it simply return the copy of the map

    public static <K, V> Map<K,V> replaceMapValue(Map<K,V> map, K key, V valueNew)
    {
        //this is ugly, but necessary to preserve order in a LinkedHashMap
        // see http://stackoverflow.com/questions/14383855/modify-both-key-and-value-in-a-linkedhashmap-without-affecting-the-insertion-ord

        //it is even more uglier,
        //because I want to put at the end the collective samples for ROIS, whose
        //names starts with 'All' 
        //I change this when I create new system of ROI identifiers

        Map<K, V> tmp = new LinkedHashMap<>(map);

        Map<K, V> result = new LinkedHashMap<>();

        for (Entry<K, V> e : tmp.entrySet())
        {
            if (e.getKey().equals(key))
            {
                result.put(key, valueNew);
            } 
            else
            {
                result.put(e.getKey(), e.getValue());
            }
        }

        return result;
    }

    public static <V> Map<String, V> convertKeysToStrings(Map<?, V> map)
    {
        Map<String, V> mapCopy = new LinkedHashMap<>();

        for(Entry<?, V> entry : map.entrySet())
        {
            Object key = entry.getKey();
            String keyNew = (key != null) ? entry.getKey().toString() : null;
            V value = entry.getValue();

            mapCopy.put(keyNew, value);
        }

        return mapCopy;
    }

    public static List<double[]> trimXRepetitionsIfNecessary(List<double[]> points, double tolerance)
    {
        if(points == null || points.isEmpty())
        {
            return points;
        }

        int n = points.size();

        int firstGoodIndex = 0;
        double firstX = points.get(0)[0];

        for(int i = 1; i<n; i++)
        {
            double x = points.get(i)[0];
            boolean equal = MathUtilities.equalWithinTolerance(x, firstX, tolerance);

            if(!equal)
            {
                firstGoodIndex = i;
                break;
            }          
        }

        int lastGoodIndex = n - 1;
        double lastX = points.get(n - 1)[0];

        for(int i = n - 2; i >= 0; i--)
        {
            double x = points.get(i)[0];
            boolean equal = MathUtilities.equalWithinTolerance(x, lastX, tolerance);

            if(!equal)
            {
                lastGoodIndex = i;
                break;
            }          
        }

        boolean trimmingNecessary = firstGoodIndex > 0 || lastGoodIndex < n - 1;

        List<double[]> trimmed = trimmingNecessary ? points.subList(firstGoodIndex, lastGoodIndex) : points;
        return trimmed;
    }

    public static void print(Map<?,?> map)
    {
        System.out.print("{");
        Iterator<? extends Entry<?,?>> iterator = map.entrySet().iterator();

        while(iterator.hasNext())
        {
            Entry<?,?> entry = iterator.next();

            Object key = entry.getKey();
            Object value = entry.getValue();

            System.out.print(key.toString());
            System.out.print(value.toString());

            System.out.print("{");
            System.out.print(key.toString());
            System.out.print(",");
            System.out.print(value.toString());

            System.out.print("}");        

            if(iterator.hasNext())
            {
                System.out.print(",");
            }     

            System.out.println();

        }
        System.out.print("}");        
    }

    public static void print(Collection<?> data) 
    {
        System.out.print("{");
        Iterator<?> iterator = data.iterator();

        while(iterator.hasNext())
        {
            Object item = iterator.next();
            System.out.print(String.valueOf(item));
            if(iterator.hasNext())
            {
                System.out.print(",");
            }               
        }
        System.out.println("}");        
    }


    //returns - 1 when the array is empty or all values are either infinite or NaN
    public static int getIndexOfValueClosestTo(List<? extends Number> values, double testVal)
    {
        return getIndexOfValueClosestTo(values, testVal, 0, values.size());
    }

    //returns - 1 when tto <= from or all values are either infinite or NaN
    public static int getIndexOfValueClosestTo(List<? extends Number> values, double testVal, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, values.size(), "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, values.size(), "to");

        int index = -1;
        double difference = Double.POSITIVE_INFINITY;     

        for(int i = from; i < to; i++)
        {
            double differenceCurrent = Math.abs(values.get(i).doubleValue() - testVal);
            if(differenceCurrent < difference)
            {
                index = i;
                difference = differenceCurrent;
            }
        }

        return index;
    }


    //returns - 1 when the array is empty or all values are either infinite or NaN
    public static int getIndexOfValueMostDistantFrom(List<? extends Number> values, double testVal)
    {
        return getIndexOfValueMostDistantFrom(values, testVal, 0, values.size());
    }

    //returns - 1 when the to <= from or all values are either infinite or NaN
    public static int getIndexOfValueMostDistantFrom(List<? extends Number> values, double testVal, int from, int to)
    {
        Validation.requireValueEqualToOrBetweenBounds(from, 0, values.size(), "from");
        Validation.requireValueEqualToOrBetweenBounds(to, 0, values.size(), "to");

        int index = -1;
        double difference = Double.NEGATIVE_INFINITY;     

        for(int i = to; i < from; i++)
        {
            double differenceCurrent = Math.abs(values.get(i).doubleValue() - testVal);
            if(differenceCurrent > difference)
            {
                index = i;
                difference = differenceCurrent;
            }
        }

        return index;
    }
}
