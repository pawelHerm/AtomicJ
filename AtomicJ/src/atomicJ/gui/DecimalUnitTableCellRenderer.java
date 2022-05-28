package atomicJ.gui;


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


/*
 * This class is a modification of DefaultTableCellRenderer form openjdk-7, Original copyrigh notice below
 * /*
 * Copyright (c) 1998, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */



import static atomicJ.gui.PreferenceKeys.*;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.StandardNumericalFormatStyle.FormattableNumericalDataState;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

public class DecimalUnitTableCellRenderer extends JLabel
implements DecimalTableCellRenderer, Serializable
{
    ///CODE BY P.HERMANOWICZ 2014
    private static final long serialVersionUID = 1L;

    private boolean showTrailingZeroes;
    private DecimalFormat format = new DecimalFormat();
    private final List<FormattableNumericalDataListener> listeners = new ArrayList<>();

    private List<PrefixedUnit> dataUnits;
    private double[] unitConversionFactors ;

    private Preferences pref = PreferencesNull.getInstance();
    ////////////////////

    /**
     * An empty <code>Border</code>. This field might not be used. To change the
     * <code>Border</code> used by this renderer override the
     * <code>getTableCellRendererComponent</code> method and set the border
     * of the returned component directly.
     */
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private static final Border NO_FOCUS_BORDER = DEFAULT_NO_FOCUS_BORDER;

    // We need a place to store the color the JLabel should be returned
    // to after its foreground and background colors have been set
    // to the selection background color.
    // These ivars will be made protected when their names are finalized.
    private Color unselectedForeground;
    private Color unselectedBackground;

    public DecimalUnitTableCellRenderer(List<PrefixedUnit> dataUnits, List<PrefixedUnit> displayedUnits)
    {
        this.dataUnits = new ArrayList<>(dataUnits);
        this.unitConversionFactors = new double[dataUnits.size()];
        calculateUnitConversionFactors(displayedUnits);

        setOpaque(true);
        setBorder(getNoFocusBorder());
        setName("Table.cellRenderer");
    }

    public DecimalUnitTableCellRenderer(List<PrefixedUnit> dataUnits, List<PrefixedUnit> displayedUnits, Preferences pref) 
    {
        this(dataUnits, displayedUnits);
        this.pref = pref;
        pullPreferences(pref);      
    }

    private void calculateUnitConversionFactors(List<PrefixedUnit> displayedUnits)
    {
        if(this.dataUnits.size() != displayedUnits.size())
        {
            throw new IllegalArgumentException("The length of 'displayedUnits' should be equal to the number of data units");
        }

        for(int i = 0; i<dataUnits.size(); i++)
        {
            PrefixedUnit displayedUnit = displayedUnits.get(i);
            PrefixedUnit dataUnit = dataUnits.get(i);

            boolean unitsSpecified = (displayedUnit != null) && (dataUnit != null);

            unitConversionFactors[i] = unitsSpecified ? dataUnit.getConversionFactorTo(displayedUnit) : 1;
        }
    }

    public void registerNewUnit(PrefixedUnit dataUnit, PrefixedUnit displayedUnit)
    {
        this.dataUnits.add(dataUnit);

        int sizeNew = this.dataUnits.size();

        boolean unitsSpecified = (displayedUnit != null) && (dataUnit != null);

        this.unitConversionFactors = Arrays.copyOf(unitConversionFactors, sizeNew);
        this.unitConversionFactors[sizeNew - 1] = unitsSpecified ? dataUnit.getConversionFactorTo(displayedUnit) : 1;
    }

    public void setNewDisplayedUnit(int index, PrefixedUnit displayedUnit)
    {
        boolean unitsSpecified = (displayedUnit != null) && (this.dataUnits.get(index) != null);
        this.unitConversionFactors[index] = unitsSpecified ? dataUnits.get(index).getConversionFactorTo(displayedUnit) : 1;
    }

    private Border getNoFocusBorder() {
        Border border = UIManager.getBorder("Table.cellNoFocusBorder");
        if (System.getSecurityManager() != null) {
            if (border != null) return border;
            return SAFE_NO_FOCUS_BORDER;
        } else if (border != null) {
            if (NO_FOCUS_BORDER == null || NO_FOCUS_BORDER == DEFAULT_NO_FOCUS_BORDER) {
                return border;
            }
        }
        return NO_FOCUS_BORDER;
    }

    /**
     * Overrides <code>JComponent.setForeground</code> to assign
     * the unselected-foreground color to the specified color.
     *
     * @param c set the foreground color to this value
     */
    @Override
    public void setForeground(Color c) {
        super.setForeground(c);
        unselectedForeground = c;
    }

    /**
     * Overrides <code>JComponent.setBackground</code> to assign
     * the unselected-background color to the specified color.
     *
     * @param c set the background color to this value
     */
    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        unselectedBackground = c;
    }

    /**
     * Notification from the <code>UIManager</code> that the look and feel
     * [L&F] has changed.
     * Replaces the current UI object with the latest version from the
     * <code>UIManager</code>.
     *
     * @see JComponent#updateUI
     */
    @Override
    public void updateUI() {
        super.updateUI();
        setForeground(null);
        setBackground(null);
    }

    // implements javax.swing.table.TableCellRenderer
    /**
     *
     * Returns the default table cell renderer.
     * <p>
     * During a printing operation, this method will be called with
     * <code>isSelected</code> and <code>hasFocus</code> values of
     * <code>false</code> to prevent selection and focus from appearing
     * in the printed output. To do other customization based on whether
     * or not the table is being printed, check the return value from
     * {@link javax.swing.JComponent#isPaintingForPrint()}.
     *
     * @param table  the <code>JTable</code>
     * @param value  the value to assign to the cell at
     *                  <code>[row, column]</code>
     * @param isSelected true if cell is selected
     * @param hasFocus true if cell has focus
     * @param row  the row of the cell to render
     * @param column the column of the cell to render
     * @return the default table cell renderer
     * @see javax.swing.JComponent#isPaintingForPrint()
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (table == null) {
            return this;
        }

        Color fg = null;
        Color bg = null;

        JTable.DropLocation dropLocation = table.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsertRow()
                && !dropLocation.isInsertColumn()
                && dropLocation.getRow() == row
                && dropLocation.getColumn() == column) {

            fg = UIManager.getColor( "Table.dropCellForeground");
            bg = UIManager.getColor("Table.dropCellBackground");

            isSelected = true;
        }

        if (isSelected) {
            super.setForeground(fg == null ? table.getSelectionForeground()
                    : fg);
            super.setBackground(bg == null ? table.getSelectionBackground()
                    : bg);
        } else {
            Color background = unselectedBackground != null
                    ? unselectedBackground
                            : table.getBackground();
            if (background == null || background instanceof javax.swing.plaf.UIResource) {
                Color alternateColor = UIManager.getColor("Table.alternateRowColor");
                if (alternateColor != null && row % 2 != 0) {
                    background = alternateColor;
                }
            }
            super.setForeground(unselectedForeground != null
                    ? unselectedForeground
                            : table.getForeground());
            super.setBackground(background);
        }

        setFont(table.getFont());

        if (hasFocus) {
            Border border = null;
            if (isSelected) {
                border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = UIManager.getBorder("Table.focusCellHighlightBorder");
            }
            setBorder(border);

            if (!isSelected && table.isCellEditable(row, column)) {
                Color col;
                col = UIManager.getColor( "Table.focusCellForeground");
                if (col != null) {
                    super.setForeground(col);
                }
                col = UIManager.getColor("Table.focusCellBackground");
                if (col != null) {
                    super.setBackground(col);
                }
            }
        } else {
            setBorder(getNoFocusBorder());
        }

        int modelColumn = table.convertColumnIndexToModel(column);

        setValue(value, modelColumn);

        return this;
    }

    /*
     * The following methods are overridden as a performance measure to
     * to prune code-paths are often called in the case of renders
     * but which we know are unnecessary.  Great care should be taken
     * when writing your own renderer to weigh the benefits and
     * drawbacks of overriding methods like these.
     */

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();
        if (p != null) {
            p = p.getParent();
        }

        // p should now be the JTable.
        boolean colorMatch = (back != null) && (p != null) &&
                back.equals(p.getBackground()) &&
                p.isOpaque();
        return !colorMatch && super.isOpaque();
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     *
     * @since 1.5
     */
    @Override
    public void invalidate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void validate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void revalidate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void repaint(Rectangle r) { }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     *
     * @since 1.5
     */
    @Override
    public void repaint() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // Strings get interned...
        if (propertyName=="text"
                || propertyName == "labelFor"
                || propertyName == "displayedMnemonic"
                || ((propertyName == "font" || propertyName == "foreground")
                        && oldValue != newValue
                        && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {

            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a>
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }




    /**
     * A subclass of <code>DefaultTableCellRenderer</code> that
     * implements <code>UIResource</code>.
     * <code>DefaultTableCellRenderer</code> doesn't implement
     * <code>UIResource</code>
     * directly so that applications can safely override the
     * <code>cellRenderer</code> property with
     * <code>DefaultTableCellRenderer</code> subclasses.
     * <p>
     * <strong>Warning:</strong>
     * Serialized objects of this class will not be compatible with
     * future Swing releases. The current serialization support is
     * appropriate for short term storage or RMI between applications running
     * the same version of Swing.  As of 1.4, support for long term storage
     * of all JavaBeans<sup><font size="-2">TM</font></sup>
     * has been added to the <code>java.beans</code> package.
     * Please see {@link java.beans.XMLEncoder}.
     */
    public static class UIResource extends DefaultTableCellRenderer
    implements javax.swing.plaf.UIResource
    {
    }






    ///////////////////// CODE WRITTEN BY P. HERMANOWICZ 2014 ///////////////////////


    public DecimalUnitTableCellRenderer(DecimalFormat format) 
    {
        this.format = format;
    }


    @Override
    public void setPreferences(Preferences pref)
    {
        this.pref = pref;
        pullPreferences(pref);
    }

    private void pullPreferences(Preferences pref)
    {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

        boolean groupingUsed = pref.getBoolean(GROUPING_USED, false);       
        showTrailingZeroes = pref.getBoolean(TRAILING_ZEROES, false);
        char groupingSeparator = (char) pref.getInt(GROUPING_SEPARATOR, ' ');               
        char decimalSeparator = (char) pref.getInt(DECIMAL_SEPARATOR, '.');             
        int maxDigit = pref.getInt(MAX_FRACTION_DIGITS, 4);
        int minDigit = showTrailingZeroes ? maxDigit : 0;

        symbols.setDecimalSeparator(decimalSeparator);
        symbols.setGroupingSeparator(groupingSeparator);

        format.setMaximumFractionDigits(maxDigit);
        format.setMinimumFractionDigits(minDigit);
        format.setGroupingUsed(groupingUsed);

        format.setDecimalFormatSymbols(symbols);
    }

    @Override
    public void saveToPreferences()
    {
        if(pref != null)
        {
            pref.putInt(MAX_FRACTION_DIGITS, getMaximumFractionDigits());
            pref.putBoolean(GROUPING_USED, isGroupingUsed());
            pref.putInt(GROUPING_SEPARATOR, getGroupingSeparator());
            pref.putInt(DECIMAL_SEPARATOR, getDecimalSeparator());
            pref.putBoolean(TRAILING_ZEROES, isShowTrailingZeroes());
        }
    }


    public void setValue(Object entry, int modelColumn)
    {
        if (entry instanceof Double) 
        {
            String text = entry.equals(Double.NaN) ? "IND" : format.format(((Double) entry).doubleValue()*unitConversionFactors[modelColumn]);
            setText(text);
        }
        else
        {
            String text = (entry != null) ? entry.toString() : "";
            setText(text);
        }
    }

    @Override
    public String getValue(Object entry, int modelColumn)
    {
        String text;
        if (entry instanceof Double) 
        {
            text = entry.equals(Double.NaN) ? "IND" : format.format(((Double) entry).doubleValue()*unitConversionFactors[modelColumn]);
        }
        else
        {
            text = (entry != null) ? entry.toString() : "";
        }

        return text;
    }

    @Override
    public int getMaximumFractionDigits()
    {
        return format.getMaximumFractionDigits();
    }           

    @Override
    public void setMaximumFractionDigits(int maxDigitsNew)
    {
        int maxDigitsOld = format.getMaximumFractionDigits();
        format.setMaximumFractionDigits(maxDigitsNew);
        if(showTrailingZeroes)
        {
            format.setMinimumFractionDigits(maxDigitsNew);
        }    

        if(maxDigitsOld != maxDigitsNew)
        {
            for(FormattableNumericalDataListener l : listeners) 
            {
                l.maximumFractionDigitsChanged(maxDigitsNew);
            }
        }
    }

    @Override
    public boolean isGroupingUsed()
    {
        return format.isGroupingUsed();
    }

    @Override
    public void setGroupingUsed(boolean groupingUsedNew)
    {
        boolean groupingUsedOld = format.isGroupingUsed();
        format.setGroupingUsed(groupingUsedNew);

        if(groupingUsedOld != groupingUsedNew)
        {
            for(FormattableNumericalDataListener l : listeners) 
            {
                l.groupingUsedChanged(groupingUsedNew);
            }
        }
    }

    @Override
    public char getGroupingSeparator()
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();

        return symbols.getGroupingSeparator();
    }

    @Override
    public void setGroupingSeparator(char separatorNew)
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();

        char separatorOld = symbols.getGroupingSeparator();
        symbols.setGroupingSeparator(separatorNew);

        format.setDecimalFormatSymbols(symbols);


        if(separatorOld != separatorNew)
        {
            for(FormattableNumericalDataListener l : listeners) 
            {
                l.groupingSeparatorChanged(separatorNew);
            }
        }
    }

    @Override
    public char getDecimalSeparator()
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        return symbols.getDecimalSeparator();
    }



    @Override
    public void setDecimalSeparator(char separatorNew)
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();

        char separatorOld = symbols.getDecimalSeparator();
        symbols.setDecimalSeparator(separatorNew);

        format.setDecimalFormatSymbols(symbols);

        if(separatorOld != separatorNew)
        {
            for(FormattableNumericalDataListener l : listeners) 
            {
                l.decimalSeparatorChanged(separatorNew);
            }
        }
    }

    @Override
    public boolean isShowTrailingZeroes()
    {
        return showTrailingZeroes;
    }

    @Override
    public void setShowTrailingZeroes(boolean trailingZeroesNew)
    {
        boolean showTrailingZeroesOld = this.showTrailingZeroes;
        this.showTrailingZeroes = trailingZeroesNew;

        int minDigits = trailingZeroesNew ? format.getMaximumFractionDigits() : 0;
        format.setMinimumFractionDigits(minDigits);

        if(showTrailingZeroesOld != trailingZeroesNew){
            for(FormattableNumericalDataListener l : listeners) 
            {
                l.showTrailingZeroesChanged(this.showTrailingZeroes);
            }
        }     
    }

    @Override
    public DecimalFormat getDecimalFormat()
    {
        return format;
    }

    @Override
    public FormattableNumericalDataState getState()
    {
        return new FormattableNumericalDataState(getMaximumFractionDigits(),
                showTrailingZeroes, isGroupingUsed(), getGroupingSeparator(), getDecimalSeparator());
    }

    @Override
    public void setState(FormattableNumericalDataState memento)
    {
        setMaximumFractionDigits(memento.getMaximumFractionDigits());
        setShowTrailingZeroes(memento.isShowTrailingZeroes());
        setGroupingUsed(memento.isGroupingUsed());
        setGroupingSeparator(memento.getGroupingSeparator());
        setDecimalSeparator(memento.getDecimalSeparator());
    }   

    public void setDecimalFormat(DecimalFormat format)
    {
        this.format = format;
    }

    @Override
    public void addListener(FormattableNumericalDataListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(FormattableNumericalDataListener listener) {
        listeners.remove(listener);
    }
}
