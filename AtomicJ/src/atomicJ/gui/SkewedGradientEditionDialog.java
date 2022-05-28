
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


import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class SkewedGradientEditionDialog extends JDialog implements GradientEditor, ItemListener, ChangeListener, ActionListener, PropertyChangeListener, ColorGradientReceiver
{
    private static final long serialVersionUID = 1L;

    private final Preferences pref = Preferences.userRoot().node(getClass().getName());

    private static final String NAME = "Color gradient editor";

    private static final String INVERT_ACTION_COMMAND = "INVERT_ACTION_COMMAND";
    private static final String IMPORT_ACTION_COMMAND = "IMPORT_ACTION_COMMAND";
    private static final String EXPORT_ACTION_COMMAND = "EXPORT_ACTION_COMMAND";
    private static final String OK_ACTION_COMMAND = "OK_ACTION_COMMAND";
    private static final String CANCEL_ACTION_COMMAND = "CANCEL_ACTION_COMMAND";

    private static final String BANDS  = "Bands";
    private static final String LINEAR = "Linear";	

    private Paint initPaint;

    private int paletteSize = 1024;
    private int gradientAngle = 0;
    private RectangleCorner gradientOrigin = RectangleCorner.UPPER_LEFT;
    private String interpolationMethod = LINEAR;
    private Color[] stopColors = new Color[] {Color.gray, Color.gray, Color.gray};
    private float[] stopPositions = new float[] {0, .5f,1};
    private ColorGradient gradient = new ColorGradientInterpolation(stopColors, stopPositions, paletteSize);

    private final JComboBox<String> comboInterpolation = new JComboBox<>(new String[] {LINEAR, BANDS}); 
    private final JComboBox<RectangleCorner> comboGradientOrigin = new JComboBox<>(RectangleCorner.values());

    private final JSpinner spinnerPaletteSize = new JSpinner(new SpinnerNumberModel(paletteSize, 2, Integer.MAX_VALUE, 1));
    private final JSpinner spinnerGradientAngle  = new JSpinner(new SpinnerNumberModel(gradientAngle, 0, 90, 1));

    private final JLabel labelPaletteSize = new JLabel("Palette size");

    private final JButton buttonInvert = new JButton("Invert");
    private final JButton buttonImport = new JButton("Import");
    private final JButton buttonExport = new JButton("Export");
    private final JButton buttonBuiltIn = new JButton(new SelectBuiltInGradientAction());

    private final JButton buttonOK = new JButton("OK");
    private final JButton buttonCancel = new JButton("Cancel");

    private final GradientEditionPanel colorPanel;
    private JFileChooser fileChooser;
    private GradientSelectionDialog builtInsDialog;

    private PaintReceiver paintReceiver;

    public SkewedGradientEditionDialog(Window parent)
    {
        super(parent, NAME, ModalityType.APPLICATION_MODAL);

        colorPanel = new GradientEditionPanel(gradient, this, this);
        colorPanel.addPropertyChangeListener(this);
        colorPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        spinnerPaletteSize.addChangeListener(this);
        JPanel mainPanel = buildMainPanel();
        JPanel panelButtons = buildButtonPanel();	

        Container content = getContentPane();
        GroupLayout layout = new GroupLayout(content);
        content.setLayout(layout);
        layout.setAutoCreateContainerGaps(false);

        layout.setHorizontalGroup
        (
                layout.createParallelGroup()
                .addComponent(panelButtons, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(mainPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(mainPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(panelButtons, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
                );

        initActionListener();
        initChangeListener();
        initItemListener();

        setEditorConsistentWithInterpolationMethod();

        pack();		
        setLocationRelativeTo(parent);
    }

    private void initActionListener()
    {
        buttonInvert.setActionCommand(INVERT_ACTION_COMMAND);
        buttonImport.setActionCommand(IMPORT_ACTION_COMMAND);
        buttonExport.setActionCommand(EXPORT_ACTION_COMMAND);
        buttonOK.setActionCommand(OK_ACTION_COMMAND);
        buttonCancel.setActionCommand(CANCEL_ACTION_COMMAND);

        buttonInvert.addActionListener(this);
        buttonImport.addActionListener(this);
        buttonExport.addActionListener(this);
        buttonOK.addActionListener(this);
        buttonCancel.addActionListener(this);
    }

    private void initChangeListener()
    {
        spinnerPaletteSize.addChangeListener(this);
        spinnerGradientAngle.addChangeListener(this);
    }

    private void initItemListener()
    {
        comboInterpolation.addItemListener(this);
        comboGradientOrigin.addItemListener(this);
    }

    private void setEditorConsistentWithInterpolationMethod()
    {
        boolean isBands = BANDS.equals(interpolationMethod);

        labelPaletteSize.setEnabled(!isBands);
        spinnerPaletteSize.setEnabled(!isBands);		
    }

    private void updateColorGradient()
    {
        this.gradient = (interpolationMethod == BANDS) ? new ColorGradientBands(stopColors, stopPositions): new ColorGradientInterpolation(stopColors, stopPositions, paletteSize);
    }

    @Override
    public ColorGradient getColorGradient()
    {	
        return gradient;
    }

    @Override
    public void setColorGradient(ColorGradient gradient)	
    {		
        if(gradient != null  && !this.gradient.equals(gradient))
        {
            this.gradient = gradient;
            boolean isPaletteResizable = gradient.isPaletteResizable();
            if(isPaletteResizable)
            {
                paletteSize = gradient.getPaletteSize();	
                spinnerPaletteSize.setValue(paletteSize);
            }	

            interpolationMethod = gradient instanceof ColorGradientInterpolation ? LINEAR: BANDS;						
            comboInterpolation.setSelectedItem(interpolationMethod);

            colorPanel.setColorGradient(gradient);
            updateReceiver();
        }

        setEditorConsistentWithInterpolationMethod();		
        updateSize();
    }



    public void showDialog(PaintReceiver paintReceiver)
    {	
        this.paintReceiver = paintReceiver;
        this.initPaint = paintReceiver.getPaint();

        if(initPaint instanceof GradientPaint)
        {
            GradientPaint gradientPaint = (GradientPaint)initPaint;
            ColorGradient tabel = gradientPaint.getGradient();
            setColorGradient(tabel);

            if(initPaint instanceof SkewedGradientPaint)
            {
                SkewedGradientPaint skewedGradientPaint = (SkewedGradientPaint)initPaint;
                this.gradientAngle = (int) ((180./Math.PI)*skewedGradientPaint.getAngle());
                this.gradientOrigin = skewedGradientPaint.getGradientOrigin();

                spinnerGradientAngle.setValue(gradientAngle);
                comboGradientOrigin.setSelectedItem(gradientOrigin);
            }
        }		
        setVisible(true);
    }

    private void updateReceiver()
    {
        if(gradient != null)
        {
            SkewedGradientPaint paint = new SkewedGradientPaint(gradient, gradientOrigin, (1./180.)*Math.PI*gradientAngle);
            paintReceiver.setPaint(paint);
        }
    }

    private void updateEditionPanel()
    {
        colorPanel.setColorGradient(gradient);
    }

    @Override
    public void actionPerformed(ActionEvent evt) 
    {
        String command = evt.getActionCommand();

        if(OK_ACTION_COMMAND.equals(command))
        {
            doOk();
        }
        else if(CANCEL_ACTION_COMMAND.equals(command))
        {
            doCancel();
        }
        else if(INVERT_ACTION_COMMAND.equals(command))
        {
            doInvert();
        }
        else if(IMPORT_ACTION_COMMAND.equals(command))
        {
            doExport();
        }
        else if(EXPORT_ACTION_COMMAND.equals(command))
        {
            doImport();
        }
    }

    private void doInvert()
    {
        colorPanel.invert();
    }

    private void doExport()
    {
        if(fileChooser == null)
        {
            fileChooser = new ExtensionFileChooser(pref,"Color gradient (.gradient)","gradient", true);
        }
        fileChooser.setApproveButtonMnemonic(KeyEvent.VK_O);
        int op = fileChooser.showOpenDialog(SkewedGradientEditionDialog.this);
        if(op == JFileChooser.APPROVE_OPTION)
        {
            File f = fileChooser.getSelectedFile();
            importLUTTable(f);				
        }
    }

    private void doImport()
    {
        if(fileChooser == null)
        {
            fileChooser = new ExtensionFileChooser(pref,"Color gradient (.gradient)","gradient", true);
        }
        fileChooser.setApproveButtonMnemonic(KeyEvent.VK_I);
        int op = fileChooser.showSaveDialog(SkewedGradientEditionDialog.this);
        if(op == JFileChooser.APPROVE_OPTION)
        {
            File f = fileChooser.getSelectedFile();
            exportLUTTable(f);				
        }
    }

    private void doSelectFromBuiltIn()
    {
        if(builtInsDialog == null)
        {
            builtInsDialog = new GradientSelectionDialog(this);
        }
        builtInsDialog.showDialog(this);
    }

    private void exportLUTTable(File f)
    {
        try 
        {
            FileOutputStream fout = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(getColorGradient());
            oos.close();
        }
        catch (Exception e) 
        { 
            e.printStackTrace(); 
        }
    }

    private void importLUTTable(File f)
    {
        try 
        {
            FileInputStream fin = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fin);
            ColorGradient table = (ColorGradient) ois.readObject();
            setColorGradient(table);
            ois.close();
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void doOk()
    {
        setVisible(false);
    }

    private void doCancel()
    {
        paintReceiver.setPaint(initPaint);
        setVisible(false);
    }

    @Override
    public void stateChanged(ChangeEvent evt)
    {
        Object source = evt.getSource();

        if(source == spinnerPaletteSize)
        {
            int paletteSizeNew = ((SpinnerNumberModel)spinnerPaletteSize.getModel()).getNumber().intValue();
            if(this.paletteSize != paletteSizeNew)
            {
                this.paletteSize = paletteSizeNew;
                updateColorGradient();
                updateReceiver();	
                updateEditionPanel();
            }			
        }	
        else if(source == spinnerGradientAngle)
        {
            int gradientAngleNew = ((SpinnerNumberModel)spinnerGradientAngle.getModel()).getNumber().intValue();
            if(this.gradientAngle != gradientAngleNew)
            {
                this.gradientAngle = gradientAngleNew;
            }
            updateReceiver();
        }
    }	

    @Override
    public void itemStateChanged(ItemEvent evt) 
    {
        Object source = evt.getSource();

        if(source == comboInterpolation)
        {		
            String interpolationMethodNew = (String)comboInterpolation.getSelectedItem();
            if(!this.interpolationMethod.equals(interpolationMethodNew))
            {
                this.interpolationMethod = interpolationMethodNew;
                setEditorConsistentWithInterpolationMethod();
                updateColorGradient();
                updateEditionPanel();
                updateReceiver();
            }			
        }
        else if(source == comboGradientOrigin)
        {
            RectangleCorner gradientOriginNew = (RectangleCorner)comboGradientOrigin.getSelectedItem();
            if(!this.gradientOrigin.equals(gradientOriginNew))
            {
                this.gradientOrigin = gradientOriginNew;
                updateReceiver();
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if(GradientEditionPanel.COLOR_PANEL_PREFERRED_SIZE.equals(name))
        {
            updateSize();
        }
    }

    private void updateSize()
    {
        Dimension preferredSize = getPreferredSize();
        Dimension actualSize = getSize();

        int newWidth = (int)Math.max(preferredSize.getWidth(), actualSize.getWidth());
        int newHeight = (int)Math.max(preferredSize.getHeight(), actualSize.getHeight());

        Dimension newSize = new Dimension(newWidth, newHeight);
        setSize(newSize);
    }

    @Override
    public void updateGradientParameters(Color[] stopColors, float[] stopPositions)
    {
        if(!(Arrays.equals(this.stopPositions, stopPositions) && Arrays.equals(this.stopColors, stopColors)))
        {
            this.stopColors = stopColors;
            this.stopPositions = stopPositions;
            updateColorGradient();
            updateEditionPanel();
            updateReceiver();
        }		
    }

    private class SelectBuiltInGradientAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public SelectBuiltInGradientAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(NAME, "Built in");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            doSelectFromBuiltIn();
        }
    }

    private JPanel buildMainPanel()
    {	
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));

        JLabel labelMixing = new JLabel("Mixing");
        JLabel labelColors = new JLabel("<html>Gradient<br>colors<html>");
        JLabel labelGradientAngle = new JLabel("Gradient angle");
        JLabel labelGradientOrigin = new JLabel("Gradient origin");

        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup
        (
                layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(labelPaletteSize, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelMixing, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelColors, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelGradientAngle, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelGradientOrigin, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)

                .addGroup(layout.createParallelGroup()			
                        .addComponent(colorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                        .addComponent(comboInterpolation, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                        .addComponent(spinnerPaletteSize, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                        .addComponent(comboGradientOrigin, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                        .addComponent(spinnerGradientAngle, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)	
                .addGroup(layout.createParallelGroup()
                        .addComponent(buttonInvert, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(buttonImport, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(buttonExport, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(buttonBuiltIn, GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))


                );

        layout.setVerticalGroup(

                layout.createParallelGroup().addGroup(
                        layout.createSequentialGroup()					    					

                        .addGroup(
                                layout.createParallelGroup()
                                .addComponent(labelColors, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                                .addComponent(colorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)				
                                .addGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,
                                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(buttonInvert,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(buttonImport,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(buttonExport,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(buttonBuiltIn,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                        .addGroup(
                                layout.createParallelGroup()
                                .addComponent(labelMixing, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(comboInterpolation, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                        .addGroup(
                                layout.createParallelGroup()
                                .addComponent(labelPaletteSize, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(spinnerPaletteSize, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED,GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                        .addGroup(
                                layout.createParallelGroup()
                                .addComponent(labelGradientOrigin, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(comboGradientOrigin, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                        .addGroup(
                                layout.createParallelGroup()
                                .addComponent(labelGradientAngle, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(spinnerGradientAngle, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

                        )
                );

        layout.linkSize(buttonInvert, buttonImport, buttonExport, buttonBuiltIn);

        return mainPanel;
    }


    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        buttonOK.setMnemonic(KeyEvent.VK_O);
        buttonCancel.setMnemonic(KeyEvent.VK_C);

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonOK).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonOK)
                .addComponent(buttonCancel));

        layout.linkSize(buttonOK,  buttonCancel);

        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
    }
}
