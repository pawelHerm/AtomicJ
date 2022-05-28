
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

package atomicJ.gui;

import java.util.prefs.Preferences;

import atomicJ.data.Channel1D;

public class RendererFactory 
{
    private RendererFactory(){};

    public static Channel1DRenderer getChannel1DRenderer(Channel1D channel)
    {
        StyleTag styleTag = new StandardStyleTag(channel.getIdentifier());       
        return getChannel1DRenderer(channel, styleTag);
    }

    public static Channel1DRenderer getChannel1DRenderer(Channel1D channel, StyleTag styleTag)
    {
        Preferences pref = Preferences.userNodeForPackage(Channel1DRenderer.class).node(styleTag.getPreferredStyleKey());

        Channel1DRenderer renderer = channel.isDicrete() ? new DiscreteSeriesRenderer(PreferredDiscreteSeriesRendererStyle.getInstance(pref, styleTag),channel.getIdentifier(), styleTag, channel.getName()) 
                : new ContinuousSeriesRenderer(PreferredContinuousSeriesRendererStyle.getInstance(pref, styleTag), channel.getIdentifier(), styleTag, channel.getName());
        return renderer;
    }

    public static Channel1DRenderer getChannel1DErrorBarRenderer(Channel1D channel)
    {
        StyleTag styleTag = new StandardStyleTag(channel.getIdentifier());       
        return getChannel1DRenderer(channel, styleTag);
    }

    public static Channel1DRenderer getChannel1DErrorBarRenderer(Channel1D channel, StyleTag styleTag)
    {
        Preferences pref = Preferences.userNodeForPackage(Channel1DRenderer.class).node(styleTag.getPreferredStyleKey());

        Channel1DRenderer renderer = new Channel1DWithErrorRenderer(PreferredContinuousSeriesErrorRendererStyle.getInstance(pref, styleTag), channel.getIdentifier(), styleTag, channel.getName());
        return renderer;
    }
}
