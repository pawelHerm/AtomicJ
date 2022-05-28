package atomicJ.utilities;

import java.awt.Dimension;
import java.awt.Toolkit;

public class GUIUtilities 
{
    private static int MINIMAL_VISIBLE_SIZE = 100;

    public static boolean isWindowSizeWellSpecified(int width, int height)
    {
        boolean sizeWellSpecified = width > MINIMAL_VISIBLE_SIZE && height > MINIMAL_VISIBLE_SIZE;       
        return sizeWellSpecified;
    }

    public static boolean isLocationWellSpecified(int width, int height, int locationX, int locationY)
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        boolean locationWellSpecified = (locationX + width) > MINIMAL_VISIBLE_SIZE
                && (locationY + height) > MINIMAL_VISIBLE_SIZE && locationX < screenSize.getWidth() - MINIMAL_VISIBLE_SIZE
                && locationY < screenSize.getHeight() - MINIMAL_VISIBLE_SIZE;
                return locationWellSpecified;
    }

    public static boolean areWindowSizeAndLocationWellSpecified(int width, int height, int locationX, int locationY)
    {
        boolean sizeWellSpecified = isWindowSizeWellSpecified(width, height);
        boolean locationWellSpecified = isLocationWellSpecified(width, height, locationX, locationY);

        boolean wellSpecified = sizeWellSpecified && locationWellSpecified;

        return wellSpecified;
    }
}
