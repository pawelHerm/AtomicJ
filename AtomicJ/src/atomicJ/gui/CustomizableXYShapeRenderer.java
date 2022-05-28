
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Hashtable;
import java.util.Locale;
import java.util.prefs.Preferences;


import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.RendererUtilities;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.PublicCloneable;

import atomicJ.data.Grid2D;
import atomicJ.data.GridChannel2DData;
import atomicJ.data.QuantitativeSample;
import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.rois.GridBiPointRecepient;
import atomicJ.gui.rois.GridBiPositionCalculator;
import atomicJ.gui.rois.ROI;
import atomicJ.gui.rois.ROIComposite;
import atomicJ.utilities.SerializationUtilities;
import atomicJ.utilities.Validation;

public class CustomizableXYShapeRenderer extends AbstractXYItemRenderer implements Channel2DRenderer, PublicCloneable, PaintScaleSource, GradientPaintReceiver, DatasetChangeListener, NumberFormatReceiver 
{
    private static final long serialVersionUID = 1L;

    private static final ColorSupplier DEFAULT_SUPPLIER = DefaultColorSupplier.getSupplier();

    private GradientPaintScale paintScale;

    private boolean outlinesVisible;
    private Paint outlinePaint;
    private Stroke outlineStroke;

    private boolean guidelinesVisible;
    private Paint guidelinesPaint;
    private Stroke guidelinesStroke;

    private boolean labelVisible;
    private Font labelFont;
    private Paint labelPaint;

    private final StyleTag styleTag;
    private String name;
    private final Preferences pref;	
    private int shapeIndex;
    private double shapeMargins;

    private double xDataDensity;
    private double yDataDensity;

    private boolean useOutsideRangeColors;
    private ColorGradient colorGradient;
    private Color gradientUnderflowColor;
    private Color gradientOverflowColor;

    private double lowerBound;
    private double upperBound;

    private ROI mask = new ROIComposite("Empty");
    private Color maskColor;

    private GradientMaskSelector gradientMaskSelector = GradientMaskSelector.NO_MASK;
    private GradientRangeSelector gradientRangeSelector = GradientRangeSelector.FULL;
    private boolean roiSamplesNeeded;

    private boolean automaticBoundsRefreshed; //calculation of automatic bounds may be time consuming, so they are refrshed only when it is necessary i.e. when the gradient range is automatic
    private double lowerAutomaticBound;
    private double upperAutomaticBound;

    private double lowerROIFullBound;
    private double upperROIFullBound;

    private double lowerFullBound;
    private double upperFullBound;

    private final TooltipStyleManager toolTipsStyleManager;

    private boolean stretchShape;

    private boolean is3DPlot;
    private XYDataset dataset;

    private ColorGradientLegend gradientLegend;

    private final DecimalFormat format = new DecimalFormat();
    private boolean showTrailingZeroes;

    private ColorSupplier supplier;	

