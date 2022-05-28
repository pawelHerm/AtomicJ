
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

package atomicJ.gui.stack;

import atomicJ.gui.AbstractChartPanelFactory;
import atomicJ.gui.MapPanel;

public class StackPanel extends MapPanel
{
    private static final long serialVersionUID = 1L;

    public StackPanel(boolean addPopup)
    {
        this(addPopup, true);
    }

    public StackPanel(boolean addPopup, boolean allowROIbasedActions)
    {
        super(addPopup, allowROIbasedActions);
    }

    @Override
    protected boolean curveMayExistForThePoint(int x, int y)
    {
        return true;
    }

    public static class StackPanelFactory implements AbstractChartPanelFactory<StackPanel>
    {
        private static final  StackPanelFactory INSTANCE = new StackPanelFactory();

        public static StackPanelFactory getInstance()
        {
            return INSTANCE;
        }

        @Override
        public StackPanel buildEmptyPanel() 
        {
            return new StackPanel(true);
        }
    }
}
