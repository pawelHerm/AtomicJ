package atomicJ.analysis;

import atomicJ.data.Datasets;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.Units;

public class AdhesionForceProcessedPackFunction implements ProcessedPackFunction<ProcessedSpectroscopyPack>
{
    private final int estimateIndex;
    private final Quantity evaluatedQuantity; 

    AdhesionForceProcessedPackFunction(int estimateIndex) 
    {
        this.estimateIndex = estimateIndex;              
        this.evaluatedQuantity = (estimateIndex == 0) ? Quantities.ADHESION_FORCE_NANONEWTONS : new UnitQuantity(Datasets.ADHESION_FORCE + " " + Integer.toString(estimateIndex + 1), Units.NANO_NEWTON_UNIT);
    }

    @Override
    public double evaluate(ProcessedSpectroscopyPack pack) 
    {
        double val = Double.NaN;

        if(estimateIndex < pack.getAdhesionForceEstimateCount())
        {
            ForceEventEstimate forceEstimate = pack.getAdhesionEstimate(estimateIndex);
            val = forceEstimate.getForceMagnitude();
        }

        return val;
    }

    @Override
    public Quantity getEvaluatedQuantity() 
    {
        return evaluatedQuantity;
    }
}
