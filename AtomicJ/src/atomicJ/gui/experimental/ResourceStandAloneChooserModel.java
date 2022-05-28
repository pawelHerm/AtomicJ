
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

package atomicJ.gui.experimental;


import java.util.*;

import atomicJ.gui.AbstractModel;
import atomicJ.resources.Resource;
import atomicJ.sources.Channel2DSource;
import atomicJ.sources.Channel2DSourceType;


public class ResourceStandAloneChooserModel extends AbstractModel
{
    public static final String DENSITY_SOURCE_TYPE = "DensitySourceType";

    public static final String ALL_SOURCES = "AllSources";
    public static final String MAP_SOURCES = "MapSources";
    public static final String IMAGE_SOURCES = "ImageSources";
    public static final String AVAILABLE_SOURCES = "AvailableSources";

    public static final String RESOURCE_IS_CHOSEN = "ResourceIsChosen";
    public static final String CHOSEN_RESOURCE = "ChosenResource";

    public static final String FINISH_ENABLED = "FinishEnabled";

    private final String taskName;
    private final String taskDescription;

    private Channel2DSourceType sourceType = Channel2DSourceType.ALL;

    private Channel2DSource<?> chosenResource;

    private final List<Channel2DSource<?>> imageSources = new ArrayList<>();
    private final List<Channel2DSource<?>> mapSources = new ArrayList<>();

    private boolean finishEnabled;

    public ResourceStandAloneChooserModel(String taskName, String taskDescription)
    {
        this.taskName = taskName;
        this.taskDescription = taskDescription;

        checkIfFinishEnabled();
    }	

    public ResourceStandAloneChooserModel(List<Channel2DSource<?>> imageSources, List<Channel2DSource<?>> mapSources, String taskName, String taskDescription)
    {        
        this.imageSources.addAll(imageSources);
        this.mapSources.addAll(mapSources);

        this.taskName = taskName;
        this.taskDescription = taskDescription;

        checkIfFinishEnabled();
    }   

    public boolean isEmpty()
    {
        boolean empty = imageSources.isEmpty() && mapSources.isEmpty();

        return empty;
    }

    public Channel2DSourceType getDensitySourceType()
    {
        return sourceType;
    }

    public void setDensitySourceType(Channel2DSourceType sourceTypeNew)
    {        
        if(!Objects.equals(sourceType, sourceTypeNew))
        {
            Channel2DSourceType sourceTypeOld = this.sourceType;
            this.sourceType = sourceTypeNew;

            List<Channel2DSource<?>> availableSourcesOld = getAvailableSources(sourceTypeOld);      
            List<Channel2DSource<?>> availableSourcesNew = getAvailableSources(sourceTypeNew);

            firePropertyChange(DENSITY_SOURCE_TYPE, sourceTypeOld, sourceTypeNew);
            firePropertyChange(AVAILABLE_SOURCES, availableSourcesOld, availableSourcesNew);      
        }
    }

    public List<Channel2DSource<?>> getMapSources()
    {
        return new ArrayList<>(mapSources);
    }

    public void setMapSources(Collection<Channel2DSource<?>> sourcesNew)
    {        
        List<Channel2DSource<?>> mapSourcesOld = new ArrayList<>(mapSources);
        List<Channel2DSource<?>> mapSourcesNew = new ArrayList<>(sourcesNew);

        List<Channel2DSource<?>> allSourcesOld = getAllSources();
        List<Channel2DSource<?>> availableSourcesOld = getCurrentlyAvailableSources();

        this.mapSources.clear();
        this.mapSources.addAll(sourcesNew);

        List<Channel2DSource<?>> allSourcesNew = getAllSources();
        List<Channel2DSource<?>> availableSourcesNew = getCurrentlyAvailableSources();


        firePropertyChange(MAP_SOURCES, mapSourcesOld, mapSourcesNew);
        firePropertyChange(ALL_SOURCES, allSourcesOld, allSourcesNew);      
        firePropertyChange(AVAILABLE_SOURCES, availableSourcesOld, availableSourcesNew);      

        if(!allSourcesNew.contains(chosenResource))
        {
            setChosenResource(null);
        }

    }  

    public List<Channel2DSource<?>> getImageSources()
    {
        return new ArrayList<>(imageSources);
    }

    public void setImageSources(Collection<Channel2DSource<?>> sourcesNew)
    {        
        List<Channel2DSource<?>> imageSourcesOld = new ArrayList<>(imageSources);
        List<Channel2DSource<?>> imageSourcesNew = new ArrayList<>(sourcesNew);

        List<Channel2DSource<?>> allSourcesOld = getAllSources();
        List<Channel2DSource<?>> availableSourcesOld = getCurrentlyAvailableSources();

        this.imageSources.clear();
        this.imageSources.addAll(sourcesNew);

        List<Channel2DSource<?>> allSourcesNew = getAllSources();
        List<Channel2DSource<?>> availableSourcesNew = getCurrentlyAvailableSources();

        firePropertyChange(IMAGE_SOURCES, imageSourcesOld, imageSourcesNew);       
        firePropertyChange(ALL_SOURCES, allSourcesOld, allSourcesNew);
        firePropertyChange(AVAILABLE_SOURCES, availableSourcesOld, availableSourcesNew);      

        if(!allSourcesNew.contains(chosenResource))
        {
            setChosenResource(null);
        }
    }  

    public List<Channel2DSource<?>> getAllSources()
    {
        List<Channel2DSource<?>> allSources = new ArrayList<>();

        allSources.addAll(imageSources);
        allSources.addAll(mapSources);

        return allSources;
    }

    public List<Channel2DSource<?>> getCurrentlyAvailableSources()
    {
        return getAvailableSources(sourceType);
    }

    private List<Channel2DSource<?>> getAvailableSources(Channel2DSourceType type)
    {
        if(Channel2DSourceType.ALL.equals(type))
        {
            return getAllSources();
        }

        else if(Channel2DSourceType.IMAGE.equals(type))
        {
            return new ArrayList<>(imageSources);
        }
        else if(Channel2DSourceType.MAP.equals(type))
        {
            return new ArrayList<>(mapSources);
        }
        else 
        {
            return Collections.emptyList();
        }
    }


    public void cancel()
    {}

    public boolean isFinishEnabled()
    {
        return finishEnabled;
    }

    private void checkIfFinishEnabled()
    {
        boolean finishEnabledNew = chosenResource != null;
        boolean finihEnabledOld = finishEnabled;
        this.finishEnabled = finishEnabledNew;

        firePropertyChange(FINISH_ENABLED, finihEnabledOld, finishEnabledNew);	
    }


    public void finish() 
    {}

    public String getTaskName() 
    {
        return taskName;
    }

    public String getTaskDescription() 
    {
        return taskDescription;
    }

    public Channel2DSource<?> getChosenResource() 
    {
        return chosenResource;
    }

    public void setChosenResource(Channel2DSource<?> sourceNew) 
    {
        Resource sourceOld = this.chosenResource;
        this.chosenResource = sourceNew;

        firePropertyChange(RESOURCE_IS_CHOSEN, (sourceOld != null), (sourceNew != null));
        firePropertyChange(CHOSEN_RESOURCE, sourceOld, sourceNew);

        checkIfFinishEnabled();
    }

    public boolean isResourceChosen() {
        return (chosenResource != null);
    }
}
