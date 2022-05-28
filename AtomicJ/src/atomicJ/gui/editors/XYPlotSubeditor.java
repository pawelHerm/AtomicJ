
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


import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;


import org.jfree.chart.plot.*;

import atomicJ.gui.BasicStrokeReceiver;
import atomicJ.gui.PaintReceiver;
import atomicJ.gui.PlotOrientationWrapper;
import atomicJ.gui.PreferredBasePlotStyle;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;
import atomicJ.utilities.Validation;

public class XYPlotSubeditor extends PlotSubeditor<XYPlot> implements ActionListener, Subeditor, PaintReceiver 
{
    private final Paint initDomainGridlinePaint;
    private final Paint initRangeGridlinePaint;

    private final Stroke initDomainGridlineStroke;
    private final Stroke initRangeGridlineStroke;

    private final boolean initDomainGridlinesVisible;
    private final boolean initRangeGridlinesVisible;

    private final PlotOrientation initPlotOrientation;

    private Paint domainGridlinePaint;
    private Paint rangeGridlinePaint;

    private Stroke domainGridlineStroke;
    private Stroke rangeGridlineStroke;

    private boolean domainGridlinesVisible;
    private boolean rangeGridlinesVisible;    

    private PlotOrientation plotOrientation;

    private final StraightStrokeSample domainGridlineStrokeSample;
    private final StraightStrokeSample rangeGridlineStrokeSample;    

    private final Action editDomainAction = new EditDomainGridlineStroke();
    private final Action editRangeAction = new EditRangeGridlineStroke();

    private final JCheckBox boxDrawDomainGridline = new JCheckBox();
    private final JCheckBox boxDrawRangeGridline = new JCheckBox();

    private final JComboBox<PlotOrientationWrapper> comboOrientation= new JComboBox<>(PlotOrientationWrapper.values());

    private StrokeChooser domainGridlineStrokeChooser;
    private StrokeChooser rangeGridlineStrokeChooser;

    private final XYPlot plot;

    public XYPlotSubeditor(XYPlot plot, List<? extends XYPlot> boundedPlots, Preferences pref) 
    {    
        super(plot, boundedPlots, pref);
        //sets initial parameters
        this.plot = Validation.requireNonNullParameterName(plot, "plot");

        this.initDomainGridlinePaint = plot.getDomainGridlinePaint();
        this.initRangeGridlinePaint = plot.getRangeGridlinePaint();

        this.initDomainGridlineStroke = plot.getDomainGridlineStroke();
        this.initRangeGridlineStroke = plot.getRangeGridlineStroke();

        this.initDomainGridlinesVisible = plot.isDomainGridlinesVisible();
        this.initRangeGridlinesVisible = plot.isRangeGridlinesVisible();

        this.initPlotOrientation = plot.getOrientation();

        setParametersToInitial();

        //builds components and set editor 

        this.domainGridlineStrokeSample = new StraightStrokeSample(initDomainGridlineStroke);
        domainGridlineStrokeSample.setStrokePaint(initDomainGridlinePaint);

        this.rangeGridlineStrokeSample = new StraightStrokeSample(initRangeGridlineStroke);
        rangeGridlineStrokeSample.setStrokePaint(initRangeGridlinePaint);

        boxDrawDomainGridline.setSelected(initDomainGridlinesVisible);
        boxDrawRangeGridline.setSelected(initRangeGridlinesVisible);

        comboOrientation.setSelectedItem(PlotOrientationWrapper.getWrapper(initPlotOrientation));

        buildMainPanel();       

        initItemListeners();
    }

    private void setParametersToInitial()
    {
        this.domainGridlinePaint = initDomainGridlinePaint;
        this.rangeGridlinePaint = initRangeGridlinePaint;

        this.domainGridlineStroke = initDomainGridlineStroke;
        this.rangeGridlineStroke = initRangeGridlineStroke;

        this.domainGridlinesVisible = initDomainGridlinesVisible;
        this.rangeGridlinesVisible = initRangeGridlinesVisible;

        this.plotOrientation = initPlotOrientation;
    }

