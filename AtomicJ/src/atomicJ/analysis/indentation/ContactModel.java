
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

import java.awt.geom.Point2D;

import atomicJ.analysis.ContactEstimationGuide;
import atomicJ.analysis.ProcessingSettings;
import atomicJ.analysis.RegressionStrategy;
import atomicJ.analysis.SampleModel;
import atomicJ.data.Channel1DData;


public interface ContactModel extends ContactEstimationGuide
{	
    public Indenter getIndenter();
    public SampleModel getSampleModel();
    public String getName();

    public double[][] getDeflectionSeparationSeparateCoordinateArrays(double[] deflectionValues, double[] zValues, int from, int to, double zContact, double dContact);
    public ContactModelFit<?> getModelFit(Channel1DData deflectionCurveBranch, double[] deflectionContactPoint, Point2D recordingPoint, ProcessingSettings settings);

    public double getPrecontactObjectiveFunctionMinimum(double[] forceValues,double[] separationValues, Point2D recordingPoint, RegressionStrategy regressionStrategy);
    public double getPostcontactObjectiveFunctionMinimum(double[] ys, double[] xs, Point2D recordingPoint, RegressionStrategy regressionStrategy);

}
