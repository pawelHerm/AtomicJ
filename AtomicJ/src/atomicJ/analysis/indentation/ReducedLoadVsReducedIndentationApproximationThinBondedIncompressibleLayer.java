package atomicJ.analysis.indentation;

import org.apache.commons.math3.analysis.UnivariateFunction;

public enum ReducedLoadVsReducedIndentationApproximationThinBondedIncompressibleLayer implements UnivariateFunction, PoissonRatioDependentFunctionSource
{
    CONE 
    {
        @Override
        public double value(double reducedIndent) 
        {
            double reducedIndent2 = reducedIndent*reducedIndent;
            double reducedIndent3 = reducedIndent*reducedIndent2;
            double reducedLoad = reducedIndent < 0.9 ? (2/Math.PI)*reducedIndent2*(1 + 0.7146840723006598*reducedIndent 
                    + 0.608619653268436*reducedIndent2 + 0.7349912797855624*reducedIndent3):
                        -0.265012777942973 + 1.2249361235408487*reducedIndent - 1.6511380130174629*reducedIndent2
                        + 2.3319222760074463*reducedIndent3 + 0.29820586516496866*reducedIndent2*reducedIndent3;

            return reducedLoad;    
        }
    }, PARABOLOID 
    {
        @Override
        public double value(double reducedIndent) 
        {
            double reducedIndentRoot = Math.sqrt(reducedIndent);
            double reducedIndentSesqui = reducedIndent*reducedIndentRoot;
            double reducedIndent2 = reducedIndent*reducedIndent;

            double reducedLoad = reducedIndent < 0.4 ? (4./3.)*reducedIndentSesqui*(1 + 1.1053416390578608*reducedIndentRoot + 1.606749412034415*reducedIndent + 1.6018294436819807*reducedIndentSesqui):
                0.6157882175705465 - 3.1143156864367647*reducedIndentRoot + 6.692972504122468*reducedIndent - 7.1702772387228935*reducedIndentSesqui 
                + 8.227673152332168*reducedIndent2+(Math.PI/2.)*reducedIndent2*reducedIndent;

            return reducedLoad;
        }

    };  

    @Override
    public UnivariateFunction getFunction(double poissonRatio)
    {
        return this;
    }
}