package atomicJ.readers.regularImage;

import java.util.Collections;
import java.util.List;

import atomicJ.data.Coordinate4D;
import atomicJ.gui.ColorGradient;
import atomicJ.gui.rois.ROIProxy;

public class DummyDensityMetadata implements Channel2DSourceMetadata
{
    private static final DummyDensityMetadata INSTANCE = new DummyDensityMetadata();

    public static DummyDensityMetadata getInstance()
    {
        return INSTANCE;
    }

    private DummyDensityMetadata()
    {}

    @Override
    public void setUseReadInROIs(boolean useReadInROIs)
    {}

    @Override
    public boolean isReadInROIsAvailable() 
    {
        return false;
    }

    @Override
    public List<ROIProxy> getReadInROIs() 
    {
        return Collections.emptyList();
    }    

    @Override
    public boolean isReadInColorGradientAvailable() 
    {
        return false;
    }

    @Override
    public ColorGradient getReadInColorGradient() {
        return null;
    }

    @Override
    public Channel2DSourceMetadata copyIfNecessary()
    {
        return this;
    }

    @Override
    public boolean isUseReadInROIs() {
        return false;
    }

    @Override
    public boolean isUseReadInColorGradient() {
        return false;
    }

    @Override
    public void registerChannelCoordinates(String channelIdentifier,
            Coordinate4D coordinates) {
        // TODO Auto-generated method stub

    }

    @Override
    public Coordinate4D getChannelCoordinates(String channelIdentifier) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Coordinate4D getImageCoordinates() {
        // TODO Auto-generated method stub
        return null;
    }
}