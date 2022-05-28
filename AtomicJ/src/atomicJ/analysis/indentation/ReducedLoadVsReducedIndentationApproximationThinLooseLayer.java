package atomicJ.analysis.indentation;

import org.apache.commons.math3.analysis.UnivariateFunction;

public enum ReducedLoadVsReducedIndentationApproximationThinLooseLayer implements UnivariateFunction, PoissonRatioDependentFunctionSource
{
    CONE 
    {
        @Override
        public double value(double reducedIndent) 
        {
            double reducedIndent2 = reducedIndent*reducedIndent;
            double reducedIndent3 = reducedIndent*reducedIndent2;
            double reducedLoad = reducedIndent < 1 ? (2/Math.PI)*reducedIndent2*(1 + 0.4609328524178679*reducedIndent + 0.34583879607230666*reducedIndent2 + 0.04839052498320088*reducedIndent3):
                (reducedIndent < 5 ? 0.08585462637137942 + 0.10333228383696946*reducedIndent - 0.06471205494467767*reducedIndent2 + 1.0565170696173116*reducedIndent3 
                        : Math.PI*reducedIndent3/3.);

            return reducedLoad;    
        }
    }, PARABOLOID 
    {
        @Override
        public double value(double reducedIndent) 
        {
            double reducedIndentRoot = Math.sqrt(reducedIndent);
            double reducedIndentSesqui = reducedIndent*reducedIndentRoot;

            double reducedLoad = reducedIndent < 0.5 ? (4./3.)*reducedIndentSesqui*(1 + 0.7219609439372858*reducedIndentRoot + 0.8221454802420807*reducedIndent):
                (reducedIndent < 450 ? -0.06327893614029767 + 0.2602586469814878*reducedIndentRoot+ Math.PI*reducedIndent*reducedIndent : Math.PI*reducedIndent*reducedIndent);

            return reducedLoad;
        }

    };  

    @Override
    public UnivariateFunction getFunction(double poissonRatio)
    {
        return this;
    }
}