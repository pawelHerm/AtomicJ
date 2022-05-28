
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2020 by Pawe³ Hermanowicz
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

package atomicJ.analysis;


import java.awt.Frame;
import java.util.List;
import java.util.Map;

import atomicJ.data.SampleCollection;
import atomicJ.gui.UserCommunicableException;
import atomicJ.gui.curveProcessing.NumericalResultsHandler;
import atomicJ.gui.histogram.HistogramDestination;
import atomicJ.gui.results.RecalculateResultsModel;
import atomicJ.gui.results.ResultView;
import atomicJ.gui.statistics.StatisticsTable;
import atomicJ.sources.Channel1DSource;

public interface ResultDestinationBasic<S extends Channel1DSource<?>, E extends Processed1DPack<E,S>> 
{   
    public Frame getPublicationSite(); 

    public ResultBatchesCoordinator getResultBatchesCoordinator();
    public default int getPublishedBatchCount()
    {
        return getResultBatchesCoordinator().getPublishedBatchCount();
    }
    public ResultView<S, E> getResultDialog();

    public void showCurves(boolean show);
    public void showFigures(boolean show);
    public void showCalculations(boolean show);
    public void showCalculationsHistograms(boolean show);
    public HistogramDestination getCalculationHistogramDestination();
    public NumericalResultsHandler<E> getDefaultNumericalResultsHandler();
    public void requestAdditionOfCalculationHistograms(SampleCollection samples);
    public void showTemporaryCalculationStatisticsDialog(Map<String,StatisticsTable> tables, String title);
    public void showRecalculationDialog(RecalculateResultsModel<S, E> recalculateModel);
    public boolean containsFiguresForSource(S source);
    public void showFigures(S source) throws UserCommunicableException;
    public void showFigures(E pack) throws UserCommunicableException;
    public void showResults(S source) throws UserCommunicableException;
    public void showResults(List<S> sources) throws UserCommunicableException;
    public void withdrawPublication();
    public void endProcessing();

    public void startPreview();
    public void startPreview(List<S> sources);
}
