
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

package atomicJ.gui.save;



import static atomicJ.gui.save.SaveModelProperties.FIRST_FRAME;
import static atomicJ.gui.save.SaveModelProperties.FRAME_COUNT;
import static atomicJ.gui.save.SaveModelProperties.FRAME_RATE;
import static atomicJ.gui.save.SaveModelProperties.LAST_FRAME;
import static atomicJ.gui.save.SaveModelProperties.MOVIE_LENGTH;
import static atomicJ.gui.save.SaveModelProperties.PLAYED_BACKWARDS;
import static atomicJ.gui.save.SaveModelProperties.QUALITY;
import static atomicJ.gui.save.SaveModelProperties.TIFF_MULTIPAGE_COMPRESSION;

import java.awt.Component;
import java.awt.geom.Rectangle2D;

public class TIFFMovieFormatModel extends BasicFormatModel 
{
    private int frameRate = 1;
    private int firstFrame = 0;
    private int lastFrame = 1;
    private int frameCount = 2;
    private double movieLength = frameCount/(double)frameRate;

    private boolean backwards;

    private SaveQuality quality = SaveQuality.HIGH;
    private TIFFMovieCompressionMethod compression = TIFFMovieCompressionMethod.UNCOMPRESSED;

    public TIFFMovieFormatSaver getChartSaver(Component parent)
    {
        boolean saveDataArea = getSaveDataArea();
        Rectangle2D chartInitialArea = getChartInitialArea();

        int width =  (int)getWidth();
        int height = (int)getHeight();

        TIFFMovieFormatSaver saver = new TIFFMovieFormatSaver(chartInitialArea, width, 
                height, compression, firstFrame, lastFrame, frameRate, backwards, saveDataArea);


        return saver;
    }

    public void specifyInitialMovieParameters(int firstFrame, int lastFrame, int frameCount, int frameRate, boolean playedBackwards)
    {
        setFrameCount(frameCount);
        setFirstFrame(firstFrame);
        setLastFrame(lastFrame);
        setFrameRate(frameRate);
        setPlayedBackwards(playedBackwards);
    }

    public boolean isPlayedBackwards()
    {
        return backwards;
    }

    public void setPlayedBackwards(boolean backwardsNew)
    {
        boolean backwardsOld = this.backwards;
        this.backwards = backwardsNew;

        firePropertyChange(PLAYED_BACKWARDS, backwardsOld, backwardsNew);
    }

    public int getFirstFrame()
    {
        return firstFrame;
    }

    public void setFirstFrame(int firstFrameNew)
    {
        if(firstFrameNew >= 0 && firstFrameNew<frameCount)
        {
            int firstFrameOld = this.firstFrame;
            this.firstFrame = firstFrameNew;

            firePropertyChange(FIRST_FRAME, firstFrameOld, firstFrameNew);
        }	
    }

    public int getLastFrame()
    {
        return lastFrame;
    }

    public void setLastFrame(int lastFrameNew)
    {
        if(lastFrameNew >= 0 && lastFrameNew<frameCount)
        {
            int lastFrameOld = this.lastFrame;
            this.lastFrame = lastFrameNew;

            firePropertyChange(LAST_FRAME, lastFrameOld, lastFrameNew);
        }		
    }

    public int getFrameCount()
    {
        return frameCount;
    }

    public void setFrameCount(int frameCountNew)
    {
        int frameCountOld = this.frameCount;
        this.frameCount = frameCountNew;

        firePropertyChange(FRAME_COUNT, frameCountOld, frameCountNew);
    }

    public double getMovieLength()
    {
        return movieLength;
    }

    public void setMovieLength(double requestedMovieLength)
    {
        double frameRateOld = this.frameRate;
        this.frameRate = (int)Math.max(1,Math.rint(frameCount/requestedMovieLength));

        double movieLengthOld = this.movieLength;
        this.movieLength = frameCount/(double)frameRate;	

        firePropertyChange(MOVIE_LENGTH, movieLengthOld, this.movieLength);
        firePropertyChange(FRAME_RATE, frameRateOld, this.frameRate);
    }

    public int getFrameRate()
    {
        return frameRate;
    }

    public void setFrameRate(int frameRateNew)
    {
        double frameRateOld = this.frameRate;
        this.frameRate = frameRateNew;

        double movieLengthOld = this.movieLength;
        this.movieLength = frameCount/(double)frameRate;

        firePropertyChange(FRAME_RATE, frameRateOld, this.frameRate);
        firePropertyChange(MOVIE_LENGTH, movieLengthOld, this.movieLength);		
    }

    public SaveQuality getQuality() 
    {
        return quality;
    }

    public void setQuality(SaveQuality qualityNew) 
    {
        SaveQuality qualityOld = quality;
        this.quality = qualityNew;

        firePropertyChange(QUALITY, qualityOld, qualityNew);
    }

    public TIFFMovieCompressionMethod getCompression()
    {
        return compression;
    }

    public void setCompression(TIFFMovieCompressionMethod compressionNew) 
    {
        TIFFMovieCompressionMethod compressionOld = compression;
        this.compression = compressionNew;

        firePropertyChange(TIFF_MULTIPAGE_COMPRESSION, compressionOld, compressionNew);
    }
}