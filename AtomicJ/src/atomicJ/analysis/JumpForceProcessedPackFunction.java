package atomicJ.analysis;

import atomicJ.data.units.Quantity;
import atomicJ.data.units.UnitQuantity;
import atomicJ.data.units.Units;

public class JumpForceProcessedPackFunction implements ProcessedPackFunction<ProcessedSpectroscopyPack>
{
    private final int estimateIndex;
    private final Quantity evaluatedQuantity; 

    JumpForceProcessedPackFunction(int estimateIndex) 
    {
        this.estimateIndex = estimateIndex;              
        this.evaluatedQuantity = new UnitQuantity("Jump force " + Integer.toString(estimateIndex + 1), Units.NANO_NEWTON_UNIT);
    }

    @Override
    public double evaluate(ProcessedSpectroscopyPack pack) 
    {
        double val = Double.NaN;

        if(estimateIndex < pack.getJumpForceEstimateCount())
        {
            ForceEventEstimate forceEstimate = pack.getJumpEstimate(estimateIndex);
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
