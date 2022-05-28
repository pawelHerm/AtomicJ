
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.jfree.ui.RectangleEdge;

import atomicJ.gui.ChartStyleSupplier;
import atomicJ.gui.CustomizableXYBaseChart;
import atomicJ.gui.RoamingTextTitle;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;


import static atomicJ.gui.PreferenceKeys.*;

public class TitlePositionSubeditor extends SubPanel implements Subeditor, ActionListener, ItemListener, ChangeListener
{
    private static final long serialVersionUID = 1L;

    private static final String RIGHT = "Right";
    private static final String BOTTOM = "Bottom";
    private static final String LEFT = "Left";
    private static final String TOP = "Top";

    private final Preferences pref;

    private final boolean initIsInside;
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


    private boolean isInside;
    private int insideX;
    private int insideY;
    private RectangleEdge outsidePosition;


    private final JCheckBox insideCheckBox = new JCheckBox();
    private final JLabel labelPositionX = new JLabel("Position");
    private final JLabel labelLeft = new JLabel("Left");
    private final JLabel labelRight = new JLabel("Right");
    private final JLabel labelBottom = new JLabel("Bottom");
    private final JLabel labelTop = new JLabel("Top");
    private final JLabel labelEdge = new JLabel("Edge");

    private final JSlider sliderX;
    private final JSlider sliderY;
    private final JComboBox<String> comboEdge = new JComboBox<>(new String[] {RIGHT, BOTTOM, LEFT, TOP});	

    private final JSpinner spinnerPaddingTop;
    private final JSpinner spinnerPaddingBottom;
    private final JSpinner spinnerPaddingLeft;
    private final JSpinner spinnerPaddingRight;

    private final JSpinner spinnerMarginTop;
    private final JSpinner spinnerMarginBottom;
    private final JSpinner spinnerMarginLeft;
    private final JSpinner spinnerMarginRight;


    private final RoamingTextTitle title;
    private final List<? extends CustomizableXYBaseChart<?>> boundedCharts;

    public TitlePositionSubeditor(RoamingTextTitle title, List<? extends CustomizableXYBaseChart<?>> boundedCharts)
    {
        this.boundedCharts = boundedCharts;
        this.title = title;
        this.pref = title.getPreferences();

        this.initIsInside = title.isInside();
        this.initInsideX = (int) Math.round(title.getInsideX()*100);
        this.initInsideY = (int)Math.round(title.getInsideY()*100);
        this.initOutsidePosition = title.getOutsidePosition();

        this.initPaddingTop = title.getTopPadding();
        this.initPaddingBottom = title.getBottomPadding();
        this.initPaddingLeft = title.getLeftPadding();
        this.initPaddingRight = title.getRightPadding();

        this.initMarginTop = title.getTopMargin();
        this.initMarginBottom = title.getBottomMargin();
        this.initMarginLeft = title.getLeftMargin();
        this.initMarginRight = title.getRightMargin();

        setParametersToInitial();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        insideCheckBox.setSelected(initIsInside);

        this.sliderX = new JSlider(0, 100, initInsideX);
        sliderX.setMajorTickSpacing(5);

        this.sliderY = new JSlider(0, 100, initInsideY);
        sliderY.setMajorTickSpacing(5);

        setEditorConsistentWithTitleLocalization();

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
        sliderX.addChangeListener(this);
        sliderY.addChangeListener(this);

        spinnerPaddingTop.addChangeListener(this); 	
        spinnerPaddingBottom.addChangeListener(this);                      
        spinnerPaddingLeft.addChangeListener(this);        
        spinnerPaddingRight.addChangeListener(this); 

        spinnerMarginTop.addChangeListener(this); 	
        spinnerMarginBottom.addChangeListener(this);                      
        spinnerMarginLeft.addChangeListener(this);        
        spinnerMarginRight.addChangeListener(this);      
    }

