package atomicJ.gui.generalProcessing;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import atomicJ.data.Channel;
import atomicJ.gui.undo.CommandIdentifier;
import atomicJ.gui.undo.UndoableCommand;
import atomicJ.resources.ChannelResource;
import atomicJ.resources.ResourceView;

public abstract class UndoableBasicCommand <R extends ChannelResource<E, D, I>, E extends Channel, D, I> implements UndoableCommand
{
    private boolean undone;

    private final R resource;
    private final String type;

    private final Map<I, D> dataMap;
    private final Set<I> identifiers;

    private final ResourceView<R, E, I> manager;
    private final CommandIdentifier compundCommandIdentifier;

    public UndoableBasicCommand(ResourceView<R, E, I> manager, String type, R resource)
    {
        this(manager, type, null, resource, null);
    }

    public UndoableBasicCommand(ResourceView<R, E, I> manager, String type, Set<I> identifiers, R resource, CommandIdentifier id)
    {
        this.manager = manager;
        this.resource = resource;
        this.type = type;
        this.dataMap = resource.getChannelData(type);
        this.compundCommandIdentifier = id;
        this.identifiers = (identifiers != null) ? Collections.unmodifiableSet(identifiers) : null;
    }

    @Override
    public CommandIdentifier getCompundCommandIdentifier()
    {
        return compundCommandIdentifier;
    }

    public Set<I> getIdentifiers()
    {
        return identifiers;
    }

    protected ResourceView<R, E, I> getResourceManager()
    {
        return manager;
    }

    public String getType()
    {
        return type;
    }

    public R getResource()
    {
        return resource;
    }

    @Override
    public void undo() 
    {
        if(canBeUndone())
        {      
            Map<I, E> changedChannels = resource.setChannelData(type, dataMap);
            handleChangeOfData(changedChannels);

            this.undone = true;
        }           
    }

    protected void undoQuiet() 
    {
        if(canBeUndone())
        {      
            resource.setChannelData(type, dataMap);
        }           
    }


    @Override
    public boolean isExecuted()
    {
        return !undone;
    }

    @Override
    public void execute()
    {
        this.undone = false;
    }

    protected void handleChangeOfData(Map<I, E> channelsMap)
    {
        manager.handleChangeOfData(channelsMap, type, resource);
    }

    @Override
    public void redo() 
    {
        if(canBeRedone())
        {
            execute();
            this.undone = false;
        }
    }

    @Override
    public boolean canBeUndone() 
    {
        boolean canBeUndone = !undone;

        return canBeUndone;
    }

    @Override
    public boolean canBeRedone() 
    {
        boolean canBeRedone = undone;

        return canBeRedone;
    }
}