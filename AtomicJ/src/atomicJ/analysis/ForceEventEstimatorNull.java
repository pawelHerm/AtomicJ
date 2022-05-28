package atomicJ.analysis;

import java.util.Collections;
import java.util.List;

import atomicJ.data.Channel1DData;

public class ForceEventEstimatorNull implements ForceEventEstimator
{
    private static final ForceEventEstimatorNull INSTANCE = new ForceEventEstimatorNull();

    private ForceEventEstimatorNull()
    {}

    public static ForceEventEstimatorNull getInstance()
    {
        return INSTANCE;
    }

    @Override
    public List<ForceEventEstimate> getEventEstimates(Channel1DData approachBranch, Channel1DData withdrawBranch, double domainMin, double domainMax)
    {
        return Collections.emptyList();
    }
}
