package atomicJ.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import atomicJ.data.Channel1DData;

public class ManualAdhesionForceEstimator implements ForceEventEstimator
{
    private final List<ForceEventEstimate> adhesionEstimates;

    public ManualAdhesionForceEstimator(List<ForceEventEstimate> adhesionEstimates)
    {
        this.adhesionEstimates = new ArrayList<>(adhesionEstimates);
    }

    @Override
    public List<ForceEventEstimate> getEventEstimates(Channel1DData approachBranch, Channel1DData withdrawBranch, double domainMin, double domainMax) 
    {
        return Collections.unmodifiableList(adhesionEstimates);
    }
}
