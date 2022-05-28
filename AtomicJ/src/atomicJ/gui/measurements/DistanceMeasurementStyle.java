package atomicJ.gui.measurements;

import java.awt.Paint;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

import atomicJ.gui.FormattableNumericalDataAdapter;
import atomicJ.gui.StandardNumericalFormatStyle;
import atomicJ.gui.annotations.AnnotationStyle;

import static atomicJ.gui.measurements.PreferredDistanceMeasurementStyle.*;


public class DistanceMeasurementStyle extends AnnotationStyle<PreferredDistanceMeasurementStyle>
{
    private static final long serialVersionUID = 1L;

    private boolean drawAbscissaMeasurementUnfinished;
    private boolean drawOrdinateMeasurementUnfinished;

    private boolean drawAbscissaMeasurementFinished;
    private boolean drawOrdinateMeasurementFinished;

    private final StandardNumericalFormatStyle decimalFormatFinished;
    private final StandardNumericalFormatStyle decimalFormatUnfinished;

    public DistanceMeasurementStyle(Preferences pref, Paint defaultPaint)
    {
        super(PreferredDistanceMeasurementStyle.getInstance(pref, defaultPaint));

        this.decimalFormatFinished = new StandardNumericalFormatStyle(pref.node("Finished"));
        this.decimalFormatUnfinished = new StandardNumericalFormatStyle(pref.node("Unfinished"));

        setDefaultMeasurementStyleStyle(getPreferredStyle());
        initPropertyChangeListeners();
    }

    private void setDefaultMeasurementStyleStyle(PreferredDistanceMeasurementStyle prefStyle)
    {
        this.drawAbscissaMeasurementUnfinished = prefStyle.isDrawAbscissaMeasurementUnfinished();
        this.drawOrdinateMeasurementUnfinished = prefStyle.isDrawOrdinateMeasurementUnfinished();

        this.drawAbscissaMeasurementFinished = prefStyle.isDrawAbscissaMeasurementFinished();
        this.drawOrdinateMeasurementFinished = prefStyle.isDrawOrdinateMeasurementFinished();
    }

    private void initPropertyChangeListeners()
    {
        decimalFormatFinished.addListener(new FormattableNumericalDataAdapter() {

            @Override
            public void formatChanged() {
                firePropertyChange("Format", false, true);                
            }
        });

        decimalFormatUnfinished.addListener(new FormattableNumericalDataAdapter() {

            @Override
            public void formatChanged() {
                firePropertyChange("Format", false, true);                

            }
        });
    }

    @Override
    public void setDefaultStyle()
    {   
        super.setDefaultStyle();
        setDefaultMeasurementStyleStyle(getPreferredStyle());   
    }   

    @Override
    public void saveStyleAsDefault()
    {
        super.saveStyleAsDefault(); 

        Preferences pref = getPreferences();

        pref.putBoolean(DRAW_ABSCISSA_MEASUREMENT_UNFINISHED, drawAbscissaMeasurementUnfinished);
        pref.putBoolean(DRAW_ORDINATE_MEASUREMENT_UNFINISHED, drawOrdinateMeasurementUnfinished);
        pref.putBoolean(DRAW_ABSCISSA_MEASUREMENT_FINISHED, drawAbscissaMeasurementFinished);
        pref.putBoolean(DRAW_ORDINATE_MEASUREMENT_FINISHED, drawOrdinateMeasurementFinished);

        decimalFormatFinished.saveToPreferences();
        decimalFormatUnfinished.saveToPreferences();
    }

    public StandardNumericalFormatStyle getDecimalFormatManagerFinished()
    {
        return decimalFormatFinished;
    }

    public StandardNumericalFormatStyle getDecomalFormatManagerUnfinished()
    {
        return decimalFormatUnfinished;
    }

    public DecimalFormat getDecimalFormat(boolean finished)
    {
        DecimalFormat format = finished ? decimalFormatFinished.getDecimalFormat() : decimalFormatUnfinished.getDecimalFormat();
        return format;
    }

    public boolean isDrawAbscissaMeasurementUnfinished()
    {
        return drawAbscissaMeasurementUnfinished;
    }

    public void setDrawAbsicssaMeasurementUnfinished(boolean drawNew)
    {
        boolean drawOld = this.drawAbscissaMeasurementUnfinished;
        this.drawAbscissaMeasurementUnfinished = drawNew;

        firePropertyChange(DRAW_ABSCISSA_MEASUREMENT_UNFINISHED, drawOld, drawNew);
    }

    public boolean isDrawAbscissaMeasurementFinished()
    {
        return drawAbscissaMeasurementFinished;
    }

    public void setDrawAbsicssaMeasurementFinished(boolean drawNew)
    {
        boolean drawOld = this.drawAbscissaMeasurementFinished;
        this.drawAbscissaMeasurementFinished = drawNew;

        firePropertyChange(DRAW_ABSCISSA_MEASUREMENT_FINISHED, drawOld, drawNew);
    }

    public boolean isDrawOrdinateMeasurementUnfinished()
    {
        return drawOrdinateMeasurementUnfinished;
    }

    public void setDrawOrdinateMeasurementUnfinished(boolean drawNew)
    {
        boolean drawOld = this.drawOrdinateMeasurementUnfinished;
        this.drawOrdinateMeasurementUnfinished = drawNew;

        firePropertyChange(DRAW_ORDINATE_MEASUREMENT_UNFINISHED, drawOld, drawNew);
    }

    public boolean isDrawOrdinateMeasurementFinished()
    {
        return drawOrdinateMeasurementFinished;
    }

    public void setDrawOrdinateMeasurementFinished(boolean drawNew)
    {
        boolean drawOld = this.drawOrdinateMeasurementFinished;
        this.drawOrdinateMeasurementFinished = drawNew;

        firePropertyChange(DRAW_ORDINATE_MEASUREMENT_FINISHED, drawOld, drawNew);
    }
}
