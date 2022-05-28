package atomicJ.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import atomicJ.sources.Channel1DSource;
import atomicJ.sources.IdentityTag;
import atomicJ.utilities.IOUtilities;
import atomicJ.utilities.Validation;

public interface Processed1DPack <E extends Processed1DPack<E,S>,S extends Channel1DSource<?>>
{
    public S getSource();
    public void setBatchIdTag(IdentityTag batchId);
    public IdentityTag getBatchIdTag();
    public List<? extends ProcessedPackFunction<? super E>> getSpecialFunctions();

    public static File getDefaultOutputFile(List<? extends Processed1DPack<?,?>> packs)
    {
        List<File> outputLocations = new ArrayList<>();

        for(Processed1DPack<?,?> pack: packs)
        {
            File location = pack.getSource().getDefaultOutputLocation();
            outputLocations.add(location);
        }

        File commonDirectory = IOUtilities.findLastCommonDirectory(outputLocations);
        return commonDirectory;
    }

    public static <E extends Processed1DPack<?,?>> double[]  getValuesForPacks(List<E> packs, ProcessedPackFunction<? super E> function)
    {	
        Validation.requireNonNullParameterName(packs, "packs");
        Validation.requireNonNullParameterName(function, "function");

        int k = packs.size();
        double[] values = new double[k];
        for(int j = 0; j<k; j++)
        {
            E pack = packs.get(j);
            values[j] = function.evaluate(pack);
        }
        return values;
    }
}
