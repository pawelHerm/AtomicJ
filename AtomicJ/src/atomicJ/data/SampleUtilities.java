package atomicJ.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SampleUtilities 
{
    public static Map<String, Map<String, QuantitativeSample>> getIncludedSamples(List<SampleCollection> sampleCollections)
    {
        Map<String, Map<String, QuantitativeSample>> allQuantitativeSamples = new LinkedHashMap<>();

        for(SampleCollection sampleCollection : sampleCollections)
        {
            Map<String, QuantitativeSample> includedSamples = sampleCollection.getIncludedSamples();
            String collectionName = sampleCollection.getName();

            allQuantitativeSamples.put(collectionName, includedSamples);
        }

        return allQuantitativeSamples;
    }

    public static Map<String, Map<String, QuantitativeSample>> extractSamples(List<SampleCollection> sampleCollections)
    {
        Map<String, Map<String, QuantitativeSample>> samples = new LinkedHashMap<>();

        for(SampleCollection collection : sampleCollections)
        {
            String collectionName = collection.getName();
            Map<String, QuantitativeSample> collectionSamples = collection.getAllSamples();

            samples.put(collectionName, collectionSamples);
        }

        return samples;
    }
}
