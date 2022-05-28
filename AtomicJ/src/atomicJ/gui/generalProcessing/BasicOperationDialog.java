
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

package atomicJ.gui.generalProcessing;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;

import atomicJ.gui.imageProcessingActions.OperationListener;


public class BasicOperationDialog <E extends BasicOperationModel> extends JDialog implements PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    private final OKAction okAction = new OKAction();
    private final ResetAction resetAction = new ResetAction();
    private final CancelAction cancelAction = new CancelAction();

    private final JButton buttonOK = new JButton(okAction);
    private final JButton buttonReset = new JButton(resetAction);
    private final JButton buttonCancel = new JButton(cancelAction);

    private E model;
    private final CustomProcessingListener processingListener = new CustomProcessingListener();

    public BasicOperationDialog(Window parent, String title, boolean temporary)
    {
        this(parent, title, temporary, ModalityType.APPLICATION_MODAL);
    }

    public BasicOperationDialog(Window parent, String title, boolean temporary, ModalityType modalityType)
    {
        super(parent, title, modalityType);
        setLayout(new BorderLayout());

        if(temporary)
        {
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }

        initWindowListener();
    }

    private void initWindowListener()
    {
        addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {                
                E model = getModel();
                if(model != null)
                {
                    model.operationFinished();
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        String property = evt.getPropertyName();

        if(BasicOperationModel.APPLY_ENABLED.equals(property))
        {
            boolean applyEnabled = (boolean)evt.getNewValue();                       
            okAction.setEnabled(applyEnabled);
        }
    }


    public void showDialog(E model)
    {
        setModel(model);        
        setVisible(true);		
    }

    protected E getModel()
    {
        return model;
    }

    protected void setModel(E modelNew)
    {
        clearOldModel();

        modelNew.addPropertyChangeListener(this);
        modelNew.addOperationListener(processingListener);

        this.model = modelNew;
        this.model.checkIfApplyEnabled();

        pullModelParameters();
        resetEditor();
    }

    protected void clearOldModel()
    {
        if(this.model != null)
        {
            this.model.removePropertyChangeListener(this);
            this.model.removeOperationListener(processingListener);
        }      
    }

    protected void pullModelParameters()
    {
    }

    protected void resetEditor()
    {        
        if(model == null)
        {
            return;
        }

        boolean applyEnabled = model.isApplyEnabled();
        okAction.setEnabled(applyEnabled);
    }

    protected void setModelToInitialState()
    {}

    protected void ok()
    {   
        model.ok();
        setVisible(false);        
    }

    protected void reset()
    {               
        setModelToInitialState();
        model.reset();
    }

    protected void cancel()
    {
        setModelToInitialState();
        model.cancel();

        setVisible(false);
    }

    //I made this method final, because it is meant to be called from the constructors of the descendant class;
    //methods called in the constructor should not be overridden, because this may cause problems when
    //non-static fields are used in the method body
    protected final JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

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
            putValue(MNEMONIC_KEY,KeyEvent.VK_O);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"OK");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            ok();
        }
    }

    private class ResetAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public ResetAction()
        {			
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
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
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
            putValue(NAME,"Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            cancel();
        }
    }

    private class CustomProcessingListener implements OperationListener
    {
        @Override
        public void finished()
        {
            setVisible(false);
        }

        @Override
        public void applied() {            
        }       
    }
}
