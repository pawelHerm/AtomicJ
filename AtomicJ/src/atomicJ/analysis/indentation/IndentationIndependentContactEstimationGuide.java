package atomicJ.analysis.indentation;

import java.awt.geom.Point2D;

import atomicJ.analysis.ContactEstimationGuide;
import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.SequentialSearchAssistant;
import atomicJ.data.Channel1DData;
import atomicJ.data.IndexRange;

public class IndentationIndependentContactEstimationGuide implements ContactEstimationGuide
{

    private final PrecontactInteractionsModel precontactModel;
    private final int postcontactFitDegree;

    public IndentationIndependentContactEstimationGuide(PrecontactInteractionsModel precontactModel, int postcontactFitDegree)
    {
        this.precontactModel = precontactModel;
        this.postcontactFitDegree = postcontactFitDegree;
    }

    @Override
    public IndexRange getRangeOfValidTrialContactPointIndices(Channel1DData deflectionChannel, Point2D recordingPoint, double springConstant)
    {
        return new IndexRange(0, deflectionChannel.getItemCount() - 1);
    }

    @Override
    public SequentialSearchAssistant getSequentialSearchAssistant(Channel1DData deflectionCurveBranch, Point2D recordingPosition, double springConstant)
    {
        return new IndentationIndependentSearchAssistant(precontactModel, postcontactFitDegree, deflectionCurveBranch, recordingPosition);
    }
}
