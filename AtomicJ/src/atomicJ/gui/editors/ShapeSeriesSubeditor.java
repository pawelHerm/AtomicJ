
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

package atomicJ.gui.editors;

import static atomicJ.gui.PreferenceKeys.*;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.jfree.ui.PaintSample;

import atomicJ.data.QuantitativeSample;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.BasicStrokeReceiver;
import atomicJ.gui.ColorGradient;
import atomicJ.gui.ColorSupplier;
import atomicJ.gui.CustomizableXYShapeRenderer;
import atomicJ.gui.FontDisplayField;
import atomicJ.gui.FontReceiver;
import atomicJ.gui.GradientColorsBuiltIn;
import atomicJ.gui.GradientMaskSelector;
import atomicJ.gui.GradientPaint;
import atomicJ.gui.GradientPaintReceiver;
import atomicJ.gui.GradientRangeSelector;
import atomicJ.gui.PlotStyleUtilities;
import atomicJ.gui.PreferredContinuousSeriesRendererStyle;
import atomicJ.gui.RangeGradientChooser;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.StyleTag;
import atomicJ.gui.SubPanel;
import atomicJ.gui.rois.ROI;
import atomicJ.utilities.SerializationUtilities;


public class ShapeSeriesSubeditor extends SubPanel implements SeriesSubeditor, GradientPaintReceiver, ActionListener, ChangeListener, ItemListener
{
    private static final long serialVersionUID = 1L;

    private final static String VALUE_LABELS_FONT_COMMAND = "VALUE_LABEL_FONT_COMMAND";
    private final static String VALUE_LABELS_PAINT_COMMAND = "VALUE_LABELS_PAINT_COMMAND";

    private final static String EDIT_OUTLINE_STROKE_COMMAND = "EDIT_OUTLINE_STROKE_COMMAND";
    private final static String EDIT_GUIDELINE_STROKE_COMMAND = "EDIT_GUIDELINE_STROKE_COMMAND";

    private final static String SELECT_COLOR_GRADIENT_COMMAND = "SELECT_COLOR_GRADIENT_COMMAND";
    private final static String SELECT_SHAPE_COMMAND = "SELECT_SHAPE_COMMAND";

    private static final Shape[] SHAPES = PlotStyleUtilities.getNonZeroAreaShapes();	

    private final ColorGradient initLUTTable;
    private final Color initGradientUnderflowColor;
    private final Color initGradientOverflowColor;
    private final boolean initOutsideRangeColors;
    private final Color initMaskColor;

    private final double initLowerGradientBound;
    private final double initUpperGradientBound;

    private final GradientMaskSelector initGradientMaskSelector;
    private final GradientRangeSelector initGradientRangeSelector;
    private final int initShapeIndex;
    private final boolean initMarkerStretched;
    private final Font initValueLabelFont;
    private final Paint initValueLabelPaint;

    private final boolean initOutlineVisible;
    private final Paint initOutlinePaint;
    private final Stroke initOutlineStroke;

    private final boolean initSeriesVisible;

    private final boolean initGuidelinesVisible;
    private final Paint initGuidelinesPaint;
    private final Stroke initGuidelinesStroke;

    private final boolean initValueLabelsVisible;
    private final double initMargin;

    private final boolean initTickLabelGroupingUsed;
    private final char initTickLabelGroupingSeparator;
    private final char initTickLabelDecimalSeparator;
    private final boolean initTickLabelTrailingZeroes;
    private final int initMaxFractionDigits;

    private ColorGradient colorGradient;
    private Color gradientUnderflowColor;
    private Color gradientOverflowColor;
    private boolean useOutsideRangeColors;
    private Color maskColor;
    private double lowerGradientBound;
    private double upperGradientBound;
    private GradientMaskSelector gradientMaskSelector;
    private GradientRangeSelector gradientRangeSelector;

    private boolean seriesVisible;
    private boolean guidelinesVisible;
    private Stroke guidelinesStroke;
    private Paint guidelinesPaint;

    private double margin;
    private int shapeIndex;
    private boolean markerStretched;
    private boolean valueLabelsVisible;
    private Font valueLabelFont;
    private Paint valueLabelPaint;

    private boolean outlineVisible;
    private Stroke outlineStroke;
    private Paint outlinePaint;

    private final double lowerFullGradientBound;
    private final double upperFullGradientBound;

    private final double lowerAutomaticGradientBound;
    private final double upperAutomaticGradientBound;

    private final PrefixedUnit dataUnit;
    private final PrefixedUnit displayedUnit;

    private boolean tickLabelGroupingUsed;
    private char tickLabelGroupingSeparator;
    private char tickLabelDecimalSeparator;
    private boolean tickLabelTrailingZeroes;
    private int maxFractionDigits;

    private final FontDisplayField fieldLabelFont;

    private final PaintSample labelPaintSample;
    private final PaintSample gradientSample;	

