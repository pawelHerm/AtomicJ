package atomicJ.analysis;

import atomicJ.analysis.indentation.HyperelasticFungSphere.FungSphereFit;
import atomicJ.analysis.indentation.ContactModelFit;
import atomicJ.data.units.DimensionlessQuantity;
import atomicJ.data.units.Quantity;

public class FungExponentProcessedPackFunction implements ProcessedPackFunction<ProcessedSpectroscopyPack>
{
    private static final FungExponentProcessedPackFunction INSTANCE = new FungExponentProcessedPackFunction();
    private static final Quantity EVALUATED_QUANTITY = new DimensionlessQuantity("Fung's exponent");

    private FungExponentProcessedPackFunction() {
    }

    public static FungExponentProcessedPackFunction getInstance()
    {
        return INSTANCE;
    }


    @Override
    public double evaluate(ProcessedSpectroscopyPack pack) 
    {
        double val = Double.NaN;

        ContactModelFit<?> modelFit = pack.getModelFit();

        if(modelFit instanceof FungSphereFit)
        {
            val = ((FungSphereFit) modelFit).getFungExponent();
        }

        return val;
    }

    @Override
    public Quantity getEvaluatedQuantity() {
        return EVALUATED_QUANTITY;
    }

}
