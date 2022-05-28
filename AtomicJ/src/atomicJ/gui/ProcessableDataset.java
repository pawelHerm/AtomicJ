package atomicJ.gui;

import org.jfree.data.DomainInfo;
import org.jfree.data.RangeInfo;
import org.jfree.data.xy.XYDataset;

import atomicJ.data.units.Quantity;

public interface ProcessableDataset<D> extends XYDataset, DomainInfo, RangeInfo
{
    public double[][] getData(int seriesNo);
    public double[][] getDataCopy(int seriesIndex);

    public void setData(int seriesNo, D data);
    public void setData(int seriesNo, D data, boolean notify);
    public void setDataRecentlyChanged(boolean dataRecentlyChanged);
    public void notifyOfDataChange(boolean notifyListeners);
    public Quantity getXQuantity();
    public Quantity getYQuantity();
    public Object getKey();
}
