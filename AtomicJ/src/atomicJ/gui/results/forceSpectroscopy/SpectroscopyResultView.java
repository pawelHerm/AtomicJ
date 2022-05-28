
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

package atomicJ.gui.results.forceSpectroscopy;

import java.awt.*;
import javax.swing.*;

import atomicJ.analysis.ProcessedSpectroscopyPack;
import atomicJ.analysis.SpectroscopyResultDestination;
import atomicJ.gui.results.ResultView;
import atomicJ.sources.SimpleSpectroscopySource;

//class is marked is final, as the constructor calls a potentially overridable method
public final class SpectroscopyResultView extends ResultView <SimpleSpectroscopySource, ProcessedSpectroscopyPack>
{
    private static final long serialVersionUID = 1L;

    public SpectroscopyResultView(SpectroscopyResultTable table, SpectroscopyResultDestination resultDestination)
    {
        super(table, resultDestination);

        Action markOnMapAction = table.getMarkOnMapAction();
        addNewCurvesMenAction(markOnMapAction);

        ImageIcon iconMarkOnMapDisabled = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Resources/MarkOnMapForDisabled.png"));
        addNewToolbarAction(markOnMapAction, new ImageIcon(GrayFilter.createDisabledImage(iconMarkOnMapDisabled.getImage())));
    }
}
