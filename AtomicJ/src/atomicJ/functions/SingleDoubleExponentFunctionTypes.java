package atomicJ.functions;

import atomicJ.functions.BareQuadratic.BareQuadraticExactFitFactory;
import atomicJ.functions.Constant.ConstantExactFitFactory;
import atomicJ.functions.InterceptlessLine.InterceptlessLineExactFitFactory;
import atomicJ.functions.PowerFunction.PowerFunctionExactFitFactory;

public enum SingleDoubleExponentFunctionTypes
{
    INTERCEPTLESS_LINE 
    {
        @Override
        public boolean accepts(double exp) 
        {
            boolean accepts = (exp == 1);
            return accepts;
        }

        @Override
        public ExactFitFactory getExactFitFactory(double exp) 
        {
            return InterceptlessLineExactFitFactory.getInstance();
        }
    },

    BARE_QUADRATIC 
    {
        @Override
        public boolean accepts(double exp) 
        {
            boolean accepts = (exp == 2);
            return accepts;
        }

        @Override
        public ExactFitFactory getExactFitFactory(double exp)
        {
            return BareQuadraticExactFitFactory.getInstance();
        }
    },

    POWER_FUNCTION 
    {
        @Override
        public boolean accepts(double exp) 
        {
            boolean accepts = exp > 0;
            return accepts;
        }

        @Override
        public ExactFitFactory getExactFitFactory(double exp)
        {           
            return PowerFunctionExactFitFactory.getInstance(exp);
        }
    },
    CONSTANT
    {
        @Override
        public boolean accepts(double deg) 
        {
            boolean accepts = (deg == 0);
            return accepts;
        }

        @Override
        public ExactFitFactory getExactFitFactory(double deg) 
        {
            return ConstantExactFitFactory.getInstance();
        }
    };

    public abstract boolean accepts(double deg);
    public abstract ExactFitFactory getExactFitFactory(double exp);

    public static ExactFitFactory findExactFitFactory(double exp)
    {
        for(SingleDoubleExponentFunctionTypes pf: SingleDoubleExponentFunctionTypes.values())
        {
            if(pf.accepts(exp))
            {
                return pf.getExactFitFactory(exp);
            }
        }

        throw new IllegalArgumentException("Unknown model for exponent " + exp);
    }
}