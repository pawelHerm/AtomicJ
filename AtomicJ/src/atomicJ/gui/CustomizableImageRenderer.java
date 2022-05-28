
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.gui;

import static atomicJ.gui.PreferenceKeys.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Hashtable;
import java.util.prefs.Preferences;


import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.RendererUtilities;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PublicCloneable;

import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.rois.GridBiPointRecepient;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIComposite;
import atomicJ.utilities.SerializationUtilities;
import atomicJ.utilities.Validation;

public class CustomizableImageRenderer extends AbstractXYItemRenderer implements  PublicCloneable, Channel2DRenderer, PaintScaleSource, DatasetChangeListener, GradientPaintReceiver 
{
    private static final long serialVersionUID = 1L;

    private static final ColorSupplier DEFAULT_SUPPLIER = DefaultColorSupplier.getSupplier();

    private final StyleTag styleTag;
    private String name;
    private final Preferences pref;	

    private double xDataDensity;
    private double yDataDensity;
    private GradientPaintScale paintScale;

    private boolean useOutsideRangeColors;
    private ColorGradient colorGradient;
    private Color gradientUnderflowColor;
    private Color gradientOverflowColor;

    //actual gradient bounds
    private double lowerGradientBound;
    private double upperGradientBound;

    private boolean automaticBoundsRefreshed; //calculation of automatic bounds may be time consuming, so they are refrshed only when it is necessary i.e. when the gradient range is automatic
    private double lowerAutomaticBound;
    private double upperAutomaticBound;

    private double lowerROIFullBound;
    private double upperROIFullBound;

    private double lowerFullBound;
    private double upperFullBound;

    private GradientMaskSelector gradientMaskSelector = GradientMaskSelector.NO_MASK;
    private GradientRangeSelector gradientRangeSelector = GradientRangeSelector.FULL;

    private final TooltipStyleManager tooltipStyleManager;

    private ROI mask = new ROIComposite("Empty");
    private Color maskColor;

    private boolean roiSamplesNeeded;

    private boolean is3DPlot = true;

    private ColorGradientLegend gradientLegend;
    private XYDataset dataset;

    private ColorSupplier supplier;

    private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    public CustomizableImageRenderer(StyleTag styleKey, String name)
    {
        this.styleTag = styleKey;
        this.name = name;
        this.pref = Preferences.userNodeForPackage(getClass()).node(styleKey.getPreferredStyleKey());

        this.tooltipStyleManager = new TooltipStyleManager(name, " Z: ", pref.node("DepthTooltip"), true);

        this.roiSamplesNeeded = calculateROISamplesNeeded();

        setAutoPopulateSeriesPaint(false);
        setPreferredStyle();    
    }

    private void setPreferredStyle()
    {        
        ColorSupplier colorSupplier = getSupplier();

        ColorGradient defaultLUTTable = colorSupplier.getGradient(styleTag);
        Paint defaultUnderflowColor = colorSupplier.getGradientUnderflow(styleTag);
        Paint defaultOverflowColor = colorSupplier.getGradientOverflow(styleTag);

        this.colorGradient = (ColorGradient)SerializationUtilities.getSerializableObject(pref, RANGE_COLOR_GRADIENT, defaultLUTTable);
        this.gradientUnderflowColor = (Color)SerializationUtilities.getSerializableObject(pref, PAINT_GRADIENT_UNDERFLOW, defaultUnderflowColor);
        this.gradientOverflowColor = (Color)SerializationUtilities.getSerializableObject(pref, PAINT_GRADIENT_OVERFLOW, defaultOverflowColor);					
        this.maskColor = (Color)SerializationUtilities.getSerializableObject(pref, MASK_COLOR, Color.black);				

        this.useOutsideRangeColors = pref.getBoolean(USE_OUTSIDE_RANGE_COLORS, false);
    }	

    @Override
    public Preferences getPreferences() 
    {
        return pref;
    }

