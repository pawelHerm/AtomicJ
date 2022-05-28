package atomicJ.statistics;

public enum JICPenalty 
{
    SMALL("Small")
    {
        @Override
        public double getAdjustmentFactor(int n, double h) 
        {
            double logn = Math.log(n);
            double penalty = Math.sqrt(h*h*logn*logn);

            return penalty;
        }
    }, 

    MODERATE("Moderate")
    {
        @Override
        public double getAdjustmentFactor(int n, double h) 
        {
            double penalty = Math.sqrt(n*h*Math.log(n));

            return penalty;
        }
    }, 

    LARGE("Large") 
    {
        @Override
        public double getAdjustmentFactor(int n, double h)
        {
            double penalty = Math.sqrt(n*h)*Math.log(n);

            return penalty;
        }
    };

    private final String prettyName;

    JICPenalty(String prettyName)
    {
        this.prettyName = prettyName;
    }

    @Override
    public String toString()
    {
        return prettyName;
    }

    public abstract double getAdjustmentFactor(int n, double h);
}
