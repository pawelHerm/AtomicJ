package atomicJ.gui.annotations;


public interface AnnotationAnchorSigned
{
    public AnnotationAnchorSigned getInnerAnchor();
    public AnnotationAnchorCore getCoreAnchor();

    public Object getKey();
    public Object getSourceKey();
}
