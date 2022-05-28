package atomicJ.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import atomicJ.data.Channel1D;
import atomicJ.gui.AbstractModel;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.MultiMap;


public class TypeModelManagerExperimental extends AbstractModel
{
    //keeps the order of types
    private final List<String> types = new ArrayList<>();

    //allows to map type to TypeModelSlim
    private final HashMap<String, TypeModel<String>> typeModels = new HashMap<>();   

    private final Map<Integer, SimpleSpectroscopySource> sources = new LinkedHashMap<>();
    private final Map<SimpleSpectroscopySource, Integer> sourcesReverse = new LinkedHashMap<>();

    private int currentSourceIndex = 0;

    public TypeModelManagerExperimental()
    {}

    public TypeModelManagerExperimental(SimpleSpectroscopySource source, List<String> types)
    {
        registerSource(source);

        Integer key = sourcesReverse.get(source);

        for(String type : types)
        {
            TypeModel<String> model = new TypeModel<>(type);
            model.registerIdentifier(key, type);

            TypeModel<String> old = typeModels.put(type, model);

            if(old == null) //in case identifiers were duplicate
            {
                types.add(type);
            }
        }      

    }

    public TypeModelManagerExperimental(TypeModelManagerExperimental that)
    {
        this.types.addAll(that.types);

        for(Entry<String, TypeModel<String>> entry: that.typeModels.entrySet())
        {
            this.typeModels.put(entry.getKey(), new TypeModel<>(entry.getValue()));
        }

        Set<SimpleSpectroscopySource> sourcesOld = that.getSources();

        for(SimpleSpectroscopySource sourceOld : sourcesOld)
        {
            SimpleSpectroscopySource sourceNew = sourceOld.copy();   
            registerSource(sourceNew);
        }
    }   

    private void registerSource(SimpleSpectroscopySource source)
    {        
        if(!sources.containsValue(source))
        {         
            sourcesReverse.put(source, currentSourceIndex);
            sources.put(currentSourceIndex, source);    
            currentSourceIndex++;
        }
    }

    public void registerIdentifier(String type, SimpleSpectroscopySource source, String identifier)
    {
        registerSource(source);

        TypeModel<String> model = typeModels.get(type);

        if(model == null)
        {           
            model = new TypeModel<>(type);
            types.add(type);
            typeModels.put(type, model);
        }

        model.registerIdentifier(sourcesReverse.get(source), identifier);  
    }

    public TypeModel<String> get(String type)
    {
        return typeModels.get(type);
    }

    public List<String> getTypes()
    {
        return new ArrayList<>(types);
    }

    public int getIndex(String type)
    {
        return types.indexOf(type);
    }

    public Set<SimpleSpectroscopySource> getSources()
    {
        Set<SimpleSpectroscopySource> sourcesCopy = new LinkedHashSet<>(sources.values());

        return sourcesCopy;
    }

    public boolean containsSource(ChannelSource source)
    {
        return sources.containsValue(source);
    }


    public MultiMap<SimpleSpectroscopySource, String> getChannelIdentifiers()
    {
        MultiMap<SimpleSpectroscopySource, String> allChannels = new MultiMap<>(); 

        for(String type : types)
        {
            Map<SimpleSpectroscopySource, List<String>> sourcesForType = getResourceIdentifierMap(type);
            allChannels.putAll(sourcesForType);
        }

        return allChannels;
    }

    //regular channel identifiers, not universal identifiers
    public Set<String> getIdentifiers(String type)
    {
        TypeModel<String> model = typeModels.get(type);

        Set<String> identifiers = (model != null) ? model.getIdentifiers() : new LinkedHashSet<>();

        return identifiers;
    }

    public Set<String> getIdentifiersForAllTypes()
    {
        Set<String> identifiers = new LinkedHashSet<>();

        for(TypeModel<String> typeModel : typeModels.values())
        {
            identifiers.addAll(typeModel.getIdentifiers());
        }

        return identifiers;
    }

    private  Map<SimpleSpectroscopySource, List<String>> getResourceIdentifierMap(String type)
    {
        Map<SimpleSpectroscopySource, List<String>> map = new LinkedHashMap<>();

        TypeModel<String> model = typeModels.get(type);

        if(model != null)
        {
            for(Entry<Integer, SimpleSpectroscopySource> entry : sources.entrySet())
            {
                Integer key = entry.getKey();
                SimpleSpectroscopySource source = entry.getValue();
                List<String> identifiers = model.getIdentifiers(key);
                map.put(source, identifiers);
            }
        }

        return map;
    }


    public Map<SimpleSpectroscopySource, List<Channel1D>> getResourceChannelMap(String type) 
    {
        Map<SimpleSpectroscopySource, List<Channel1D>> map = new LinkedHashMap<>();

        TypeModel<String> model = typeModels.get(type);
        if(model != null)
        {
            for(Entry<Integer, SimpleSpectroscopySource> entry : sources.entrySet())
            {
                Integer key = entry.getKey();
                SimpleSpectroscopySource source = entry.getValue();
                List<String> identifiers = model.getIdentifiers(key);
                List<Channel1D> channels = new ArrayList<>(source.getChannels(identifiers));
                map.put(source, channels);
            }
        }

        return map;   
    }



}


