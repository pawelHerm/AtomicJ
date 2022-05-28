package atomicJ.gui.boxplots;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;


import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.BoxAndWhiskerXYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.Range;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PaintUtilities;

import atomicJ.gui.ChannelRenderer;
import atomicJ.gui.ShapeSupplier;
import atomicJ.gui.StyleTag;

public class CustomizableXYBoxAndWhiskerRenderer extends AbstractXYItemRenderer implements ChannelRenderer
{
    private static final long serialVersionUID = 1L;

    private final StyleTag style;
    private String name;
    private final Preferences pref;   

    //box   
    private double boxWidth;
    private boolean boxFilled;
    private transient Paint boxFillPaint;
    private boolean boxOutlineVisible;

    //whiskers
    private transient Stroke whiskerStroke;
    private transient Paint whiskerPaint;
    private boolean whiskerCrossBarVisible;
    private double whiskerCrossBarWidth;
    private transient Paint whiskerCrossBarPaint;
    private transient Stroke whiskerCrossBarStroke;

    //median   
    private boolean medianVisible;
    private transient Paint medianPaint;
    private transient Stroke medianStroke;

    //mean
    private boolean meanVisible;   
    private boolean meanFilled;
    private transient Paint meanFillPaint;  
    private boolean meanOutlineVisible;
    private transient Paint meanOutlinePaint;
    private transient Stroke meanOutlineStroke;

    //outliers
    private boolean outliersVisible;
    private int outlierMarkerIndex;
    private float outlierSize;   
    private boolean outlierFilled;
    private transient Paint outlierFillPaint;
    private boolean outlierOutlineVisible;
    private transient Stroke outlierStroke;
    private transient Paint outlierStrokePaint;


    //    double upperOutlierThreshold = q3 + (interQuartileRange * 1.5);
    //    double lowerOutlierThreshold = q1 - (interQuartileRange * 1.5);
    //    
    //    double upperFaroutThreshold = q3 + (interQuartileRange * 2.0);
    //    double lowerFaroutThreshold = q1 - (interQuartileRange * 2.0);

    //maxRegular value is the largest value present in the sample that os smaller then upperOutlierThreshold
    public CustomizableXYBoxAndWhiskerRenderer(StyleTag key, String name)
    {
        this(key, name, -1);
    }

    public CustomizableXYBoxAndWhiskerRenderer(StyleTag style, String name, double boxWidth) 
    {
        this.style = style;
        this.name = name;
        this.pref = Preferences.userNodeForPackage(getClass()).node(getClass().getName()).node(style.getPreferredStyleKey());   

        setBaseToolTipGenerator(new BoxAndWhiskerXYToolTipGenerator());

        PreferredBoxAndWhiskerRendererStyle preferredStyle = PreferredBoxAndWhiskerRendererStyle.getInstance(this.pref, style);
        setPreferredStyle(preferredStyle);
    }

    @Override
    public String getName()
    {
        return name;
    }

