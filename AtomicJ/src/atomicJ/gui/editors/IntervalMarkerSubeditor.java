
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.jfree.ui.*;

import atomicJ.gui.AtomicJ;
import atomicJ.gui.BasicStrokeReceiver;
import atomicJ.gui.CustomizableIntervalMarker;
import atomicJ.gui.CustomizableXYBasePlot;
import atomicJ.gui.GradientPaint;
import atomicJ.gui.PaintReceiver;
import atomicJ.gui.SkewedGradientEditionDialog;
import atomicJ.gui.StraightStrokeSample;
import atomicJ.gui.StrokeChooser;
import atomicJ.gui.SubPanel;
import atomicJ.utilities.SerializationUtilities;
import static atomicJ.gui.PreferenceKeys.*;

public class IntervalMarkerSubeditor implements Subeditor, ActionListener, PaintReceiver 
{
    private static final String EDIT_OUTLINE_STROKE_COMMAND = "EDIT_OUTLINE_STROKE_COMMAND";
    private static final String SELECT__PAINT_COMMAND = "SELECT__PAINT_COMMAND";

    private final Preferences pref;

    private final boolean initBackground;
    private final float initAlpha;
    private final Paint initFillPaint;
    private final boolean initUseGradientForBackground;
    private final Paint initOutlinePaint;
    private final Stroke initOutlineStroke;

    private boolean background;
    private float alpha;
    private Paint fillPaint;
    private boolean useGradientForFill;
    private Paint outlinePaint;   
    private Stroke outlineStroke;

    private final JCheckBox boxBackground = new JCheckBox();
    private final JSpinner spinnerAlpha = new JSpinner(new SpinnerNumberModel(Float.valueOf(0.3f), Float.valueOf(0.f), Float.valueOf(1.f), Float.valueOf(0.1f)));

    private final StraightStrokeSample outlineStrokeSample;

    private final JButton buttonSelectBackgroundPaint = new JButton("Select");
    private final JButton buttonEditOutline = new JButton("Edit");

    private final JCheckBox boxUseGradientPaint = new JCheckBox();
    private final PaintSample backgroundPaintSample;

    private SkewedGradientEditionDialog gradientDialog;
    private StrokeChooser outlineStrokeChooser;

    private final CustomizableXYBasePlot plot;
    private final List<CustomizableIntervalMarker> boundedMarkers;
    private final CustomizableIntervalMarker marker;

    private final boolean domainMarker;

    private final JPanel editorPanel = new JPanel(new BorderLayout());

    public IntervalMarkerSubeditor(CustomizableIntervalMarker marker, List<CustomizableIntervalMarker> boundedMarkers, CustomizableXYBasePlot plot, boolean background, boolean domain) 
    {    	
        //sets initial parameters
        this.marker = marker;
        this.boundedMarkers = boundedMarkers;
        this.plot = plot;
        this.pref = marker.getPreferences();
        this.domainMarker = domain;

        this.initBackground = background;
        this.initAlpha = marker.getAlpha();
        this.initFillPaint = marker.getPaint();
        this.initOutlinePaint = marker.getOutlinePaint();
        this.initUseGradientForBackground = initFillPaint instanceof GradientPaint;

        this.initOutlineStroke = marker.getOutlineStroke();

        setParametersToInitial();

        //builds components and set editor 
        this.backgroundPaintSample = new PaintSample(initFillPaint);
        this.outlineStrokeSample = new StraightStrokeSample(initOutlineStroke);
        outlineStrokeSample.setStrokePaint(initOutlinePaint);

        boxBackground.setSelected(initBackground);
        boxUseGradientPaint.setSelected(initUseGradientForBackground);    

        JPanel mainPanel = buildMainPanel();       
        editorPanel.add(mainPanel, BorderLayout.NORTH);

        JPanel buttonPanel = buildButtonPanel();
        editorPanel.add(buttonPanel, BorderLayout.SOUTH);

        initActionListener();
        initChangeListener();
        initItemListener();
    }

    private void setParametersToInitial()
    {
        this.background = initBackground;
        this.alpha = initAlpha;
        this.fillPaint = initFillPaint;
        this.outlinePaint = initOutlinePaint;
        this.useGradientForFill = initUseGradientForBackground;   	
        this.outlineStroke = initOutlineStroke;            
    }

    private void initActionListener()
    {
        buttonSelectBackgroundPaint.setActionCommand(SELECT__PAINT_COMMAND);
        buttonEditOutline.setActionCommand(EDIT_OUTLINE_STROKE_COMMAND);

        buttonSelectBackgroundPaint.addActionListener(this);
        buttonEditOutline.addActionListener(this);
    }

