
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.analysis.indentation;

import java.util.Arrays;

import atomicJ.analysis.SortedArrayOrder;
import atomicJ.data.Channel1DData;
import atomicJ.data.FlexibleFlatChannel1DData;
import atomicJ.data.Point1DData;
import atomicJ.data.Quantities;
import atomicJ.utilities.ArrayUtilities;

public class AdhesiveAnnotatedForceIndentation implements AnnotatedForceIndentation
{
    private final double[] indentationValues;
    private final double[] forceValues;

    private final double contactPointZ;
    private final double contactPointD;

    private final double adhesionForce;
    private final Point1DData deflectionIndentPullOff;
    private final Point1DData maxForceIndentationPoint;

    private final double baselineDeflection;

    private final double deflectionForceConversion;

    private final int lowerFitLimitIndex;
    private final int upperFitLimitIndex;
    private final boolean fitAllIndentationData;

    public AdhesiveAnnotatedForceIndentation(double[] forceIndentationXs, double[] forceIndentationYs, int lowerFitLimitIndex, int upperFitLimitIndex, double[] deflectionContactPoint, double adhesionForce, double[] deflectionPullOffPoint,  double baselineDeflection, double deflectionForceConversion)
    {
        this.indentationValues = forceIndentationXs;
        this.forceValues = forceIndentationYs;
        this.lowerFitLimitIndex = lowerFitLimitIndex;
        this.upperFitLimitIndex = upperFitLimitIndex;
        this.fitAllIndentationData = (lowerFitLimitIndex <= 0) && (upperFitLimitIndex >= forceIndentationXs.length);

        this.contactPointZ = deflectionContactPoint[0];
        this.contactPointD = deflectionContactPoint[1];

        this.deflectionIndentPullOff = new Point1DData(deflectionPullOffPoint[0], deflectionPullOffPoint[1], Quantities.INDENTATION_MICRONS, Quantities.DEFLECTION_MICRONS);
        this.adhesionForce = adhesionForce;

        int maxIndentationIndex = ArrayUtilities.getMaximumIndex(forceIndentationXs);
        this.maxForceIndentationPoint = new Point1DData(forceIndentationXs[maxIndentationIndex], forceIndentationYs[maxIndentationIndex], Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS);
        this.baselineDeflection = baselineDeflection;
        this.deflectionForceConversion = deflectionForceConversion;       
    }    

    @Override
    public double[] getForceValues()
    {
        return forceValues;
    }

    @Override
    public double[] getFittableForceValues()
    {
        double[] fittableForceValues = fitAllIndentationData ? forceValues : Arrays.copyOfRange(forceValues, lowerFitLimitIndex, upperFitLimitIndex);
        return fittableForceValues;
    }

    @Override
    public double[] getFittableForceValuesCopy()
    {
        double[] fittableForceValues = fitAllIndentationData ? Arrays.copyOf(forceValues, forceValues.length) : Arrays.copyOfRange(forceValues, lowerFitLimitIndex, upperFitLimitIndex);
        return fittableForceValues;
    }   

    @Override
    public double[] getIndentationValues()
    {
        return indentationValues;
    }

    @Override
    public double[] getFittableIndentationValues()
    {
        double[] fittableIndentation = fitAllIndentationData ? indentationValues: Arrays.copyOfRange(indentationValues, lowerFitLimitIndex, upperFitLimitIndex);
        return fittableIndentation;
    }

    @Override
    public double[] getFittableIndentationValuesCopy()
    {
        double[] fittableIndentation = fitAllIndentationData ? Arrays.copyOf(indentationValues, indentationValues.length): Arrays.copyOfRange(indentationValues, lowerFitLimitIndex, upperFitLimitIndex);
        return fittableIndentation;
    }


    @Override
    public double getMinimalIndentation()
    {
        return indentationValues[0]; 
    }

    @Override
    public double getMaximalIndentation()
    {
        return maxForceIndentationPoint.getX(); 
    }

    @Override
    public Point1DData getMinimalForceIndentationPoint()
    {
        return new Point1DData(indentationValues[0], forceValues[0], Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS);
    }

    @Override
    public Point1DData getMaximalForceIndentationPoint()
    {
        return maxForceIndentationPoint;
    }

