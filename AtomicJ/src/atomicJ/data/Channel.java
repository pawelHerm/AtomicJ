package atomicJ.data;

import org.jfree.data.Range;

import atomicJ.data.units.Quantity;

public interface Channel
{
    public String getIdentifier();
    public int getItemCount();
    public double getX(int item);
    public double getY(int item);
    public Range getXRange();
    public Range getYRange();
    public Quantity getXQuantity();
    public Quantity getYQuantity();
}