    private void initItemListener()
    {
        boxBackground.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                if(background != selected)
                {
                    background = selected;
                    setMarkerPosition(selected);
                }                     
            }
        });
        boxUseGradientPaint.addItemListener(new ItemListener() {         
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean selected = (e.getStateChange()== ItemEvent.SELECTED);
                useGradientForFill = selected;                
            }
        });
    }

    private void initChangeListener()
    {
        spinnerAlpha.addChangeListener(new ChangeListener() 
        {            
            @Override
            public void stateChanged(ChangeEvent e)
            {
                alpha = ((SpinnerNumberModel)spinnerAlpha.getModel()).getNumber().floatValue();
                marker.setAlpha(alpha);                
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();
        if (command.equals(SELECT__PAINT_COMMAND)) 
        {
            attemptFillPaintSelection();
        }
        else if (command.equals(EDIT_OUTLINE_STROKE_COMMAND)) 
        {
            attemptOutlineStrokeSelection();
        }
    }

    private void hideRoot()
    {
        Component root = SwingUtilities.getRoot(editorPanel);
        root.setVisible(false);
    }

    private void attemptFillPaintSelection() 
    { 	
        if(useGradientForFill)
        {
            if(gradientDialog == null)
            {
                gradientDialog =  new SkewedGradientEditionDialog(SwingUtilities.getWindowAncestor(editorPanel));
            }
            gradientDialog.showDialog(this);			
        }
        else
        {
            Paint backgroundPaintNew = JColorChooser.showDialog(editorPanel, "Plot background color", Color.blue);	        
            if (backgroundPaintNew != null) 
            {
                fillPaint = backgroundPaintNew;
                backgroundPaintSample.setPaint(backgroundPaintNew);
                marker.setPaint(backgroundPaintNew);			
            }
        }    	
    }



    private void attemptOutlineStrokeSelection() 
    {
        if(outlineStrokeChooser == null)
        {
            outlineStrokeChooser = new StrokeChooser(SwingUtilities.getWindowAncestor(editorPanel), new BasicStrokeReceiver()
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
                    marker.setOutlineStroke(stroke);
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
                    marker.setOutlinePaint(paint);
                }       	
            }
                    );

        }
        outlineStrokeChooser.showDialog();
    }

    private void resetEditor()
    {
        boxBackground.setSelected(background);
        spinnerAlpha.setValue(alpha);
        backgroundPaintSample.setPaint(fillPaint);
        outlineStrokeSample.setStroke(outlineStroke);
        boxUseGradientPaint.setSelected(useGradientForFill);
    }

    private void setMarkerPosition( boolean backgroundNew)
    {        
        if(domainMarker)
        {
            plot.removeDomainMarker(marker);
            Layer newLayer = background ? Layer.BACKGROUND : Layer.FOREGROUND;
            plot.addDomainMarker(marker, newLayer);
        }
        else
        {
            plot.removeRangeMarker(marker);
            Layer newLayer = backgroundNew ? Layer.BACKGROUND : Layer.FOREGROUND;
            plot.addRangeMarker(marker, newLayer);
        }
    }

    private void resetMarker(CustomizableIntervalMarker p)
    {    	
        //only the last method call should invoke notification mechanism  	
        p.setAlpha(alpha);
        p.setPaint(fillPaint);	    
        p.setOutlinePaint(outlinePaint);	    
        p.setOutlineStroke(outlineStroke);
    }

    @Override
    public void resetToDefaults() 
    {
        alpha = pref.getFloat(INTERVAL_MARKER_ALPHA, 0.3f);
        fillPaint = (Paint)SerializationUtilities.getSerializableObject(pref, INTERVAL_MARKER_FILL_PAINT, Color.blue);
        outlinePaint = (Paint)SerializationUtilities.getSerializableObject(pref, INTERVAL_MARKER_OUTLINE_PAINT, Color.black);
        outlineStroke = SerializationUtilities.getStroke(pref, INTERVAL_MARKER_STROKE, CustomizableIntervalMarker.DEFAULT_OUTLINE_STROKE);
        useGradientForFill = fillPaint instanceof GradientPaint;

        resetEditor();
        resetMarker(marker);	   
    }

    @Override
    public void saveAsDefaults() 
    {	    
        pref.putFloat(INTERVAL_MARKER_ALPHA, alpha);
        try 
        {
            SerializationUtilities.putSerializableObject(pref, BACKGROUND_PAINT, fillPaint);
            SerializationUtilities.putSerializableObject(pref, INTERVAL_MARKER_OUTLINE_PAINT, outlinePaint);
            SerializationUtilities.putStroke(pref, INTERVAL_MARKER_STROKE, outlineStroke);
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
        for(CustomizableIntervalMarker marker: boundedMarkers)
        {
            resetMarker(marker);
        }
    }

    @Override
    public void undoChanges() 
    {
        setParametersToInitial();
        setMarkerPosition(background);
        resetMarker(marker);
        resetEditor();
    }

    @Override
    public Component getEditionComponent()
    {
        return editorPanel;
    }

    @Override
    public boolean isApplyToAllEnabled()
    {
        return boundedMarkers.size() > 1;
    }

    @Override
    public Paint getPaint() 
    {
        return fillPaint;
    }

    @Override
    public void setPaint(Paint paint) 
    {
        if(paint != null)
        {			
            fillPaint = paint;
            backgroundPaintSample.setPaint(fillPaint);
            marker.setPaint(fillPaint);			
        }		
    }

    private JPanel buildMainPanel()
    {
        SubPanel mainPanel = new SubPanel();

        mainPanel.addComponent(new JLabel("Background"), 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(boxBackground, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);        

        mainPanel.addComponent(new JLabel("Use gradient"), 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(boxUseGradientPaint, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 0);		

        mainPanel.addComponent(new JLabel("Background color"), 0, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(backgroundPaintSample, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);		
        mainPanel.addComponent(buttonSelectBackgroundPaint, 2, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);		

        mainPanel.addComponent(new JLabel("Alpha"), 0, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(spinnerAlpha, 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 0);        

        mainPanel.addComponent(new JLabel("Outline stroke"), 0, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0);
        mainPanel.addComponent(outlineStrokeSample, 1, 4, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 0);		
        mainPanel.addComponent(buttonEditOutline, 2, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, .03, 0);


        mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7),
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Marker style"),BorderFactory.createEmptyBorder(8, 6, 6, 8))));

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
            JOptionPane.showMessageDialog(editorPanel, "Marker style was applied to all charts of the same type", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(editorPanel, "Default marker style was changed", AtomicJ.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
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
