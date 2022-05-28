
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
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.*;
import atomicJ.utilities.Validation;


public class TooltipSubeditor implements ItemListener, Subeditor 
{
    private final Preferences pref;

    //CURRENT parameters

    private boolean visible;
    private boolean prefixVisible;
    private int maxDigits;
    private boolean groupingUsed;
    private char groupingSeparator;
    private char decimalSeparator;
    private boolean trailingZeroes;

    //INITIAL general parameters

    private final boolean initVisible;
    private final boolean initPrefixVisible;
    private final int initMaxDigits;
    private final boolean initTickLabelGroupingUsed;
    private final char initTickLabelGroupingSeparator;
    private final char initTickLabelDecimalSeparator;
    private final boolean initTickLabelTrailingZeroes;

    //format panel
    private final JCheckBox boxVisible = new JCheckBox();
    private final JCheckBox boxPrefixVisible = new JCheckBox();

    private final JSpinner spinnerFractionDigits = new JSpinner(new SpinnerNumberModel(1,0,1000,1));
    private final JComboBox<Character> comboDecimalSeparator = new JComboBox<>(new Character[] {'.',','});
    private final JComboBox<Character> comboGroupingSeparator = new JComboBox<>(new Character[] {' ',',','.','\''});
    private final JCheckBox boxTrailingZeroes = new JCheckBox();
    private final JCheckBox boxUseThousandGrouping = new JCheckBox("Use separator");

    private final List<TooltipSubeditor> chartBoundedTooltipSubeditors = new ArrayList<>();
    private final List<TooltipStyleManager> typeBoundedManagers = new ArrayList<>();

    private final TooltipStyleManager styleManager;  

    private boolean boundTooltips = false;

    private final JPanel editorPanel = new JPanel(new BorderLayout());

    public TooltipSubeditor(TooltipStyleManager styleManager) 
    {
        this.styleManager = Validation.requireNonNullParameterName(styleManager, "styleManager");
        this.pref = styleManager.getPreferences();

        this.initVisible = styleManager.isVisible();
        this.initPrefixVisible = styleManager.isPrefixVisible();
        this.initMaxDigits = styleManager.getMaximumFractionDigits();
        this.initTickLabelGroupingUsed = styleManager.isGroupingUsed();
        this.initTickLabelGroupingSeparator = styleManager.getGroupingSeparator();
        this.initTickLabelDecimalSeparator = styleManager.getDecimalSeparator();
        this.initTickLabelTrailingZeroes = styleManager.isTrailingZeroes();

        setParametersToInitial();
        resetEditor();

        JPanel formatPanel = buildFormatPanel();        
        editorPanel.add(formatPanel, BorderLayout.CENTER);

        initChangeListener();
        initItemListener();
    }

    //OK
    public void addBoundedSubeditor(TooltipSubeditor boundedSubeditor)
    {        
        this.chartBoundedTooltipSubeditors.add(boundedSubeditor);
    }

    public void addTypeBoundedManagers(List<TooltipStyleManager> boundedAxes)
    {
        this.typeBoundedManagers.addAll(boundedAxes);
    }

    public boolean isBoundTooltips()
    {
        return boundTooltips;
    }

    public void setBoundTooltips(boolean boundTooltips)
    {
        this.boundTooltips = boundTooltips;
    }