    private final JButton selectGradientButton = new JButton("Select");	
    private final JButton selectShapeButton = new JButton("Select");
    private final JButton buttonSelectLabelPaint = new JButton("Select");
    private final JButton buttonSelectLabelFont = new JButton("Select");

    private final JButton buttonEditGuidelines = new JButton("Edit");
    private final JButton buttonEditOutline = new JButton("Edit");

    private final JLabel labelShapeIcon;		

    private final JCheckBox boxSeriesVisible = new JCheckBox("Show");
    private final JCheckBox boxOutlineVisible = new JCheckBox();
    private final JCheckBox boxGuidelinesVisible = new JCheckBox();
    private final JCheckBox boxStretchMarker = new JCheckBox("Stretch");
    private final JCheckBox boxValueLabelsVisible = new JCheckBox();

    //format panel
    private final JComboBox<Character> comboDecimalSeparator = new JComboBox<>(new Character[] {'.',','});
    private final JComboBox<Character> comboGroupingSeparator = new JComboBox<>(new Character[] {' ',',','.','\''});
    private final JCheckBox boxTrailingZeroes = new JCheckBox();
    private final JCheckBox boxUseThousandGrouping = new JCheckBox("Use separator");
    private final JSpinner spinnerFractionDigits = new JSpinner(new SpinnerNumberModel(1,0,Integer.MAX_VALUE,1));

    private final StraightStrokeSample guidelineStrokeSample = new StraightStrokeSample();
    private final StraightStrokeSample outlineStrokeSample = new StraightStrokeSample();

    private final JSpinner spinnerMargins;

    private StrokeChooser guidelineStrokeChooser;
    private StrokeChooser outlineStrokeChooser;
    private final ShapeChooser shapeChooser;
    private RangeGradientChooser gradientChooser;
    private final JTabbedPane tabbedPane;

    private final Preferences pref;       
    private final String seriesName;

    private FontChooserDialog fontChooserDialog;

    private final CustomizableXYShapeRenderer renderer;
    private final List<CustomizableXYShapeRenderer> boundededRenderers;

