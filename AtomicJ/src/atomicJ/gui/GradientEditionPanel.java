
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GradientEditionPanel extends JPanel implements MouseListener, MouseMotionListener, PropertyChangeListener
{
    private static final long serialVersionUID = 1L;

    public static final String COLOR_PANEL_PREFERRED_SIZE = "COLOR_PANEL_PREFERRED_SIZE";
    public static final String COLOR_STOPS = "COLOR_STOPS";
    public static final String STOP_POSITIONS = "STOP_POSITIONS";

    private static final int SWATCH_HEIGHT = 12;
    private static final int MINIMAL_HEIGHT = 100;

    private static final int KNOB_WIDTH = 17;
    private static final int KNOB_HEIGHT = 17;
    private static final int STRIP_WIDTH = 24;
    private static final int STRIP_SWATCHES_SPACER = 14;
    private static final int MINIMAL_SWATCH_SPACER = 4;
    private static final int DEFAULT_WIDTH = 115;
    private static final int SPACER = 7;

    private int clickedStop = -1;
    private int caughtKnobIndex = -1;

    private int stopCount;
    private Color[] stopColors;
    private float[] stopPositions;

    private ColorGradient gradient;
    private SkewedGradientPaint gradientPaint;

    private Shape stripArea;
    private Shape knobRegion;
    private Shape swatchRegion;

    private List<Shape> knobAreas;

    private List<Shape> swatchAreas = new ArrayList<>();

    private Dimension preferredSize = new Dimension(DEFAULT_WIDTH, MINIMAL_HEIGHT + SWATCH_HEIGHT+MINIMAL_SWATCH_SPACER + 3*SPACER);
    private final ColorChooserDialog colorChooser;

    private final GradientEditor parent;

    public GradientEditionPanel(ColorGradient gradient, GradientEditor parent, Window w)
    {
        this.gradient = gradient;
        this.parent = parent;
        this.colorChooser = new ColorChooserDialog(w, "Choose gradient component");
        this.stopPositions = gradient.getStopPositions();
        this.stopColors = gradient.getStopColors();
        this.stopCount = stopColors.length;

        addMouseListener(this);
        addMouseMotionListener(this);

        updatePreferredSize();

        initPropertyChangeListener();   	
    }

    private void initPropertyChangeListener()
    {
        colorChooser.addPropertyChangeListener(this);
    }

    public void setColorGradient(ColorGradient gradient)
    {
        if(!this.gradient.equals(gradient))
        {
            this.gradient = gradient;
            this.stopCount = gradient.getStopCount();

            Color[] stopColorsNew = Arrays.copyOf(gradient.getStopColors(), stopCount);      	
            float[] stopPositionNew = Arrays.copyOf(gradient.getStopPositions(), stopCount);

            this.stopColors = stopColorsNew;
            this.stopPositions = stopPositionNew;

            parent.updateGradientParameters(stopColors, stopPositions);

            updateStripPaint();

            revalidate();
            repaint();
            updatePreferredSize();
        }   	
    }

    private void updateStripPaint()
    {
        gradientPaint = new SkewedGradientPaint(gradient, RectangleCorner.BOTTOM_RIGHT, Math.PI/2);
    }

    public Color[] getStopColors()
    {
        return stopColors;
    }

    private int getSwatchIndex(Point2D p)
    {
        for(int i = 0; i< swatchAreas.size();i++)
        {
            Shape area = swatchAreas.get(i);
            boolean isContained = area.contains(p);
            if(isContained)
            {
                return i;
            }
        }

        return -1;       
    }   

    private int getKnobIndex(Point2D p)
    {
        for(int i = 0; i< knobAreas.size();i++)
        {
            Shape area = knobAreas.get(i);
            boolean isContained = area.contains(p);
            if(isContained)
            {
                return i;
            }
        }

        return -1;       
    }   


    private void setStopColor(Color c, int index)
    {
        Color[] newColors = Arrays.copyOf(stopColors, stopColors.length);
        newColors[index] = c;
        this.stopColors = newColors;
        parent.updateGradientParameters(stopColors, stopPositions);
        repaint();
    }

    void invert() 
    {	   
        Color[] colorStopsNew = new Color[stopCount];

        for (int i = 0; i < stopCount; i++) 
        {
            colorStopsNew[i] = stopColors[stopCount - i - 1];		
        }
        this.stopColors = colorStopsNew;
        parent.updateGradientParameters(stopColors, stopPositions);

        repaint();
    }

    private void deleteStop(int removedIndex)
    {
        if(removedIndex> 0 && removedIndex < (stopCount -1))
        {
            int stopCountNew = stopCount -1;

            Color[] stopColorsNew = new Color[stopCountNew];
            float[] stopPositionsNew = new float[stopCountNew];

            for(int i = 0; i<removedIndex; i++)
            {
                stopColorsNew[i] = stopColors[i];
                stopPositionsNew[i] = stopPositions[i];
            }

            for(int i = removedIndex; i<stopCountNew; i++)
            {
                stopColorsNew[i] = stopColors[i + 1];
                stopPositionsNew[i] = stopPositions[i + 1];
            }		   
            this.stopCount = stopCountNew;
            this.stopColors = stopColorsNew;
            this.stopPositions = stopPositionsNew;
            parent.updateGradientParameters(stopColors, stopPositions);
            repaint();
        }
    }

    private void insertColor(Color c, float position)
    {
        int stopCountNew = stopCount + 1;
        int insertedIndex = 0;

        for(int i = 1;i<stopPositions.length;i++)
        {
            float p = stopPositions[i];
            if(position< p)
            {
                insertedIndex = i;
                break;
            }		
        }

        Color[] stopColorsNew = new Color[stopCountNew];
        float[] stopPositionsNew = new float[stopCountNew];

        for(int i = 0; i<insertedIndex; i++)
        {
            stopColorsNew[i] = stopColors[i];
            stopPositionsNew[i] = stopPositions[i];
        }

        stopColorsNew[insertedIndex] = c;
        stopPositionsNew[insertedIndex] = position;

        for(int i = insertedIndex + 1; i<stopCountNew; i++)
        {
            stopColorsNew[i] = stopColors[i - 1];
            stopPositionsNew[i] = stopPositions[i - 1];
        }

        this.stopCount = stopCountNew;	   
        this.stopColors = stopColorsNew;
        this.stopPositions = stopPositionsNew;
        parent.updateGradientParameters(stopColors, stopPositions);

        repaint();
    }


    @Override
    public void paintComponent(Graphics g) 
    {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Insets insets = getInsets();
        int left = insets.left;
        int right = insets.right;
        int top = insets.top;
        int bottom = insets.bottom;

        int width = (int)getSize().getWidth();
        int height = (int)getSize().getHeight();
        int availableWidth = width - left - right; 
        int availableHeight = height - top - bottom;

        //draws gradient strip

        double stripX0 = left + SPACER + KNOB_WIDTH;
        double stripY0 = top + SPACER + KNOB_HEIGHT/2.;
        double stripHeight = availableHeight - 4*SPACER - KNOB_HEIGHT;


        g2.setPaint(gradientPaint);

        stripArea = new Rectangle2D.Double(stripX0, stripY0, STRIP_WIDTH, stripHeight);

        g2.fill(stripArea);
        g2.setPaint(Color.black);
        g2.draw(stripArea);


        //draw controls (pluses and minuses)	     


        int swatchWidth = availableWidth - KNOB_WIDTH - STRIP_WIDTH - STRIP_SWATCHES_SPACER - 2*SPACER;

        Stroke controlsOutlineStroke = new BasicStroke(2f);
        g2.setStroke(controlsOutlineStroke);
        g2.setColor(Color.black);

        swatchAreas = new ArrayList<>();

        Stroke entriesOutlineStroke = new BasicStroke(1.5f);
        g2.setStroke(entriesOutlineStroke);

        int swatchAreasRegionX0 = left + SPACER + KNOB_WIDTH + STRIP_WIDTH + STRIP_SWATCHES_SPACER;
        int swatchAreasRegionY0 = (int)Math.rint(stripY0 - 0.5*SWATCH_HEIGHT);


        Shape droplet = getDropletShape();


        this.swatchRegion = new Rectangle2D.Double(swatchAreasRegionX0, swatchAreasRegionY0, swatchWidth, stripHeight);

        double knobRegionX0 = left + SPACER;
        double knobRegionY0 = stripY0;

        GeneralPath knob = new GeneralPath();
        knob.moveTo(knobRegionX0 + KNOB_WIDTH, knobRegionY0);
        knob.lineTo(knobRegionX0, knobRegionY0 -KNOB_HEIGHT/2.);
        knob.lineTo(knobRegionX0, knobRegionY0 + KNOB_HEIGHT/2.);

        knob.closePath();

        knobAreas = new ArrayList<>();

        double swatchMinimalSpacer = 12;
        double previousPosition = swatchAreasRegionY0 + stripHeight + swatchMinimalSpacer;
        for(int i = 0; i<stopCount; i++)
        {

            float position = stopPositions[i];
            double x = stripHeight *(1 - position);


            //draws droplets
            AffineTransform dropletTransform = new AffineTransform();

            double translationInY = Math.min(previousPosition - swatchMinimalSpacer,swatchAreasRegionY0 + x);

            dropletTransform.translate(swatchAreasRegionX0, translationInY);
            Shape currentDropet = dropletTransform.createTransformedShape(droplet);
            g2.setColor(stopColors[i]);
            g2.fill(currentDropet);
            g2.setColor(Color.black);
            g2.draw(currentDropet);

            swatchAreas.add(currentDropet.getBounds());

            //draw knobs



            AffineTransform transformKnobs = new AffineTransform();
            transformKnobs.translate(0, x);

            Shape currentKnob = transformKnobs.createTransformedShape(knob);

            knobAreas.add(currentKnob);

            Color knobPaint = (i == 0 || i == stopCount - 1) ? Color.black : Color.white;

            g2.setPaint(knobPaint);
            g2.fill(currentKnob);
            g2.setPaint(Color.black);
            g2.draw(currentKnob);

            previousPosition = translationInY;
        }                    

        this.knobRegion = new Rectangle2D.Double(knobRegionX0, knobRegionY0, KNOB_WIDTH, stripHeight + KNOB_HEIGHT);

        g2.dispose();
    }

    private Shape getDropletShape()
    {
        GeneralPath droplet = new GeneralPath();
        droplet.moveTo(0, 0);
        droplet.curveTo(2.16, 7.68, 7.2, 8.4, 6.24, 13.44);
        droplet.curveTo(5.04, 19.68, -5.04, 19.68, -6.24, 13.44);
        droplet.curveTo(-7.2, 8.4, -2.16, 7.68, 0, 0);
        droplet.closePath();

        return droplet;
    }

    private void updatePreferredSize()
    {
        Dimension newPreferredSize = new Dimension(DEFAULT_WIDTH + getInsets().left + getInsets().right, getInsets().top + getInsets().bottom + stopCount*(SWATCH_HEIGHT + MINIMAL_SWATCH_SPACER)  + 5*SPACER + MINIMAL_HEIGHT);
        Dimension oldPreferredSize = preferredSize;

        this.preferredSize = newPreferredSize;

        firePropertyChange(COLOR_PANEL_PREFERRED_SIZE, oldPreferredSize, newPreferredSize);
    }

    @Override
    public Dimension getPreferredSize()  
    {
        return preferredSize;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) 
    {
        String name = evt.getPropertyName();

        if(ColorChooserDialog.SELECTED_COLOR.equals(name))
        {
            if(clickedStop >-1)
            {
                Color selectedColor = (Color)evt.getNewValue();
                if(selectedColor != null)
                {
                    setStopColor(selectedColor, clickedStop);
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent evt) 
    {
        if(isEnabled() && caughtKnobIndex>= 0)
        {
            int y = evt.getY();	        

            float newPosition =  getNewStopPosition(y, caughtKnobIndex);
            float[] stopPositionsNew = Arrays.copyOf(stopPositions, stopCount);

            stopPositionsNew[caughtKnobIndex] = newPosition;

            stopPositions = stopPositionsNew;
            parent.updateGradientParameters(stopColors, stopPositions);
            repaint();
        }  		
    }

    private float getClickedPosition(double y)
    {
        double stripY0 = stripArea.getBounds2D().getY();
        double stripHeight = stripArea.getBounds2D().getHeight();

        float position = 1f - (float)((y - stripY0)/stripHeight);

        return position;
    }

    private float getNewStopPosition(int y, int index)
    {
        double stripY0 = stripArea.getBounds2D().getY();
        double stripHeight = stripArea.getBounds2D().getHeight();

        if(index == 0)
        {
            return 0f;
        }
        else if(index == stopCount - 1)
        {
            return 1f;
        }
        else 
        {
            float previousPosition = stopPositions[index - 1];
            float nextPosition = stopPositions[index + 1];
            float newPosition = (float)Math.max(previousPosition,Math.min(nextPosition,1f - (y - stripY0)/stripHeight));			
            return newPosition;
        }
    }

    @Override
    public void mouseMoved(MouseEvent evt) 
    {
    }

    @Override
    public void mouseReleased(MouseEvent evt)
    {
        if(isEnabled())
        {
            Point2D p = evt.getPoint();

            clickedStop = getSwatchIndex(p);
            if(clickedStop>-1)
            {
                Color initialColor = stopColors[clickedStop];
                boolean approved = colorChooser.showDialog(initialColor);
                if (!approved)
                {
                    setStopColor(initialColor, clickedStop);
                }
            }
            clickedStop = -1;	   
            caughtKnobIndex = -1;     
        }	   
    }


    @Override
    public void mouseClicked(MouseEvent e)
    {
        if(isEnabled())
        {
            Point2D p = e.getPoint();
            if(stripArea.contains(p))
            {
                float newPosition = getClickedPosition(p.getY());
                if(newPosition>0f && newPosition<1.f)
                {
                    Color newStopColor = gradient.getColor(newPosition);
                    insertColor(newStopColor, newPosition);
                }
            }
            else if(knobRegion.contains(p))
            {
                boolean multiple = e.getClickCount()>=2;
                boolean left = SwingUtilities.isLeftMouseButton(e);
                if(multiple && left)
                {
                    int knobIndex = getKnobIndex(p);
                    if(knobIndex >0 && knobIndex< (stopCount - 1))
                    {
                        deleteStop(knobIndex);
                    }
                }			   
            }
        }	   				   
    }

    @Override
    public void mouseEntered(MouseEvent e){}
    @Override
    public void mouseExited(MouseEvent e){}
    @Override
    public void mousePressed(MouseEvent e)
    {
        if(isEnabled())
        {
            Point2D p = e.getPoint();
            caughtKnobIndex = getKnobIndex(p);
        }	   
    }
}
