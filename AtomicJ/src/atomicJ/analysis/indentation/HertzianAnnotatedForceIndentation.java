
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

public class HertzianAnnotatedForceIndentation implements AnnotatedForceIndentation
{
    private final double[] indentationValues;
    private final double[] forceValues;

    private final double contactPointZ;
    private final double contactPointD;

    private final double baselineDeflection;

    private final Point1DData maxForceIndentationPoint;
    private final double deflectionForceConversion;

    private final int lowerFitLimitIndex;
    private final int upperFitLimitIndex;
    private final boolean fitAllIndentationData;

    public HertzianAnnotatedForceIndentation(double[] indentationValues, double[] forceValues, int lowerFitLimitIndex, int upperFitLimitIndex, double[] contactPoint, double baselineDeflection, double deflectionForceConversion)
    {
        this.indentationValues = indentationValues;
        this.forceValues = forceValues;

        this.lowerFitLimitIndex = lowerFitLimitIndex;
        this.upperFitLimitIndex = upperFitLimitIndex;
        this.fitAllIndentationData = (lowerFitLimitIndex <= 0) && (upperFitLimitIndex >= indentationValues.length);

        this.contactPointZ = contactPoint[0];
        this.contactPointD = contactPoint[1];

        int maxForceIndentationPointIndex = ArrayUtilities.getMaximumIndex(forceValues);
        this.maxForceIndentationPoint = new Point1DData(indentationValues[maxForceIndentationPointIndex], forceValues[maxForceIndentationPointIndex], Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS);

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
        return new Point1DData(indentationValues[0], forceValues[1], Quantities.INDENTATION_MICRONS, Quantities.FORCE_NANONEWTONS);
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
        double absoluteForce = forceCurvePoint.getY();
        double absoluteDeflection = absoluteForce/deflectionForceConversion;
        double relativeZ = contactPointZ - forceCurvePoint.getX();
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

    //force should be in nN, while indentation in microns
    @Override
    public Channel1DData convertToForceCurvePoints(double[] forceValues, double[] indentationValues)
    {
        int n = forceValues.length;

        double[] absoluteForceValues = new double[n];
        double[] zValues = new double[n];

        for(int i = 0; i<n;i++)
        {
            double relativeForce = forceValues[i];
            double indent = indentationValues[i];
            double relativeDeflection = relativeForce/deflectionForceConversion;

            absoluteForceValues[i] = relativeForce + deflectionForceConversion*baselineDeflection;
            zValues[i] = contactPointZ - (indent + relativeDeflection + baselineDeflection - contactPointD);           
        }

        Channel1DData forceCurveChannel = new FlexibleFlatChannel1DData(zValues, absoluteForceValues,Quantities.DISTANCE_MICRONS, Quantities.FORCE_NANONEWTONS, SortedArrayOrder.DESCENDING);
        return forceCurveChannel;
    } 

    @Override
    public double[] convertToDeflectionCurvePoint(double indent, double relativeForce)
    {
        double relativeDeflection = relativeForce/deflectionForceConversion;
        double absoluteDeflection = relativeDeflection + baselineDeflection;

        double z = contactPointZ - (indent + relativeDeflection + baselineDeflection - contactPointD);

        return new double[] {z, absoluteDeflection};
    }

    @Override
    public Point1DData convertToDeflectionCurvePoint(Point1DData forceIndentPoint)
    {
        double indent = forceIndentPoint.getX();
        double relativeForce = forceIndentPoint.getY();

        double relativeDeflection = relativeForce/deflectionForceConversion;
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

    public double[][] getDeflectionIndentation()
    {
        int n = indentationValues.length;

        double[][] deflectionIndentation = new double[n][];

        for(int i = 0; i<n; i++)
        {
            double indentation = indentationValues[0];
            double force = forceValues[1];
            double deflection = force/deflectionForceConversion;

            deflectionIndentation[i] = new double[] {indentation, deflection};
        }

        return deflectionIndentation;
    }

    @Override
    public double getBaselineDeflection()
    {
        return baselineDeflection;            
    }
}
