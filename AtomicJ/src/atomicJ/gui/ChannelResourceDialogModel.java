package atomicJ.gui;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import atomicJ.data.Channel;
import atomicJ.data.ChannelFilter2;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIComposite;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.gui.undo.ConcurrentRedoTask;
import atomicJ.gui.undo.ConcurrentUndoTask;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.resources.ChannelResource;
import atomicJ.resources.ResourceView;
import atomicJ.utilities.MetaMap;
import atomicJ.utilities.MultiMap;

public class ChannelResourceDialogModel<E extends Channel,D,I,R extends ChannelResource<E, D, I>> extends ResourceViewModel<R>
{     
    public static final String SELECTED_SOURCES_FROM_MAPS = "SelectedSourcesFromMap";

    public ChannelResourceDialogModel()
    {
        super();
    }

    public ChannelResourceDialogModel(Collection<R> resources, String selectedType)
    {
        super(resources, selectedType);
    }

    public Set<I> getSelectedResourcesChannelIdentifiers()
    {
        Set<I> identifiers = new LinkedHashSet<>();

        List<? extends R> selectedResources = getAllSelectedResources();

        for(R resource : selectedResources)
        {
            identifiers.addAll(resource.getIdentifiersForAllTypes());
        }

        return identifiers;
    }

    public Set<I> getSelectedResourcesChannelIdentifiers(ChannelFilter2<E> filter)
    {
        Set<I> identifiers = new LinkedHashSet<>();

        List<? extends R> selectedResources = getAllSelectedResources();

        for(R resource : selectedResources)
        {
            identifiers.addAll(resource.getIdentifiersForAllTypes(filter));
        }

        return identifiers;
    }

    public Set<I> getAllResourcesChannelIdentifiers()
    {
        Set<I> identifiers = new LinkedHashSet<>();

        List<? extends R> resources = getResources();

        for(R resource : resources)
        {
            identifiers.addAll(resource.getIdentifiersForAllTypes());
        }

        return identifiers;
    }

    public Set<I> getAllResourcesChannelIdentifiers(ChannelFilter2<E> filter)
    {
        Set<I> identifiers = new LinkedHashSet<>();

        List<? extends R> resources = getResources();

        for(R resource : resources)
        {
            identifiers.addAll(resource.getIdentifiersForAllTypes(filter));
        }

        return identifiers;
    }

    public Set<I> getSelectedResourcesChannelIdentifiers(String type)
    {
        Set<I> identifiers = new LinkedHashSet<>();

        List<? extends R> selectedResources = getAllSelectedResources();

        for(R resource : selectedResources)
        {
            identifiers.addAll(resource.getIdentifiers(type));
        }

        return identifiers;
    }

    public Set<I> getAllResourcesChannelIdentifiers(String type)
    {
        Set<I> identifiers = new LinkedHashSet<>();

        List<? extends R> resources = getResources();

        for(R resource : resources)
        {
            identifiers.addAll(resource.getIdentifiers(type));
        }

        return identifiers;
    }

    public Set<I> getAllResourcesChannelIdentifiers(String type, ChannelFilter2<E> filter)
    {
        Set<I> identifiers = new LinkedHashSet<>();

        List<? extends R> resources = getResources();

        for(R resource : resources)
        {
            identifiers.addAll(resource.getIdentifiers(type, filter));
        }

        return identifiers;
    }

    public Set<I> getSelectedResourcesChannelIdentifiers(String type, ChannelFilter2<E> filter)
    {
        Set<I> identifiers = new LinkedHashSet<>();

        List<? extends R> selectedResources = getAllSelectedResources();

        for(R resource : selectedResources)
        {
            identifiers.addAll(resource.getIdentifiers(type, filter));
        }

        return identifiers;
    }

    public boolean canUndoBeEnabled()
    {
        boolean undoEnabled = false;

        R resource = getSelectedResource();
        String type = getSelectedType();

        if(resource != null && type != null)
        {
            undoEnabled = resource.canBeUndone(type);
        }

        return undoEnabled;
    }

