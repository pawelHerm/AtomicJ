package atomicJ.gui;


public interface TooltipManagerSource 
{
    public boolean hasDomainTooltipManagers();
    public int getDomainTooltipManagerCount();
    public TooltipStyleManager getDomainTooltipManager(int index);

    public boolean hasRangeTooltipManagers();
    public int getRangeTooltipManagerCount();
    public TooltipStyleManager getRangeTooltipManager(int index);

    public boolean hasDepthTooltipManagers();
    public int getDepthTooltipManagerCount();
    public TooltipStyleManager getDepthTooltipManager(int index);
}   
