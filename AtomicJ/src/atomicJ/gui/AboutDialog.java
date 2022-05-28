
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013 - 2020 by Pawe³ Hermanowicz
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;

public class AboutDialog extends JDialog
{
    private static final long serialVersionUID = 1L;

    public AboutDialog(JFrame parent)
    {
        super(parent, "About " + AtomicJ.APPLICATION_NAME, true);

        Container content = getContentPane();
        content.setLayout(new BorderLayout());

        JPanel panelOK = new JPanel();
        JPanel mainPanel = new JPanel(new BorderLayout());

        ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage("Resources/image3.png"));
        String labelText =
                "<html><B><center><font size = +2>" + AtomicJ.APPLICATION_NAME + "</B></font>" +
                        "<BR><center>Version " + AtomicJ.APPLICATION_VERSION +" <BR>Created by Pawe³ Hermanowicz <BR> " + AtomicJ.CONTACT_MAIL +" </center>";
        JLabel authorLabel = new JLabel(labelText,icon,SwingConstants.CENTER);
        authorLabel.setIconTextGap(24);
        authorLabel.setBorder(BorderFactory.createEmptyBorder(5,5,10,5));

        JTextArea copyrightArea = new JTextArea(15,50);
        copyrightArea.setEditable(false);
        String copyrightNotice = AtomicJ.COPYRRIGHT_NOTICE;
        copyrightArea.setText(copyrightNotice);
        copyrightArea.setLineWrap(true);
        copyrightArea.setWrapStyleWord(true);
        copyrightArea.setFont(new Font("Monospaced", Font.PLAIN,12));

        JScrollPane scrollPane = new JScrollPane(copyrightArea);

        mainPanel.add(authorLabel,BorderLayout.NORTH);
        mainPanel.add(scrollPane,BorderLayout.CENTER);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton buttonOK = new JButton(new OKAction());
        panelOK.add(buttonOK);

        content.add(mainPanel,BorderLayout.CENTER);
        content.add(panelOK,BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
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
            AboutDialog.this.setVisible(false);
        };
    }
}
