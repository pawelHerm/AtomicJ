package atomicJ.gui;

import org.jfree.data.xy.XYDomainInfo;
import org.jfree.data.xy.XYRangeInfo;

import atomicJ.curveProcessing.Channel1DDataTransformation;
import atomicJ.data.Channel1DData;
import atomicJ.data.ChannelGroupTag;

public interface ProcessableXYDataset<D> extends ProcessableDataset<D>, XYDomainInfo, XYRangeInfo
{
    public void setData(int seriesNo, Channel1DData data, boolean notify);
    public Channel1DDataset getCopy(Comparable<?> keyNew);
    public boolean isNamedAs(Comparable<?> key);
    public void changeName(Comparable<?> keyNew);
    public void transform(Channel1DDataTransformation tr);
    public ChannelGroupTag getGroupTag();
}
