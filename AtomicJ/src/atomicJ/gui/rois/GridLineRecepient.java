package atomicJ.gui.rois;

public interface GridLineRecepient
{
    //rowFrom, rowTo - exclusive
    public void addLine(int column, int rowFrom, int rowTo);        
}