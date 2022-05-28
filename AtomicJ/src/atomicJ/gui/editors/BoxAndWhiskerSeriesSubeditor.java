
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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.BasicStrokeReceiver;
import atomicJ.gui.PaintSampleFlexible;
import atomicJ.gui.PlotStyleUtilities;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.SubPanel;
import atomicJ.gui.boxplots.CustomizableXYBoxAndWhiskerRenderer;
import atomicJ.gui.boxplots.PreferredBoxAndWhiskerRendererStyle;



public class BoxAndWhiskerSeriesSubeditor implements Subeditor, ActionListener, ItemListener, ChangeListener
{
    private static final String SELECT_BOX_PAINT_COMMAND = "SELECT_BOX_PAINT_COMMAND";
    private static final String SELECT_MEAN_PAINT_COMMAND = "SELECT_MEAN_PAINT_COMMAND";

    private static final String SELECT_OUTLIER_FILL_PAINT_COMMAND = "SELECT_OUTLIER_FILL_PAINT_COMMAND";
    private static final String SELECT_OUTLIER_MARKER_SHAPE_COMMAND = "SELECT_OUTLIER_MARKER_SHAPE_COMMAND";

    private static final String EDIT_OUTLIER_STROKE_COMMAND = "EDIT_OUTLIER_STROKE_COMMAND";

    private static final String EDIT_BOX_OUTLINE_STROKE_COMMAND = "EDIT_BOX_OUTLINE_STROKE_COMMAND";
    private static final String EDIT_MEDIAN_STROKE_COMMAND = "EDIT_MEDIAN_STROKE_COMMAND";
    private static final String EDIT_MEAN_OUTLINE_STROKE_COMMAND = "EDIT_MEAN_OUTLINE_STROKE_COMMAND";

    private static final String EDIT_WHISKER_STROKE_COMMAND = "EDIT_WHISKER_STROKE_COMMAND";
    private static final String EDIT_WHISKER_CROSS_BAR_STROKE_COMMAND = "EDIT_WHISKER_CROSS_BAR_STROKE_COMMAND";

    private static final Shape[] SHAPES = PlotStyleUtilities.getNonZeroAreaShapes();    

    private boolean boxFilled;
    private Paint boxPaint;
    private double boxWidth;

    private boolean outlineVisible;
    private Paint outlinePaint;
    private Stroke outlineStroke;

    //current whiskers properties
    private Paint whiskerPaint;
    private Stroke whiskerStroke;
    private boolean whiskerCrossBarVisible;
    private double whiskerCrossBarWidth;
    private Paint whiskerCrossBarPaint;
    private Stroke whiskerCrossBarStroke;

    //current mean properties
    private boolean meanVisible;
    private boolean meanFilled;
    private Paint meanFillPaint;
    private boolean meanOutlineVisible;
    private Paint meanOutlinePaint;
    private Stroke meanOutlineStroke;

    //current median properties
    private boolean medianVisible;
    private Paint medianPaint;
    private Stroke medianStroke;

    //current outliers properties
    private boolean outliersVisible;
    private int outlierMarkerIndex;
    private float outlierSize;
    private boolean outlierFilled;
    private Paint outlierFillPaint;
    private boolean outlierOutlineVisible;
    private Stroke outlierStroke;
    private Paint outlierStrokePaint;


    //initial box properties
    private final boolean initBoxFilled;
    private final Paint initBoxPaint;
    private final double initBoxWidth;

    //initial box outline properties
    private final boolean initOutlineVisible;
    private final Paint initOutlinePaint;
    private final Stroke initOutlineStroke;

    //initial mean properties
    private final boolean initMeanVisible;
    private final boolean initMeanFilled;
    private final Paint initMeanFillPaint;
    private final boolean initMeanOutlineVisible;
    private final Paint initMeanOutlinePaint;
    private final Stroke initMeanOutlineStroke;

    //initial whisker properties
    private final Paint initWhiskerPaint;
    private final Stroke initWhiskerStroke;
    private final boolean initWhiskerCrossBarVisible;
    private final double initWhiskerCrossBarWidth;
    private final Paint initWhiskerCrossBarPaint;
    private final Stroke initWhiskerCrossBarStroke;

    //initial median properties
    private final boolean initMedianVisible;
    private final Paint initMedianPaint;
    private final Stroke initMedianStroke;

    //initial outlier properties
    private final boolean initOutliersVisible;
    private final int initOutlierMarkerIndex;
    private final float initOutlierSize;
    private final boolean initOutlierFilled;
    private final Paint initOutlierFillPaint;
    private final boolean initOutlierOutlineVisible;
    private final Stroke initOutlierStroke;
    private final Paint initOutlierStrokePaint;

    /////////////////// tab box

    private final JCheckBox boxFillBox = new JCheckBox("Fill");
    private final PaintSampleFlexible boxFillPaintSample = new PaintSampleFlexible();
    private final JSpinner spinnerBoxWidth 
    = new JSpinner(new SpinnerNumberModel(1,1,Short.MAX_VALUE,1));
    private final JButton buttonSelectBoxFillPaint = new JButton("Select"); 

    private final JCheckBox boxOutlineVisible = new JCheckBox("Show");
    private final StraightStrokeSample outlineStrokeSample = new StraightStrokeSample();
    private final JButton buttonEditOutlineStroke = new JButton("Edit");        

