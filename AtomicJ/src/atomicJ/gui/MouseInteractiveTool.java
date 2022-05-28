package atomicJ.gui;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

public interface MouseInteractiveTool extends MouseInputResponse
{
    public void notifyOfToolModeLoss(); 
    public Set<MouseInputType> getUsedMouseInputTypes();
    public boolean isComplexElementUnderConstruction();
    public void addMouseToolListener(MouseInteractiveToolListener listener);
    public void removeMouseToolListerner(MouseInteractiveToolListener listener);
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis,int rendererIndex, PlotRenderingInfo info);

    public static interface MouseInteractiveToolListener
    {
        public void toolToRedraw();
    }

    public static class MouseInteractiveToolListenerSupport
    {
        private final List<MouseInteractiveToolListener> listeners = new ArrayList<>();

        public void addMouseToolListener(MouseInteractiveToolListener listener)
        {
            if(!listeners.contains(listener))
            {
                listeners.add(listener);
            }
        }

        public void removeMouseListener(MouseInteractiveToolListener listener)
        {
            listeners.remove(listener);
        }

        public void fireToolToRedraw()
        {
            for(MouseInteractiveToolListener listener : listeners)
            {
                listener.toolToRedraw();
            }
        }
    }
}