    //OK
    private void initChangeListener()
    {
        spinnerFractionDigits.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                int maxDigits = ((Number)spinnerFractionDigits.getValue()).intValue();
                setMaximumFractionDigits(maxDigits);                
            }
        });
    }

    //OK
    private void initItemListener()
    {
        boxVisible.addItemListener(this);
        boxPrefixVisible.addItemListener(this);
        boxUseThousandGrouping.addItemListener(this);
        boxTrailingZeroes.addItemListener(this); 

        comboDecimalSeparator.addItemListener(this);
        comboGroupingSeparator.addItemListener(this);
    }

    //OK
    private void setParametersToInitial()
    {
        this.visible = initVisible;
        this.prefixVisible = initPrefixVisible;
        this.maxDigits = initMaxDigits;
        this.groupingUsed = initTickLabelGroupingUsed;
        this.groupingSeparator = initTickLabelGroupingSeparator;
        this.decimalSeparator = initTickLabelDecimalSeparator;
        this.trailingZeroes = initTickLabelTrailingZeroes;
    }

    //OK
    private JPanel buildFormatPanel()
    {
        JPanel formatPanel = new JPanel();
        SubPanel innerPanel = new SubPanel();   

        JLabel labelVisible = new JLabel("Show: ");
        JLabel labelPrefixVisible = new JLabel("Prefix: ");
        JLabel labelTrailingZeroes = new JLabel("Trailing zeroes: ");
        JLabel labelFractionDigits = new JLabel("Fraction digits: ");
        JLabel labelDecimalSeparator = new JLabel("Decimal separator: ");
        JLabel labelThousandSeparator = new JLabel("Thousand separator: ");     

        comboDecimalSeparator.setPreferredSize(comboGroupingSeparator.getPreferredSize());

        innerPanel.addComponent(labelVisible, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(boxVisible, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        innerPanel.addComponent(labelPrefixVisible, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(boxPrefixVisible, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        innerPanel.addComponent(Box.createVerticalStrut(10), 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(Box.createVerticalStrut(10), 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        innerPanel.addComponent(labelTrailingZeroes, 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(boxTrailingZeroes, 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        innerPanel.addComponent(labelFractionDigits, 0, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(spinnerFractionDigits, 1, 4, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        innerPanel.addComponent(labelDecimalSeparator, 0, 5, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(comboDecimalSeparator, 1, 5, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        innerPanel.addComponent(labelThousandSeparator, 0, 6, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(comboGroupingSeparator, 1, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(boxUseThousandGrouping,2, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        formatPanel.add(innerPanel);
        formatPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return formatPanel; 
    }

    @Override
    public void resetToDefaults() 
    {      
        this.visible = pref.getBoolean(VISIBLE, true);
        this.prefixVisible = pref.getBoolean(PREFIX_VISIBLE, true);
        this.maxDigits = pref.getInt(MAX_FRACTION_DIGITS, 3);
        this.groupingUsed = pref.getBoolean(GROUPING_USED, false);       
        this.trailingZeroes = pref.getBoolean(TRAILING_ZEROES, true);
        this.groupingSeparator = (char) pref.getInt(GROUPING_SEPARATOR, ' ');                
        this.decimalSeparator = (char) pref.getInt(DECIMAL_SEPARATOR, '.');              
    }

    @Override
    public void saveAsDefaults() 
    {         
        pref.putBoolean(VISIBLE, visible);
        pref.putBoolean(PREFIX_VISIBLE, prefixVisible);
        pref.putInt(MAX_FRACTION_DIGITS, maxDigits);
        pref.putBoolean(GROUPING_USED, groupingUsed);       
        pref.putInt(GROUPING_SEPARATOR, groupingSeparator);             
        pref.putInt(DECIMAL_SEPARATOR, decimalSeparator);               
        pref.putBoolean(TRAILING_ZEROES, trailingZeroes);

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
    }

    protected void resetBoundedTooltipManager(TooltipStyleManager manager)
    {   
        manager.setVisible(visible);
        manager.setPrefixVisible(prefixVisible);
        manager.setMaximumFractionDigits(maxDigits);
        manager.setGroupingUsed(groupingUsed);
        manager.setDecimalSeparator(decimalSeparator);
        manager.setGroupingSeparator(groupingSeparator);
        manager.setTickLabelShowTrailingZeroes(trailingZeroes);    
    }

    //OK
    protected void resetEditor()
    {
        boxVisible.setSelected(visible);
        boxPrefixVisible.setSelected(prefixVisible);
        spinnerFractionDigits.setValue(maxDigits);
        boxUseThousandGrouping.setSelected(groupingUsed);  
        boxTrailingZeroes.setSelected(trailingZeroes);
        comboDecimalSeparator.setSelectedItem(decimalSeparator);
        comboGroupingSeparator.setSelectedItem(groupingSeparator);
    }

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        Object source = evt.getSource();

        boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);

        if(source == boxVisible)
        {
            setTooltipsVisible(selected);
        }
        else if(source == boxPrefixVisible)
        {
            setPrefixVisible(selected);
        }
        else if(source == boxTrailingZeroes)
        {
            setTrailingZeroes(selected);
        }
        else if(source == boxUseThousandGrouping)
        {   
            setGroupingUsed(selected);
        }
        else if(source == comboDecimalSeparator)
        {           
            char decimalSeparator = (Character)comboDecimalSeparator.getSelectedItem();     
            setDecimalSeparator(decimalSeparator);    
        }
        else if(source == comboGroupingSeparator)
        {
            char groupingSeparator = (Character)comboGroupingSeparator.getSelectedItem();       
            setGroupingSeparator(groupingSeparator);
        }           
    }

    public void setTooltipsVisible(boolean visibleNew)
    {
        if(this.visible != visibleNew)
        {
            this.visible = visibleNew;
            styleManager.setVisible(visibleNew);
            boxVisible.setSelected(visibleNew);

            if(isBoundTooltips())
            {
                for(TooltipSubeditor subEditor : chartBoundedTooltipSubeditors)
                {
                    subEditor.setTooltipsVisible(visibleNew);                   
                }
            }
        }
    }

    public void setPrefixVisible(boolean prefixVisibleNew)
    {
        if(this.prefixVisible != prefixVisibleNew)
        {
            this.prefixVisible = prefixVisibleNew;
            styleManager.setPrefixVisible(prefixVisibleNew);
            boxPrefixVisible.setSelected(prefixVisibleNew);

            if(isBoundTooltips())
            {
                for(TooltipSubeditor subEditor : chartBoundedTooltipSubeditors)
                {
                    subEditor.setPrefixVisible(prefixVisibleNew);
                }
            }
        }
    }

    public void setTrailingZeroes(boolean trailingZeroesNew)
    {
        if(this.trailingZeroes != trailingZeroesNew)
        {
            this.trailingZeroes = trailingZeroesNew;
            styleManager.setTickLabelShowTrailingZeroes(trailingZeroesNew);
            boxTrailingZeroes.setSelected(trailingZeroesNew);

            if(isBoundTooltips())
            {
                for(TooltipSubeditor subEditor : chartBoundedTooltipSubeditors)
                {
                    subEditor.setTrailingZeroes(trailingZeroesNew);
                }
            }
        }
    }

    public void setMaximumFractionDigits(int maxDigitsNew)
    {
        if(this.maxDigits != maxDigitsNew)
        {
            this.maxDigits = maxDigitsNew;
            styleManager.setMaximumFractionDigits(maxDigitsNew);
            spinnerFractionDigits.setValue(maxDigitsNew);

            if(isBoundTooltips())
            {
                for(TooltipSubeditor subEditor : chartBoundedTooltipSubeditors)
                {
                    subEditor.setMaximumFractionDigits(maxDigitsNew);
                }
            }
        }
    }

    public void setGroupingUsed(boolean groupingUsedNew)
    {
        if(this.groupingUsed != groupingUsedNew)
        {
            this.groupingUsed = groupingUsedNew;

            boxUseThousandGrouping.setSelected(groupingUsedNew);
            styleManager.setGroupingUsed(groupingUsedNew);
            comboGroupingSeparator.setEnabled(groupingUsedNew);

            if(isBoundTooltips())
            {
                for(TooltipSubeditor subEditor : chartBoundedTooltipSubeditors)
                {
                    subEditor.setGroupingUsed(groupingUsedNew);
                }
            }
        }
    }

    public void setDecimalSeparator(char decimalSeparatorNew)
    {
        if(this.decimalSeparator != decimalSeparatorNew)
        {
            this.decimalSeparator = decimalSeparatorNew;
            comboDecimalSeparator.setSelectedItem(Character.valueOf(decimalSeparatorNew));        
            styleManager.setDecimalSeparator(decimalSeparatorNew);

            if(isBoundTooltips())
            {
                for(TooltipSubeditor subEditor : chartBoundedTooltipSubeditors)
                {
                    subEditor.setDecimalSeparator(decimalSeparatorNew);
                }
            }
        }       
    }

    public void setGroupingSeparator(char groupingSeparatorNew)
    {
        if(this.groupingSeparator != groupingSeparatorNew)
        {
            this.groupingSeparator = groupingSeparatorNew;
            comboGroupingSeparator.setSelectedItem(Character.valueOf(groupingSeparatorNew));      
            styleManager.setGroupingSeparator(groupingSeparatorNew);

            if(isBoundTooltips())
            {
                for(TooltipSubeditor subEditor : chartBoundedTooltipSubeditors)
                {
                    subEditor.setGroupingSeparator(groupingSeparatorNew);
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

    @Override
    public void applyChangesToAll()
    {
        for(TooltipStyleManager a: typeBoundedManagers)
        {           
            resetBoundedTooltipManager(a);
        }           
    }

    @Override
    public Component getEditionComponent() 
    {
        return editorPanel;
    }

    public void setEditionComponetBorder(Border border)
    {
        editorPanel.setBorder(border);
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return typeBoundedManagers.size() > 1;
    }
}
