package atomicJ.utilities;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileExtensionPatternFilter implements FileFilter
{
    private final List<Pattern> acceptedPatterns;

    public FileExtensionPatternFilter(String... acceptedPatterns)
    {
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

}
