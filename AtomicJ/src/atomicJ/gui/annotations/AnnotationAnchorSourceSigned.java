package atomicJ.gui.annotations;

public class AnnotationAnchorSourceSigned implements AnnotationAnchorSigned
{
    private final AnnotationAnchorCore anchor;
    private final Object sourceKey;

    public AnnotationAnchorSourceSigned(AnnotationAnchorCore anchor, Object sourceKey)
    {
        this.anchor = anchor;
        this.sourceKey = sourceKey;
    }

    @Override
    public AnnotationAnchorCore getCoreAnchor()
    {
        return anchor;
    }

    @Override
    public Object getSourceKey()
    {
        return sourceKey;
    }

    @Override
    public AnnotationAnchorSourceSigned getInnerAnchor()
    {
        return this;
    }

    @Override
    public Object getKey()
    {
        return sourceKey;
    }

    public boolean isSource()
    {
        return true;
    }
}
