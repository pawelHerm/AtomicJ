
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.lowagie.text.Font;



public class GradientSelectionDialog implements ActionListener
{
    private final AddNewGradientAction addNewGradientAction = new AddNewGradientAction();
    private final ClearUserAction clearUserGradientsAction = new ClearUserAction();


    private List<ColorGradient> initGradients;


    private final JPanel mainPanel;

    private final Map<String, ColorGradient> gradients;
    private final Map<String, ColorGradient> userGradients = new LinkedHashMap<>();
    private final Map<String, JMenuItem> userButtons = new LinkedHashMap<>();

    private List<ColorGradientReceiver> gradientReceivers;
    private GradientEditionDialog editionDialog;

    private final JDialog viewDialog;

    private boolean containsUserSpecifiedGradients = false;

    public GradientSelectionDialog(Window parent)
    {
        this(parent, true);
    }

    public GradientSelectionDialog(Window parent, boolean addMenuBar)
    {
        viewDialog = new JDialog(parent, "Select gradient", ModalityType.MODELESS);	

        this.gradients = GradientColorsBuiltIn.getGradients();

        mainPanel = buildMainPane();

        JScrollPane scrollPane = new JScrollPane(mainPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
        scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),scrollPane.getBorder()));

        JPanel buttonPanel = buildButtonPanel();

        viewDialog.setLayout(new BorderLayout());
        viewDialog.add(scrollPane, BorderLayout.CENTER);
        viewDialog.add(buttonPanel, BorderLayout.SOUTH);

        if(addMenuBar)
        {
            JMenuBar menuBar = new JMenuBar();
            menuBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredSoftBevelBorder(), BorderFactory.createEmptyBorder(3,3,3,3)));

            viewDialog.setJMenuBar(menuBar);

            JMenu menuGradients = new JMenu("Custom");
            menuGradients.setMnemonic(KeyEvent.VK_U);

            JMenuItem addNewGradientItem = new JMenuItem(addNewGradientAction);
            JMenuItem clearUserGradientsItem = new JMenuItem(clearUserGradientsAction);

            menuGradients.add(addNewGradientItem);
            menuGradients.addSeparator();
            menuGradients.add(clearUserGradientsItem);

            menuBar.add(menuGradients);
        }	

        setConsistenWithUserGradientCount();

        viewDialog.pack();
        viewDialog.setSize(viewDialog.getWidth(), parent.getHeight());			
        viewDialog.setLocationRelativeTo(parent);
    }


    public boolean isVisible() 
    {
        return viewDialog.isVisible();
    }

    public void setVisible(boolean visible) 
    {
        viewDialog.setVisible(visible);
    }

    public void dispose() 
    {
        viewDialog.dispose();
    }

    private void clearUserGradients()
    {
        int n = JOptionPane.showConfirmDialog(viewDialog, "Do you want to remove all user-specified gradients?",null, JOptionPane.YES_NO_OPTION);

        if(n == JOptionPane.OK_OPTION)
        {
            removeUserGradientsForMainPanel();
            removeUserGradientFiles();
            userGradients.clear();
            setConsistenWithUserGradientCount();
        }
    }

    private void addNewGradient()
    {
        if(editionDialog == null)
        {
            editionDialog = new GradientEditionDialog(viewDialog, ModalityType.APPLICATION_MODAL, true);
        }

        final ColorGradient initialGradient = new ColorGradientInterpolation(new Color[] {new Color(0, 0, 0), new Color(79, 25, 7), new Color(119, 49, 0), new Color(158, 81, 0), new Color(178, 115, 0), new Color(211, 157, 0), new Color(233, 186, 16), new Color(254, 215, 26), new Color(255, 249, 112)},
                new float[] {0.0f, 0.1576087f, 0.32427537f, 0.44746378f, 0.5416666f, 0.66284406f, 0.767094f, 0.88247865f, 1.0f}, 1024);

        editionDialog.showDialog(new ColorGradientReceiver() {

            @Override
            public void setColorGradient(ColorGradient gradient)
            {				
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) 
            {				
            }

            @Override
            public ColorGradient getColorGradient() 
            {
                return initialGradient;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) 
            {			
            }
        });

        boolean approved = editionDialog.isApproved();

        if(approved)
        {
            String name = editionDialog.getGradientName();

            ColorGradient gradient = editionDialog.getColorGradient();

            gradients.put(name, gradient);
            userGradients.put(name, gradient);

            exportGradientToDefaultLocation(name, gradient);
            addUserGradientToMainPanel(name, gradient);
            setConsistenWithUserGradientCount();
        }
    }

    private void removeUserGradientFiles()
    {		

        File directory = new File("Resources/UserGradients");

        if(directory.exists())
        {
            final FileNameExtensionFilter extFilter = new FileNameExtensionFilter("", "gradient");

            File[] files = directory.listFiles(new FileFilter() 
            {
                @Override
                public boolean accept(File file) 
                {
                    boolean accepted = (file.isFile() && extFilter.accept(file));
                    return accepted;
                }
            }
                    );

            for(File file : files)
            {
                file.delete();
            }
        }	
    }

    private Map<String,ColorGradient> readInUserGradients()
    {
        Map<String,ColorGradient> userGradients = new LinkedHashMap<>();

        File directory = new File("Resources/UserGradients");

        if(!directory.exists())
        {
            boolean success = directory.mkdirs();
            if(!success)
            {
                return userGradients;
            }
        }

        final FileNameExtensionFilter extFilter = new FileNameExtensionFilter("", "gradient");

        File[] files = directory.listFiles(new FileFilter() 
        {
            @Override
            public boolean accept(File file) 
            {
                boolean accepted = (file.isFile() && extFilter.accept(file));
                return accepted;
            }
        }
                );

        for(File file : files)
        {
            ColorGradient gradient = importGradient(file);
            if(gradient != null)
            {
                String fileName = file.getName();

                int n = fileName.lastIndexOf(".");

                String gradientName = fileName.substring(0, n);

                userGradients.put(gradientName, gradient);
            }
        }

        return userGradients;
    }

    private ColorGradient importGradient(File f)
    {
        ColorGradient gradient = null;
        try 
        {
            FileInputStream fin = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fin);
            gradient = (ColorGradient) ois.readObject();

            ois.close();
        }
        catch (Exception e) 
        { 
            e.printStackTrace(); 
        }
        return gradient;
    }

    private void exportGradientToDefaultLocation(String name, ColorGradient gradient)
    {
        File directory = new File("Resources/UserGradients");

        if(!directory.exists())
        {
            boolean success = directory.mkdirs();
            if(!success)
            {
                return;
            }
        }

        File file = new File(directory, name + ".gradient");

        exportGradient(file, gradient);
    }

    private void exportGradient(File f, ColorGradient gradient)
    {
        try 
        {
            FileOutputStream fout = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(gradient);
            oos.close();
        }
        catch (Exception e) 
        { 
            e.printStackTrace(); 
        }
    }

    public void showDialog(ColorGradientReceiver gradientReceiver)
    {
        setReceiver(gradientReceiver);
        viewDialog.setVisible(true);
    }

    public void showDialog(List<ColorGradientReceiver> gradientReceivers)
    {
        setReceivers(gradientReceivers);
        viewDialog.setVisible(true);
    }

    public void setReceiver(ColorGradientReceiver gradientReceiver)
    {
        this.gradientReceivers = Collections.singletonList(gradientReceiver);
        this.initGradients = Collections.singletonList(gradientReceiver.getColorGradient());
    }

    public void setReceivers(List<ColorGradientReceiver> gradientReceivers)
    {
        this.gradientReceivers = new ArrayList<>(gradientReceivers);
        this.initGradients = new ArrayList<>();

        for(ColorGradientReceiver receiver : this.gradientReceivers)
        {
            this.initGradients.add(receiver.getColorGradient());
        }
    }

    public void cleanUp()
    {
        this.gradientReceivers = null;
    }

    private void finish()
    {
        viewDialog.setVisible(false);
    }

    private void reset()
    {
        int n = gradientReceivers.size();
        for(int i = 0; i<n ; i++)
        {
            ColorGradientReceiver receiver = gradientReceivers.get(i);
            ColorGradient initColorGradient = initGradients.get(i);
            receiver.setColorGradient(initColorGradient);
        }
    }

    private void cancel()
    {
        reset();
        viewDialog.setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent evt) 
    {
        String command = evt.getActionCommand();

        ColorGradient gradient = gradients.get(command);

        if(gradient != null)
        {
            for(ColorGradientReceiver receiver : gradientReceivers)
            {
                receiver.setColorGradient(gradient);            
            }
        }
    }

    private static WritableRaster createRaster(int w, int h, ColorGradient table)
    {
        WritableRaster raster = ColorModel.getRGBdefault().createCompatibleWritableRaster(w, h);

        for (int j = 0; j < h; j++) 
        {
            for (int i = 0; i < w; i++) 
            {
                if(i == 0||j == 0 || i == w - 1 || j == h - 1)
                {
                    raster.setPixel(i, j, new int[] {0,0,0,255});
                }
                else
                {
                    double fraction = (double)i/w;

                    Color color = table.getColor(fraction);
                    raster.setPixel(i, j, new int[] {color.getRed(), color.getGreen(), color.getBlue(), 255});
                }         	   
            } 
        } 
        return raster;
    }

    private JPanel buildMainPane()
    {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        mainPanel.setLayout(new GridLayout(0, 1, 0, 1));

        for(Entry<String, ColorGradient> entry : gradients.entrySet())
        {
            String name = entry.getKey();
            ColorGradient gradient = entry.getValue();

            WritableRaster raster = createRaster(100,20,gradient);

            BufferedImage image = new BufferedImage(100, 20, BufferedImage.TYPE_INT_ARGB);
            image.setData(raster);
            Icon icon = new ImageIcon(image);

            JMenuItem button = new JMenuRollover(name, icon);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setIconTextGap(5);
            button.setActionCommand(name);
            button.addActionListener(this);

            button.setSelected(true);

            mainPanel.add(button);
        }

        Map<String, ColorGradient> readInUserGradients = readInUserGradients();
        gradients.putAll(readInUserGradients);
        userGradients.putAll(readInUserGradients());

        containsUserSpecifiedGradients = userGradients.size()>0;

        if(containsUserSpecifiedGradients)
        {
            //mainPanel.add(new JPopupMenu.Separator());

            JMenuItem itemCustom = new JMenuItem("Custom gradients");
            itemCustom.setFont(itemCustom.getFont().deriveFont(Font.BOLD));       

            mainPanel.add(itemCustom);

            //mainPanel.add(new JPopupMenu.Separator());

            for(Entry<String, ColorGradient> entry : userGradients.entrySet())
            {
                String name = entry.getKey();
                ColorGradient colorGradient = entry.getValue();

                WritableRaster raster = createRaster(100,20,colorGradient);

                BufferedImage image = new BufferedImage(100, 20, BufferedImage.TYPE_INT_ARGB);
                image.setData(raster);
                Icon icon = new ImageIcon(image);

                JMenuItem button = new JMenuRollover(name, icon);
                button.setHorizontalAlignment(SwingConstants.LEFT);
                button.setIconTextGap(5);
                button.setActionCommand(name);
                button.addActionListener(this);

                button.setSelected(true);

                userButtons.put(name, button);
                mainPanel.add(button);
            }
        }
        return mainPanel;		
    }

    private void removeUserGradientsForMainPanel()
    {
        for(JMenuItem item : userButtons.values())
        {
            mainPanel.remove(item);
            item.removeActionListener(this);
        }

        userButtons.clear();
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void setConsistenWithUserGradientCount()
    {
        boolean enabled = userGradients.size()>0;

        clearUserGradientsAction.setEnabled(enabled);
    }

    private void addUserGradientToMainPanel(String name, ColorGradient gradient)
    {
        if(!containsUserSpecifiedGradients)
        {
            containsUserSpecifiedGradients = true;
            mainPanel.add(new JPopupMenu.Separator());
        }
        WritableRaster raster = createRaster(100,20, gradient);

        BufferedImage image = new BufferedImage(100, 20, BufferedImage.TYPE_INT_ARGB);
        image.setData(raster);
        Icon icon = new ImageIcon(image);

        JMenuItem button = new JMenuRollover(name, icon);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(5);
        button.setActionCommand(name);
        button.addActionListener(this);

        button.setSelected(true);

        userButtons.put(name, button);
        mainPanel.add(button);

        viewDialog.revalidate();
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
            finish();
        }
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
        }
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
        }
    }

    private static class JMenuRollover extends JMenuItem implements MouseListener
    {
        private static final long serialVersionUID = 1L;

        public JMenuRollover(String name, Icon icon)
        {
            super(name, icon);
            addMouseListener(this);
        }

        @Override
        public void mouseClicked(MouseEvent e) 
        {			
        }

        @Override
        public void mouseEntered(MouseEvent e) 
        {
            setArmed(true);
        }

        @Override
        public void mouseExited(MouseEvent e) 
        {
            setArmed(false);
        }

        @Override
        public void mousePressed(MouseEvent e)
        {			
        }

        @Override
        public void mouseReleased(MouseEvent e) 
        {			
        }
    }


    private class AddNewGradientAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public AddNewGradientAction() 
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_N);
            putValue(NAME, "Add new gradient");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            addNewGradient();
        }
    }

    private class ClearUserAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ClearUserAction() 
        {
            putValue(NAME, "Clear custom gradients");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            clearUserGradients();
        }
    }
}
