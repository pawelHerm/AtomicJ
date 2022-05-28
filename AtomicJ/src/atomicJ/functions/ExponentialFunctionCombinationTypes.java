package atomicJ.functions;

import java.util.Arrays;

import atomicJ.functions.BareQuadratic.BareQuadraticExactFitFactory;
import atomicJ.functions.Constant.ConstantExactFitFactory;
import atomicJ.functions.InterceptlessLine.InterceptlessLineExactFitFactory;
import atomicJ.functions.InterceptlessQuadratic.InterceptlessQuadraticExactFitFactory;
import atomicJ.functions.Line.LineExactFitFactory;
import atomicJ.functions.Polynomial.PolynomialExactFitFactory;
import atomicJ.functions.PowerFunction.PowerFunctionExactFitFactory;
import atomicJ.functions.PowerFunctionCombination.PowerFunctionCombinationExactFitFactory;
import atomicJ.functions.PowerFunctionWithIntercept.PowerFunctionWithInterceptExactFitFactory;
import atomicJ.functions.Quadratic.QuadraticExactFitFactory;
import atomicJ.functions.SesquiPower.SesquiPowerExactFitFactory;
import atomicJ.functions.SesquiPowerFunctionWithIntercept.SesquiPowerFunctionWithInterceptExactFitFactory;

public enum ExponentialFunctionCombinationTypes
{
    BARE_QUADRATIC 
    {
        @Override
        public boolean accepts(double[] modelSorted) 
        {
            return BareQuadraticExactFitFactory.accepts(modelSorted);
        }

        @Override
        public ExactFitFactory getExactFitFactory(double[] modelSorted)
        {

            return BareQuadraticExactFitFactory.getInstance();
        }
    }, 
    SESQUIPOWER
    {
        @Override
        public boolean accepts(double[] modelSorted) 
        {
            return SesquiPowerExactFitFactory.accepts(modelSorted);
        }

        @Override
        public ExactFitFactory getExactFitFactory(double[] modelSorted) 
        {
            return SesquiPowerExactFitFactory.getInstance();
        }
    },
    SESQUIPOWER_WITH_INTERCEPTH
    {
        @Override
        public boolean accepts(double[] modelSorted) 
        {
            return SesquiPowerFunctionWithInterceptExactFitFactory.accepts(modelSorted);
        }

        @Override
        public ExactFitFactory getExactFitFactory(double[] modelSorted) 
        {
            return SesquiPowerFunctionWithInterceptExactFitFactory.getInstance();
        }
    },
    INTERCEPTLESS_LINE 
    {
        @Override
        public boolean accepts(double[] modelSorted) 
        {
            return InterceptlessLineExactFitFactory.accepts(modelSorted);
        }

        @Override
        public ExactFitFactory getExactFitFactory(double[] modelSorted) 
        {
            return InterceptlessLineExactFitFactory.getInstance();
        }
    },

    LINE 
    {
        @Override
        public boolean accepts(double[] modelSorted)
        {
            return LineExactFitFactory.accepts(modelSorted);
        }

        @Override
        public ExactFitFactory getExactFitFactory(double[] modelSorted)
        {
            return LineExactFitFactory.getInstance();
        }
    },
    INTERCEPTLESS_QUADRATIC 
    {
        @Override
        public boolean accepts(double[] modelSorted) 
        {
            return InterceptlessQuadraticExactFitFactory.accepts(modelSorted);
        }

        @Override
        public ExactFitFactory getExactFitFactory(double[] model)
        {
            return InterceptlessQuadraticExactFitFactory.getInstance();
        }
    },
    QUADRATIC {
        @Override
        public boolean accepts(double[] modelSorted) 
        {
            return QuadraticExactFitFactory.accepts(modelSorted);
        }

        @Override
        public ExactFitFactory getExactFitFactory(double[] model)
        {
            return QuadraticExactFitFactory.getInstance();
        }
    },
    CONSTANT
    {
        @Override
        public boolean accepts(double[] modelSorted) 
        {
            return ConstantExactFitFactory.accepts(modelSorted);
        }

        @Override
        public ExactFitFactory getExactFitFactory(double[] modelSorted) 
        {
            return ConstantExactFitFactory.getInstance();
        }
    },
    POWER_FUNCTION
    {
        @Override
        public boolean accepts(double[] modelSorted) 
        {
            return PowerFunctionExactFitFactory.accepts(modelSorted);
        }

        @Override
        public ExactFitFactory getExactFitFactory(double[] modelSorted) 
        {
            return PowerFunctionExactFitFactory.getInstance(modelSorted[0]);
        }
    },
    POWER_FUNCTION_WITH_INTERCEPT
    {
        @Override
        public boolean accepts(double[] modelSorted) 
        {
            return PowerFunctionWithInterceptExactFitFactory.accepts(modelSorted);
        }

        @Override
        public ExactFitFactory getExactFitFactory(double[] modelSorted) 
        {
            return PowerFunctionWithInterceptExactFitFactory.getInstance(modelSorted[1]);
        }
    },
    POLYNOMIAL 
    {
        @Override
        public boolean accepts(double[] modelSorted) 
        {
            return PolynomialExactFitFactory.accepts(modelSorted);
        }

        @Override
        public ExactFitFactory getExactFitFactory(double[] modelSorted)
        {
            return PolynomialExactFitFactory.getInstance(modelSorted);
        }
    },
    POWER_FUNCTION_COMBINATIN {
        @Override
        public boolean accepts(double[] model)
        {
            return true;
        }

        @Override
        public ExactFitFactory getExactFitFactory(double[] model) 
        {
            return PowerFunctionCombinationExactFitFactory.getInstance(model);
        }
    };

    public abstract boolean accepts(double[] model);
    public abstract ExactFitFactory getExactFitFactory(double[] model);

    public static ExactFitFactory findExactFitFactory(double[] model)
    {
        for(ExponentialFunctionCombinationTypes pf: ExponentialFunctionCombinationTypes.values())
        {
            if(pf.accepts(model))
            {
                return pf.getExactFitFactory(model);
            }
        }

        throw new IllegalArgumentException("Unknown model for the model " + Arrays.toString(model));

    }
}