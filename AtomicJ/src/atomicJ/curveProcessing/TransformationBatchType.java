package atomicJ.curveProcessing;

import java.util.List;

import atomicJ.resources.Resource;
import atomicJ.resources.ResourceView;

public enum TransformationBatchType 
{
    ONLY_SELECTED("Only selected") {
        @Override
        public <R extends Resource> List<? extends R> getResources(
                ResourceView<R, ?, ?> resourceManager)
        {
            return resourceManager.getAllSelectedResources();
        }
    }, ALL("All") {
        @Override
        public <R extends Resource> List<? extends R> getResources(
                ResourceView<R, ?, ?> resourceManager) 
        {
            return resourceManager.getResources();
        }
    };

    private final String name;

    TransformationBatchType(String name)
    {
        this.name = name;
    }

    public abstract <R extends Resource> List<? extends R> getResources(ResourceView<R, ?, ?> resourceManager);

    @Override
    public String toString()
    {
        return name;
    }
}
