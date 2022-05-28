package atomicJ.readers.mi;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

import atomicJ.data.units.SimplePrefixedUnit;
import atomicJ.data.units.PrefixedUnit;
import atomicJ.data.units.UnitUtilities;

public class MIChunkGroup
{
    private static final double TOLERANCE = 1e-12;

    private static final MIChunkGroup NULL_INSTANCE = new MIChunkGroup(Collections.<MIChunk>emptyList(), Collections.<Integer,MIChunk>emptyMap(), Collections.<Integer,MIChunk>emptyMap(), SimplePrefixedUnit.getNullInstance());

    private static final String CHUNK = "chunk";

    private final List<MIChunk> chunks;
    private final Map<Integer,MIChunk> approachChunks;
    private final Map<Integer, MIChunk> withdrawChunks;

    private final PrefixedUnit unit;

    private MIChunkGroup(List<MIChunk> chunks, Map<Integer,MIChunk> approachChunks, Map<Integer,MIChunk> withdrawChunks, PrefixedUnit unit)
    {
        this.approachChunks = approachChunks;
        this.withdrawChunks = withdrawChunks;
        this.chunks = chunks;
        this.unit = unit;
    }

    public static MIChunkGroup getNullInstance()
    {
        return NULL_INSTANCE;
    }

    //this method would be better if FileChannel and Scanner were used, but scanner for some reasons reaches end of file unexpectedly, in the middle
    //of the MI file header
    public static MIChunkGroup readIn(FileChannel channel, double factorXY) throws IOException
    {
        long initChannelPosition = channel.position();
        int readInTextLength = 0;

        List<MIChunk> chunks = new ArrayList<>();
        Map<Integer, MIChunk> approachChunks = new HashMap<>();
        Map<Integer, MIChunk> withdrawChunks = new HashMap<>();

        int lastKey = -1;

        List<MIChunk> ommitedChunks = new ArrayList<>();
        List<MIChunk> currentChunks = new ArrayList<>();

        try(Scanner scanner = new Scanner(channel,"ISO-8859-1"))
        {
            String unitLine = scanner.nextLine();

            readInTextLength = unitLine.length() + 1;

            String[] unitLineWords = unitLine.split("\\s+");
            PrefixedUnit unit = UnitUtilities.getSIUnit(unitLineWords[unitLineWords.length - 1]);

            String lastLine = null;
            String line;
            while((line = scanner.nextLine()) != null)
            {
                readInTextLength += line.length() + 1;

                if(!line.startsWith(CHUNK))
                {
                    lastLine = line;
                    break;
                }

                String[] words = line.split("\\s+");

                int count = Integer.parseInt(words[2]);
                double start = Double.parseDouble(words[5]);
                double increment = Double.parseDouble(words[6]);
                Integer key = words.length >= 10 ? Integer.parseInt(words[9]) : Integer.parseInt(words[2]);                  

                if(key > lastKey)
                {
                    chunks.addAll(ommitedChunks);
                    ommitedChunks = new ArrayList<>();

                    chunks.addAll(currentChunks);
                    currentChunks = new ArrayList<>();
                }

                if(Math.abs(increment)>TOLERANCE)
                {
                    MIChunk chunk = new MIChunk(count, factorXY*start, factorXY*increment, key);
                    currentChunks.add(chunk);

                    if(chunk.isApproach())
                    {
                        approachChunks.put(key, chunk);
                    }
                    else
                    {
                        withdrawChunks.put(key, chunk);
                    }                       
                }

                for(int i = lastKey + 1; i < key; i++)
                {                    
                    MIChunk approachChunk = approachChunks.get(lastKey);
                    MIChunk withdrawChunk = withdrawChunks.get(lastKey);

                    if(approachChunk != null)
                    {
                        MIChunk copyChunk = approachChunk.copy(i);
                        ommitedChunks.add(copyChunk);
                        approachChunks.put(i, copyChunk);
                    }
                    if(withdrawChunk != null)
                    {
                        MIChunk copyChunk = withdrawChunk.copy(i);
                        ommitedChunks.add(copyChunk);
                        withdrawChunks.put(i, copyChunk);
                    }
                }   

                lastKey = key; 
            }

            channel.position(initChannelPosition + readInTextLength);

            chunks.addAll(ommitedChunks);
            chunks.addAll(currentChunks);

            MIChunkGroup chunkReader = new MIChunkGroup(chunks, approachChunks, withdrawChunks, unit);  

            return chunkReader;  
        }        
    }

    public boolean isEmpty()
    {
        return chunks.isEmpty();
    }

    public PrefixedUnit getUnit()
    {
        return unit;
    }

    public List<MIChunk> getAllChunks()
    {
        return chunks;
    }

    public Map<Integer, MIChunk> getApproachChunks()
    {
        return approachChunks;
    }

    public Map<Integer, MIChunk> getWithdrawChunks()
    {
        return withdrawChunks;
    }

    public SortedSet<Integer> getKeys()
    {
        SortedSet<Integer> allKeys = new TreeSet<>();
        allKeys.addAll(approachChunks.keySet());
        allKeys.addAll(withdrawChunks.keySet());

        return allKeys;
    }
}