    @Override
    public StyleTag getStyleKey()
    {
        return styleTag;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public boolean hasDepthAxis()
    {
        return (getDepthAxis() != null);
    }

    @Override
    public CustomizableNumberAxis getDepthAxis()
    {
        CustomizableNumberAxis depthAxis = (gradientLegend != null) ? gradientLegend.getAxis() : null;

        return depthAxis;
    }

    @Override
    public PrefixedUnit getDisplayedUnit()
    {
        CustomizableNumberAxis depthAxis = getDepthAxis();
        PrefixedUnit unit = depthAxis != null ? depthAxis.getDisplayedUnit() : null;

        return unit;
    }

    @Override
    public ColorGradientLegend getColorGradientLegend()
    {
        return gradientLegend;
    }

    @Override
    public void setColorGradientLegend(ColorGradientLegend legend)
    {
        this.gradientLegend = legend;
    }

    @Override
    public ColorGradient getColorGradient()
    {
        return colorGradient;
    }

    @Override
    public void setColorGradient(ColorGradient colorGradientNew)
    {
        ColorGradient colorGradientOld = this.colorGradient;
        this.colorGradient = colorGradientNew;

        firePropertyChange(GradientPaintReceiver.GRADIENT_COLOR, colorGradientOld, colorGradientNew);

        updatePaintScale();
    }

    @Override
    public Color getGradientUnderflowColor()
    {
        return gradientUnderflowColor;
    }

    @Override
    public void setGradientUnderflowColor(Color underflowColorNew)
    {
        Color underflowColorOld = this.gradientUnderflowColor;
        this.gradientUnderflowColor = underflowColorNew;

        firePropertyChange(GradientPaintReceiver.UNDERFLOW_COLOR, underflowColorOld, underflowColorNew);

        updatePaintScale();
    }

    @Override
    public Color getGradientOverflowColor()
    {
        return gradientOverflowColor;
    }	

    @Override
    public void setGradientOverflowColor(Color overflowColorNew)
    {
        Color overflowColor = overflowColorNew;
        this.gradientOverflowColor = overflowColorNew;

        firePropertyChange(GradientPaintReceiver.OVERFLOW_COLOR, overflowColor, gradientOverflowColor);

        updatePaintScale();
    }

    @Override
    public boolean getUseOutsideRangeColors()
    {
        return useOutsideRangeColors;
    }

    @Override
    public void setUseOutsideRangeColors(boolean useOutsideRangeColorsNew)
    {
        boolean useOutsideRangeColorsOld = this.useOutsideRangeColors;
        this.useOutsideRangeColors = useOutsideRangeColorsNew;

        firePropertyChange(GradientPaintReceiver.USE_OUTSIDE_RANGE_COLORS, useOutsideRangeColorsOld, this.useOutsideRangeColors);

        updatePaintScale();
    }

    @Override
    public TooltipStyleManager getTooltipStyleManager()
    {
        return tooltipStyleManager;
    }

    @Override
    public boolean isFullRange()
    {
        return GradientRangeSelector.FULL.equals(gradientRangeSelector);
    }

    private void setConsistentWithFullRange()
    {
        if(isFullRange())
        {
            setInternalLowerGradientBound(lowerFullBound);
            setInternalUpperGradientBound(upperFullBound);

            updatePaintScale();
        }		
    }

    @Override
    public boolean isAutomaticRange()
    {
        return GradientRangeSelector.AUTOMATIC.equals(gradientRangeSelector);
    }

    private void setConsistentWithAutomaticRange()
    {	
        if(isAutomaticRange())
        {
            if(!automaticBoundsRefreshed && dataset instanceof ProcessableXYZDataset)
            {
                ProcessableXYZDataset processableDataset = (ProcessableXYZDataset)dataset;

                Range automaticZRange = processableDataset.getAutomaticZRange();
                double lowerAutomaticBoundNew = automaticZRange.getLowerBound();
                double upperAutomaticBoundNew = automaticZRange.getUpperBound();

                setLowerAutomaticBound(lowerAutomaticBoundNew);
                setUpperAutomaticBound(upperAutomaticBoundNew);
            }

            setInternalLowerGradientBound(lowerAutomaticBound);
            setInternalUpperGradientBound(upperAutomaticBound);

            updatePaintScale();	
        }		
    }

    @Override
    public PrefixedUnit getDataUnit()
    {
        PrefixedUnit unit = (gradientLegend != null) ? gradientLegend.getDataUnit() : SimplePrefixedUnit.getNullInstance();       
        return unit;
    }

    @Override
    public double getLowerBound()
    {
        return lowerGradientBound;
    }

    private void setInternalLowerGradientBound(double lowerBoundNew)
    {
        double lowerBoundOld = this.lowerGradientBound;		
        this.lowerGradientBound = lowerBoundNew;

        firePropertyChange(RangeModel.LOWER_BOUND, lowerBoundOld, lowerBoundNew);
    }

    @Override
    public void setLowerBound(double lowerBoundNew)
    {
        setInternalLowerGradientBound(lowerBoundNew);
        setGradientRangeSelector(GradientRangeSelector.MANUAL);
        updatePaintScale();		
    }

    @Override
    public double getUpperBound()
    {
        return upperGradientBound;
    }

    private void setInternalUpperGradientBound(double upperBoundNew)
    {
        double upperGradientBoundOld = this.upperGradientBound;
        this.upperGradientBound = upperBoundNew;

        firePropertyChange(RangeModel.UPPER_BOUND, upperGradientBoundOld, upperBoundNew);		
    }

    @Override
    public void setUpperBound(double upperBoundNew)
    {
        setInternalUpperGradientBound(upperBoundNew);
        setGradientRangeSelector(GradientRangeSelector.MANUAL);

        updatePaintScale();		
    }

    @Override
    public double getLowerFullBound()
    {
        return lowerFullBound;
    }

    public void setLowerFullBound(double lowerFullBoundNew)
    {
        double lowerFullBoundOld = this.lowerFullBound;
        this.lowerFullBound = lowerFullBoundNew;

        firePropertyChange(RangeModel.LOWER_FULL_BOUND, lowerFullBoundOld, this.lowerFullBound);
    }

    @Override
    public double getUpperFullBound()
    {
        return upperFullBound;
    }

    public void setUpperFullBound(double upperFullBoundNew)
    {
        double upperFullBoundOld = this.upperFullBound;
        this.upperFullBound = upperFullBoundNew;

        firePropertyChange(RangeModel.UPPER_FULL_BOUND, upperFullBoundOld, this.upperFullBound);
    }

    @Override
    public double getLowerAutomaticBound()
    {
        return lowerAutomaticBound;
    }

    private void setLowerAutomaticBound(double lowerAutomaticBoundNew)
    {
        double lowerAutomaticBoundOld = this.lowerAutomaticBound;
        this.lowerAutomaticBound = lowerAutomaticBoundNew;

        firePropertyChange(RangeModel.LOWER_AUTOMATIC_BOUND, lowerAutomaticBoundOld, this.lowerAutomaticBound);
    }

    @Override
    public double getUpperAutomaticBound()
    {
        return upperAutomaticBound;
    }

    private void setUpperAutomaticBound(double upperAutomaticBoundNew)
    {
        double upperAutomaticBoundOld = this.upperAutomaticBound;
        this.upperAutomaticBound = upperAutomaticBoundNew;

        firePropertyChange(RangeModel.UPPER_AUTOMATIC_BOUND, upperAutomaticBoundOld, this.upperAutomaticBound);
    }

    public boolean isManualRange()
    {
        return GradientRangeSelector.MANUAL.equals(gradientRangeSelector);
    }

    @Override
    public boolean isColorROIFullRange()
    {
        return GradientRangeSelector.ROI_FULL.equals(gradientRangeSelector);
    }

    @Override
    public double getLowerROIBound()
    {
        return lowerROIFullBound;
    }

    @Override
    public void setLowerROIBound(double lowerLensBoundNew)
    {
        this.lowerROIFullBound = lowerLensBoundNew;
        setConsistentWithROIFullRange();
    }

    @Override
    public double getUpperROIBound()
    {
        return upperROIFullBound;
    }

    @Override
    public void setUpperROIBound(double upperLensBoundNew)
    {
        this.upperROIFullBound = upperLensBoundNew;
        setConsistentWithROIFullRange();
    }

    @Override
    public void setLensToFull()
    {
        this.lowerROIFullBound = lowerFullBound;
        this.upperROIFullBound = upperFullBound;
        setConsistentWithROIFullRange();
    }

    private void setConsistentWithROIFullRange()
    {		
        if(isColorROIFullRange())
        {
            setInternalLowerGradientBound(lowerROIFullBound);
            setInternalUpperGradientBound(upperROIFullBound);

            updatePaintScale();
        }			
    }

    @Override
    public void setGradientBounds(double lowerBoundNew, double upperBoundNew)
    {		
        setInternalLowerGradientBound(lowerBoundNew);
        setInternalUpperGradientBound(upperBoundNew);

        setGradientRangeSelector(GradientRangeSelector.MANUAL);		
        updatePaintScale();	
    }

    @Override
    public GradientRangeSelector getGradientRangeSelector() 
    {
        return gradientRangeSelector;
    }

    @Override
    public void setGradientRangeSelector(GradientRangeSelector selectorNew) 
    {
        GradientRangeSelector gradientRangeSelectorOld = this.gradientRangeSelector;
        this.gradientRangeSelector = selectorNew;

        firePropertyChange(GradientPaintReceiver.GRADIENT_RANGE_SELECTOR, gradientRangeSelectorOld, selectorNew);

        setConsistentWithGradientSelector();
        checkIfROISamplesNeeded();

    }

    @Override
    public boolean areROISamplesNeeded(){
        return roiSamplesNeeded;
    }

    private void checkIfROISamplesNeeded()
    {
        boolean roiSamplesNeededOld = this.roiSamplesNeeded;
        this.roiSamplesNeeded = calculateROISamplesNeeded();

        firePropertyChange(Channel2DSupervisor.ROI_SAMPLES_NEEDED, roiSamplesNeededOld, roiSamplesNeeded);
    }

    private boolean calculateROISamplesNeeded()
    {
        return GradientRangeSelector.ROI_FULL.equals(gradientRangeSelector);
    }

    private void setConsistentWithGradientSelector()
    {
        if(isFullRange())
        {
            setConsistentWithFullRange();
        }
        else if(isAutomaticRange())
        {
            setConsistentWithAutomaticRange();
        }
        else if(isColorROIFullRange())
        {
            setConsistentWithROIFullRange();
        }
    }	

    public boolean isNothingMasked()
    {
        return GradientMaskSelector.NO_MASK.equals(gradientMaskSelector);	
    }	

    public boolean isROIInsideMasked()
    {
        return GradientMaskSelector.MASK_INSIDE.equals(gradientMaskSelector);	
    }

    public boolean isROIOutsideMasked()
    {
        return GradientMaskSelector.MASK_OUTSIDE.equals(gradientMaskSelector);	
    }

    @Override
    public Color getMaskColor()
    {
        return maskColor;
    }

    @Override
    public void setMaskColor(Color maskColorNew)
    {
        Color maskColorOld = this.maskColor;
        this.maskColor = maskColorNew;

        if(!this.maskColor.equals(maskColorOld))
        {
            firePropertyChange(GradientPaintReceiver.MASK_COLOR, maskColorOld, this.maskColor);
            fireChangeEvent();
        }
    }

    @Override
    public GradientMaskSelector getGradientMaskSelector()
    {
        return gradientMaskSelector;
    }

    @Override
    public void setGradientMaskSelector(GradientMaskSelector gradientMaskSelectorNew)
    {
        GradientMaskSelector gradientMaskSelectorOld = this.gradientMaskSelector;
        this.gradientMaskSelector = gradientMaskSelectorNew;


        if(!gradientMaskSelectorOld.equals(this.gradientMaskSelector))
        {
            firePropertyChange(GRADIENT_MASK_SELECTOR, gradientMaskSelectorOld, this.gradientMaskSelector);
            fireChangeEvent();
        }
    }

    public ROI getMaskedRegion()
    {
        return mask;
    }

    @Override
    public void setMaskedRegion(ROI roiNew)
    {
        this.mask = roiNew;

        if(!isNothingMasked())
        {
            fireChangeEvent();
        }
    }

    public ColorSupplier getSupplier()
    {
        ColorSupplier supplier = (this.supplier != null) ? this.supplier : DEFAULT_SUPPLIER;
        return supplier;
    }

    public void setSupplier(ColorSupplier supplier)
    {
        this.supplier = supplier;
    }

    @Override
    public void datasetChanged(DatasetChangeEvent evt) 
    {               
        XYDataset dataset = (XYDataset)evt.getDataset();
        fitPaintScaleToDataset(dataset);
    }

    @Override
    public void registerDataset(XYDataset dataset)
    {
        if(this.dataset != null)
        {
            this.dataset.removeChangeListener(this);
        }

        this.dataset = dataset;
        dataset.addChangeListener(this);
    }

    @Override
    public void fitAndRegisterDataset(XYDataset dataset)
    {
        if(this.dataset != null)
        {
            this.dataset.removeChangeListener(this);
        }

        this.dataset = dataset;

        dataset.addChangeListener(this);

        fitPaintScaleToDataset(dataset);
    }

    public void fitPaintScaleToDataset(XYDataset dataset)
    {		
        this.is3DPlot = dataset instanceof ProcessableXYZDataset  && dataset.getItemCount(0)>0;

        if(is3DPlot)
        {	
            ProcessableXYZDataset processableDataset = (ProcessableXYZDataset)dataset;

            Range zRange = processableDataset.getZRange();


            setLowerFullBound(zRange.getLowerBound());
            setUpperFullBound(zRange.getUpperBound());


            if(isAutomaticRange())
            {
                Range automaticRange = processableDataset.getAutomaticZRange();

                setLowerAutomaticBound(automaticRange.getLowerBound());
                setUpperAutomaticBound(automaticRange.getUpperBound());

                automaticBoundsRefreshed = true;
            }
            else
            {
                automaticBoundsRefreshed = false;
            }


            boolean dataRecentlyChanged = processableDataset.isDataRecentlyChanged();

            if(dataRecentlyChanged)
            {
                processableDataset.setDataRecentlyChanged(false);
            }

            boolean setToFull = isFullRange() || (isManualRange() && dataRecentlyChanged);

            if(setToFull) 
            {                   
                setGradientBounds(lowerFullBound, upperFullBound);                  
            }
            else if(isAutomaticRange())
            {
                setGradientBounds(lowerAutomaticBound, upperAutomaticBound);
            }

            setLensToFull();

            this.xDataDensity = processableDataset.getXDataDensity();
            this.yDataDensity = processableDataset.getYDataDensity();
        }       

        updatePaintScale();
    }

    public void updatePaintScale()
    {
        //if the plot is not 3D, we don't need a paint scale
        if(!is3DPlot)
        {
            return;
        }

        Color underflowColor = useOutsideRangeColors ? gradientUnderflowColor : colorGradient.getColor(0);
        Color overflowColor = useOutsideRangeColors ? gradientOverflowColor : colorGradient.getColor(1);

        GradientPaintScale paintScale = new GradientPaintScale(lowerGradientBound, upperGradientBound, colorGradient, underflowColor, overflowColor);
        setPaintScale(paintScale);

    }

    @Override
    public double getXDataDensity() 
    {
        return this.xDataDensity;
    }

    public void setXDataDensity(double width) 
    {
        this.xDataDensity = Validation.requireValueGreaterThanParameterName(width, 0, "width");
        fireChangeEvent();
    }

    @Override
    public double getYDataDensity() 
    {
        return this.yDataDensity;
    }

    public void setYDataDensity(double height) 
    {
        this.yDataDensity = Validation.requireValueGreaterThanParameterName(height, 0, "height");
        fireChangeEvent();
    }

    @Override
    public GradientPaintScale getPaintScale() 
    {
        return this.paintScale;
    }

    private void setPaintScale(GradientPaintScale scale) 
    {
        this.paintScale = Validation.requireNonNullParameterName(scale, "scale");       

        if(gradientLegend != null)
        {
            gradientLegend.setScale(paintScale);
        }   

        fireChangeEvent();
    }

    @Override
    public Range findDomainBounds(XYDataset dataset) 
    {
        if (dataset instanceof ProcessableXYZDataset) 
        {
            ProcessableXYZDataset processableDataset = (ProcessableXYZDataset) dataset;
            Range dataRange = processableDataset.getXRange();

            double yDataDensity = processableDataset.getXDataDensity();

            Range paddedRange = new Range(dataRange.getLowerBound() - yDataDensity/2, dataRange.getUpperBound() + yDataDensity/2);   
            return paddedRange;
        }

        return super.findDomainBounds(dataset);
    }


    @Override
    public Range findRangeBounds(XYDataset dataset) 
    {
        if (dataset instanceof ProcessableXYZDataset) 
        {
            ProcessableXYZDataset processableDataset = (ProcessableXYZDataset) dataset;
            Range dataRange = processableDataset.getYRange();

            double yDataDensity = processableDataset.getYDataDensity();

            Range paddedRange = new Range(dataRange.getLowerBound() - yDataDensity/2, dataRange.getUpperBound() + yDataDensity/2);   
            return paddedRange;
        }

        return super.findRangeBounds(dataset);
    }

    @Override
    public QuantitativeSample getPaintedSample() 
    {
        QuantitativeSample sample = null;
        if(dataset instanceof ProcessableXYZDataset)
        {
            ProcessableXYZDataset processableDataset = (ProcessableXYZDataset)dataset;
            sample = processableDataset.getSample(0);
        }
        return sample;
    }

    //we do not want to draw individual items, as it is a performance-killer
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) 
    {
        throw new UnsupportedOperationException(); 
    }

