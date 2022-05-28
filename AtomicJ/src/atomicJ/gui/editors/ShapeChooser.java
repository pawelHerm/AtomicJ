
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

package atomicJ.gui.editors;


import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

import atomicJ.gui.PlotStyleUtilities;


public class ShapeChooser extends JPanel
{
    private static final long serialVersionUID = 1L;
    private static final Shape[] shapes = PlotStyleUtilities.getNonZeroAreaShapes();	

    private final JComboBox<Shape> markerShapeCombo = new JComboBox<>(shapes);	
    private final SeriesSubeditor editor;

    public ShapeChooser(SeriesSubeditor editor)
    {
        this.editor = editor;
        int markerIndex = editor.getMarkerIndex();
        setLayout(new GridLayout(1, 0, 2, 2));

        markerShapeCombo.setRenderer(new GraphicsCellRenderer());
        markerShapeCombo.setSelectedIndex(markerIndex);
        JPanel shapePanel = new JPanel();
        shapePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Shape"));	
        shapePanel.add(markerShapeCombo);
        add(shapePanel);
    }

    public int getSelectedMarkerIndex()
    {
        int selected = markerShapeCombo.getSelectedIndex();
        return selected;
    }


    private class GraphicsCellRenderer extends JLabel implements ListCellRenderer<Shape> 
    {
        private static final long serialVersionUID = 1L;

        private GraphicsCellRenderer() 
        {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Shape> list, Shape value, int index, boolean isSelected, boolean cellHasFocus)
        {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } 
            else 
            {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            };
            BufferedImage img = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);		
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(editor.getPaint());
            g2.fill(value);
            setIcon(new ImageIcon(img));

            return this;
        }
    }
}
