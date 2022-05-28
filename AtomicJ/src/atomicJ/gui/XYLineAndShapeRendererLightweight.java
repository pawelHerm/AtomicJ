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
 * XYLineAndShapeRenderer.java
 * ---------------------------
 * (C) Copyright 2004-2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes:
 * --------
 * 27-Jan-2004 : Version 1 (DG);
 * 10-Feb-2004 : Minor change to drawItem() method to make cut-and-paste
 *               overriding easier (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState (DG);
 * 25-Aug-2004 : Added support for chart entities (required for tooltips) (DG);
 * 24-Sep-2004 : Added flag to allow whole series to be drawn as a path
 *               (necessary when using a dashed stroke with many data
 *               items) (DG);
 * 04-Oct-2004 : Renamed BooleanUtils --> BooleanUtilities (DG);
 * 11-Nov-2004 : Now uses ShapeUtilities to translate shapes (DG);
 * 27-Jan-2005 : The getLegendItem() method now omits hidden series (DG);
 * 28-Jan-2005 : Added new constructor (DG);
 * 09-Mar-2005 : Added fillPaint settings (DG);
 * 20-Apr-2005 : Use generators for legend tooltips and URLs (DG);
 * 22-Jul-2005 : Renamed defaultLinesVisible --> baseLinesVisible,
 *               defaultShapesVisible --> baseShapesVisible and
 *               defaultShapesFilled --> baseShapesFilled (DG);
 * 29-Jul-2005 : Added code to draw item labels (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 20-Jul-2006 : Set dataset and series indices in LegendItem (DG);
 * 06-Feb-2007 : Fixed bug 1086307, crosshairs with multiple axes (DG);
 * 21-Feb-2007 : Fixed bugs in clone() and equals() (DG);
 * 20-Apr-2007 : Updated getLegendItem() for renderer change (DG);
 * 18-May-2007 : Set dataset and seriesKey for LegendItem (DG);
 * 08-Jun-2007 : Fix for bug 1731912 where entities are created even for data
 *               items that are not displayed (DG);
 * 26-Oct-2007 : Deprecated override attributes (DG);
 * 02-Jun-2008 : Fixed tooltips at lower edges of data area (DG);
 * 17-Jun-2008 : Apply legend shape, font and paint attributes (DG);
 * 19-Sep-2008 : Fixed bug with drawSeriesLineAsPath - patch by Greg Darke (DG);
 * 18-May-2009 : Clip lines in drawPrimaryLine() (DG);
 * 
 */

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.util.LineUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ShapeUtilities;

import atomicJ.utilities.GeometryUtilities;

public abstract class XYLineAndShapeRendererLightweight <E extends XYLineAndShapeRendererData> extends AbstractXYItemRendererLightweight <E>
implements XYItemRenderer, Cloneable, PublicCloneable {

    private Shape legendLine;

    public XYLineAndShapeRendererLightweight(E rendererData)
    {
        super(rendererData);
        this.legendLine = new Line2D.Double(-7.0, 0.0, 7.0, 0.0);
    }

    @Override
    protected abstract XYLineAndShapeRendererDataMutable getDataForModification();

    public boolean getDrawSeriesLineAsPath() {
        return getData().getDrawSeriesLineAsPath();
    }

    @Override
    public int getPassCount() {
        return 2;
    }

    public Shape getLegendLine() {
        return this.legendLine;
    }

    public boolean getBaseLinesVisible() 
    {
        return getData().getBaseLinesVisible();
    }

    public void setBaseLinesVisible(boolean visible)
    {
        getDataForModification().setBaseLinesVisible(visible);
    }

    public boolean getBaseShapesVisible() {
        return getData().getBaseShapesVisible();
    }

    public void setBaseShapesVisible(boolean visible)
    {
        getDataForModification().setBaseShapesVisible(visible);
    }

    public boolean getBaseShapesFilled() 
    {
        return getData().getBaseShapesFilled();
    }

    public void setBaseShapesFilled(boolean filled)
    {
        getDataForModification().setBaseShapesFilled(filled);
    }

    public boolean getDrawOutlines() {
        return getData().getDrawOutlines();
    }

    public void setDrawOutlines(boolean draw)
    {
        getDataForModification().setDrawOutlines(draw);
    }

    public boolean getUseFillPaint() {
        return getData().getUseFillPaint();
    }

    public void setUseFillPaint(boolean use)
    {
        getDataForModification().setUseFillPaint(use);
    }

    public boolean getUseOutlinePaint() {
        return getData().getUseOutlinePaint();
    }

    public void setUseOutlinePaint(boolean use)
    {
        getDataForModification().setUseOutlinePaint(use);
    }

    /**
     * Records the state for the renderer.  This is used to preserve state
     * information between calls to the drawItem() method for a single chart
     * drawing.
     */
    public static class State extends XYItemRendererState {

        /** The path for the current series. */
        public GeneralPath seriesPath;

        /**
         * A flag that indicates if the last (x, y) point was 'good'
         * (non-null).
         */
        private boolean lastPointGood;

        /**
         * Creates a new state instance.
         *
         * @param info  the plot rendering info.
         */
        public State(PlotRenderingInfo info) {
            super(info);
        }

        /**
         * Returns a flag that indicates if the last point drawn (in the
         * current series) was 'good' (non-null).
         *
         * @return A boolean.
         */
        public boolean isLastPointGood() {
            return this.lastPointGood;
        }

        /**
         * Sets a flag that indicates if the last point drawn (in the current
         * series) was 'good' (non-null).
         *
         * @param good  the flag.
         */
        public void setLastPointGood(boolean good) {
            this.lastPointGood = good;
        }

        /**
         * This method is called by the {@link XYPlot} at the start of each
         * series pass.  We reset the state for the current series.
         *
         * @param dataset  the dataset.
         * @param series  the series index.
         * @param firstItem  the first item index for this pass.
         * @param lastItem  the last item index for this pass.
         * @param pass  the current pass index.
         * @param passCount  the number of passes.
         */
        @Override
        public void startSeriesPass(XYDataset dataset, int series,
                int firstItem, int lastItem, int pass, int passCount) {
            this.seriesPath.reset();
            this.lastPointGood = false;
            super.startSeriesPass(dataset, series, firstItem, lastItem, pass,
                    passCount);
        }

    }

    /**
     * Initialises the renderer.
     * <P>
     * This method will be called before the first item is rendered, giving the
     * renderer an opportunity to initialise any state information it wants to
     * maintain.  The renderer can do nothing if it chooses.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area inside the axes.
     * @param plot  the plot.
     * @param data  the data.
     * @param info  an optional info collection object to return data back to
     *              the caller.
     *
     * @return The renderer state.
     */
    @Override
    public XYItemRendererState initialise(Graphics2D g2,
            Rectangle2D dataArea,
            XYPlot plot,
            XYDataset data,
            PlotRenderingInfo info) {

        State state = new State(info);
        state.seriesPath = new GeneralPath();
        return state;

    }

    /**
     * Returns <code>true</code> if the specified pass is the one for drawing
     * lines.
     *
     * @param pass  the pass.
     *
     * @return A boolean.
     */
    protected boolean isLinePass(int pass) {
        return pass == 0;
    }

    /**
     * Returns <code>true</code> if the specified pass is the one for drawing
     * items.
     *
     * @param pass  the pass.
     *
     * @return A boolean.
     */
    protected boolean isItemPass(int pass) {
        return pass == 1;
    }

    /**
     * Draws the item (first pass). This method draws the lines
     * connecting the items.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the data is being drawn.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param pass  the pass.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     */
    protected void drawPrimaryLine(XYItemRendererState state,
            Graphics2D g2,
            XYPlot plot,
            XYDataset dataset,
            int pass,
            int series,
            int item,
            ValueAxis domainAxis,
            ValueAxis rangeAxis,
            Rectangle2D dataArea) {
        if (item == 0) {
            return;
        }

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(y1) || Double.isNaN(x1)) {
            return;
        }

        double x0 = dataset.getXValue(series, item - 1);
        double y0 = dataset.getYValue(series, item - 1);
        if (Double.isNaN(y0) || Double.isNaN(x0)) {
            return;
        }

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        double transX0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
        double transY0 = rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation);

        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        // only draw if we have good values
        if (Double.isNaN(transX0) || Double.isNaN(transY0)
                || Double.isNaN(transX1) || Double.isNaN(transY1)) {
            return;
        }

        PlotOrientation orientation = plot.getOrientation();
        boolean visible = false;
        if (orientation == PlotOrientation.HORIZONTAL) {
            state.workingLine.setLine(transY0, transX0, transY1, transX1);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            state.workingLine.setLine(transX0, transY0, transX1, transY1);
        }
        visible = LineUtilities.clipLine(state.workingLine, dataArea);
        if (visible) {
            drawFirstPassShape(g2, pass, series, item, state.workingLine);
        }
    }

    /**
     * Draws the first pass shape.
     *
     * @param g2  the graphics device.
     * @param pass  the pass.
     * @param series  the series index.
     * @param item  the item index.
     * @param shape  the shape.
     */
    protected void drawFirstPassShape(Graphics2D g2, int pass, int series,
            int item, Shape shape) {
        g2.setStroke(getBaseStroke());
        g2.setPaint(getBasePaint());
        g2.draw(shape);
    }


    /**
     * Draws the item (first pass). This method draws the lines
     * connecting the items. Instead of drawing separate lines,
     * a GeneralPath is constructed and drawn at the end of
     * the series painting.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param plot  the plot (can be used to obtain standard color information
     *              etc).
     * @param dataset  the dataset.
     * @param pass  the pass.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataArea  the area within which the data is being drawn.
     */
    protected void drawPrimaryLineAsPath(XYItemRendererState state,
            Graphics2D g2, XYPlot plot,
            XYDataset dataset,
            int pass,
            int series,
            int item,
            ValueAxis domainAxis,
            ValueAxis rangeAxis,
            Rectangle2D dataArea) {


        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        State s = (State) state;
        // update path to reflect latest point
        if (!Double.isNaN(transX1) && !Double.isNaN(transY1)) {
            float x = (float) transX1;
            float y = (float) transY1;
            PlotOrientation orientation = plot.getOrientation();
            if (orientation == PlotOrientation.HORIZONTAL) {
                x = (float) transY1;
                y = (float) transX1;
            }
            if (s.isLastPointGood()) {
                s.seriesPath.lineTo(x, y);
            }
            else {
                s.seriesPath.moveTo(x, y);
            }
            s.setLastPointGood(true);
        }
        else {
            s.setLastPointGood(false);
        }
        // if this is the last item, draw the path ...
        if (item == s.getLastItemIndex()) {
            // draw path
            drawFirstPassShape(g2, pass, series, item, s.seriesPath);
        }
    }

    /**
     * Draws the item shapes and adds chart entities (second pass). This method
     * draws the shapes which mark the item positions. If <code>entities</code>
     * is not <code>null</code> it will be populated with entity information
     * for points that fall within the data area.
     *
     * @param g2  the graphics device.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param dataArea  the area within which the data is being drawn.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param pass  the pass.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  the crosshair state.
     * @param entities the entity collection.
     */
    protected void drawSecondaryPass(Graphics2D g2, XYPlot plot,
            XYDataset dataset,
            int pass,  int firstItem, int lastItem,
            ValueAxis domainAxis,
            Rectangle2D dataArea,
            ValueAxis rangeAxis,
            CrosshairState crosshairState,
            EntityCollection entities) {


        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
        int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);

        boolean shapesVisible = this.getBaseShapesVisible();
        Shape originalShape = getBaseShape();

        for (int item = firstItem; item <= lastItem; item++) 
        {
            Shape entityArea = null;

            // get the data point...
            double x1 = dataset.getXValue(0, item);
            double y1 = dataset.getYValue(0, item);
            if (Double.isNaN(y1) || Double.isNaN(x1)) {
                continue;
            }

            double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
            double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

            if (shapesVisible) 
            {      
                Shape shape = (orientation == PlotOrientation.HORIZONTAL) ? GeometryUtilities.createTranslatedShapeForPlotting(originalShape, transY1, transX1) :GeometryUtilities.createTranslatedShapeForPlotting(originalShape, transX1, transY1);

                entityArea = shape;
                if (shape.intersects(dataArea)) {
                    if (this.getBaseShapesFilled()) 
                    {
                        Paint paintForInside = getUseFillPaint() ? getBaseFillPaint() : getBasePaint();

                        g2.setPaint(paintForInside);
                        g2.fill(shape);
                    }
                    if (this.getDrawOutlines()) 
                    {
                        Paint paintForOutline = getUseOutlinePaint() ? getBaseOutlinePaint(): getBasePaint();

                        g2.setPaint(paintForOutline);
                        g2.setStroke(getBaseOutlineStroke());
                        g2.draw(shape);
                    }
                }
            }

            double xx = transX1;
            double yy = transY1;
            if (orientation == PlotOrientation.HORIZONTAL) {
                xx = transY1;
                yy = transX1;
            }

            // draw the item label if there is one...
            if (getBaseItemLabelsVisible()) {
                drawItemLabel(g2, orientation, dataset, 0, item, xx, yy,
                        (y1 < 0.0));
            }

            updateCrosshairValues(crosshairState, x1, y1, domainAxisIndex,
                    rangeAxisIndex, transX1, transY1, orientation);

            // add an entity for the item, but only if it falls within the data
            // area...
            if (entities != null && GeometryUtilities.isPointInRect(dataArea, xx, yy))
            {
                addEntity(entities, entityArea, dataset,item, xx, yy);
            }
        }  

    }


    /**
     * Returns a legend item for the specified series.
     *
     * @param datasetIndex  the dataset index (zero-based).
     * @param series  the series index (zero-based).
     *
     * @return A legend item for the series (possibly <code>null</code).
     */
    @Override
    public LegendItem getLegendItem(int datasetIndex) {
        XYPlot plot = getPlot();
        if (plot == null) {
            return null;
        }

        XYDataset dataset = plot.getDataset(datasetIndex);
        if (dataset == null) {
            return null;
        }

        if (!getBaseSeriesVisible()) {
            return null;
        }
        String label = getLegendItemLabelGenerator().generateLabel(dataset);
        String description = label;
        String toolTipText = null;
        if (getLegendItemToolTipGenerator() != null) {
            toolTipText = getLegendItemToolTipGenerator().generateLabel(dataset);
        }
        String urlText = null;
        if (getLegendItemURLGenerator() != null) {
            urlText = getLegendItemURLGenerator().generateLabel(dataset);
        }
        boolean shapeIsVisible = getBaseShapesVisible();
        Shape shape = lookupLegendShape();
        boolean shapeIsFilled = getBaseShapesFilled();
        Paint fillPaint = (getUseFillPaint() ? getBaseFillPaint() : getBasePaint());
        boolean shapeOutlineVisible = getDrawOutlines();
        Paint outlinePaint = (getUseOutlinePaint() ? getBaseOutlinePaint() : getBasePaint());
        Stroke outlineStroke = getBaseOutlineStroke();
        boolean lineVisible = getBaseLinesVisible();
        Stroke lineStroke = getBaseStroke();
        Paint linePaint = getBasePaint();
        LegendItem result = new LegendItem(label, description, toolTipText,
                urlText, shapeIsVisible, shape, shapeIsFilled, fillPaint,
                shapeOutlineVisible, outlinePaint, outlineStroke, lineVisible,
                this.legendLine, lineStroke, linePaint);
        result.setLabelFont(getBaseLegendTextFont());
        Paint labelPaint = getBaseLegendTextPaint();
        if (labelPaint != null) {
            result.setLabelPaint(labelPaint);
        }
        result.setSeriesKey(dataset.getSeriesKey(0));
        result.setDataset(dataset);
        result.setDatasetIndex(datasetIndex);

        return result;
    }

    /**
     * Returns a clone of the renderer.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if the clone cannot be created.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        XYLineAndShapeRendererLightweight<?>clone = (XYLineAndShapeRendererLightweight<?>) super.clone();
        if (this.legendLine != null) {
            clone.legendLine = ShapeUtilities.clone(this.legendLine);
        }
        return clone;
    }

    /**
     * Tests this renderer for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return <code>true</code> or <code>false</code>.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYLineAndShapeRendererLightweight)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        XYLineAndShapeRendererLightweight<?> that = (XYLineAndShapeRendererLightweight<?>) obj;

        if (!ShapeUtilities.equal(this.legendLine, that.legendLine)) {
            return false;
        }             

        return true;
    }
}
