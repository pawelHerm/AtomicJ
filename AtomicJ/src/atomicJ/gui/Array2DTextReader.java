package atomicJ.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Array2DTextReader 
{    
    private static final String TSV_DELIMITER = "((?:\\p{javaWhitespace}*)[\\n\\t]+(?:\\p{javaWhitespace}*))";
    private static final String CSV_DELIMITER = "((?:\\p{javaWhitespace}*)[,\\n]+(?:\\p{javaWhitespace}*))";

    public double[][] read(File f, String[] extensions) throws IOException
    {
        String extension = extensions[0];
        String delimiter = "csv".equals(extension) ? CSV_DELIMITER : TSV_DELIMITER;

        return read(f, delimiter);
    }

    public double[][] read(File f, String delimiter) throws IOException
    {               
        try (BufferedReader br = new BufferedReader(new FileReader(f)))
        {   
            List<double[]> allValues = new ArrayList<>();

            String line;
            while ((line = br.readLine()) != null)
            {
                String trimmedLine = line.trim();
                if(!trimmedLine.isEmpty())
                {
                    String[] words = trimmedLine.split(delimiter);
                    int n = words.length;
                    double[] values = new double[n];

                    for(int i = 0; i<n; i++)
                    {
                        double el = Double.parseDouble(words[i]);
                        values[i] = el;
                    }

                    allValues.add(values);
                }
            }

            return allValues.toArray(new double[][] {});
        }
    }
}
