
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.jfree.ui.RectangleEdge;

import atomicJ.gui.ChartStyleSupplier;
import atomicJ.gui.PreferredRoamingTitleLegendStyle;
import atomicJ.gui.RoamingLegend;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;

public class LegendPositionSubeditor implements Subeditor
{
    private static final String RIGHT = "Right";
    private static final String BOTTOM = "Bottom";
    private static final String LEFT = "Left";
    private static final String TOP = "Top";

    private final Preferences pref;

    private final boolean initLegendVisible;
    private final boolean initIsLegendInside;
    private final int initInsideX;
    private final int initInsideY;
    private final RectangleEdge initOutsidePosition;

    private double marginTop;
    private double marginBottom;
    private double marginLeft;
    private double marginRight;

    private double paddingTop;
    private double paddingBottom;
    private double paddingLeft;
    private double paddingRight;

    private final double initMarginTop;
    private final double initMarginBottom;
    private final double initMarginLeft;
    private final double initMarginRight;

    private final double initPaddingTop;
    private final double initPaddingBottom;
    private final double initPaddingLeft;
    private final double initPaddingRight;

    private boolean isVisible;
    private boolean isLegendInside;
    private int insideX;
    private int insideY;
    private RectangleEdge outsidePosition;

    private final JCheckBox legendCheckBox = new JCheckBox();
    private final JCheckBox legendInsideCheckBox = new JCheckBox();
    private final JLabel labelPositionX = new JLabel("Position");
    private final JLabel labelLeft = new JLabel("Left");
    private final JLabel labelRight = new JLabel("Right");
    private final JLabel labelBottom = new JLabel("Bottom");
    private final JLabel labelTop = new JLabel("Top");
    private final JLabel labelEdge = new JLabel("Edge");

    private final JSlider sliderLegendX;
    private final JSlider sliderLegendY;
    private final JComboBox<String> comboEdge = new JComboBox<>(new String[] {RIGHT, BOTTOM, LEFT, TOP});	

    private final JSpinner spinnerPaddingTop;
    private final JSpinner spinnerPaddingBottom;
    private final JSpinner spinnerPaddingLeft;
    private final JSpinner spinnerPaddingRight;

    private final JSpinner spinnerMarginTop;
    private final JSpinner spinnerMarginBottom;
    private final JSpinner spinnerMarginLeft;
    private final JSpinner spinnerMarginRight;

    private final JPanel editorPanel = new JPanel(new BorderLayout());

    private final RoamingLegend legend;
    private final List<RoamingLegend> boundedLegends;

    public LegendPositionSubeditor(RoamingLegend legend, List<RoamingLegend> boundedLegends)
    {
        this.boundedLegends = boundedLegends;
        this.legend = legend;
        this.pref = legend.getPreferences();

        this.initLegendVisible = legend.isVisible();
        this.initIsLegendInside = legend.isInside();
        this.initInsideX = (int) Math.round(legend.getInsideX()*100);
        this.initInsideY = (int)Math.round(legend.getInsideY()*100);
        this.initOutsidePosition = legend.getOutsidePosition();

        this.initPaddingTop = legend.getTopPadding();
        this.initPaddingBottom = legend.getBottomPadding();
        this.initPaddingLeft = legend.getLeftPadding();
        this.initPaddingRight = legend.getRightPadding();

        this.initMarginTop = legend.getTopMargin();
        this.initMarginBottom = legend.getBottomMargin();
        this.initMarginLeft = legend.getLeftMargin();
        this.initMarginRight = legend.getRightMargin();

        setParametersToInitial();

        editorPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        legendCheckBox.setSelected(initLegendVisible);       
        legendInsideCheckBox.setSelected(initIsLegendInside);

        this.sliderLegendX = new JSlider(0, 100, initInsideX);
        sliderLegendX.setMajorTickSpacing(5);

        this.sliderLegendY = new JSlider(0, 100, initInsideY);
        sliderLegendY.setMajorTickSpacing(5);

        setEditorConsistentWithLegendLocalization();

        spinnerPaddingTop = new JSpinner(new SpinnerNumberModel(initPaddingTop,0,1000,0.2));   		
        spinnerPaddingBottom = new JSpinner(new SpinnerNumberModel(initPaddingBottom,0,1000,0.2));                     
        spinnerPaddingLeft = new JSpinner(new SpinnerNumberModel(initPaddingLeft,0,1000,0.2));       
        spinnerPaddingRight = new JSpinner(new SpinnerNumberModel(initPaddingRight,0,1000,0.2));

        spinnerMarginTop = new JSpinner(new SpinnerNumberModel(initMarginTop,0,1000,0.2));   		
        spinnerMarginBottom = new JSpinner(new SpinnerNumberModel(initMarginBottom,0,1000,0.2));                     
        spinnerMarginLeft = new JSpinner(new SpinnerNumberModel(initMarginLeft,0,1000,0.2));       
        spinnerMarginRight = new JSpinner(new SpinnerNumberModel(initMarginRight,0,1000,0.2));       

        addComponentsAndLayout();  

        initItemListener();
        initChangeListener();
    }

