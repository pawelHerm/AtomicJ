
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;

public class ItemPopupMenuList <E> extends ItemList <E> 
{
    private static final long serialVersionUID = 1L;

    private final ItemListMaster<E> master;

    public ItemPopupMenuList(ItemListMaster<E> master)
    {
        this.master = master;
        createAndRegisterPopupMenu();
        createAndRegisterKeyListener();
    }

    private void createAndRegisterKeyListener()
    {
        addKeyListener(new KeyListener() 
        {
            @Override
            public void keyPressed(KeyEvent e) 
            {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                {
                    List<E> selectedItems = getSelectedValuesList();
                    master.removeItems(selectedItems);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) { }

            @Override
            public void keyTyped(KeyEvent e) { }
        });
    }

    private void createAndRegisterPopupMenu() 
    {
        final JPopupMenu popup = new JPopupMenu();

        JMenuItem itemDelete = new JMenuItem("Delete");
        itemDelete.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                List<E> selectedItems = getSelectedValuesList();        		
                master.removeItems(selectedItems);
            }
        });
        popup.add(itemDelete);

        //Add listener to the text area so the popup menu can come up.

        MouseListener listener = new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)  {check(e);}

            @Override
            public void mouseReleased(MouseEvent e) {check(e);}

            public void check(MouseEvent e) 
            {
                if (e.isPopupTrigger()) 
                {
                    if(isNonEmpty())
                    {
                        int clicked = locationToIndex(e.getPoint());
                        if(!isSelectedIndex(clicked))
                        {
                            setSelectedIndex(clicked); 
                        }		 
                        popup.show(ItemPopupMenuList.this, e.getX(), e.getY());
                    }		    	
                }
            }
        };
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        addMouseListener(listener);
    }
}
