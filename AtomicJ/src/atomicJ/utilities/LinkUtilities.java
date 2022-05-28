package atomicJ.utilities;

import java.awt.Desktop;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

//http://stackoverflow.com/questions/527719/how-to-add-hyperlink-in-jlabel

public class LinkUtilities
{
    public static void makeLinkable(JLabel c, MouseListener ml) {
        assert ml != null;
        c.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        c.addMouseListener(ml);
    }

    public static boolean isBrowsingSupported() {
        if (!Desktop.isDesktopSupported()) {
            return false;
        }
        boolean result = false;
        Desktop desktop = java.awt.Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.BROWSE)) {
            result = true;
        }
        return result;

    }
}
