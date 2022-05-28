
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberTickUnit;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.*;
import atomicJ.utilities.MathUtilities;
import atomicJ.utilities.SerializationUtilities;


public class NumberAxisSubeditor extends AxisSubeditor<CustomizableNumberAxis> implements ActionListener, Subeditor 
{
    public static final String SELECT_AXIS_LINE_STROKE_COMMAND = "SELECT_AXIS_LINE_STROKE_COMMAND";

    public static final String TOP_LOCATION = "Top";
    public static final String BOTTOM_LOCATION = "Bottom";
    public static final String LEFT_LOCATION = "Left";
    public static final String RIGHT_LOCATION = "Right";

    private static final String AUTO_RANGE_ON_OFF_COMMAND = "AUTO_RANGE_ON_OFF_COMMAND";

    private final Preferences pref;

    //generalParameters 
    private boolean inverted;

    //range parameters
    private boolean autoRange;
    private double minimumRange;
    private double maximumRange;
    private double upperRangePadding;

    //label parameters
    private double labelOuterSpace;
    private double labelInnerSpace;

    //ticks parameters
    private boolean useAutomaticTickIntervals;
    private NumberTickUnit intervalUnit;
    private PrefixedUnit axisUnit;

    private boolean verticalTickLabel;

    //position parameters
    private AxisLocation axisLocation;

    //format parameters
    private boolean tickLabelGroupingUsed;
    private char tickLabelGroupingSeparator;
    private char tickLabelDecimalSeparator;
    private boolean tickLabelTrailingZeroes;


    //INITIAL general parameters

    private final boolean initInverted;

    private final boolean initAutoRange;
    private final double initMinimumRange;
    private final double initMaximumRange;
    private final double initUpperRangePadding;
    private final double initSpaceAboveLabel;
    private final double initSpaceBelowLabel;
    private final boolean initUseAutomaticTickUnits;

    private final NumberTickUnit initIntervalUnit;
    private final PrefixedUnit initAxisUnit;
    private final boolean quantityWithDimension;
    private final boolean initVerticalTickLabel;

    private final AxisLocation initAxisLocation;

    private final boolean initTickLabelGroupingUsed;
    private final char initTickLabelGroupingSeparator;
    private final char initTickLabelDecimalSeparator;
    private final boolean initTickLabelTrailingZeroes;

    //general panel
    private final JCheckBox boxInverted = new JCheckBox();

    //range panel
    private final JCheckBox boxAutoRange = new JCheckBox();
    private final NumericalField fieldMinimumRange = new NumericalField("Range minimum must be a number lesser than maximum");
    private final NumericalField fieldMaximumRange = new NumericalField("Range maximum must be a number greater than minimum");
    private final NumericalField fieldUpperRangePadding = new NumericalField();

    //label panel
    private final JSpinner spinnerSpaceAboveLabel;
    private final JSpinner spinnerSpaceBelowLabel;

    //ticks panel
    private final JSpinner spinnerTickInterval;
    private final JComboBox<PrefixedUnit> comboAxisUnits = new JComboBox<>();

    private final JCheckBox boxUseAutomaticTickUnit = new JCheckBox("Automatic");
    private final JCheckBox boxVerticalTickLabels = new JCheckBox();

    //position panel

    private final JComboBox<AxisLocation> comboLocation = new JComboBox<>(new AxisLocation[] {AxisLocation.TOP_OR_RIGHT, AxisLocation.BOTTOM_OR_LEFT});

    //format panel
    private final JComboBox<Character> comboDecimalSeparator = new JComboBox<>(new Character[] {'.',','});
    private final JComboBox<Character> comboGroupingSeparator = new JComboBox<>(new Character[] {' ',',','.','\''});
    private final JCheckBox boxTrailingZeroes = new JCheckBox();
    private final JCheckBox boxUseThousandGrouping = new JCheckBox("Use separator");

    private final List<NumberAxisSubeditor> chartBoundedAxesSubeditors = new ArrayList<>();

    private final CustomizableNumberAxis axis;  

