package atomicJ.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MetaMap<K, E, V> 
{
    private Map<K, Map<E, V>> delegate = new LinkedHashMap<>();

    public MetaMap()
    {}

    public MetaMap(Map<? extends K, ? extends Map<? extends E, ? extends V>> oldMap)
    {
        for(Entry<? extends K, ? extends Map<? extends E, ? extends V>> entry : oldMap.entrySet())
        {
            K key = entry.getKey();
            Map<E, V> valueCopy = new LinkedHashMap<>(entry.getValue());

            delegate.put(key, valueCopy);
        }
    }

    public Map<K, Map<E, V>> getMapCopy()
    {
        Map<K, Map<E, V>> delegateCopy = new LinkedHashMap<>();

        for(Entry<K, Map<E, V>> entry : delegate.entrySet())
        {
            K key = entry.getKey();
            Map<E, V> valueCopy = new LinkedHashMap<>(entry.getValue());

            delegateCopy.put(key, valueCopy);
        }

        return delegateCopy;
    }

    public MetaMap<E, K, V> swapKeyOrder()
    {
        Map<E,Map<K,V>> swappedDelegate = CollectionsUtilities.swapNestedHierarchy(delegate);
        MetaMap<E,K,V> metMapSwapped = new MetaMap<>();

        metMapSwapped.delegate = swappedDelegate;

        return metMapSwapped;
    }

    public Map<K, Map<E, V>> getSubMapCopy(E innerKey)
    {
        Map<K, Map<E, V>> copy = new LinkedHashMap<>();

        for(Entry<K, Map<E,V>> entry : delegate.entrySet())
        {
            K outerKey = entry.getKey();
            Map<E,V> innerMap = entry.getValue();

            Map<E,V> innerSubMap = new LinkedHashMap<>();
            if(innerMap.containsKey(innerKey))
            {                
                innerSubMap.put(innerKey, innerMap.get(innerKey));
            }

            copy.put(outerKey, innerSubMap);
        }

        return copy;
    }

    public int size() 
    {
        return delegate.size();
    }

    public int size(K outerKey)
    {
        int size = 0;

        Map<E, V> value = delegate.get(outerKey);
        if(value != null)
        {
            size = value.size();
        }

        return size;
    }

    public int getTotalSize()
    {
        int totalSize = 0;

        for(Map<E, V> values : delegate.values())
        {
            totalSize += values.size();
        }

        return totalSize;
    }

    public boolean isEmpty() 
    {
        return delegate.isEmpty();
    }

    public boolean isEmpty(K outerKey)
    {
        boolean empty = true;

        Map<E, V> value = delegate.get(outerKey);
        if(value != null)
        {
            empty = value.isEmpty();
        }

        return empty;
    }

    public boolean containsOuterKey(Object key) 
    {
        return delegate.containsKey(key);
    }

    public boolean containsKeyPair(Object outerKey, Object innerKey)
    {
        Map<?,?> subMap = delegate.get(outerKey);

        boolean contains =  subMap != null ? subMap.containsKey(innerKey) : false;

        return contains;
    }

    public boolean containsValue(Object value) 
    {
        boolean contains = false;

        for(Map<?, ?> valueList : delegate.values())
        {
            contains = valueList.containsValue(value);
            if(contains)
            {
                break;
            }
        }
        return contains;   
    }

    public Map<E, V> get(Object outerKey)
    {
        Map<E, V> value = delegate.get(outerKey);

        if(value != null)
        {
            return value;
        }

        return new LinkedHashMap<>(); 
    }

    public Map<E, V> getCopy(Object outerKey)
    {
        Map<E, V> value = delegate.get(outerKey);

        if(value != null)
        {
            return new LinkedHashMap<>(value);
        }

        return new LinkedHashMap<>(); 
    }

    public V get(Object outerKey, Object innerKey)
    {
        V value = null;
        Map<E, V> innerMap = delegate.get(outerKey);
        if(innerMap != null)
        {
            value = innerMap.get(innerKey);
        }
        return value; 
    }

    public List<V> getInnerKeyValuesCopy(E innerKey)
    {
        List<V> values = new ArrayList<>();

        for(Entry<K, Map<E,V>> outerEntry : delegate.entrySet())
        {
            Map<E, V> innerMap = outerEntry.getValue();

            //we use contains() instead of checking whether get(innerKey) returns null
            //because it may be the case that the value itself is null, i.e.
            //the fact that get(innerKey) returns null does not necessarily mean
            //the innerKey is absent
            if(innerMap.containsKey(innerKey))
            {
                values.add(innerMap.get(innerKey));
            }
        }

        return values;
    }

    public List<V> getOuterKeyValuesCopy(K outerKey)
    {
        List<V> values = new ArrayList<>();
        Map<E, V> innerMap = delegate.get(outerKey);

        if(innerMap != null)
        {
            values.addAll(innerMap.values());
        }

        return values;
    }

    public List<V> getAllValuesCopy()
    {
        List<V> values = new ArrayList<>();

        for(Entry<K, Map<E,V>> entry : delegate.entrySet())
        {
            Map<E, V> innerMap = entry.getValue();
            if(innerMap != null)
            {
                values.addAll(innerMap.values());
            }           
        }

        return values;
    }

    public void putInAllInnerMaps(E innerKey, V value)
    {
        for(Map<E, V> innerMap : delegate.values())
        {
            innerMap.put(innerKey, value);
        }
    }

    public void put(K outerKey, E innerKey, V value) 
    {
        Map<E, V> innerMap = delegate.get(outerKey);
        if(innerMap == null)
        {
            innerMap = new LinkedHashMap<>();
            delegate.put(outerKey, innerMap);
        }

        innerMap.put(innerKey, value);
    }

    /*
     * Adds new elements for a particular keys 
     * The map of values passed to the method is not modified inside the method.
     * 
     * @param key key to store values
     * @param values new values for the key
     */
    public void putAll(K key, Map<? extends E, ? extends V> values) 
    {
        Map<E, V> innerMap = delegate.get(key);
        if(innerMap == null)
        {
            innerMap = new LinkedHashMap<>();
            delegate.put(key, innerMap);
        }

        innerMap.putAll(values);
    }  


    public void putAll(Map<? extends K, ? extends Map<? extends E, ? extends V>> m)
    {
        for(Entry<? extends K, ? extends Map<? extends E, ? extends V>> entry : m.entrySet())
        {
            K key = entry.getKey();
            Map<? extends E, ? extends V> newValues = entry.getValue();

            putAll(key, newValues);
        }
    }

    public void putAll(MetaMap<? extends K, E, V> m)
    {
        for(Entry<? extends K, Map<E, V>> entry : m.entrySet())
        {
            K key = entry.getKey();
            Map<E, V> newValues = entry.getValue();

            putAll(key, newValues);
        }
    }

    public Map<E, V> remove(Object outerKey) 
    {
        return delegate.remove(outerKey);
    }

    public V remove(Object outerKey, Object innerKey) 
    {
        Map<E, V> innerMap = delegate.get(outerKey);

        V removed = null;
        if(innerMap != null)
        {
            removed = innerMap.remove(innerKey);
        }
        return removed;
    }

    public void removeInAllInnerMaps(Object innerKey)
    {
        for(Map<E, V> innerMap : delegate.values())
        {
            innerMap.remove(innerKey);
        }
    }

    public void copyInnerValues(E innerKey, Map<? extends K, ? extends Map<? extends E, ? extends V>> otherMap)
    {
        for(Entry<? extends K, Map<E, V>> outerEntry : delegate.entrySet())
        {
            K outerKey = outerEntry.getKey();

            Map<? extends E, ? extends V> otherInnerMap = otherMap.get(outerKey);

            if(otherInnerMap != null)
            {
                Map<E, V> innerMap = outerEntry.getValue();
                innerMap.put(innerKey, otherInnerMap.get(innerKey));
            }
        }
    }

    public void clear() 
    {
        delegate.clear();
    }

    public Set<K> keySet() 
    {
        return delegate.keySet();
    }

    public Collection<Map<E, V>> values() 
    {
        return delegate.values();
    }

    public Set<Entry<K, Map<E, V>>> entrySet() 
    {
        return delegate.entrySet();
    }
}
