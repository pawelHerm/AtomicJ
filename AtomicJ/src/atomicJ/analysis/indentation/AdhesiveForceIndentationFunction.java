package atomicJ.analysis.indentation;

import atomicJ.functions.FittedUnivariateFunction;
import atomicJ.functions.ParametrizedUnivariateFunction;

public interface AdhesiveForceIndentationFunction extends ParametrizedUnivariateFunction, FittedUnivariateFunction
{
    public double getYoungModulus();
    public double getAdhesionWork();
    public double getAdhesionForce();
}
