package atomicJ.functions;

import atomicJ.functions.BareQuadratic.BareQuadraticExactFitFactory;
import atomicJ.functions.Constant.ConstantExactFitFactory;
import atomicJ.functions.IntegerPowerFunction.IntegerPowerFunctionExactFitFactory;
import atomicJ.functions.InterceptlessLine.InterceptlessLineExactFitFactory;

public enum SingleIntegerExponentFunctionTypes
{
    INTERCEPTLESS_LINE 
    {
        @Override
        public boolean accepts(int exp) 
        {
            boolean accepts = (exp == 1);
            return accepts;
        }

        @Override
        public ExactFitFactory getExactFitFactory(int exp) 
        {
            return InterceptlessLineExactFitFactory.getInstance();
        }
    },

    BARE_QUADRATIC 
    {
        @Override
        public boolean accepts(int exp) 
        {
            boolean accepts = (exp == 2);
            return accepts;
        }

        @Override
        public ExactFitFactory getExactFitFactory(int exp)
        {
            return BareQuadraticExactFitFactory.getInstance();
        }
    },

    INTEGER_POWER_FUNCTION 
    {
        @Override
        public boolean accepts(int exp) 
        {
            boolean accepts = exp > 0;
            return accepts;
        }

        @Override
        public ExactFitFactory getExactFitFactory(int exp)
        {           
            return IntegerPowerFunctionExactFitFactory.getInstance(exp);
        }
    },
    CONSTANT
    {
        @Override
        public boolean accepts(int deg) 
        {
            boolean accepts = (deg == 0);
            return accepts;
        }

        @Override
        public ExactFitFactory getExactFitFactory(int deg) 
        {
            return ConstantExactFitFactory.getInstance();
        }
    };

    public abstract boolean accepts(int deg);
    public abstract ExactFitFactory getExactFitFactory(int exp);

    public static ExactFitFactory findExactFitFactory(int exp)
    {
        for(SingleIntegerExponentFunctionTypes pf: SingleIntegerExponentFunctionTypes.values())
        {
            if(pf.accepts(exp))
            {
                return pf.getExactFitFactory(exp);
            }
        }

        throw new IllegalArgumentException("Unknown model for exponent " + exp);
    }
}