    ////////////////////tab whisker

    private final StraightStrokeSample whiskerStrokeSample = new StraightStrokeSample();
    private final JButton buttonEditWhiskerStroke = new JButton("Edit"); 

    private final JCheckBox boxWhiskerCrossBar = new JCheckBox("Show");
    private final StraightStrokeSample whiskerCrossBarStrokeSample = new StraightStrokeSample();
    private final JButton buttonEditWhiskerCrossBarStroke = new JButton("Edit");   
    private final JSpinner spinnerWhiskerCrossBarWidth = new JSpinner(new SpinnerNumberModel(1,1,Short.MAX_VALUE,1));

    ////////////////////tab median

    private final JCheckBox boxMedianVisible = new JCheckBox("Show");
    private final StraightStrokeSample medianStrokeSample = new StraightStrokeSample();
    private final JButton buttonEditMedianStroke = new JButton("Edit");        


    ////////////////////tab mean
    private final JCheckBox boxMeanVisible = new JCheckBox("Show");

    private final JCheckBox boxMeanFilled = new JCheckBox("Filled");
    private final PaintSampleFlexible meanFillPaintSample = new PaintSampleFlexible();
    private final JButton buttonSelectMeanPaint = new JButton("Select"); 

    private final JCheckBox boxMeanOutlineVisible = new JCheckBox("Show");
    private final StraightStrokeSample meanOutlineStrokeSample = new StraightStrokeSample();
    private final JButton buttonEditMeanOutlineStroke = new JButton("Edit");  

    /////////////////////tab outliers

    private ShapeAndSizeChooser outlierMarkerChooser;

    private final JCheckBox boxOutliersVisible = new JCheckBox("Show");

    private final JLabel labelOutlierMarkerShape = new JLabel();
    private final JButton buttonSelectOutlierMarkerShape = new JButton("Select");

    private final JCheckBox boxOutlierFilled = new JCheckBox("Filled");
    private final PaintSampleFlexible outlierFillPaintSample = new PaintSampleFlexible();
    private final JButton buttonSelectOutlierFillPaint = new JButton("Select");      

    private final JCheckBox boxOutlierOutlineVisible = new JCheckBox("Show");
    private final StraightStrokeSample outlierStrokeSample = new StraightStrokeSample();
    private final JButton buttonEditOutlierStroke = new JButton("Edit");        

    ///////////////////////////////////////////////////////////////////////////////

    private final PreferredBoxAndWhiskerRendererStyle pref;       
    private final String seriesName;

    private StrokeChooser boxOutlineStrokeChooser;
    private StrokeChooser whiskerStrokeChooser;
    private StrokeChooser whiskerCrossBarStrokeChooser;
    private StrokeChooser medianStrokeChooser;
    private StrokeChooser meanOutlineStrokeChooser;
    private StrokeChooser outlierStrokeChooser;

    private final CustomizableXYBoxAndWhiskerRenderer renderer;
    private final List<CustomizableXYBoxAndWhiskerRenderer> boundededRenderers;

    private final SubPanel editorPanel = new SubPanel();

