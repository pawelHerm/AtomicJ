package atomicJ.gui.rois.line;

import java.awt.geom.Point2D;

import atomicJ.gui.profile.ProfileStyle;

public enum ROICurveType
{
    LINE("Line")
    {
        @Override
        public ROICurve buildCurve(Point2D startPoint, Integer key, ProfileStyle style) 
        {
            return new ROILine(startPoint, startPoint, key, style);
        }
    }, 
    POLY_LINE("Polyline") 
    {
        @Override
        public ROICurve buildCurve(Point2D startPoint, Integer key, ProfileStyle style)
        {
            return new ROIPolyLine(startPoint, key, style);
        }
    }, 
    FREE_HAND("Free hand") 
    {
        @Override
        public ROICurve buildCurve(Point2D startPoint, Integer key, ProfileStyle style)
        {
            return new ROIFreeHandCurve(startPoint, key, style);
        }
    };

    private final String name;

    ROICurveType(String name)
    {
        this.name = name;
    }

    public abstract ROICurve buildCurve(Point2D startPoint, Integer key, ProfileStyle style);

    @Override
    public String toString()
    {
        return name;
    }    
}