package atomicJ.gui;

import java.util.List;

import atomicJ.resources.Resource;

public class ResourceGroupAdapter <R extends Resource> implements ResourceGroupListener<R>
{
    @Override
    public void resourceCleared(){}
    @Override
    public void resourceSet(int index, R resourceOld, R resourceNew){}
    @Override
    public void resourceAdded(R resource){}
    @Override
    public void resourcesAdded(List<? extends R> resourcesAdded){}
    @Override
    public void resourceRemoved(int index, R removedResource){}
    @Override
    public void resourcesRemoved(List<? extends R> resourcesRemoved){}
}