    @Override
    public double[] convertToForceIndentationPoint(double absoluteZ, double absoluteForce)
    {
        double absoluteDeflection = absoluteForce/deflectionForceConversion;
        double relativeZ = contactPointZ - absoluteZ;
        double indent = relativeZ - (absoluteDeflection - contactPointD);

        return new double[] {indent, absoluteForce - baselineDeflection*deflectionForceConversion};
    }


    @Override
    public Point1DData convertToForceIndentationPoint(Point1DData forceCurvePoint)
    {
        double absoluteZ = forceCurvePoint.getX();
        double absoluteForce = forceCurvePoint.getY();

        double absoluteDeflection = absoluteForce/deflectionForceConversion;
        double relativeZ = contactPointZ - absoluteZ;
        double indent = relativeZ - (absoluteDeflection - contactPointD);

        return new Point1DData(indent, absoluteForce - baselineDeflection*deflectionForceConversion, Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS);       
    }


    @Override
    public double[] convertToForceCurvePoint(double indent, double relativeForce)
    {
        double relativeDeflection = relativeForce/(deflectionForceConversion);
        double absoluteForce = relativeForce + deflectionForceConversion*baselineDeflection;

        double z = contactPointZ - (indent + relativeDeflection + baselineDeflection - contactPointD);

        return new double[] {z, absoluteForce};
    }


    @Override
    public Point1DData convertToForceCurvePoint(Point1DData forceIndentPoint) 
    {
        double indent = forceIndentPoint.getX();
        double relativeForce = forceIndentPoint.getY();

        double relativeDeflection = relativeForce/(deflectionForceConversion);
        double absoluteForce = relativeForce + deflectionForceConversion*baselineDeflection;

        double z = contactPointZ - (indent + relativeDeflection + baselineDeflection - contactPointD);

        return new Point1DData(z, absoluteForce, Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS);
    }

    @Override
    public Channel1DData convertToForceCurvePoints(double[] forceValues, double[] indentationValues)
    {
        int n = forceValues.length;

        double[] absoluteForceValues = new double[n];
        double[] zValues = new double[n];

        for(int i = 0; i < n; i++)
        {
            double indent = indentationValues[i];
            double relativeForce = forceValues[i];
            double relativeDeflection = relativeForce/deflectionForceConversion;

            absoluteForceValues[i] = relativeForce + deflectionForceConversion*baselineDeflection;
            zValues[i] = contactPointZ - (indent + relativeDeflection + baselineDeflection - contactPointD);
        }

        Channel1DData forceCurveChannel = new FlexibleFlatChannel1DData(zValues, absoluteForceValues, Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS, SortedArrayOrder.DESCENDING);
        return forceCurveChannel;
    } 


    @Override
    public double[] convertToDeflectionCurvePoint(double indent, double relativeForce)
    {
        double relativeDeflection = relativeForce/(deflectionForceConversion);
        double absoluteDeflection = relativeDeflection + baselineDeflection;

        double z = contactPointZ - (indent + relativeDeflection + baselineDeflection - contactPointD);

        return new double[] {z, absoluteDeflection};
    }

    @Override
    public Point1DData convertToDeflectionCurvePoint(Point1DData forceIndentPoint)
    {
        double indent = forceIndentPoint.getX();
        double relativeForce = forceIndentPoint.getY();

        double relativeDeflection = relativeForce/(deflectionForceConversion);
        double absoluteDeflection = relativeDeflection + baselineDeflection;

        double z = contactPointZ - (indent + relativeDeflection + baselineDeflection - contactPointD);

        return new Point1DData(z, absoluteDeflection, Quantities.DISTANCE_MICRONS, Quantities.DEFLECTION_MICRONS);
    }

    @Override
    public double[][] convertToDeflectionCurvePoints(double[][] forceIndentationPoints)
    {
        int n = forceIndentationPoints.length;

        double[][] forceCurvePoints = new double[n][];

        for(int i = 0; i<n;i++)
        {
            double[] forceIndentationPoint = forceIndentationPoints[i];
            forceCurvePoints[i] = convertToDeflectionCurvePoint(forceIndentationPoint[0], forceIndentationPoint[1]);
        }

        return forceCurvePoints;
    } 

