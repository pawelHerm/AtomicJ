
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

package atomicJ.gui.statistics;

import static atomicJ.gui.PreferenceKeys.WINDOW_HEIGHT;
import static atomicJ.gui.PreferenceKeys.WINDOW_WIDTH;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.LayoutStyle;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import atomicJ.analysis.Batch;
import atomicJ.analysis.Processed1DPack;
import atomicJ.utilities.GUIUtilities;



public class ResultsChooser<E extends Processed1DPack<E,?>>
{
    private static final int DEFAULT_HEIGHT = Math.round(Toolkit.getDefaultToolkit().getScreenSize().height/3);
    private static final int DEFAULT_WIDTH = Math.round(Toolkit.getDefaultToolkit().getScreenSize().width/4);

    private final Preferences pref = Preferences.userRoot().node(getClass().getName());

    private final Action okAction = new OKAction();
    private final Action cancelAction = new CancelAction();
    private final JTree tree;

    private List<E> selectedPacks;

    private boolean selectionApproved;

    private final JDialog viewDialog;

    public ResultsChooser(List<Batch<E>> batches, Window parent)
    {
        viewDialog = new JDialog(parent, "Choose sample", ModalityType.APPLICATION_MODAL);

        tree = buildTree(batches);
        JPanel buttonPanel = buildButtonPanel();

        JScrollPane scrollPane = new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS); 
        scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),scrollPane.getBorder()));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        viewDialog.add(mainPanel,BorderLayout.CENTER);

        viewDialog.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent evt)
            {               
                pref.putInt(WINDOW_HEIGHT, viewDialog.getHeight());
                pref.putInt(WINDOW_WIDTH, viewDialog.getWidth());
            }
        });

        int height =  pref.getInt(WINDOW_HEIGHT,DEFAULT_HEIGHT);
        int width =  pref.getInt(WINDOW_WIDTH,DEFAULT_WIDTH);
        if(GUIUtilities.isWindowSizeWellSpecified(width, height))
        {
            viewDialog.setSize(width,height);
        }
        else
        {
            viewDialog.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }

        viewDialog.setLocationRelativeTo(parent);
    }

    public boolean showDialog()
    {
        selectionApproved = false;
        viewDialog.setVisible(true);
        return selectionApproved;
    }

    public List<E> getSelectedPacks()
    {
        return selectedPacks;
    }

    private void updateSelectedPacks()
    {
        selectedPacks = new ArrayList<>();

        TreePath[] selectedPaths = tree.getSelectionPaths();

        for(TreePath path: selectedPaths)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if(node.isLeaf())
            {
                E pack = (E)node.getUserObject();
                selectedPacks.add(pack);
            }
            else
            {
                Batch<E> batch = (Batch<E>)node.getUserObject();
                List<E> packs = batch.getPacks();
                selectedPacks.addAll(packs);
            }
        }
    }

    private JTree buildTree(List<Batch<E>> batches)
    {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Results");

        for(Batch<E> batch: batches)
        {
            List<E> leaves = batch.getPacks();

            DefaultMutableTreeNode node = new DefaultMutableTreeNode(batch);
            rootNode.add(node);

            for(E leaf: leaves)
            {
                DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(leaf);
                node.add(leafNode);
            }
        }

        JTree tree = new JTree(rootNode);
        return tree;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        JButton buttonOK = new JButton(okAction);
        JButton buttonCancel = new JButton(cancelAction);

        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonOK)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonCancel));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonOK)
                .addComponent(buttonCancel));

        layout.linkSize(buttonOK, buttonCancel);

        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        return buttonPanel;
    }


    private class CancelAction extends AbstractAction
    {
        private static final long serialVersionUID = -7329880597164066698L;

        public CancelAction()
        {
            putValue(NAME, "Cancel");
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            selectionApproved = false;
            viewDialog.setVisible(false);            
        }

    }

    private class OKAction extends AbstractAction
    {
        private static final long serialVersionUID = 5592616545160896957L;

        public OKAction()
        {
            putValue(NAME, "OK");
            putValue(MNEMONIC_KEY,KeyEvent.VK_O);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            updateSelectedPacks();
            selectionApproved = true;
            viewDialog.setVisible(false);            
        }
    }
}
