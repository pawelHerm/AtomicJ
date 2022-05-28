
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

public interface WizardPageModel extends PropertyChangeSource
{
    // next(), back() i skip() mog³yby zwracaæ nastêpne WizardPageModel. Dziêki temu WizardModel mog³by nic nie wiedziec o rodzaju WizardPage,
    //ktory przechowuje
    public String getTaskName();
    public String getTaskDescription();

    public boolean isFirst();
    public boolean isLast();

    public void back();
    public void next();
    public void skip();
    public void finish();
    public void cancel();

    public boolean isBackEnabled();
    public boolean isNextEnabled();
    public boolean isSkipEnabled();
    public boolean isFinishEnabled();

    public boolean isNecessaryInputProvided();
}
