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

package atomicJ.gui.results;

import java.util.EventListener;

import atomicJ.analysis.Processed1DPack;
import atomicJ.sources.Channel1DSource;


public interface ResultDataListener <S extends Channel1DSource<?>, E extends Processed1DPack<E, S>>  extends EventListener
{
    public void batchesCleared(ResultDataEvent<S,E> event);
    public void batchesAdded(ResultDataEvent<S,E> event);
    public void batchesRemoved(ResultDataEvent<S,E> event); 
    public void packsAdded(ResultDataEvent<S,E> event);
    public void packsRemoved(ResultDataEvent<S,E> event);
}



