
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
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ShapeAndSizeChooser extends JDialog implements ChangeListener, ItemListener
{
    private static final long serialVersionUID = 1L;

    private float markerSize;
    private int markerIndex;

    private final float initMarkerSize;
    private final int initMarkerIndex;

    private final JSpinner spinnerMarkerSize;
    private final JComboBox<Shape> comboMarkerShape;	
    private final MarkerStyleReceiver receiver;

    public ShapeAndSizeChooser(Window parent, MarkerStyleReceiver editor, Shape[] shapes)
    {
        this(parent, editor, ModalityType.APPLICATION_MODAL, shapes);
    }

    public ShapeAndSizeChooser(Window parent, MarkerStyleReceiver editor, ModalityType modalityType, Shape[] shapes)
    {
        super(parent, "Marker style", ModalityType.MODELESS);		

        this.receiver = editor;
        this.initMarkerIndex = editor.getMarkerIndex();
        this.initMarkerSize = editor.getMarkerSize();

        setParametersToInitial();

        this.spinnerMarkerSize = new JSpinner(new SpinnerNumberModel(Float.valueOf(initMarkerSize),Float.valueOf(0.1f),Float.valueOf(Short.MAX_VALUE),Float.valueOf(0.1f)));   		        

        this.comboMarkerShape= new JComboBox<>(shapes);
        comboMarkerShape.setRenderer(new GraphicsCellRenderer());
        comboMarkerShape.setSelectedIndex(initMarkerIndex);	

        JPanel mainPanel = buildMainPanel();
        JPanel buttonPanel = buildButtonPanel();

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        initChangeListener();
        initItemListener();

        pack();
        setLocationRelativeTo(parent);
    }

    private void setParametersToInitial()
    {
        this.markerSize = this.initMarkerSize;
        this.markerIndex = this.initMarkerIndex;
    }

    private void initChangeListener()
    {
        this.spinnerMarkerSize.addChangeListener(this);
    }

    private void initItemListener()
    {
        this.comboMarkerShape.addItemListener(this);
    }

    public int getMarkerIndex()
    {
        int selected = comboMarkerShape.getSelectedIndex();
        return selected;
    }

    public float getMarkerSize()
    {
        return markerSize;
    }

    private void resetReceiver()
    {
        receiver.setMarkerIndex(markerIndex);
        receiver.setMarkerSize(markerSize);
    }

    private void resetChooser()
    {
        spinnerMarkerSize.setValue(markerSize);
        comboMarkerShape.setSelectedIndex(markerIndex);
    }

    private void cancel()
    {
        setParametersToInitial();

        resetReceiver();		
        resetChooser();

        setVisible(false);
    }

    private void reset()
    {
        setParametersToInitial();

        resetReceiver();		
        resetChooser();
    }


    @Override
    public void stateChanged(ChangeEvent evt) 
    {
        Object src = evt.getSource();

        if(spinnerMarkerSize == src)
        {
            markerSize = ((SpinnerNumberModel)spinnerMarkerSize.getModel()).getNumber().floatValue();
            receiver.setMarkerSize(markerSize);
        }
    }
    @Override
    public void itemStateChanged(ItemEvent evt)
    {
        Object source = evt.getSource();

        if(source == comboMarkerShape)
        {
            markerIndex = comboMarkerShape.getSelectedIndex();
            this.receiver.setMarkerIndex(markerIndex);
        }
    }

    private JPanel buildMainPanel()
    {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new GridLayout(1, 0, 2, 2));

        JPanel sizePanel = new JPanel();
        sizePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Size"));
        sizePanel.add(spinnerMarkerSize);

        JPanel shapePanel = new JPanel();
        shapePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Shape"));	
        shapePanel.add(comboMarkerShape);

        mainPanel.add(shapePanel);
        mainPanel.add(sizePanel);

        return mainPanel;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        JButton buttonOK = new JButton(new OKAction());
        JButton buttonReset = new JButton(new ResetAction());
        JButton buttonCancel = new JButton(new CancelAction());

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonOK).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(buttonReset).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonOK)
                .addComponent(buttonReset)
                .addComponent(buttonCancel));

        layout.linkSize(buttonOK, buttonReset, buttonCancel);

        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
    }


    private class OKAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public OKAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(NAME,"OK");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            setVisible(false);
        };
    }

    private class ResetAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ResetAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME,"Reset");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            reset();
        };
    }

    private class CancelAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public CancelAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME,"Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            cancel();
        };
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
            if (isSelected) 
            {
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
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(receiver.getMarkerFillPaint());
            g2.fill(value);

            if(receiver.getDrawMarkerOutline())
            {
                g2.setPaint(receiver.getMarkerOutlinePaint());
                g2.setStroke(receiver.getMarkerOutlineStroke());
                g2.draw(value);
            }

            setIcon(new ImageIcon(img));

            return this;
        }
    }
}
