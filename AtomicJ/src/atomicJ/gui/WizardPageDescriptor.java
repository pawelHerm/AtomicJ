
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

public class WizardPageDescriptor 
{
    private final boolean isFirst;
    private final boolean isLast;
    private final boolean optional;

    private final String subPageBackCommand;
    private final String subPageNextCommand;

    private final String taskName;
    private final String taskDescription;

    public WizardPageDescriptor(String taskName, String taskDescription, boolean isFirst, boolean isLast, boolean optional)
    {
        this(taskName, taskDescription, isFirst, isLast, optional, "","");
    }

    public WizardPageDescriptor(String taskName, String taskDescription, boolean isFirst, boolean isLast,
            boolean optional, String subPageBackCommand, String subPageNextCommand)
    {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.isFirst = isFirst;
        this.isLast = isLast;
        this.optional = optional;

        this.subPageBackCommand = subPageBackCommand;
        this.subPageNextCommand = subPageNextCommand;
    }

    public String getSubPageNextCommand()
    {
        return subPageNextCommand;
    }

    public String getSubPageBackCommand()
    {
        return subPageBackCommand;
    }

    public boolean isFirst()
    {
        return isFirst;
    }

    public boolean isLast()
    {
        return isLast;
    }

    public boolean canBeSkipped()
    {
        return optional;
    }

    public String getTaskName()
    {
        return taskName;
    }

    public String getTaskDescription()
    {
        return taskDescription;
    }
}