    private void initChangeListener()
    {
        sliderLegendX.addChangeListener(new ChangeListener()
        {            
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                insideX = sliderLegendX.getValue();
                double x = insideX/100.;
                legend.setInsideX(x);
            }
        });
        sliderLegendY.addChangeListener(new ChangeListener()
        {            
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                insideY = sliderLegendY.getValue();
                double y = insideY/100.;
                legend.setInsideY(y);
            }
        });

        spinnerPaddingTop.addChangeListener(new ChangeListener()
        {            
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                paddingTop = ((SpinnerNumberModel)spinnerPaddingTop.getModel()).getNumber().doubleValue();
                legend.setTopPadding(paddingTop);  
            }
        }); 	
        spinnerPaddingBottom.addChangeListener(new ChangeListener()
        {            
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                paddingBottom = ((SpinnerNumberModel)spinnerPaddingBottom.getModel()).getNumber().doubleValue();
                legend.setBottomPadding(paddingBottom); 
            }
        });                      
        spinnerPaddingLeft.addChangeListener(new ChangeListener()
        {            
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                paddingLeft = ((SpinnerNumberModel)spinnerPaddingLeft.getModel()).getNumber().doubleValue();
                legend.setLeftPadding(paddingLeft); 
            }
        });        
        spinnerPaddingRight.addChangeListener(new ChangeListener()
        {            
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                paddingRight = ((SpinnerNumberModel)spinnerPaddingRight.getModel()).getNumber().doubleValue();
                legend.setRightPadding(paddingRight);  
            }
        }); 

        spinnerMarginTop.addChangeListener(new ChangeListener()
        {            
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                marginTop = ((SpinnerNumberModel)spinnerMarginTop.getModel()).getNumber().doubleValue();
                legend.setTopMargin(marginTop);  
            }
        }); 	
        spinnerMarginBottom.addChangeListener(new ChangeListener()
        {            
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                marginBottom = ((SpinnerNumberModel)spinnerMarginBottom.getModel()).getNumber().doubleValue();
                legend.setBottomMargin(marginBottom); 
            }
        });                      
        spinnerMarginLeft.addChangeListener(new ChangeListener()
        {            
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                marginLeft = ((SpinnerNumberModel)spinnerMarginLeft.getModel()).getNumber().doubleValue();
                legend.setLeftMargin(marginLeft);  
            }
        });        
        spinnerMarginRight.addChangeListener(new ChangeListener()
        {            
            @Override
            public void stateChanged(ChangeEvent e) 
            {
                marginRight = ((SpinnerNumberModel)spinnerMarginRight.getModel()).getNumber().doubleValue();
                legend.setRightMargin(marginRight);  
            }
        });      
    }

    private void initItemListener()
    {
        legendCheckBox.addItemListener(new ItemListener() 
        {          
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                isVisible = selected;
                legend.setVisible(selected);
            }
        });
        legendInsideCheckBox.addItemListener(new ItemListener()
        {            
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                isLegendInside = selected;
                legend.setInside(selected);
                setEditorConsistentWithLegendLocalization();                
            }
        });
        comboEdge.addItemListener(new ItemListener() 
        {           
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                String selectedPosition = comboEdge.getItemAt(comboEdge.getSelectedIndex());
                if(selectedPosition.equals(RIGHT))
                {
                    outsidePosition = RectangleEdge.RIGHT;
                }
                else if(selectedPosition.equals(BOTTOM))
                {
                    outsidePosition = RectangleEdge.BOTTOM;
                }
                else if(selectedPosition.equals(LEFT))
                {
                    outsidePosition = RectangleEdge.LEFT;
                }
                else if(selectedPosition.equals(TOP))
                {
                    outsidePosition = RectangleEdge.TOP;
                }
                legend.setOutsidePosition(outsidePosition);                
            }
        });
    }

    private void setParametersToInitial()
    {
        this.isVisible = initLegendVisible ;
        this.isLegendInside = initIsLegendInside;
        this.insideX = initInsideX;
        this.insideY = initInsideY;
        this.outsidePosition = initOutsidePosition;
        this.paddingTop = initPaddingTop;
        this.paddingBottom = initPaddingBottom;
        this.paddingLeft = initPaddingLeft;
        this.paddingRight = initPaddingRight;

        this.marginTop = initMarginTop;
        this.marginBottom = initMarginBottom;
        this.marginLeft = initMarginLeft;
        this.marginRight = initMarginRight;
    }

    private void addComponentsAndLayout()
    {
        editorPanel.setLayout(new BorderLayout());

        SubPanel positionPanel = new SubPanel();  	

        positionPanel.addComponent(new JLabel("Show"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        positionPanel.addComponent(legendCheckBox, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);

        positionPanel.addComponent(new JLabel("Draw inside"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        positionPanel.addComponent(legendInsideCheckBox, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);

        SubPanel panelSpinnera = new SubPanel();

        panelSpinnera.addComponent(labelLeft, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);
        panelSpinnera.addComponent(sliderLegendX, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,1, 1);
        panelSpinnera.addComponent(labelRight, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);

        panelSpinnera.addComponent(labelBottom, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);
        panelSpinnera.addComponent(sliderLegendY, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelSpinnera.addComponent(labelTop, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);

        positionPanel.addComponent(labelPositionX, 0, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,.05, 1);
        positionPanel.addComponent(panelSpinnera, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,1, 1);

        positionPanel.addComponent(labelEdge, 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        positionPanel.addComponent(comboEdge, 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        SubPanel panelMargins = new SubPanel();

        panelMargins.addComponent(spinnerPaddingTop, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);        
        panelMargins.addComponent(new JLabel("above"), 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        panelMargins.addComponent(spinnerPaddingBottom, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelMargins.addComponent(new JLabel("below"), 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);

        panelMargins.addComponent(spinnerPaddingLeft, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);        
        panelMargins.addComponent(new JLabel("left"), 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        panelMargins.addComponent(spinnerPaddingRight, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelMargins.addComponent(new JLabel("right"), 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);       

        positionPanel.addComponent(new JLabel("Padding"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        positionPanel.addComponent(panelMargins, 1, 4, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,.05,1, new Insets(5,0,5,0));   

        SubPanel panelPadding = new SubPanel();

        panelPadding.addComponent(spinnerMarginTop, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);        
        panelPadding.addComponent(new JLabel("above"), 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        panelPadding.addComponent(spinnerMarginBottom, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelPadding.addComponent(new JLabel("below"), 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);

        panelPadding.addComponent(spinnerMarginLeft, 0, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);        
        panelPadding.addComponent(new JLabel("left"), 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        panelPadding.addComponent(spinnerMarginRight, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        panelPadding.addComponent(new JLabel("right"), 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);       

        positionPanel.addComponent(new JLabel("Margins"), 0, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        positionPanel.addComponent(panelPadding, 1, 5, 1, 1,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,.05,1, new Insets(5,0,5,0));   

        editorPanel.add(positionPanel, BorderLayout.NORTH);   	
        editorPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
    }

    private void setEditorConsistentWithLegendLocalization()
    {
        boolean inside = legendInsideCheckBox.isSelected();

        labelPositionX.setEnabled(inside);
        sliderLegendX.setEnabled(inside);
        labelLeft.setEnabled(inside);
        labelRight.setEnabled(inside);

        sliderLegendY.setEnabled(inside);
        labelBottom.setEnabled(inside);
        labelTop.setEnabled(inside);
    }

    @Override
    public void applyChangesToAll() 
    {
        for(RoamingLegend leg: boundedLegends)
        {
            resetLegend(leg);
        }		
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();
        resetLegend(legend);	
        resetEditor();
    }

    private void resetLegend(RoamingLegend legend)
    {
        legend.setVisible(isVisible);
        legend.setInside(isLegendInside);

        double x = ((double)insideX)/100;		
        double y = ((double)insideY)/100;
        legend.setInsidePosition(x, y);
        legend.setOutsidePosition(outsidePosition);

        legend.setTopPadding(paddingTop);
        legend.setBottomPadding(paddingBottom);
        legend.setLeftPadding(paddingLeft);
        legend.setRightPadding(paddingRight);

        legend.setTopMargin(marginTop);
        legend.setBottomMargin(marginBottom);
        legend.setLeftMargin(marginLeft);
        legend.setRightMargin(marginRight);
    }

    private void resetEditor()
    {
        legendCheckBox.setSelected(isVisible);
        legendInsideCheckBox.setSelected(isLegendInside);
        sliderLegendX.setValue(insideX);
        sliderLegendY.setValue(insideY);
        comboEdge.setSelectedItem(outsidePosition);

        spinnerPaddingTop.setValue(paddingTop);
        spinnerPaddingBottom.setValue(paddingBottom);
        spinnerPaddingLeft.setValue(paddingLeft);
        spinnerPaddingRight.setValue(paddingRight);

        spinnerMarginTop.setValue(marginTop);
        spinnerMarginBottom.setValue(marginBottom);
        spinnerMarginLeft.setValue(marginLeft);
        spinnerMarginRight.setValue(marginRight);
    }

    @Override
    public void resetToDefaults() 
    {
        ChartStyleSupplier defaultStyle = legend.getSupplier();
        String key = legend.getKey();

        double defaultInsideX = defaultStyle.getDefaultLegendInsideX(key);
        double defaultInsideY = defaultStyle.getDefaultLegendInsideY(key);

        isVisible = pref.getBoolean(PreferredRoamingTitleLegendStyle.LEGEND_VISIBLE, true);
        isLegendInside = pref.getBoolean(PreferredRoamingTitleLegendStyle.LEGEND_INSIDE, false);
        insideX = (int) Math.round(pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_INSIDE_X, defaultInsideX));
        insideY = (int) Math.round(pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_INSIDE_Y, defaultInsideY));
        outsidePosition = (RectangleEdge)SerializationUtilities.getSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_OUTSIDE_POSITION, RectangleEdge.RIGHT);

        paddingTop = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_PADDING_TOP, paddingTop);
        paddingBottom = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_PADDING_BOTTOM, paddingBottom);
        paddingLeft = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_PADDING_LEFT, paddingLeft);
        paddingRight = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_PADDING_RIGHT, paddingRight);

        marginTop = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_TOP, marginTop);
        marginBottom = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_BOTTOM, marginBottom);
        marginLeft = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_LEFT, marginLeft);
        marginRight = pref.getDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_RIGHT, marginRight);

        resetLegend(legend);
        resetEditor();
    }

    @Override
    public void saveAsDefaults() 
    {		
        pref.putBoolean(PreferredRoamingTitleLegendStyle.LEGEND_VISIBLE, isVisible);		
        pref.putBoolean(PreferredRoamingTitleLegendStyle.LEGEND_INSIDE, isLegendInside);

        double x = insideX/100.;
        pref.putDouble(PreferredRoamingTitleLegendStyle.LEGEND_INSIDE_X, x);

        double y = insideY/100.;
        pref.putDouble(PreferredRoamingTitleLegendStyle.LEGEND_INSIDE_Y, y);

        pref.putDouble(PreferredRoamingTitleLegendStyle.LEGEND_PADDING_TOP, paddingTop);
        pref.putDouble(PreferredRoamingTitleLegendStyle.LEGEND_PADDING_BOTTOM, paddingBottom);
        pref.putDouble(PreferredRoamingTitleLegendStyle.LEGEND_PADDING_LEFT, paddingLeft);
        pref.putDouble(PreferredRoamingTitleLegendStyle.LEGEND_PADDING_RIGHT, paddingRight);

        pref.putDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_TOP, marginTop);
        pref.putDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_BOTTOM, marginBottom);
        pref.putDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_LEFT, marginLeft);
        pref.putDouble(PreferredRoamingTitleLegendStyle.LEGEND_MARGIN_RIGHT, marginRight);

        try 
        {

            SerializationUtilities.putSerializableObject(pref, PreferredRoamingTitleLegendStyle.LEGEND_OUTSIDE_POSITION, outsidePosition);
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
    public Component getEditionComponent()
    {
        return editorPanel;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return boundedLegends.size() > 1;
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
}
