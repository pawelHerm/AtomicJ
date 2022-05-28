package atomicJ.analysis.indentation;

import org.apache.commons.math3.analysis.UnivariateFunction;

public enum XiApproximationLooseLayer implements UnivariateFunction, PoissonRatioDependentFunctionSource
{
    XI_ZERO 
    {
        @Override
        public double value(double tau) 
        {
            double tau2 = tau*tau;
            double tau3 = tau*tau2;
            double xi0 = tau > 1.65 ?  -0.3838553953270864 + 1.553893809221951*tau + 0.00138818040746098*tau2 - 0.00003107802923772486*tau3 :
                0.7426462444663599*tau + 0.5708600467411756*tau2 + 0.008619952995014581*tau3 - 0.1799716509234812*tau2*tau2 + 0.0574929480459991*tau2*tau3;

            return xi0;    
        }
    }, XI_ONE 
    {
        @Override
        public double value(double tau) 
        {
            double tau2 = tau*tau;
            double tau3 = tau*tau2;

            double xi1 = tau > 1.5 ? -0.19780576303746483 + 1.3118905104788365*tau + 0.0015862058615164192*tau2 - 0.000033687593408216816*tau3 :
                0.7426240927442844*tau + 0.5755848412464301*tau2 - 0.1200162826590152*tau3 - 0.13429521229209693*tau2*tau2 + 0.05960273238470134*tau2*tau3 ;

            return xi1;
        }

    }, XI_TWO 
    {
        @Override
        public double value(double tau) 
        {
            double tau2 = tau*tau;
            double tau3 = tau*tau2;

            double xi2 = tau > 1.4 ? -0.0804743668472124 + 1.1664930087790777*tau + 0.0005220048502676038*tau2 - 6.589791847112107e-6 *tau3 : 
                0.7426372621229123*tau + 0.5767373024588222*tau2 - 0.18821252518221146*tau3 - 0.11841780506609963*tau2*tau2 + 0.06607373434562053*tau2*tau3;

            return xi2;
        }
    };  

    @Override
    public UnivariateFunction getFunction(double poissonRatio)
    {
        return this;
    }
}