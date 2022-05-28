package atomicJ.readers.regularImage;

import java.util.List;

import atomicJ.data.Coordinate4D;
import atomicJ.data.units.UnitExpression;

public interface ChannelProvider
{
    public double[][][] getChannelData(ImageInterpretationModel model);
    public int getRowCount();
    public int getColumnCount();
    public UnitExpression getXLength();
    public UnitExpression getYLength();
    public String getColorSpaceName();
    public Channel2DSourceMetadata getImageMetadata();
    public List<String> getChannelNames();
    public Coordinate4D getChannelCoordinates(int channelIndex);
    public Coordinate4D getCombinedChannelCoordinates();
}