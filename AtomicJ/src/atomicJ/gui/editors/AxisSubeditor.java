
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


import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import org.jfree.chart.axis.Axis;
import org.jfree.ui.*;

import atomicJ.gui.*;
import atomicJ.gui.FontDisplayField;
import atomicJ.utilities.SerializationUtilities;

public class AxisSubeditor <E extends Axis> implements ActionListener, PropertyChangeListener, ItemListener,ChangeListener, Subeditor 
{
    public static final String SELECT_AXIS_LINE_STROKE_COMMAND = "SELECT_AXIS_LINE_STROKE_COMMAND";

    public static final String TOP_LOCATION = "Top";
    public static final String BOTTOM_LOCATION = "Bottom";
    public static final String LEFT_LOCATION = "Left";
    public static final String RIGHT_LOCATION = "Right";

    private static final String SELECT_LABEL_FONT_COMMAND = "SELECT_LABEL_FONT_COMMAND";
    private static final String SELECT_LABEL_PAINT_COMMAND = "SELECT_LABEL_PAINT_COMMAND";
    private static final String TICK_MARK_STROKE_COMMAND = "SELECT_TICK_MARK_STROKE_COMMAND";

    private static final String SELECT_TICK_LABEL_FONT_COMMAND = "SELECT_TICK_LABEL_FONT_COMMAND";
    private static final String SELECT_TICK_LABEL_PAINT_COMMAND = "SELECT_TICK_LABEL_PAINT_COMMAND";

    private final Preferences pref;

    //generalParameters 
    private boolean visible;

    //label parameters
    private String label;
    private Font labelFont;
    private Paint labelPaint;

    //ticks parameters
    private boolean tickMarksVisible;
    private float tickMarkLengthInside;
    private float tickMarkLengthOutside;
    private Paint tickMarkPaint;
    private Stroke tickMarkStroke;

    private boolean tickLabelsVisible;
    private Paint tickLabelPaint;
    private Font tickLabelFont;

    //axis line parameters
    private boolean axisLineVisible;
    private Paint axisLinePaint;
    private Stroke axisLineStroke;

    //INITIAL general parameters

    private final boolean initVisible;

    private final String initLabel;
    private final boolean initShowAxisLine;
    private final Paint initAxisLinePaint;
    private final Stroke initAxisLineStroke;
    private final Font initLabelFont;
    private final Font initTickLabelFont;
    private final Paint initLabelPaint;
    private final Paint initTickLabelPaint;

    private final boolean initShowTickLabels;
    private final boolean initShowTickMarks;
    private final float initTickMarkLengthInside;
    private final float initTickMarkLengthOutside;
    private final Paint initTickMarkPaint;
    private final Stroke initTickMarkStroke;

    //general panel
    private final JCheckBox boxVisible = new JCheckBox();

    //label panel
    private final JFormattedTextField fieldLabelText = new JFormattedTextField(new DefaultFormatter());;
    private final FontDisplayField fieldLabelFont;
    private final PaintSample labelPaintSample;

    //ticks panel
    private final JSpinner spinnerTickMarkLengthInside;
    private final JSpinner spinnerTickMarkLengthOutside;
    private final FontDisplayField fieldTickLabelFont;
    private final PaintSample tickLabelPaintSample;
    private final StraightStrokeSample tickMarkStrokeSample = new StraightStrokeSample();

    private final JCheckBox boxShowTickMarks = new JCheckBox("Show");
    private final JCheckBox boxShowTickLabels = new JCheckBox();
    private StrokeChooser tickMarkStrokeChooser;

    //line panel
    private final JCheckBox boxShowAxisLine = new JCheckBox();
    private final StraightStrokeSample lineStrokeSample = new StraightStrokeSample();
    private StrokeChooser axisLineStrokeChooser;

    private FontChooserDialog fontChooserDialog;

    private final JTabbedPane mainPane;
    private SubPanel generalPanelContent;
    private SubPanel labelPanelContent;
    private SubPanel ticksPanelContent;
    private SubPanel axisLinePanelContent;

    private final E axis;  
    private final List<Axis> typeBoundedAxes = new ArrayList<>();
    private final List<AxisSubeditor<? extends Axis>> chartBoundedAxesSubeditors = new ArrayList<>();

