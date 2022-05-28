package atomicJ.analysis;

import atomicJ.analysis.indentation.AdhesiveContactModelFit;
import atomicJ.analysis.indentation.ContactModelFit;
import atomicJ.data.Quantities;
import atomicJ.data.units.Quantity;

public class AdhesionWorkProcessedPackFunction implements ProcessedPackFunction<ProcessedSpectroscopyPack>
{

    private static final AdhesionWorkProcessedPackFunction INSTANCE = new AdhesionWorkProcessedPackFunction();

    private AdhesionWorkProcessedPackFunction() {
    }

    public static AdhesionWorkProcessedPackFunction getInstance()
    {
        return INSTANCE;
    }


    @Override
    public double evaluate(ProcessedSpectroscopyPack pack) 
    {
        double val = Double.NaN;

        ContactModelFit<?> modelFit = pack.getModelFit();

        if(modelFit instanceof AdhesiveContactModelFit)
        {
            val = ((AdhesiveContactModelFit<?>) modelFit).getAdhesionWork();
        }

        return val;
    }

    @Override
    public Quantity getEvaluatedQuantity() {
        return Quantities.ADHESION_WORK_MILIJOULES;
    }

}
