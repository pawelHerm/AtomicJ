
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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

package atomicJ.gui.curveProcessing;

import static atomicJ.data.Datasets.LIVE_PREVIEW_INDENTATION_PLOT;
import static atomicJ.data.Datasets.LIVE_PREVIEW_POINTWISE_MODULUS_PLOT;
import static atomicJ.gui.PreferenceKeys.WINDOW_HEIGHT;
import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_X;
import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_Y;
import static atomicJ.gui.PreferenceKeys.WINDOW_WIDTH;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle;

import atomicJ.data.IndentationCurve;
import atomicJ.data.PointwiseModulusCurve;
import atomicJ.gui.Channel1DChart;
import atomicJ.gui.ChannelChart;
import atomicJ.gui.IndentationPlotFactory;
import atomicJ.gui.Channel1DPlot;
import atomicJ.gui.MultipleXYChartPanel;
import atomicJ.gui.NumericalField;
import atomicJ.gui.PointwiseModulusPlotFactory;
import atomicJ.utilities.GUIUtilities;

public class LivePreviewDialog extends JDialog
{
    private static final long serialVersionUID = 1L;

    private final static Preferences PREF =  Preferences.userNodeForPackage(LivePreviewDialog.class).node("SimpleChartPresentationDialog");

    private static final int DEFAULT_HEIGHT = Math.round(3*Toolkit.getDefaultToolkit().getScreenSize().height/5);
    private static final int DEFAULT_WIDTH = Math.round(2*Toolkit.getDefaultToolkit().getScreenSize().width/5);

    private final MultipleXYChartPanel<ChannelChart<Channel1DPlot>> indentationPanel = new MultipleXYChartPanel<>();
    private final MultipleXYChartPanel<ChannelChart<Channel1DPlot>> pointwiseModulusPanel = new MultipleXYChartPanel<>();

    private final NumericalField fieldYoungModulus = new NumericalField();
    private final NumericalField fieldTransitionIndentation = new NumericalField();
    private final NumericalField fieldTransitionForce = new NumericalField();