    public NumberAxisSubeditor(CustomizableNumberAxis axis, AxisType axisType) 
    {
        super(axis, axisType, axis.getPreferences());
        this.axis = axis;
        this.pref = axis.getPreferences();

        this.initInverted = axis.isInverted();
        this.initSpaceAboveLabel = axis.getLabelOuterSpace();
        this.initSpaceBelowLabel = axis.getLabelInnerSpace();

        this.initUseAutomaticTickUnits = axis.isAutoTickUnitSelection();
        this.initIntervalUnit = axis.getTickUnit();

        this.initAxisUnit = axis.getDisplayedUnit();
        this.quantityWithDimension = axis.getDisplayedQuantity().hasDimension();

        this.initVerticalTickLabel = axis.isVerticalTickLabels();

        this.initAutoRange = axis.isAutoRange();
        this.initMinimumRange = axis.getLowerBound();
        this.initMaximumRange = axis.getUpperBound();   
        this.initUpperRangePadding = axis.getUpperRangePadding();

        this.initAxisLocation = axis.getPreferredAxisLocation();

        this.initTickLabelGroupingUsed = axis.isTickLabelGroupingUsed();
        this.initTickLabelGroupingSeparator = axis.getTickLabelGroupingSeparator();
        this.initTickLabelDecimalSeparator = axis.getTickLabelDecimalSeparator();
        this.initTickLabelTrailingZeroes = axis.isTickLabelTrailingZeroes();

        setParametersToInitial();

        boxInverted.setSelected(initInverted);


        ListCellRenderer<? super AxisLocation> locationRenderer = AxisType.DOMAIN.equals(axisType) ? new HorizontalAxisLocationRenderer() : new VerticalAxisLocationRenderer();

        comboLocation.setRenderer(locationRenderer);

        List<PrefixedUnit> proposedAxisUnits = axis.getProposedDisplayedUnits();

        for(PrefixedUnit unit : proposedAxisUnits)
        {
            comboAxisUnits.addItem(unit);
        }

        comboAxisUnits.setEnabled(!initUseAutomaticTickUnits && quantityWithDimension);
        comboAxisUnits.setSelectedItem(initAxisUnit);

        comboDecimalSeparator.setSelectedItem(initTickLabelDecimalSeparator);
        comboGroupingSeparator.setSelectedItem(initTickLabelGroupingSeparator);
        comboLocation.setSelectedItem(initAxisLocation);

        comboGroupingSeparator.setEnabled(initTickLabelGroupingUsed);
        boxUseThousandGrouping.setSelected(initTickLabelGroupingUsed);
        boxTrailingZeroes.setSelected(initTickLabelTrailingZeroes);

        double prefixConversionFactor = axis.getDisplayedToDataScaling();

        double initTickUnitSize = prefixConversionFactor*initIntervalUnit.getSize();

        double rangeLength = axis.getRange().getLength();
        int exp = (int)Math.rint(Math.floor(Math.log10(rangeLength))) - 1;
        double step = Math.pow(10, exp);

        this.spinnerTickInterval = new JSpinner(new SpinnerNumberModel(initTickUnitSize,
                Math.min(initTickUnitSize, 1e-6), Math.max(initTickUnitSize, 1e6), step));        
        this.spinnerTickInterval.setEnabled(!initUseAutomaticTickUnits);

        JSpinner.NumberEditor editor = (JSpinner.NumberEditor)spinnerTickInterval.getEditor();  
        DecimalFormat format = editor.getFormat();  
        format.setMaximumFractionDigits(6);  

        boxVerticalTickLabels.setSelected(initVerticalTickLabel);
        boxAutoRange.setSelected(initAutoRange);
        boxUseAutomaticTickUnit.setSelected(initUseAutomaticTickUnits);

        fieldMinimumRange.setValue(initMinimumRange);
        fieldMinimumRange.setMaximum(initMaximumRange);
        fieldMinimumRange.setEnabled(!initAutoRange);

        fieldMaximumRange.setEnabled(!initAutoRange);
        fieldMaximumRange.setValue(initMaximumRange);
        fieldMaximumRange.setMinimum(initMinimumRange);

        fieldUpperRangePadding.setValue(initUpperRangePadding);

        spinnerSpaceAboveLabel = new JSpinner(new SpinnerNumberModel(labelOuterSpace, 0, 1000, 0.2));          
        spinnerSpaceBelowLabel = new JSpinner(new SpinnerNumberModel(labelInnerSpace, 0, 1000, 0.2));


        JTabbedPane tabbedPane = getMainPane();

        buildGeneralPanel();
        buildLabelPanel();
        buildTicksPanel();     

        // range

        JPanel rangePanel = buildRangePanel();
        tabbedPane.add("Range", rangePanel);

        //location
        JPanel positionPanel = buildPositionPanel();
        tabbedPane.add("Position", positionPanel);

        // format

        JPanel formatPanel = buildFormatPanel();        
        tabbedPane.add("Format", formatPanel);

        initPropertyChangeListeners();
        initChangeListeners();
        initItemListeners();
    }