    @Override
    public EntityCollection drawItems(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYZDataset dataset,
            int series,  CrosshairState crosshairState, int pass) 
    {        
        EntityCollection entityList = new StandardEntityCollection();

        if(!isSeriesVisible(series))
        {
            return entityList;
        }  		

        if(dataset instanceof Channel2DDataset && ((Channel2DDataset) dataset).getDisplayedChannel().getChannelData() instanceof GridChannel2DData)
        {	
            GridChannel2DData channelData = (GridChannel2DData) ((Channel2DDataset) dataset).getDisplayedChannel().getChannelData();

            EntityCollection ents = drawItemsFast(g2, state, dataArea, info, plot, domainAxis, rangeAxis, channelData, crosshairState, pass);
            entityList.addAll(ents);
        }
        else
        {            
            int firstItem = 0;
            int lastItem = dataset.getItemCount(series) - 1;
            if (lastItem == -1) 
            {
                return entityList;
            }
            if (state.getProcessVisibleItemsOnly()) 
            {
                int[] itemBounds = RendererUtilities.findLiveItems(dataset, series, domainAxis.getLowerBound(), domainAxis.getUpperBound());
                firstItem = Math.max(itemBounds[0] - 1, 0);
                lastItem = Math.min(itemBounds[1] + 1, lastItem);
            }
            state.startSeriesPass(dataset, series, firstItem, lastItem, pass, getPassCount());
            //changes by P.Hermanowicz starts here

            int[] items = new int[lastItem - firstItem + 1];

            for (int item = firstItem, i = 0; item <= lastItem; item++, i++) 
            {
                items[i] = item;

            }


            float xIncrement = (float)domainAxis.lengthToJava2D(xDataDensity, dataArea, plot.getDomainAxisEdge());
            float yIncrement = (float)rangeAxis.lengthToJava2D(yDataDensity, dataArea, plot.getRangeAxisEdge());

            PlotOrientation orientation = plot.getOrientation();
            boolean isHorizontal = orientation.equals(PlotOrientation.HORIZONTAL);

            RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();
            RectangleEdge rangeAxisRange = plot.getRangeAxisEdge();

            for(int item: items)
            {
                if(!getItemVisible(series, item)) 
                {
                    break;   
                }

                double x = dataset.getXValue(series, item);
                double y = dataset.getYValue(series, item);

                if (Double.isNaN(x) || Double.isNaN(y)) 
                {
                    break;
                }

                double  z = dataset.getZValue(series, item);

                Paint p = paintScale.getPaint(z);

                double xOrigin = x - 0.5*xDataDensity;
                double yOrigin = y + 0.5*yDataDensity;

                float trXOrigin = (float)domainAxis.valueToJava2D(xOrigin, dataArea, domainAxisEdge);
                float trYOrigin = (float)rangeAxis.valueToJava2D(yOrigin, dataArea, rangeAxisRange);

                Rectangle2D shape;
                if (isHorizontal) 
                {
                    if (dataArea.intersects(trYOrigin, trXOrigin, yIncrement, xIncrement)) 
                    {
                        shape = new Rectangle2D.Float(trYOrigin, trXOrigin, yIncrement, xIncrement);

                        g2.setPaint(p);
                        g2.fill(shape);
                    }
                }
                else 
                {
                    if (dataArea.intersects(trXOrigin, trYOrigin,xIncrement, yIncrement)) 
                    {
                        shape = new Rectangle2D.Float(trXOrigin, trYOrigin,xIncrement, yIncrement);

                        g2.setPaint(p);
                        g2.fill(shape);
                    }
                }            
            }

            state.endSeriesPass(dataset, series, firstItem,
                    lastItem, pass, getPassCount());

        }

        return entityList;
    }

