package atomicJ.gui;

import java.awt.Cursor;
import java.awt.geom.Point2D;

import atomicJ.data.Channel1D;
import atomicJ.data.ChannelGroupTag;

public interface Channel1DModificationSupervisor
{
    public boolean isValidValue(Channel1D channel, int itemIndex, double[] newValue);
    public Point2D correctPosition(Channel1D channel, int itemIndex, Point2D dataPoint);
    public void itemMoved(Channel1D channel, int itemIndex, double[] newValue);
    public void channelTranslated(Channel1D channel);
    public void itemAdded(Channel1D channel, double[] itemNew);   
    public void channelAdded(Channel1D channel);
    public void channelRemoved(Channel1D channel);
    public ChannelGroupTag getNextGroupMemberTag(Object groupKey);
    public void requestCursorChange(Cursor cursor);
}