    public LivePreviewDialog(JDialog parent, String title, boolean modal)
    {
        super(parent, title, modal);

        JPanel buttonPanel = buildPanelButtons();

        JPanel panelFields = buildPanelFields(); 

        JTabbedPane tabPane = new JTabbedPane();

        indentationPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 5, 2));
        tabPane.add(indentationPanel, "Indentation");

        pointwiseModulusPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 5, 2));
        tabPane.add(pointwiseModulusPanel, "Pointwise modulus");


        add(tabPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(panelFields, BorderLayout.EAST);

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent evt)
            {				
                PREF.putInt(WINDOW_HEIGHT, getHeight());
                PREF.putInt(WINDOW_WIDTH, getWidth());
                PREF.putInt(WINDOW_LOCATION_X, (int)getLocation().getX());			
                PREF.putInt(WINDOW_LOCATION_Y, (int)getLocation().getY());
            }
        });

        int locationX = PREF.getInt(WINDOW_LOCATION_X, 0);
        int locationY = PREF.getInt(WINDOW_LOCATION_Y, 0);
        int height = PREF.getInt(WINDOW_HEIGHT, DEFAULT_HEIGHT);
        int width = PREF.getInt(WINDOW_WIDTH, DEFAULT_WIDTH);  

        if(GUIUtilities.areWindowSizeAndLocationWellSpecified(width, height, locationX, locationY))
        { 
            setSize(width,height);
            setLocation(locationX,locationY);
        }
        else
        {
            setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            setLocation(0, 0);
        } 
    }

    private JPanel buildPanelFields()
    {
        JPanel panelFields = new JPanel();

        JLabel labelYoungModulus = new JLabel("Young's modulus (kPa)");
        JLabel labelTranitionIndentation = new JLabel("Transition indent (μm)");
        JLabel labelTransitionForce = new JLabel("Transition force (nN)");

        fieldYoungModulus.setEnabled(false);
        fieldTransitionIndentation.setEnabled(false);
        fieldTransitionForce.setEnabled(false);

        GroupLayout layout = new GroupLayout(panelFields);
        panelFields.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(labelYoungModulus)
                .addComponent(fieldYoungModulus)
                .addComponent(labelTranitionIndentation)
                .addComponent(fieldTransitionIndentation)
                .addComponent(labelTransitionForce)
                .addComponent(fieldTransitionForce)
                );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(labelYoungModulus)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fieldYoungModulus)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelTranitionIndentation)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fieldTransitionIndentation)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelTransitionForce)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fieldTransitionForce)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );

        layout.linkSize(labelYoungModulus,fieldYoungModulus,labelTranitionIndentation, fieldTransitionIndentation, labelTransitionForce,fieldTransitionForce);

        return panelFields;
    }

    private JPanel buildPanelButtons()
    {
        JPanel buttonPanel = new JPanel();
        JButton buttonClose = new JButton(new CloseAction());
        buttonPanel.add(buttonClose);
        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
    }

    public void clear()
    {
        fieldYoungModulus.setValue(Double.NaN);
        fieldTransitionIndentation.setValue(Double.NaN);
        fieldTransitionForce.setValue(Double.NaN);

        indentationPanel.clear();
        pointwiseModulusPanel.clear();
    }

    protected void setYoungModulus(Double youngModulus)
    {
        fieldYoungModulus.setValue(youngModulus);
    }

    protected void setTransitionIndentation(Double transIndentation)
    {
        fieldTransitionIndentation.setValue(transIndentation);
    }

    protected void setTransitionForce(Double transForce)
    {
        fieldTransitionForce.setValue(transForce);
    }

    protected void setIndentationChart(ChannelChart<Channel1DPlot> chart)
    {	
        indentationPanel.clear();
        indentationPanel.setSelectedChart(chart);
    }

    protected void setPointwiseModulusChart(ChannelChart<Channel1DPlot> pointwiseModulusChart)
    {	
        pointwiseModulusPanel.clear();
        pointwiseModulusPanel.setSelectedChart(pointwiseModulusChart);
    }

    public void setLivePreviewPack(LivePreviewPack pack)
    {
        IndentationCurve indentationCurve = pack.getIndentationCurve();
        if(indentationPanel.getChartCount() == 0)
        {
            Channel1DPlot indentationPlot = IndentationPlotFactory.getInstance().getPlot(indentationCurve);
            ChannelChart<Channel1DPlot> indentationChart = new Channel1DChart<>(indentationPlot, LIVE_PREVIEW_INDENTATION_PLOT);		

            setIndentationChart(indentationChart);
        }
        else
        {
            Channel1DPlot plot = indentationPanel.getChartAt(0).getCustomizablePlot();            
            plot.addOrReplaceData(indentationCurve);
        }

        PointwiseModulusCurve pointwiseModulusCurve = pack.getPointwiseModulusCurve();
        if(pointwiseModulusPanel.getChartCount() == 0)
        {
            Channel1DPlot pointwiseMoudulusPlot = PointwiseModulusPlotFactory.getInstance().getPlot(pointwiseModulusCurve);
            ChannelChart<Channel1DPlot> pointwiseModulusChart = new Channel1DChart<>(pointwiseMoudulusPlot, LIVE_PREVIEW_POINTWISE_MODULUS_PLOT);		

            setPointwiseModulusChart(pointwiseModulusChart);
        }
        else
        {
            Channel1DPlot plot = pointwiseModulusPanel.getChartAt(0).getCustomizablePlot();
            plot.addOrReplaceData(pointwiseModulusCurve);
        }		

        setYoungModulus(pack.getYoungModulus());
        setTransitionIndentation(pack.getTransitionIndentation());
        setTransitionForce(pack.getTransitionForce());
    }

    private class CloseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CloseAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME,"Close");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            setVisible(false);
        };
    }
}
