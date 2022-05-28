package atomicJ.analysis;

import atomicJ.data.units.DimensionlessQuantity;
import atomicJ.data.units.Quantity;

public class CoefficientOfDeterminationProcessedPackFunction implements ProcessedPackFunction<ProcessedSpectroscopyPack>
{
    private static final CoefficientOfDeterminationProcessedPackFunction INSTANCE = new CoefficientOfDeterminationProcessedPackFunction();
    private static final Quantity EVALUATED_QUANTITY = new DimensionlessQuantity("R Squared"); 

    private CoefficientOfDeterminationProcessedPackFunction()
    {}

    public static CoefficientOfDeterminationProcessedPackFunction getInstance()
    {
        return INSTANCE;
    }

    @Override
    public double evaluate(ProcessedSpectroscopyPack pack)
    {        
        return pack.getResults().getRSquared();
    }

    @Override
    public Quantity getEvaluatedQuantity() {
        return EVALUATED_QUANTITY;
    }

}
