package atomicJ.analysis.indentation;

import org.apache.commons.math3.analysis.UnivariateFunction;

public enum TauLambdaDependenceBondedIncompressible implements UnivariateFunction, PoissonRatioDependentFunctionSource
{
    DEGREE_ONE 
    {
        @Override
        public double value(double reducedIndent)
        {
            if(reducedIndent > 9.514364454222584)//tan(84 Degree)
            {
                double tau = -1.1953818158766758 + 1.4907652211827471*reducedIndent;
                return tau;
            }

            double reducedIndent2 = reducedIndent*reducedIndent;

            double tau = reducedIndent > 0.9656887748070739 /*tan(44 Degree)*/ ? -0.17382727426763853 + 1.0347255570372615*reducedIndent + 0.09063867415207108*reducedIndent2 - 0.00881201985623603*reducedIndent2*reducedIndent + 0.0003313077679726521*reducedIndent2*reducedIndent2: 
                reducedIndent*(2./Math.PI + 0.22074106344661595*reducedIndent + 0.23103915038581332*reducedIndent2 - 0.11329617027396305*reducedIndent2*reducedIndent - 0.033153615675188514*reducedIndent2*reducedIndent2);      

            return tau;
        }
    },

    DEGREE_TWO 
    {
        @Override
        public double value(double reducedIndent)
        {
            double reducedIndentRoot = Math.sqrt(reducedIndent);

            double tau = reducedIndent > 1.5 ? (reducedIndent > 40 ? -1.1064882794679092 + 1.9829308743846004*reducedIndentRoot : -0.2241056593488353 + 1.552311731822475*reducedIndentRoot + 0.07887873832556244*reducedIndent - 0.00519697358423013*reducedIndent*reducedIndentRoot) : 
                reducedIndentRoot*(1 + 0.3592980145040053*reducedIndentRoot + 0.5556434843733393*reducedIndent - 0.7889887753916968*reducedIndent*reducedIndentRoot + 0.28291558877834333*reducedIndent*reducedIndent);      

            return tau;
        }
    };        


    @Override
    public UnivariateFunction getFunction(double poissonRatio)
    {
        return this;
    }
}