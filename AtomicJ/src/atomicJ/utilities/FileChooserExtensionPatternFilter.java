package atomicJ.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;


public final class FileChooserExtensionPatternFilter extends FileFilter 
{
    private final String description;
    private final List<Pattern> acceptedPatterns;

    public FileChooserExtensionPatternFilter(String description, String... acceptedPatterns) {
        if (acceptedPatterns == null || acceptedPatterns.length == 0) {
            throw new IllegalArgumentException(
                    "Extensions must be non-null and not empty");
        }
        this.description = description;
        this.acceptedPatterns = new ArrayList<>();

        for(String ext : acceptedPatterns)
        {
            this.acceptedPatterns.add(Pattern.compile(ext));
        }
    }

    @Override
    public boolean accept(File f) 
    {
        if (f == null) 
        {
            return false;
        }

        if (f.isDirectory()) 
        {
            return true;
        }

        String ext = IOUtilities.getExtension(f);

        for (Pattern pattern : acceptedPatterns) 
        {
            Matcher matcher = pattern.matcher(ext);
            if (matcher.matches()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public List<Pattern> getExtensions() {

        return new ArrayList<>(acceptedPatterns);
    }

    @Override
    public String toString() {
        return super.toString() + "[description=" + getDescription() +
                " extensions=" + java.util.Arrays.asList(getExtensions()) + "]";
    }
}

