package atomicJ.analysis;

import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;

public class AdhesionForceMainProcessedPackFunction implements ProcessedPackFunction<ProcessedSpectroscopyPack>
{
    private static final AdhesionForceMainProcessedPackFunction INSTANCE = new AdhesionForceMainProcessedPackFunction();

    public static AdhesionForceMainProcessedPackFunction getInstance()
    {
        return INSTANCE;
    }

    private AdhesionForceMainProcessedPackFunction(){};

    @Override
    public double evaluate(ProcessedSpectroscopyPack pack) 
    {
        if(pack == null)
        {
            return Double.NaN;
        }

        NumericalSpectroscopyProcessingResults results = pack.getResults();
        double adhesionForce = results.getAdhesionForce();
        return adhesionForce;
    }

    @Override
    public Quantity getEvaluatedQuantity() 
    {
        return Quantities.ADHESION_FORCE_NANONEWTONS;
    }
}
