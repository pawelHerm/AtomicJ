
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class IOUtilities 
{
    private IOUtilities() {}

    public static File findClosestDirectory(File file, final String extension, int searchDepth)
    {        
        File testFile = file.isDirectory() ? file : file.getParentFile();

        //if the file is not a directory, but its parent is unknown, the only solution is to return null;
        if(testFile == null)
        {
            return testFile;
        }

        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name)
            {                
                boolean accept = name.endsWith(extension);
                return accept;
            }
        };

        File result = null;

        for(int i = 0; i<searchDepth; i++)
        {
            if(testFile == null)
            {
                return testFile;
            }

            File[] acceptedFiles = testFile.listFiles(filter);

            boolean found = (acceptedFiles != null) && (acceptedFiles.length > 0);

            if(found)
            {
                result = testFile;
                break;
            }

            testFile = testFile.getParentFile();           
        }


        return result;
    }

    public static File findLastCommonDirectory(List<File> files)
    {
        if(files == null || files.isEmpty() || files.contains(null))
        {
            return null;
        }      

        File firstFile = files.get(0);
        File testFile = firstFile.isDirectory() ? firstFile : firstFile.getParentFile();

        while(testFile != null)
        {
            boolean isCommonDirectory = true;
            Path testPath = testFile.toPath();
            for(File file: files)
            {
                Path path = file.toPath();
                isCommonDirectory = isCommonDirectory && path.startsWith(testPath);
            }
            if(isCommonDirectory)
            {
                break;
            }
            testFile = testFile.getParentFile();
        }
        return testFile;
    }

    public static List<File> findAcceptableChildrenFiles(File[] files, FileFilter fileFilter)
    {
        List<File> result = new ArrayList<>();

        for(File file: files)
        {               
            addChildrenFiles(file, result, fileFilter);
        }

        return result;
    }

    private static void addChildrenFiles(File path, List<File> sources, FileFilter filter)
    {
        if(path.isDirectory())
        {
            File[] files = path.listFiles(filter);

            for(File file: files)
            {
                addChildrenFiles(file, sources, filter);
            }
        }
        else
        {
            sources.add(path);
        }

    }

    public static boolean isFilenameValid(String file) 
    {
        File f = new File(file);
        try 
        {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) 
        {
            return false;
        }
    }

    public static String getExtension(String fileName)
    {
        int n = fileName.lastIndexOf('.') + 1;

        String extension = n > 0 ? fileName.substring(n) :"";
        return extension;
    }

    public static String getExtension(File file)
    {
        String fileName = file.getName();
        int n = fileName.lastIndexOf('.') + 1;

        String extension = n > 0 ? fileName.substring(n) :"";
        return extension;
    }

    public static String getBareName(String fileName)
    {
        int n = fileName.lastIndexOf('.');

        String bareName = n > 0 ? fileName.substring(0, n) : fileName;

        return bareName;
    }

    public static String getBareName(File file)
    {
        String fileName = file.getName();
        int n = fileName.lastIndexOf('.');

        String bareName = n > 0 ? fileName.substring(0, n) : fileName;

        return bareName;
    }

    public static ZipEntry getFirstEntryByExtension(ZipFile zipFile, String extension)
    {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while(entries.hasMoreElements())
        {
            ZipEntry entry = entries.nextElement();

            String entryName = entry.getName();
            String entryExtension = getExtension(entryName);
            boolean rightExtension = extension.equals(entryExtension);

            if(rightExtension)
            {                                    
                return entry;
            }
        }

        return null;
    }

    public static boolean containsEntryByExtension(ZipFile zipFile, String extension)
    {
        ZipEntry entry = getFirstEntryByExtension(zipFile, extension);

        boolean containsEntry = (entry != null);
        return containsEntry;
    }

    public static byte[] getZipEntryBytes(File f, String entryExtension)
    {
        try(ZipFile zipFile = new ZipFile(f))
        { 
            ZipEntry entry = getFirstEntryByExtension(zipFile, entryExtension);
            int byteCount = (int)entry.getSize();

            byte[] bytes = new byte[byteCount];

            try(BufferedInputStream ins = new BufferedInputStream(zipFile.getInputStream(entry)))
            {
                ins.read(bytes, 0, byteCount);
            }

            return bytes;

        } catch (ZipException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public static void zip(File zip, File source) throws IOException 
    {
        zip(zip, source, source.getName());
    }

    public static void zip(File zip, File source, String entryName) throws IOException 
    {
        try(ZipOutputStream zos  = new ZipOutputStream(new FileOutputStream(zip)))
        {    
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);

            try(FileInputStream fis = new FileInputStream(source))
            {
                byte[] byteBuffer = new byte[1024];
                int bytesRead = -1;
                while ((bytesRead = fis.read(byteBuffer)) != -1) {
                    zos.write(byteBuffer, 0, bytesRead);
                }
                zos.flush();
            } 

            zos.closeEntry(); 
            zos.flush();
        } 
    }

    public static String findRigthCSVFieldSeparator(DecimalFormat format)
    {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();

        char decimalSeparator = symbols.getDecimalSeparator();
        char groupingSeparator = symbols.getGroupingSeparator();

        String csvSeparator = (decimalSeparator == ',' || groupingSeparator == ',') ? ";" : ","; 
        return csvSeparator;
    }

}
