package atomicJ.analysis.indentation;

import org.apache.commons.math3.analysis.UnivariateFunction;

public enum XiApproximationBondedIncompressibleLayer implements UnivariateFunction, PoissonRatioDependentFunctionSource
{
    XI_ZERO 
    {
        @Override
        public double value(double tau) 
        {
            double tau2 = tau*tau;
            double tau3 = tau*tau2;
            double xi0 = tau > 1.2 ?  -0.32200988000179354 + 1.9372155069019295*tau + 0.8542206369854419*tau2 + 0.1474038965378127*tau3:
                1.127294838473304*tau + 1.2534743855684407*tau2 + 0.7845575578931572*tau3 - 0.8066500581952372*tau2*tau2 + 0.25992549153472266*tau2*tau3;

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
            double xi0 = tau > 1.1 ?  -0.1491537744970472 + 1.712372863277364*tau + 0.725316400362848*tau2 + 0.10014025394169285*tau3 :
                1.1266745714308386*tau + 1.2742858704361761*tau2 + 0.44696986115029574*tau3 - 0.7721787647745494*tau2*tau2 + 0.3103005719474599*tau2*tau3;

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

            double xi2 = tau > 1.2 ? -0.049463774585330134 + 1.5972494791751466*tau + 0.6385113885724849*tau2 + 0.07384012861062446*tau3:  
                1.1262478390694162*tau + 1.2949883832304696*tau2  + 0.18732178957520357*tau3 - 0.6364554846834951*tau2*tau2 + 0.27907820937683936*tau2*tau3;

            return xi2;
        }
    };   

    @Override
    public UnivariateFunction getFunction(double poissonRatio)
    {
        return this;
    }
}