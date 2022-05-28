
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

import java.util.List;

import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.data.Channel1DData;
import atomicJ.data.Point1DData;


public interface ContactModelFit <E extends ContactModel>
{	
    public E getContactModel();   
    public List<? extends ProcessedPackFunction<ProcessedSpectroscopyPack>> getSpecialFunctions();

    public double getYoungModulus();
    public boolean isPullOffSeparateFromContact();

    public double getPointwiseModulus(double indentation, double force);
    public Channel1DData getPointwiseModulus();
    public Channel1DData getPointwiseModulusFit(int plotPoints);

    public Channel1DData getForceIndentation();
    public Channel1DData getForceIndentationFit(int plotPoints);	

    public double getCoefficientOfDetermination();

    public double[] convertToForceCurvePoint(double[] forceIndentationPoint);
    public double[] convertToDeflectionCurvePoint(double[] forceIndentationPoint);
    public Channel1DData convertToForceCurvePoints(double[] forceValues, double[] indentationValues);
    public double[][] convertToDeflectionCurvePoints(double[][] forceIndentationPoints);

    public Point1DData getForceIndentationTransitionPoint();
    public Point1DData getPointwiseModulusTransitionPoint();

    public Point1DData getDeflectionCurveTransitionPoint();
    public Point1DData getForceCurveTransitionPoint();

    public Point1DData getDeflectionCurveContactPoint();
    public Point1DData getForceCurveContactPoint();

    public Point1DData getDeflectionCurvePullOffPoint();
    public Point1DData getForceCurvePullOffPoint();

    public Point1DData getMaximalDeformationPoint();
}
