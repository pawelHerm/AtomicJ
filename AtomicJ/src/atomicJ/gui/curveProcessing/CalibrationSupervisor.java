package atomicJ.gui.curveProcessing;

import java.awt.Cursor;

import org.jfree.data.Range;

public interface CalibrationSupervisor 
{
    public Range getRange();
    public void setRange(Range range);
    public void requestLowerRangeBound(double lowerBound);    
    public void requestUpperRangeBound(double upperBound);
    public Range getCurrentMaximumRange();
    public void requestCursorChange(Cursor cursor);
}