    public PreferredBoxAndWhiskerRendererStyle getPreferredRendererStyle()
    {
        return PreferredBoxAndWhiskerRendererStyle.getInstance(this.pref, style);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) {

        PlotOrientation orientation = plot.getOrientation();
        boolean isHorizontal = (orientation == PlotOrientation.HORIZONTAL);

        AffineTransform origTransform = g2.getTransform();

        if(isHorizontal)
        {            
            AffineTransform newTransform = new AffineTransform(0, 1, 1, 0, 0, 0);
            g2.transform(newTransform);
        }

        // setup for collecting optional entity info...
        EntityCollection entities = null;
        if (info != null) {
            entities = info.getOwner().getEntityCollection();
        }

        BoxAndWhiskerXYDataset data
        = (BoxAndWhiskerXYDataset) dataset;

        Number x = data.getX(series, item);
        Number yMax = data.getMaxRegularValue(series, item);
        Number yMin = data.getMinRegularValue(series, item);
        Number yMedian = data.getMedianValue(series, item);
        Number yAverage = data.getMeanValue(series, item);
        Number yQ1Median = data.getQ1Value(series, item);
        Number yQ3Median = data.getQ3Value(series, item);
        List<?> yOutliers = data.getOutliers(series, item);
        // yOutliers can be null, but we'd prefer it to be an empty list in
        // that case...
        if (yOutliers == null) {
            yOutliers = Collections.EMPTY_LIST;
        }

        double xx = domainAxis.valueToJava2D(x.doubleValue(), dataArea,plot.getDomainAxisEdge());

        RectangleEdge rangeLocation = plot.getRangeAxisEdge();
        double yyMax = rangeAxis.valueToJava2D(yMax.doubleValue(), dataArea, rangeLocation);
        double yyMin = rangeAxis.valueToJava2D(yMin.doubleValue(), dataArea, rangeLocation);
        double yyMedian = rangeAxis.valueToJava2D(yMedian.doubleValue(), dataArea, rangeLocation);
        double yyAverage = 0.0;
        if (yAverage != null) {
            yyAverage = rangeAxis.valueToJava2D(yAverage.doubleValue(),
                    dataArea, rangeLocation);
        }
        double yyQ1Median = rangeAxis.valueToJava2D(yQ1Median.doubleValue(), dataArea, rangeLocation);
        double yyQ3Median = rangeAxis.valueToJava2D(yQ3Median.doubleValue(), dataArea, rangeLocation);

        double exactBoxWidth = this.boxWidth;
        double width = exactBoxWidth;
        double dataAreaX = dataArea.getWidth();
        double maxBoxPercent = 0.1;
        double maxBoxWidth = dataAreaX * maxBoxPercent;

        //calculates automatic box width
        if (exactBoxWidth <= 0.0) 
        {
            int itemCount = data.getItemCount(series);
            exactBoxWidth = dataAreaX / itemCount * 4.5 / 7;
            if (exactBoxWidth < 3) 
            {
                width = 3;
            }
            else if (exactBoxWidth > maxBoxWidth)
            {
                width = maxBoxWidth;
            }
            else {
                width = exactBoxWidth;
            }
        }

        double halfBoxWidth = width/2;

        /////////////////// DRAW WHISKERS ////////////////////////////////////


        g2.setStroke(this.whiskerStroke);
        g2.setPaint(this.whiskerPaint);

        // draw the vertical part of the upper whisker
        g2.draw(new Line2D.Double(xx, yyMax, xx, yyQ3Median));

        // draw the vertical part of the lower whisker
        g2.draw(new Line2D.Double(xx, yyMin, xx, yyQ1Median));

        if(this.whiskerCrossBarVisible)
        {
            g2.setPaint(this.whiskerCrossBarPaint);
            g2.setStroke(this.whiskerCrossBarStroke);

            // draw the crossbar of the upper whisker

            double absoluteCrossBarWidth = halfBoxWidth*whiskerCrossBarWidth/100.;

            g2.draw(new Line2D.Double(xx - absoluteCrossBarWidth, yyMax, xx + absoluteCrossBarWidth, yyMax));

            // draw the crossbar of the lower whisker
            g2.draw(new Line2D.Double(xx - absoluteCrossBarWidth, yyMin, xx + absoluteCrossBarWidth, yyMin)); 
        }  

        /////////////////// DRAW BOX ////////////////////////////////////

        Shape box = new Rectangle2D.Double(xx - halfBoxWidth, Math.min(yyQ3Median, yyQ1Median),
                width, Math.abs(yyQ1Median - yyQ3Median));

        if (this.boxFilled) 
        {
            g2.setPaint(this.boxFillPaint);
            g2.fill(box);
        }

        if(this.boxOutlineVisible)
        {
            g2.setStroke(getItemOutlineStroke(series, item));
            g2.setPaint(getItemOutlinePaint(series, item));
            g2.draw(box);
        }

        /////////////////// DRAW MEDIAN ////////////////////////////////////

        if(this.medianVisible)
        {
            // draw median
            g2.setPaint(this.medianPaint);
            g2.setStroke(this.medianStroke);
            g2.draw(new Line2D.Double(xx - halfBoxWidth, yyMedian, xx + halfBoxWidth, yyMedian));
        }


        /////////////////// DRAW MEAN ////////////////////////////////////


        if(this.meanVisible)
        {
            // draw mean
            if (yAverage != null)
            {
                double meanSize = width / 4;

                Ellipse2D.Double meanMarker = new Ellipse2D.Double(xx - meanSize,
                        yyAverage - meanSize, meanSize * 2, meanSize * 2);

                if(this.meanFilled)
                {
                    g2.setPaint(this.meanFillPaint);
                    g2.fill(meanMarker);
                }

                if(this.meanOutlineVisible)
                {
                    g2.setStroke(this.meanOutlineStroke);
                    g2.setPaint(this.meanOutlinePaint);
                    g2.draw(meanMarker);
                }
            }
        }


        double maxRegularValue = data.getMaxRegularValue(series,item).doubleValue();
        double minRegularValue = data.getMinRegularValue(series,item).doubleValue();

        Area joinedOutlierArea = new Area();

        if(this.outliersVisible)
        {
            for(int i = 0; i < yOutliers.size(); i++) 
            {
                double outlier = ((Number) yOutliers.get(i)).doubleValue();

                if (outlier > maxRegularValue || outlier < minRegularValue) 
                {
                    double yyOutlier = rangeAxis.valueToJava2D(outlier, dataArea, rangeLocation);

                    Shape shape = ShapeSupplier.createShape(outlierMarkerIndex, (float)xx, (float)yyOutlier, outlierSize, outlierSize);

                    joinedOutlierArea.add(new Area(shape));

                }              
            }

            if(this.outlierFilled)
            {
                g2.setPaint(this.outlierFillPaint);
                g2.fill(joinedOutlierArea);
            }
            if(this.outlierOutlineVisible)
            {
                g2.setPaint(this.outlierStrokePaint);
                g2.setStroke(this.outlierStroke);
                g2.draw(joinedOutlierArea);
            }

        }



        // add an entity for the item...
        if (entities != null && box.intersects(dataArea)) {
            addEntity(entities, box, dataset, series, item, xx, yyAverage);
        }

        g2.setTransform(origTransform);
    }

