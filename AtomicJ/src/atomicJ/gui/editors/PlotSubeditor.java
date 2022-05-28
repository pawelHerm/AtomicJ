
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
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;


import org.jfree.chart.plot.*;
import org.jfree.ui.*;

import atomicJ.gui.AtomicJ;
import atomicJ.gui.BasicStrokeReceiver;
import atomicJ.gui.GradientPaint;
import atomicJ.gui.PaintReceiver;
import atomicJ.gui.PreferredBasePlotStyle;
import atomicJ.gui.RectangleCorner;
import atomicJ.gui.SkewedGradientEditionDialog;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;
import atomicJ.utilities.Validation;

import static atomicJ.gui.PreferenceKeys.*;

public class PlotSubeditor<E extends Plot> implements ActionListener, Subeditor, PaintReceiver 
{
    private static final String EDIT_OUTLINE_STROKE_COMMAND = "EDIT_OUTLINE_STROKE_COMMAND";
    private static final String SELECT_BACKGROUND_PAINT_COMMAND = "SELECT_BACKGROUND_PAINT_COMMAND";

    private final Preferences pref;

    private final Paint initBackgroundPaint;
    private final boolean initUseGradientForBackground;
    private final Paint initOutlinePaint;

    private final Stroke initOutlineStroke;

    private final boolean initOutlineVisible;

    private Paint backgroundPaint;
    private boolean useGradientForBackground;
    private RectangleCorner gradientOrigin;
    private double gradientAngle;

    private Paint outlinePaint;
    private Stroke outlineStroke;
    private boolean outlineVisible;

    private final StraightStrokeSample outlineStrokeSample;

    private final JButton buttonSelectBackgroundPaint = new JButton("Select");
    private final JButton buttonEditOutline = new JButton("Edit");

    private final JCheckBox boxDrawOutline = new JCheckBox();

    private final JCheckBox boxUseGradientPaint = new JCheckBox();
    private final PaintSample backgroundPaintSample;

    private SkewedGradientEditionDialog gradientDialog;
    private StrokeChooser outlineStrokeChooser;

    private final SubPanel mainPanel;
    private final JPanel outerPanel = new JPanel(new BorderLayout());

    private final List<? extends E> boundedPlots;
    private final E plot;

    public PlotSubeditor(E plot, List<? extends E> boundedPlots, Preferences pref) 
    {    	
        //sets initial parameters
        this.plot = Validation.requireNonNullParameterName(plot, "plot");
        this.boundedPlots = Validation.requireNonNullParameterName(boundedPlots, "boundedPlots");
        this.pref = Validation.requireNonNullParameterName(pref, "pref");

        this.initBackgroundPaint = plot.getBackgroundPaint();
        this.initOutlinePaint = plot.getOutlinePaint();
        this.initUseGradientForBackground = initBackgroundPaint instanceof GradientPaint;

        this.initOutlineStroke = plot.getOutlineStroke();
        this.initOutlineVisible = plot.isOutlineVisible() && initOutlineStroke != null;

        setParametersToInitial();

        //builds components and set editor 
        this.backgroundPaintSample = new PaintSample(initBackgroundPaint);
        this.outlineStrokeSample = new StraightStrokeSample(initOutlineStroke);
        outlineStrokeSample.setStrokePaint(initOutlinePaint);

        boxUseGradientPaint.setSelected(initUseGradientForBackground);
        boxDrawOutline.setSelected(initOutlineVisible);

        mainPanel = buildMainPanel();       
        outerPanel.add(mainPanel, BorderLayout.NORTH);

        JPanel buttonPanel = buildButtonPanel();
        outerPanel.add(buttonPanel, BorderLayout.SOUTH);

        initActionListener();
        initItemListeners();
    }

    protected E getPlot()
    {
        return plot;
    }

    protected List<? extends E> getPlots()
    {
        return boundedPlots;
    }

    protected Preferences getPreferences()
    {
        return pref;
    }

    protected SubPanel getEditorPanel()
    {
        return mainPanel;
    }

    private void setParametersToInitial()
    {
        this.backgroundPaint = initBackgroundPaint;
        this.outlinePaint = initOutlinePaint;
        this.useGradientForBackground = initUseGradientForBackground;

        this.outlineStroke = initOutlineStroke;

        this.outlineVisible = initOutlineVisible;
    }

