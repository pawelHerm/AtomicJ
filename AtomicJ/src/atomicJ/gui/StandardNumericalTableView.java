
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Paweł Hermanowicz
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
import java.awt.Dialog.ModalityType;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.*;

import atomicJ.utilities.GUIUtilities;

public class StandardNumericalTableView
{
    private static final int DEFAULT_HEIGHT = Math.round(Toolkit.getDefaultToolkit().getScreenSize().height/2);
    private static final int DEFAULT_WIDTH = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/2);
    private static final int DEFAULT_LOCATION_X = Math.round(2*Toolkit.getDefaultToolkit().getScreenSize().width/3);

    private final Preferences pref = Preferences.userRoot().node(getClass().getName());

    private final FormatAction formatAction = new FormatAction();
    private final SaveAction saveAction = new SaveAction();
    private final PrintAction printAction = new PrintAction();
    private final TextFileChooser fileChooser = new TextFileChooser();
    private final NumericalFormatDialog customizeDialog;

    private final MinimalNumericalTable table;

    private final JScrollPane scrollPane;
    private final JMenu menuCustomize = new JMenu("Customize");

    private final JDialog viewDialog;
    private final DataViewSupport dataViewSupport = new DataViewSupport();

    public StandardNumericalTableView(Window parent, ModalityType modalityType, MinimalNumericalTable table, String title)
    {
        viewDialog = new JDialog(parent, title, modalityType);
        viewDialog.setLayout(new BorderLayout(1,5));

        this.table = table;
        this.customizeDialog = new NumericalFormatDialog(viewDialog, table, "Customize number format");

        JMenuBar menuBar = buildMenuBar();

        scrollPane = new JScrollPane(table); 
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),scrollPane.getBorder()));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(menuBar, BorderLayout.NORTH);
        viewDialog.add(mainPanel,BorderLayout.CENTER);

        JPanel buttonGroupResults = new JPanel(new GridLayout(1, 0, 5, 5));
        JPanel buttonGroupResultsContainer = new JPanel();

        JButton buttonClose = new JButton(new CloseAction());
        JButton buttonShowAll = new JButton(saveAction);
        JButton buttonPrint = new JButton(printAction);

        buttonGroupResults.add(buttonShowAll);
        buttonGroupResults.add(buttonPrint);
        buttonGroupResults.add(buttonClose);
        buttonGroupResultsContainer.add(buttonGroupResults);
        buttonGroupResultsContainer.setBorder(BorderFactory.createRaisedBevelBorder());
        viewDialog.add(buttonGroupResultsContainer,BorderLayout.SOUTH);

        viewDialog.pack();
        viewDialog.setLocationRelativeTo(parent);

        initViewListeners();
        initPropertyChangeListener();

        int height = pref.getInt(WINDOW_HEIGHT,DEFAULT_HEIGHT);
        int width = pref.getInt(WINDOW_WIDTH,DEFAULT_WIDTH);
        int locationX = pref.getInt(WINDOW_LOCATION_X, DEFAULT_LOCATION_X);
        int locationY = pref.getInt(WINDOW_LOCATION_Y, DEFAULT_HEIGHT);

        if(GUIUtilities.areWindowSizeAndLocationWellSpecified(width, height, locationX, locationY))
        {
            viewDialog.setSize(width,height);
            viewDialog.setLocation(locationX,locationY);
        }
        else
        {
            viewDialog.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            viewDialog.setLocation(DEFAULT_LOCATION_X, DEFAULT_HEIGHT);
        } 
    }

    private void initPropertyChangeListener()
    {
        table.addPropertyChangeListener(MinimalNumericalTable.TABLE_EMPTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                fireDataAvailabilityChanged(!(Boolean)evt.getNewValue());
            }
        });
    }

    private void initViewListeners()
    {
        //we use WindowListener to detect closing of the viewDialog
        //instead of ComponentListener. The componentHidden method of ComponentListener
        //is not called when a window is disposed on closing (i.e if we called viewDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //thus, using WindowListener is more reliable

        viewDialog.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {               
                pref.putInt(WINDOW_HEIGHT, viewDialog.getHeight());
                pref.putInt(WINDOW_WIDTH, viewDialog.getWidth());
                pref.putInt(WINDOW_LOCATION_X, (int) viewDialog.getLocation().getX());         
                pref.putInt(WINDOW_LOCATION_Y, (int) viewDialog.getLocation().getY());

                dataViewSupport.fireDataViewVisiblityChanged(false);
            }
        });

        //we use ComponentListener instead of WindowListener.windowOpened(), because that method is called only when the window is shown for the first
        //time
        viewDialog.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent evt)
            {
                dataViewSupport.fireDataViewVisiblityChanged(true);
            }
        });
    }

    public boolean isVisible()
    {
        return viewDialog.isVisible();
    }

    public void setVisible(boolean visible)
    {
        viewDialog.setVisible(visible);
    }

    public void addDataViewListener(DataViewListener listener)
    {
        dataViewSupport.addDataViewListener(listener);
    }

    public void removeDataViewListener(DataViewListener listener)
    {
        dataViewSupport.removeDataViewListener(listener);
    }

    protected void fireDataAvailabilityChanged(boolean dataAvailableNew)
    {
        dataViewSupport.fireDataAvailabilityChanged(dataAvailableNew);
    }

    public boolean isAnyDataAvailable()
    {
        boolean dataAvailable = !table.isEmpty();

        return dataAvailable;
    }

    public MinimalNumericalTable getTable()
    {
        return table;
    }

    protected JDialog getMainDialog()
    {
        return viewDialog;
    }

    protected int showSaveDialog()
    {
        int reponse = getFileChooser().showSaveDialog(viewDialog);
        return reponse;
    }

    protected void showErrorMessage(String message)
    {
        JOptionPane.showMessageDialog(viewDialog, "Error encountered while saving", "", JOptionPane.ERROR_MESSAGE);          
    }

    protected JScrollPane getScrollPane()
    {
        return scrollPane;
    }

    private JMenuBar buildMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic(KeyEvent.VK_F);

        JMenuItem itemSave = new JMenuItem(saveAction);
        JMenuItem itemPrint = new JMenuItem(printAction);
        JMenuItem itemClose = new JMenuItem(new CloseAction());

        menuFile.add(itemSave);
        menuFile.add(itemPrint);
        menuFile.addSeparator();
        menuFile.add(itemClose);

        menuCustomize.setMnemonic(KeyEvent.VK_U);
        JMenuItem itemCustomize = new JMenuItem(formatAction);

        menuCustomize.add(itemCustomize);

        menuBar.add(menuFile);
        menuBar.add(menuCustomize); 

        menuBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredSoftBevelBorder(), BorderFactory.createEmptyBorder(3,3,3,3)));

        return menuBar;
    }

    protected void addMenuItem(JMenuItem menuItem)
    {
        menuCustomize.add(menuItem);
    }

    protected void setHorizontalScrollBarPolicy(int policy)
    {
        scrollPane.setHorizontalScrollBarPolicy(policy);
    }

    protected void setVerticalScrollBarPolicy(int policy)
    {
        scrollPane.setVerticalScrollBarPolicy(policy);
    }

    public void saveTable()
    {
        if(!table.isEmpty())
        {
            File path = table.getDefaultOutputDirectory();

            JFileChooser chooser = getFileChooser();           
            chooser.setCurrentDirectory(path);
            int op = chooser.showSaveDialog(viewDialog.getParent());

            if(op != JFileChooser.APPROVE_OPTION)
            {
                return;
            }

            try 
            {                
                NumericalTableExporter exporter = new NumericalTableExporter(); 
                File selectedFile = getFileChooser().getSelectedFile();
                String selectedExt = fileChooser.getSelectedExtension();

                if(TextFileChooser.TSV_EXTENSION.equals(selectedExt))
                {                   
                    exporter.exportTableAsTSV(table, selectedFile, table.getDecimalFormat());
                }
                else
                {                  
                    exporter.exportTableAsCSV(table, selectedFile, table.getDecimalFormat());
                }
                table.setSaved(true);
            } 
            catch (IOException ex) 
            {
                ex.printStackTrace();
                showErrorMessage("Error encountered while saving");			
            }

        }		
    }


    protected JFileChooser getFileChooser() {
        return fileChooser;
    }


    private class SaveAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public SaveAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME, "Save");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {			
            saveTable();
        }
    }

    private class PrintAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public PrintAction()
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(NAME, "Print");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            try 
            {
                table.print();
            } 
            catch (PrinterException pe) 
            {
                JOptionPane.showMessageDialog(viewDialog, pe.getMessage(), "", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class CloseAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;
        public CloseAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(NAME, "Close");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            setVisible(false);
        }
    }

    private class FormatAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public FormatAction()
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
            putValue(NAME,"Format data");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            customizeDialog.setVisible(true);
        }
    }
}
