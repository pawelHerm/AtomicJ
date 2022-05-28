
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

import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JOptionPane;

import atomicJ.gui.AtomicJ;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.curveProcessing.CurveVisualizationHandler;
import atomicJ.gui.curveProcessing.MapSourceHandler;
import atomicJ.gui.curveProcessing.NumericalResultsHandler;
import atomicJ.gui.curveProcessing.SpectroscopyCurveAveragingHandler;
import atomicJ.readers.IllegalImageException;
import atomicJ.readers.IllegalSpectroscopySourceException;
import atomicJ.readers.ReadingPack;
import atomicJ.sources.IdentityTag;
import atomicJ.sources.ImageSource;
import atomicJ.sources.MapSource;
import atomicJ.utilities.Validation;


public class SpectroscopyProcessingHandler implements ProcessingResultsHandler<SpectroscopyProcessingResult>
{	
    private final List<VisualizableSpectroscopyPack> allVisualizablePacks = new ArrayList<>();
    private Map<MapSource<?>, List<ImageSource>> mapSources;

    private final SpectroscopyResultDestination destination;

    private final Map<IdentityTag, Batch<ProcessedSpectroscopyPack>> processedBatches = new LinkedHashMap<>();	

    private final MapSourceHandler mapSourceHandler;
    private final CurveVisualizationHandler<VisualizableSpectroscopyPack> curveVisualizationHandler;
    private final NumericalResultsHandler<ProcessedSpectroscopyPack> resultHandler;
    private final SpectroscopyCurveAveragingHandler averagingHandler;

    public SpectroscopyProcessingHandler(SpectroscopyResultDestination destination, MapSourceHandler mapSourceHandler, CurveVisualizationHandler<VisualizableSpectroscopyPack> curveVisualizationHandler, NumericalResultsHandler<ProcessedSpectroscopyPack> resultHandler, SpectroscopyCurveAveragingHandler averagingHandler)
    {
        this.destination = Validation.requireNonNullParameterName(destination, "destination");	
        this.mapSourceHandler = Validation.requireNonNullParameterName(mapSourceHandler,"mapSourceHandler");
        this.curveVisualizationHandler = Validation.requireNonNullParameterName(curveVisualizationHandler, "curveVisualizationHandler");
        this.resultHandler = Validation.requireNonNullParameterName(resultHandler, "resultHandler");
        this.averagingHandler = Validation.requireNonNullParameterName(averagingHandler, "averagingHandler");
    }

    @Override
    public Component getAssociatedComponent()
    {
        return destination.getPublicationSite();
    }

    @Override
    public void acceptAndSegregateResults(List<SpectroscopyProcessingResult> results)
    {       
        Map<MapSource<?>, ReadingPack<ImageSource>> allMapSourcesTemporary = new LinkedHashMap<>();

        for(SpectroscopyProcessingResult result: results)
        {
            ProcessedSpectroscopyPack processedPack = result.getProcessedPack();          
            IdentityTag tag = processedPack.getBatchIdTag();

            Batch<ProcessedSpectroscopyPack> batch = processedBatches.get(tag);
            if(batch == null)
            {
                batch = new Batch<>(tag.getLabel(), (int)tag.getKey());
                processedBatches.put(tag, batch);
            }

            batch.addProcessedPack(processedPack);

            MapProcessingSettings mapSettings = result.getMapSettings();

            mapSourceHandler.handleProcessedPackRegistrationRequest(mapSettings, processedPack);
            mapSourceHandler.handleMapSourceAndImageAdditionRequest(mapSettings, processedPack, allMapSourcesTemporary);

            VisualizableSpectroscopyPack visualized = result.getVisualizablePack();
            if(visualized != null)
            {
                allVisualizablePacks.add(visualized);
            }
        }

        mapSources = readInImagesAccompanyingMaps(allMapSourcesTemporary);
        mapSourceHandler.handleSealingRequest(mapSources.keySet());
    }

    private Map<MapSource<?>, List<ImageSource>> readInImagesAccompanyingMaps(Map<MapSource<?>, ReadingPack<ImageSource>> allMapSourcesTemporary)
    {
        Map<MapSource<?>, List<ImageSource>> readInImageSources = new LinkedHashMap<>();
        for(Entry<MapSource<?>,ReadingPack<ImageSource>> entry : allMapSourcesTemporary.entrySet())
        {
            MapSource<?> mapSource = entry.getKey();
            ReadingPack<ImageSource> readingPack = entry.getValue();

            List<ImageSource> imageSources = new ArrayList<>();
            if(readingPack != null)
            {                
                try 
                {
                    List<ImageSource> imagesForMap = readingPack.readSources();    
                    imageSources.addAll(imagesForMap);                    
                } 
                catch (UserCommunicableException | IllegalImageException
                        | IllegalSpectroscopySourceException e)
                {
                    e.printStackTrace();
                }
            }

            readInImageSources.put(mapSource, imageSources);
        }       

        return readInImageSources;
    }

    @Override
    public void sendResultsToDestination()
    {        
        destination.getResultBatchesCoordinator().countNewBatches(processedBatches.keySet());

        resultHandler.handlePublicationRequest(processedBatches.values());
        mapSourceHandler.handlePublicationRequest(mapSources);
        curveVisualizationHandler.handlePublicationRequest(allVisualizablePacks);
        averagingHandler.handleAveragingRequest(processedBatches);
    }

    @Override
    public void reactToFailures(int failuresCount)
    {
        if(failuresCount > 0)
        {
            JOptionPane.showMessageDialog(destination.getPublicationSite(), "Errors occured during processing of " + failuresCount + " source files", AtomicJ.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
        }  
    }

    @Override
    public void reactToCancellation()
    {
        destination.withdrawPublication();
    }

    @Override
    public void endProcessing()
    {
        destination.endProcessing();
    }
}
