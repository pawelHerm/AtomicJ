
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

package atomicJ.analysis;

public final class VisualizationSettings
{
    private final VisualizationChartSettings plotCurve;
    private final VisualizationChartSettings plotIndentation;
    private final VisualizationChartSettings plotModulus;

    public VisualizationSettings()
    {
        this(new VisualizationChartSettings(), new VisualizationChartSettings(), new VisualizationChartSettings());
    }

    public VisualizationSettings(VisualizationChartSettings plotForceCurve, VisualizationChartSettings plotIndentation, 
            VisualizationChartSettings plotModulus)
    {
        this.plotCurve = plotForceCurve;
        this.plotIndentation = plotIndentation;
        this.plotModulus = plotModulus;
    }

    public boolean isAnyResultToBeVisualized()
    {
        return plotCurve.isShown() || plotIndentation.isShown() || plotModulus.isShown();
    }

    public boolean isPlotRecordedCurve()
    {
        return plotCurve.isShown();
    }

    public boolean isPlotRecordedCurveFit()
    {
        return plotCurve.isFitPlotted();
    }

    public boolean isPlotIndentation()
    {
        return plotIndentation.isShown();
    }

    public boolean isPlotIndentationFit()
    {
        return plotIndentation.isFitPlotted();
    }

    public boolean isPlotModulus()
    {
        return plotModulus.isShown();
    }

    public boolean isPlotModulusFit()
    {
        return plotModulus.isFitPlotted();
    }
}
