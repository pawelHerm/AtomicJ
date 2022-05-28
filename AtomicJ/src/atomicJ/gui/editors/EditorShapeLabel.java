package atomicJ.gui.editors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import atomicJ.gui.PlotStyleUtilities;

public class EditorShapeLabel
{
    private static final Shape[] SHAPES = PlotStyleUtilities.getNonZeroAreaShapes();    

    private final JLabel label;

    private EditorShapeLabel(JLabel label)
    {
        this.label = label;
    }

    public static EditorShapeLabel buildShapeLabel()
    {
        JLabel shapeLabel = new JLabel();
        shapeLabel.setBorder(BorderFactory.createLineBorder(Color.black));
        shapeLabel.setHorizontalAlignment(SwingConstants.LEFT);

        EditorShapeLabel labelEditor = new EditorShapeLabel(shapeLabel);

        return labelEditor;
    }


    public static EditorShapeLabel buildShapeLabel(int markerIndex, float markerSize, Paint markerPaint)
    {
        BufferedImage img = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);     
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(markerPaint);
        g2.fill(SHAPES[markerIndex]);
        g2.dispose();

        String shapeString = "Size: " + NumberFormat.getInstance(Locale.US).format(markerSize);
        JLabel shapeLabel = new JLabel(shapeString, new ImageIcon(img), SwingConstants.LEFT);
        shapeLabel.setBorder(BorderFactory.createLineBorder(Color.black));

        EditorShapeLabel labelEditor = new EditorShapeLabel(shapeLabel);

        return labelEditor;
    }

    public void update(int markerIndex, float markerSize, Paint markerPaint)
    {
        BufferedImage img = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);       
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(markerPaint);
        g2.fill(SHAPES[markerIndex]);
        g2.dispose();

        String shapeString = "Size: " + NumberFormat.getInstance(Locale.US).format(markerSize);
        label.setText(shapeString);
        label.setIcon(new ImageIcon(img));
        label.repaint();
    }

    public void setEnabled(boolean enabled)
    {
        label.setEnabled(enabled);
    }

    public JLabel getLabel()
    {
        return label;
    }
}