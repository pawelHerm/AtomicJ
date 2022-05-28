package atomicJ.gui;

import java.util.List;

import atomicJ.resources.Resource;

public interface ResourceCollection <R extends Resource> extends PropertyChangeSource
{
    public static final String RESOURCES_EMPTY = "ResourcesEmpty";
    public static final String RESOURCE_COUNT = "ResourceCount";
    public int getResourceCount();     
    public boolean isEmpty();
    public List<R> getResources();
}
