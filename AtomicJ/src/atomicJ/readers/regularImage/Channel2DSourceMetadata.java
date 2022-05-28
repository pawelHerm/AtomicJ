package atomicJ.readers.regularImage;

import java.util.List;

import atomicJ.data.Coordinate4D;
import atomicJ.gui.ColorGradient;
import atomicJ.gui.rois.ROIProxy;

public interface Channel2DSourceMetadata
{
    public boolean isUseReadInROIs();
    public void setUseReadInROIs(boolean useReadInROIs);
    public boolean isUseReadInColorGradient();
    public List<ROIProxy> getReadInROIs();
    public boolean isReadInROIsAvailable();
    public ColorGradient getReadInColorGradient();
    public boolean isReadInColorGradientAvailable();
    public void registerChannelCoordinates(String channelIdentifier, Coordinate4D coordinates);
    public Coordinate4D getChannelCoordinates(String channelIdentifier);
    public Coordinate4D getImageCoordinates();
    public Channel2DSourceMetadata copyIfNecessary();
}