package atomicJ.analysis;

import atomicJ.data.Channel1DData;

public interface StiffnessEstimator 
{
    public double getEstimate(Channel1DData forceIndentationData);
}
