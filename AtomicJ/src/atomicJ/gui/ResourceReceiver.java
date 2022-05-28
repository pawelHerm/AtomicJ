package atomicJ.gui;

import java.util.Set;

import atomicJ.resources.Resource;


public interface ResourceReceiver<R extends Resource>
{
    public void setResource(R r, Set<String> types);
}
