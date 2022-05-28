package atomicJ.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MultiMap<K, V> 
{
    private final Map<K, List<V>> delegate = new LinkedHashMap<>();

    public MultiMap()
    {}

    public MultiMap(Map<K, List<V>> oldMap)
    {
        for(Entry<K, List<V>> entry : oldMap.entrySet())
        {
            K key = entry.getKey();
            List<V> valueCopy = new ArrayList<>(entry.getValue());

            delegate.put(key, valueCopy);
        }
    }

    public MultiMap(MultiMap<K,V> oldMap)
    {
        for(Entry<K, List<V>> entry : oldMap.delegate.entrySet())
        {
            K key = entry.getKey();
            List<V> valueCopy = new ArrayList<>(entry.getValue());

            this.delegate.put(key, valueCopy);
        }
    }

    public int size() 
    {
        return delegate.size();
    }

    public int size(K key)
    {
        int size = 0;

        List<V> value = delegate.get(key);
        if(value != null)
        {
            size = value.size();
        }

        return size;
    }

    public int getTotalSize()
    {
        int totalSize = 0;

        for(List<V> values : delegate.values())
        {
            totalSize += values.size();
        }

        return totalSize;
    }

    public Map<K, Integer> getInnerSizes()
    {
        Map<K, Integer> innerSizes = new LinkedHashMap<>();

        for(Entry<K, List<V>> entry:  delegate.entrySet())
        {
            innerSizes.put(entry.getKey(), Integer.valueOf(entry.getValue().size()));
        }

        return innerSizes;
    }

    public boolean isEmpty() 
    {
        boolean empty = true;

        for(List<V> values : delegate.values())
        {
            empty = empty && values.isEmpty();

            if(!empty)
            {
                break;
            }
        }

        return empty;
    }

    public boolean isEmpty(K outerKey)
    {
        boolean empty = true;

        List<V> value = delegate.get(outerKey);
        if(value != null)
        {
            empty = value.isEmpty();
        }

        return empty;
    }

    public boolean contains(Object key, Object value)
    {
        List<V> values = delegate.get(key);

        boolean contains = values != null ? values.contains(value) : false;

        return contains;
    }

    public boolean containsKey(Object key) 
    {
        List<?> values = delegate.get(key);
        boolean contains = values != null  && !values.isEmpty();
        return contains;
    }

    public boolean containsValue(Object value) 
    {
        boolean contains = false;

        for(List<?> valueList : delegate.values())
        {
            contains = valueList.contains(value);
            if(contains)
            {
                break;
            }
        }
        return contains;
    }

    public List<V> get(Object key)
    {
        List<V> values = delegate.get(key);
        if(values != null)
        {
            return values;
        }
        return new ArrayList<>(); 
    }

    public List<V> getCopy(Object key)
    {
        List<V> values = delegate.get(key);
        if(values != null)
        {
            return new ArrayList<>(values);
        }
        return new ArrayList<>(); 
    }


    public void put(K key, V value) 
    {
        List<V> listForKey = delegate.get(key);
        if(listForKey == null)
        {
            listForKey = new ArrayList<>();
            delegate.put(key, listForKey);
        }

        listForKey.add(value);
    }

    public void put(Map<K, V> simpleMap) 
    {
        for(Entry<K,V> entry: simpleMap.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    /*
     * Adds new elements for a particular keys 
     * The list of values passed to the method is copied inside the method.
     * 
     * @param key key to store values
     * @param values new values for the key
     */
    public void putAll(K key, Collection<? extends V> values) 
    {
        if(values.isEmpty())
        {
            return;
        }

        List<V> listForKey = delegate.get(key);
        if(listForKey == null)
        {
            listForKey = new ArrayList<>();
            delegate.put(key, listForKey);
        }

        listForKey.addAll(values);
    }  

    public void putAll(Map<K, List<V>> m)
    {
        for(Entry<K, List<V>> entry : m.entrySet())
        {
            K key = entry.getKey();
            List<V> valuesNew = entry.getValue();

            putAll(key, valuesNew);
        }
    }

    public void putAll(MultiMap<K, V> m)
    {
        for(Entry<K, List<V>> entry : m.entrySet())
        {
            K key = entry.getKey();
            List<V> newValues = entry.getValue();

            putAll(key, newValues);
        }
    }

    public List<V> remove(Object key) 
    {
        return delegate.remove(key);
    }

    public boolean remove(Object key, Object value) 
    {
        boolean changed = false;

        List<V> allValues = delegate.get(key);
        if(value != null)
        {
            changed = allValues.remove(value);
            if(allValues.isEmpty())
            {
                delegate.remove(key);
            }
        }
        return changed;
    }

    public boolean removeAll(Object key, List<?> valuesToRemove) 
    {
        boolean changed = false;

        List<V> allValues = delegate.get(key);
        if(valuesToRemove != null)
        {
            changed = allValues.remove(valuesToRemove);
            if(allValues.isEmpty())
            {
                delegate.remove(key);
            }
        }
        return changed;
    }

    public void removeInAll(Object value) 
    {
        Iterator<Entry<K, List<V>>> it= delegate.entrySet().iterator();

        while(it.hasNext())
        {
            Entry<K, List<V>> entry = it.next();

            List<V> values = entry.getValue();
            values.remove(value);

            if(values.isEmpty())
            {
                it.remove();
            }
        }
    }

    public void removeAllInAll(List<?> valuesToRemove) 
    {
        Iterator<Entry<K, List<V>>> it = delegate.entrySet().iterator();

        while(it.hasNext())
        {
            Entry<K, List<V>> entry = it.next();

            List<V> values = entry.getValue();
            values.removeAll(valuesToRemove);

            if(values.isEmpty())
            {
                it.remove();
            }
        }
    }

    public void setValues(K key, Collection<V> values)
    {
        delegate.put(key, new ArrayList<>(values));
    }

    public void clear() 
    {
        delegate.clear();
    }

    public void clear(K key)
    {
        delegate.remove(key);
    }

    public Set<K> keySet() 
    {
        return delegate.keySet();
    }

    public Collection<List<V>> values() 
    {
        return delegate.values();
    }

    public List<V> allValues()
    {
        List<V> allValues = new ArrayList<>();

        for(List<V> val : delegate.values())
        {
            allValues.addAll(val);
        }

        return allValues;
    }

    public Set<java.util.Map.Entry<K, List<V>>> entrySet() 
    {
        return delegate.entrySet();
    }

    @Override
    public boolean equals(Object other)
    {
        if(other instanceof MultiMap)
        {
            MultiMap<?,?> mapOther = (MultiMap<?,?>) other;
            return this.delegate.equals(mapOther.delegate);
        }

        return false;
    }

    @Override
    public int hashCode() 
    {      
        return delegate.hashCode();
    }
}
