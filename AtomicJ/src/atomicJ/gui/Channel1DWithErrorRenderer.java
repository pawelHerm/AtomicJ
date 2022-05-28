package atomicJ.gui;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.prefs.Preferences;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

import atomicJ.data.Channel1DData;
import atomicJ.data.Channel1DDataWithErrors;

public class Channel1DWithErrorRenderer extends Channel1DRenderer
{
    public Channel1DWithErrorRenderer(Channel1DErrorRendererData rendererData, Preferences pref, Object layerKey, StyleTag styleTag, String name)
    {
        super(rendererData, pref, layerKey, styleTag, name);
    }

    public Channel1DWithErrorRenderer(PreferredContinuousSeriesErrorRendererStyle rendererStyle, Object layerKey, StyleTag styleTag, String name)
    {
        super(rendererStyle.getChannel1DRendererData(), rendererStyle.getPreferences(), layerKey, styleTag, name);
    }

    @Override
    public void setData(Channel1DRendererData data)
    {
        Channel1DErrorRendererData dataError = (Channel1DErrorRendererData)data;
        Channel1DErrorRendererData dataNew = data.isImmutable() ? dataError : dataError.getMutableCopy();
        replaceData(dataNew);
    }

    public Stroke getBarStroke()
    {
        return getData().getBarStroke();
    }

    public void setBarStroke(Stroke barStroke)
    {
        getDataForModification().setBarStroke(barStroke);
    }

    public Paint getBarPaint()
    {
        return getData().getBarPaint();
    }

    public void setBarPaint(Paint barBaint)
    {
        getDataForModification().setBarPaint(barBaint);
    }

    public boolean isCapVisible()
    {
        return getData().isCapVisible();
    }

    public void setCapVisible(boolean capVisible)
    {
        getDataForModification().setCapVisible(capVisible);
    }

    public Paint getCapPaint()
    {
        return getData().getCapPaint();
    }

    public void setCapPaint(Paint capPaint)
    {
        getDataForModification().setCapPaint(capPaint);
    }

    public Stroke getCapStroke()
    {
        return getData().getCapStroke();
    }

    public void setCapStroke(Stroke capStrokes)
    {
        getDataForModification().setCapStroke(capStrokes);
    } 

    public double getCapWidth()
    {
        return getData().getCapWidth();
    }

    public void setCapWidth(double capWidth)
    {
        getDataForModification().setCapWidth(capWidth);
    }

    public ErrorBarDirection getErrorBarDrawingDirection() 
    {
        return getData().getErrorBarDrawingDirection();
    };

    public void setErrorBarDrawingDirection(ErrorBarDirection drawingDirectionNew) 
    {
        getDataForModification().setErrorBarDrawingDirection(drawingDirectionNew);
    };

    @Override
    public Channel1DErrorRendererDataImmutable getImmutableData()
    {
        return getData().getImmutableVersion();
    }

    @Override
    public Channel1DErrorRendererData getData()
    {
        return (Channel1DErrorRendererData)super.getData();
    }

    @Override
    protected Channel1DErrorRendererDataMutable getDataForModification()
    {
        Channel1DErrorRendererData rendererData = getData();
        if(!rendererData.isImmutable())
        {
            //current data are mutable
            return rendererData.getMutableVersion();
        }

        //current data are immutable
        Channel1DErrorRendererDataMutable rendererDataNew = rendererData.getMutableCopy();
        replaceData(rendererDataNew);

        return rendererDataNew;
    }