    private void initActionListener()
    {
        buttonSelectBackgroundPaint.setActionCommand(SELECT_BACKGROUND_PAINT_COMMAND);
        buttonEditOutline.setActionCommand(EDIT_OUTLINE_STROKE_COMMAND);

        buttonSelectBackgroundPaint.addActionListener(this);
        buttonEditOutline.addActionListener(this);
    }

    private void initItemListeners()
    {
        boxUseGradientPaint.addItemListener(new ItemListener()
        {           
            @Override
            public void itemStateChanged(ItemEvent e) 
            {
                boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
                useGradientForBackground = selected;
            }
        });
        boxDrawOutline.addItemListener(new ItemListener() 
        {         
            @Override
            public void itemStateChanged(ItemEvent e) {               
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                outlineVisible = selected;
                plot.setOutlineVisible(selected);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();
        if (command.equals(SELECT_BACKGROUND_PAINT_COMMAND)) 
        {
            attemptBackgroundPaintSelection();
        }
        else if (command.equals(EDIT_OUTLINE_STROKE_COMMAND)) 
        {
            attemptOutlineStrokeSelection();
        }
    }

    private void hideRoot()
    {
        Component root = SwingUtilities.getRoot(outerPanel);
        root.setVisible(false);
    }

    private void attemptBackgroundPaintSelection() 
    {
        if(useGradientForBackground)
        {
            if(gradientDialog == null)
            {
                gradientDialog = new SkewedGradientEditionDialog(SwingUtilities.getWindowAncestor(outerPanel));
            }
            gradientDialog.showDialog(this);			
        }
        else
        {
            Paint backgroundPaintNew = JColorChooser.showDialog(PlotSubeditor.this.outerPanel, "Plot background color", Color.blue);	        
            if (backgroundPaintNew != null) 
            {
                backgroundPaint = backgroundPaintNew;
                backgroundPaintSample.setPaint(backgroundPaintNew);
                plot.setBackgroundPaint(backgroundPaintNew);			
            }
        }    	
    }

    private void attemptOutlineStrokeSelection() 
    {
        if(outlineStrokeChooser == null)
        {
            outlineStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(outerPanel), new BasicStrokeReceiver()
            {
                @Override
                public BasicStroke getStroke() 
                {
                    return (BasicStroke)outlineStroke;
                }

                @Override
                public void setStroke(BasicStroke stroke) 
                {
                    outlineStroke = stroke;
                    outlineStrokeSample.setStroke(stroke);
                    plot.setOutlineStroke(stroke);
                }

                @Override
                public Paint getStrokePaint() 
                {
                    return outlinePaint;
                }

                @Override
                public void setStrokePaint(Paint paint) 
                {
                    outlinePaint = paint;
                    outlineStrokeSample.setStrokePaint(paint);
                    plot.setOutlinePaint(paint);
                }       	
            }
                    );
        }
        outlineStrokeChooser.showDialog();
    }

    protected void resetEditor()
    {
        backgroundPaintSample.setPaint(backgroundPaint);
        outlineStrokeSample.setStroke(outlineStroke);
        outlineStrokeSample.setStrokePaint(outlinePaint);
        boxDrawOutline.setSelected(outlineVisible);
        boxUseGradientPaint.setSelected(useGradientForBackground);
    }

    protected void resetPlot(E p)
    {    	
        //only the last method call should invoke notification mechanism

        p.setBackgroundPaint(backgroundPaint);

        p.setOutlinePaint(outlinePaint);	    
        p.setOutlineStroke(outlineStroke);
        p.setOutlineVisible(outlineVisible);    	
    }

    @Override
    public void resetToDefaults() 
    {
        this.backgroundPaint = (Paint)SerializationUtilities.getSerializableObject(pref, BACKGROUND_PAINT, Color.white);
        this.outlinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, PreferredBasePlotStyle.PLOT_OUTLINE_PAINT, Color.black);
        this.outlineStroke = SerializationUtilities.getStroke(pref, PreferredBasePlotStyle.PLOT_OUTLINE_STROKE, Plot.DEFAULT_OUTLINE_STROKE);
        this.outlineVisible = pref.getBoolean(PreferredBasePlotStyle.PLOT_OUTLINE_VISIBLE, true);
        this.useGradientForBackground = backgroundPaint instanceof GradientPaint;

        resetEditor();
        resetPlot(plot);	   
    }

    @Override
    public void saveAsDefaults() 
    {	    
        pref.putBoolean(PreferredBasePlotStyle.PLOT_OUTLINE_VISIBLE, outlineVisible);

        try 
        {
            SerializationUtilities.putSerializableObject(pref, BACKGROUND_PAINT, backgroundPaint);
            SerializationUtilities.putSerializableObject(pref, PreferredBasePlotStyle.PLOT_OUTLINE_PAINT, outlinePaint);
            SerializationUtilities.putStroke(pref, PreferredBasePlotStyle.PLOT_OUTLINE_STROKE, outlineStroke);
        }
        catch (ClassNotFoundException | IOException | BackingStoreException e) 
        {
            e.printStackTrace();
        }	

        try 
        {
            pref.flush();
        } 
        catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    @Override
    public void applyChangesToAll() 
    {
        for(E p: boundedPlots)
        {
            resetPlot(p);
        }
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();
        resetPlot(plot);
        resetEditor();
    }

    @Override
    public Component getEditionComponent()
    {
        return outerPanel;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return boundedPlots.size() > 1;
    }

    @Override
    public Paint getPaint() 
    {
        return backgroundPaint;
    }

    @Override
    public void setPaint(Paint paint) 
    {
        if(paint != null)
        {			
            backgroundPaint = paint;
            backgroundPaintSample.setPaint(backgroundPaint);
            plot.setBackgroundPaint(backgroundPaint);			
        }		
    }

    private SubPanel buildMainPanel()
    {
        SubPanel mainPanel = new SubPanel();

        mainPanel.addComponent(new JLabel("Use gradient"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(boxUseGradientPaint, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        mainPanel.addComponent(new JLabel("Background color"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(backgroundPaintSample, 1, 1, 3, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        mainPanel.addComponent(buttonSelectBackgroundPaint, 4, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);		

        SubPanel outlinePanel = new SubPanel();

        outlinePanel.addComponent(boxDrawOutline, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, 0.075, 0);     
        outlinePanel.addComponent(new JLabel("Stroke"), 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        outlinePanel.addComponent(outlineStrokeSample, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);             

        mainPanel.addComponent(new JLabel("Outline"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(outlinePanel, 1, 2, 3, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);		
        mainPanel.addComponent(buttonEditOutline, 4, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);

        mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7),
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Plot style"),BorderFactory.createEmptyBorder(8, 6, 6, 8))));

        return mainPanel;
    }

    private JPanel buildButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        BatchApplyAction batchApplyAction = new BatchApplyAction();
        JButton buttonBatchApplyAll = new JButton(batchApplyAction);
        batchApplyAction.setEnabled(isApplyToAllEnabled());

        JButton buttonClose = new JButton(new CloseAction());
        JButton buttonSave = new JButton(new SaveAsDefaultsAction());
        JButton buttonReset = new JButton(new ResetToDefaultsAction());
        JButton buttonUndo = new JButton(new UndoAction());


        GroupLayout layout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup().addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonBatchApplyAll).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSave).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonReset).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonClose).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonUndo));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(buttonBatchApplyAll)
                .addComponent(buttonSave)
                .addComponent(buttonReset)
                .addComponent(buttonClose)
                .addComponent(buttonUndo));

        layout.linkSize(buttonClose, buttonBatchApplyAll, buttonSave, buttonReset, buttonUndo);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return buttonPanel;
    }

    @Override
    public String getSubeditorName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setNameBorder(boolean b) {
        // TODO Auto-generated method stub

    }

    private class BatchApplyAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public BatchApplyAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(NAME, "Batch apply");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            applyChangesToAll();
            JOptionPane.showMessageDialog(PlotSubeditor.this.outerPanel, "Plot style was applied to all charts of the same type", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class UndoAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public UndoAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(NAME, "Undo");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            undoChanges();
        }
    }

    private class SaveAsDefaultsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public SaveAsDefaultsAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(NAME, "Use as defaults");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            saveAsDefaults();
            JOptionPane.showMessageDialog(PlotSubeditor.this.outerPanel, "Default plot style was changed", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class ResetToDefaultsAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public ResetToDefaultsAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(NAME, "Reset to defaults");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            resetToDefaults();
        }
    }

    private class CloseAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public CloseAction() 
        {
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
            putValue(NAME, "Close");
        }

        @Override
        public void actionPerformed(ActionEvent event) 
        {
            hideRoot();
        }
    }
}
