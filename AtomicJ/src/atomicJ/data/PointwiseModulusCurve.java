
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

package atomicJ.data;

import static atomicJ.data.Datasets.MODEL_TRANSITION_POINT;
import static atomicJ.data.Datasets.MODEL_TRANSITION_POINT_POINTWISE_MODULUS;
import static atomicJ.data.Datasets.POINTWISE_MODULUS;
import static atomicJ.data.Datasets.POINTWISE_MODULUS_DATA;
import static atomicJ.data.Datasets.POINTWISE_MODULUS_COMPOSITE;

import java.util.ArrayList;
import java.util.List;

import atomicJ.analysis.VisualizationSettings;
import atomicJ.analysis.indentation.ContactModelFit;

public class PointwiseModulusCurve implements Data1D
{	
    private static final String PLOT_IDENTIFIER = "PointwiseModulusPlot";

    private final Channel1D pointwiseModulus;	
    private final Channel1D transitionIndentationPoint;
    private final Channel1D fit;

    public PointwiseModulusCurve(Channel1D pointwiseModulus, Channel1D transitionIndentationPoint)
    {
        this(pointwiseModulus, transitionIndentationPoint, null);
    }

    public PointwiseModulusCurve(Channel1D pointwiseModulus, Channel1D transitionIndentationPoint, Channel1D fit)
    {        
        this.pointwiseModulus = pointwiseModulus;
        this.transitionIndentationPoint = transitionIndentationPoint;
        this.fit = fit;
    }

    public static PointwiseModulusCurve buildPointwiseModulusCurve(ContactModelFit<?> modelFit, VisualizationSettings visSettings)
    {
        PointwiseModulusCurve modulusCurve = null;

        if(visSettings.isPlotModulus())
        {       
            Channel1DData pointwiseModulusData = modelFit.getPointwiseModulus();  
            Point1DData pointwiseModulusTransitionPointData = modelFit.getPointwiseModulusTransitionPoint();
            Channel1D pointwiseModulus = new Channel1DStandard(pointwiseModulusData, POINTWISE_MODULUS_DATA, POINTWISE_MODULUS);
            Channel1D transitionPointPointwiseModulus = new Channel1DStandard(pointwiseModulusTransitionPointData, MODEL_TRANSITION_POINT_POINTWISE_MODULUS, MODEL_TRANSITION_POINT);
            modulusCurve = visSettings.isPlotModulusFit() ? new PointwiseModulusCurve(pointwiseModulus, transitionPointPointwiseModulus, new Channel1DStandard(modelFit.getPointwiseModulusFit(20),  Datasets.POINTWISE_MODULUS_FIT, Datasets.FIT)) : new PointwiseModulusCurve(pointwiseModulus,transitionPointPointwiseModulus);
        }

        return modulusCurve;
    }

    @Override
    public List<Channel1D> getChannels()
    {
        List<Channel1D> curves = new ArrayList<>();
        curves.add(pointwiseModulus);

        if(fit != null)
        {
            curves.add(fit);
        }
        curves.add(transitionIndentationPoint);

        return curves;
    }

    @Override
    public String getIdentifier() 
    {
        return POINTWISE_MODULUS_COMPOSITE;
    }

    @Override
    public String getName() 
    {
        return POINTWISE_MODULUS;
    }

    public String getPlotIdentfier() 
    {
        return PLOT_IDENTIFIER;
    }
}
