package atomicJ.analysis;

import java.awt.geom.Point2D;

import atomicJ.data.Channel1DData;
import atomicJ.data.IndexRange;

public interface ContactEstimationGuide 
{
    public IndexRange getRangeOfValidTrialContactPointIndices(Channel1DData deflectionChannel, Point2D recordingPoint, double springConstant);
    public SequentialSearchAssistant getSequentialSearchAssistant(Channel1DData deflectionCurveBranch, Point2D recordingPosition, double springConstant);
}