    public ShapeSeriesSubeditor(final CustomizableXYShapeRenderer renderer, List<CustomizableXYShapeRenderer> boundedRenderers)
    {
        this.renderer = renderer;
        this.boundededRenderers = boundedRenderers;

        this.initLUTTable = renderer.getColorGradient();
        this.initGradientUnderflowColor = renderer.getGradientUnderflowColor();
        this.initGradientOverflowColor = renderer.getGradientOverflowColor();
        this.initOutsideRangeColors = renderer.getUseOutsideRangeColors();

        this.initMaskColor = renderer.getMaskColor();

        this.initLowerGradientBound = renderer.getLowerBound();
        this.initUpperGradientBound = renderer.getUpperBound();
        this.initGradientMaskSelector = renderer.getGradientMaskSelector();
        this.initGradientRangeSelector = renderer.getGradientRangeSelector();
        this.lowerFullGradientBound = renderer.getLowerFullBound();
        this.upperFullGradientBound = renderer.getUpperFullBound();


        this.lowerAutomaticGradientBound = renderer.getLowerAutomaticBound();
        this.upperAutomaticGradientBound = renderer.getUpperAutomaticBound();

        this.dataUnit = renderer.getDataUnit();
        this.displayedUnit = renderer.getDisplayedUnit();

        this.initShapeIndex = renderer.getShapeIndex();
        this.initMarkerStretched = renderer.isShapeStretched();

        this.initSeriesVisible = renderer.getBaseSeriesVisible();
        this.initGuidelinesVisible = renderer.isGuidelinesVisible();
        this.initGuidelinesPaint = renderer.getGuideLinePaint();
        this.initGuidelinesStroke = renderer.getGuideLineStroke();

        this.initOutlineVisible = renderer.isOutlinesVisible();
        this.initOutlinePaint = renderer.getOutlinePaint();
        this.initOutlineStroke = renderer.getOutlineStroke();

        this.initMargin = renderer.getShapeMargins();

        this.initValueLabelsVisible = renderer.isLabelVisible();		
        this.initValueLabelFont = renderer.getLabelFont();
        this.initValueLabelPaint = renderer.getLabelPaint();

        this.initTickLabelGroupingUsed = renderer.isTickLabelGroupingUsed();
        this.initTickLabelGroupingSeparator = renderer.getTickLabelGroupingSeparator();
        this.initTickLabelDecimalSeparator = renderer.getTickLabelDecimalSeparator();
        this.initTickLabelTrailingZeroes = renderer.isTickLabelTrailingZeroes();
        this.initMaxFractionDigits = renderer.getMaximumFractionDigits();

        setParametersToInitial();

        this.seriesName = renderer.getName();	
        this.pref = renderer.getPreferences();

        this.shapeChooser = new ShapeChooser(this);

        comboGroupingSeparator.setEnabled(initTickLabelGroupingUsed);
        boxUseThousandGrouping.setSelected(initTickLabelGroupingUsed);
        boxTrailingZeroes.setSelected(initTickLabelTrailingZeroes);
        spinnerFractionDigits.setValue(initMaxFractionDigits);

        boxSeriesVisible.setSelected(initSeriesVisible);		
        boxGuidelinesVisible.setSelected(initGuidelinesVisible);	
        boxOutlineVisible.setSelected(initOutlineVisible);
        boxValueLabelsVisible.setSelected(initValueLabelsVisible);
        boxStretchMarker.setSelected(initMarkerStretched);

        this.fieldLabelFont = new FontDisplayField(initValueLabelFont);
        this.labelPaintSample = new PaintSample(initValueLabelPaint);

        guidelineStrokeSample.setStroke(initGuidelinesStroke);
        outlineStrokeSample.setStrokePaint(initGuidelinesPaint);

        outlineStrokeSample.setStroke(initOutlineStroke);
        outlineStrokeSample.setStrokePaint(initOutlinePaint);

        buttonEditGuidelines.setActionCommand(EDIT_GUIDELINE_STROKE_COMMAND);
        buttonEditGuidelines.addActionListener(this);

        buttonEditOutline.setActionCommand(EDIT_OUTLINE_STROKE_COMMAND);
        buttonEditOutline.addActionListener(this);

        buttonSelectLabelPaint.setActionCommand(VALUE_LABELS_PAINT_COMMAND);
        buttonSelectLabelPaint.addActionListener(this);

        buttonSelectLabelFont.setActionCommand(VALUE_LABELS_FONT_COMMAND);
        buttonSelectLabelFont.addActionListener(this);		

        Paint gradientPaint = new GradientPaint(colorGradient);
        gradientSample = new PaintSample(gradientPaint);

        selectGradientButton.setActionCommand(SELECT_COLOR_GRADIENT_COMMAND);
        selectGradientButton.addActionListener(this);

        selectShapeButton.setActionCommand(SELECT_SHAPE_COMMAND);
        selectShapeButton.addActionListener(this);

        spinnerMargins = new JSpinner(new SpinnerNumberModel(100*margin,-200,100,1));
        spinnerMargins.addChangeListener(this);

        labelShapeIcon = buildShapeLabel();
        //        shapeLabel.setBorder(BorderFactory.createLineBorder(Color.black));        

        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), seriesName)));

        //label panel

        JPanel mainPanel = buildMainPanel();
        tabbedPane.add("General", mainPanel);

        JPanel labelPanel = buildFormatPanel();
        tabbedPane.add("Label",labelPanel);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.NORTH);

        initChangeListener();
        initItemListener();
    }

    @Override
    public void setNameBorder(boolean b)
    {
        if(b)
        {
            tabbedPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5), BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), seriesName)));

        }
        else
        {
            tabbedPane.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));  
        }
    }

    private void initChangeListener()
    {
        spinnerFractionDigits.addChangeListener(this);
    }

    private void initItemListener()
    {
        comboDecimalSeparator.addItemListener(this);
        comboGroupingSeparator.addItemListener(this);
        boxUseThousandGrouping.addItemListener(this);
        boxTrailingZeroes.addItemListener(this);       

        boxSeriesVisible.addItemListener(this);
        boxGuidelinesVisible.addItemListener(this);
        boxOutlineVisible.addItemListener(this);
        boxStretchMarker.addItemListener(this);
        boxValueLabelsVisible.addItemListener(this);
    }

    @Override
    public String getSubeditorName()
    {
        return seriesName;
    }

    @Override
    public void resetToDefaults() 
    {
        StyleTag style = renderer.getStyleKey();
        ColorSupplier supplier = renderer.getSupplier();
        ColorGradient defaultLUTTable = GradientColorsBuiltIn.getGradients().get("Golden");
        Paint defaultUnderflowColor = supplier.getGradientUnderflow(style);
        Paint defaultOverflowColor = supplier.getGradientOverflow(style);
        Paint defaultOutlinePaint = Color.black;

        Paint defaultGuidelinePaint = Color.black;

        guidelinesVisible = pref.getBoolean(MAP_GUIDELINES_VISIBLE, false);
        guidelinesPaint = (Paint)SerializationUtilities.getSerializableObject(pref, MAP_GUIDELINES_PAINT, defaultGuidelinePaint);
        guidelinesStroke = SerializationUtilities.getStroke(pref, MAP_GUIDELINES_STROKE, new BasicStroke(1.f));

        outlineVisible = pref.getBoolean(MAP_OUTLINES_VISIBLE, false);
        outlineStroke = SerializationUtilities.getStroke(pref, MAP_OUTLINES_STROKE, new BasicStroke(1.0f));
        outlinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, MAP_OUTLINES_PAINT, defaultOutlinePaint);

        markerStretched = pref.getBoolean(MAP_MARKERS_STRETCHED, true);
        margin = pref.getDouble(MAP_MARKER_MARGIN, 0);
        shapeIndex = pref.getInt(PreferredContinuousSeriesRendererStyle.SHAPE_INDEX, 0);
        valueLabelsVisible = pref.getBoolean(MAP_VALUE_LABELS_VISIBLE, true);

        colorGradient = (ColorGradient)SerializationUtilities.getSerializableObject(pref, RANGE_COLOR_GRADIENT, defaultLUTTable);
        gradientUnderflowColor = (Color)SerializationUtilities.getSerializableObject(pref, PAINT_GRADIENT_UNDERFLOW, defaultUnderflowColor);
        gradientOverflowColor = (Color)SerializationUtilities.getSerializableObject(pref, PAINT_GRADIENT_OVERFLOW, defaultOverflowColor);				

        maskColor = (Color)SerializationUtilities.getSerializableObject(pref, MASK_COLOR, Color.black);				

        valueLabelFont = (Font)SerializationUtilities.getSerializableObject(pref, MAP_VALUE_LABEL_FONT, new Font("Dialog", Font.BOLD, 34));
        valueLabelPaint = (Paint)SerializationUtilities.getSerializableObject(pref, MAP_VALUE_LABEL_PAINT, Color.black);

        tickLabelGroupingUsed = pref.getBoolean(MAP_LABEL_GROUPING_USED, false);		
        tickLabelTrailingZeroes = pref.getBoolean(MAP_LABEL_TRAILING_ZEROES, true);
        tickLabelGroupingSeparator = (char) pref.getInt(MAP_LABEL_GROUPING_SEPARATOR, ' ');				
        tickLabelDecimalSeparator = (char) pref.getInt(MAP_LABEL_DECIMAL_SEPARATOR, '.');	

        resetRenderer(renderer);		
        resetEditor();
        ensureGradinetChooserConsistencyWithReceiver();
    }

    @Override
    public void saveAsDefaults() 
    {
        pref.putBoolean(MAP_OUTLINES_VISIBLE, outlineVisible);
        pref.putBoolean(MAP_GUIDELINES_VISIBLE, guidelinesVisible);
        pref.putBoolean(MAP_MARKERS_STRETCHED, markerStretched);

        pref.putDouble(MAP_MARKER_MARGIN, margin);
        pref.putInt(PreferredContinuousSeriesRendererStyle.SHAPE_INDEX, shapeIndex);
        pref.putBoolean(MAP_VALUE_LABELS_VISIBLE, valueLabelsVisible);

        try 
        {
            SerializationUtilities.putSerializableObject(pref, RANGE_COLOR_GRADIENT, colorGradient);
            SerializationUtilities.putSerializableObject(pref, PAINT_GRADIENT_UNDERFLOW, gradientUnderflowColor);
            SerializationUtilities.putSerializableObject(pref, PAINT_GRADIENT_OVERFLOW, gradientOverflowColor);

            SerializationUtilities.putSerializableObject(pref, MASK_COLOR, maskColor);

            SerializationUtilities.putSerializableObject(pref, MAP_VALUE_LABEL_FONT, valueLabelFont);
            SerializationUtilities.putSerializableObject(pref, MAP_VALUE_LABEL_PAINT, valueLabelPaint);

            SerializationUtilities.putSerializableObject(pref, MAP_GUIDELINES_PAINT, guidelinesPaint);
            SerializationUtilities.putStroke(pref, MAP_GUIDELINES_STROKE, guidelinesStroke);


            SerializationUtilities.putSerializableObject(pref, MAP_OUTLINES_PAINT, outlinePaint);
            SerializationUtilities.putStroke(pref, MAP_OUTLINES_STROKE, outlineStroke);
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }

        pref.putBoolean(MAP_LABEL_GROUPING_USED, tickLabelGroupingUsed);		
        pref.putInt(MAP_LABEL_GROUPING_SEPARATOR, tickLabelGroupingSeparator);				
        pref.putInt(MAP_LABEL_DECIMAL_SEPARATOR, tickLabelDecimalSeparator);				
        pref.putBoolean(MAP_LABEL_TRAILING_ZEROES, tickLabelTrailingZeroes);

        try 
        {
            pref.flush();
        } 
        catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    @Override
    public void applyChangesToAll() 
    {
        for(CustomizableXYShapeRenderer r : boundededRenderers)
        {
            resetRenderer(r);
        }
    }

    private void setParametersToInitial()
    {
        this.seriesVisible = initSeriesVisible;

        this.colorGradient = initLUTTable;
        this.gradientUnderflowColor = initGradientUnderflowColor;
        this.gradientOverflowColor = initGradientOverflowColor;
        this.maskColor = initMaskColor;
        this.lowerGradientBound = initLowerGradientBound;
        this.upperGradientBound = initUpperGradientBound;
        this.gradientMaskSelector = initGradientMaskSelector;
        this.gradientRangeSelector = initGradientRangeSelector;
        this.useOutsideRangeColors = initOutsideRangeColors;

        this.outlineVisible = initOutlineVisible;
        this.outlinePaint = initOutlinePaint;
        this.outlineStroke = initOutlineStroke;

        this.guidelinesVisible = initGuidelinesVisible;
        this.guidelinesPaint = initGuidelinesPaint;
        this.guidelinesStroke = initGuidelinesStroke;

        this.margin = initMargin;
        this.shapeIndex = initShapeIndex;
        this.markerStretched = initMarkerStretched;
        this.valueLabelsVisible = initValueLabelsVisible;
        this.valueLabelFont = initValueLabelFont;
        this.valueLabelPaint = initValueLabelPaint;

        this.tickLabelGroupingUsed = initTickLabelGroupingUsed;
        this.tickLabelGroupingSeparator = initTickLabelGroupingSeparator;
        this.tickLabelDecimalSeparator = initTickLabelDecimalSeparator;
        this.tickLabelTrailingZeroes = initTickLabelTrailingZeroes;
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();
        resetRenderer(renderer);
        resetEditor();
        ensureGradinetChooserConsistencyWithReceiver();
    }

    @Override
    public Component getEditionComponent() 
    {
        return this;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return boundededRenderers.size()>1;
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();

        if(command.equals(VALUE_LABELS_FONT_COMMAND))
        {
            attemptFontSelection();
        }
        else if(command.equals(VALUE_LABELS_PAINT_COMMAND))
        {
            attemptLabelPaintSelection();
        }
        else if(command.equals(EDIT_GUIDELINE_STROKE_COMMAND))
        {
            attemptGuidelinesStrokeSelection();
        }
        else if(command.equals(EDIT_OUTLINE_STROKE_COMMAND))
        {
            attemptOutlineStrokeSelection();
        }
        else if(command.equals(SELECT_SHAPE_COMMAND))
        {
            attemptShapeSelection();
        }
        else if(command.equals(SELECT_COLOR_GRADIENT_COMMAND))
        {
            if(gradientChooser == null)
            {
                gradientChooser = new RangeGradientChooser(SwingUtilities.getWindowAncestor(this), this);
                gradientChooser.setVisible(true);
            }
            else
            {
                gradientChooser.showDialog(this);
            }
        }
    }	

    private void attemptShapeSelection() 
    {
        int result = JOptionPane.showConfirmDialog(this, shapeChooser, "Shape properties", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) 
        {
            this.shapeIndex = shapeChooser.getSelectedMarkerIndex();
            updateShapeLabel();
            renderer.setShapeIndex(shapeIndex);
        }
    }

    private void attemptFontSelection() 
    {
        if( fontChooserDialog == null)
        {
            this.fontChooserDialog = new FontChooserDialog(SwingUtilities.getWindowAncestor(this), "Font selection");
        }

        this.fontChooserDialog.showDialog(new FontReceiver() 
        {

            @Override
            public void setFont(Font newFont) 
            { 
                valueLabelFont = newFont;
                fieldLabelFont.setDisplayFont(valueLabelFont);
                renderer.setLabelFont(valueLabelFont);
                revalidate();
            }

            @Override
            public Font getFont()
            {
                return valueLabelFont;
            }
        });

    }

    private void attemptLabelPaintSelection() 
    {
        Color defaultColor = (valueLabelPaint instanceof Color ? (Color) valueLabelPaint : Color.blue);
        Color c = JColorChooser.showDialog(this, "Color", defaultColor);
        if (c != null) 
        {
            valueLabelPaint = c;
            labelPaintSample.setPaint(c);
            renderer.setLabelPaint(valueLabelPaint);
        }
    }

    private void attemptGuidelinesStrokeSelection() 
    {
        if(guidelineStrokeChooser == null)
        {
            guidelineStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(this), new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke)guidelinesStroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    guidelinesStroke = stroke;
                    guidelineStrokeSample.setStroke(stroke);
                    renderer.setGuideLineStroke(stroke);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return guidelinesPaint;
                }

                @Override
                public void setStrokePaint(Paint paint) 
                {
                    guidelinesPaint = paint;
                    guidelineStrokeSample.setStrokePaint(paint);
                    renderer.setGuideLinePaint(paint);
                }       	
            }
                    );

        }
        guidelineStrokeChooser.showDialog();
    }

    private void attemptOutlineStrokeSelection() 
    {
        if(outlineStrokeChooser == null)
        {
            outlineStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(this), new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke)outlineStroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    outlineStroke = stroke;
                    outlineStrokeSample.setStroke(stroke);
                    renderer.setOutlineStroke(stroke);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return outlinePaint;
                }

                @Override
                public void setStrokePaint(Paint paint) 
                {
                    outlinePaint = paint;
                    outlineStrokeSample.setStrokePaint(paint);
                    renderer.setOutlinePaint(paint);
                }       	
            }
                    );

        }
        outlineStrokeChooser.showDialog();
    }

    private void resetRenderer(CustomizableXYShapeRenderer renderer)
    {
        renderer.setTickLabelGroupingUsed(tickLabelGroupingUsed);
        renderer.setTickLabelDecimalSeparator(tickLabelDecimalSeparator);
        renderer.setTickLabelGroupingSeparator(tickLabelGroupingSeparator);
        renderer.setTickLabelShowTrailingZeroes(tickLabelTrailingZeroes); 

        renderer.setBaseSeriesVisible(seriesVisible);

        renderer.setLowerBound(lowerGradientBound);
        renderer.setUpperBound(upperGradientBound);
        renderer.setGradientMaskSelector(gradientMaskSelector);
        renderer.setGradientRangeSelector(gradientRangeSelector);
        renderer.setColorGradient(colorGradient);
        renderer.setGradientUnderflowColor(gradientUnderflowColor);
        renderer.setGradientOverflowColor(gradientOverflowColor);
        renderer.setUseOutsideRangeColors(useOutsideRangeColors);

        renderer.setMaskColor(maskColor);

        renderer.setOutlinesVisible(outlineVisible);
        renderer.setOutlinePaint(outlinePaint);
        renderer.setOutlineStroke(outlineStroke);

        renderer.setGuidelinesVisible(guidelinesVisible);
        renderer.setGuideLinePaint(guidelinesPaint);
        renderer.setGuideLineStroke(guidelinesStroke);

        renderer.setShapeMargin(margin);
        renderer.setShapeIndex(shapeIndex);
        renderer.setStretchShape(markerStretched);

        renderer.setBaseItemLabelsVisible(valueLabelsVisible);
        renderer.setBaseItemLabelFont(valueLabelFont);
        renderer.setBaseItemLabelPaint(valueLabelPaint);		
    }

    private void resetEditor()
    {
        boxUseThousandGrouping.setSelected(tickLabelGroupingUsed);	
        boxTrailingZeroes.setSelected(tickLabelTrailingZeroes);
        comboDecimalSeparator.setSelectedItem(tickLabelDecimalSeparator);
        comboGroupingSeparator.setSelectedItem(tickLabelGroupingSeparator);


        boxSeriesVisible.setSelected(seriesVisible);

        boxGuidelinesVisible.setSelected(guidelinesVisible);
        guidelineStrokeSample.setStroke(guidelinesStroke);
        guidelineStrokeSample.setStrokePaint(guidelinesPaint);

        boxOutlineVisible.setSelected(outlineVisible);
        outlineStrokeSample.setStroke(outlineStroke);
        outlineStrokeSample.setStrokePaint(outlinePaint);

        boxStretchMarker.setSelected(markerStretched);
        boxValueLabelsVisible.setSelected(valueLabelsVisible);
        spinnerMargins.setValue(100*margin);
        fieldLabelFont.setDisplayFont(valueLabelFont);
        labelPaintSample.setPaint(valueLabelPaint);
        updateGradientPaintSample();
        updateShapeLabel();
    }

    private void ensureGradinetChooserConsistencyWithReceiver()
    {
        if(gradientChooser != null)
        {
            gradientChooser.ensureConsistencyWithReceiver();
        }
    }

    @Override
    public void stateChanged(ChangeEvent evt) 
    {	
        Object source = evt.getSource();

        if(source == spinnerMargins)
        {
            margin = 0.01*((SpinnerNumberModel)spinnerMargins.getModel()).getNumber().floatValue();;
            renderer.setShapeMargin(margin); 
        }	
        else if(source == spinnerFractionDigits)
        {
            maxFractionDigits = ((SpinnerNumberModel)spinnerFractionDigits.getModel()).getNumber().intValue();
            renderer.setMaximumFractionDigits(maxFractionDigits);  
        }
    }

    private JLabel buildShapeLabel()
    {
        BufferedImage img = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);		
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.darkGray);
        g2.fill(SHAPES[shapeIndex]);
        JLabel shapeLabel = new JLabel("", new ImageIcon(img), SwingConstants.LEFT);
        return shapeLabel;
    }

    private void updateShapeLabel()
    {
        BufferedImage img = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);	

        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.darkGray);
        g2.fill(SHAPES[shapeIndex]);

        labelShapeIcon.setIcon(new ImageIcon(img));
        labelShapeIcon.repaint();
    }

    @Override
    public int getMarkerIndex() 
    {
        return shapeIndex;
    }

    @Override
    public float getMarkerSize() 
    {
        return 0;
    }

    @Override
    public Paint getPaint() 
    {
        return Color.darkGray;
    }

    @Override
    public boolean isFullRange() 
    {
        return GradientRangeSelector.FULL.equals(gradientRangeSelector);
    }

    @Override
    public boolean isAutomaticRange() 
    {
        return GradientRangeSelector.AUTOMATIC.equals(gradientRangeSelector);
    }

    @Override
    public boolean isColorROIFullRange()
    {
        return GradientRangeSelector.ROI_FULL.equals(gradientRangeSelector);
    }

    @Override
    public double getLowerROIBound()
    {
        return renderer.getLowerROIBound();
    }

    @Override
    public void setLowerROIBound(double lowerLensBoundNew)
    {
        renderer.setLowerROIBound(lowerLensBoundNew);
    }

    @Override
    public double getUpperROIBound()
    {
        return renderer.getUpperROIBound();
    }

    @Override
    public void setUpperROIBound(double upperLensBoundNew)
    {
        renderer.setUpperROIBound(upperLensBoundNew);
    }

    @Override
    public void setLensToFull()
    {
        renderer.setLensToFull();
    }

    @Override
    public double getLowerFullBound() 
    {
        return lowerFullGradientBound;
    }

    @Override
    public double getUpperFullBound() 
    {
        return upperFullGradientBound;
    }

    @Override
    public double getLowerBound() 
    {
        return lowerGradientBound;
    }

    @Override
    public void setLowerBound(double lb) 
    {
        this.lowerGradientBound = lb;
        renderer.setLowerBound(lb);
    }

    @Override
    public double getUpperBound() 
    {
        return upperGradientBound;
    }

    @Override
    public void setUpperBound(double ub) 
    {
        this.upperGradientBound = ub;
        renderer.setUpperBound(ub);	
    }

    @Override
    public void setGradientBounds(double lowerGradientBound, double upperGradientBound)
    {
        this.lowerGradientBound = lowerGradientBound;
        this.upperGradientBound = upperGradientBound;
        renderer.setGradientBounds(lowerGradientBound, upperGradientBound);		
    }

    @Override
    public PrefixedUnit getDataUnit()
    {
        return dataUnit;
    }

    @Override
    public PrefixedUnit getDisplayedUnit()
    {
        return displayedUnit;
    }


    @Override
    public double getLowerAutomaticBound() 
    {
        return lowerAutomaticGradientBound;
    }

    @Override
    public double getUpperAutomaticBound() 
    {
        return upperAutomaticGradientBound;
    }

    @Override
    public GradientRangeSelector getGradientRangeSelector() 
    {
        return gradientRangeSelector;
    }

    @Override
    public void setGradientRangeSelector(GradientRangeSelector selector) 
    {
        this.gradientRangeSelector = selector;
        renderer.setGradientRangeSelector(selector);
    }

    @Override
    public boolean areROISamplesNeeded()
    {
        return renderer.areROISamplesNeeded();
    }

    @Override
    public ColorGradient getColorGradient() 
    {
        return colorGradient;
    }

    @Override
    public void setColorGradient(ColorGradient lc) 
    {
        this.colorGradient = lc;
        renderer.setColorGradient(colorGradient);	
        updateGradientPaintSample();
    }

    private void updateGradientPaintSample()
    {
        Paint gradientPaint = new GradientPaint(colorGradient);
        gradientSample.setPaint(gradientPaint);
    }

    @Override
    public Color getGradientUnderflowColor() 
    {
        return gradientUnderflowColor;
    }

    @Override
    public void setGradientUnderflowColor(Color ufc) 
    {
        this.gradientUnderflowColor = ufc;
        renderer.setGradientUnderflowColor(ufc);	
    }

    @Override
    public Color getGradientOverflowColor() 
    {
        return gradientOverflowColor;
    }

    @Override
    public void setGradientOverflowColor(Color ofc) 
    {
        this.gradientOverflowColor = ofc;
        renderer.setGradientOverflowColor(ofc);			
    }

    @Override
    public boolean getUseOutsideRangeColors() 
    {
        return useOutsideRangeColors;
    }

    @Override
    public void setUseOutsideRangeColors(boolean useEndcolors) 
    {
        this.useOutsideRangeColors = useEndcolors;

        renderer.setUseOutsideRangeColors(useEndcolors);
    }


    @Override
    public void setMaskedRegion(ROI roi)
    {
        renderer.setMaskedRegion(roi);
    }

    @Override
    public Color getMaskColor() 
    {
        return maskColor;
    }

    @Override
    public void setMaskColor(Color maskColor) 
    {
        this.maskColor = maskColor;
        renderer.setMaskColor(maskColor);
    } 

    @Override
    public GradientMaskSelector getGradientMaskSelector() 
    {
        return gradientMaskSelector;
    }

    @Override
    public void setGradientMaskSelector(GradientMaskSelector selector) 
    {
        this.gradientMaskSelector = selector;
        renderer.setGradientMaskSelector(selector);
    }

    @Override
    public QuantitativeSample getPaintedSample() 
    {
        return renderer.getPaintedSample();
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);
        Object source = evt.getSource();

        if(source == boxOutlineVisible)
        {
            this.outlineVisible = selected;
            renderer.setOutlinesVisible(outlineVisible);
        }		
        else if(source == boxGuidelinesVisible)
        {
            guidelinesVisible = boxGuidelinesVisible.isSelected();
            renderer.setGuidelinesVisible(guidelinesVisible);
        }
        else if(source == boxSeriesVisible)
        {
            seriesVisible = boxSeriesVisible.isSelected();
            renderer.setBaseSeriesVisible(seriesVisible);
            renderer.setSeriesVisible(0, seriesVisible);
        }
        else if(source == boxValueLabelsVisible)
        {
            valueLabelsVisible = boxValueLabelsVisible.isSelected();
            renderer.setLabelVisible(valueLabelsVisible);
        }
        else if(source == boxStretchMarker)
        {
            markerStretched = boxStretchMarker.isSelected();
            renderer.setStretchShape(markerStretched);
        }
        if(source == boxTrailingZeroes)
        {
            tickLabelTrailingZeroes = selected;
            renderer.setTickLabelShowTrailingZeroes(tickLabelTrailingZeroes);
        }
        else if(source == comboDecimalSeparator)
        {			
            tickLabelDecimalSeparator = (Character)comboDecimalSeparator.getSelectedItem();		
            renderer.setTickLabelDecimalSeparator(tickLabelDecimalSeparator);
        }
        else if(source == comboGroupingSeparator)
        {
            tickLabelGroupingSeparator = (Character)comboGroupingSeparator.getSelectedItem();		
            renderer.setTickLabelGroupingSeparator(tickLabelGroupingSeparator);
        }	
    }

    private JPanel buildMainPanel()
    {
        JPanel outerPanel = new JPanel(new BorderLayout());

        SubPanel mainPanel = new SubPanel();

        JLabel labelShape = new JLabel("Shape");
        labelShape.setHorizontalAlignment(SwingConstants.RIGHT);

        mainPanel.addComponent(boxSeriesVisible, 2, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);       

        SubPanel marginsPanel = new SubPanel();
        marginsPanel.addComponent(new JLabel("Margin (%)"), 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.05, 0);
        marginsPanel.addComponent(spinnerMargins, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);


        mainPanel.addComponent(labelShape, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(labelShapeIcon, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);
        mainPanel.addComponent(boxStretchMarker, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);        
        mainPanel.addComponent(marginsPanel, 3, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,1, 0);
        mainPanel.addComponent(selectShapeButton, 5, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        SubPanel outlineStrokePanel = new SubPanel();
        outlineStrokePanel.addComponent(boxOutlineVisible, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.015, 0);     
        outlineStrokePanel.addComponent(outlineStrokeSample, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);           

        mainPanel.addComponent(new JLabel("Outlines"), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(outlineStrokePanel, 1, 2, 4, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0.075, 0);     
        mainPanel.addComponent(buttonEditOutline, 5, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        SubPanel guidelineStrokePanel = new SubPanel();
        guidelineStrokePanel.addComponent(boxGuidelinesVisible, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,0.015, 0);
        guidelineStrokePanel.addComponent(guidelineStrokeSample, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);      

        mainPanel.addComponent(new JLabel("Guidelines"), 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(guidelineStrokePanel, 1, 3, 4, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,0.075, 0);
        mainPanel.addComponent(buttonEditGuidelines, 5, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);


        mainPanel.addComponent(new JLabel("Gradient"), 0, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(gradientSample, 1, 4, 4, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);
        mainPanel.addComponent(selectGradientButton, 5, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);       

        outerPanel.add(mainPanel, BorderLayout.NORTH);     
        outerPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return outerPanel;
    }       

    private JPanel buildFormatPanel()
    {
        JPanel formatPanel = new JPanel();
        SubPanel innerPanel = new SubPanel();			

        JLabel labelTrailingZeroes = new JLabel("Trailing zeroes");
        JLabel labelMaxFractionDigits = new JLabel("Fraction digits: ");
        JLabel labelDecimalSeparator = new JLabel("Decimal separator: ");
        JLabel labelThousandSeparator = new JLabel("Thousand separator: ");	

        innerPanel.addComponent(new JLabel("Value labels"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(boxValueLabelsVisible, 1, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, .1, 0);

        innerPanel.addComponent(new JLabel("Label font"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(fieldLabelFont, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);
        innerPanel.addComponent(buttonSelectLabelFont, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .1, 0);     

        innerPanel.addComponent(new JLabel("Label color"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(labelPaintSample, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);
        innerPanel.addComponent(buttonSelectLabelPaint, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .1, 0);     

        innerPanel.addComponent(Box.createVerticalStrut(10), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(Box.createVerticalStrut(10), 1, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 0);		

        innerPanel.addComponent(labelTrailingZeroes, 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(boxTrailingZeroes, 1, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        innerPanel.addComponent(labelMaxFractionDigits, 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(spinnerFractionDigits, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        innerPanel.addComponent(labelDecimalSeparator, 0, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(comboDecimalSeparator, 1, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .2, 0);

        innerPanel.addComponent(labelThousandSeparator, 0, 7, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        innerPanel.addComponent(comboGroupingSeparator, 1, 7, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        innerPanel.addComponent(boxUseThousandGrouping,2, 7, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .1, 0);


        formatPanel.add(innerPanel);
        formatPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return formatPanel;	
    }
}
