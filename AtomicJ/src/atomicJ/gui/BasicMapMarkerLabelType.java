package atomicJ.gui;

import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.Locale;

import atomicJ.utilities.RomanNumeralConverter;


public enum BasicMapMarkerLabelType implements MapMarkerLabelType
{
    LETTER_UPPER_CASE("A, B, C ...") 
    {
        @Override
        public String getLabel(MapMarker marker) 
        {
            return Character.toString((char) (marker.getKey() + 64));
        }
    }, 
    LETTER_LOWER_CASE("a, b, c ...") {
        @Override
        public String getLabel(MapMarker marker) 
        {
            return Character.toString((char) (marker.getKey() + 96));
        }
    }, 
    ROMAN_UPPER_CASE("I, II, III ...") 
    {
        @Override
        public String getLabel(MapMarker marker) {
            return RomanNumeralConverter.convertToRoman(marker.getKey());
        }
    },
    ROMAN_LOWER_CASE("i, ii, iii ....") {
        @Override
        public String getLabel(MapMarker marker) 
        {
            String label = RomanNumeralConverter.convertToRoman(marker.getKey()).toLowerCase();
            return label;
        }
    }, 
    INTEGERS("1, 2, 3, ....") 
    {
        @Override
        public String getLabel(MapMarker marker) 
        {
            String label = marker.getKey().toString();
            return label;
        }
    }, POSITION("Position") 
    {
        @Override
        public String getLabel(MapMarker marker) 
        {
            Point2D controlPoint = marker.getControlDataPoint();
            double x = controlPoint.getX();
            double y = controlPoint.getY();

            NumberFormat format = NumberFormat.getInstance(Locale.US);
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);

            String xFormatted = format.format(x);
            String yFormatted = format.format(y);

            String label = "(" + xFormatted + ", " + yFormatted + ")";
            return label;
        }
    }, CURVE_NAME("Curve name") 
    {
        @Override
        public String getLabel(MapMarker marker) 
        {
            String label = marker.getCurveName();
            return label;
        }
    }, 

    POSITION_DESCRIPTION("Grid position") 
    {
        @Override
        public String getLabel(MapMarker marker) 
        {
            String label = marker.getPositionDescription();
            return label;
        }
    };

    private final String name;

    private BasicMapMarkerLabelType(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }

}