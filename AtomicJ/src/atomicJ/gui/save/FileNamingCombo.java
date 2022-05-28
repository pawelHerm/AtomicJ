
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


import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;

import atomicJ.gui.NameComponent;


public class FileNamingCombo extends JComboBox<Object>
{
    private static final long serialVersionUID = 1L;

    private final static NameCellRenderer renderer =  new NameCellRenderer();

    private final FileNamingComboType type;
    private String key;

    public FileNamingCombo(Object[] items, FileNamingComboType type, String key)
    {
        super(items);
        this.type = type;
        this.key = key;
        setRenderer(renderer);
        setEditable(true);
        setEditor(new NameComboEditor());		
    }

    public FileNamingComboType getType()
    {
        return type;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    enum FileNamingComboType
    {
        PREFIX, ROOT, SUFFIX;
    }

    private static class NameCellRenderer implements ListCellRenderer<Object> 
    {
        protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) 
        {
            Component component = defaultRenderer.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            Font font = component.getFont();
            if (value.equals(NameComponent.NAME)||value.equals(NameComponent.SERIAL_NUMBER)) 
            {
                component.setBackground(UIManager.getColor("Label.background"));
            }
            else 
            {
                component.setFont(font.deriveFont(Font.ITALIC));
            }
            return component;
        }
    }

    private static class NameComboEditor implements ComboBoxEditor 
    {
        private final JTextField editor= new JTextField();
        private final EventListenerList listenerList = new EventListenerList();

        public NameComboEditor() 
        {
            editor.setBorder(null);
            editor.setDisabledTextColor(Color.black);
        }

        @Override
        public void addActionListener(ActionListener l) 
        {
            listenerList.add(ActionListener.class, l);
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
            listenerList.remove(ActionListener.class, l);
        }

        @Override
        public void selectAll() 
        {
        }

        @Override
        public void setItem(Object newValue) 
        {
            String text = newValue.toString();
            editor.setText(text);			

            if(NameComponent.NAME.equals(newValue)||NameComponent.SERIAL_NUMBER.equals(newValue))
            {
                editor.setEnabled(false);
                editor.setFont(editor.getFont().deriveFont(Font.PLAIN));
            }
            else
            {
                editor.setEnabled(true);
                editor.setFont(editor.getFont().deriveFont(Font.ITALIC));
                editor.selectAll();
            }
        }
    }
}

