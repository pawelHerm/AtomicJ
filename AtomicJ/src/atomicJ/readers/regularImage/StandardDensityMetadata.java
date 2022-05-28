package atomicJ.readers.regularImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import atomicJ.data.Coordinate4D;
import atomicJ.data.units.UnitExpression;
import atomicJ.gui.ColorGradient;
import atomicJ.gui.rois.ROIProxy;

public class StandardDensityMetadata implements Channel2DSourceMetadata
{        
    private UnitExpression timePosition = null;
    private UnitExpression xPosition = null;
    private UnitExpression yPosition = null;
    private UnitExpression zPosition = null;

    private boolean useReadInROIs = false;
    private boolean useReadInColorGradient = false;
    private List<ROIProxy> readInROIs = Collections.emptyList();
    private ColorGradient suggestedColorGradient;

    private final Map<String, Coordinate4D> channelCoordinates = new LinkedHashMap<>();

    public UnitExpression getTimePosition()
    {
        return timePosition;
    }

    public void setTimePosition(UnitExpression timePosition)
    {
        this.timePosition = timePosition;
    }

    public UnitExpression getXPosition()
    {
        return xPosition;
    }

    public void setXPosition(UnitExpression xPosition)
    {
        this.xPosition = xPosition;
    }

    public UnitExpression getYPosition()
    {
        return yPosition;
    }

    public void setYPosition(UnitExpression yPosition)
    {
        this.yPosition = yPosition;
    }

    public UnitExpression getZPosition()
    {
        return zPosition;
    }

    public void setZPosition(UnitExpression zPosition)
    {
        this.zPosition = zPosition;
    }

    public void setReadInROIs(List<ROIProxy> readInROIs)
    {
        this.readInROIs = new ArrayList<>(readInROIs);
    }  

    @Override
    public boolean isUseReadInROIs() 
    {
        return useReadInROIs;
    }

    @Override
    public void setUseReadInROIs(boolean useReadInROIs)
    {
        this.useReadInROIs = useReadInROIs;
    }

    @Override
    public boolean isUseReadInColorGradient() 
    {
        return useReadInColorGradient;
    }

    public void setUseReadInColorGradient(boolean useReadInColorGradient)
    {
        this.useReadInColorGradient = useReadInColorGradient;
    }

    @Override
    public boolean isReadInROIsAvailable() 
    {
        return !readInROIs.isEmpty();
    }

    @Override
    public List<ROIProxy> getReadInROIs()
    {
        return Collections.unmodifiableList(readInROIs);
    }

    public void setSuggestedColorGradient(ColorGradient suggestedColorGradient)
    {
        this.suggestedColorGradient = suggestedColorGradient;
    }

    @Override
    public boolean isReadInColorGradientAvailable() 
    {
        boolean available = (suggestedColorGradient != null);
        return available;
    }

    @Override
    public ColorGradient getReadInColorGradient()
    {
        return suggestedColorGradient;
    }

    @Override
    public StandardDensityMetadata copyIfNecessary()
    {
        StandardDensityMetadata copy = new StandardDensityMetadata();
        copy.readInROIs = new ArrayList<>(readInROIs);
        copy.suggestedColorGradient = suggestedColorGradient;

        return copy;
    }

    @Override
    public void registerChannelCoordinates(String channelIdentifier, Coordinate4D coordinates) 
    {        
        channelCoordinates.put(channelIdentifier, coordinates);
    }

    @Override
    public Coordinate4D getChannelCoordinates(String channelIdentifier) 
    {
        return channelCoordinates.get(channelIdentifier);
    }

    @Override
    public Coordinate4D getImageCoordinates()
    {
        return Coordinate4D.getCommonCoordinates(new ArrayList<>(channelCoordinates.values()));
    }
}