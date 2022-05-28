package atomicJ.sources;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.analysis.ProcessedPackFunction;
import atomicJ.data.Channel2DData;
import atomicJ.data.ChannelDomainIdentifier;
import atomicJ.data.FlexibleChannel2DData;
import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.units.Quantity;
import atomicJ.imageProcessing.Channel2DDataTransformation;

public class ProcessedPackTransformation implements Channel2DDataTransformation
{
    private final ProcessedPackFunction function;
    private final List<ProcessedSpectroscopyPack> packs;

    public ProcessedPackTransformation(List<ProcessedSpectroscopyPack> packsToReplace, ProcessedPackFunction function)
    {
        this.function = function;
        this.packs = new ArrayList<>(packsToReplace);
    }

    @Override
    public Channel2DData transform(Channel2DData channel) 
    {
        if(packs.isEmpty())
        {
            return channel;
        }

        if(channel instanceof GridChannel2DData)
        {
            return transformGridChannel((GridChannel2DData)channel);
        }

        return transformChannel(channel);
    }

    private FlexibleChannel2DData transformChannel(Channel2DData channelData)
    {
        double[] xs = channelData.getXCoordinatesCopy();
        double[] ys = channelData.getYCoordinatesCopy();
        double[] zs = channelData.getZCoordinatesCopy();

        ChannelDomainIdentifier dataDomain = channelData.getDomainIdentifier();
        Quantity xQuantity = channelData.getXQuantity();
        Quantity yQuantity = channelData.getYQuantity();
        Quantity zQuantity = channelData.getZQuantity();

        for(ProcessedSpectroscopyPack pack: packs)
        {
            int index = pack.getSource().getMapPosition();                
            zs[index] = function.evaluate(pack);
        }

        double[][] data = new double[][] {xs, ys, zs};

        FlexibleChannel2DData channelDataNew = new FlexibleChannel2DData(data, dataDomain, xQuantity, yQuantity, zQuantity);

        return channelDataNew;
    }

    private GridChannel2DData transformGridChannel(GridChannel2DData channelData)
    {
        Grid2D grid = channelData.getGrid();
        double[][] gridData = channelData.getDataCopy();

        for(ProcessedSpectroscopyPack pack : packs)
        {
            SimpleSpectroscopySource source = pack.getSource();
            Point2D p = source.getRecordingPoint();

            if(p != null)
            {   
                int i = grid.getRow(p);
                int j = grid.getColumn(p);

                gridData[i][j] = function.evaluate(pack);
            }
        }

        GridChannel2DData channelDataNew = new GridChannel2DData(gridData, grid, channelData.getZQuantity());

        return channelDataNew;
    }
}