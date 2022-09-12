
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

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.text.InternationalFormatter;
import javax.swing.text.NumberFormatter;

public class NumericalField extends JFormattedTextField
{
    private static final long serialVersionUID = 1L;
    public static final String VALUE_EDITED = "valueEdited";

    private static final String DEFAULT_MESSAGE = "Invalid input: only numeric values are allowed";	
    private static final NumberFormat DEFAULT_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    static {DEFAULT_FORMAT.setMaximumFractionDigits(8);}	

    private String invalidInputMessage;
    private MessageDisplayer messageDisplayer;

    public NumericalField()
    {
        this(DEFAULT_FORMAT);
    }

    public NumericalField(String invalidInputMessage)
    {
        this(DEFAULT_FORMAT, invalidInputMessage, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public NumericalField(String invalidInputMessage, double min, double max)
    {
        this(DEFAULT_FORMAT, invalidInputMessage, min, max);
    }

    public NumericalField(NumberFormat format)
    {
        this(format, DEFAULT_MESSAGE, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public NumericalField(String invalidInputMessage, double min)
    {
        this(DEFAULT_FORMAT, invalidInputMessage, min, Double.MAX_VALUE);
    }

    public NumericalField(String invalidInputMessage, double min, boolean acceptReals)
    {
        this(DEFAULT_FORMAT, invalidInputMessage, min, Double.MAX_VALUE, acceptReals);
    }

    public NumericalField(NumberFormat format, String invalidInputMessage, double min)
    {
        this(format, invalidInputMessage, min, Double.MAX_VALUE);
    }

    public NumericalField(NumberFormat format,  String invalidInputMessage, double min, double max)
    {
        this(format, invalidInputMessage, min, max, true);
    }

    public NumericalField(NumberFormat format,  String invalidInputMessage, double min, double max, boolean acceptReals)
    {
        super(new PermissiveNumberFormatter(format, min, max, acceptReals));
        setColumns(6);

        setHorizontalAlignment(SwingConstants.RIGHT);
        this.messageDisplayer = new DefaultMessageDisplayer(this);
        this.invalidInputMessage = invalidInputMessage;
    }

    public NumericalField(String invalidInputMessage, NumberFormatter formatter)
    {
        super(formatter);
        setColumns(6);

        setHorizontalAlignment(SwingConstants.RIGHT);
        this.messageDisplayer = new DefaultMessageDisplayer(this);
        this.invalidInputMessage = invalidInputMessage;
    }

    public void setMessageDisplayer(MessageDisplayer displayer)
    {
        this.messageDisplayer = displayer;
    }

    public void setMessage(String invalidInputMessageNew)
    {
        this.invalidInputMessage = invalidInputMessageNew;
    }

    public void setMinimum(double min)
    {
        InternationalFormatter formatter = (InternationalFormatter)getFormatter();
        formatter.setMinimum(min);
    }

    public void setMaximum(double max)
    {
        InternationalFormatter formatter = (InternationalFormatter)getFormatter();
        formatter.setMaximum(max);
    }

    @Override
    public void commitEdit() throws ParseException
    {
        Object valueOld = getValue();

        super.commitEdit();

        Object valueNew = getValue();

        firePropertyChange(VALUE_EDITED, valueOld, valueNew);
    }

    @Override
    public Number getValue()
    {
        Number value = (Number)super.getValue();

        Double v = (value == null) ? Double.NaN :Double.valueOf(value.doubleValue());
        return v;
    }

    @Override
    public void processFocusEvent(FocusEvent event)
    {
        if(event.getID() == FocusEvent.FOCUS_LOST && (!event.isTemporary())&&(!isEditValid()))
        {  
            displayErrorMessage();
        }
        super.processFocusEvent(event);
    }

    @Override
    public void invalidEdit()
    {
        displayErrorMessage();
    }

    @Override
    public void setValue(Object val)
    {
        super.setValue(val);
        clearErrorMessage();
    }

    private void displayErrorMessage()
    {
        messageDisplayer.publishErrorMessage(invalidInputMessage);
    }

    private void clearErrorMessage()
    {
        messageDisplayer.clearMessage();
    }

    public static class DefaultMessageDisplayer implements MessageDisplayer
    {
        private final Component parentComponent;

        public DefaultMessageDisplayer(Component parentComponent)
        {
            this.parentComponent = parentComponent;
        }

        @Override
        public void publishErrorMessage(String message) 
        {
            JOptionPane.showMessageDialog(parentComponent, message, "", JOptionPane.ERROR_MESSAGE);              
        }

        @Override
        public void publishWarningMessage(String message) 
        {
            JOptionPane.showMessageDialog(parentComponent, message, "", JOptionPane.WARNING_MESSAGE);              
        }

        @Override
        public void publishInformationMessage(String message)
        {
            JOptionPane.showMessageDialog(parentComponent, message, "", JOptionPane.INFORMATION_MESSAGE);              
        }

        @Override
        public void clearMessage() {            
        }
    }
}