    @Override
    public void addBoundedSubeditor(AxisSubeditor<? extends Axis> boundedSubeditor)
    {
        super.addBoundedSubeditor(boundedSubeditor);
        if(boundedSubeditor instanceof NumberAxisSubeditor)
        {
            this.chartBoundedAxesSubeditors.add((NumberAxisSubeditor) boundedSubeditor);
        }
    }

    private void initPropertyChangeListeners()
    {
        fieldMinimumRange.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                minimumRange = ((Number)evt.getNewValue()).doubleValue();
                fieldMaximumRange.setMinimum(minimumRange);
                axis.setLowerBound(minimumRange);                
            }
        });

        fieldMaximumRange.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                maximumRange = ((Number)evt.getNewValue()).doubleValue();
                fieldMinimumRange.setMaximum(maximumRange);
                axis.setUpperBound(maximumRange);
            }
        });
        fieldUpperRangePadding.addPropertyChangeListener(NumericalField.VALUE_EDITED, new PropertyChangeListener() {            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                upperRangePadding = ((Number)evt.getNewValue()).doubleValue();
                axis.setUpperRangePadding(upperRangePadding);                
            }
        });

    }

    private void initChangeListeners()
    {
        spinnerTickInterval.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                double tickInterval = ((SpinnerNumberModel)spinnerTickInterval.getModel()).getNumber().doubleValue();
                setTickInterval(tickInterval);                
            }
        });
        spinnerSpaceAboveLabel.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                double spaceAboveLabel = ((SpinnerNumberModel)spinnerSpaceAboveLabel.getModel()).getNumber().doubleValue();
                setSpaceAboveLabel(spaceAboveLabel);                
            }
        }); 
        spinnerSpaceBelowLabel.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                double spaceBelowLabel = ((SpinnerNumberModel)spinnerSpaceBelowLabel.getModel()).getNumber().doubleValue();
                setSpaceBelowLabel(spaceBelowLabel);                  
            }
        });
    }

    private void initItemListeners()
    {
        boxInverted.addItemListener(new ItemListener() {          
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
                if(inverted != selected)
                {
                    inverted = selected;
                    axis.setInverted(selected);
                }
            }
        });

        comboDecimalSeparator.addItemListener(new ItemListener() {          
            @Override
            public void itemStateChanged(ItemEvent e) {
                char tickLabelDecimalSeparator = (Character)comboDecimalSeparator.getSelectedItem();     
                setTickLabelDecimalSeparator(tickLabelDecimalSeparator);                 
            }
        });
        comboGroupingSeparator.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent e) {
                char tickLabelGroupingSeparator = (Character)comboGroupingSeparator.getSelectedItem();       
                setTickLabelGroupingSeparator(tickLabelGroupingSeparator);                
            }
        });
        boxUseThousandGrouping.addItemListener(new ItemListener() {         
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
                setTickLabelGroupingUsed(selected);
            }
        });
        boxTrailingZeroes.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
                setTickLabelTrailingZeroes(selected);
            }
        }); 

        comboLocation.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                axisLocation = (AxisLocation)comboLocation.getSelectedItem();
                axis.setPreferredAxisLocation(axisLocation);                
            }
        });
        comboAxisUnits.addItemListener(new ItemListener() 
        {            
            @Override
            public void itemStateChanged(ItemEvent e) {
                PrefixedUnit axisUnit = (PrefixedUnit) comboAxisUnits.getSelectedItem();
                setAxisUnit(axisUnit);                   
            }
        });
        boxUseAutomaticTickUnit.addItemListener(new ItemListener() {           
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
                setUseAutomaticTickUnits(selected);
            }
        });      
        boxVerticalTickLabels.addItemListener(new ItemListener() {            
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
                verticalTickLabel = selected;
                axis.setVerticalTickLabels(selected);
            }
        });
    }

    private void setParametersToInitial()
    {
        this.inverted = initInverted;

        this.autoRange = initAutoRange;
        this.minimumRange = initMinimumRange;
        this.maximumRange = initMaximumRange;
        this.upperRangePadding = initUpperRangePadding;
        this.labelOuterSpace = initSpaceAboveLabel;
        this.labelInnerSpace = initSpaceBelowLabel;
        this.useAutomaticTickIntervals = initUseAutomaticTickUnits;
        this.intervalUnit = initIntervalUnit;
        this.axisUnit = initAxisUnit;
        this.verticalTickLabel = initVerticalTickLabel;

        this.axisLocation = initAxisLocation;

        this.tickLabelGroupingUsed = initTickLabelGroupingUsed;
        this.tickLabelGroupingSeparator = initTickLabelGroupingSeparator;
        this.tickLabelDecimalSeparator = initTickLabelDecimalSeparator;
        this.tickLabelTrailingZeroes = initTickLabelTrailingZeroes;
    }

    private JPanel buildGeneralPanel()
    {

        SubPanel innerPanel = getGeneralPanelContent();

        innerPanel.addComponent(new JLabel("Inverted"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        innerPanel.addComponent(boxInverted, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        return innerPanel;
    }

    private JPanel buildLabelPanel()
    {
        SubPanel innerPanel = getLabelPanelContent();

        innerPanel.addComponent(new JLabel("Space"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        innerPanel.addComponent(spinnerSpaceAboveLabel, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);        
        innerPanel.addComponent(new JLabel("from outside"), 2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        innerPanel.addComponent(spinnerSpaceBelowLabel, 3, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        innerPanel.addComponent(new JLabel("from inside"), 4, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);

        return innerPanel;
    }

    private JPanel buildTicksPanel()
    {
        SubPanel innerPanel = getTicksPanelContent();

        innerPanel.addComponent(new JLabel("Labels vertical"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        innerPanel.addComponent(boxVerticalTickLabels, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      

        SubPanel panelUnits = new SubPanel();

        panelUnits.addComponent(spinnerTickInterval, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0.1, 1);     
        panelUnits.addComponent(comboAxisUnits, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.1, 1);     
        panelUnits.addComponent(Box.createHorizontalGlue(), 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);     

        innerPanel.addComponent(new JLabel("Tick units"), 0, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        innerPanel.addComponent(boxUseAutomaticTickUnit, 1, 6, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);
        innerPanel.addComponent(new JLabel("Unit"), 2, 6, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0.1, 1);     
        innerPanel.addComponent(panelUnits, 3, 6, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        return innerPanel;
    }

    private JPanel buildRangePanel()
    {
        JPanel rangePanel = new JPanel(new BorderLayout());

        boxAutoRange.setActionCommand(AUTO_RANGE_ON_OFF_COMMAND);
        boxAutoRange.addActionListener(this);

        fieldMinimumRange.addPropertyChangeListener(NumericalField.VALUE_EDITED, this);
        fieldMaximumRange.addPropertyChangeListener(NumericalField.VALUE_EDITED, this);
        fieldUpperRangePadding.addPropertyChangeListener(NumericalField.VALUE_EDITED, this);

        SubPanel innerPanel = new SubPanel();

        innerPanel.addComponent(new JLabel("Auto - adjust"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        innerPanel.addComponent(boxAutoRange, 1, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        innerPanel.addComponent(new JLabel("Minimum"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        innerPanel.addComponent(fieldMinimumRange, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);

        innerPanel.addComponent(new JLabel("Maximum"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        innerPanel.addComponent(fieldMaximumRange, 1, 2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);      

        innerPanel.addComponent(new JLabel("Padding"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, .05, 1);
        innerPanel.addComponent(fieldUpperRangePadding, 1, 3, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 1);        

        rangePanel.add(innerPanel, BorderLayout.NORTH);
        rangePanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return rangePanel;
    }

    private JPanel buildPositionPanel()
    {
        JPanel formatPanel = new JPanel(new BorderLayout());
        SubPanel innerPanel = new SubPanel();   

        JLabel labelPosition = new JLabel("Position");

        innerPanel.addComponent(labelPosition, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 0, 1);
        innerPanel.addComponent(comboLocation, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        formatPanel.add(innerPanel, BorderLayout.NORTH);
        formatPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return formatPanel; 
    }

    private JPanel buildFormatPanel()
    {
        JPanel formatPanel = new JPanel();
        SubPanel innerPanel = new SubPanel();   

        JLabel labelTrailingZeroes = new JLabel("Trailing zeroes: ");
        JLabel labelDecimalSeparator = new JLabel("Decimal separator: ");
        JLabel labelThousandSeparator = new JLabel("Thousand separator: ");     

        comboDecimalSeparator.setPreferredSize(comboGroupingSeparator.getPreferredSize());

        innerPanel.addComponent(labelTrailingZeroes, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(boxTrailingZeroes, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        innerPanel.addComponent(labelDecimalSeparator, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(comboDecimalSeparator, 1, 1, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        innerPanel.addComponent(labelThousandSeparator, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(comboGroupingSeparator, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(boxUseThousandGrouping,2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        formatPanel.add(innerPanel);
        formatPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return formatPanel; 
    }

    public void updateAutoRange() 
    {       
        autoRange = boxAutoRange.isSelected();
        axis.setAutoRange(autoRange);

        if(autoRange) 
        {
            minimumRange = axis.getLowerBound();
            maximumRange = axis.getUpperBound();

            fieldMinimumRange.setValue(minimumRange);
            fieldMinimumRange.setEnabled(false);
            fieldMaximumRange.setValue(maximumRange);
            fieldMaximumRange.setEnabled(false);
        }
        else 
        {
            fieldMinimumRange.setEnabled(true);
            fieldMaximumRange.setEnabled(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        super.actionPerformed(event);

        String command = event.getActionCommand();

        if (command.equals(AUTO_RANGE_ON_OFF_COMMAND)) 
        {
            updateAutoRange();
        }
    }

    @Override
    public void resetToDefaults() 
    {
        this.inverted = pref.getBoolean(PreferredAxisStyle.AXIS_INVERTED, false);

        this.labelOuterSpace = pref.getDouble(PreferredAxisStyle.AXIS_LABEL_TOP_SPACE,7);
        this.labelInnerSpace = pref.getDouble(PreferredAxisStyle.AXIS_LABEL_BOTTOM_SPACE, 5);

        this.verticalTickLabel = pref.getBoolean(PreferredAxisStyle.TICK_LABEL_VERTICAL, false);

        this.autoRange = pref.getBoolean(PreferredAxisStyle.AXIS_AUTO_RANGE, true);

        this.axisLocation = (AxisLocation)SerializationUtilities.getSerializableObject(pref, PreferredAxisStyle.AXIS_LOCATION, AxisLocation.BOTTOM_OR_LEFT);

        this.tickLabelGroupingUsed = pref.getBoolean(PreferredAxisStyle.TICK_LABEL_GROUPING_USED, false);       
        this.tickLabelTrailingZeroes = pref.getBoolean(PreferredAxisStyle.TICK_LABEL_TRAILING_ZEROES, true);
        this.tickLabelGroupingSeparator = (char) pref.getInt(PreferredAxisStyle.TICK_LABEL_GROUPING_SEPARATOR, ' ');                
        this.tickLabelDecimalSeparator = (char) pref.getInt(PreferredAxisStyle.TICK_LABEL_DECIMAL_SEPARATOR, '.');              

        super.resetToDefaults();
    }

    @Override
    public void saveAsDefaults() 
    {   
        try 
        {
            SerializationUtilities.putSerializableObject(pref, PreferredAxisStyle.AXIS_LOCATION, axisLocation);
        } 
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }

        pref.putBoolean(PreferredAxisStyle.AXIS_INVERTED, inverted);

        pref.putDouble(PreferredAxisStyle.AXIS_LABEL_TOP_SPACE, labelOuterSpace);
        pref.putDouble(PreferredAxisStyle.AXIS_LABEL_BOTTOM_SPACE, labelInnerSpace);
        pref.putBoolean(PreferredAxisStyle.TICK_LABEL_VERTICAL, verticalTickLabel);
        pref.putBoolean(PreferredAxisStyle.AXIS_AUTO_RANGE, autoRange);

        pref.putBoolean(PreferredAxisStyle.TICK_LABEL_GROUPING_USED, tickLabelGroupingUsed);       
        pref.putInt(PreferredAxisStyle.TICK_LABEL_GROUPING_SEPARATOR, tickLabelGroupingSeparator);             
        pref.putInt(PreferredAxisStyle.TICK_LABEL_DECIMAL_SEPARATOR, tickLabelDecimalSeparator);               
        pref.putBoolean(PreferredAxisStyle.TICK_LABEL_TRAILING_ZEROES, tickLabelTrailingZeroes);

        try 
        {
            pref.flush();
        } 
        catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }

        super.saveAsDefaults();
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();
        super.undoChanges();
    }

    @Override
    protected void resetAxis(CustomizableNumberAxis a)
    {   
        basicResetAxis(a);

        a.setLowerBound(minimumRange);
        a.setUpperBound(maximumRange);
    }

    @Override
    protected void resetBoundedAxis(Axis a)
    {   
        basicResetAxis(a);

        if(a instanceof CustomizableNumberAxis)
        {
            CustomizableNumberAxis na = (CustomizableNumberAxis)a;
            if(!autoRange)
            {
                na.setLowerBound(minimumRange);
                na.setUpperBound(maximumRange);
            }
        }

    }

    @Override
    protected void basicResetAxis(Axis a)
    {   
        super.basicResetAxis(a);

        if(a instanceof CustomizableNumberAxis)
        {
            basicResetNumberAxis((CustomizableNumberAxis) a);
        }
    }

    private void basicResetNumberAxis(CustomizableNumberAxis na)
    {
        na.setInverted(inverted);

        na.setLabelOuterSpace(labelOuterSpace);
        na.setLabelInnerSpace(labelInnerSpace);

        na.setTickUnit(intervalUnit);
        na.setDisplayedUnit(axisUnit);
        na.setAutoTickUnitSelection(useAutomaticTickIntervals);

        na.setVerticalTickLabels(verticalTickLabel);

        na.setUpperRangePadding(upperRangePadding);

        na.setTickLabelGroupingUsed(tickLabelGroupingUsed);
        na.setTickLabelDecimalSeparator(tickLabelDecimalSeparator);
        na.setTickLabelGroupingSeparator(tickLabelGroupingSeparator);
        na.setTickLabelShowTrailingZeroes(tickLabelTrailingZeroes);

        na.setPreferredAxisLocation(axisLocation);

        na.setAutoRange(autoRange);
    }

    @Override
    protected void resetEditor()
    {
        boxInverted.setSelected(inverted);

        boxUseThousandGrouping.setSelected(tickLabelGroupingUsed);  
        boxTrailingZeroes.setSelected(tickLabelTrailingZeroes);
        comboDecimalSeparator.setSelectedItem(tickLabelDecimalSeparator);
        comboGroupingSeparator.setSelectedItem(tickLabelGroupingSeparator);

        boxUseAutomaticTickUnit.setSelected(useAutomaticTickIntervals);
        spinnerTickInterval.setValue(intervalUnit.getSize());
        boxVerticalTickLabels.setSelected(verticalTickLabel);
        spinnerSpaceAboveLabel.setValue(labelOuterSpace);
        spinnerSpaceBelowLabel.setValue(labelInnerSpace);

        boxAutoRange.setSelected(autoRange);
        fieldMaximumRange.setValue(minimumRange);
        fieldMaximumRange.setValue(maximumRange);
        fieldUpperRangePadding.setValue(upperRangePadding);

        comboAxisUnits.setSelectedItem(axisUnit);
        comboLocation.setSelectedItem(axisLocation);

        comboAxisUnits.setEnabled(!useAutomaticTickIntervals && quantityWithDimension);
        spinnerTickInterval.setEnabled(!useAutomaticTickIntervals);

        super.resetEditor();
    }

    private DecimalFormat getDecimalFormat()
    {
        DecimalFormat format = new DecimalFormat();

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

        int maxDigit = MathUtilities.getFractionCount(((Number)spinnerTickInterval.getValue()).doubleValue());

        int minDigits = tickLabelTrailingZeroes ? maxDigit : 0;

        symbols.setDecimalSeparator(tickLabelDecimalSeparator);
        symbols.setGroupingSeparator(tickLabelGroupingSeparator);
        format.setGroupingUsed(tickLabelGroupingUsed);
        format.setMaximumFractionDigits(maxDigit);
        format.setMinimumFractionDigits(minDigits);

        format.setDecimalFormatSymbols(symbols);

        return format;
    }

    public void setUseAutomaticTickUnits(boolean automaticUnitsNew)
    {
        if(this.useAutomaticTickIntervals != automaticUnitsNew)
        {
            this.useAutomaticTickIntervals = automaticUnitsNew;
            axis.setAutoTickUnitSelection(useAutomaticTickIntervals); 

            spinnerTickInterval.setEnabled(!useAutomaticTickIntervals);
            comboAxisUnits.setEnabled(!useAutomaticTickIntervals && quantityWithDimension);

            if(!useAutomaticTickIntervals)
            {
                axis.setTickUnit(intervalUnit, true, false);
            }
        }
    }

    public void setAxisUnit(PrefixedUnit axisUnitNew)
    {
        if(!this.axisUnit.equals(axisUnitNew))
        {
            this.axisUnit = axisUnitNew;
            axis.setDisplayedUnit(this.axisUnit);

            String label = axis.getLabel();
            setLabel(label);    

            double prefixScalingFactor = axis.getDisplayedToDataScaling();  
            double rawTickInterval = axis.getTickUnit().getSize();
            double scaledTickInterval = prefixScalingFactor*rawTickInterval;;

            spinnerTickInterval.setValue(scaledTickInterval);
        }
    }

    public void setTickInterval(double tickIntervalScaled)
    {
        double scalingFactor = axis.getDisplayedToDataScaling();
        double tickIntervalRaw = tickIntervalScaled/scalingFactor;

        PrefixedTickUnit intervalUnitNew = new PrefixedTickUnit(tickIntervalRaw, getDecimalFormat(), intervalUnit.getMinorTickCount(), scalingFactor);
        this.intervalUnit = intervalUnitNew;

        axis.setTickUnit(intervalUnitNew, true, false);  
    }

    private PrefixedTickUnit getManualIntervalUnit()
    {
        double prefixConversionFactor = axis.getDisplayedToDataScaling();
        double tickInterval = ((SpinnerNumberModel)spinnerTickInterval.getModel()).getNumber().doubleValue();
        double tickIntervalScaled = tickInterval/prefixConversionFactor;

        PrefixedTickUnit tickUnitNew = new PrefixedTickUnit(tickIntervalScaled, getDecimalFormat(), intervalUnit.getMinorTickCount(), prefixConversionFactor);
        return tickUnitNew;
    }



    private void setTickLabelTrailingZeroes(boolean tickLabelTrailingZeroes)
    {
        if(this.tickLabelTrailingZeroes != tickLabelTrailingZeroes)
        {
            this.tickLabelTrailingZeroes = tickLabelTrailingZeroes;
            axis.setTickLabelShowTrailingZeroes(tickLabelTrailingZeroes);
            boxTrailingZeroes.setSelected(tickLabelTrailingZeroes);

            if(isBoundChartAxes())
            {
                for(NumberAxisSubeditor subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setTickLabelTrailingZeroes(tickLabelTrailingZeroes);
                }
            }
        }
    }

    private void setTickLabelGroupingUsed(boolean tickLabelGroupingUsed)
    {
        if(this.tickLabelGroupingUsed != tickLabelGroupingUsed)
        {
            this.tickLabelGroupingUsed = tickLabelGroupingUsed;

            boxUseThousandGrouping.setSelected(tickLabelGroupingUsed);
            axis.setTickLabelGroupingUsed(tickLabelGroupingUsed);
            comboGroupingSeparator.setEnabled(tickLabelGroupingUsed);

            if(isBoundChartAxes())
            {
                for(NumberAxisSubeditor subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setTickLabelGroupingUsed(tickLabelGroupingUsed);
                }
            }
        }
    }

    private void setTickLabelDecimalSeparator(char tickLabelDecimalSeparator)
    {
        if(this.tickLabelDecimalSeparator != tickLabelDecimalSeparator)
        {
            this.tickLabelDecimalSeparator = tickLabelDecimalSeparator;
            comboDecimalSeparator.setSelectedItem(Character.valueOf(tickLabelDecimalSeparator));        
            axis.setTickLabelDecimalSeparator(tickLabelDecimalSeparator);

            if(isBoundChartAxes())
            {
                for(NumberAxisSubeditor subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setTickLabelDecimalSeparator(tickLabelDecimalSeparator);
                }
            }
        }       
    }

    private void setTickLabelGroupingSeparator(char tickLabelGroupingSeparator)
    {
        if(this.tickLabelGroupingSeparator != tickLabelGroupingSeparator)
        {
            this.tickLabelGroupingSeparator = tickLabelGroupingSeparator;
            comboGroupingSeparator.setSelectedItem(Character.valueOf(tickLabelGroupingSeparator));      
            axis.setTickLabelGroupingSeparator(tickLabelGroupingSeparator);

            if(isBoundChartAxes())
            {
                for(NumberAxisSubeditor subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setTickLabelGroupingSeparator(tickLabelGroupingSeparator);
                }
            }
        }     
    }

    private void setSpaceBelowLabel(double spaceBelowLabel)
    {
        if(this.labelInnerSpace != spaceBelowLabel)
        {
            this.labelInnerSpace = spaceBelowLabel;
            spinnerSpaceBelowLabel.setValue(spaceBelowLabel);
            axis.setLabelInnerSpace(spaceBelowLabel);

            if(isBoundChartAxes())
            {
                for(NumberAxisSubeditor subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setSpaceBelowLabel(spaceBelowLabel);
                }
            }
        }
    }

    private void setSpaceAboveLabel(double spaceAboveLabel)
    {
        if(this.labelOuterSpace != spaceAboveLabel)
        {
            this.labelOuterSpace = spaceAboveLabel;
            spinnerSpaceAboveLabel.setValue(spaceAboveLabel);
            axis.setLabelOuterSpace(spaceAboveLabel);

            if(isBoundChartAxes())
            {
                for(NumberAxisSubeditor subEditor : chartBoundedAxesSubeditors)
                {
                    subEditor.setSpaceAboveLabel(spaceAboveLabel);
                }
            }
        }
    }


    private class HorizontalAxisLocationRenderer extends DefaultListCellRenderer 
    {
        private static final long serialVersionUID = 1L;

        private HorizontalAxisLocationRenderer() 
        {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            if (isSelected) 
            {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } 
            else 
            {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            };
            String name = "";
            if(AxisLocation.BOTTOM_OR_LEFT.equals(value))
            {
                name = "Bottom";
            }
            else if(AxisLocation.BOTTOM_OR_RIGHT.equals(value))
            {
                name = "Bottom";
            }
            else if(AxisLocation.TOP_OR_RIGHT.equals(value))
            {
                name = "Top";
            }
            else if(AxisLocation.TOP_OR_LEFT.equals(value))
            {
                name = "Top";
            }
            setText(name);

            return this;
        }
    }

    private class VerticalAxisLocationRenderer extends DefaultListCellRenderer 
    {
        private static final long serialVersionUID = 1L;

        private VerticalAxisLocationRenderer() 
        {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            if (isSelected) 
            {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } 
            else 
            {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            };
            String name = "";
            if(AxisLocation.BOTTOM_OR_LEFT.equals(value))
            {
                name = "Left";
            }
            else if(AxisLocation.BOTTOM_OR_RIGHT.equals(value))
            {
                name = "Right";
            }
            else if(AxisLocation.TOP_OR_RIGHT.equals(value))
            {
                name = "Right";
            }
            else if(AxisLocation.TOP_OR_LEFT.equals(value))
            {
                name = "Left";
            }
            setText(name);

            return this;
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