    @Override
    protected void drawSecondaryPass(Graphics2D g2, XYPlot plot, XYDataset dataset,
            int pass,  int firstItem, int lastItem,
            ValueAxis domainAxis,
            Rectangle2D dataArea,
            ValueAxis rangeAxis,
            CrosshairState crosshairState, EntityCollection entities)
    {     
        if(dataset instanceof Channel1DDataset)
        {
            Channel1DDataset channelDataset = (Channel1DDataset)dataset;
            Channel1DData channelData = channelDataset.getDisplayedChannel().getChannelData();

            if(channelData instanceof Channel1DDataWithErrors)
            {
                Channel1DErrorRendererData rendererData = getData();
                Stroke barStroke = rendererData.getBarStroke();
                Paint barPaint = rendererData.getBarPaint();
                Paint capPaint = rendererData.getCapPaint();
                Stroke capStroke = rendererData.getCapStroke();
                double capWidth = rendererData.getCapWidth();
                boolean capVisible = rendererData.isCapVisible();
                ErrorBarDirection barDirection = rendererData.getErrorBarDrawingDirection();
                boolean negativeErrorBarSideDrawn = barDirection.isNegativeSideDrawn();
                boolean positiveErrorBarSideDrawn = barDirection.isPositiveSideDrawn();

                Channel1DDataWithErrors channel1DDataWithErrors = (Channel1DDataWithErrors)channelData;

                RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();
                RectangleEdge rangeAxisEdge = plot.getRangeAxisEdge();          
                PlotOrientation orientation = plot.getOrientation();

                for (int item = firstItem; item <= lastItem; item++) 
                {
                    // get the data point...
                    double x1 = dataset.getXValue(0, item);
                    double y1 = dataset.getYValue(0, item);
                    if (Double.isNaN(y1) || Double.isNaN(x1)) {
                        continue;
                    }

                    double meanX = channel1DDataWithErrors.getX(item);
                    double errorBarStart = negativeErrorBarSideDrawn ? channel1DDataWithErrors.getYMinusError(item) :channel1DDataWithErrors.getY(item);
                    double errorBarEnd = positiveErrorBarSideDrawn ? channel1DDataWithErrors.getYPlusError(item) : channel1DDataWithErrors.getY(item);

                    if(orientation == PlotOrientation.VERTICAL)
                    {
                        double errorBarStart2D = rangeAxis.valueToJava2D(errorBarStart, dataArea, rangeAxisEdge);
                        double errorBarEnd2D = rangeAxis.valueToJava2D(errorBarEnd, dataArea, rangeAxisEdge);
                        double meanXj2D = domainAxis.valueToJava2D(meanX, dataArea, domainAxisEdge);

                        g2.setStroke(barStroke);
                        g2.setPaint(barPaint);
                        Line2D line = new Line2D.Double(meanXj2D,errorBarStart2D,meanXj2D, errorBarEnd2D);
                        g2.draw(line);

                        if(capVisible)
                        {
                            g2.setStroke(capStroke);
                            g2.setPaint(capPaint);

                            if(negativeErrorBarSideDrawn)
                            {
                                Line2D lowerCapLine = new Line2D.Double(meanXj2D - capWidth/2, errorBarStart2D,meanXj2D + capWidth/2, errorBarStart2D);
                                g2.draw(lowerCapLine);
                            }

                            if(positiveErrorBarSideDrawn)
                            {
                                Line2D upperCapLine = new Line2D.Double(meanXj2D - capWidth/2, errorBarEnd2D,meanXj2D + capWidth/2, errorBarEnd2D);
                                g2.draw(upperCapLine); 
                            }                           
                        }
                    }
                    else
                    {
                        double errorBarStart2D = rangeAxis.valueToJava2D(errorBarStart, dataArea, rangeAxisEdge);
                        double errorBarEnd2D = rangeAxis.valueToJava2D(errorBarEnd, dataArea, rangeAxisEdge);
                        double meanXj2D = domainAxis.valueToJava2D(meanX, dataArea, domainAxisEdge);

                        Line2D line = new Line2D.Double(errorBarStart2D, meanXj2D, errorBarEnd2D, meanXj2D);

                        g2.setStroke(barStroke);
                        g2.setPaint(barPaint);
                        g2.draw(line);

                        if(capVisible)
                        {
                            g2.setStroke(capStroke);
                            g2.setPaint(capPaint);

                            if(negativeErrorBarSideDrawn)
                            {
                                Line2D lowerCapLine = new Line2D.Double(errorBarStart2D, meanXj2D - capWidth/2, errorBarStart2D, meanXj2D + capWidth/2);
                                g2.draw(lowerCapLine);
                            }

                            if(positiveErrorBarSideDrawn)
                            {
                                Line2D upperCapLine = new Line2D.Double(errorBarEnd2D, meanXj2D - capWidth/2, errorBarEnd2D, meanXj2D + capWidth/2);
                                g2.draw(upperCapLine);
                            }
                        }
                    }

                } 
            }
        }

        super.drawSecondaryPass(g2, plot, dataset, pass, firstItem, lastItem, domainAxis, dataArea, rangeAxis, crosshairState, entities);
    }
}
