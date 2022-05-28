package atomicJ.analysis.indentation;

import org.apache.commons.math3.analysis.UnivariateFunction;

public enum XiApproximationBondedIdeallyCompressibleLayer implements UnivariateFunction, PoissonRatioDependentFunctionSource
{
    XI_ZERO 
    {
        @Override
        public double value(double tau) 
        {
            double tau2 = tau*tau;
            double tau3 = tau*tau2;
            double xi0 = tau > 1.5 ?  -0.33294442393732854 + 1.5342411518628014*tau + 0.005042851886799859*tau2 - 0.000260182832698187*tau3 + 4.362991470005079e-6*tau2*tau2:
                0.770308111530346*tau + 0.6165420289577782*tau2 - 0.0384621101679078*tau3 - 0.19394949807916162*tau2*tau2 + 0.07260749644777371*tau2*tau3;

            return xi0;    
        }
    }, 
    XI_ONE 
    {
        @Override
        public double value(double tau) 
        {
            double tau2 = tau*tau;
            double tau3 = tau*tau2;
            double xi0 = tau > 1.39 ?  -0.17612081142870384 + 1.312417670373447*tau + 0.0015676502590688147*tau2 - 0.00003361130600450031*tau3
                    : 0.7702490680055991*tau + 0.6226361055276388*tau2 -  0.19569536328754805*tau3 - 0.12105555771843761*tau2*tau2 + 0.06899595334998736*tau3*tau2;

            return xi0;    
        }
    }, 

    XI_TWO 
    {
        @Override
        public double value(double tau) 
        {
            double tau2 = tau*tau;
            double tau3 = tau*tau2;

            double xi2 = tau > 1.2 ? -0.07211840687188241 + 1.1732036628962754*tau - 0.00003833865369068702*tau2 + 5.573593812292691e-6*tau3:
                0.7704482249489791*tau + 0.6174649711691794*tau2 - 0.24374119925429605*tau3 - 0.1478398479755551*tau2*tau2 + 0.10033096860570354*tau2*tau3;

            return xi2;
        }
    };   

    @Override
    public UnivariateFunction getFunction(double poissonRatio)
    {
        return this;
    }
}