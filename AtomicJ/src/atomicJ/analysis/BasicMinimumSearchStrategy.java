package atomicJ.analysis;

import org.apache.commons.math3.analysis.UnivariateFunction;

public enum BasicMinimumSearchStrategy implements MinimumSearchStrategy
{
    EXHAUSTIVE ("Exhaustive") 
    {
        @Override
        public double getMinimum(UnivariateFunction f, double start, double end) 
        {
            ExhaustiveIntegerSearch eis = new ExhaustiveIntegerSearch();
            return eis.getMinimum(f, start, end);
        }
    }, 
    FOCUSED_GRID("Focused grid") 
    {
        @Override
        public double getMinimum(UnivariateFunction f, double start, double end) 
        {
            FocusedGridIntegerSearch eis = new FocusedGridIntegerSearch();
            return eis.getMinimum(f, start, end);
        }
    }, 
    GOLDEN_SECTION("Golden section") 
    {
        @Override
        public double getMinimum(UnivariateFunction f, double start, double end) 
        {
            GoldenSectionSearch gss = new GoldenSectionSearch(f);
            return gss.goldenSectionSearch(start, start + 0.5*(end - start), end, .5);
        }
    };

    private final String prettyName;

    BasicMinimumSearchStrategy(String prettyName)
    {
        this.prettyName = prettyName;
    }

    @Override
    public String toString()
    {
        return prettyName;
    }

    @Override
    public abstract double getMinimum(UnivariateFunction f, double start, double end);

}
