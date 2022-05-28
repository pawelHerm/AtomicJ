package atomicJ.gui;

import java.util.List;

import atomicJ.resources.Resource;

public interface ResourceGroupListener <R extends Resource>
{
    public void resourceCleared();
    public void resourceSet(int index, R resourceOld, R resourceNew);
    public void resourceAdded(R resource);
    public void resourcesAdded(List<? extends R> resourcesAdded);
    public void resourceRemoved(int index, R removedResource);
    public void resourcesRemoved(List<? extends R> resourcesRemoved);
}