    public EntityCollection drawItemsFast(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, 
            final GridChannel2DData channel, CrosshairState crosshairState, int pass) 
    { 
        EntityCollection entities = new StandardEntityCollection();

        PlotOrientation orientation = plot.getOrientation();
        boolean isVertical = orientation.equals(PlotOrientation.VERTICAL);
        boolean isDomainInverted = domainAxis.isInverted();
        boolean isRangeInverted = rangeAxis.isInverted();

        RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();
        RectangleEdge rangeAxisEdge = plot.getRangeAxisEdge();

        Grid2D grid = channel.getGrid();

        //the extremal values of data plus margins, so that the pixel's diameters is taken into account
        //the extremal coordinates of the largest rectangle which may be  filled with data (taking into account  pixel's diamater)

        //the extremal coordinates of the rectangle filled with data (plus pixel margins)
        double minHorizonatalJava2D = dataArea.getX();
        double maxHorizontalJava2D = dataArea.getX() + dataArea.getWidth();

        double minVerticalJava2D =  dataArea.getY();
        double maxVerticalJava2D = dataArea.getY() + dataArea.getHeight();


        double x0java2D = isVertical ? minHorizonatalJava2D: maxVerticalJava2D;
        double x1java2D = isVertical ? maxHorizontalJava2D: minVerticalJava2D;

        double y0java2D = isVertical ? maxVerticalJava2D: minHorizonatalJava2D;
        double y1java2D = isVertical ? minVerticalJava2D: maxHorizontalJava2D;
        double x0, x1, y0, y1;
        if(isDomainInverted)
        {
            x0 = domainAxis.java2DToValue(x1java2D, dataArea, domainAxisEdge);
            x1 = domainAxis.java2DToValue(x0java2D, dataArea, domainAxisEdge);
        }
        else
        {
            x0 = domainAxis.java2DToValue(x0java2D, dataArea, domainAxisEdge);
            x1 = domainAxis.java2DToValue(x1java2D, dataArea, domainAxisEdge);
        }
        if(isRangeInverted)
        {
            y0 = rangeAxis.java2DToValue(y1java2D, dataArea, rangeAxisEdge);
            y1 = rangeAxis.java2DToValue(y0java2D, dataArea, rangeAxisEdge);

        }
        else
        {
            y0 = rangeAxis.java2DToValue(y0java2D, dataArea, rangeAxisEdge);
            y1 = rangeAxis.java2DToValue(y1java2D, dataArea, rangeAxisEdge);       
        }
        final int minColumn = Math.max(0, grid.getColumn(x0) - 1);
        final int maxColumn = Math.min(grid.getColumnCount() - 1, grid.getColumn(x1) + 1);
        final int minRow = Math.max(0, grid.getRow(y0) - 1);
        final int maxRow = Math.min(grid.getRowCount() - 1,grid.getRow(y1) + 1);

        //////////////////////////
        if(minColumn > - 1 && maxColumn >-1 && minRow> - 1 && maxRow >-1)
        {		
        }
        else
        {
            return entities;
        }

        double xIncrement = grid.getXIncrement();
        double yIncrement = grid.getYIncrement();
        double minX = grid.getX(minColumn) - 0.5*xIncrement;
        double maxX = grid.getX(maxColumn) + 0.5*xIncrement;
        double minY = grid.getY(minRow) - 0.5*yIncrement;
        double maxY = grid.getY(maxRow) + 0.5*yIncrement;

        double occupiedAreaMinX = isVertical ? domainAxis.valueToJava2D(minX, dataArea, domainAxisEdge) : rangeAxis.valueToJava2D(minY, dataArea, rangeAxisEdge);
        double occupiedAreaMinY = isVertical ? rangeAxis.valueToJava2D(maxY, dataArea, rangeAxisEdge) : domainAxis.valueToJava2D(maxX, dataArea, domainAxisEdge);
        double occupiedAreaMaxX = isVertical ? domainAxis.valueToJava2D(maxX, dataArea, domainAxisEdge) : rangeAxis.valueToJava2D(maxY, dataArea, rangeAxisEdge);
        double occupiedAreaMaxY = isVertical ? rangeAxis.valueToJava2D(minY, dataArea, rangeAxisEdge) : domainAxis.valueToJava2D(minX, dataArea, domainAxisEdge);

        Rectangle2D occupiedDataArea = new Rectangle2D.Double(occupiedAreaMinX, occupiedAreaMinY, occupiedAreaMaxX - occupiedAreaMinX, occupiedAreaMaxY - occupiedAreaMinY);

        entities.add(new ChartEntity(occupiedDataArea, null, null));

        BufferedImage image = null;

        final int maskColorInt = maskColor.getRGB();

        if(isVertical)
        {            
            final int imageWidth = maxColumn - minColumn + 1;
            final int imageHeight = maxRow - minRow + 1;

            final DataBuffer buffer = new DataBufferInt(imageWidth*imageHeight);

            int index = 0;

            if(isNothingMasked())
            {
                for(int i = maxRow ; i>=minRow; i--)
                {
                    for(int j = minColumn; j<=maxColumn;j++)
                    {
                        double z = channel.getZ(i, j);
                        int color = paintScale.getColorInteger(z);
                        buffer.setElem(index++,color);
                    }
                }	
            }
            else if(isROIOutsideMasked())
            {
                mask.dividePoints(grid, minRow, maxRow, minColumn, maxColumn, new GridBiPointRecepient() 
                {                               
                    @Override
                    public void addPointInside(int row, int column) 
                    {   
                        double z = channel.getZ(row, column);
                        int color = paintScale.getColorInteger(z);

                        int index = imageWidth*(maxRow - row) + column - minColumn;

                        buffer.setElem(index,color);
                    }

                    @Override
                    public void addPointOutside(int row, int column)
                    {                       
                        int index = imageWidth*(maxRow - row) + column - minColumn;                        
                        buffer.setElem(index,maskColorInt);

                    }
                });
            }
            else if(isROIInsideMasked())
            {
                mask.dividePoints(grid, minRow, maxRow, minColumn, maxColumn, new GridBiPointRecepient() 
                {                               
                    @Override
                    public void addPointInside(int row, int column) 
                    {                                                  
                        int index = imageWidth*(maxRow - row) + column - minColumn;                        
                        buffer.setElem(index, maskColorInt);
                    }

                    @Override
                    public void addPointOutside(int row, int column)
                    {                      
                        double z = channel.getZ(row, column);
                        int color = paintScale.getColorInteger(z);

                        int index = imageWidth*(maxRow - row) + column - minColumn;

                        buffer.setElem(index, color);
                    }
                });

            }

            SinglePixelPackedSampleModel sppsm = 
                    new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, imageWidth, imageHeight, imageWidth,
                            new int[] {0x00ff0000,0x0000ff00, 0x000000ff, 0xff000000});
            WritableRaster raster = Raster.createWritableRaster(sppsm, buffer, new Point(0,0));				
            ColorModel model = new DirectColorModel(32,0x00ff0000,0x0000ff00, 0x000000ff, 0xff000000);
            image =  new BufferedImage(model, raster, false, new Hashtable<>());
        }
        else
        {
            final int imageWidth = maxRow - minRow + 1;
            final int imageHeight = maxColumn - minColumn + 1;

            final DataBuffer buffer = new DataBufferInt(imageWidth*imageHeight);
            int index = 0;

            if(isNothingMasked())
            {
                for(int i = maxColumn; i >= minColumn; i--)
                {
                    for(int j = minRow; j <= maxRow; j++)
                    {
                        double z = channel.getZ(j, i);
                        int color = paintScale.getColorInteger(z);
                        buffer.setElem(index++, color);
                    }
                }	
            }
            else if(isROIOutsideMasked())
            {
                mask.dividePoints(grid, minRow, maxRow, minColumn, maxColumn, new GridBiPointRecepient() 
                {                               
                    @Override
                    public void addPointInside(int row, int column) 
                    {   
                        double z = channel.getZ(row, column);
                        int color = paintScale.getColorInteger(z);

                        int index = imageWidth*(row - minRow) + (maxColumn - column);

                        buffer.setElem(index,color);
                    }

                    @Override
                    public void addPointOutside(int row, int column)
                    {                       
                        int index = imageWidth*(row - minRow) + (maxColumn - column);                       
                        buffer.setElem(index,maskColorInt);

                    }
                });
            }
            else if(isROIInsideMasked())
            {
                mask.dividePoints(grid, minRow, maxRow, minColumn, maxColumn, new GridBiPointRecepient() 
                {                               
                    @Override
                    public void addPointInside(int row, int column) 
                    {   
                        int index = imageHeight*(maxColumn - column) + (row - minRow);                
                        buffer.setElem(index,maskColorInt);
                    }

                    @Override
                    public void addPointOutside(int row, int column)
                    {                                               
                        double z = channel.getZ(row, column);
                        int color = paintScale.getColorInteger(z);

                        int index = imageHeight*(maxColumn - column) + (row - minRow);                
                        buffer.setElem(index,color);
                    }
                });
            }

            WritableRaster raster = Raster.createPackedRaster(buffer, imageWidth, imageHeight, imageWidth, new int[] {0x00ff0000,0x0000ff00, 0x000000ff, 0xff000000}, null);				
            ColorModel model = new DirectColorModel(32,0x00ff0000,0x0000ff00, 0x000000ff, 0xff000000);
            image =  new BufferedImage(model, raster, false, new Hashtable<>());
        }

