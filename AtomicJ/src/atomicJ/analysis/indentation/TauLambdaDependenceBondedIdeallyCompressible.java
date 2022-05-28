package atomicJ.analysis.indentation;

import org.apache.commons.math3.analysis.UnivariateFunction;

public enum TauLambdaDependenceBondedIdeallyCompressible implements UnivariateFunction, PoissonRatioDependentFunctionSource
{
    DEGREE_ONE 
    {
        @Override
        public double value(double lambda)
        {
            if(lambda > 1.5)
            {
                double tau = -0.18717411651626933 + 1.0004215294606353*lambda;
                return tau;
            }

            double lambda2 = lambda*lambda;

            double tau = lambda*(2/Math.PI + 0.14982670914020374*lambda + 0.1274991250593938*lambda2 - 0.1202020552866834*lambda2*lambda + 0.02615296093341243*lambda2*lambda2);         
            return tau;
        }
    },

    DEGREE_TWO 
    {
        @Override
        public double value(double lambda)
        {
            double lambdaRoot = Math.sqrt(lambda);

            double tau = lambda > 1.96 ? -0.1745639314629399 + 1.4127383104725935*lambdaRoot: 
                lambdaRoot*(1 + 0.2559239820724107*lambdaRoot + 0.20465882312203648*lambda - 0.3190623337445563*lambda*lambdaRoot + 0.10575847281886055*lambda*lambda);      

            return tau;
        }
    };  


    @Override
    public UnivariateFunction getFunction(double poissonRatio)
    {
        return this;
    }
}