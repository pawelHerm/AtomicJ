package atomicJ.functions;

import atomicJ.functions.Constant.ConstantExactFitFactory;
import atomicJ.functions.InterceptlessLine.InterceptlessLineExactFitFactory;
import atomicJ.functions.InterceptlessQuadratic.InterceptlessQuadraticExactFitFactory;
import atomicJ.functions.Line.LineExactFitFactory;
import atomicJ.functions.Polynomial.PolynomialExactFitFactory;
import atomicJ.functions.Quadratic.QuadraticExactFitFactory;

public enum PolynomialFunctionTypes
{
    INTERCEPTLESS_LINE 
    {
        @Override
        public boolean accepts(int deg, boolean constant) 
        {
            return InterceptlessLineExactFitFactory.accepts(deg, constant);
        }

        @Override
        public ExactFitFactory getExactFitFactory(int deg, boolean constant) 
        {
            return InterceptlessLineExactFitFactory.getInstance();
        }
    },
    LINE 
    {
        @Override
        public boolean accepts(int deg, boolean constant)
        {
            return LineExactFitFactory.accepts(deg, constant);
        }

        @Override
        public ExactFitFactory getExactFitFactory(int deg, boolean constant)
        {
            return LineExactFitFactory.getInstance();
        }
    },
    INTERCEPTLESS_QUADRATIC 
    {
        @Override
        public boolean accepts(int deg, boolean constant) 
        {
            return InterceptlessQuadraticExactFitFactory.accepts(deg, constant);
        }

        @Override
        public ExactFitFactory getExactFitFactory(int deg, boolean constant)
        {
            return InterceptlessQuadraticExactFitFactory.getInstance();
        }
    },
    QUADRATIC 
    {
        @Override
        public boolean accepts(int deg, boolean constant) 
        {
            return QuadraticExactFitFactory.accepts(deg, constant);
        }

        @Override
        public ExactFitFactory getExactFitFactory(int deg, boolean constant)
        {           
            return QuadraticExactFitFactory.getInstance();
        }
    },
    CONSTANT
    {
        @Override
        public boolean accepts(int deg, boolean constant) 
        {
            return ConstantExactFitFactory.accepts(deg, constant);
        }

        @Override
        public ExactFitFactory getExactFitFactory(int deg, boolean constant) 
        {
            return ConstantExactFitFactory.getInstance();
        }
    },
    POLYNOMIAL 
    {
        @Override
        public boolean accepts(int deg, boolean constant) 
        {
            return PolynomialExactFitFactory.accepts(deg, constant);
        }

        @Override
        public ExactFitFactory getExactFitFactory(int deg, boolean constant)
        {        
            return PolynomialExactFitFactory.getInstance(deg, constant);
        }
    };

    public abstract boolean accepts(int deg, boolean constant);
    public abstract ExactFitFactory getExactFitFactory(int deg, boolean constant);

    public static ExactFitFactory findExactFitFactory(int deg, boolean constant)
    {
        for(PolynomialFunctionTypes pf: PolynomialFunctionTypes.values())
        {
            if(pf.accepts(deg, constant))
            {
                return pf.getExactFitFactory(deg, constant);
            }
        }

        throw new IllegalArgumentException("Unknown model for degree " + deg + " and constant " + constant);
    }
}