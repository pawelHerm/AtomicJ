package atomicJ.gui.rois;

public enum ROIRelativePosition 
{
    EVERYTHING(false), INSIDE(true), OUTSIDE(true);

    private final boolean  roiDependent;

    ROIRelativePosition(boolean roiDependet)
    {
        this.roiDependent = roiDependet;
    }

    public boolean isROIDependent()
    {
        return roiDependent;
    }
}
