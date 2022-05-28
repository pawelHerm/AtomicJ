package atomicJ.analysis;

public class ForceCurveSimpleStorage
{
    private final double[][] approach;
    private final double[][] withdraw;

    public ForceCurveSimpleStorage(double[][] approach, double[][] withdraw)
    {
        this.approach = approach;
        this.withdraw = withdraw;
    }

    public static ForceCurveSimpleStorage getEmpty()
    {

        return new ForceCurveSimpleStorage(new double[][] {}, new double[][] {});
    }

    public double[][] getApproach()
    {
        return approach;
    }

    public double[][] getWithdraw()
    {
        return withdraw;
    }
}