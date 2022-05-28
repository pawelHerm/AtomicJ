
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe� Hermanowicz
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


import java.util.List;

import atomicJ.gui.curveProcessing.CurveVisualizationHandler;
import atomicJ.gui.curveProcessing.MapSourceHandler;
import atomicJ.gui.curveProcessing.NumericalResultsHandler;
import atomicJ.gui.curveProcessing.ProcessingBatchModel;
import atomicJ.sources.SimpleSpectroscopySource;



public interface SpectroscopyProcessingOrigin 
{	
    public void startProcessing();	
    public void startProcessing(int batchIndex);
    public void startProcessing(List<SimpleSpectroscopySource> sources, int batchIndex);
    public void startProcessing(List<ProcessingBatchModel> batches);
    public void startProcessing(List<ProcessingBatchModel> batches,
            MapSourceHandler mapSourceHandler, CurveVisualizationHandler<VisualizableSpectroscopyPack> curveVisualizationHandler, NumericalResultsHandler<ProcessedSpectroscopyPack> numericalResultsHandler);
}
