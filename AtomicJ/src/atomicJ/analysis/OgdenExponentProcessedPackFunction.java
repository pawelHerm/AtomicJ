package atomicJ.analysis;

import atomicJ.analysis.indentation.OgdenIndentation.OgdenFit;
import atomicJ.analysis.indentation.ContactModelFit;
import atomicJ.data.units.DimensionlessQuantity;
import atomicJ.data.units.Quantity;

public class OgdenExponentProcessedPackFunction implements ProcessedPackFunction<ProcessedSpectroscopyPack>
{
    private static final OgdenExponentProcessedPackFunction INSTANCE = new OgdenExponentProcessedPackFunction();
    private static final Quantity EVALUATED_QUANTITY = new DimensionlessQuantity("Ogden's exponent");

    private OgdenExponentProcessedPackFunction() {
    }

    public static OgdenExponentProcessedPackFunction getInstance()
    {
        return INSTANCE;
    }

    @Override
    public double evaluate(ProcessedSpectroscopyPack pack) 
    {
        double val = Double.NaN;

        ContactModelFit<?> modelFit = pack.getModelFit();

        if(modelFit instanceof OgdenFit)
        {
            val = ((OgdenFit) modelFit).getOgdenExponent();
        }

        return val;
    }

    @Override
    public Quantity getEvaluatedQuantity() {
        return EVALUATED_QUANTITY;
    }

}
