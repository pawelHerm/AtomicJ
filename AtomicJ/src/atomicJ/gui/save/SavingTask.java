
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

import java.awt.Component;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;

import atomicJ.analysis.MonitoredSwingWorker;



public class SavingTask extends MonitoredSwingWorker<Void, Void> 
{
    private final List<? extends Saveable> packs;
    private final Component parent;		

    public SavingTask(Component parent, List<? extends Saveable> packs)
    {
        super(parent, "Saving in progress", "Saved", packs.size());

        this.packs = packs;		
        this.parent = parent;	
    }

    @Override
    public Void doInBackground() throws IOException
    {
        final int n = packs.size();

        for(int i = 0; i < n;i++)
        {
            Saveable pack = packs.get(i);
            pack.save();			
            setStep(i + 1);
        }
        return null;
    }	

    @Override
    protected void done()
    {
        super.done();

        if(isCancelled())
        {
            JOptionPane.showMessageDialog(parent, "Saving terminated", "", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void cancelAllTasks() 
    {
        cancel(true);
    }
}
