package atomicJ.curveProcessing;

public enum SpanType
{
    POINT_COUNT("no of points") 
    {
        @Override
        public int correctPolynomialDegree(double span, int degree)
        {
            return Math.max(0, Math.min((int)Math.rint(span) - 1, degree));
        }

        @Override
        public double correctSpanValue(double span) 
        {
            return Math.max(1, Math.rint(span));
        }

        @Override
        public double correctSpanValue(double span, int degree)
        {
            return Math.max(Math.rint(span), degree + 1);
        }

        @Override
        public boolean isPolynomialDegreeAceptable(double span, int degree)
        {
            boolean acceptable = degree < Math.rint(span) && degree >= 0;
            return acceptable;
        }

        @Override
        public boolean isSpanValueAcceptable(double span) 
        {
            int pointCount = (int)Math.rint(span);
            boolean acceptable = (pointCount == span) && pointCount > 0;
            return acceptable;
        }

        @Override
        public boolean isSpanValueAcceptable(double span, double degree)
        {
            int pointCount = (int)Math.rint(span);
            boolean acceptable = (pointCount == span) && pointCount > degree;
            return acceptable;
        }

        @Override
        public int getSpanLengthInPoints(double span, int pointCount)
        {
            return (int)Math.rint(span);
        }

    },

    POINT_FRACTION("% of points") 
    {
        @Override
        public int correctPolynomialDegree(double span, int degree) 
        {
            return  Math.max(0, degree);
        }

        @Override
        public double correctSpanValue(double span)
        {
            return Math.max(0, span);
        }

        @Override
        public double correctSpanValue(double span, int degree)
        {
            return Math.max(0, span);
        }

        @Override
        public boolean isPolynomialDegreeAceptable(double span, int degree) 
        {
            boolean acceptable = degree >= 0;
            return acceptable;
        }

        @Override
        public boolean isSpanValueAcceptable(double span) 
        {
            boolean acceptable = span > 0;
            return acceptable;
        }

        @Override
        public boolean isSpanValueAcceptable(double span, double degree)
        {
            boolean acceptable = span > 0;
            return acceptable;
        }

        @Override
        public int getSpanLengthInPoints(double span, int pointCount) 
        {
            int n = (int)Math.rint(0.01*span*pointCount);
            return n;
        }
    };

    private final String label;

    SpanType(String label)
    {
        this.label = label;
    }

    public abstract int getSpanLengthInPoints(double span, int pointCount);

    public abstract boolean isPolynomialDegreeAceptable(double span, int degree);

    public abstract int correctPolynomialDegree(double span, int degree);

    public abstract boolean isSpanValueAcceptable(double span);

    public abstract boolean isSpanValueAcceptable(double span, double degree);

    public abstract double correctSpanValue(double span);

    public abstract double correctSpanValue(double span, int degree);

    public String getLabel()
    {
        return label;
    }

    @Override
    public String toString()
    {
        return label;
    }

    public static SpanType getValue(String identifier)
    {
        return getValue(identifier, null);
    }

    public static SpanType getValue(String identifier, SpanType fallBackValue)
    {
        SpanType estimator = fallBackValue;

        if(identifier != null)
        {
            for(SpanType est : SpanType.values())
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

    public String getIdentifier()
    {
        return name();
    }
}