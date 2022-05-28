
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

package atomicJ.gui;

import java.awt.Window;
import java.util.*;

import atomicJ.resources.Resource;


public class ResourceStandardChooserModel extends AbstractModel implements ResourceChooserModel<Resource>, WizardPageModel
{
    public static final String RESOURCE_IDENTIFIERS = "ResourceIdentifiers";
    public static final String RESOURCES = "Resources";
    public static final String RESOURCE_IS_CHOSEN = "ResourceIsChosen";
    public static final String CHOSEN_RESOURCE = "ChosenResource";

    private final String taskName;
    private final String taskDescription;

    private Resource chosenResource;
    private final Map<Resource, List<String>> allResources = new LinkedHashMap<>();

    private final List<String> identifiers = new ArrayList<>();

    private final boolean isFirst;
    private final boolean isLast;

    private final boolean backEnabled;
    private boolean nextEnabled;
    private final boolean skipEnabled;
    private boolean finishEnabled;

    private boolean necessaryInputProvided;

    public ResourceStandardChooserModel(String taskName, String taskDescription, boolean isFirst, boolean isLast)
    {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.isFirst = isFirst;
        this.isLast = isLast;

        this.skipEnabled = false;
        this.backEnabled = !isFirst;

        checkIfNecessaryInputProvided();
        checkIfNextEnabled();
        checkIfFinishEnabled();
    }	

    @Override
    public String getIdentifier()
    {
        return "";
    }

    @Override
    public void setResources(Map<Resource, List<String>> allResourcesNew)
    {        
        List<Resource> resourcesOld = new ArrayList<>(allResources.keySet());
        List<Resource> resourcesNew= new ArrayList<>(allResourcesNew.keySet());

        this.allResources.clear();
        this.allResources.putAll(allResourcesNew);

        setChosenResource(null);

        firePropertyChange(ResourceStandardChooserModel.RESOURCES, resourcesOld, resourcesNew);
    }  

    public void addResources(Map<Resource, List<String>> newResources)
    {
        Map<Resource, List<String>> resources = new LinkedHashMap<>(allResources);
        resources.putAll(newResources);

        setResources(resources);
    }

    public void removeResources(List<Resource> removedSources)
    {
        Map<Resource, List<String>> resources = new LinkedHashMap<>(allResources);

        for(Resource r : removedSources)
        {
            resources.remove(r);
        }

        setResources(resources);
    }


    @Override
    public void cancel()
    {
    }

    @Override
    public Window getParent() 
    {
        return null;
    }

    private void checkIfNecessaryInputProvided()
    {
        boolean necessaryInputProvidedNew = chosenResource != null;
        boolean necessaryInputProvidedOld = this.necessaryInputProvided;

        this.necessaryInputProvided = necessaryInputProvidedNew;

        firePropertyChange(WizardModelProperties.NECESSARY_INPUT_PROVIDED, necessaryInputProvidedOld, necessaryInputProvidedNew);
    }

    private void checkIfNextEnabled()
    {
        boolean nextEnabledNew = !(chosenResource == null || isLast);
        boolean enabledOld = nextEnabled;
        this.nextEnabled = nextEnabledNew;

        firePropertyChange(WizardModelProperties.NEXT_ENABLED, enabledOld, nextEnabledNew);	
    }

    private void checkIfFinishEnabled()
    {
        boolean finishEnabledNew = chosenResource != null && isLast;
        boolean finihEnabledOld = finishEnabled;
        this.finishEnabled = finishEnabledNew;

        firePropertyChange(WizardModelProperties.FINISH_ENABLED, finihEnabledOld, finishEnabledNew);	
    }

    @Override
    public void back() {

    }

    @Override
    public void next() {

    }

    @Override
    public void skip() {

    }

    @Override
    public void finish() {

    }


    @Override
    public boolean isFirst() 
    {
        return isFirst;
    }

    @Override
    public boolean isLast() 
    {
        return isLast;
    }

    @Override
    public boolean isNecessaryInputProvided() 
    {
        return necessaryInputProvided;
    }
    @Override
    public boolean isBackEnabled() 
    {
        return backEnabled;
    }

    @Override
    public boolean isNextEnabled() 
    {
        return nextEnabled;
    }

    @Override
    public boolean isSkipEnabled() 
    {
        return skipEnabled;
    }

    @Override
    public boolean isFinishEnabled() 
    {
        return finishEnabled;
    }


    @Override
    public String getTaskName() 
    {
        return taskName;
    }

    @Override
    public String getTaskDescription() 
    {
        return taskDescription;
    }

    @Override
    public Resource getChosenResource() 
    {
        return chosenResource;
    }

    public List<String> getChosenIdentifiers()
    {
        List<String> identifiers = (chosenResource != null) ? allResources.get(chosenResource) : new ArrayList<>();
        return identifiers;
    }

    @Override
    public void setChosenResource(Resource chosenResourceNew) 
    {
        Resource chosenResourceOld = this.chosenResource;
        this.chosenResource = chosenResourceNew;

        List<String> identifiersOld = this.identifiers;
        List<String> identifiersNew = (chosenResourceNew != null) ? allResources.get(chosenResource) : new ArrayList<>();

        firePropertyChange(ResourceStandardChooserModel.RESOURCE_IS_CHOSEN, (chosenResourceOld != null), (chosenResourceNew != null));
        firePropertyChange(ResourceStandardChooserModel.CHOSEN_RESOURCE, chosenResourceOld, chosenResourceNew);
        firePropertyChange(ResourceStandardChooserModel.RESOURCE_IDENTIFIERS, identifiersOld, identifiersNew);

        checkIfNecessaryInputProvided();
        checkIfNextEnabled();
        checkIfFinishEnabled();
    }

    @Override
    public List<Resource> getResources() 
    {
        List<Resource> resources= new ArrayList<>(allResources.keySet());
        return resources;
    }

    @Override
    public boolean isResourceChosen() {
        return (chosenResource != null);
    }
}
