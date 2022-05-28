
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

package atomicJ.gui.save;

import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;

import atomicJ.gui.NumericalFormatStyle;
import atomicJ.gui.NumericFormatSelectionPanel;
import atomicJ.gui.StandardNumericalFormatStyle;

public abstract class AbstractTextFormatType implements ChartSaveFormatType
{
    private final Preferences pref = Preferences.userNodeForPackage(AbstractTextFormatType.class).node("NumericChartSave");
    private final NumericalFormatStyle formatModel =  new StandardNumericalFormatStyle(pref);
    private final NumericFormatSelectionPanel numericPanel  = new NumericFormatSelectionPanel(formatModel);
    private final JPanel wrappingPanel = buildWrappingPanel();	

    @Override
    public boolean supportMultiplePages()
    {
        return false;
    }

    private JPanel buildWrappingPanel()
    {
        JPanel wrappingPanel = new JPanel();
        GroupLayout layout = new GroupLayout(wrappingPanel);
        wrappingPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(numericPanel,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE));

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(numericPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));	

        return wrappingPanel;
    }

    public DecimalFormat getNumberFormat()
    {
        DecimalFormat format = (DecimalFormat) formatModel.getDecimalFormat().clone();
        return format;
    }

    @Override
    public void specifyInitialDimensions(Rectangle2D chartArea, Number dataWidthNew, Number dataHeightNew)
    {}

    @Override
    public boolean isNecessaryIputProvided()
    {
        return true;
    }

    protected NumericFormatSelectionPanel getNumericTableFormatPanel()
    {
        return numericPanel;
    }			

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {}

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {}

    @Override
    public JPanel getParametersInputPanel()
    {
        return wrappingPanel;
    }
}
