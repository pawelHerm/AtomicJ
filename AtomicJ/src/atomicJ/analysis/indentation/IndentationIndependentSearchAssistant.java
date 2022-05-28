package atomicJ.analysis.indentation;

import java.awt.geom.Point2D;

import atomicJ.analysis.PrecontactInteractionsModel;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.analysis.SequentialSearchAssistant;
import atomicJ.data.Channel1DData;

public class IndentationIndependentSearchAssistant implements SequentialSearchAssistant
{
    private final PrecontactInteractionsModel precontactModel;
    private final Point2D recordingPosition;
    private final double[] zValues;
    private final double[] deflectionValues;
    private final int postcontactFitDegree;

    public IndentationIndependentSearchAssistant(PrecontactInteractionsModel precontactModel, int postcontactFitDegree, Channel1DData curveBranch, Point2D recordingPosition)
    {
        this.precontactModel = precontactModel;
        this.zValues = curveBranch.getXCoordinates();
        this.deflectionValues = curveBranch.getYCoordinates();
        this.recordingPosition = recordingPosition;
        this.postcontactFitDegree = postcontactFitDegree;
    }

    private double[][] getDeltaZVersusDeltaD(double[] zValues, double[] dValues, double zContact, double dContact)
    {
        int n = zValues.length;
        double[] transformedZ = new double[n];
        double[] transformedD = new double[n];

        for(int i = 0; i<n; i++)
        {
            double z = zValues[i];
            double d = dValues[i];
            double delta_d = d - dContact;
            double delta_z = zContact - z;

            transformedZ[i] = delta_z;
            transformedD[i] = delta_d; 
        }       

        return new double[][] {transformedZ, transformedD};
    }

    @Override
    public double getObjectiveFunctionValue(RegressionStrategy precontactStrategy, RegressionStrategy postcontactStartegy, int contactIndex) 
    {
        int n = zValues.length;
        double zContact = zValues[contactIndex];
        double dContact = deflectionValues[contactIndex];

        double[][] deltaZVsDeltaD = getDeltaZVersusDeltaD(zValues,deflectionValues, zContact, dContact);

        double precontactOFMinimum = precontactModel.getPrecontactObjectiveFunctionMinimum(deltaZVsDeltaD[1], deltaZVsDeltaD[0], 0, contactIndex, recordingPosition, precontactStrategy);
        double postcontactOFMinimum = postcontactStartegy.getObjectiveFunctionMinimum(deltaZVsDeltaD[1], deltaZVsDeltaD[0], contactIndex, n, postcontactFitDegree, false);

        double result = precontactOFMinimum + postcontactOFMinimum;

        return result;
    }              
}