        g2.drawImage(image, (int) Math.rint(occupiedDataArea.getX()), (int) Math.rint(occupiedDataArea.getY()),  (int) Math.ceil(occupiedDataArea.getWidth()), (int) Math.ceil(occupiedDataArea.getHeight()), null);        

        return entities;
    }  

    @Override
    public void drawDomainMarker(java.awt.Graphics2D g2, XYPlot plot, ValueAxis axis, Marker marker, java.awt.geom.Rectangle2D dataArea)
    {
        if(marker instanceof CustomizableIntervalMarker)
        {
            if(!((CustomizableIntervalMarker) marker).isVisible())
            {
                return;
            }
        }
        else if(marker instanceof CustomizableValueMarker)
        {
            if(!((CustomizableValueMarker) marker).isVisible())
            {
                return;
            }
            ((CustomizableValueMarker) marker).drawKnobs(g2, plot, dataArea, axis, plot.getRangeAxis(), true);
        }
        super.drawDomainMarker(g2, plot, axis, marker, dataArea);
    }

    @Override
    public void drawRangeMarker(java.awt.Graphics2D g2, XYPlot plot, ValueAxis axis, Marker marker, java.awt.geom.Rectangle2D dataArea)
    {
        if(marker instanceof CustomizableIntervalMarker)
        {
            if(!((CustomizableIntervalMarker) marker).isVisible())
            {
                return;
            }
        }
        else if(marker instanceof CustomizableValueMarker)
        {
            if(!((CustomizableValueMarker) marker).isVisible())
            {
                return;
            }
            ((CustomizableValueMarker) marker).drawKnobs(g2, plot, dataArea, plot.getRangeAxis(), axis, false);
        }
        super.drawRangeMarker(g2, plot, axis, marker, dataArea);
    }


    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertySupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertySupport.removePropertyChangeListener(listener);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        propertySupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public Object clone()
    {
        try 
        {
            CustomizableImageRenderer clone = (CustomizableImageRenderer) super.clone();

            clone.dataset = null;
            clone.gradientLegend = null;

            return clone;

        } 
        catch (CloneNotSupportedException e) 
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void requestPaintScaleChange(Point2D java2DPoint, double percent)
    {
        if(gradientLegend == null)
        {
            return;
        }

        if(gradientLegend.isStripClicked(java2DPoint))
        {
            double ratio = gradientLegend.getClickedPositionRatio(java2DPoint);                            

            double rangeLength = upperGradientBound - lowerGradientBound;
            double increment = percent*rangeLength;

            if(ratio<1/3.)
            {                   
                double newGradientLowerBound = lowerGradientBound - increment;              
                setLowerBound(newGradientLowerBound);  
            }
            else if(ratio>2/3.)
            {                                           
                double newGradientUpperBound =  upperGradientBound + increment;                             
                setUpperBound(newGradientUpperBound);
            }
            else if(ratio >= 1/3. && ratio<=2/3.)
            {
                double newGradientLowerBound = lowerGradientBound - increment;
                double newGradientUpperBound =  upperGradientBound + increment;

                setGradientBounds(newGradientLowerBound, newGradientUpperBound);
            }
        }        
    }
}