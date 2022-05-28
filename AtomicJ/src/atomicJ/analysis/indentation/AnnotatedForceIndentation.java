
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

import atomicJ.data.Channel1DData;
import atomicJ.data.Point1DData;

public interface AnnotatedForceIndentation 
{  
    public double getMinimalIndentation();
    public double getMaximalIndentation();
    public double getBaselineDeflection();
    public Point1DData getMinimalForceIndentationPoint();
    public Point1DData getMaximalForceIndentationPoint();

    public double[] convertToForceIndentationPoint(double z, double force);
    public double[] convertToForceCurvePoint(double indentation, double relativeForce);
    public double[] convertToDeflectionCurvePoint(double indentation, double relativeForce);

    public Point1DData convertToForceIndentationPoint(Point1DData forceCurvePoint);
    public Point1DData convertToForceCurvePoint(Point1DData forceIndentPoint);
    public Point1DData convertToDeflectionCurvePoint(Point1DData forceIndentPoint);

    public double[] getForceValues();
    public double[] getFittableForceValues();
    public double[] getFittableForceValuesCopy();
    public double[] getIndentationValues();
    public double[] getFittableIndentationValues();
    public double[] getFittableIndentationValuesCopy();

    public double[][] getForceIndentation();
    public double[][] getFittableForceIndentation();
    public double[][] getFittableForceIndentationXYView();

    public double[][] convertToDeflectionCurvePoints(double[][] forceIndentationPoints);
    public Channel1DData convertToForceCurvePoints(double[] forceValues, double[] indentationValues);
}
