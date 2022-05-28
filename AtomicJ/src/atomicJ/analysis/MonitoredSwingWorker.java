
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

package atomicJ.analysis;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

public abstract class MonitoredSwingWorker<K,V> extends SwingWorker<K,V>
{
    public static final String STATE = "state";
    public static final String STEP = "step";
    private final ProgressMonitor progressMonitor;
    private int step = 0;

    private boolean runOutOfMemory = false;

    public MonitoredSwingWorker(Component parent, String message, final String actionName, final int problemSize)
    {	
        this.progressMonitor = new ProgressMonitor(parent, message,"", 0, 100);

        addPropertyChangeListener(STEP, new PropertyChangeListener() 
        {
            @Override
            public  void propertyChange(PropertyChangeEvent evt) 
            {                
                if (progressMonitor.isCanceled() || isDone()) 
                {
                    progressMonitor.close();
                    cancelAllTasks();
                    return;
                } 
                else 
                {
                    int step = (Integer)evt.getNewValue();
                    int progress = Math.round(100*(step)/problemSize);

                    String message = actionName + " " + progress + "%" + "   (" + step + " of " + problemSize +")";

                    progressMonitor.setNote(message);
                    progressMonitor.setProgress(progress);
                }
            }
        });

        addPropertyChangeListener(STATE, new PropertyChangeListener() 
        {
            @Override
            public  void propertyChange(PropertyChangeEvent evt) 
            {
                if (progressMonitor.isCanceled() || isDone()) 
                {                    
                    progressMonitor.close();
                    cancelAllTasks();
                    return;
                } 
            }
        });
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        PropertyChangeSupport support = getPropertyChangeSupport();
        support.addPropertyChangeListener(propertyName, listener);
    }

    protected void setStep(int stepNew)
    {
        int stepOld = this.step;
        this.step = stepNew;

        firePropertyChange(STEP, stepOld, stepNew);
    }

    protected void updateProgressMonitor(final String message)
    {
        progressMonitor.setNote(message);
    }

    public abstract void cancelAllTasks();

    public void closeProgressMonitor()
    {
        progressMonitor.close();
    }


    public boolean isRunOutOfMemory()
    {
        return runOutOfMemory;
    }

    public void setRunOutOfMemory()
    {
        this.runOutOfMemory = true;
    }

    @Override
    protected void done()
    {
        try {
            get();//called to get exceptions
        } catch (InterruptedException | ExecutionException e) 
        {
            Logger.getLogger("").log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        if(runOutOfMemory)
        {
            handleOOME();
        }
    }

    protected String getOOMETip()
    {
        return "";
    }

    private void handleOOME()
    {	    		 
        Object[] options = {"Yes", "No"};
        int n = JOptionPane.showOptionDialog(null,  "<html>AtomicJ has run out of memory. The application will probably not work properly now." 
                + getOOMETip() + "<br>Do you want to close AtomicJ now?</html>","Out of memory error", JOptionPane.YES_NO_OPTION,JOptionPane.ERROR_MESSAGE, null,  options, options[0]); 

        if(n == 0)
        {
            System.exit(-1);
        }
    }
}