    private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);


    public CustomizableXYShapeRenderer(StyleTag styleKey, String name)
    {
        this.styleTag = styleKey;
        this.name = name;
        this.pref = Preferences.userNodeForPackage(getClass()).node(styleKey.getPreferredStyleKey());

        this.outlinesVisible = false;
        this.guidelinesVisible = false;
        this.guidelinesPaint = Color.darkGray;
        this.guidelinesStroke = new BasicStroke();

        setBaseShape(new Ellipse2D.Double(-5.0, -5.0, 10.0, 10.0));
        setAutoPopulateSeriesShape(false);

        this.toolTipsStyleManager = new TooltipStyleManager(name, " Z: ", pref.node("DepthTooltip"), true);

        this.roiSamplesNeeded = calculateROISamplesNeeded();
        ItemLabelPosition labelPosition = new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER);

        setBasePositiveItemLabelPosition(labelPosition);
        setBaseNegativeItemLabelPosition(labelPosition);

        setAutoPopulateSeriesPaint(false);

        setPreferredStyle();
    }

    @Override
    public Preferences getPreferences() 
    {
        return pref;
    }

    private void setPreferredStyle()
    {
        ColorGradient defaultColorGradient = GradientColorsBuiltIn.getGradients().get("Golden");

        Paint defaultUnderflowColor = getSupplier().getGradientUnderflow(styleTag);
        Paint defaultOverflowColor = getSupplier().getGradientOverflow(styleTag);

        Paint defaultGuidelinePaint = Color.black;
        Paint defaultOutlinePaint = Color.black;

        int defIndex = 0;
        double defShapeMargin = 0.;		

        guidelinesVisible = pref.getBoolean(MAP_GUIDELINES_VISIBLE, false);
        guidelinesPaint = (Paint)SerializationUtilities.getSerializableObject(pref, MAP_GUIDELINES_PAINT, defaultGuidelinePaint);
        guidelinesStroke = SerializationUtilities.getStroke(pref, MAP_GUIDELINES_STROKE, new BasicStroke(1.f));

        outlinesVisible = pref.getBoolean(MAP_OUTLINES_VISIBLE, false);
        outlinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, MAP_OUTLINES_PAINT, defaultOutlinePaint);
        outlineStroke = SerializationUtilities.getStroke(pref, MAP_OUTLINES_STROKE, new BasicStroke(1.f));

        shapeIndex = pref.getInt(PreferredContinuousSeriesRendererStyle.SHAPE_INDEX, defIndex);
        shapeMargins = pref.getDouble(MAP_MARKER_MARGIN, defShapeMargin);
        stretchShape = pref.getBoolean(MAP_MARKERS_STRETCHED, true);

        labelVisible = pref.getBoolean(MAP_VALUE_LABELS_VISIBLE, true);

        labelFont = (Font)SerializationUtilities.getSerializableObject(pref, MAP_VALUE_LABEL_FONT, new Font("Dialog", Font.BOLD, 14));
        labelPaint = (Paint)SerializationUtilities.getSerializableObject(pref, MAP_VALUE_LABEL_PAINT, Color.black);
        colorGradient = (ColorGradient)SerializationUtilities.getSerializableObject(pref, RANGE_COLOR_GRADIENT, defaultColorGradient);
        gradientUnderflowColor = (Color)SerializationUtilities.getSerializableObject(pref, PAINT_GRADIENT_UNDERFLOW, defaultUnderflowColor);
        gradientOverflowColor = (Color)SerializationUtilities.getSerializableObject(pref, PAINT_GRADIENT_OVERFLOW, defaultOverflowColor);				
        useOutsideRangeColors = pref.getBoolean(USE_OUTSIDE_RANGE_COLORS, false);

        maskColor = (Color)SerializationUtilities.getSerializableObject(pref, MASK_COLOR, Color.black);				

        setOutlinesVisible(outlinesVisible);	    
        setGuidelinesVisible(guidelinesVisible);
        setDecimalFormatPreferrences(pref);
    }

    private void setDecimalFormatPreferrences(Preferences pref)
    {		
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

        boolean groupingUsed = pref.getBoolean(MAP_LABEL_GROUPING_USED, false);		
        showTrailingZeroes = pref.getBoolean(MAP_LABEL_TRAILING_ZEROES, false);
        char groupingSeparator = (char) pref.getInt(MAP_LABEL_GROUPING_SEPARATOR, ' ');				
        char decimalSeparator = (char) pref.getInt(MAP_LABEL_DECIMAL_SEPARATOR, '.');				
        int maxDigit = pref.getInt(MAP_LABEL_MAX_FRACTION_DIGITS, 4);
        int minDigit = showTrailingZeroes ? maxDigit : 0;

        symbols.setDecimalSeparator(decimalSeparator);
        symbols.setGroupingSeparator(groupingSeparator);

        format.setMaximumFractionDigits(maxDigit);
        format.setMinimumFractionDigits(minDigit);
        format.setGroupingUsed(groupingUsed);

        format.setDecimalFormatSymbols(symbols);		
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
    public PrefixedUnit getDataUnit()
    {
        PrefixedUnit unit = (gradientLegend != null) ? gradientLegend.getDataUnit(): SimplePrefixedUnit.getNullInstance();

        return unit;
    }

    @Override
    public double getXDataDensity() 
    {
        return this.xDataDensity;
    }

    @Override
    public double getYDataDensity() 
    {
        return this.yDataDensity;
    }

    public int getShapeIndex()
    {
        return shapeIndex;
    }

    public void setShapeIndex(int i)
    {
        shapeIndex = i;
        notifyListeners(new RendererChangeEvent(this));
    }

    public double getShapeMargins()
    {
        return shapeMargins;
    }

    public void setShapeMargin(double marginNew)
    {
        this.shapeMargins = marginNew;
        notifyListeners(new RendererChangeEvent(this));
    }

    @Override
    public TooltipStyleManager getTooltipStyleManager()
    {
        return toolTipsStyleManager;
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
    public boolean isFullRange()
    {
        return GradientRangeSelector.FULL.equals(gradientRangeSelector);
    }	

    private void setConsistentWithFullRange()
    {
        setInternalLowerGradientBound(lowerFullBound);
        setInternalUpperGradientBound(upperFullBound);

        updatePaintScale();
    }

    @Override
    public boolean isAutomaticRange()
    {
        return GradientRangeSelector.AUTOMATIC.equals(gradientRangeSelector);
    }

    private void setConsistentWithAutomaticRange()
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
    public void setLowerROIBound(double lowerROIBoundNew)
    {
        this.lowerROIFullBound = lowerROIBoundNew;
        setConsistentWithColorLensRange();
    }

    @Override
    public double getUpperROIBound()
    {
        return upperROIFullBound;
    }

    @Override
    public void setUpperROIBound(double upperROIBoundNew)
    {
        this.upperROIFullBound = upperROIBoundNew;
        setConsistentWithColorLensRange();
    }

    @Override
    public void setLensToFull()
    {
        this.lowerROIFullBound = lowerFullBound;
        this.upperROIFullBound = upperFullBound;
        setConsistentWithColorLensRange();
    }

    private void setConsistentWithColorLensRange()
    {
        if(isColorROIFullRange())
        {
            setInternalLowerGradientBound(lowerROIFullBound);
            setInternalUpperGradientBound(upperROIFullBound);

            updatePaintScale();
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

    public ROI getROI()
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

    @Override
    public double getLowerBound()
    {
        return lowerBound;
    }

    private void setInternalLowerGradientBound(double lowerBoundNew)
    {
        double lowerBoundOld = this.lowerBound;		
        this.lowerBound = lowerBoundNew;

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
        return upperBound;
    }

    private void setInternalUpperGradientBound(double upperBoundNew)
    {
        double upperGradientBoundOld = this.upperBound;
        this.upperBound = upperBoundNew;

        firePropertyChange(RangeModel.UPPER_BOUND, upperGradientBoundOld, upperBoundNew);		
    }

    @Override
    public void setUpperBound(double upperBoundNew)
    {
        setInternalUpperGradientBound(upperBoundNew);
        setGradientRangeSelector(GradientRangeSelector.MANUAL);

        updatePaintScale();		
    }

    public boolean isManualRange()
    {
        return GradientRangeSelector.MANUAL.equals(gradientRangeSelector);
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

        checkIfROISamplesNeeded();

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
            setConsistentWithColorLensRange();
        }
    }

    @Override
    public boolean areROISamplesNeeded()
    {
        return roiSamplesNeeded;
    }

    private void checkIfROISamplesNeeded()
    {
        boolean roiSamplesNeededOld = roiSamplesNeeded;
        this.roiSamplesNeeded = calculateROISamplesNeeded();

        firePropertyChange(Channel2DSupervisor.ROI_SAMPLES_NEEDED, roiSamplesNeededOld, roiSamplesNeeded);
    }

    private boolean calculateROISamplesNeeded()
    {
        return GradientRangeSelector.ROI_FULL.equals(gradientRangeSelector);
    }

    public boolean isShapeStretched()
    {
        return stretchShape;
    }

    public void setStretchShape(boolean stretchShape)
    {
        this.stretchShape = stretchShape;
        notifyListeners(new RendererChangeEvent(this));
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
        fitRendererToDataset(dataset);
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

        fitRendererToDataset(dataset);
    }

    public void fitRendererToDataset(XYDataset dataset)
    {
        int seriesCount = dataset.getSeriesCount();
        this.is3DPlot = (dataset instanceof ProcessableXYZDataset) && seriesCount>0 && dataset.getItemCount(0)>0;

        if(this.is3DPlot)
        {	
            ProcessableXYZDataset processableDataset = (ProcessableXYZDataset)dataset;
            Range zRange = processableDataset.getZRange();

            if(zRange != null)
            {
                setLowerFullBound(zRange.getLowerBound());
                setUpperFullBound(zRange.getUpperBound());
            }

            if(isAutomaticRange())
            {
                Range automaticRange = processableDataset.getAutomaticZRange();

                if(automaticRange != null)
                {
                    setLowerAutomaticBound(automaticRange.getLowerBound());
                    setUpperAutomaticBound(automaticRange.getUpperBound());

                    automaticBoundsRefreshed = true;
                }
                automaticBoundsRefreshed = false;
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

        //fit labels

        boolean largeDataset = dataset.getItemCount(0) > 900;
        labelVisible = labelVisible && !largeDataset;
        guidelinesVisible = guidelinesVisible && !largeDataset;
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

        notifyListeners(new RendererChangeEvent(this));
    }

    @Override

    public void requestPaintScaleChange(Point2D java2DPoint, double percent)
    {
        if(gradientLegend != null && gradientLegend.isStripClicked(java2DPoint))
        {
            double ratio = gradientLegend.getClickedPositionRatio(java2DPoint);                            

            double rangeLength = upperBound - lowerBound;
            double increment = percent*rangeLength;

            if(ratio<1/3.)
            {                   
                double newGradientLowerBound = lowerBound - increment;              
                setLowerBound(newGradientLowerBound);  
            }
            else if(ratio>2/3.)
            {                                           
                double newGradientUpperBound =  upperBound + increment;                             
                setUpperBound(newGradientUpperBound);
            }
            else if(ratio >= 1/3. && ratio<=2/3.)
            {
                double newGradientLowerBound = lowerBound - increment;
                double newGradientUpperBound =  upperBound + increment;

                setGradientBounds(newGradientLowerBound, newGradientUpperBound);
            }       
        }

    }

    private void updatePaintScale()
    {
        //if the plot is not 3D, we don't need a paint scale
        if(!is3DPlot)
        {
            return;
        }

        Color underflow = useOutsideRangeColors ? gradientUnderflowColor : colorGradient.getColor(0);
        Color overflow = useOutsideRangeColors ? gradientOverflowColor : colorGradient.getColor(1);

        GradientPaintScale paintScale = new GradientPaintScale(lowerBound, upperBound, colorGradient, underflow, overflow);
        setPaintScale(paintScale);      	
    }

    public boolean isOutlinesVisible() 
    {
        return this.outlinesVisible;
    }

    public void setOutlinesVisible(boolean flag) 
    {
        this.outlinesVisible = flag;
        fireChangeEvent();
    }

    public Paint getOutlinePaint()
    {
        return outlinePaint;
    }

    @Override
    public void setOutlinePaint(Paint outlinePaint)
    {
        this.outlinePaint = outlinePaint;
        fireChangeEvent();
    }

    public Stroke getOutlineStroke()
    {
        return outlineStroke;
    }

    @Override
    public void setOutlineStroke(Stroke outlineStroke)
    {
        this.outlineStroke = outlineStroke;
        fireChangeEvent();
    }

    public boolean isGuidelinesVisible() 
    {
        return this.guidelinesVisible;
    }

    public void setGuidelinesVisible(boolean visible) 
    {
        this.guidelinesVisible = visible;
        fireChangeEvent();
    }

    public Paint getGuideLinePaint() 
    {
        return this.guidelinesPaint;
    }

    public void setGuideLinePaint(Paint paint) 
    {
        this.guidelinesPaint = Validation.requireNonNullParameterName(paint, "paint");
        fireChangeEvent();
    }

    public Stroke getGuideLineStroke() 
    {
        return this.guidelinesStroke;
    }

    public void setGuideLineStroke(Stroke stroke) 
    {
        this.guidelinesStroke = Validation.requireNonNullParameterName(stroke, "stroke");
        fireChangeEvent();
    }

    public boolean isLabelVisible()
    {
        return labelVisible;
    }

    public void setLabelVisible(boolean labelVisible)
    {
        this.labelVisible = labelVisible;       
        fireChangeEvent();
    }

    public Font getLabelFont()
    {
        return labelFont;
    }

    public void setLabelFont(Font labelFont)
    {
        this.labelFont = labelFont;
        fireChangeEvent();
    }

    public Paint getLabelPaint()
    {
        return labelPaint;
    }

    public void setLabelPaint(Paint labelPaint)
    {
        this.labelPaint = labelPaint;
        fireChangeEvent();
    }

    @Override
    public Range findDomainBounds(XYDataset dataset) 
    {
        Range paddedRange = null;
        if (dataset != null) 
        {
            Range dataRange = DatasetUtilities.findDomainBounds(dataset, false);
            if (dataRange != null & dataset instanceof ProcessableXYZDataset) 
            {
                //we get the values of xDataDensity and yDataDensity, because the values dtored in the renderer itself may be out of data
                //e.g. this is the case when the method findDomainBounds() is called by the plot in response to DatasetChangeEvent,
                //becauase plots are notified before renderers.
                ProcessableXYZDataset processableDataset = ((ProcessableXYZDataset)dataset);
                double xDataDensity = processableDataset.getXDataDensity();
                paddedRange = new Range(dataRange.getLowerBound() - xDataDensity/2, dataRange.getUpperBound() + xDataDensity/2);  
            }               
        }

        return paddedRange;
    }

    @Override
    public Range findRangeBounds(XYDataset dataset) 
    {
        Range paddedRange = null;
        if (dataset != null) 
        {
            Range dataRange = DatasetUtilities.findRangeBounds(dataset, false);
            if (dataRange != null && dataset instanceof ProcessableXYZDataset) 
            {
                ProcessableXYZDataset processableDataset = ((ProcessableXYZDataset)dataset);
                double yDataDensity = processableDataset.getYDataDensity();
                paddedRange = new Range(dataRange.getLowerBound() - yDataDensity/2, dataRange.getUpperBound() + yDataDensity/2);
            }
        }
        return paddedRange;
    }

    public Range findZBounds(XYZDataset dataset) 
    {
        Range range = null;
        if (dataset != null) 
        {
            range = DatasetUtilities.findZBounds(dataset);
        }
        return range;
    }

    @Override
    public int getPassCount() 
    {
        return 2;
    }

    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) 
    {
        Object datasetKey = dataset instanceof ProcessableXYZDataset ? ((ProcessableXYZDataset) dataset).getKey() : "";

        if(!getItemVisible(series, item)) 
        {
            return;   
        }

        byte seriesByte = (byte)series;

        double x = dataset.getXValue(series, item);
        double y = dataset.getYValue(series, item);

        if (Double.isNaN(x) || Double.isNaN(y)) 
        {
            return;
        }

        double xShapeSizeProper = (1. - shapeMargins)*xDataDensity;
        double yShapeSizeProper = (1. - shapeMargins)*yDataDensity;

        float trXCenter = (float) domainAxis.valueToJava2D(x, dataArea, plot.getDomainAxisEdge());
        float trYCenter = (float) rangeAxis.valueToJava2D(y, dataArea, plot.getRangeAxisEdge());
        float xLength = (float) domainAxis.lengthToJava2D(xShapeSizeProper, dataArea, plot.getDomainAxisEdge());
        float yLength = (float) rangeAxis.lengthToJava2D(yShapeSizeProper, dataArea, plot.getRangeAxisEdge());

        PlotOrientation orientation = plot.getOrientation();
        boolean isVertical = orientation.equals(PlotOrientation.VERTICAL);

        Shape shape;
        if(stretchShape)
        {
            if(isVertical)
            {
                shape = ShapeSupplier.createShape(shapeIndex, trXCenter, trYCenter, xLength, yLength);
            }
            else
            {
                shape = ShapeSupplier.createShape(shapeIndex, trYCenter, trXCenter, yLength, xLength);
            }
        }
        else
        {
            float shapeSize = Math.min(xLength, yLength);
            if(isVertical)
            {
                shape = ShapeSupplier.createShape(shapeIndex, trXCenter, trYCenter, shapeSize);
            }
            else
            {
                shape = ShapeSupplier.createShape(shapeIndex,trYCenter, trXCenter, shapeSize);
            }
        }

        EntityCollection entities = null;
        if (info != null) 
        {
            entities = info.getOwner().getEntityCollection();
        }

        if ((pass == 0) && this.guidelinesVisible) 
        {
            g2.setStroke(this.guidelinesStroke);
            g2.setPaint(this.guidelinesPaint);
            if (isVertical) 
            {
                g2.draw(new Line2D.Double(trXCenter, dataArea.getMinY(), trYCenter, dataArea.getMaxY()));
                g2.draw(new Line2D.Double(dataArea.getMinX(), trYCenter, dataArea.getMaxX(), trYCenter));
            }
            else 
            {               
                g2.draw(new Line2D.Double(trYCenter, dataArea.getMinY(), trYCenter, dataArea.getMaxY()));
                g2.draw(new Line2D.Double(dataArea.getMinX(), trXCenter, dataArea.getMaxX(), trXCenter));
            }
        }
        else if (pass == 1) 
        {
            if (shape.intersects(dataArea)) 
            {
                g2.setPaint(getPaint(dataset, series, item));
                g2.fill(shape);

                if (this.outlinesVisible) 
                {                
                    g2.setPaint(outlinePaint);               
                    g2.setStroke(outlineStroke);
                    g2.draw(shape);
                }
            }

            if (entities != null) 
            {
                String url = null;
                if (getURLGenerator() != null) {
                    url = getURLGenerator().generateURL(dataset, series, item);
                }

                LightweightXYItemEntity entity = new LightweightXYItemEntity(shape, datasetKey, seriesByte, item,
                        url);

                entities.add(entity);
            }
        }
        if (isItemLabelVisible(series, item))
        {
            drawItemLabel(g2, orientation, dataset, series, item, trXCenter, trYCenter, (trYCenter < 0.0));
        }
    }

    @Override
    public EntityCollection drawItems(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYZDataset dataset,
            int series,  CrosshairState crosshairState, int pass) 
    {
        EntityCollection entityList = new StandardEntityCollection();

        Object datasetKey = dataset instanceof ProcessableXYZDataset ? ((ProcessableXYZDataset) dataset).getKey() : "";


        if(!isSeriesVisible(series))
        {
            return entityList;
        }
        byte seriesByte = (byte)series;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float xShapeSizeProper = (float) ((1. - shapeMargins)*xDataDensity);
        float yShapeSizeProper = (float) ((1. - shapeMargins)*yDataDensity);

        float xLength = (float)domainAxis.lengthToJava2D(xShapeSizeProper, dataArea, plot.getDomainAxisEdge());
        float yLength = (float)rangeAxis.lengthToJava2D(yShapeSizeProper, dataArea, plot.getRangeAxisEdge());

        PlotOrientation orientation = plot.getOrientation();
        boolean isVertical = orientation.equals(PlotOrientation.VERTICAL);

        EntityCollection entities = null;
        if (info != null) 
        {
            entities = info.getOwner().getEntityCollection();
        }

        boolean drawGuidlines = (pass == 1) && guidelinesVisible;

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

        int i = 0;
        for (int item = firstItem; item <= lastItem; item++) 
        {
            items[i++] = item;

        }

        if(dataset instanceof Channel2DDataset && ((Channel2DDataset) dataset).getDisplayedChannel().getChannelData() instanceof GridChannel2DData && stretchShape && 
                shapeIndex == 0 && !drawGuidlines && !labelVisible && !outlinesVisible)
        {
            Channel2DDataset gridDataset =  ((Channel2DDataset) dataset);
            GridChannel2DData channelData = (GridChannel2DData)gridDataset.getDisplayedChannel().getChannelData();

            drawItemsFast(g2, state, dataArea, info, plot, domainAxis, rangeAxis, channelData, gridDataset.getKey(), crosshairState, pass, series, seriesByte,
                    firstItem, lastItem, items, xLength, yLength, entities, entityList);

            return entityList;
        }

        PrefixedUnit displayedUnit = getDisplayedUnit();
        PrefixedUnit dataUnit = getDataUnit();

        double unitConversionFactor = (displayedUnit != null && dataUnit != null) ? dataUnit.getConversionFactorTo(displayedUnit) : 1;

        if(isNothingMasked())
        {

            for(int item: items)
            {
                if(!getItemVisible(series, item)) 
                {
                    break;   
                }

                double x = dataset.getXValue(series, item);
                double y = dataset.getYValue(series, item);
                double z = dataset.getZValue(series, item);

                if (Double.isNaN(x) || Double.isNaN(y)) 
                {
                    break;
                }

                float trXCenter = (float) domainAxis.valueToJava2D(x, dataArea, plot.getDomainAxisEdge());
                float trYCenter = (float) rangeAxis.valueToJava2D(y, dataArea, plot.getRangeAxisEdge());

                Shape shape;
                if(stretchShape)
                {
                    shape = isVertical ? ShapeSupplier.createIntrShape(shapeIndex, trXCenter, trYCenter, xLength, yLength)
                            : ShapeSupplier.createIntrShape(shapeIndex, trYCenter, trXCenter, yLength, xLength);
                }
                else
                {
                    float shapeSize = Math.min(xLength, yLength);
                    shape = isVertical ? ShapeSupplier.createShape(shapeIndex, trXCenter, trYCenter, shapeSize) 
                            : ShapeSupplier.createShape(shapeIndex, trYCenter, trXCenter, shapeSize);
                }

                if (pass == 1) 
                {
                    if (shape.intersects(dataArea)) 
                    {
                        g2.setPaint(paintScale.getPaint(z));
                        g2.fill(shape);

                        if (outlinesVisible) 
                        {                
                            g2.setPaint(outlinePaint);               
                            g2.setStroke(outlineStroke);
                            g2.draw(shape);
                        }

                        if (entities != null) 
                        {
                            String url = null;
                            if (getURLGenerator() != null) {
                                url = getURLGenerator().generateURL(dataset, series, item);
                            }
                            LightweightXYItemEntity entity = new LightweightXYItemEntity(shape, datasetKey, seriesByte, item, url);

                            entities.add(entity);
                            entityList.add(entity);
                        }
                    }          
                }

                if (drawGuidlines) 
                {
                    g2.setStroke(guidelinesStroke);
                    g2.setPaint(guidelinesPaint);
                    if (isVertical) 
                    {
                        g2.draw(new Line2D.Float(trXCenter, (float)dataArea.getMinY(), trXCenter,  (float)dataArea.getMaxY()));
                        g2.draw(new Line2D.Float((float)dataArea.getMinX(), trYCenter, (float)dataArea.getMaxX(), trYCenter));
                    }
                    else 
                    {               
                        g2.draw(new Line2D.Float(trYCenter, (float)dataArea.getMinY(), trYCenter, (float)dataArea.getMaxY()));
                        g2.draw(new Line2D.Float((float)dataArea.getMinX(), trXCenter, (float)dataArea.getMaxX(), trXCenter));
                    }
                }
                if (labelVisible)
                {
                    drawLabel(g2, orientation, series, item, trXCenter, trYCenter, unitConversionFactor*z, (trYCenter < 0.0));
                }

                state.endSeriesPass(dataset, series, firstItem,
                        lastItem, pass, getPassCount());
            }
        }
        else if(isROIOutsideMasked())
        {
            for(int item: items)
            {
                if(!getItemVisible(series, item)) 
                {
                    break;   
                }

                double x = dataset.getXValue(series, item);
                double y = dataset.getYValue(series, item);
                double z = dataset.getZValue(series, item);

                if (Double.isNaN(x) || Double.isNaN(y)) 
                {
                    break;
                }


                boolean notMasked = mask.contains(new Point2D.Double(x,y));

                float trXCenter = (float) domainAxis.valueToJava2D(x, dataArea, plot.getDomainAxisEdge());
                float trYCenter = (float) rangeAxis.valueToJava2D(y, dataArea, plot.getRangeAxisEdge());

                Shape shape;
                if(stretchShape)
                {                 
                    shape = isVertical ? ShapeSupplier.createIntrShape(shapeIndex, trXCenter, trYCenter, xLength, yLength) 
                            : ShapeSupplier.createIntrShape(shapeIndex, trYCenter, trXCenter, yLength, xLength);                   
                }
                else
                {
                    float shapeSize = Math.min(xLength, yLength);

                    shape = isVertical ? ShapeSupplier.createShape(shapeIndex, trXCenter, trYCenter, shapeSize) 
                            :ShapeSupplier.createShape(shapeIndex, trYCenter, trXCenter, shapeSize);

                }

                if (pass == 1) 
                {
                    if (shape.intersects(dataArea)) 
                    {
                        Paint paint = notMasked ? paintScale.getPaint(z) : maskColor;
                        g2.setPaint(paint);
                        g2.fill(shape);

                        if (outlinesVisible) 
                        {                
                            g2.setPaint(outlinePaint);               
                            g2.setStroke(outlineStroke);
                            g2.draw(shape);
                        }

                        if (entities != null) 
                        {
                            String url = null;
                            if (getURLGenerator() != null) {
                                url = getURLGenerator().generateURL(dataset, series, item);
                            }
                            LightweightXYItemEntity entity = 
                                    new LightweightXYItemEntity(shape, datasetKey, seriesByte, item,
                                            url);

                            entities.add(entity);
                            entityList.add(entity);
                        }
                    }          
                }

                if (drawGuidlines) 
                {
                    g2.setStroke(guidelinesStroke);
                    g2.setPaint(guidelinesPaint);
                    if (isVertical) 
                    {
                        g2.draw(new Line2D.Float(trXCenter, (float)dataArea.getMinY(), trXCenter,  (float)dataArea.getMaxY()));
                        g2.draw(new Line2D.Float((float)dataArea.getMinX(), trYCenter, (float)dataArea.getMaxX(), trYCenter));
                    }
                    else 
                    {               
                        g2.draw(new Line2D.Float(trYCenter, (float)dataArea.getMinY(), trYCenter, (float)dataArea.getMaxY()));
                        g2.draw(new Line2D.Float((float)dataArea.getMinX(), trXCenter, (float)dataArea.getMaxX(), trXCenter));
                    }
                }
                if (labelVisible && notMasked)
                {
                    drawLabel(g2, orientation, series, item, trXCenter, trYCenter, unitConversionFactor*z, (trYCenter < 0.0));
                }

                state.endSeriesPass(dataset, series, firstItem,
                        lastItem, pass, getPassCount());
            }

        }
        else if(isROIInsideMasked())
        {
            for(int item: items)
            {
                if(!getItemVisible(series, item)) 
                {
                    break;   
                }

                double x = dataset.getXValue(series, item);
                double y = dataset.getYValue(series, item);
                double z = dataset.getZValue(series, item);

                if (Double.isNaN(x) || Double.isNaN(y)) 
                {
                    break;
                }


                boolean notMasked = !mask.contains(new Point2D.Double(x,y));

                float trXCenter = (float) domainAxis.valueToJava2D(x, dataArea, plot.getDomainAxisEdge());
                float trYCenter = (float) rangeAxis.valueToJava2D(y, dataArea, plot.getRangeAxisEdge());

                Shape shape;
                if(stretchShape)
                {
                    shape = isVertical ? ShapeSupplier.createIntrShape(shapeIndex, trXCenter, trYCenter, xLength, yLength) 
                            :ShapeSupplier.createIntrShape(shapeIndex, trYCenter, trXCenter, yLength, xLength);
                }
                else
                {
                    float shapeSize = Math.min(xLength, yLength);
                    shape = isVertical ? ShapeSupplier.createShape(shapeIndex, trXCenter, trYCenter, shapeSize) 
                            :ShapeSupplier.createShape(shapeIndex,trYCenter, trXCenter, shapeSize);
                }

                if (pass == 1) 
                {
                    if (shape.intersects(dataArea)) 
                    {
                        Paint paint = notMasked ? paintScale.getPaint(z) : maskColor;
                        g2.setPaint(paint);
                        g2.fill(shape);

                        if (outlinesVisible) 
                        {                
                            g2.setPaint(outlinePaint);               
                            g2.setStroke(outlineStroke);
                            g2.draw(shape);
                        }

                        if (entities != null) 
                        {
                            String url = null;
                            if (getURLGenerator() != null) {
                                url = getURLGenerator().generateURL(dataset, series, item);
                            }
                            LightweightXYItemEntity entity = new LightweightXYItemEntity(shape, dataset, seriesByte, item, url);

                            entities.add(entity);
                            entityList.add(entity);
                        }
                    }          
                }

                if (drawGuidlines) 
                {
                    g2.setStroke(guidelinesStroke);
                    g2.setPaint(guidelinesPaint);
                    if (isVertical) 
                    {
                        g2.draw(new Line2D.Float(trXCenter, (float)dataArea.getMinY(), trXCenter,  (float)dataArea.getMaxY()));
                        g2.draw(new Line2D.Float((float)dataArea.getMinX(), trYCenter, (float)dataArea.getMaxX(), trYCenter));
                    }
                    else 
                    {               
                        g2.draw(new Line2D.Float(trYCenter, (float)dataArea.getMinY(), trYCenter, (float)dataArea.getMaxY()));
                        g2.draw(new Line2D.Float((float)dataArea.getMinX(), trXCenter, (float)dataArea.getMaxX(), trXCenter));
                    }
                }
                if (labelVisible && notMasked)
                {
                    drawLabel(g2, orientation, series, item, trXCenter, trYCenter, unitConversionFactor*z, (trYCenter < 0.0));
                }

                state.endSeriesPass(dataset, series, firstItem,
                        lastItem, pass, getPassCount());
            }
        }

        return entityList;
    }

    public void drawItemsFast(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, 
            final GridChannel2DData channel, Object datasetKey, CrosshairState crosshairState, 
            int pass, int series, byte seriesByte, int firstItem, int lastItem, int[] items, float xLength, float yLength,
            EntityCollection entities,  EntityCollection entityList) 
    {                 
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
        if(!(minColumn > - 1 && maxColumn >-1 && minRow> - 1 && maxRow >-1))
        {       
            return;
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
                GridBiPositionCalculator calculator = new GridBiPositionCalculator(25, 25);
                calculator.dividePoints(grid, mask, minRow, maxRow, minColumn, maxColumn, new GridBiPointRecepient() 
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
                GridBiPositionCalculator calculator = new GridBiPositionCalculator(25, 25);

                calculator.dividePoints(grid, mask, minRow, maxRow, minColumn, maxColumn, new GridBiPointRecepient() 
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

                        buffer.setElem(index,color);
                    }
                });

            }
            WritableRaster raster = Raster.createPackedRaster(buffer, imageWidth, imageHeight, imageWidth, new int[] {0x00ff0000,0x0000ff00, 0x000000ff, 0xff000000}, null);                
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
                GridBiPositionCalculator calculator = new GridBiPositionCalculator(25, 25);
                calculator.dividePoints(grid, mask, minRow, maxRow, minColumn, maxColumn, new GridBiPointRecepient() 
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
                GridBiPositionCalculator calculator = new GridBiPositionCalculator(25, 25);
                calculator.dividePoints(grid, mask, minRow, maxRow, minColumn, maxColumn, new GridBiPointRecepient() 
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


        if(entities == null || pass != 1)
        {
            return;
        }
        for(int item: items)
        {           
            double x = dataset.getXValue(series, item);
            double y = dataset.getYValue(series, item);

            if (Double.isNaN(x) || Double.isNaN(y)) 
            {
                continue;
            }

            float trXCenter = (float) domainAxis.valueToJava2D(x, dataArea, plot.getDomainAxisEdge());
            float trYCenter = (float) rangeAxis.valueToJava2D(y, dataArea, plot.getRangeAxisEdge());

            float shapeSize = Math.min(xLength, yLength);

            Shape shape = isVertical ? ShapeSupplier.createShape(shapeIndex, trXCenter, trYCenter, shapeSize) :
                ShapeSupplier.createShape(shapeIndex,trYCenter, trXCenter, shapeSize);

            if (shape.intersects(dataArea)) 
            {  
                String url = getURLGenerator() != null ? getURLGenerator().generateURL(dataset, series, item): null;

                LightweightXYItemEntity entity = new LightweightXYItemEntity(shape, datasetKey, seriesByte, item,
                        url);

                entities.add(entity);
                entityList.add(entity);
            }  
            state.endSeriesPass(dataset, series, firstItem, lastItem, pass, getPassCount());
        }
    }  

    //copied
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

    //copied
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

    protected Paint getPaint(XYDataset dataset, int series, int item) 
    {
        if (dataset instanceof XYZDataset) 
        {
            double z = ((XYZDataset) dataset).getZValue(series, item);
            return this.paintScale.getPaint(z);
        }
        else
        {
            return Color.black;
        }
    }  

    @Override
    public boolean isTickLabelTrailingZeroes()
    {
        return showTrailingZeroes;
    }

    @Override
    public void setTickLabelShowTrailingZeroes(boolean trailingZeroes)
    {    	
        this.showTrailingZeroes = trailingZeroes;
        int minDigits = trailingZeroes ? format.getMaximumFractionDigits() : 0;
        format.setMinimumFractionDigits(minDigits);	
        fireChangeEvent();
    }

    @Override
    public boolean isTickLabelGroupingUsed()
    {
        return format.isGroupingUsed();
    }

    @Override
    public void setTickLabelGroupingUsed(boolean used)
    {
        format.setGroupingUsed(used);
        fireChangeEvent();
    }

    @Override
    public char getTickLabelGroupingSeparator()
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        return symbols.getGroupingSeparator();
    }

    @Override
    public void setTickLabelGroupingSeparator(char separator)
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(separator);
        format.setDecimalFormatSymbols(symbols);  
        fireChangeEvent();
    }

    @Override
    public char getTickLabelDecimalSeparator()
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        return symbols.getDecimalSeparator();
    }

    @Override
    public void setTickLabelDecimalSeparator(char separator)
    {   	
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        symbols.setDecimalSeparator(separator);
        format.setDecimalFormatSymbols(symbols);
        fireChangeEvent();
    }

    public int getMaximumFractionDigits()
    {
        return format.getMaximumFractionDigits();
    }

    public void setMaximumFractionDigits(int n)
    {
        format.setMaximumFractionDigits(n);
        if(showTrailingZeroes)
        {
            format.setMinimumFractionDigits(n);
        }    
        fireChangeEvent();
    }

    protected void drawLabel(Graphics2D g2, PlotOrientation orientation, int series, int item, double x, double y, double z, boolean negative)
    {        
        g2.setFont(labelFont);
        g2.setPaint(labelPaint);
        String label = format.format(z);

        // get the label position..
        ItemLabelPosition position = null;
        if (!negative) {
            position = getPositiveItemLabelPosition(series, item);
        }
        else {
            position = getNegativeItemLabelPosition(series, item);
        }

        // work out the label anchor point...
        Point2D anchorPoint = calculateLabelAnchorPoint(
                position.getItemLabelAnchor(), x, y, orientation);
        TextUtilities.drawRotatedString(label, g2,(float) anchorPoint.getX(), (float) anchorPoint.getY(),position.getTextAnchor(), position.getAngle(),
                position.getRotationAnchor());
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
    public CustomizableXYShapeRenderer clone()
    {
        try 
        {
            CustomizableXYShapeRenderer clone = (CustomizableXYShapeRenderer)super.clone();

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
}