    //to keep the color of the legend item correct
    @Override
    public Paint lookupSeriesPaint(int series)
    {
        return boxFillPaint;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CustomizableXYBoxAndWhiskerRenderer)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        CustomizableXYBoxAndWhiskerRenderer that = (CustomizableXYBoxAndWhiskerRenderer) obj;

        /////////////////////////// BOX ///////////////////////////////////

        if (this.boxWidth != that.getBoxWidth()) 
        {
            return false;
        }
        if (!PaintUtilities.equal(this.boxFillPaint, that.boxFillPaint))
        {
            return false;
        }
        if (this.boxFilled != that.boxFilled)
        {
            return false;
        }
        if(this.boxOutlineVisible != that.boxOutlineVisible)
        {
            return false;
        }


        //////////////////////////////// WHISKERS ////////////////////////////////

        if(this.whiskerCrossBarVisible != that.whiskerCrossBarVisible)
        {
            return false;
        }
        if(this.whiskerCrossBarWidth != that.whiskerCrossBarWidth)
        {
            return false;
        }
        if(!PaintUtilities.equal(this.whiskerCrossBarPaint, that.whiskerCrossBarPaint))
        {
            return false;
        }
        if(!Objects.equals(this.whiskerCrossBarStroke, that.whiskerCrossBarStroke))
        {
            return false;
        }

        if (!PaintUtilities.equal(this.whiskerPaint, that.whiskerPaint)) {
            return false;
        }
        if (!Objects.equals(this.whiskerStroke, that.whiskerStroke)) {
            return false;
        }

        /////////////////////////// MEDIAN ////////////////////////////////////////


        if(this.medianVisible != that.medianVisible)
        {
            return false;
        }
        if (!PaintUtilities.equal(this.medianPaint, that.medianPaint)) {
            return false;
        }
        if (!Objects.equals(this.medianStroke, that.medianStroke)) {
            return false;
        }

        //////////////////////////////// MEAN ///////////////////////////////////////

        if(this.meanVisible != that.meanVisible)
        {
            return false;
        }
        if (!PaintUtilities.equal(this.meanFillPaint, that.meanFillPaint)) {
            return false;
        }
        if(this.meanOutlineVisible != that.meanOutlineVisible)
        {
            return false;
        }
        if(!Objects.equals(this.meanOutlineStroke, that.meanOutlineStroke))
        {
            return false;
        }
        if(!PaintUtilities.equal(this.meanOutlinePaint, that.meanOutlinePaint))
        {
            return false;
        }

        ////////////////////////// OUTLIERS ////////////////////////////////////////

        if(this.outliersVisible != that.outliersVisible)
        {
            return false;
        }

