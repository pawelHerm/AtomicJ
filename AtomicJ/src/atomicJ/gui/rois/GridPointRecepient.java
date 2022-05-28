package atomicJ.gui.rois;

public interface GridPointRecepient
{
    public void addPoint(int row, int column);

    //rowFrom, columnFrom - inclusive; rowTo, columnTo - exclusive
    public void addBlock(int rowFrom, int rowTo, int columnFrom, int columnTo);        
}