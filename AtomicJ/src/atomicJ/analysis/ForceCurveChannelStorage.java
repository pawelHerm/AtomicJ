package atomicJ.analysis;

import atomicJ.data.Channel1DData;

public class ForceCurveChannelStorage
{
    private final Channel1DData approach;
    private final Channel1DData withdraw;

    public ForceCurveChannelStorage(Channel1DData approach, Channel1DData withdraw)
    {
        this.approach = approach;
        this.withdraw = withdraw;
    }

    public Channel1DData getApproach()
    {
        return approach;
    }

    public Channel1DData getWithdraw()
    {
        return withdraw;
    }
}