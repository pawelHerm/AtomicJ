
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
import java.awt.event.ActionListener;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;


public class EditableComboBox extends JComboBox<String>
{
    private static final long serialVersionUID = 1L;

    public EditableComboBox(String[] items)
    {
        super(items);
        setEditable(true);
        setEditor(new NameComboEditor());		
    }


    private static class NameComboEditor implements ComboBoxEditor 
    {
        private final JTextField editor = new JTextField();

        public NameComboEditor() 
        {
            editor.setBorder(null);
        }

        @Override
        public void addActionListener(ActionListener l) 
        {
            editor.addActionListener(l);
        }

        @Override
        public Component getEditorComponent() {
            return editor;
        }

        @Override
        public Object getItem() 
        {
            String text = editor.getText();
            return text;
        }

        @Override
        public void removeActionListener(ActionListener l) {
            editor.removeActionListener(l);
        }

        @Override
        public void selectAll() 
        {
            editor.selectAll();
        }

        @Override
        public void setItem(Object newValue) 
        {
            String text = newValue.toString();
            editor.setText(text);			
        }
    }
}