    private boolean boundChartAxes = false;

    private final SubPanel editorPanel = new SubPanel();

    public AxisSubeditor(E axis, AxisType axisType, Preferences pref) 
    {
        this.axis = axis;
        this.pref = pref;

        this.initVisible = axis.isVisible();

        this.initLabel = axis.getLabel();
        this.initLabelFont = axis.getLabelFont();
        this.initLabelPaint = axis.getLabelPaint();

        this.initShowAxisLine = axis.isAxisLineVisible();
        this.initAxisLinePaint = axis.getAxisLinePaint();
        this.initAxisLineStroke = axis.getAxisLineStroke();        

        this.initTickLabelFont = axis.getTickLabelFont();
        this.initTickLabelPaint = axis.getTickLabelPaint();
        this.initShowTickLabels = axis.isTickLabelsVisible();
        this.initShowTickMarks = axis.isTickMarksVisible();
        this.initTickMarkLengthInside = axis.getTickMarkInsideLength();
        this.initTickMarkLengthOutside = axis.getTickMarkOutsideLength();
        this.initTickMarkPaint = axis.getTickMarkPaint();
        this.initTickMarkStroke = axis.getTickMarkStroke();

        setParametersToInitial();

        this.boxVisible.setSelected(initVisible);

        this.boxShowAxisLine.setSelected(initShowAxisLine);

        this.lineStrokeSample.setStroke(initAxisLineStroke);
        this.lineStrokeSample.setStrokePaint(initAxisLinePaint);

        this.labelPaintSample = new PaintSample(initLabelPaint);
        this.tickLabelPaintSample = new PaintSample(initTickLabelPaint);
        this.tickMarkStrokeSample.setStroke(initTickMarkStroke);
        this.tickMarkStrokeSample.setStrokePaint(initTickMarkPaint);

        this.spinnerTickMarkLengthInside = new JSpinner(new SpinnerNumberModel(initTickMarkLengthInside,0,100,0.05));        
        this.spinnerTickMarkLengthOutside = new JSpinner(new SpinnerNumberModel(initTickMarkLengthOutside,0,100,0.05));

        fieldLabelText.setValue(initLabel);
        DefaultFormatter formatter = (DefaultFormatter)fieldLabelText.getFormatter();
        formatter.setOverwriteMode(false);
        formatter.setCommitsOnValidEdit(true);

        fieldLabelFont = new FontDisplayField(initLabelFont);
        boxShowTickLabels.setSelected(initShowTickLabels);
        boxShowTickMarks.setSelected(initShowTickMarks);

        fieldTickLabelFont = new FontDisplayField(initTickLabelFont);

        this.mainPane = new JTabbedPane();
        mainPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));  

        //general panel
        JPanel generalPanel = buildGeneralPanel();
        mainPane.add("General", generalPanel);

        //label panel

        JPanel labelPanel = buildLabelPanel();
        mainPane.add("Label",labelPanel);

        // ticks

        JPanel ticksPanel = buildTicksPanel();     
        mainPane.add("Ticks", ticksPanel);

        // line

        JPanel linePanel = buildAxisLinePanel();
        mainPane.add("Axis line",linePanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        mainPanel.add(mainPane, BorderLayout.NORTH);

        editorPanel.setLayout(new BorderLayout());
        editorPanel.add(mainPanel, BorderLayout.CENTER);

        initChangeListener();
        initItemListener();
    }


    protected E getAxis()
    {
        return axis;
    }

    public List<Axis> getTypeBoundedAxes()
    {
        return typeBoundedAxes;
    }

    protected JTabbedPane getMainPane()
    {
        return mainPane;
    }

    protected SubPanel getGeneralPanelContent()
    {
        return generalPanelContent;
    }

    protected SubPanel getLabelPanelContent()
    {
        return labelPanelContent;
    }

    protected SubPanel getTicksPanelContent()
    {
        return ticksPanelContent;
    }

    public SubPanel getAxisLinePanelContent()
    {
        return axisLinePanelContent;
    }

    public void addBoundedAxis(Axis boundedAxis)
    {
        this.typeBoundedAxes.add(boundedAxis);
    }

    public void addBoundedAxes(List<Axis> boundedAxes)
    {        
        this.typeBoundedAxes.addAll(boundedAxes);
    }

    public void addBoundedSubeditor(AxisSubeditor<? extends Axis> boundedSubeditor)
    {
        this.chartBoundedAxesSubeditors.add(boundedSubeditor);
    }

    private void initChangeListener()
    {
        spinnerTickMarkLengthInside.addChangeListener(this); 
        spinnerTickMarkLengthOutside.addChangeListener(this);
    }

    private void initItemListener()
    {
        boxVisible.addItemListener(this);

        boxShowAxisLine.addItemListener(this);
        boxShowTickMarks.addItemListener(this);
        boxShowTickLabels.addItemListener(this);        
    }

    private void setParametersToInitial()
    {
        this.visible = initVisible;

        this.label = initLabel;
        this.axisLineVisible = initShowAxisLine;
        this.axisLinePaint = initAxisLinePaint;
        this.axisLineStroke = initAxisLineStroke ;
        this.labelFont = initLabelFont;
        this.tickLabelFont = initTickLabelFont;
        this.labelPaint = initLabelPaint;
        this.tickLabelPaint = initTickLabelPaint;
        this.tickLabelsVisible = initShowTickLabels;
        this.tickMarksVisible = initShowTickMarks;
        this.tickMarkLengthInside = initTickMarkLengthInside;
        this.tickMarkLengthOutside = initTickMarkLengthOutside;
        this.tickMarkPaint = initTickMarkPaint;
        this.tickMarkStroke = initTickMarkStroke;
    }

    public boolean isBoundChartAxes()
    {
        return boundChartAxes;
    }

    public void setBoundChartAxes(boolean boundChartAxes)
    {
        this.boundChartAxes = boundChartAxes;
    }

    private JPanel buildGeneralPanel()
    {
        JPanel outerPanel = new JPanel(new BorderLayout());

        this.generalPanelContent = new SubPanel();

        generalPanelContent.addComponent(new JLabel("Show"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        generalPanelContent.addComponent(boxVisible, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        outerPanel.add(generalPanelContent, BorderLayout.NORTH);       
        outerPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return outerPanel;
    }

    private JPanel buildLabelPanel()
    {
        JPanel outerPanel = new JPanel(new BorderLayout());

        fieldLabelText.addPropertyChangeListener("value", this);

        JButton buttonSelectLabelFont = new JButton("Select");
        buttonSelectLabelFont.setActionCommand(SELECT_LABEL_FONT_COMMAND);
        buttonSelectLabelFont.addActionListener(this);

        JButton buttonSelectLabelPaint = new JButton("Select");
        buttonSelectLabelPaint.setActionCommand(SELECT_LABEL_PAINT_COMMAND);
        buttonSelectLabelPaint.addActionListener(this);

        this.labelPanelContent = new SubPanel();

        labelPanelContent.addComponent(new JLabel("Label"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        labelPanelContent.addComponent(fieldLabelText, 1, 0, 4, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        labelPanelContent.addComponent(new JLabel("Font"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        labelPanelContent.addComponent(fieldLabelFont, 1, 1, 4, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        labelPanelContent.addComponent(buttonSelectLabelFont, 5, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     

        labelPanelContent.addComponent(new JLabel("Color"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        labelPanelContent.addComponent(labelPaintSample, 1, 2, 4, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        labelPanelContent.addComponent(buttonSelectLabelPaint, 5, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     

        outerPanel.add(labelPanelContent, BorderLayout.NORTH);
        outerPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return outerPanel;
    }

    private JPanel buildTicksPanel()
    {
        JPanel outerPanel = new JPanel(new BorderLayout());

        JButton buttonSelectTickLabelFont = new JButton("Select");
        buttonSelectTickLabelFont.setActionCommand(SELECT_TICK_LABEL_FONT_COMMAND);
        buttonSelectTickLabelFont.addActionListener(this);

        JButton buttonSelectTickLabelPaint = new JButton("Select");
        buttonSelectTickLabelPaint.setActionCommand(SELECT_TICK_LABEL_PAINT_COMMAND);
        buttonSelectTickLabelPaint.addActionListener(this);        

        JButton buttonSelectTickMarkStroke = new JButton("Select");
        buttonSelectTickMarkStroke.setActionCommand(TICK_MARK_STROKE_COMMAND);
        buttonSelectTickMarkStroke.addActionListener(this);   


        this.ticksPanelContent = new SubPanel();

        ticksPanelContent.addComponent(new JLabel("Show labels"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        ticksPanelContent.addComponent(boxShowTickLabels, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        ticksPanelContent.addComponent(new JLabel("Label font"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        ticksPanelContent.addComponent(fieldTickLabelFont, 1, 2, 6, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        ticksPanelContent.addComponent(buttonSelectTickLabelFont, 8, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     

        ticksPanelContent.addComponent(new JLabel("Label color"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        ticksPanelContent.addComponent(tickLabelPaintSample, 1, 3, 6, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        ticksPanelContent.addComponent(buttonSelectTickLabelPaint, 8, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     

        SubPanel panelTickLength = new SubPanel();

        panelTickLength.addComponent(spinnerTickMarkLengthInside, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, .05, 1);        
        panelTickLength.addComponent(new JLabel("inside"), 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        panelTickLength.addComponent(spinnerTickMarkLengthOutside, 3, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, .05, 1);
        panelTickLength.addComponent(new JLabel("outside"), 4, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);

        ticksPanelContent.addComponent(new JLabel("Markers"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        ticksPanelContent.addComponent(boxShowTickMarks, 1, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        ticksPanelContent.addComponent(new JLabel("Length"), 2, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, .05, 1);
        ticksPanelContent.addComponent(panelTickLength, 3, 4, 4, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);

        ticksPanelContent.addComponent(new JLabel("Stroke"), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        ticksPanelContent.addComponent(tickMarkStrokeSample, 1, 5, 6, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        ticksPanelContent.addComponent(buttonSelectTickMarkStroke, 8, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     

        outerPanel.add(ticksPanelContent, BorderLayout.NORTH);     
        outerPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return outerPanel;
    }

    private JPanel buildAxisLinePanel()
    {
        JPanel outerPanel = new JPanel(new BorderLayout());

        JButton buttonSelectLineStroke = new JButton("Select");
        buttonSelectLineStroke.setActionCommand(SELECT_AXIS_LINE_STROKE_COMMAND);
        buttonSelectLineStroke.addActionListener(this);

        this.axisLinePanelContent = new SubPanel();

        axisLinePanelContent.addComponent(new JLabel("Show"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        axisLinePanelContent.addComponent(boxShowAxisLine, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        axisLinePanelContent.addComponent(new JLabel("Stroke"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        axisLinePanelContent.addComponent(lineStrokeSample, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        axisLinePanelContent.addComponent(buttonSelectLineStroke, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);     

        outerPanel.add(axisLinePanelContent, BorderLayout.NORTH);       
        outerPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return outerPanel;
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();

        if (command.equals(SELECT_AXIS_LINE_STROKE_COMMAND)) 
        {
            attemptLineStrokeSelection();
        }
        else if (command.equals(SELECT_LABEL_FONT_COMMAND)) 
        {
            attemptLabelFontSelection();
        }
        else if (command.equals(SELECT_LABEL_PAINT_COMMAND)) 
        {
            attemptModifyLabelPaint();
        }
        else if (command.equals(SELECT_TICK_LABEL_FONT_COMMAND)) 
        {
            attemptTickLabelFontSelection();
        }
        else if(command.equals(SELECT_TICK_LABEL_PAINT_COMMAND))
        {
            attemptModifyTickLabelPaint();
        }
        else if (command.equals(TICK_MARK_STROKE_COMMAND)) 
        {
            attemptTickMarkStrokeSelection();
        }  
    }

    private void attemptModifyLabelPaint() 
    {
        Color defaultColor = (labelPaint instanceof Color ? (Color) labelPaint : Color.black);
        Color c = JColorChooser.showDialog(editorPanel, "Axis label color", defaultColor);
        if (c != null) 
        {
            setLabelPaint(c);
        }
    }

    protected void setLabel(String label)
    {
        this.label = label;
        fieldLabelText.setValue(label);
    }

    public void setLabelPaint(Color labelPaintNew)
    {
        if(!this.labelPaint.equals(labelPaintNew))
        {
            labelPaint = labelPaintNew;
            labelPaintSample.setPaint(labelPaint);
            axis.setLabelPaint(labelPaint);

            if(boundChartAxes)
            {
                for(AxisSubeditor<? extends Axis> subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setLabelPaint(labelPaintNew);
                }
            }         
        }
    }

    private void attemptModifyTickLabelPaint() 
    {
        Color defaultColor = (tickLabelPaint instanceof Color ? (Color) tickLabelPaint : Color.black);
        Color c = JColorChooser.showDialog(editorPanel, "Tick label color", defaultColor);
        if (c != null) 
        {
            setTickLabelPaint(c);
        }
    }

    public void setTickLabelPaint(Color labelPaintNew)
    {
        if(!this.tickLabelPaint.equals(labelPaintNew))
        {
            tickLabelPaint = labelPaintNew;
            tickLabelPaintSample.setPaint(tickLabelPaint);
            axis.setTickLabelPaint(tickLabelPaint);

            if(boundChartAxes)
            {
                for(AxisSubeditor<? extends Axis> subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setTickLabelPaint(labelPaintNew);
                }
            }
        }
    }

    private void attemptTickMarkStrokeSelection() 
    {
        if(tickMarkStrokeChooser == null)
        {
            tickMarkStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(editorPanel), new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke)tickMarkStroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    setTickMarkStroke(stroke);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return tickMarkPaint;
                }

                @Override
                public void setStrokePaint(Paint paint) 
                {
                    setTickMarkStrokePaint(paint);
                }           
            }
                    );                         
        }
        tickMarkStrokeChooser.showDialog();
    }

    public void setTickMarkStroke(BasicStroke strokeNew)
    {
        if(!this.tickMarkStroke.equals(strokeNew))
        {
            tickMarkStroke = strokeNew;
            tickMarkStrokeSample.setStroke(strokeNew);
            axis.setTickMarkStroke(strokeNew);

            if(boundChartAxes)
            {
                for(AxisSubeditor<? extends Axis> subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setTickMarkStroke(strokeNew);
                }
            }
        }       
    }

    public void setTickMarkStrokePaint(Paint paintNew)
    {
        if(!this.tickMarkPaint.equals(paintNew))
        {
            tickMarkPaint = paintNew;
            tickMarkStrokeSample.setStrokePaint(paintNew);
            axis.setTickMarkPaint(tickMarkPaint);

            if(boundChartAxes)
            {
                for(AxisSubeditor<? extends Axis> subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setTickMarkStrokePaint(paintNew);
                }
            }
        }    
    }

    private void attemptLineStrokeSelection() 
    {
        if(axisLineStrokeChooser == null)
        {
            this.axisLineStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(editorPanel), new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke)axisLineStroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    setAxisLineStroke(stroke);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return axisLinePaint;
                }

                @Override
                public void setStrokePaint(Paint paint) 
                {
                    setAxisLinePaint(paint);
                }           
            }
                    );                            
        }
        axisLineStrokeChooser.showDialog();
    }

    public void setAxisLineStroke(BasicStroke strokeNew)
    {
        if(this.axisLineStroke != strokeNew)
        {
            this.axisLineStroke = strokeNew;
            lineStrokeSample.setStroke(strokeNew);
            axis.setAxisLineStroke(axisLineStroke);

            if(boundChartAxes)
            {
                for(AxisSubeditor<? extends Axis> subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setAxisLineStroke(strokeNew);
                }
            }
        }
    }

    public void setAxisLinePaint(Paint paintNew)
    {
        if(this.axisLinePaint != paintNew)
        {
            this.axisLinePaint = paintNew;
            lineStrokeSample.setStrokePaint(paintNew);
            axis.setAxisLinePaint(axisLinePaint);

            if(boundChartAxes)
            {
                for(AxisSubeditor<? extends Axis> subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setAxisLinePaint(paintNew);
                }
            }
        }
    }

    private void attemptLabelFontSelection() 
    {
        if( fontChooserDialog == null)
        {
            this.fontChooserDialog = new FontChooserDialog(SwingUtilities.getWindowAncestor(editorPanel), "Font selection");
        }

        this.fontChooserDialog.showDialog(new FontReceiver() 
        {
            @Override
            public void setFont(Font newFont) 
            { 
                setLabelFont(newFont);
            }

            @Override
            public Font getFont()
            {
                return labelFont;
            }
        });
    }

    public void setLabelFont(Font fontNew)
    {
        if(this.labelFont != fontNew)
        {
            this.labelFont = fontNew;
            axis.setLabelFont(labelFont);
            fieldLabelFont.setDisplayFont(labelFont);

            if(boundChartAxes)
            {
                for(AxisSubeditor<? extends Axis> subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setLabelFont(fontNew);
                }
            }
        }
    }

    public void attemptTickLabelFontSelection() 
    {
        if( fontChooserDialog == null)
        {
            this.fontChooserDialog = new FontChooserDialog(SwingUtilities.getWindowAncestor(editorPanel), "Font selection");
        }

        this.fontChooserDialog.showDialog(new FontReceiver() 
        {

            @Override
            public void setFont(Font newFont) 
            { 
                setTickLabelFont(newFont);
            }

            @Override
            public Font getFont()
            {
                return tickLabelFont;
            }
        });
    }

    public void setTickLabelFont(Font fontNew)
    {
        if(this.tickLabelFont != fontNew)
        {
            this.tickLabelFont = fontNew;
            fieldTickLabelFont.setDisplayFont(tickLabelFont);
            axis.setTickLabelFont(tickLabelFont);

            if(boundChartAxes)
            {
                for(AxisSubeditor<? extends Axis> subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setTickLabelFont(fontNew);
                }
            }
        }
    }

    @Override
    public void resetToDefaults() 
    {
        this.visible = pref.getBoolean(PreferredAxisStyle.AXIS_VISIBLE, true);

        this.axisLinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredAxisStyle.AXIS_LINE_PAINT, Color.black);
        this.axisLineStroke = SerializationUtilities.getStroke(pref, PreferredAxisStyle.AXIS_LINE_STROKE, new BasicStroke(1.0f));

        this.labelPaint = (Paint)SerializationUtilities.getSerializableObject(pref, LABEL_PAINT, Color.black);
        this.labelFont = (Font)SerializationUtilities.getSerializableObject(pref, LABEL_FONT, new Font("SansSerif", Font.BOLD, 14));
        this.tickLabelFont = (Font)SerializationUtilities.getSerializableObject(pref, PreferredAxisStyle.TICK_LABEL_FONT, new Font("SansSerif", Font.BOLD, 14));
        this.tickLabelPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredAxisStyle.TICK_LABEL_PAINT, Color.black);  

        this.axisLineVisible = pref.getBoolean(PreferredAxisStyle.AXIS_LINE_VISIBLE, true);
        this.tickLabelsVisible = pref.getBoolean(PreferredAxisStyle.TICK_LABELS_VISIBLE, true);

        this.tickMarksVisible = pref.getBoolean(PreferredAxisStyle.TICK_MARKS_VISIBLE, true);
        this.tickMarkPaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredAxisStyle.TICK_MARK_PAINT, Color.black);
        this.tickMarkStroke = SerializationUtilities.getStroke(pref, PreferredAxisStyle.TICK_MARK_STROKE, new BasicStroke(1.0f));

        this.tickMarkLengthInside = pref.getFloat(PreferredAxisStyle.TICK_MARK_LENGTH_OUTSIDE, 0f);
        this.tickMarkLengthOutside = pref.getFloat(PreferredAxisStyle.TICK_MARK_LENGTH_OUTSIDE, 0.5f);             

        resetAxis(axis);
        resetEditor();
    }

    @Override
    public void saveAsDefaults() 
    {   
        try 
        {
            SerializationUtilities.putSerializableObject(pref, LABEL_PAINT, labelPaint);
            SerializationUtilities.putSerializableObject(pref, LABEL_FONT, labelFont);
            SerializationUtilities.putSerializableObject(pref, PreferredAxisStyle.TICK_LABEL_FONT, tickLabelFont);
            SerializationUtilities.putSerializableObject(pref, PreferredAxisStyle.TICK_LABEL_PAINT, tickLabelPaint);
            SerializationUtilities.putSerializableObject(pref, PreferredAxisStyle.AXIS_LINE_PAINT, axisLinePaint);
            SerializationUtilities.putStroke(pref, PreferredAxisStyle.AXIS_LINE_STROKE, axisLineStroke);
            SerializationUtilities.putSerializableObject(pref, PreferredAxisStyle.TICK_MARK_PAINT, tickMarkPaint);
            SerializationUtilities.putStroke(pref, PreferredAxisStyle.TICK_MARK_STROKE, tickMarkStroke);
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }

        pref.putBoolean(PreferredAxisStyle.AXIS_VISIBLE, visible);

        pref.putBoolean(PreferredAxisStyle.AXIS_LINE_VISIBLE, axisLineVisible);
        pref.putBoolean(PreferredAxisStyle.TICK_LABELS_VISIBLE, tickLabelsVisible);
        pref.putBoolean(PreferredAxisStyle.TICK_MARKS_VISIBLE, tickMarksVisible);  
        pref.putFloat(PreferredAxisStyle.TICK_MARK_LENGTH_INSIDE, tickMarkLengthInside);
        pref.putFloat(PreferredAxisStyle.TICK_MARK_LENGTH_OUTSIDE, tickMarkLengthOutside);

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
        for(Axis a: typeBoundedAxes)
        {           
            resetBoundedAxis(a);
        }   
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();
        resetAxis(axis);
        resetEditor();
    }

    @Override
    public Component getEditionComponent() 
    {
        return editorPanel;
    }

    public void setEmptyBorderForEditionComponent(int top, int left, int bottom, int right)
    {
        editorPanel.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return typeBoundedAxes.size() > 1;
    }

    protected void resetAxis(E a)
    {   
        basicResetAxis(a);
    }

    protected void resetBoundedAxis(Axis a)
    {   
        basicResetAxis(a);
    }

    protected void basicResetAxis(Axis a)
    {   
        a.setVisible(visible);

        a.setLabel(label);
        a.setLabelFont(labelFont);
        a.setLabelPaint(labelPaint);
        a.setTickLabelFont(tickLabelFont);
        a.setTickLabelPaint(tickLabelPaint);
        a.setTickLabelsVisible(tickLabelsVisible);
        a.setTickMarksVisible(tickMarksVisible);
        a.setTickMarkInsideLength(tickMarkLengthInside);
        a.setTickMarkOutsideLength(tickMarkLengthOutside);
        a.setTickMarkPaint(tickMarkPaint);
        a.setTickMarkStroke(tickMarkStroke);

        a.setAxisLineVisible(axisLineVisible);
        a.setAxisLinePaint(axisLinePaint);
        a.setAxisLineStroke(axisLineStroke);
    }

    protected void resetEditor()
    {
        boxVisible.setSelected(visible);

        boxShowTickLabels.setSelected(tickLabelsVisible);

        boxShowTickMarks.setSelected(tickMarksVisible); 
        spinnerTickMarkLengthInside.setValue(tickMarkLengthInside);
        spinnerTickMarkLengthOutside.setValue(tickMarkLengthOutside);
        tickMarkStrokeSample.setStroke(tickMarkStroke);
        tickMarkStrokeSample.setStrokePaint(tickMarkPaint);

        boxShowAxisLine.setSelected(axisLineVisible);
        lineStrokeSample.setStroke(axisLineStroke);
        lineStrokeSample.setStrokePaint(axisLinePaint);

        fieldLabelText.setValue(initLabel);
        fieldLabelFont.setDisplayFont(labelFont);
        labelPaintSample.setPaint(labelPaint);

        fieldTickLabelFont.setDisplayFont(tickLabelFont);
        tickLabelPaintSample.setPaint(tickLabelPaint);
    }

    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object source = evt.getSource();
        if(source == spinnerTickMarkLengthInside)
        {
            float tickMarkLengthInside = ((SpinnerNumberModel)spinnerTickMarkLengthInside.getModel()).getNumber().floatValue();
            setTickMarkLengthInside(tickMarkLengthInside);
        }
        else if(source == spinnerTickMarkLengthOutside)
        {
            float tickMarkLengthOutside = ((SpinnerNumberModel)spinnerTickMarkLengthOutside.getModel()).getNumber().floatValue();
            setTickMarkLengthOutside(tickMarkLengthOutside);  
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        Object source = evt.getSource();
        if(source == fieldLabelText)
        {
            label = evt.getNewValue().toString();
            axis.setLabel(label);
        }   
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        Object source = evt.getSource();

        boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);

        if(source == boxVisible)
        {
            setAxisVisible(selected);
        }
        if(source == boxShowAxisLine)
        {
            setAxisLineVisible(selected);
        }
        if(source == boxShowTickMarks)
        {
            setTickMarksVisible(selected);
        }
        else if(source == boxShowTickLabels)
        {
            setTickLabelVisible(selected);
        }                
    }

    private void setAxisVisible(boolean visibleNew)
    {
        if(this.visible != visibleNew)
        {
            visible = visibleNew;
            axis.setVisible(visible);
            boxVisible.setSelected(visibleNew);

            if(boundChartAxes)
            {
                for(AxisSubeditor<? extends Axis> subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setAxisVisible(visibleNew);
                }
            }
        }

    }

    private void setAxisLineVisible(boolean axisLineVisible)
    {
        if(this.axisLineVisible != axisLineVisible)
        {
            this.axisLineVisible = axisLineVisible;
            axis.setAxisLineVisible(axisLineVisible);
            boxShowAxisLine.setSelected(axisLineVisible);

            if(boundChartAxes)
            {
                for(AxisSubeditor<? extends Axis> subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setAxisLineVisible(axisLineVisible);
                }
            }
        }
    }

    private void setTickMarksVisible(boolean tickMarksVisible)
    {
        if(this.tickMarksVisible != tickMarksVisible)
        {
            this.tickMarksVisible = tickMarksVisible;
            axis.setTickMarksVisible(tickMarksVisible);
            boxShowTickMarks.setSelected(tickMarksVisible);

            if(boundChartAxes)
            {
                for(AxisSubeditor<? extends Axis> subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setTickMarksVisible(tickMarksVisible);
                }
            }
        }
    }

    private void setTickLabelVisible(boolean tickLabelsVisible)
    {
        if(this.tickLabelsVisible != tickLabelsVisible)
        {
            this.tickLabelsVisible = tickLabelsVisible;
            axis.setTickLabelsVisible(tickLabelsVisible);
            boxShowTickLabels.setSelected(tickLabelsVisible);

            if(boundChartAxes)
            {
                for(AxisSubeditor<? extends Axis> subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setTickLabelVisible(tickLabelsVisible);
                }
            }
        }   
    }



    private void setTickMarkLengthInside(float lengthInside)
    {
        if(this.tickMarkLengthInside != lengthInside)
        {
            this.tickMarkLengthInside = lengthInside;
            axis.setTickMarkInsideLength(tickMarkLengthInside);  
            spinnerTickMarkLengthInside.setValue(tickMarkLengthInside);

            if(boundChartAxes)
            {
                for(AxisSubeditor<? extends Axis> subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setTickMarkLengthInside(lengthInside);
                }
            }
        }
    }

    private void setTickMarkLengthOutside(float lengthOutside)
    {
        if(this.tickMarkLengthOutside != lengthOutside)
        {
            this.tickMarkLengthOutside = lengthOutside;
            axis.setTickMarkOutsideLength(tickMarkLengthOutside);  
            spinnerTickMarkLengthOutside.setValue(tickMarkLengthOutside);

            if(boundChartAxes)
            {
                for(AxisSubeditor<? extends Axis> subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setTickMarkLengthOutside(lengthOutside);
                }
            }
        }
    }

    @Override
    public String getSubeditorName() 
    {
        return null;
    }

    @Override
    public void setNameBorder(boolean b) 
    {
    }
}
