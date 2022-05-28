package atomicJ.statistics;

public enum LocalRegressionWeightFunction 
{
    TRICUBE("Tricube") 
    {
        @Override
        public double value(double x) 
        {
            double absX = Math.abs(x);
            if (absX >= 1.0) {
                return 0.0;
            }
            double tmp = 1 - absX * absX * absX;
            return tmp * tmp * tmp;
        }
    }, 

    BISQUARE("Bisquare") 
    {
        @Override
        public double value(double x) 
        {
            double xSq = x * x;

            if (xSq >= 1.0) {
                return 0.0;
            }

            double tmp = 1 - xSq;
            return tmp * tmp;
        }
    }, 

    EPANCHENIKOV("Epanchenikov") 
    {
        @Override
        public double value(double x)
        {
            return 1 - x*x;
        }
    },

    UNIFORM("Uniform")
    {
        @Override
        public double value(double x) 
        {
            return  1;
        }

    };

    private final String label;

    LocalRegressionWeightFunction(String name)
    {
        this.label = name;
    }

    public abstract double value(double x);

    public double[] getWeights(int ml, int mp) 
    {        
        return getWeights2(-ml, ml + mp + 1);
    }

    public double[] getWeights2(int min, int n) 
    {
        double[] weights = new double[n];
        double denom = 1./Math.max(Math.abs(min), Math.abs(min + n));

        for(int i = 0; i < n; i++)
        {
            weights[i] = value((min + i)*denom);
        }

        return weights;
    }

    @Override
    public String toString()
    {
        return label;
    }

    public static LocalRegressionWeightFunction getValue(String identifier)
    {
        return getValue(identifier, null);
    }

    public static LocalRegressionWeightFunction getValue(String identifier, LocalRegressionWeightFunction fallBackValue)
    {
        LocalRegressionWeightFunction estimator = fallBackValue;

        if(identifier != null)
        {
            for(LocalRegressionWeightFunction est : LocalRegressionWeightFunction.values())
            {
                String estIdentifier = est.getIdentifier();
                if(estIdentifier.equals(identifier))
                {
                    estimator = est;
                    break;
                }
            }
        }

        return estimator;
    }

    public static boolean canBeParsed(String identifier)
    {
        boolean canBeParsed = false;

        if(identifier != null)
        {
            for(LocalRegressionWeightFunction est : LocalRegressionWeightFunction.values())
            {
                String estIdentifier = est.getIdentifier();
                if(estIdentifier.equals(identifier))
                {
                    canBeParsed = true;
                    break;
                }
            }
        }

        return canBeParsed;
    }

    public String getIdentifier()
    {
        return label;
    }
}
