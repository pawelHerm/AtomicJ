package atomicJ.gui;

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2011, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------------------
 * AbstractXYItemRenderer.java
 * ---------------------------
 * (C) Copyright 2002-2011, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Richard Atkinson;
 *                   Focus Computer Services Limited;
 *                   Tim Bardzil;
 *                   Sergei Ivanov;
 *                   Peter Kolb (patch 2809117);
 *                   Martin Krauskopf;
 *
 * Changes:
 * --------
 * 15-Mar-2002 : Version 1 (DG);
 * 09-Apr-2002 : Added a getToolTipGenerator() method reflecting the change in
 *               the XYItemRenderer interface (DG);
 * 05-Aug-2002 : Added a urlGenerator member variable to support HTML image
 *               maps (RA);
 * 20-Aug-2002 : Added property change events for the tooltip and URL
 *               generators (DG);
 * 22-Aug-2002 : Moved property change support into AbstractRenderer class (DG);
 * 23-Sep-2002 : Fixed errors reported by Checkstyle tool (DG);
 * 18-Nov-2002 : Added methods for drawing grid lines (DG);
 * 17-Jan-2003 : Moved plot classes into a separate package (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 01-May-2003 : Modified initialise() return type and drawItem() method
 *               signature (DG);
 * 15-May-2003 : Modified to take into account the plot orientation (DG);
 * 21-May-2003 : Added labels to markers (DG);
 * 05-Jun-2003 : Added domain and range grid bands (sponsored by Focus Computer
 *               Services Ltd) (DG);
 * 27-Jul-2003 : Added getRangeType() to support stacked XY area charts (RA);
 * 31-Jul-2003 : Deprecated all but the default constructor (DG);
 * 13-Aug-2003 : Implemented Cloneable (DG);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 29-Oct-2003 : Added workaround for font alignment in PDF output (DG);
 * 05-Nov-2003 : Fixed marker rendering bug (833623) (DG);
 * 11-Feb-2004 : Updated labelling for markers (DG);
 * 25-Feb-2004 : Added updateCrosshairValues() method.  Moved deprecated code
 *               to bottom of source file (DG);
 * 16-Apr-2004 : Added support for IntervalMarker in drawRangeMarker() method
 *               - thanks to Tim Bardzil (DG);
 * 05-May-2004 : Fixed bug (948310) where interval markers extend beyond axis
 *               range (DG);
 * 03-Jun-2004 : Fixed more bugs in drawing interval markers (DG);
 * 26-Aug-2004 : Added the addEntity() method (DG);
 * 29-Sep-2004 : Added annotation support (with layers) (DG);
 * 30-Sep-2004 : Moved drawRotatedString() from RefineryUtilities -->
 *               TextUtilities (DG);
 * 06-Oct-2004 : Added findDomainBounds() method and renamed
 *               getRangeExtent() --> findRangeBounds() (DG);
 * 07-Jan-2005 : Removed deprecated code (DG);
 * 27-Jan-2005 : Modified getLegendItem() to omit hidden series (DG);
 * 24-Feb-2005 : Added getLegendItems() method (DG);
 * 08-Mar-2005 : Fixed positioning of marker labels (DG);
 * 20-Apr-2005 : Renamed XYLabelGenerator --> XYItemLabelGenerator and
 *               added generators for legend labels, tooltips and URLs (DG);
 * 01-Jun-2005 : Handle one dimension of the marker label adjustment
 *               automatically (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 20-Jul-2006 : Set dataset and series indices in LegendItem (DG);
 * 24-Oct-2006 : Respect alpha setting in markers (see patch 1567843 by Sergei
 *               Ivanov) (DG);
 * 24-Oct-2006 : Added code to draw outlines for interval markers (DG);
 * 24-Nov-2006 : Fixed cloning for legend item generators (DG);
 * 06-Feb-2007 : Added new updateCrosshairValues() method that takes into
 *               account multiple axis plots (see bug 1086307) (DG);
 * 20-Feb-2007 : Fixed equals() method implementation (DG);
 * 01-Mar-2007 : Fixed interval marker drawing (patch 1670686 thanks to
 *               Sergei Ivanov) (DG);
 * 22-Mar-2007 : Modified the tool tip generator look up (DG);
 * 23-Mar-2007 : Added drawDomainLine() method (DG);
 * 20-Apr-2007 : Updated getLegendItem() for renderer change, and deprecated
 *               itemLabelGenerator and toolTipGenerator override fields (DG);
 * 18-May-2007 : Set dataset and seriesKey for LegendItem (DG);
 * 12-Nov-2007 : Fixed domain and range band drawing methods (DG);
 * 07-Apr-2008 : Minor API doc update (DG);
 * 14-May-2008 : Updated addEntity() method to take plot orientation into
 *               account when the incoming area is null (DG);
 * 02-Jun-2008 : Added isPointInRect() method (DG);
 * 17-Jun-2008 : Apply legend shape, font and paint attributes (DG);
 * 09-Mar-2009 : Added getAnnotations() method (DG);
 * 27-Mar-2009 : Added new findDomainBounds() and findRangeBounds() methods to
 *               take account of hidden series (DG);
 * 01-Apr-2009 : Moved defaultEntityRadius up to superclass (DG);
 * 28-Apr-2009 : Updated getLegendItem() method to observe new
 *               'treatLegendShapeAsLine' flag (DG);
 * 24-Jun-2009 : Added support for annotation events - see patch 2809117
 *               by PK (DG);
 * 01-Sep-2009 : Bug 2840132 - set renderer index when drawing
 *               annotations (DG);
 * 06-Oct-2011 : Add utility methods to work with 1.4 API in GeneralPath (MK)
 * 
 */

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.AnnotationChangeEvent;
import org.jfree.chart.event.AnnotationChangeListener;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.text.TextUtilities;
import org.jfree.ui.GradientPaintTransformer;
import org.jfree.ui.Layer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ObjectUtilities;

import atomicJ.utilities.Validation;

public abstract class AbstractXYItemRendererLightweight <E extends AbstractXYRendererData> extends AbstractRendererLightweight <E>
implements XYItemRenderer, AnnotationChangeListener, Cloneable {

    private XYPlot plot;

    private List<XYAnnotation> backgroundAnnotations = new ArrayList<>();
    private List<XYAnnotation> foregroundAnnotations = new ArrayList<>();

    public AbstractXYItemRendererLightweight(E rendererData) 
    {
        super(rendererData);
    }

    @Override
    public int getPassCount() {
        return 1;
    }

    @Override
    public XYPlot getPlot() {
        return this.plot;
    }

    @Override
    public void setPlot(XYPlot plot) {
        this.plot = plot;
    }

    @Override
    protected abstract AbstractXYRendererDataMutable<?> getDataForModification();

    @Override
    public XYItemRendererState initialise(Graphics2D g2,
            Rectangle2D dataArea,
            XYPlot plot,
            XYDataset data,
            PlotRenderingInfo info) {

        XYItemRendererState state = new XYItemRendererState(info);
        return state;

    }

    @Override
    public XYItemLabelGenerator getBaseItemLabelGenerator() {
        return getData().getBaseItemLabelGenerator();
    }

    public void setBaseItemLabelGenerator(XYItemLabelGenerator generator)
    {
        getDataForModification().setBaseItemLabelGenerator(generator);
    }


    @Override
    public XYToolTipGenerator getBaseToolTipGenerator() {
        return getData().getBaseToolTipGenerator();
    }

    public void setBaseToolTipGenerator(XYToolTipGenerator generator)
    {
        getDataForModification().setBaseToolTipGenerator(generator);
    }

    @Override
    public XYURLGenerator getURLGenerator() {
        return getData().getURLGenerator();
    }

    public void setURLGenerator(XYURLGenerator urlGenerator)
    {
        getDataForModification().setURLGenerator(urlGenerator);
    }

    @Override
    public void addAnnotation(XYAnnotation annotation) {
        addAnnotation(annotation, Layer.FOREGROUND);
    }

    @Override
    public void addAnnotation(XYAnnotation annotation, Layer layer) {

        Validation.requireNonNullParameterName(annotation, "annotation");

        if (Layer.FOREGROUND.equals(layer)) {
            this.foregroundAnnotations.add(annotation);
            annotation.addChangeListener(this);
            fireChangeEvent();
        }
        else if (Layer.BACKGROUND.equals(layer)) {
            this.backgroundAnnotations.add(annotation);
            annotation.addChangeListener(this);
            fireChangeEvent();
        }
        else {
            // should never get here
            throw new RuntimeException("Unknown layer.");
        }
    }

    @Override
    public boolean removeAnnotation(XYAnnotation annotation) {
        boolean removed = this.foregroundAnnotations.remove(annotation);
        removed = removed & this.backgroundAnnotations.remove(annotation);
        annotation.removeChangeListener(this);
        fireChangeEvent();
        return removed;
    }

    @Override
    public void removeAnnotations()
    {
        for(int i = 0; i < this.foregroundAnnotations.size(); i++)
        {
            XYAnnotation annotation  = this.foregroundAnnotations.get(i);
            annotation.removeChangeListener(this);
        }
        for(int i = 0; i < this.backgroundAnnotations.size(); i++)
        {
            XYAnnotation annotation = this.backgroundAnnotations.get(i);
            annotation.removeChangeListener(this);
        }
        this.foregroundAnnotations.clear();
        this.backgroundAnnotations.clear();
        fireChangeEvent();
    }

    @Override
    public void annotationChanged(AnnotationChangeEvent event)
    {
        fireChangeEvent();
    }

    public Collection<XYAnnotation> getAnnotations() {
        List<XYAnnotation> result = new ArrayList<>(this.foregroundAnnotations);
        result.addAll(this.backgroundAnnotations);
        return result;
    }

    @Override
    public XYSeriesLabelGenerator getLegendItemLabelGenerator() {
        return getData().getLegendItemLabelGenerator();
    }

    public void setLegendItemLabelGenerator(XYSeriesLabelGenerator generator)
    {
        getDataForModification().setLegendItemLabelGenerator(generator);
    }


    public XYSeriesLabelGenerator getLegendItemToolTipGenerator() {
        return getData().getLegendItemToolTipGenerator();
    }

    public XYSeriesLabelGenerator getLegendItemURLGenerator() {
        return getData().getLegendItemURLGenerator();
    }


    @Override
    public Range findDomainBounds(XYDataset dataset) {
        return findDomainBounds(dataset, false);
    }

    protected Range findDomainBounds(XYDataset dataset,
            boolean includeInterval) {
        if (dataset == null) {
            return null;
        }
        if (getDataBoundsIncludesVisibleSeriesOnly()) {
            List<Comparable<?>> visibleSeriesKeys = new ArrayList<>();
            if (getBaseSeriesVisible()) {
                visibleSeriesKeys.add(dataset.getSeriesKey(0));
            }
            return DatasetUtilities.findDomainBounds(dataset,
                    visibleSeriesKeys, includeInterval);
        }
        return DatasetUtilities.findDomainBounds(dataset, includeInterval);
    }

    @Override
    public Range findRangeBounds(XYDataset dataset) {
        return findRangeBounds(dataset, false);
    }

    protected Range findRangeBounds(XYDataset dataset,
            boolean includeInterval) {
        if (dataset == null) {
            return null;
        }
        if (getDataBoundsIncludesVisibleSeriesOnly()) {
            List<Comparable<?>> visibleSeriesKeys = new ArrayList<>();
            if (getBaseSeriesVisible()) {
                visibleSeriesKeys.add(dataset.getSeriesKey(0));
            }
            // the bounds should be calculated using just the items within
            // the current range of the x-axis...if there is one
            Range xRange = null;
            XYPlot p = getPlot();
            if (p != null) {
                ValueAxis xAxis = null;
                int index = p.getIndexOf(this);
                if (index >= 0) {
                    xAxis = this.plot.getDomainAxisForDataset(index);
                }
                if (xAxis != null) {
                    xRange = xAxis.getRange();
                }
            }
            if (xRange == null) {
                xRange = new Range(Double.NEGATIVE_INFINITY,
                        Double.POSITIVE_INFINITY);
            }
            return DatasetUtilities.findRangeBounds(dataset,
                    visibleSeriesKeys, xRange, includeInterval);
        }
        return DatasetUtilities.findRangeBounds(dataset, includeInterval);
    }

    @Override
    public LegendItemCollection getLegendItems() {
        if (this.plot == null) {
            return new LegendItemCollection();
        }
        LegendItemCollection result = new LegendItemCollection();
        int index = this.plot.getIndexOf(this);
        XYDataset dataset = this.plot.getDataset(index);
        if (dataset != null) {
            if (getBaseSeriesVisibleInLegend()) {
                LegendItem item = getLegendItem(index);
                if (item != null) {
                    result.add(item);
                }
            }
        }

        return result;
    }

    /**
     * Returns a default legend item for the specified series.  Subclasses
     * should override this method to generate customised items.
     *
     * @param datasetIndex  the dataset index (zero-based).
     * @param series  the series index (zero-based).
     *
     * @return A legend item for the series.
     */
    @Override
    public LegendItem getLegendItem(int datasetIndex) {
        XYPlot xyplot = getPlot();
        if (xyplot == null) {
            return null;
        }
        XYDataset dataset = xyplot.getDataset(datasetIndex);
        if (dataset == null) {
            return null;
        }
        String label = getLegendItemLabelGenerator().generateLabel(dataset);
        String toolTipText = null;
        if (getLegendItemToolTipGenerator() != null) {
            toolTipText = getLegendItemToolTipGenerator().generateLabel(dataset);
        }
        String urlText = null;
        if (getLegendItemURLGenerator() != null) {
            urlText = getLegendItemURLGenerator().generateLabel(dataset);
        }
        Shape shape = getBaseLegendShape();
        Paint paint = getBasePaint();
        LegendItem item = new LegendItem(label, paint);
        item.setToolTipText(toolTipText);
        item.setURLText(urlText);
        item.setLabelFont(getBaseLegendTextFont());
        Paint labelPaint = getBaseLegendTextPaint();
        if (labelPaint != null) {
            item.setLabelPaint(labelPaint);
        }
        item.setSeriesKey(dataset.getSeriesKey(0));
        item.setDataset(dataset);
        item.setDatasetIndex(datasetIndex);

        if (getTreatLegendShapeAsLine()) {
            item.setLineVisible(true);
            item.setLine(shape);
            item.setLinePaint(paint);
            item.setShapeVisible(false);
        }
        else {
            Paint outlinePaint = getBaseOutlinePaint();
            Stroke outlineStroke = getBaseOutlineStroke();
            item.setOutlinePaint(outlinePaint);
            item.setOutlineStroke(outlineStroke);
        }
        return item;
    }

    /**
     * Fills a band between two values on the axis.  This can be used to color
     * bands between the grid lines.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the domain axis.
     * @param dataArea  the data area.
     * @param start  the start value.
     * @param end  the end value.
     */
    @Override
    public void fillDomainGridBand(Graphics2D g2, XYPlot plot, ValueAxis axis,
            Rectangle2D dataArea, double start, double end) {

        double x1 = axis.valueToJava2D(start, dataArea,
                plot.getDomainAxisEdge());
        double x2 = axis.valueToJava2D(end, dataArea,
                plot.getDomainAxisEdge());
        Rectangle2D band;
        if (plot.getOrientation() == PlotOrientation.VERTICAL) {
            band = new Rectangle2D.Double(Math.min(x1, x2), dataArea.getMinY(),
                    Math.abs(x2 - x1), dataArea.getWidth());
        }
        else {
            band = new Rectangle2D.Double(dataArea.getMinX(), Math.min(x1, x2),
                    dataArea.getWidth(), Math.abs(x2 - x1));
        }
        Paint paint = plot.getDomainTickBandPaint();

        if (paint != null) {
            g2.setPaint(paint);
            g2.fill(band);
        }

    }

    /**
     * Fills a band between two values on the range axis.  This can be used to
     * color bands between the grid lines.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the range axis.
     * @param dataArea  the data area.
     * @param start  the start value.
     * @param end  the end value.
     */
    @Override
    public void fillRangeGridBand(Graphics2D g2, XYPlot plot, ValueAxis axis,
            Rectangle2D dataArea, double start, double end) {

        double y1 = axis.valueToJava2D(start, dataArea,
                plot.getRangeAxisEdge());
        double y2 = axis.valueToJava2D(end, dataArea, plot.getRangeAxisEdge());
        Rectangle2D band;
        if (plot.getOrientation() == PlotOrientation.VERTICAL) {
            band = new Rectangle2D.Double(dataArea.getMinX(), Math.min(y1, y2),
                    dataArea.getWidth(), Math.abs(y2 - y1));
        }
        else {
            band = new Rectangle2D.Double(Math.min(y1, y2), dataArea.getMinY(),
                    Math.abs(y2 - y1), dataArea.getHeight());
        }
        Paint paint = plot.getRangeTickBandPaint();

        if (paint != null) {
            g2.setPaint(paint);
            g2.fill(band);
        }

    }

    /**
     * Draws a grid line against the range axis.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the value axis.
     * @param dataArea  the area for plotting data (not yet adjusted for any
     *                  3D effect).
     * @param value  the value at which the grid line should be drawn.
     */
    @Override
    public void drawDomainGridLine(Graphics2D g2,
            XYPlot plot,
            ValueAxis axis,
            Rectangle2D dataArea,
            double value) {

        Range range = axis.getRange();
        if (!range.contains(value)) {
            return;
        }

        PlotOrientation orientation = plot.getOrientation();
        double v = axis.valueToJava2D(value, dataArea,
                plot.getDomainAxisEdge());
        Line2D line = null;
        if (orientation == PlotOrientation.HORIZONTAL) {
            line = new Line2D.Double(dataArea.getMinX(), v,
                    dataArea.getMaxX(), v);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            line = new Line2D.Double(v, dataArea.getMinY(), v,
                    dataArea.getMaxY());
        }

        Paint paint = plot.getDomainGridlinePaint();
        Stroke stroke = plot.getDomainGridlineStroke();
        g2.setPaint(paint != null ? paint : Plot.DEFAULT_OUTLINE_PAINT);
        g2.setStroke(stroke != null ? stroke : Plot.DEFAULT_OUTLINE_STROKE);
        g2.draw(line);

    }

    /**
     * Draws a line perpendicular to the domain axis.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the value axis.
     * @param dataArea  the area for plotting data (not yet adjusted for any 3D
     *                  effect).
     * @param value  the value at which the grid line should be drawn.
     * @param paint  the paint (<code>null</code> not permitted).
     * @param stroke  the stroke (<code>null</code> not permitted).
     *
     * @since 1.0.5
     */
    @Override
    public void drawDomainLine(Graphics2D g2, XYPlot plot, ValueAxis axis,
            Rectangle2D dataArea, double value, Paint paint, Stroke stroke) {

        Range range = axis.getRange();
        if (!range.contains(value)) {
            return;
        }

        PlotOrientation orientation = plot.getOrientation();
        Line2D line = null;
        double v = axis.valueToJava2D(value, dataArea,
                plot.getDomainAxisEdge());
        if (orientation == PlotOrientation.HORIZONTAL) {
            line = new Line2D.Double(dataArea.getMinX(), v, dataArea.getMaxX(),
                    v);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            line = new Line2D.Double(v, dataArea.getMinY(), v,
                    dataArea.getMaxY());
        }

        g2.setPaint(paint);
        g2.setStroke(stroke);
        g2.draw(line);

    }

    /**
     * Draws a line perpendicular to the range axis.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param axis  the value axis.
     * @param dataArea  the area for plotting data (not yet adjusted for any 3D
     *                  effect).
     * @param value  the value at which the grid line should be drawn.
     * @param paint  the paint.
     * @param stroke  the stroke.
     */
    @Override
    public void drawRangeLine(Graphics2D g2,
            XYPlot plot,
            ValueAxis axis,
            Rectangle2D dataArea,
            double value,
            Paint paint,
            Stroke stroke) {

        Range range = axis.getRange();
        if (!range.contains(value)) {
            return;
        }

        PlotOrientation orientation = plot.getOrientation();
        Line2D line = null;
        double v = axis.valueToJava2D(value, dataArea, plot.getRangeAxisEdge());
        if (orientation == PlotOrientation.HORIZONTAL) {
            line = new Line2D.Double(v, dataArea.getMinY(), v,
                    dataArea.getMaxY());
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            line = new Line2D.Double(dataArea.getMinX(), v,
                    dataArea.getMaxX(), v);
        }

        g2.setPaint(paint);
        g2.setStroke(stroke);
        g2.draw(line);

    }

    /**
     * Draws a vertical line on the chart to represent a 'range marker'.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param domainAxis  the domain axis.
     * @param marker  the marker line.
     * @param dataArea  the axis data area.
     */
    @Override
    public void drawDomainMarker(Graphics2D g2,
            XYPlot plot,
            ValueAxis domainAxis,
            Marker marker,
            Rectangle2D dataArea) 
    {

        //added by P.Hermanowicz on 09.05.2021
        if(marker instanceof CustomizableValueMarker)
        {
            if(!((CustomizableValueMarker) marker).isVisible())
            {
                return;
            }
        }

        if (marker instanceof ValueMarker)
        {
            ValueMarker vm = (ValueMarker) marker;
            double value = vm.getValue();
            Range range = domainAxis.getRange();
            if (!range.contains(value)) {
                return;
            }

            double v = domainAxis.valueToJava2D(value, dataArea,
                    plot.getDomainAxisEdge());

            PlotOrientation orientation = plot.getOrientation();
            Line2D line = null;
            if (orientation == PlotOrientation.HORIZONTAL) {
                line = new Line2D.Double(dataArea.getMinX(), v,
                        dataArea.getMaxX(), v);
            }
            else if (orientation == PlotOrientation.VERTICAL) {
                line = new Line2D.Double(v, dataArea.getMinY(), v,
                        dataArea.getMaxY());
            }

            final Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, marker.getAlpha()));
            g2.setPaint(marker.getPaint());
            g2.setStroke(marker.getStroke());
            g2.draw(line);

            String label = marker.getLabel();
            RectangleAnchor anchor = marker.getLabelAnchor();
            if (label != null) {
                Font labelFont = marker.getLabelFont();
                g2.setFont(labelFont);
                g2.setPaint(marker.getLabelPaint());
                Point2D coordinates = calculateDomainMarkerTextAnchorPoint(
                        g2, orientation, dataArea, line.getBounds2D(),
                        marker.getLabelOffset(),
                        LengthAdjustmentType.EXPAND, anchor);
                TextUtilities.drawAlignedString(label, g2,
                        (float) coordinates.getX(), (float) coordinates.getY(),
                        marker.getLabelTextAnchor());
            }
            g2.setComposite(originalComposite);
        }
        else if (marker instanceof IntervalMarker) {
            IntervalMarker im = (IntervalMarker) marker;
            double start = im.getStartValue();
            double end = im.getEndValue();
            Range range = domainAxis.getRange();
            if (!(range.intersects(start, end))) {
                return;
            }

            double start2d = domainAxis.valueToJava2D(start, dataArea,
                    plot.getDomainAxisEdge());
            double end2d = domainAxis.valueToJava2D(end, dataArea,
                    plot.getDomainAxisEdge());
            double low = Math.min(start2d, end2d);
            double high = Math.max(start2d, end2d);

            PlotOrientation orientation = plot.getOrientation();
            Rectangle2D rect = null;
            if (orientation == PlotOrientation.HORIZONTAL) {
                // clip top and bottom bounds to data area
                low = Math.max(low, dataArea.getMinY());
                high = Math.min(high, dataArea.getMaxY());
                rect = new Rectangle2D.Double(dataArea.getMinX(),
                        low, dataArea.getWidth(),
                        high - low);
            }
            else if (orientation == PlotOrientation.VERTICAL) {
                // clip left and right bounds to data area
                low = Math.max(low, dataArea.getMinX());
                high = Math.min(high, dataArea.getMaxX());
                rect = new Rectangle2D.Double(low,
                        dataArea.getMinY(), high - low,
                        dataArea.getHeight());
            }

            final Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, marker.getAlpha()));
            Paint p = marker.getPaint();
            if (p instanceof GradientPaint) {
                GradientPaint gp = (GradientPaint) p;
                GradientPaintTransformer t = im.getGradientPaintTransformer();
                if (t != null) {
                    gp = t.transform(gp, rect);
                }
                g2.setPaint(gp);
            }
            else {
                g2.setPaint(p);
            }
            g2.fill(rect);

            // now draw the outlines, if visible...
            if (im.getOutlinePaint() != null && im.getOutlineStroke() != null) {
                if (orientation == PlotOrientation.VERTICAL) {
                    Line2D line = new Line2D.Double();
                    double y0 = dataArea.getMinY();
                    double y1 = dataArea.getMaxY();
                    g2.setPaint(im.getOutlinePaint());
                    g2.setStroke(im.getOutlineStroke());
                    if (range.contains(start)) {
                        line.setLine(start2d, y0, start2d, y1);
                        g2.draw(line);
                    }
                    if (range.contains(end)) {
                        line.setLine(end2d, y0, end2d, y1);
                        g2.draw(line);
                    }
                }
                else { // PlotOrientation.HORIZONTAL
                    Line2D line = new Line2D.Double();
                    double x0 = dataArea.getMinX();
                    double x1 = dataArea.getMaxX();
                    g2.setPaint(im.getOutlinePaint());
                    g2.setStroke(im.getOutlineStroke());
                    if (range.contains(start)) {
                        line.setLine(x0, start2d, x1, start2d);
                        g2.draw(line);
                    }
                    if (range.contains(end)) {
                        line.setLine(x0, end2d, x1, end2d);
                        g2.draw(line);
                    }
                }
            }

            String label = marker.getLabel();
            RectangleAnchor anchor = marker.getLabelAnchor();
            if (label != null) {
                Font labelFont = marker.getLabelFont();
                g2.setFont(labelFont);
                g2.setPaint(marker.getLabelPaint());
                Point2D coordinates = calculateDomainMarkerTextAnchorPoint(
                        g2, orientation, dataArea, rect,
                        marker.getLabelOffset(), marker.getLabelOffsetType(),
                        anchor);
                TextUtilities.drawAlignedString(label, g2,
                        (float) coordinates.getX(), (float) coordinates.getY(),
                        marker.getLabelTextAnchor());
            }
            g2.setComposite(originalComposite);

        }

    }

    /**
     * Calculates the (x, y) coordinates for drawing a marker label.
     *
     * @param g2  the graphics device.
     * @param orientation  the plot orientation.
     * @param dataArea  the data area.
     * @param markerArea  the rectangle surrounding the marker area.
     * @param markerOffset  the marker label offset.
     * @param labelOffsetType  the label offset type.
     * @param anchor  the label anchor.
     *
     * @return The coordinates for drawing the marker label.
     */
    protected Point2D calculateDomainMarkerTextAnchorPoint(Graphics2D g2,
            PlotOrientation orientation,
            Rectangle2D dataArea,
            Rectangle2D markerArea,
            RectangleInsets markerOffset,
            LengthAdjustmentType labelOffsetType,
            RectangleAnchor anchor) {

        Rectangle2D anchorRect = null;
        if (orientation == PlotOrientation.HORIZONTAL) {
            anchorRect = markerOffset.createAdjustedRectangle(markerArea,
                    LengthAdjustmentType.CONTRACT, labelOffsetType);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            anchorRect = markerOffset.createAdjustedRectangle(markerArea,
                    labelOffsetType, LengthAdjustmentType.CONTRACT);
        }
        return RectangleAnchor.coordinates(anchorRect, anchor);

    }

    /**
     * Draws a horizontal line across the chart to represent a 'range marker'.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param rangeAxis  the range axis.
     * @param marker  the marker line.
     * @param dataArea  the axis data area.
     */
    @Override
    public void drawRangeMarker(Graphics2D g2,
            XYPlot plot,
            ValueAxis rangeAxis,
            Marker marker,
            Rectangle2D dataArea) 
    {

        //added by P.Hermanowicz on 09.05.2021
        if(marker instanceof CustomizableValueMarker)
        {
            if(!((CustomizableValueMarker) marker).isVisible())
            {
                return;
            }
        }
        if (marker instanceof ValueMarker) {
            ValueMarker vm = (ValueMarker) marker;
            double value = vm.getValue();
            Range range = rangeAxis.getRange();
            if (!range.contains(value)) {
                return;
            }

            double v = rangeAxis.valueToJava2D(value, dataArea,
                    plot.getRangeAxisEdge());
            PlotOrientation orientation = plot.getOrientation();
            Line2D line = null;
            if (orientation == PlotOrientation.HORIZONTAL) {
                line = new Line2D.Double(v, dataArea.getMinY(), v,
                        dataArea.getMaxY());
            }
            else if (orientation == PlotOrientation.VERTICAL) {
                line = new Line2D.Double(dataArea.getMinX(), v,
                        dataArea.getMaxX(), v);
            }
            else {
                throw new IllegalStateException("Unknown orientation.");
            }

            final Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, marker.getAlpha()));
            g2.setPaint(marker.getPaint());
            g2.setStroke(marker.getStroke());
            g2.draw(line);

            String label = marker.getLabel();
            RectangleAnchor anchor = marker.getLabelAnchor();
            if (label != null) {
                Font labelFont = marker.getLabelFont();
                g2.setFont(labelFont);
                g2.setPaint(marker.getLabelPaint());
                Point2D coordinates = calculateRangeMarkerTextAnchorPoint(
                        g2, orientation, dataArea, line.getBounds2D(),
                        marker.getLabelOffset(),
                        LengthAdjustmentType.EXPAND, anchor);
                TextUtilities.drawAlignedString(label, g2,
                        (float) coordinates.getX(), (float) coordinates.getY(),
                        marker.getLabelTextAnchor());
            }
            g2.setComposite(originalComposite);
        }
        else if (marker instanceof IntervalMarker) {
            IntervalMarker im = (IntervalMarker) marker;
            double start = im.getStartValue();
            double end = im.getEndValue();
            Range range = rangeAxis.getRange();
            if (!(range.intersects(start, end))) {
                return;
            }

            double start2d = rangeAxis.valueToJava2D(start, dataArea,
                    plot.getRangeAxisEdge());
            double end2d = rangeAxis.valueToJava2D(end, dataArea,
                    plot.getRangeAxisEdge());
            double low = Math.min(start2d, end2d);
            double high = Math.max(start2d, end2d);

            PlotOrientation orientation = plot.getOrientation();
            Rectangle2D rect = null;
            if (orientation == PlotOrientation.HORIZONTAL) {
                // clip left and right bounds to data area
                low = Math.max(low, dataArea.getMinX());
                high = Math.min(high, dataArea.getMaxX());
                rect = new Rectangle2D.Double(low,
                        dataArea.getMinY(), high - low,
                        dataArea.getHeight());
            }
            else if (orientation == PlotOrientation.VERTICAL) {
                // clip top and bottom bounds to data area
                low = Math.max(low, dataArea.getMinY());
                high = Math.min(high, dataArea.getMaxY());
                rect = new Rectangle2D.Double(dataArea.getMinX(),
                        low, dataArea.getWidth(),
                        high - low);
            }

            final Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, marker.getAlpha()));
            Paint p = marker.getPaint();
            if (p instanceof GradientPaint) {
                GradientPaint gp = (GradientPaint) p;
                GradientPaintTransformer t = im.getGradientPaintTransformer();
                if (t != null) {
                    gp = t.transform(gp, rect);
                }
                g2.setPaint(gp);
            }
            else {
                g2.setPaint(p);
            }
            g2.fill(rect);

            // now draw the outlines, if visible...
            if (im.getOutlinePaint() != null && im.getOutlineStroke() != null) {
                if (orientation == PlotOrientation.VERTICAL) {
                    Line2D line = new Line2D.Double();
                    double x0 = dataArea.getMinX();
                    double x1 = dataArea.getMaxX();
                    g2.setPaint(im.getOutlinePaint());
                    g2.setStroke(im.getOutlineStroke());
                    if (range.contains(start)) {
                        line.setLine(x0, start2d, x1, start2d);
                        g2.draw(line);
                    }
                    if (range.contains(end)) {
                        line.setLine(x0, end2d, x1, end2d);
                        g2.draw(line);
                    }
                }
                else { // PlotOrientation.HORIZONTAL
                    Line2D line = new Line2D.Double();
                    double y0 = dataArea.getMinY();
                    double y1 = dataArea.getMaxY();
                    g2.setPaint(im.getOutlinePaint());
                    g2.setStroke(im.getOutlineStroke());
                    if (range.contains(start)) {
                        line.setLine(start2d, y0, start2d, y1);
                        g2.draw(line);
                    }
                    if (range.contains(end)) {
                        line.setLine(end2d, y0, end2d, y1);
                        g2.draw(line);
                    }
                }
            }

            String label = marker.getLabel();
            RectangleAnchor anchor = marker.getLabelAnchor();
            if (label != null) {
                Font labelFont = marker.getLabelFont();
                g2.setFont(labelFont);
                g2.setPaint(marker.getLabelPaint());
                Point2D coordinates = calculateRangeMarkerTextAnchorPoint(
                        g2, orientation, dataArea, rect,
                        marker.getLabelOffset(), marker.getLabelOffsetType(),
                        anchor);
                TextUtilities.drawAlignedString(label, g2,
                        (float) coordinates.getX(), (float) coordinates.getY(),
                        marker.getLabelTextAnchor());
            }
            g2.setComposite(originalComposite);
        }
    }

    /**
     * Calculates the (x, y) coordinates for drawing a marker label.
     *
     * @param g2  the graphics device.
     * @param orientation  the plot orientation.
     * @param dataArea  the data area.
     * @param markerArea  the marker area.
     * @param markerOffset  the marker offset.
     * @param labelOffsetForRange  ??
     * @param anchor  the label anchor.
     *
     * @return The coordinates for drawing the marker label.
     */
    private Point2D calculateRangeMarkerTextAnchorPoint(Graphics2D g2,
            PlotOrientation orientation,
            Rectangle2D dataArea,
            Rectangle2D markerArea,
            RectangleInsets markerOffset,
            LengthAdjustmentType labelOffsetForRange,
            RectangleAnchor anchor) {

        Rectangle2D anchorRect = null;
        if (orientation == PlotOrientation.HORIZONTAL) {
            anchorRect = markerOffset.createAdjustedRectangle(markerArea,
                    labelOffsetForRange, LengthAdjustmentType.CONTRACT);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            anchorRect = markerOffset.createAdjustedRectangle(markerArea,
                    LengthAdjustmentType.CONTRACT, labelOffsetForRange);
        }
        return RectangleAnchor.coordinates(anchorRect, anchor);

    }

    /**
     * Returns a clone of the renderer.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if the renderer does not support
     *         cloning.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        AbstractXYItemRendererLightweight<?> clone = (AbstractXYItemRendererLightweight<?>) super.clone();
        // 'plot' : just retain reference, not a deep copy

        clone.foregroundAnnotations = (List<XYAnnotation>) ObjectUtilities.deepClone(
                this.foregroundAnnotations);
        clone.backgroundAnnotations = (List<XYAnnotation>) ObjectUtilities.deepClone(
                this.backgroundAnnotations);

        return clone;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AbstractXYItemRendererLightweight)) {
            return false;
        }
        AbstractXYItemRendererLightweight<?> that = (AbstractXYItemRendererLightweight<?>) obj;

        if (!this.foregroundAnnotations.equals(that.foregroundAnnotations)) {
            return false;
        }
        if (!this.backgroundAnnotations.equals(that.backgroundAnnotations)) {
            return false;
        }

        return super.equals(obj);
    }

    /**
     * Considers the current (x, y) coordinate and updates the crosshair point
     * if it meets the criteria (usually means the (x, y) coordinate is the
     * closest to the anchor point so far).
     *
     * @param crosshairState  the crosshair state (<code>null</code> permitted,
     *                        but the method does nothing in that case).
     * @param x  the x-value (in data space).
     * @param y  the y-value (in data space).
     * @param domainAxisIndex  the index of the domain axis for the point.
     * @param rangeAxisIndex  the index of the range axis for the point.
     * @param transX  the x-value translated to Java2D space.
     * @param transY  the y-value translated to Java2D space.
     * @param orientation  the plot orientation (<code>null</code> not
     *                     permitted).
     *
     * @since 1.0.4
     */
    protected void updateCrosshairValues(CrosshairState crosshairState,
            double x, double y, int domainAxisIndex, int rangeAxisIndex,
            double transX, double transY, PlotOrientation orientation) {

        Validation.requireNonNullParameterName(orientation, "orientation");

        if (crosshairState != null) {
            // do we need to update the crosshair values?
            if (this.plot.isDomainCrosshairLockedOnData()) {
                if (this.plot.isRangeCrosshairLockedOnData()) {
                    // both axes
                    crosshairState.updateCrosshairPoint(x, y, domainAxisIndex,
                            rangeAxisIndex, transX, transY, orientation);
                }
                else {
                    // just the domain axis...
                    crosshairState.updateCrosshairX(x, domainAxisIndex);
                }
            }
            else {
                if (this.plot.isRangeCrosshairLockedOnData()) {
                    // just the range axis...
                    crosshairState.updateCrosshairY(y, rangeAxisIndex);
                }
            }
        }

    }

    /**
     * Draws an item label.
     *
     * @param g2  the graphics device.
     * @param orientation  the orientation.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param x  the x coordinate (in Java2D space).
     * @param y  the y coordinate (in Java2D space).
     * @param negative  indicates a negative value (which affects the item
     *                  label position).
     */
    protected void drawItemLabel(Graphics2D g2, PlotOrientation orientation,
            XYDataset dataset, int series, int item, double x, double y,
            boolean negative) {

        XYItemLabelGenerator generator = getBaseItemLabelGenerator();
        if (generator != null) {
            Font labelFont = getBaseItemLabelFont();
            Paint paint = getBaseItemLabelPaint();
            g2.setFont(labelFont);
            g2.setPaint(paint);
            String label = generator.generateLabel(dataset, series, item);

            // get the label position..
            ItemLabelPosition position = (!negative) ? getBasePositiveItemLabelPosition() : getBaseNegativeItemLabelPosition();

            // work out the label anchor point...
            Point2D anchorPoint = AbstractRendererData.calculateLabelAnchorPoint(
                    position.getItemLabelAnchor(), x, y, getItemLabelAnchorOffset(), orientation);
            TextUtilities.drawRotatedString(label, g2,
                    (float) anchorPoint.getX(), (float) anchorPoint.getY(),
                    position.getTextAnchor(), position.getAngle(),
                    position.getRotationAnchor());
        }

    }

    /**
     * Draws all the annotations for the specified layer.
     *
     * @param g2  the graphics device.
     * @param dataArea  the data area.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param layer  the layer.
     * @param info  the plot rendering info.
     */
    @Override
    public void drawAnnotations(Graphics2D g2,
            Rectangle2D dataArea,
            ValueAxis domainAxis,
            ValueAxis rangeAxis,
            Layer layer,
            PlotRenderingInfo info) {

        Iterator<XYAnnotation> iterator = null;
        if (layer.equals(Layer.FOREGROUND)) {
            iterator = this.foregroundAnnotations.iterator();
        }
        else if (layer.equals(Layer.BACKGROUND)) {
            iterator = this.backgroundAnnotations.iterator();
        }
        else {
            // should not get here
            throw new RuntimeException("Unknown layer.");
        }
        while (iterator.hasNext()) {
            XYAnnotation annotation = iterator.next();
            int index = this.plot.getIndexOf(this);
            annotation.draw(g2, this.plot, dataArea, domainAxis, rangeAxis,
                    index, info);
        }

    }

    /**
     * Adds an entity to the collection.
     *
     * @param entities  the entity collection being populated.
     * @param area  the entity area (if <code>null</code> a default will be
     *              used).
     * @param dataset  the dataset.
     * @param series  the series.
     * @param item  the item.
     * @param entityX  the entity's center x-coordinate in user space (only
     *                 used if <code>area</code> is <code>null</code>).
     * @param entityY  the entity's center y-coordinate in user space (only
     *                 used if <code>area</code> is <code>null</code>).
     */
    protected void addEntity(EntityCollection entities, Shape area,
            XYDataset dataset, int item,
            double entityX, double entityY) {
        if (!getBaseCreateEntities()) {
            return;
        }
        Shape hotspot = area;
        if (hotspot == null) {
            double r = getDefaultEntityRadius();
            double w = r * 2;
            if (getPlot().getOrientation() == PlotOrientation.VERTICAL) {
                hotspot = new Ellipse2D.Double(entityX - r, entityY - r, w, w);
            }
            else {
                hotspot = new Ellipse2D.Double(entityY - r, entityX - r, w, w);
            }
        }
        String tip = null;
        XYToolTipGenerator generator = getBaseToolTipGenerator();
        if (generator != null) {
            tip = generator.generateToolTip(dataset, 0, item);
        }
        String url = null;
        if (getURLGenerator() != null) {
            url = getURLGenerator().generateURL(dataset, 0, item);
        }
        XYItemEntity entity = new XYItemEntity(hotspot, dataset, 0, item,
                tip, url);
        entities.add(entity);
    }
}