    public double[][] getDeflectionIndentation()
    {
        int n = indentationValues.length;

        double[][] deflectionIndentation = new double[n][];

        for(int i = 0; i<n; i++)
        {
            double indentation = indentationValues[i];
            double force = forceValues[i];
            double deflection = force/deflectionForceConversion;

            deflectionIndentation[i] = new double[] {indentation, deflection};
        }

        return deflectionIndentation;
    }

    @Override
    public double[][] getForceIndentation() 
    {
        int n = indentationValues.length;
        return getForceIndentation(0, n);
    }

    public double[][] getForceIndentation(int from, int to) 
    {
        int n = to - from;

        double[][] forceIndentation = new double[n][];

        for(int i = from; i<to; i++)
        {
            double indentation = indentationValues[i];
            double force = forceValues[i];

            forceIndentation[i - from] = new double[] {indentation, force};
        }

        return forceIndentation;

    }

    @Override
    public double[][] getFittableForceIndentation()
    {
        double[][] fittableForceIndentation = (fitAllIndentationData) ? getForceIndentation() : getForceIndentation(lowerFitLimitIndex, upperFitLimitIndex);
        return fittableForceIndentation;
    }

    @Override
    public double[][] getFittableForceIndentationXYView()
    {
        if(fitAllIndentationData)
        {
            double[][] fittableForceIndentation = new double[][] {indentationValues, forceValues};
            return fittableForceIndentation;
        }

        double[][] fittableForceIndentation = new double[][] {Arrays.copyOfRange(indentationValues, lowerFitLimitIndex, upperFitLimitIndex), Arrays.copyOfRange(forceValues, lowerFitLimitIndex, upperFitLimitIndex)};
        return fittableForceIndentation;
    }

    public double[][] getForceIndentationSI()
    {
        int n = indentationValues.length;

        double[][] forceIndentationSI = new double[n][];

        for(int i = 0; i< n; i++)
        {
            double indentation = 1e-6*indentationValues[i];
            double force = 1e-9*forceValues[i];
            forceIndentationSI[i] = new double[] {indentation, force};
        }

        return forceIndentationSI;

    }

    public double[][] getPositiveForceIndentationSI()
    {
        int n = indentationValues.length;

        double[][] positiveForceIndentationSI = new double[n][];

        int j = 0;
        for(int i = 0; i < n;i++)
        {
            double indentation = 1e-6*indentationValues[i];
            double force = 1e-9*forceValues[i];

            if(indentation > 0)
            {
                positiveForceIndentationSI[j] = new double[] {indentation, force};
                j++;
            }
        }

        if(j == n)
        {
            return positiveForceIndentationSI;
        }

        double[][] positiveForceIndentationSITrimmed = Arrays.copyOfRange(positiveForceIndentationSI, 0, j);
        return positiveForceIndentationSITrimmed;
    }

    public double[][] getPositiveForceIndentationSIXYView()
    {
        int n = indentationValues.length;

        double[] positiveForceIndentationSIXs = new double[n];
        double[] positiveForceIndentationSIYs = new double[n];

        int j = 0;
        for(int i = 0; i < n; i++)
        {
            double indentation = 1e-6*indentationValues[i];
            double force = 1e-9*forceValues[i];

            if(indentation > 0)
            {
                positiveForceIndentationSIXs[j] = indentation;
                positiveForceIndentationSIYs[j] = force;
                j++;
            }
        }

        if(j == n)
        {
            return new double[][] {positiveForceIndentationSIXs, positiveForceIndentationSIYs};
        }

        double[][] positiveForceIndentationSITrimmed = new double[][] {Arrays.copyOfRange(positiveForceIndentationSIXs, 0, j), Arrays.copyOfRange(positiveForceIndentationSIYs, 0, j)};
        return positiveForceIndentationSITrimmed;
    }

    @Override
    public double getBaselineDeflection()
    {
        return baselineDeflection;            
    }

    public double getAdhesion()
    {
        return adhesionForce;
    }

    public double[] getDeflectionContactPoint()
    {
        return new double[] {contactPointZ, contactPointD};
    }

    public double[] getContactPoint()
    {
        return new double[] {contactPointZ, deflectionForceConversion*contactPointD};
    }

    public Point1DData getPullOffPoint()
    {
        return deflectionIndentPullOff.getCopy(deflectionForceConversion, Quantities.FORCE_NANONEWTONS);
    }
}
