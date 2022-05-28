
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

import static atomicJ.gui.PreferenceKeys.WINDOW_HEIGHT;
import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_X;
import static atomicJ.gui.PreferenceKeys.WINDOW_LOCATION_Y;
import static atomicJ.gui.PreferenceKeys.WINDOW_WIDTH;

import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;

import javax.swing.*;

import atomicJ.readers.SourceReadingModel;
import atomicJ.sources.ChannelSource;
import atomicJ.utilities.GUIUtilities;



public class FileOpeningWizard<E extends ChannelSource> extends JDialog implements ActionListener, MessageDisplayer
{
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_HEIGHT = (int)Math.round(0.6*Toolkit.getDefaultToolkit().getScreenSize().height);
    private static final int DEFAULT_WIDTH = Math.min(DEFAULT_HEIGHT, Toolkit.getDefaultToolkit().getScreenSize().width);

    private final Preferences pref = Preferences.userRoot().node(getClass().getName());

    private static final String CANCEL_ACTION_COMMAND = "CancelCommand";

    private final JLabel labelTaskName = new JLabel();
    private final JLabel labelTaskDescription = new JLabel();
    private final JLabel labelMessage = new JLabel();

    private final JButton buttonCancel = new JButton("Cancel");

    private final JPanel panelPages = new JPanel(new BorderLayout());
    private final JPanel panelControls = new JPanel(new BorderLayout());
    private final OpenSourceSelectionPageExperimental<E> pageSourceSelection;
    private final OpeningModelStandard<E> model;

    public FileOpeningWizard(OpeningModelStandard<E> model, SourceReadingModel<E> readingModel)
    {
        super(model.getParent(), "Open files", ModalityType.MODELESS);

        this.model = model;
        Container content = getContentPane();
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        JPanel panelTaskInformation = buildTaskInformationPanel();
        JPanel panelButtons = buildButtonPanel();	

        pageSourceSelection = new OpenSourceSelectionPageExperimental<>(model,readingModel);

        panelPages.add(pageSourceSelection.getView(), BorderLayout.CENTER);
        panelControls.add(pageSourceSelection.getControls(), BorderLayout.CENTER);

        String name = pageSourceSelection.getTaskName();
        String description = pageSourceSelection.getTaskDescription();

        labelTaskName.setText(name);
        labelTaskDescription.setText(description);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                FileOpeningWizard.this.endPreview();
            }
        });

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent evt)
            {				
                pref.putInt(WINDOW_HEIGHT, FileOpeningWizard.this.getHeight());
                pref.putInt(WINDOW_WIDTH, FileOpeningWizard.this.getWidth());
                pref.putInt(WINDOW_LOCATION_X, (int) FileOpeningWizard.this.getLocation().getX());			
                pref.putInt(WINDOW_LOCATION_Y, (int) FileOpeningWizard.this.getLocation().getY());
            }
        });


        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelControls, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(
                        layout.createParallelGroup()
                        .addComponent(panelButtons)
                        .addComponent(panelPages, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelTaskInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
                );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(panelTaskInformation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup()
                        .addComponent(panelControls)
                        .addComponent(panelPages, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addComponent(panelButtons, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                );

        int height = pref.getInt(WINDOW_HEIGHT,DEFAULT_HEIGHT);
        int width =  pref.getInt(WINDOW_WIDTH,DEFAULT_WIDTH);
        if(GUIUtilities.isWindowSizeWellSpecified(width, height))
        {
            setSize(width, height);
        }
        else
        {
            setSize(DEFAULT_WIDTH,DEFAULT_HEIGHT);
        }

        setLocationRelativeTo(model.getParent());
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        String command = evt.getActionCommand();
        if(command.equals(CANCEL_ACTION_COMMAND))
        {
            endPreview();
        }
    }	

    private JPanel buildTaskInformationPanel()
    {
        SubPanel panel = new SubPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));	

        Color color = UIManager.getColor("Label.background");
        panel.setBackground(color);
        labelTaskName.setBackground(color);
        labelTaskDescription.setBackground(color);
        labelMessage.setBackground(color);

        Font font = UIManager.getFont("TextField.font");
        labelTaskName.setFont(font.deriveFont(Font.BOLD));
        labelTaskDescription.setFont(font);

        panel.addComponent(labelTaskName, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        panel.addComponent(labelTaskDescription, 0, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);
        panel.addComponent(labelMessage, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 1, 1);

        return panel;
    }

    private JPanel buildButtonPanel()
    {
        buttonCancel.setMnemonic(KeyEvent.VK_C);
        buttonCancel.setActionCommand(CANCEL_ACTION_COMMAND);
        buttonCancel.addActionListener(this);

        JPanel panelButtons = new JPanel();	

        GroupLayout layout = new GroupLayout(panelButtons);
        panelButtons.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonCancel));

        return panelButtons;
    }

    public void endPreview()
    {
        pageSourceSelection.cancel();
        model.clear();
        setVisible(false);
    }

    @Override
    public void publishErrorMessage(String message) 
    {
        Icon icon = UIManager.getIcon("OptionPane.errorIcon");
        labelMessage.setIcon(icon);
        labelMessage.setText(message);
        pack();
        validate();
    }

    @Override
    public void publishWarningMessage(String message) 
    {
        Icon icon = UIManager.getIcon("OptionPane.warningIcon");
        labelMessage.setIcon(icon);
        labelMessage.setText(message);
        pack();
        validate();
    }

    @Override
    public void publishInformationMessage(String message) 
    {
        Icon icon = UIManager.getIcon("OptionPane.informationIcon");
        labelMessage.setIcon(icon);
        labelMessage.setText(message);
        pack();
        validate();
    }

    @Override
    public void clearMessage() 
    {
        labelMessage.setIcon(null);
        labelMessage.setText(null);
        pack();
        validate();
    }
}
