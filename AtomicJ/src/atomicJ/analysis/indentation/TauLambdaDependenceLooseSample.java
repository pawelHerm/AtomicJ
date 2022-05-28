package atomicJ.analysis.indentation;

import org.apache.commons.math3.analysis.UnivariateFunction;

public enum TauLambdaDependenceLooseSample implements UnivariateFunction, PoissonRatioDependentFunctionSource
{
    DEGREE_ONE
    {
        @Override
        public double value(double reducedIndent)
        {
            if(reducedIndent > 1.880726465346332)
            {
                return -0.18274923623497788 + 1.0008015634197245*reducedIndent;
            }

            double reducedIndent2 = reducedIndent*reducedIndent;
            double tau = reducedIndent*(2/Math.PI + 0.14288516677843738*reducedIndent + 0.12578389701471404*reducedIndent2 - 
                    0.11022646350015611*reducedIndent2*reducedIndent + 0.02281739739595173*reducedIndent2*reducedIndent2);
            return tau;
        }
    },
    DEGREE_TWO  
    {
        @Override
        public double value(double reducedIndent)
        {
            double reducedIndent2Root = Math.sqrt(reducedIndent);

            double tau = reducedIndent > 2 ? -0.1717575870686137 + 1.4132970673629544*reducedIndent2Root : 
                reducedIndent2Root*(1 + 0.24367696437039466*reducedIndent2Root + 0.21180726851593626*reducedIndent - 0.30291747387618695*reducedIndent*reducedIndent2Root + 0.09544533719040021*reducedIndent*reducedIndent);

            return tau;
        }
    };     

    @Override
    public UnivariateFunction getFunction(double poissonRatio)
    {
        return this;
    }
}