
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

package atomicJ.gui.stack;

import java.awt.Window;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import atomicJ.analysis.*;
import atomicJ.data.Channel2D;
import atomicJ.sources.MapSource;


public class StackTask extends MonitoredSwingWorker<List<Channel2D>, Void> 
{
    private final MapSource<?> mapSource;
    private final List<Channel2D> channels = new ArrayList<>();

    private final NumberFormat format;

    private final double minimum;
    private final double step;
    private final int frameCount;
    private final StackingPackFunctionFactory<?> factory;

    public StackTask(StackingPackFunctionFactory<?> factory, NumberFormat format, double minimum, double step, int frameCount, MapSource<?> mapSource, Window parent)
    {
        super(parent, "Stack generating in progress", "Generated", frameCount);
        this.factory = factory;
        this.format = format;
        this.minimum = minimum;
        this.step = step;
        this.frameCount = frameCount;
        this.mapSource = mapSource;
    }

    @Override
    public List<Channel2D> doInBackground() 
    {						
        String unit = factory.getStackingQuantity().getFullUnitName();
        for(int i = 0;i<frameCount;i++)
        {
            double level = minimum + step*i;
            ProcessedPackFunction f = factory.getFunction(level);
            Channel2D channel = mapSource.getChannel(f);
            channels.add(channel);

            String info = format.format(level) + " " + unit;
            channel.setChannelInfo(info);
            setStep(i + 1);
        }
        return channels;
    }

    @Override
    public void cancelAllTasks() 
    {
        cancel(true);
    }
}
