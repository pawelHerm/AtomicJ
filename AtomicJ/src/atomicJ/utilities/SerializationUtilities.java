
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

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.io.*;
import java.util.prefs.*;

//this code is heavily based on the article at http://www.ibm.com/developerworks/java/library/j-prefapi/
// written by Greg Trevis

public class SerializationUtilities
{
    // Max byte count is 3/4 max string length (see Preferences
    // documentation).
    static private final int pieceLength = ((3*Preferences.MAX_VALUE_LENGTH)/4);

    public static void putSerializableObject(Preferences prefs, String key, Object o) throws IOException, BackingStoreException, ClassNotFoundException 
    {
        byte[] raw = objectToBytes(o);
        byte[][] pieces = breakIntoPieces(raw);
        writePieces(prefs, key, pieces);
    }

    public static void putStroke(Preferences prefs, String key, Stroke s) throws IOException, BackingStoreException, ClassNotFoundException 
    {
        byte[] raw = strokeToBytes(s);
        byte[][] pieces = breakIntoPieces(raw);
        writePieces(prefs, key, pieces);
    }

    public static Object getSerializableObject(Preferences prefs, String key, Object def)
    {	  
        try 
        {
            byte[][] pieces = readPieces(prefs, key);
            byte[] raw = combinePieces(pieces);
            Object o = bytesToObject(raw);
            return o;
        } 
        catch (BackingStoreException | ClassNotFoundException | IOException e) 
        {
            return def;
        }			
    }

    public static Stroke getStroke(Preferences prefs, String key, Stroke def)
    {
        try 
        {
            byte[][] pieces = readPieces(prefs, key);
            byte[] raw = combinePieces(pieces);
            Stroke s = bytesToStroke(raw);
            return s;
        } 
        catch (BackingStoreException | ClassNotFoundException | IOException e) 
        {
            return def;
        }
    }

    private static byte[] objectToBytes(Object o) throws IOException 
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        byte[] bytes = baos.toByteArray();
        oos.close();
        return bytes;
    }

    private static byte[] strokeToBytes(Stroke s) throws IOException 
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        writeStroke(s, oos);
        byte[] bytes = baos.toByteArray();
        oos.close();
        return bytes;
    }

    private static byte[][] breakIntoPieces(byte raw[]) 
    {
        int numPieces = (raw.length + pieceLength - 1) / pieceLength;
        byte pieces[][] = new byte[numPieces][];
        for (int i=0; i<numPieces; ++i) 
        {
            int startByte = i * pieceLength;
            int endByte = startByte + pieceLength;
            if (endByte > raw.length) endByte = raw.length;
            int length = endByte - startByte;
            pieces[i] = new byte[length];
            System.arraycopy( raw, startByte, pieces[i], 0, length );
        }

        return pieces;
    }

    //We put a boolean flag to fire PreferenceChangeEvent
    private static void writePieces(Preferences prefs, String key, byte pieces[][]) throws BackingStoreException 
    {
        boolean flag = prefs.getBoolean(key, false);
        Preferences node = prefs.node(key);
        node.clear();
        for (int i=0; i<pieces.length; ++i) 
        {
            node.putByteArray( ""+i, pieces[i] );
        }

        prefs.putBoolean(key, !flag);
    }

    private static byte[][] readPieces(Preferences prefs, String key) throws BackingStoreException 
    {
        Preferences node = prefs.node(key);
        String[] keys = node.keys();
        int numPieces = keys.length;
        byte[][] pieces = new byte[numPieces][];
        for(int i=0; i<numPieces; ++i) 
        {
            pieces[i] = node.getByteArray("" + i, null);
        }
        return pieces;
    }

    private static byte[] combinePieces( byte pieces[][] ) 
    {
        int length = 0;
        for (int i=0; i<pieces.length; ++i) 
        {
            length += pieces[i].length;
        }
        byte raw[] = new byte[length];
        int cursor = 0;
        for (int i=0; i<pieces.length; ++i) 
        {
            System.arraycopy( pieces[i], 0, raw, cursor, pieces[i].length );
            cursor += pieces[i].length;
        }
        return raw;
    }

    private static Object bytesToObject(byte raw[]) throws IOException, ClassNotFoundException 
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( raw );
        ObjectInputStream ois = new ObjectInputStream( bais );
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    private static Stroke bytesToStroke(byte raw[]) throws IOException, ClassNotFoundException 
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(raw);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Stroke s = readStroke(ois);
        ois.close();
        return s;
    }  

    private static void writeStroke(final Stroke stroke, final ObjectOutputStream stream)  throws IOException 
    {
        if (stream == null) 
        {
            throw new IllegalArgumentException("Null 'stream' argument.");   
        }

        if (stroke != null) 
        {
            stream.writeObject(false);
            if (stroke instanceof BasicStroke) 
            {
                final BasicStroke s = (BasicStroke) stroke;
                stream.writeObject(BasicStroke.class);
                stream.writeObject(s.getLineWidth());
                stream.writeObject(s.getEndCap());	
                stream.writeObject(s.getLineJoin());
                stream.writeObject(s.getMiterLimit());
                stream.writeObject(s.getDashArray());	
                stream.writeObject(s.getDashPhase());	
            }

            else 
            {	
                stream.writeObject(stroke.getClass());	
                stream.writeObject(stroke);
            }	
        }

        else 
        {
            stream.writeObject(true);
        }

    }

    private static Stroke readStroke(final ObjectInputStream stream) throws IOException, ClassNotFoundException 
    {
        if (stream == null) 
        {
            throw new IllegalArgumentException("Null 'stream' argument.");   
        }

        Stroke result = null;
        final boolean isNull = (Boolean)stream.readObject();

        if (!isNull) 
        {
            final Class c = (Class) stream.readObject();

            if (c.equals(BasicStroke.class)) {

                final float width = (Float)stream.readObject();
                final int cap = (Integer)stream.readObject();
                final int join = (Integer)stream.readObject();
                final float miterLimit = (Float)stream.readObject();
                final float[] dash = (float[]) stream.readObject();
                final float dashPhase = (Float)stream.readObject();
                result = new BasicStroke(width, cap, join, miterLimit, dash, dashPhase);
            }
            else 
            {
                result = (Stroke) stream.readObject();
            }
        }

        return result;	    
    }
}