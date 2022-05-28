package atomicJ.gui.curveProcessing;

import java.util.Map;

import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;
import atomicJ.sources.IdentityTag;

public interface CurveAveragingHandler <E extends Processed1DPack<E, ?>>
{
    public void handleAveragingRequest(Map<IdentityTag, Batch<E>> curves);
}
