package atomicJ.gui.imageProcessing;

public class KernelColumnChangeEvent 
{   
    private final int yRadiusOld;
    private final int yRadiusNew;

    public KernelColumnChangeEvent(int yRadiusOld, int yRadiusNew)
    {
        this.yRadiusOld = yRadiusOld;
        this.yRadiusNew = yRadiusNew;       
    }

    public int getYRadiusOld()
    {
        return yRadiusOld;
    }

    public int getYRadiusNew()
    {
        return yRadiusNew;
    }

    public int getDifference()
    {
        return yRadiusNew - yRadiusOld;
    }
}
