package atomicJ.gui.annotations;


public class AnnotationAnchorWrappedSigned implements AnnotationAnchorSigned
{
    private final AnnotationAnchorSigned innerAnchor;
    private final Object componentKey;

    public AnnotationAnchorWrappedSigned(AnnotationAnchorSigned innerAnchor, Object componentKey)
    {
        if(innerAnchor == null)
        {
            throw new IllegalArgumentException("Inner anchor cannot be null");
        }
        this.innerAnchor = innerAnchor;
        this.componentKey = componentKey;
    }

    @Override
    public AnnotationAnchorSigned getInnerAnchor()
    {
        return innerAnchor;
    }

    @Override
    public Object getSourceKey() 
    {
        return innerAnchor.getSourceKey();
    }

    @Override
    public AnnotationAnchorCore getCoreAnchor()
    {
        return innerAnchor.getCoreAnchor();
    }

    @Override
    public Object getKey()
    {
        return componentKey;
    }

    public boolean isSource()
    {
        return false;
    }
}
