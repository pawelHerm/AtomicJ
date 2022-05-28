package atomicJ.gui;

import java.util.prefs.Preferences;

import org.jfree.chart.axis.Axis;

public interface AxisSource 
{
    public boolean hasDomainAxes();
    public int getDomainAxisCount();
    public Axis getDomainAxis(int index);
    public String getDomainAxisName(int index);
    public Preferences getDomainAxisPreferences(int index);

    public boolean hasRangeAxes();
    public int getRangeAxisCount();
    public Axis getRangeAxis(int index);
    public String getRangeAxisName(int index);
    public Preferences getRangeAxisPreferences(int index);

    public boolean hasDepthAxes();
    public int getDepthAxisCount();
    public Axis getDepthAxis(int index);
    public String getDepthAxisName(int index);
    public Preferences getDepthAxisPreferences(int index);
}   
