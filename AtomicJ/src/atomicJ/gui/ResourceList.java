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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import atomicJ.gui.editors.FontChooserDialog;
import atomicJ.resources.Resource;
import atomicJ.utilities.SerializationUtilities;


public class ResourceList <E extends Resource> extends ItemList<E>
{
    private static final long serialVersionUID = 1L;

    public static String USE_SHORT_RESOURCE_NAME = "UseShortResourceName";

    private FontChooserDialog fontChooserDialog;

    private final UseShortNamesAction useShortNamesAction = new UseShortNamesAction();
    private final ChangeListFontAction changeListFontAction = new ChangeListFontAction();
    private final ResourceCellRenderer cellRenderer;

    public ResourceList(Preferences pref)
    {
        this.cellRenderer = new ResourceCellRenderer(pref);

        boolean useShortName = pref.getBoolean(USE_SHORT_RESOURCE_NAME, false);
        useShortNamesAction.putValue(Action.SELECTED_KEY, useShortName);

        Font resourceListFont = (Font)SerializationUtilities.getSerializableObject(pref, USE_SHORT_RESOURCE_NAME, getFont());

        cellRenderer.setUseShortName(useShortName);
        setFont(resourceListFont);

        setCellRenderer(cellRenderer);


        JPopupMenu popupMenu = buildDefaultPopupMenu();
        registerPopupMenu(popupMenu);
    }

    public void setUseShortNames(boolean useShortNamesNew)
    {
        boolean useShortNamesOld = cellRenderer.getUseShortName();

        if(useShortNamesOld != useShortNamesNew)
        {
            cellRenderer.setUseShortName(useShortNamesNew);
            repaint();

            firePropertyChange(USE_SHORT_RESOURCE_NAME, useShortNamesOld, useShortNamesNew);
        }
    }

    public String getDisplayedText(Resource resource)
    {
        return cellRenderer.getDisplayedText(resource);
    }

    public void attemptListFontSelection() 
    {
        if( fontChooserDialog == null)
        {
            this.fontChooserDialog = new FontChooserDialog(SwingUtilities.getWindowAncestor(this), "Font selection");
        }

        this.fontChooserDialog.showDialog(new FontReceiver() 
        {

            @Override
            public void setFont(Font newFont) 
            { 
                ResourceList.this.setFont(newFont);
                ResourceList.this.repaint();
            }

            @Override
            public Font getFont()
            {
                return ResourceList.this.getFont();
            }
        });
    }

    private JPopupMenu buildDefaultPopupMenu()
    {
        JPopupMenu popup = new JPopupMenu();        


        JCheckBoxMenuItem itemUseShortNames = new JCheckBoxMenuItem(useShortNamesAction);
        popup.add(itemUseShortNames);

        JMenuItem itemChangeFont = new JMenuItem(changeListFontAction);
        popup.add(itemChangeFont);


        return popup;
    }

    private void registerPopupMenu(final JPopupMenu menu)
    {
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
                        menu.show(ResourceList.this, e.getX(), e.getY());
                    }               
                }
            }
        };

        addMouseListener(listener);
    }


    private void createAndRegisterPopupMenu() 
    {}


    private class UseShortNamesAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public UseShortNamesAction() 
        {
            putValue(NAME, "Short names");
            putValue(SELECTED_KEY, false);
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            boolean useShortNames = (boolean) getValue(SELECTED_KEY);
            setUseShortNames(useShortNames);
        }
    }

    private class ChangeListFontAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ChangeListFontAction() 
        {
            putValue(NAME, "Change font");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            attemptListFontSelection();
        }
    }
}
