
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


import java.util.List;
import java.util.Map;

import atomicJ.gui.Channel1DResultsView;
import atomicJ.gui.curveProcessing.ContactSelectionDialog;
import atomicJ.gui.curveProcessing.CurveVisualizationHandler;
import atomicJ.gui.curveProcessing.MapSourceHandler;
import atomicJ.gui.curveProcessing.ProcessingBatchModel;
import atomicJ.gui.curveProcessing.SpectroscopyCurveAveragingHandler;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.MapImageResource;
import atomicJ.sources.SimpleSpectroscopySource;
import atomicJ.sources.ChannelSource;


public interface SpectroscopyResultDestination extends ResultDestinationBasic<SimpleSpectroscopySource, ProcessedSpectroscopyPack>
{	
    public MapSourceHandler getDefaultMapSourceHandler();
    public MapSourceHandler getRecalculationMapSourceHandler();
    public CurveVisualizationHandler<VisualizableSpectroscopyPack> getDefaultCurveVisualizationHandler();
    public SpectroscopyCurveAveragingHandler getDefaultCurveAveragingHandler();

    public ContactSelectionDialog getContactPointSelectionDialog();

    public Channel1DResultsView getGraphicalResultsDialog();
    public void showMaps(boolean show);
    public void showImages(boolean show);
    public void markSourcePositions(SimpleSpectroscopySource selectedResource);
    public void markSourcePositions(List<SimpleSpectroscopySource> sources);
    public void showMapHistograms(boolean show);
    public void showImageHistograms(boolean show);

    public boolean isAnyMapResourceAvailable();
    public List<MapImageResource> getMapResources();
    public boolean areChannel2DResourcesAvailable();
    public List<? extends Channel2DResource> getImageResources();

    public void startPreviewForAllSources(List<ChannelSource> sources);
    //returns failure count
    public int recalculate(ProcessingBatchModel model, Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld);
    //returns failure count
    public void recalculate(List<ProcessingBatchModel> models, 
            Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld,List<ProcessedSpectroscopyPack> packsToRemove, boolean deleteOldNumericalResults, boolean deleteOldCurveCharts, boolean modifyMapsInPlace);

    public void recalculateSources(List<ProcessingBatchModel> models, Map<SimpleSpectroscopySource, SimpleSpectroscopySource> sourcesNewVsOld, List<SimpleSpectroscopySource> sourcesToRemove, boolean deleteOldNumericalResults, boolean deleteOldCurveCharts, boolean modifyMapsInPlace);
}
