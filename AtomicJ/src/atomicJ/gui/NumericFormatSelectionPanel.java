
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

import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.gui.StandardNumericalFormatStyle.FormattableNumericalDataState;

public class NumericFormatSelectionPanel extends SubPanel
{
    private static final long serialVersionUID = 1L;

    private final JSpinner spinnerFractionDigits = new JSpinner(new SpinnerNumberModel(1,0,1000,1));
    private final JComboBox<Character> comboDecimalSeparator = new JComboBox<>(new Character[] {'.',','});
    private final JComboBox<Character> comboGroupingSeparator = new JComboBox<>(new Character[] {' ',',','.','\''});
    private final JCheckBox boxUseThousandGrouping = new JCheckBox("Use separator");
    private final JCheckBox boxTrailingZeroes = new JCheckBox();

    private final FormattableNumericalDataListener listener;

    private NumericalFormatStyle formattable;

    public NumericFormatSelectionPanel()
    {
        this(new StandardNumericalFormatStyle());
    }

    public NumericFormatSelectionPanel(NumericalFormatStyle formattable)
    {
        this.formattable = formattable;
        this.listener = buildPropertyListener();
        pullModelProperties();

        buildLayout();
        initChangeListener();
        initItemListeners();
    }

    public void reset(FormattableNumericalDataState memento)
    {
        this.formattable.setState(memento);
    }

    public FormattableNumericalDataState getStateMemento()
    {
        return formattable.getState();
    }

    public void saveToPreferences()
    {
        this.formattable.saveToPreferences();
    }

    private FormattableNumericalDataListener buildPropertyListener()
    {
        FormattableNumericalDataListener listener = new FormattableNumericalDataListener() {

            @Override
            public void showTrailingZeroesChanged(boolean shown) {
                boxTrailingZeroes.setSelected(true);
            }

            @Override
            public void maximumFractionDigitsChanged(int maxDigits) {
                spinnerFractionDigits.setValue(maxDigits);
            }

            @Override
            public void groupingUsedChanged(boolean used) {
                boxUseThousandGrouping.setSelected(used);
            }

            @Override
            public void groupingSeparatorChanged(char separator) {
                comboGroupingSeparator.setSelectedItem(separator);
            }

            @Override
            public void decimalSeparatorChanged(char separator) {
                comboDecimalSeparator.setSelectedItem(separator);
            }
        };

        return listener;
    }

    public void setFormattableData(NumericalFormatStyle formattableNew)
    {
        if(this.formattable != null)
        {
            this.formattable.removeListener(listener);
        }

        this.formattable = formattableNew;
        this.formattable.addListener(listener);
        pullModelProperties();
    }

    private void pullModelProperties()
    {
        int maxDigits = formattable.getMaximumFractionDigits(); 
        boolean showTrailingZeroes = formattable.isShowTrailingZeroes();
        boolean separateThousands = formattable.isGroupingUsed();
        char decimalSeparator = formattable.getDecimalSeparator();
        char thousandSeparator = formattable.getGroupingSeparator();

        spinnerFractionDigits.setValue(maxDigits);
        boxTrailingZeroes.setSelected(showTrailingZeroes);
        boxUseThousandGrouping.setSelected(separateThousands);
        comboDecimalSeparator.setSelectedItem(decimalSeparator);
        comboGroupingSeparator.setSelectedItem(thousandSeparator);
    }


    private void initChangeListener()
    {
        spinnerFractionDigits.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent evt) {
                int digitsNew = ((Number)spinnerFractionDigits.getValue()).intValue();
                formattable.setMaximumFractionDigits(digitsNew);
            }
        });
    }

    private void initItemListeners()
    {
        boxUseThousandGrouping.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt)
            {
                boolean selected = boxUseThousandGrouping.isSelected();

                comboGroupingSeparator.setEnabled(selected);
                formattable.setGroupingUsed(selected);

            }
        });
        boxTrailingZeroes.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                boolean selected = boxTrailingZeroes.isSelected();                
                formattable.setShowTrailingZeroes(selected);
            }
        });
        comboDecimalSeparator.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                char decimalSeparator = (char)comboDecimalSeparator.getSelectedItem();
                formattable.setDecimalSeparator(decimalSeparator);
            }
        });
        comboGroupingSeparator.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                char groupingSeparator = (char)comboGroupingSeparator.getSelectedItem();
                formattable.setGroupingSeparator(groupingSeparator);
            }
        });
    }

    private void buildLayout()
    {				
        JLabel labelTrailingZeroes = new JLabel("Trailing zeroes");
        JLabel labelMaxFractionDigits = new JLabel("Fraction digits: ");
        JLabel labelDecimalSeparator = new JLabel("Decimal separator: ");
        JLabel labelThousandSeparator = new JLabel("Thousand separator: ");

        labelTrailingZeroes.setDisplayedMnemonic(KeyEvent.VK_T);
        boxTrailingZeroes.setMnemonic(KeyEvent.VK_T);

        labelMaxFractionDigits.setDisplayedMnemonic(KeyEvent.VK_F);
        labelMaxFractionDigits.setLabelFor(spinnerFractionDigits);

        labelDecimalSeparator.setDisplayedMnemonic(KeyEvent.VK_D);
        labelDecimalSeparator.setLabelFor(comboDecimalSeparator);

        labelThousandSeparator.setDisplayedMnemonic(KeyEvent.VK_H);
        labelThousandSeparator.setLabelFor(comboGroupingSeparator);

        boxUseThousandGrouping.setMnemonic(KeyEvent.VK_U);

        comboDecimalSeparator.setPreferredSize(spinnerFractionDigits.getPreferredSize());
        comboGroupingSeparator.setPreferredSize(spinnerFractionDigits.getPreferredSize());

        addComponent(labelTrailingZeroes, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        addComponent(boxTrailingZeroes, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        addComponent(labelMaxFractionDigits, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        addComponent(spinnerFractionDigits, 1, 1, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        addComponent(labelDecimalSeparator, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        addComponent(comboDecimalSeparator, 1, 2, 2, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        addComponent(labelThousandSeparator, 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        addComponent(boxUseThousandGrouping,2, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);
        addComponent(comboGroupingSeparator, 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }
}