        if(this.outlierMarkerIndex != that.outlierMarkerIndex)
        {
            return false;
        }
        if(this.outlierSize != that.outlierSize)
        {
            return false;
        }
        if(this.outlierFilled != that.outlierFilled)
        {
            return false;
        }
        if (!PaintUtilities.equal(this.outlierFillPaint, that.outlierFillPaint)) {
            return false;
        }
        if(this.outlierOutlineVisible != that.outlierOutlineVisible)
        {
            return false;
        }
        if (!Objects.equals(this.outlierStroke, that.outlierStroke)) {
            return false;
        }
        if (!PaintUtilities.equal(this.outlierStrokePaint, that.outlierStrokePaint)) {
            return false;
        }      

        return true;

    }

    @Override
    public Range findRangeBounds(XYDataset dataset)
    {
        return findRangeBounds(dataset, true);
    }

    @Override
    public StyleTag getStyleKey()
    {
        return style;
    }

    ///////////////////////// BOX /////////////////////////////////////////


    public boolean getBoxFilled() 
    {
        return boxFilled;
    }

    public void setBoxFilled(boolean boxFilled) 
    {
        if(this.boxFilled != boxFilled)
        {
            this.boxFilled = boxFilled;
            fireChangeEvent();
        }
    }

    public Paint getBoxPaint() 
    {
        return this.boxFillPaint;
    }

    public void setBoxPaint(Paint boxPaint) 
    {
        if(!Objects.equals(this.boxFillPaint, boxPaint))
        {
            this.boxFillPaint = boxPaint;
            fireChangeEvent();
        }
    }

    public double getBoxWidth() 
    {
        return this.boxWidth;
    }

    public void setBoxWidth(double boxWidth)
    {
        if (this.boxWidth != boxWidth) 
        {
            this.boxWidth = boxWidth;
            fireChangeEvent();
        }
    }

    public boolean isBoxOutlineVisible()
    {
        return boxOutlineVisible;
    }

    public void setBoxOutlineVisible(boolean boxOutlineVisible)
    {
        if(!Objects.equals(this.boxOutlineVisible, boxOutlineVisible))
        {
            this.boxOutlineVisible = boxOutlineVisible;
            fireChangeEvent();
        }
    }


    ///////////////////////////// OUTLIERS /////////////////////////////////////////

    public boolean isOutliersVisible()
    {
        return outliersVisible;
    }

    public void setOutliersVisible(boolean outliersVisible)
    {
        if(this.outliersVisible != outliersVisible)
        {
            this.outliersVisible = outliersVisible;
            fireChangeEvent();
        }
    }

    public int getOutlierMarkerIndex()
    {
        return outlierMarkerIndex;
    }

    public void setOutlierMarkerIndex(int outlierMarkerIndex)
    {
        if(this.outlierMarkerIndex != outlierMarkerIndex)
        {
            this.outlierMarkerIndex = outlierMarkerIndex;
            fireChangeEvent();
        }
    }

    public float getOutlierSize()
    {
        return outlierSize;
    }

    public void setOutlierSize(float outlierSize)
    {
        if(this.outlierSize != outlierSize)
        {
            this.outlierSize = outlierSize;
            fireChangeEvent();
        }
    }

    public boolean isOutlierFilled()
    {
        return outlierFilled;
    }

    public void setOutlierFilled(boolean outlierFilled)
    {
        if(this.outlierFilled != outlierFilled)
        {
            this.outlierFilled = outlierFilled;
            fireChangeEvent();
        }
    }

    public Paint getOutlierFillPaint()
    {
        return outlierFillPaint;
    }

    public void setOutlierFillPaint(Paint outlierFillPaint)
    {
        if(!Objects.equals(this.outlierFillPaint, outlierFillPaint))
        {
            this.outlierFillPaint = outlierFillPaint;
            fireChangeEvent();
        }
    }

    public boolean isOutlierOutlineVisible()
    {
        return outlierOutlineVisible;
    }

    public void setOutlierOutlineVisible(boolean outlierOutlineVisible) 
    {
        if(this.outlierOutlineVisible != outlierOutlineVisible)
        {
            this.outlierOutlineVisible = outlierOutlineVisible;
            fireChangeEvent();
        }
    }

    public Stroke getOutlierStroke()
    {
        return outlierStroke;
    }

    public void setOutlierStroke(Stroke outlierStroke)
    {
        if(!Objects.equals(this.outlierStroke, outlierStroke))
        {
            this.outlierStroke = outlierStroke;
            fireChangeEvent();
        }
    }

    public Paint getOutlierStrokePaint()
    {
        return outlierStrokePaint;
    }

    public void setOutlierStrokePaint(Paint outlierStrokePaint)
    {
        if(!Objects.equals(this.outlierStrokePaint, outlierStrokePaint))
        {
            this.outlierStrokePaint = outlierStrokePaint;
            fireChangeEvent();
        }
    }

    /////////////////////////// MEAN /////////////////////////////////

    public boolean isMeanVisible()
    {
        return meanVisible;
    }

    public void setMeanVisible(boolean meanVisible)
    {
        if(this.meanVisible != meanVisible)
        {
            this.meanVisible = meanVisible;
            fireChangeEvent();
        }
    }

    public boolean isMeanFilled()
    {
        return meanFilled;
    }

    public void setMeanFilled(boolean meanFilled)
    {
        if(this.meanFilled != meanFilled)
        {
            this.meanFilled = meanFilled;
            fireChangeEvent();
        }
    }

    public Paint getMeanFillPaint()
    {
        return meanFillPaint;
    }


    public void setMeanFillPaint(Paint meanFillPaint)
    {
        if(!Objects.equals(this.meanFillPaint, meanFillPaint))
        {
            this.meanFillPaint = meanFillPaint;
            fireChangeEvent();
        }
    }

    public boolean isMeanOutlineVisible()
    {
        return meanOutlineVisible;
    }

    public void setMeanOutlineVisible(boolean meanOutlineVisible)
    {
        if(this.meanOutlineVisible != meanOutlineVisible)
        {
            this.meanOutlineVisible = meanOutlineVisible;
            fireChangeEvent();
        }
    }

    public Paint getMeanOutlinePaint()
    {
        return meanOutlinePaint;
    }

    public void setMeanOutlinePaint(Paint meanOutlinePaint)
    {
        if(!Objects.equals(this.meanOutlinePaint, meanOutlinePaint))
        {
            this.meanOutlinePaint = meanOutlinePaint;
            fireChangeEvent();
        }
    }

    public Stroke getMeanOutlineStroke()
    {
        return meanOutlineStroke;
    }

    public void setMeanOutlineStroke(Stroke meanOutlineStroke)
    {
        if(!Objects.equals(this.meanOutlineStroke, meanOutlineStroke))
        {
            this.meanOutlineStroke = meanOutlineStroke;
            fireChangeEvent();
        }
    }

    /////////////////////// MEDIAN //////////////////////////////////////

    public boolean isMedianVisible()
    {
        return medianVisible;
    }

    public void setMedianVisible(boolean medianVisible)
    {
        if(this.medianVisible != medianVisible)
        {
            this.medianVisible = medianVisible;
            fireChangeEvent();
        }
    }

    public Paint getMedianPaint()
    {
        return medianPaint;
    }

    public void setMedianPaint(Paint medianPaint)
    {
        if(!Objects.equals(this.medianPaint, medianPaint))
        {
            this.medianPaint = medianPaint;
            fireChangeEvent();
        }
    }

    public Stroke getMedianStroke()
    {
        return medianStroke;
    }

    public void setMedianStroke(Stroke medianStroke)
    {
        if(!Objects.equals(this.medianStroke, medianStroke))
        {
            this.medianStroke = medianStroke;
            fireChangeEvent();
        }
    }


    ////////////////////////////////// WHISKERS ////////////////////////////////////

    public Paint getWhiskerPaint()
    {
        return whiskerPaint;
    }

    public void setWhiskerPaint(Paint whiskerPaint)
    {
        if(!Objects.equals(this.whiskerPaint, whiskerPaint))
        {
            this.whiskerPaint = whiskerPaint;
            fireChangeEvent();
        }
    }

    public Stroke getWhiskerStroke()
    {
        return whiskerStroke;
    }    

    public void setWhiskerStroke(Stroke whiskerStroke)
    {
        if(!Objects.equals(this.whiskerStroke, whiskerStroke))
        {
            this.whiskerStroke = whiskerStroke;
            fireChangeEvent();
        }
    }

    public boolean isWhiskerCrossBarVisible()
    {
        return whiskerCrossBarVisible;
    }

    public void setWhiskerCrossBarVisible(boolean whiskerCrossBarVisible)
    {
        if(this.whiskerCrossBarVisible != whiskerCrossBarVisible)
        {
            this.whiskerCrossBarVisible = whiskerCrossBarVisible;
            fireChangeEvent();
        }
    }

    public double getWhiskerCrossBarWidth()
    {
        return whiskerCrossBarWidth;
    }

    public void setWhiskerCrossBarWidth(double whiskerCrossBarWidth)
    {
        if(this.whiskerCrossBarWidth != whiskerCrossBarWidth)
        {
            this.whiskerCrossBarWidth = whiskerCrossBarWidth;
            fireChangeEvent();
        }
    }

    public Paint getWhiskerCrossBarPaint()
    {
        return whiskerCrossBarPaint;
    }

    public void setWhiskerCrossBarPaint(Paint whiskerCrossBarPaint)
    {
        if(!Objects.equals(this.whiskerCrossBarPaint, whiskerCrossBarPaint))
        {
            this.whiskerCrossBarPaint = whiskerCrossBarPaint;
            fireChangeEvent();
        }
    }

    public Stroke getWhiskerCrossBarStroke()
    {
        return whiskerCrossBarStroke;
    }

    public void setWhiskerCrossBarStroke(Stroke whiskerCrossBarStroke)
    {
        if(!Objects.equals(this.whiskerCrossBarStroke, whiskerCrossBarStroke))
        {
            this.whiskerCrossBarStroke = whiskerCrossBarStroke;
            fireChangeEvent();
        }
    }


    @Override
    public Preferences getPreferences() 
    {
        return pref;
    }


    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {

        stream.defaultReadObject();
        this.boxFillPaint = SerialUtilities.readPaint(stream);
        this.meanFillPaint = SerialUtilities.readPaint(stream);
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    private void setPreferredStyle(PreferredBoxAndWhiskerRendererStyle preferredStyle)
    {
        this.boxFilled = preferredStyle.getFillBox();
        this.boxWidth = preferredStyle.getBoxWidth();
        this.boxFillPaint = preferredStyle.getBoxFillPaint();
        this.boxOutlineVisible = preferredStyle.isBoxOutlineVisible();

        Paint boxOutlinePaint = preferredStyle.getBoxOutlinePaint();
        Stroke boxOutlineStroke = preferredStyle.getBoxOutlineStroke();
        setBaseOutlinePaint(boxOutlinePaint);
        setBaseOutlineStroke(boxOutlineStroke);

        //mean
        this.meanVisible = preferredStyle.isMeanVisible();
        this.meanFilled = preferredStyle.isMeanFilled();
        this.meanFillPaint = preferredStyle.getMeanPaint();
        this.meanOutlineVisible = preferredStyle.isMeanOutlineVisible();
        this.meanOutlinePaint = preferredStyle.getMeanOutlinePaint();
        this.meanOutlineStroke = preferredStyle.getMeanOutlineStroke();

        //median
        this.medianVisible = preferredStyle.isMedianVisible();
        this.medianPaint = preferredStyle.getMedianPaint();
        this.medianStroke = preferredStyle.getMedianStroke();

        //whiskers
        this.whiskerPaint = preferredStyle.getWhiskerPaint();
        this.whiskerStroke = preferredStyle.getWhiskerStroke();

        this.whiskerCrossBarVisible = preferredStyle.isWhiskerCrossBarVisible();
        this.whiskerCrossBarWidth = preferredStyle.getWhiskerCrossBarWidth();
        this.whiskerCrossBarPaint = preferredStyle.getWhiskerCrossBarPaint();
        this.whiskerCrossBarStroke = preferredStyle.getWhiskerCrossBarStroke();


        //outliers
        this.outliersVisible = preferredStyle.isOutliersVisible();
        this.outlierMarkerIndex = preferredStyle.getOutlierMarkerIndex();
        this.outlierSize = preferredStyle.getOutlierSize();
        this.outlierFilled = preferredStyle.isOutlierFilled();
        this.outlierFillPaint = preferredStyle.getOutlierFillPaint();
        this.outlierOutlineVisible = preferredStyle.isOutlierOutlineVisible();
        this.outlierStroke = preferredStyle.getOutlierStroke();
        this.outlierStrokePaint = preferredStyle.getOutlierStrokePaint();
    }


    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint(this.boxFillPaint, stream);
        SerialUtilities.writePaint(this.meanFillPaint, stream);
    }
}