    public BoxAndWhiskerSeriesSubeditor(final CustomizableXYBoxAndWhiskerRenderer renderer, List<CustomizableXYBoxAndWhiskerRenderer> boundedRenderers)
    {
        this.renderer = renderer;
        this.boundededRenderers = boundedRenderers;

        this.initBoxPaint = renderer.getBoxPaint();
        this.initBoxFilled = renderer.getBoxFilled();
        this.initBoxWidth = renderer.getBoxWidth();

        this.initOutlinePaint = renderer.getBaseOutlinePaint();
        this.initOutlineStroke = renderer.getBaseOutlineStroke();
        this.initOutlineVisible = renderer.isBoxOutlineVisible();

        this.initWhiskerCrossBarVisible = renderer.isWhiskerCrossBarVisible();
        this.initWhiskerPaint = renderer.getWhiskerPaint();
        this.initWhiskerStroke = renderer.getWhiskerStroke();
        this.initWhiskerCrossBarWidth = renderer.getWhiskerCrossBarWidth();
        this.initWhiskerCrossBarPaint = renderer.getWhiskerCrossBarPaint();
        this.initWhiskerCrossBarStroke = renderer.getWhiskerCrossBarStroke();

        this.initMeanVisible = renderer.isMeanVisible();
        this.initMeanFilled = renderer.isMeanFilled();
        this.initMeanFillPaint = renderer.getMeanFillPaint();
        this.initMeanOutlineVisible = renderer.isMeanOutlineVisible();
        this.initMeanOutlinePaint = renderer.getMeanOutlinePaint();
        this.initMeanOutlineStroke = renderer.getMeanOutlineStroke();

        this.initMedianVisible = renderer.isMedianVisible();
        this.initMedianPaint = renderer.getMedianPaint();
        this.initMedianStroke = renderer.getMedianStroke();

        this.initOutliersVisible = renderer.isOutliersVisible();
        this.initOutlierMarkerIndex = renderer.getOutlierMarkerIndex();
        this.initOutlierSize = renderer.getOutlierSize();
        this.initOutlierFilled = renderer.isOutlierFilled();
        this.initOutlierFillPaint = renderer.getOutlierFillPaint();
        this.initOutlierOutlineVisible = renderer.isOutlierOutlineVisible();
        this.initOutlierStroke = renderer.getOutlierStroke();
        this.initOutlierStrokePaint = renderer.getOutlierStrokePaint();

        this.seriesName = renderer.getName();
        this.pref = renderer.getPreferredRendererStyle();

        setParametersToInitial();		
        resetEditor();

        JTabbedPane mainPane = new JTabbedPane();
        mainPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5)); 

        //box panel
        JPanel boxPanel = buildBoxPanel();
        mainPane.add("Box", boxPanel);

        //whiskers panel
        JPanel whiskersPanel = buildWhiskersPanel();
        mainPane.add("Whiskers", whiskersPanel);

        //median panel
        JPanel medianPanel = buildMedianPanel();
        mainPane.add("Median", medianPanel);

        //mean panel
        JPanel meanPanel = buildMeanPanel();
        mainPane.add("Mean", meanPanel);

        //outliers panel
        JPanel outlierPanel = buildOutlierPanel();
        mainPane.add("Outliers", outlierPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        mainPanel.add(mainPane, BorderLayout.NORTH);

        editorPanel.setLayout(new BorderLayout());
        editorPanel.add(mainPanel, BorderLayout.CENTER);
        editorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        initActionListener();
        initChangeListener();
        initItemListener();
    }

    private JPanel buildBoxPanel()
    {
        JPanel outerPanel = new JPanel(new BorderLayout());

        SubPanel contentPanel = new SubPanel();

        contentPanel.addComponent(new JLabel("Width"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        contentPanel.addComponent(spinnerBoxWidth, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);       

        contentPanel.addComponent(new JLabel("Color"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        contentPanel.addComponent(boxFillBox, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);       
        contentPanel.addComponent(boxFillPaintSample, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);       
        contentPanel.addComponent(buttonSelectBoxFillPaint, 3, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);       

        contentPanel.addComponent(new JLabel("Outline"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        contentPanel.addComponent(boxOutlineVisible, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);       
        contentPanel.addComponent(outlineStrokeSample, 2, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);       
        contentPanel.addComponent(buttonEditOutlineStroke, 3, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);       

        outerPanel.add(contentPanel, BorderLayout.NORTH);     
        outerPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return outerPanel;
    }

    private JPanel buildWhiskersPanel()
    {
        JPanel outerPanel = new JPanel(new BorderLayout());

        SubPanel contentPanel = new SubPanel();

        contentPanel.addComponent(new JLabel("Whiskers"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        contentPanel.addComponent(whiskerStrokeSample, 1, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);       
        contentPanel.addComponent(buttonEditWhiskerStroke, 3, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 1);       

        contentPanel.addComponent(new JLabel("Crossbar"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        contentPanel.addComponent(boxWhiskerCrossBar, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0, 1);       
        contentPanel.addComponent(whiskerCrossBarStrokeSample, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);       
        contentPanel.addComponent(buttonEditWhiskerCrossBarStroke, 3, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 1);       

        contentPanel.addComponent(new JLabel("Width"), 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        contentPanel.addComponent(spinnerWhiskerCrossBarWidth, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);       

        outerPanel.add(contentPanel, BorderLayout.NORTH);     
        outerPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return outerPanel;
    }

    private JPanel buildMedianPanel()
    {
        JPanel outerPanel = new JPanel(new BorderLayout());

        SubPanel contentPanel = new SubPanel();

        contentPanel.addComponent(new JLabel("Median"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        contentPanel.addComponent(boxMedianVisible, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);       
        contentPanel.addComponent(medianStrokeSample, 2, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);       
        contentPanel.addComponent(buttonEditMedianStroke, 3, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 1);       

        outerPanel.add(contentPanel, BorderLayout.NORTH);     
        outerPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return outerPanel;
    }

    private JPanel buildMeanPanel()
    {
        JPanel outerPanel = new JPanel(new BorderLayout());

        SubPanel contentPanel = new SubPanel();

        contentPanel.addComponent(boxMeanVisible, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);       

        contentPanel.addComponent(boxMeanFilled, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);       
        contentPanel.addComponent(meanFillPaintSample, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);       
        contentPanel.addComponent(buttonSelectMeanPaint, 3, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 1);       

        contentPanel.addComponent(new JLabel("Outline"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        contentPanel.addComponent(boxMeanOutlineVisible, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);       
        contentPanel.addComponent(meanOutlineStrokeSample, 2, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);       
        contentPanel.addComponent(buttonEditMeanOutlineStroke, 3, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 1);       

        outerPanel.add(contentPanel, BorderLayout.NORTH);     
        outerPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return outerPanel;
    }

    private JPanel buildOutlierPanel()
    {
        JPanel outerPanel = new JPanel(new BorderLayout());

        SubPanel contentPanel = new SubPanel();

        contentPanel.addComponent(boxOutliersVisible, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);       
        contentPanel.addComponent(new JLabel("Shape"), 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        contentPanel.addComponent(labelOutlierMarkerShape, 3, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);       
        contentPanel.addComponent(buttonSelectOutlierMarkerShape, 4, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 1);       

        contentPanel.addComponent(new JLabel("Color"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        contentPanel.addComponent(boxOutlierFilled, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);       
        contentPanel.addComponent(outlierFillPaintSample, 2, 1, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);       
        contentPanel.addComponent(buttonSelectOutlierFillPaint, 4, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 1);       

        contentPanel.addComponent(new JLabel("Outline"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);
        contentPanel.addComponent(boxOutlierOutlineVisible, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);       
        contentPanel.addComponent(outlierStrokeSample, 2, 2, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);       
        contentPanel.addComponent(buttonEditOutlierStroke, 4, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0, 1);       


        outerPanel.add(contentPanel, BorderLayout.NORTH);     
        outerPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return outerPanel;
    }

    @Override
    public void setNameBorder(boolean named)
    {
        Border border = named ? BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5),
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), seriesName))
                : BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder());

        editorPanel.setBorder(border);
    }

    private void initActionListener()
    {
        buttonSelectBoxFillPaint.setActionCommand(SELECT_BOX_PAINT_COMMAND);
        buttonEditOutlineStroke.setActionCommand(EDIT_BOX_OUTLINE_STROKE_COMMAND);
        buttonSelectMeanPaint.setActionCommand(SELECT_MEAN_PAINT_COMMAND);
        buttonEditMedianStroke.setActionCommand(EDIT_MEDIAN_STROKE_COMMAND);
        buttonEditMeanOutlineStroke.setActionCommand(EDIT_MEAN_OUTLINE_STROKE_COMMAND);
        buttonEditWhiskerStroke.setActionCommand(EDIT_WHISKER_STROKE_COMMAND);
        buttonEditWhiskerCrossBarStroke.setActionCommand(EDIT_WHISKER_CROSS_BAR_STROKE_COMMAND);
        buttonSelectOutlierMarkerShape.setActionCommand(SELECT_OUTLIER_MARKER_SHAPE_COMMAND);
        buttonSelectOutlierFillPaint.setActionCommand(SELECT_OUTLIER_FILL_PAINT_COMMAND);
        buttonEditOutlierStroke.setActionCommand(EDIT_OUTLIER_STROKE_COMMAND);

        buttonSelectBoxFillPaint.addActionListener(this);
        buttonEditOutlineStroke.addActionListener(this);
        buttonSelectMeanPaint.addActionListener(this);
        buttonEditMedianStroke.addActionListener(this);
        buttonEditMeanOutlineStroke.addActionListener(this);
        buttonEditWhiskerStroke.addActionListener(this);
        buttonEditWhiskerCrossBarStroke.addActionListener(this);
        buttonSelectOutlierMarkerShape.addActionListener(this);
        buttonSelectOutlierFillPaint.addActionListener(this);
        buttonEditOutlierStroke.addActionListener(this);
    }

    private void initChangeListener()
    {
        spinnerBoxWidth.addChangeListener(this);
        spinnerWhiskerCrossBarWidth.addChangeListener(this);
    }

    private void initItemListener()
    {
        boxOutlineVisible.addItemListener(this);
        boxFillBox.addItemListener(this);
        boxWhiskerCrossBar.addItemListener(this);
        boxMedianVisible.addItemListener(this);
        boxMeanVisible.addItemListener(this);
        boxMeanFilled.addItemListener(this);
        boxMeanOutlineVisible.addItemListener(this);
        boxOutliersVisible.addItemListener(this);
        boxOutlierFilled.addItemListener(this);
        boxOutlierOutlineVisible.addItemListener(this);
    }

    private void setParametersToInitial()
    {
        this.boxFilled = initBoxFilled;
        this.boxPaint = initBoxPaint;
        this.boxWidth = initBoxWidth;

        this.outlineVisible = initOutlineVisible;
        this.outlinePaint = initOutlinePaint;
        this.outlineStroke = initOutlineStroke;

        this.whiskerPaint = initWhiskerPaint;
        this.whiskerStroke = initWhiskerStroke; 

        this.whiskerCrossBarVisible = initWhiskerCrossBarVisible;
        this.whiskerCrossBarWidth = initWhiskerCrossBarWidth;
        this.whiskerCrossBarPaint = initWhiskerCrossBarPaint;
        this.whiskerCrossBarStroke = initWhiskerCrossBarStroke;

        this.meanVisible = initMeanVisible;
        this.meanFilled = initMeanFilled;
        this.meanFillPaint = initMeanFillPaint;
        this.meanOutlineVisible = initMeanOutlineVisible;
        this.meanOutlinePaint = initMeanOutlinePaint;
        this.meanOutlineStroke = initMeanOutlineStroke;

        this.medianVisible = initMedianVisible;
        this.medianPaint = initMedianPaint;
        this.medianStroke = initMedianStroke;    

        this.outliersVisible = initOutliersVisible;
        this.outlierMarkerIndex = initOutlierMarkerIndex;
        this.outlierSize = initOutlierSize;
        this.outlierFilled = initOutlierFilled;
        this.outlierFillPaint = initOutlierFillPaint;
        this.outlierOutlineVisible = initOutlierOutlineVisible;
        this.outlierStroke = initOutlierStroke;
        this.outlierStrokePaint = initOutlierStrokePaint;
    }

    @Override
    public String getSubeditorName()
    {
        return seriesName;
    }

    @Override
    public void resetToDefaults() 
    {
        this.boxFilled = pref.getFillBox();
        this.boxPaint = pref.getBoxFillPaint();
        this.boxWidth = pref.getBoxWidth();

        this.outlineVisible = pref.isBoxOutlineVisible();
        this.outlinePaint = pref.getBoxOutlinePaint();
        this.outlineStroke = pref.getBoxOutlineStroke();

        this.whiskerPaint = pref.getWhiskerPaint();
        this.whiskerStroke = pref.getWhiskerStroke();

        this.whiskerCrossBarVisible = pref.isWhiskerCrossBarVisible();
        this.whiskerCrossBarWidth = pref.getWhiskerCrossBarWidth();
        this.whiskerCrossBarPaint = pref.getWhiskerCrossBarPaint();
        this.whiskerCrossBarStroke = pref.getWhiskerCrossBarStroke();

        this.meanVisible = pref.isMeanVisible();
        this.meanFilled = pref.isMeanFilled();
        this.meanFillPaint = pref.getMeanPaint();
        this.meanOutlineVisible = pref.isMeanOutlineVisible();
        this.meanOutlinePaint = pref.getMeanOutlinePaint();
        this.meanOutlineStroke = pref.getMeanOutlineStroke();

        this.medianVisible = pref.isMedianVisible();
        this.medianPaint = pref.getMedianPaint();
        this.medianStroke = pref.getMedianStroke();

        this.outliersVisible = pref.isOutliersVisible();
        this.outlierMarkerIndex = pref.getOutlierMarkerIndex();
        this.outlierSize = pref.getOutlierSize();
        this.outlierFilled = pref.isOutlierFilled();
        this.outlierFillPaint = pref.getOutlierFillPaint();
        this.outlierOutlineVisible = pref.isOutlierOutlineVisible();
        this.outlierStroke = pref.getOutlierStroke();
        this.outlierStrokePaint = pref.getOutlierStrokePaint();

        resetRenderer(renderer);	
        resetEditor();
    }

    private void resetRenderer(CustomizableXYBoxAndWhiskerRenderer r)
    {      
        r.setBoxPaint(boxPaint);
        r.setBoxFilled(boxFilled);
        r.setBoxWidth(boxWidth);

        r.setBoxOutlineVisible(outlineVisible);
        r.setBaseOutlinePaint(outlinePaint);
        r.setBaseOutlineStroke(outlineStroke);

        r.setWhiskerPaint(whiskerPaint);
        r.setWhiskerStroke(whiskerStroke);

        r.setWhiskerCrossBarVisible(whiskerCrossBarVisible);
        r.setWhiskerCrossBarWidth(whiskerCrossBarWidth);
        r.setWhiskerCrossBarPaint(whiskerCrossBarPaint);
        r.setWhiskerCrossBarStroke(whiskerCrossBarStroke);

        r.setMeanVisible(meanVisible);
        r.setMeanFilled(meanFilled);
        r.setMeanFillPaint(meanFillPaint);
        r.setMeanOutlineVisible(meanOutlineVisible);
        r.setMeanOutlinePaint(meanOutlinePaint);
        r.setMeanOutlineStroke(meanOutlineStroke);

        r.setMedianVisible(medianVisible);
        r.setMedianPaint(medianPaint);
        r.setMedianStroke(medianStroke);

        r.setOutliersVisible(outliersVisible);
        r.setOutlierMarkerIndex(outlierMarkerIndex);
        r.setOutlierSize(outlierSize);
        r.setOutlierFilled(outlierFilled);
        r.setOutlierFillPaint(outlierFillPaint);
        r.setOutlierOutlineVisible(outlierOutlineVisible);
        r.setOutlierStroke(outlierStroke);
        r.setOutlierStrokePaint(outlierStrokePaint);
    }


    @Override
    public void saveAsDefaults() 
    {
        pref.setFillBox(boxFilled);
        pref.setBoxFillPaint(boxPaint);
        pref.setBoxWidth(boxWidth);

        pref.setBoxOutlineVisible(outlineVisible);
        pref.setBoxOutlinePaint(outlinePaint);
        pref.setBoxOutlineStroke(outlineStroke);

        pref.setWhiskerPaint(whiskerPaint);
        pref.setWhiskerStroke(whiskerStroke);
        pref.setWhiskerCrossBarVisible(whiskerCrossBarVisible);
        pref.setWhiskerCrossBarWidth(whiskerCrossBarWidth);
        pref.setWhiskerCrossBarPaint(whiskerCrossBarPaint);
        pref.setWhiskerCrossBarStroke(whiskerCrossBarStroke);

        pref.setMeanVisible(meanVisible);
        pref.setMeanFilled(meanFilled);
        pref.setMeanPaint(meanFillPaint);
        pref.setMeanOutlineVisible(meanOutlineVisible);
        pref.setMeanOutlinePaint(meanOutlinePaint);
        pref.setMeanOutlineStroke(meanOutlineStroke);

        pref.setMedianVisible(medianVisible);
        pref.setMedianPaint(medianPaint);
        pref.setMedianStroke(medianStroke);

        pref.setOutliersVisible(outliersVisible);
        pref.setOutlierMarkerIndex(outlierMarkerIndex);
        pref.setOutlierMarkerSize(outlierSize);
        pref.setOutlierFilled(outlierFilled);
        pref.setOutlierFillPaint(outlierFillPaint);
        pref.setOutlierOutlineVisible(outlierOutlineVisible);
        pref.setOutlierStroke(outlierStroke);
        pref.setOutlierStrokePaint(outlierStrokePaint);
    }

    @Override
    public void applyChangesToAll() 
    {
        for(CustomizableXYBoxAndWhiskerRenderer r : boundededRenderers)
        {
            resetRenderer(r);	
        }
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();		
        resetRenderer(renderer);			
        resetEditor();
    }

    @Override
    public Component getEditionComponent() 
    {
        return editorPanel;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return boundededRenderers.size() > 1;
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();

        if (SELECT_BOX_PAINT_COMMAND.equals(command)) 
        {
            attemptBoxPaintSelection();           
        }	
        else if (SELECT_MEAN_PAINT_COMMAND.equals(command)) 
        {
            attemptMeanPaintSelection();           
        }   
        else if(SELECT_OUTLIER_MARKER_SHAPE_COMMAND.equals(command))
        {
            attemptOutlierMarkerSelection();
        } 
        else if(SELECT_OUTLIER_FILL_PAINT_COMMAND.equals(command))
        {
            attemptOutlierFillPaintSelection();
        } 
        else if(EDIT_OUTLIER_STROKE_COMMAND.equals(command))
        {
            attemptOutlierStrokeSelection();
        }
        else if(EDIT_BOX_OUTLINE_STROKE_COMMAND.equals(command))
        {
            attemptOutlineStrokeSelection();
        }
        else if(EDIT_MEDIAN_STROKE_COMMAND.equals(command))
        {
            attemptMedianStrokeSelection();
        }
        else if(EDIT_MEAN_OUTLINE_STROKE_COMMAND.equals(command))
        {
            attemptMeanOutlineStrokeSelection();
        }
        else if(EDIT_WHISKER_STROKE_COMMAND.equals(command))
        {
            attemptWhiskerStrokeSelection();
        }
        else if(EDIT_WHISKER_CROSS_BAR_STROKE_COMMAND.equals(command))
        {
            attemptWhiskerCrossBarStrokeSelection();
        }
    }	

    private void attemptBoxPaintSelection() 
    {
        Paint p = this.boxPaint;
        Color defaultColor = (p instanceof Color ? (Color) p : Color.blue);
        Color c = JColorChooser.showDialog(editorPanel, "Color", defaultColor);

        if (c != null) 
        {
            this.boxPaint = c;
            this.boxFillPaintSample.setPaint(c);
            renderer.setBoxPaint(boxPaint);
        }
    }

    private void attemptMeanPaintSelection() 
    {
        Paint p = this.meanFillPaint;
        Color defaultColor = (p instanceof Color ? (Color) p : Color.blue);
        Color c = JColorChooser.showDialog(editorPanel, "Color", defaultColor);

        if (c != null) 
        {
            this.meanFillPaint = c;
            this.meanFillPaintSample.setPaint(c);
            renderer.setMeanFillPaint(meanFillPaint);
        }
    }

    private void attemptOutlierFillPaintSelection() 
    {
        Paint p = this.outlierFillPaint;
        Color defaultColor = (p instanceof Color ? (Color) p : Color.blue);
        Color c = JColorChooser.showDialog(editorPanel, "Color", defaultColor);

        if (c != null) 
        {
            this.outlierFillPaint = c;
            this.outlierFillPaintSample.setPaint(c);
            renderer.setOutlierFillPaint(outlierFillPaint);
        }
    }

    private void attemptOutlineStrokeSelection() 
    {
        if(boxOutlineStrokeChooser == null)
        {
            Window parentWindow = SwingUtilities.getWindowAncestor(editorPanel);
            boxOutlineStrokeChooser = new StrokeChooser(parentWindow, new OutlineStrokeReceiver()); 
        }

        boxOutlineStrokeChooser.showDialog();
    }

    private void attemptWhiskerStrokeSelection() 
    {
        if(whiskerStrokeChooser == null)
        {
            Window parentWindow = SwingUtilities.getWindowAncestor(editorPanel);
            whiskerStrokeChooser = new StrokeChooser(parentWindow, new WhiskerStrokeReceiver()); 
        }

        whiskerStrokeChooser.showDialog();
    }

    private void attemptWhiskerCrossBarStrokeSelection() 
    {
        if(whiskerCrossBarStrokeChooser == null)
        {
            Window parentWindow = SwingUtilities.getWindowAncestor(editorPanel);
            whiskerCrossBarStrokeChooser = new StrokeChooser(parentWindow, new WhiskerCrossBarStrokeReceiver()); 
        }

        whiskerCrossBarStrokeChooser.showDialog();
    }

    private void attemptMedianStrokeSelection() 
    {
        if(medianStrokeChooser == null)
        {
            Window parentWindow = SwingUtilities.getWindowAncestor(editorPanel);
            medianStrokeChooser = new StrokeChooser(parentWindow, new MedianStrokeReceiver()); 
        }

        medianStrokeChooser.showDialog();
    }


    private void attemptMeanOutlineStrokeSelection() 
    {
        if(meanOutlineStrokeChooser == null)
        {
            Window parentWindow = SwingUtilities.getWindowAncestor(editorPanel);
            meanOutlineStrokeChooser = new StrokeChooser(parentWindow, new MeanOutlineStrokeReceiver()); 
        }

        meanOutlineStrokeChooser.showDialog();
    }

    private void attemptOutlierStrokeSelection() 
    {
        if(outlierStrokeChooser == null)
        {
            Window parentWindow = SwingUtilities.getWindowAncestor(editorPanel);
            outlierStrokeChooser = new StrokeChooser(parentWindow, new OutlierStrokeReceiver()); 
        }

        outlierStrokeChooser.showDialog();
    }


    public void attemptOutlierMarkerSelection() 
    {
        if(outlierMarkerChooser == null)
        {
            Window parentWindow = SwingUtilities.getWindowAncestor(editorPanel);

            this.outlierMarkerChooser =
                    new ShapeAndSizeChooser(parentWindow, new OutlierMarkerStyleReceiver(), PlotStyleUtilities.getNonZeroAreaShapes());
        }
        outlierMarkerChooser.setVisible(true);
    }

    private void resetEditor()
    {
        boxFillBox.setSelected(boxFilled);
        boxFillPaintSample.setPaint(boxPaint);
        spinnerBoxWidth.setValue(boxWidth);

        boxOutlineVisible.setSelected(outlineVisible);
        outlineStrokeSample.setStroke(outlineStroke);
        outlineStrokeSample.setStrokePaint(outlinePaint);

        whiskerStrokeSample.setStroke(whiskerStroke);
        whiskerStrokeSample.setStrokePaint(whiskerPaint);

        boxWhiskerCrossBar.setSelected(whiskerCrossBarVisible);
        spinnerWhiskerCrossBarWidth.setValue(whiskerCrossBarWidth);
        whiskerCrossBarStrokeSample.setStroke(whiskerCrossBarStroke);
        whiskerCrossBarStrokeSample.setStrokePaint(whiskerCrossBarPaint);

        boxMeanVisible.setSelected(meanVisible);
        boxMeanFilled.setSelected(meanFilled);
        meanFillPaintSample.setPaint(meanFillPaint);
        boxMeanOutlineVisible.setSelected(meanOutlineVisible);
        meanOutlineStrokeSample.setStroke(meanOutlineStroke);
        meanOutlineStrokeSample.setStrokePaint(meanOutlinePaint);

        boxMedianVisible.setSelected(medianVisible);
        medianStrokeSample.setStroke(medianStroke);
        medianStrokeSample.setStrokePaint(medianPaint);

        boxOutliersVisible.setSelected(outliersVisible); 
        boxOutlierFilled.setSelected(outlierFilled);
        outlierFillPaintSample.setPaint(outlierFillPaint);
        boxOutlierOutlineVisible.setSelected(outlierOutlineVisible);
        outlierStrokeSample.setStroke(outlierStroke);
        outlierStrokeSample.setStrokePaint(outlierStrokePaint);

        updateOutlierMarkerShapeLabel();
    }

    @Override
    public void stateChanged(ChangeEvent evt) 
    {	
        Object source = evt.getSource();

        if(source == spinnerBoxWidth)
        {
            this.boxWidth = ((SpinnerNumberModel)spinnerBoxWidth.getModel()).getNumber().doubleValue();;
            renderer.setBoxWidth(boxWidth);  
        }	
        else if(source == spinnerWhiskerCrossBarWidth)
        {
            this.whiskerCrossBarWidth = ((SpinnerNumberModel)spinnerWhiskerCrossBarWidth.getModel()).getNumber().doubleValue();;
            renderer.setWhiskerCrossBarWidth(whiskerCrossBarWidth);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);
        Object source = evt.getSource();

        if(source == boxOutlineVisible)
        {
            this.outlineVisible = selected;
            renderer.setBoxOutlineVisible(outlineVisible);
        }
        else if(source == boxFillBox)
        {
            this.boxFilled = selected;
            renderer.setBoxFilled(boxFilled);
        }
        else if(source == boxWhiskerCrossBar)
        {
            this.whiskerCrossBarVisible = selected;
            renderer.setWhiskerCrossBarVisible(whiskerCrossBarVisible);
        }
        else if(source == boxMedianVisible)
        {
            this.medianVisible = selected;
            renderer.setMedianVisible(medianVisible);
        }
        else if(source == boxMeanVisible)
        {
            this.meanVisible = selected;
            renderer.setMeanVisible(meanVisible);
        }
        else if(source == boxMeanFilled)
        {
            this.meanFilled = selected;
            renderer.setMeanFilled(meanFilled);
        }
        else if(source == boxMeanOutlineVisible)
        {
            this.meanOutlineVisible = selected;
            renderer.setMeanOutlineVisible(meanOutlineVisible);
        }
        else if(source == boxOutliersVisible)
        {
            this.outliersVisible = selected;
            renderer.setOutliersVisible(outliersVisible);
        }
        else if(source == boxOutlierFilled)
        {
            this.outlierFilled = selected;
            renderer.setOutlierFilled(outlierFilled);
        }
        else if(source == boxOutlierOutlineVisible)
        {
            this.outlierOutlineVisible = selected;
            renderer.setOutlierOutlineVisible(outlierOutlineVisible);
        }
    }

    private void updateOutlierMarkerShapeLabel()
    {
        updateShapeLabel(labelOutlierMarkerShape, outlierFillPaint,
                outlierMarkerIndex, outlierSize);
    }

    private void updateShapeLabel(JLabel labelShape, Paint paint, int markerIndex, float markerSize)
    {
        BufferedImage img = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);       
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(paint);
        g2.fill(SHAPES[markerIndex]);
        g2.dispose();

        String shapeString = "Size: " + NumberFormat.getInstance(Locale.US).format(markerSize);
        labelShape.setText(shapeString);
        labelShape.setIcon(new ImageIcon(img));
        labelShape.repaint();
    }

    private final class OutlierMarkerStyleReceiver implements MarkerStyleReceiver {
        @Override
        public void setMarkerSize(float size)
        {
            outlierSize = size;
            renderer.setOutlierSize(size);
            updateOutlierMarkerShapeLabel();
        }

        @Override
        public void setMarkerIndex(int i) 
        {
            outlierMarkerIndex = i;
            renderer.setOutlierMarkerIndex(i);
            updateOutlierMarkerShapeLabel();
        }

        @Override
        public float getMarkerSize() 
        {
            return outlierSize;
        }

        @Override
        public Stroke getMarkerOutlineStroke() 
        {
            return null;
        }

        @Override
        public Paint getMarkerOutlinePaint() 
        {
            return outlierFillPaint;
        }

        @Override
        public int getMarkerIndex() 
        {
            return outlierMarkerIndex;
        }

        @Override
        public Paint getMarkerFillPaint() 
        {
            return outlierFillPaint;
        }

        @Override
        public boolean getDrawMarkerOutline() 
        {
            return false;
        }
    }

    private final class OutlierStrokeReceiver implements BasicStrokeReceiver {
        @Override
        public BasicStroke getStroke() 
        {
            return (BasicStroke)outlierStroke;
        }

        @Override
        public void setStroke(BasicStroke stroke) 
        {
            outlierStroke = stroke;
            outlierStrokeSample.setStroke(stroke);
            renderer.setOutlierStroke(stroke);
        }

        @Override
        public Paint getStrokePaint() 
        {
            return outlierStrokePaint;
        }

        @Override
        public void setStrokePaint(Paint paint) 
        {
            outlierStrokePaint = paint;
            outlierStrokeSample.setStrokePaint(paint);
            renderer.setOutlierStrokePaint(paint);
        }
    }

    private final class MedianStrokeReceiver implements BasicStrokeReceiver {
        @Override
        public BasicStroke getStroke() 
        {
            return (BasicStroke)medianStroke;
        }

        @Override
        public void setStroke(BasicStroke stroke) 
        {
            medianStroke = stroke;
            medianStrokeSample.setStroke(stroke);
            renderer.setMedianStroke(stroke);
        }

        @Override
        public Paint getStrokePaint() 
        {
            return medianPaint;
        }

        @Override
        public void setStrokePaint(Paint paint) 
        {
            medianPaint = paint;
            medianStrokeSample.setStrokePaint(paint);
            renderer.setMedianPaint(paint);
        }
    }

    private final class MeanOutlineStrokeReceiver implements BasicStrokeReceiver {
        @Override
        public BasicStroke getStroke() 
        {
            return (BasicStroke)meanOutlineStroke;
        }

        @Override
        public void setStroke(BasicStroke stroke) 
        {
            meanOutlineStroke = stroke;
            meanOutlineStrokeSample.setStroke(stroke);
            renderer.setMeanOutlineStroke(stroke);
        }

        @Override
        public Paint getStrokePaint() 
        {
            return meanOutlinePaint;
        }

        @Override
        public void setStrokePaint(Paint paint) 
        {
            meanOutlinePaint = paint;
            meanOutlineStrokeSample.setStrokePaint(paint);
            renderer.setMeanOutlinePaint(paint);
        }
    }

    private final class WhiskerStrokeReceiver implements BasicStrokeReceiver 
    {
        @Override
        public BasicStroke getStroke() 
        {
            return (BasicStroke)whiskerStroke;
        }

        @Override
        public void setStroke(BasicStroke stroke) 
        {
            whiskerStroke = stroke;
            whiskerStrokeSample.setStroke(stroke);
            renderer.setWhiskerStroke(stroke);
        }

        @Override
        public Paint getStrokePaint() 
        {
            return whiskerPaint;
        }

        @Override
        public void setStrokePaint(Paint paint) 
        {
            whiskerPaint = paint;
            whiskerStrokeSample.setStrokePaint(paint);
            renderer.setWhiskerPaint(paint);
        }
    }

    private final class OutlineStrokeReceiver implements BasicStrokeReceiver 
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
            renderer.setBaseOutlineStroke(stroke);
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
            renderer.setBaseOutlinePaint(paint);
        }
    }

    private final class WhiskerCrossBarStrokeReceiver implements BasicStrokeReceiver 
    {
        @Override
        public BasicStroke getStroke() 
        {
            return (BasicStroke)whiskerCrossBarStroke;
        }

        @Override
        public void setStroke(BasicStroke stroke) 
        {
            whiskerCrossBarStroke = stroke;
            whiskerCrossBarStrokeSample.setStroke(stroke);
            renderer.setWhiskerCrossBarStroke(stroke);
        }

        @Override
        public Paint getStrokePaint() 
        {
            return whiskerCrossBarPaint;
        }

        @Override
        public void setStrokePaint(Paint paint) 
        {
            whiskerCrossBarPaint = paint;
            whiskerCrossBarStrokeSample.setStrokePaint(paint);
            renderer.setWhiskerCrossBarPaint(paint);
        }
    }
}