    public boolean canRedoBeEnabled()
    {
        boolean redoEnabled = false;

        R resource = getSelectedResource();
        String type = getSelectedType();

        if(resource != null && type != null)
        {
            redoEnabled = resource.canBeRedone(type);
        }

        return redoEnabled;
    }


    public boolean canUndoAllBeEnabled()
    {
        boolean undoAllEnabled = false;

        R resource = getSelectedResource();
        String type = getSelectedType();

        if(resource != null && type != null)
        {
            undoAllEnabled = (resource.getCommandToUndoCompundIdentifier(type) != null);
        }

        return undoAllEnabled;
    }    

    public boolean canRedoAllBeEnabled()
    {
        boolean redoAllEnabled = false;

        R resource = getSelectedResource();
        String type = getSelectedType();

        if(resource != null && type != null)
        {
            redoAllEnabled = (resource.getCommandToRedoCompundIdentifier(type) != null);
        }

        return redoAllEnabled;
    }

    public void pushCommands(MetaMap<R, String, UndoableCommand> commands)
    {
        for(Entry<R,Map<String,UndoableCommand>> entry : commands.entrySet())
        {
            R resource = entry.getKey();
            for(Entry<String, UndoableCommand> innerEntry : entry.getValue().entrySet())
            {
                UndoableCommand command = innerEntry.getValue();
                String type = innerEntry.getKey();
                if(command != null)
                {
                    resource.pushCommand(type, command);
                }
            }
        }
    }

    public void redoAll(ResourceView<?,?,?> manager) 
    {
        String selectedType = getSelectedType();
        ChannelResource<E, D, I> selectedResource = getSelectedResource();

        if(selectedType == null || selectedResource == null)
        {
            return;
        }

        CommandIdentifier compoundCommandId = selectedResource.getCommandToRedoCompundIdentifier(selectedType);

        if(compoundCommandId == null)
        {
            return;
        }

        MultiMap<ChannelResource<?, ?, ?>, String> resourcesToRedo = new MultiMap<>();

        List<? extends ChannelResource<E, D, I>> allResources = getResources();

        for(ChannelResource<E, D, I> res : allResources)
        {
            List<String> types = res.getAllTypes();

            for(String type : types)
            {
                CommandIdentifier currentId = res.getCommandToRedoCompundIdentifier(selectedType);
                if(compoundCommandId.equals(currentId))
                {
                    resourcesToRedo.put(res, type);
                }
            }
        }    

        if(!resourcesToRedo.isEmpty())
        {
            ConcurrentRedoTask task = new ConcurrentRedoTask(manager, resourcesToRedo);      
            task.execute();
        }
    }


    public void undoAll(ResourceView<?,?,?> manager)
    {
        String selectedType = getSelectedType();
        ChannelResource<E, D, I> selectedResource = getSelectedResource();

        if(selectedType == null || selectedResource == null)
        {
            return;
        }

        CommandIdentifier compoundCommandId = selectedResource.getCommandToUndoCompundIdentifier(selectedType);

        if(compoundCommandId == null)
        {
            return;
        }

        MultiMap<ChannelResource<?, ?, ?>, String> resourcesToUndo = new MultiMap<>();
        List<? extends ChannelResource<E, D, I>> allResources = getResources();

        for(ChannelResource<E, D, I> res : allResources)
        {
            List<String> types = res.getAllTypes();
            for(String type : types)
            {
                CommandIdentifier currentId = res.getCommandToUndoCompundIdentifier(type);
                if(compoundCommandId.equals(currentId))
                {
                    resourcesToUndo.put(res, type);
                }
            }         
        }    

        if(!resourcesToUndo.isEmpty())
        {
            ConcurrentUndoTask task = new ConcurrentUndoTask(manager, resourcesToUndo);      
            task.execute();       
        }
    }

    public ROI getROIUnion() 
    {
        R selectedResource = getSelectedResource();

        ROI roi = (selectedResource != null) ? selectedResource.getROIUnion() : new ROIComposite("All");
        return roi;
    }

}
