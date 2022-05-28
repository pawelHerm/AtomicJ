
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

import static atomicJ.data.Datasets.INDENTATION;
import static atomicJ.data.Datasets.INDENTATION_DATA;
import static atomicJ.data.Datasets.MODEL_TRANSITION_POINT;
import static atomicJ.data.Datasets.MODEL_TRANSITION_POINT_INDENTATION_CURVE;

import java.util.ArrayList;
import java.util.List;

import atomicJ.analysis.VisualizationSettings;
import atomicJ.analysis.indentation.ContactModelFit;


public class IndentationCurve implements Data1D
{
    private static final String PLOT_IDENTIFIER = "IndentationPlot";

    private final Channel1D indentation;	
    private final Channel1D transitionIndentationPoint;

    private final Channel1D fit;	

    public IndentationCurve(Channel1D indentation, Channel1D transitionIndentationPoint)
    {
        this(indentation, transitionIndentationPoint, null);
    }

    public IndentationCurve(Channel1D indentation, Channel1D transitionIndentationPoint, Channel1D fit)
    {        
        this.indentation = indentation;
        this.transitionIndentationPoint = transitionIndentationPoint;
        this.fit = fit;
    }

    @Override
    public List<Channel1D> getChannels() 
    {
        List<Channel1D> channels = new ArrayList<>();
        channels.add(indentation);

        if(fit != null)
        {
            channels.add(fit);
        }
        channels.add(transitionIndentationPoint);

        return channels;
    }

    @Override
    public String getIdentifier() 
    {
        return Datasets.INDENTATION;
    }

    @Override
    public String getName() 
    {
        return Datasets.INDENTATION_COMPOSITE;
    }

    public String getPlotIdentfier() 
    {
        return PLOT_IDENTIFIER;
    }

    public static IndentationCurve buildIndentationCurve(ContactModelFit<?> modelFit, Channel1DData forceIndentationFit, VisualizationSettings visSettings)
    {
        IndentationCurve indentationCurve = null;

        if(visSettings.isPlotIndentation())
        {
            Point1DData indentationTansitionPointData = modelFit.getForceIndentationTransitionPoint();
            Channel1DData forceIndentationData = modelFit.getForceIndentation();
            Channel1D forceIndentation = new Channel1DStandard(forceIndentationData, INDENTATION_DATA, INDENTATION);
            Channel1D forceIndentationFitChannel = new Channel1DStandard(forceIndentationFit, Datasets.INDENTATION_FIT,  Datasets.FIT);
            Channel1D transitionPointIndentation = new Channel1DStandard(indentationTansitionPointData, MODEL_TRANSITION_POINT_INDENTATION_CURVE,MODEL_TRANSITION_POINT,new ProjectionConstraint1D(forceIndentation.getIdentifier(), 4, 0));
            indentationCurve = visSettings.isPlotIndentationFit() ? new IndentationCurve(forceIndentation, transitionPointIndentation,forceIndentationFitChannel) : new IndentationCurve(forceIndentation, transitionPointIndentation); 
        }

        return indentationCurve;
    }
}
