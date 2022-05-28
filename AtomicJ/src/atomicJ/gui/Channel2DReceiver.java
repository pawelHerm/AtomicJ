package atomicJ.gui;

import java.awt.Window;

import atomicJ.data.Channel2D;


public interface Channel2DReceiver 
{
    public void setGridChannel(Channel2D gridChannel);
    public Window getParent();
}