    private void initItemListeners()
    {
        boxDrawDomainGridline.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                domainGridlinesVisible = selected;
                plot.setDomainGridlinesVisible(domainGridlinesVisible);
            }
        });
        boxDrawRangeGridline.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                rangeGridlinesVisible = selected;
                plot.setRangeGridlinesVisible(rangeGridlinesVisible);
            }
        });
        comboOrientation.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                plotOrientation = ((PlotOrientationWrapper)comboOrientation.getSelectedItem()).getPlotOrientation();
                plot.setOrientation(plotOrientation);                
            }
        });
    }

    private void attemptDomainGridlineStrokeSelection() 
    {
        if(domainGridlineStrokeChooser == null)
        {
            domainGridlineStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(getEditorPanel()), new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke) domainGridlineStroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    domainGridlineStroke = stroke;
                    domainGridlineStrokeSample.setStroke(stroke);
                    plot.setDomainGridlineStroke(stroke);
                }
                @Override
                public void setStrokePaint(Paint paint)
                {
                    domainGridlinePaint = paint;
                    domainGridlineStrokeSample.setStrokePaint(paint);
                    plot.setDomainGridlinePaint(paint);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return domainGridlinePaint;
                }     	
            }
                    );
        }        
        domainGridlineStrokeChooser.showDialog();
    }


    private void attemptRangeGridlineStrokeSelection() 
    {
        if(rangeGridlineStrokeChooser == null)
        {
            rangeGridlineStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(getEditorPanel()), new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke)rangeGridlineStroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    rangeGridlineStroke = stroke;
                    rangeGridlineStrokeSample.setStroke(stroke);
                    plot.setRangeGridlineStroke(stroke);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return rangeGridlinePaint;
                }

                @Override
                public void setStrokePaint(Paint paint) 
                {
                    rangeGridlinePaint = paint;
                    rangeGridlineStrokeSample.setStrokePaint(paint);
                    plot.setRangeGridlinePaint(paint);
                }       	
            }
                    );
        }

        rangeGridlineStrokeChooser.showDialog();
    }

    @Override
    protected void resetEditor()
    {
        super.resetEditor();

        domainGridlineStrokeSample.setStroke(domainGridlineStroke);
        domainGridlineStrokeSample.setStrokePaint(domainGridlinePaint);

        rangeGridlineStrokeSample.setStroke(rangeGridlineStroke);
        rangeGridlineStrokeSample.setStrokePaint(rangeGridlinePaint);

        boxDrawDomainGridline.setSelected(domainGridlinesVisible);
        boxDrawRangeGridline.setSelected(rangeGridlinesVisible);
        comboOrientation.setSelectedItem(PlotOrientationWrapper.getWrapper(plotOrientation));
    }

    @Override
    protected void resetPlot(XYPlot p)
    {    	
        super.resetPlot(p);

        //only the last method call should invoke notification mechanism

        p.setDomainGridlineStroke(domainGridlineStroke);
        p.setDomainGridlinePaint(domainGridlinePaint);
        p.setDomainGridlinesVisible(domainGridlinesVisible);    	

        p.setRangeGridlineStroke(rangeGridlineStroke);
        p.setRangeGridlinePaint(rangeGridlinePaint);
        p.setRangeGridlinesVisible(rangeGridlinesVisible);     	

        p.setOrientation(plotOrientation);
    }

    @Override
    public void resetToDefaults() 
    {        
        Preferences pref = getPreferences();
        this.domainGridlinePaint = (Paint) SerializationUtilities.getSerializableObject(pref, PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_PAINT, Color.red);
        this.rangeGridlinePaint = (Paint) SerializationUtilities.getSerializableObject(pref, PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_PAINT, Color.red);
        this.domainGridlineStroke = SerializationUtilities.getStroke(pref, PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_STROKE, XYPlot.DEFAULT_GRIDLINE_STROKE);
        this.rangeGridlineStroke = SerializationUtilities.getStroke(pref, PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_STROKE, XYPlot.DEFAULT_GRIDLINE_STROKE);
        this.plotOrientation = pref.getBoolean(PreferredBasePlotStyle.PLOT_VERTICAL,true) ? PlotOrientation.VERTICAL: PlotOrientation.HORIZONTAL;
        this.domainGridlinesVisible = pref.getBoolean(PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_VISIBLE, false);
        this.rangeGridlinesVisible = pref.getBoolean(PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_STROKE, false);

        super.resetToDefaults();	   
    }

    @Override
    public void saveAsDefaults() 
    {	   
        super.saveAsDefaults();

        Preferences pref = getPreferences();

        pref.putBoolean(PreferredBasePlotStyle.PLOT_VERTICAL, plotOrientation.equals(PlotOrientation.VERTICAL));
        pref.putBoolean(PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_VISIBLE, domainGridlinesVisible);
        pref.putBoolean(PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_VISIBLE, rangeGridlinesVisible);

        try 
        {
            SerializationUtilities.putSerializableObject(pref, PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_PAINT, domainGridlinePaint);
            SerializationUtilities.putSerializableObject(pref, PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_PAINT, rangeGridlinePaint);
            SerializationUtilities.putStroke(pref, PreferredBasePlotStyle.PLOT_DOMAIN_GRIDLINE_STROKE, domainGridlineStroke);
            SerializationUtilities.putStroke(pref, PreferredBasePlotStyle.PLOT_RANGE_GRIDLINE_STROKE, rangeGridlineStroke);
        }
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }	

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
    public void undoChanges() 
    {
        setParametersToInitial();
        super.undoChanges();
    }

    private JPanel buildMainPanel()
    {
        SubPanel mainPanel = getEditorPanel();

        JButton buttonEditDomainGridlines = new JButton(editDomainAction);
        JButton buttonEditRangeGridlines = new JButton(editRangeAction);

        SubPanel domainGridlinePanel = new SubPanel();

        domainGridlinePanel.addComponent(boxDrawDomainGridline, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.075, 0);     
        domainGridlinePanel.addComponent(new JLabel("Stroke"), 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        domainGridlinePanel.addComponent(domainGridlineStrokeSample, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);                

        mainPanel.addComponent(new JLabel("Domain gridlines"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(domainGridlinePanel, 1, 3, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        mainPanel.addComponent(buttonEditDomainGridlines, 4, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        SubPanel rangeGridlinePanel = new SubPanel();

        rangeGridlinePanel.addComponent(boxDrawRangeGridline, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.075, 0);     
        rangeGridlinePanel.addComponent(new JLabel("Stroke"), 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        rangeGridlinePanel.addComponent(rangeGridlineStrokeSample, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);                

        mainPanel.addComponent(new JLabel("Range gridlines"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(rangeGridlinePanel, 1, 4, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        mainPanel.addComponent(buttonEditRangeGridlines, 4, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        mainPanel.addComponent(new JLabel("Plot Orientation"), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(comboOrientation, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		

        mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7),
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Plot style"),BorderFactory.createEmptyBorder(8, 6, 6, 8))));

        return mainPanel;
    }

    @Override
    public String getSubeditorName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setNameBorder(boolean b) {
        // TODO Auto-generated method stub

    }

    private class EditDomainGridlineStroke extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public EditDomainGridlineStroke() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(NAME, "Edit");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            attemptDomainGridlineStrokeSelection();
        }
    }

    private class EditRangeGridlineStroke extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public EditRangeGridlineStroke() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(NAME, "Edit");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            attemptRangeGridlineStrokeSelection();
        }
    }

}