    private void initItemListener()
    {
        insideCheckBox.addItemListener(this);
        comboEdge.addItemListener(this);
    }

    private void setParametersToInitial()
    {
        this.isInside = initIsInside;
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
        setLayout(new BorderLayout());

        SubPanel positionPanel = new SubPanel();  	

        positionPanel.addComponent(new JLabel("Draw inside"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        positionPanel.addComponent(insideCheckBox, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);

        SubPanel panelSpinnera = new SubPanel();

        panelSpinnera.addComponent(labelLeft, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);
        panelSpinnera.addComponent(sliderX, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,1, 1);
        panelSpinnera.addComponent(labelRight, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1);

        panelSpinnera.addComponent(labelBottom, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);
        panelSpinnera.addComponent(sliderY, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
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

        add(positionPanel, BorderLayout.NORTH);   	
        setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
    }

    private void setEditorConsistentWithTitleLocalization()
    {
        boolean inside = insideCheckBox.isSelected();

        labelPositionX.setEnabled(inside);
        sliderX.setEnabled(inside);
        labelLeft.setEnabled(inside);
        labelRight.setEnabled(inside);

        sliderY.setEnabled(inside);
        labelBottom.setEnabled(inside);
        labelTop.setEnabled(inside);
    }

    @Override
    public void applyChangesToAll() 
    {
        for(CustomizableXYBaseChart<?> chart: boundedCharts)
        {
            RoamingTextTitle currentTitle = chart.getRoamingTitle();
            if(currentTitle != null)
            {
                resetTitle(currentTitle);
            }
        }	
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();
        resetTitle(title);	
        resetEditor();
    }

    private void resetTitle(RoamingTextTitle title)
    {
        title.setInside(isInside);

        double x = ((double)insideX)/100;		
        double y = ((double)insideY)/100;
        title.setInsidePosition(x, y);
        title.setOutsidePosition(outsidePosition);

        title.setTopPadding(paddingTop);
        title.setBottomPadding(paddingBottom);
        title.setLeftPadding(paddingLeft);
        title.setRightPadding(paddingRight);

        title.setTopMargin(marginTop);
        title.setBottomMargin(marginBottom);
        title.setLeftMargin(marginLeft);
        title.setRightMargin(marginRight);
    }

    private void resetEditor()
    {
        insideCheckBox.setSelected(isInside);
        sliderX.setValue(insideX);
        sliderY.setValue(insideY);
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
        ChartStyleSupplier defaultStyle = title.getSupplier();
        String key = title.getKey();

        double defaultInsideX = defaultStyle.getDefaultTitleInsideX(key);
        double defaultInsideY = defaultStyle.getDefaultTitleInsideY(key);

        isInside = pref.getBoolean(TITLE_INSIDE, false);
        insideX = (int) Math.round(pref.getDouble(TITLE_INSIDE_X, defaultInsideX));
        insideY = (int) Math.round(pref.getDouble(TITLE_INSIDE_Y, defaultInsideY));
        outsidePosition = (RectangleEdge)SerializationUtilities.getSerializableObject(pref, TITLE_OUTSIDE_POSITION, RectangleEdge.RIGHT);

        paddingTop = pref.getDouble(TITLE_PADDING_TOP, paddingTop);
        paddingBottom = pref.getDouble(TITLE_PADDING_BOTTOM, paddingBottom);
        paddingLeft = pref.getDouble(TITLE_PADDING_LEFT, paddingLeft);
        paddingRight = pref.getDouble(TITLE_PADDING_RIGHT, paddingRight);

        marginTop = pref.getDouble(TITLE_MARGIN_TOP, marginTop);
        marginBottom = pref.getDouble(TITLE_MARGIN_BOTTOM, marginBottom);
        marginLeft = pref.getDouble(TITLE_MARGIN_LEFT, marginLeft);
        marginRight = pref.getDouble(TITLE_MARGIN_RIGHT, marginRight);

        resetTitle(title);
        resetEditor();
    }

    @Override
    public void saveAsDefaults() 
    {		
        pref.putBoolean(TITLE_INSIDE, isInside);

        double x = insideX/100.;
        pref.putDouble(TITLE_INSIDE_X, x);

        double y = insideY/100.;
        pref.putDouble(TITLE_INSIDE_Y, y);

        pref.putDouble(TITLE_PADDING_TOP, paddingTop);
        pref.putDouble(TITLE_PADDING_BOTTOM, paddingBottom);
        pref.putDouble(TITLE_PADDING_LEFT, paddingLeft);
        pref.putDouble(TITLE_PADDING_RIGHT, paddingRight);

        pref.putDouble(TITLE_MARGIN_TOP, marginTop);
        pref.putDouble(TITLE_MARGIN_BOTTOM, marginBottom);
        pref.putDouble(TITLE_MARGIN_LEFT, marginLeft);
        pref.putDouble(TITLE_MARGIN_RIGHT, marginRight);

        try 
        {
            SerializationUtilities.putSerializableObject(pref, TITLE_OUTSIDE_POSITION, outsidePosition);
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
        return this;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return boundedCharts.size()>1;
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {        

    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        boolean selected = (evt.getStateChange()== ItemEvent.SELECTED);
        Object source = evt.getSource();

        if(source == insideCheckBox)
        {
            isInside = selected;
            title.setInside(isInside);
            setEditorConsistentWithTitleLocalization();
        }
        else if(source == comboEdge)
        {
            String selectedPosition = (String)comboEdge.getSelectedItem();
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
            title.setOutsidePosition(outsidePosition);
        }
    }

    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object source = evt.getSource();

        if(source == sliderX)
        {
            double value = sliderX.getValue();
            insideX = (int) value;
            double x = insideX/100.;
            title.setInsideX(x);
        }
        else if(source == sliderY)
        {
            double value = sliderY.getValue();
            insideY = (int) value;
            double y = insideY/100.;
            title.setInsideY(y);
        }
        else if(source == spinnerPaddingTop)
        {
            paddingTop = ((SpinnerNumberModel)spinnerPaddingTop.getModel()).getNumber().doubleValue();
            title.setTopPadding(paddingTop);  
        }
        else if(source == spinnerPaddingBottom)
        {
            paddingBottom = ((SpinnerNumberModel)spinnerPaddingBottom.getModel()).getNumber().doubleValue();
            title.setBottomPadding(paddingBottom);  
        }
        else if(source == spinnerPaddingLeft)
        {
            paddingLeft = ((SpinnerNumberModel)spinnerPaddingLeft.getModel()).getNumber().doubleValue();
            title.setLeftPadding(paddingLeft);  
        }
        else if(source == spinnerPaddingRight)
        {
            paddingRight = ((SpinnerNumberModel)spinnerPaddingRight.getModel()).getNumber().doubleValue();
            title.setRightPadding(paddingRight);  
        }
        else if(source == spinnerMarginTop)
        {
            marginTop = ((SpinnerNumberModel)spinnerMarginTop.getModel()).getNumber().doubleValue();
            title.setTopMargin(marginTop);  
        }
        else if(source == spinnerMarginBottom)
        {
            marginBottom = ((SpinnerNumberModel)spinnerMarginBottom.getModel()).getNumber().doubleValue();
            title.setBottomMargin(marginBottom);  
        }
        else if(source == spinnerMarginLeft)
        {
            marginLeft = ((SpinnerNumberModel)spinnerMarginLeft.getModel()).getNumber().doubleValue();
            title.setLeftMargin(marginLeft);  
        }
        else if(source == spinnerMarginRight)
        {
            marginRight = ((SpinnerNumberModel)spinnerMarginRight.getModel()).getNumber().doubleValue();
            title.setRightMargin(marginRight);  
        }
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
