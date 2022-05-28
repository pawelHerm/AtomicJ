package atomicJ.utilities;

import java.awt.Desktop;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;


//http://stackoverflow.com/questions/527719/how-to-add-hyperlink-in-jlabel

public class LinkRunner extends SwingWorker<Void, Void> {

    private final URI uri;

    public LinkRunner(URI uri) 
    {
        Validation.requireNonNullParameterName(uri, "uri");
        this.uri = uri;
    }

    @Override
    protected Void doInBackground() throws Exception {
        Desktop desktop = java.awt.Desktop.getDesktop();
        desktop.browse(uri);
        return null;
    }

    @Override
    protected void done() {
        try {
            get();
        } catch (ExecutionException | InterruptedException ee) {
            handleException(uri, ee);
        } 
    }

    private static void handleException(URI u, Exception e) {
        JOptionPane.showMessageDialog(null, "Sorry, a problem occurred while trying to open this link in your system's standard browser.", "A problem occured", JOptionPane.ERROR_MESSAGE);
    }
}