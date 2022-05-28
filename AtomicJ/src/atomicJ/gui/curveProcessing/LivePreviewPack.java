
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

package atomicJ.gui.curveProcessing;

import atomicJ.data.IndentationCurve;
import atomicJ.data.PointwiseModulusCurve;

public class LivePreviewPack 
{
    private final IndentationCurve indentationCurve;
    private final PointwiseModulusCurve pointwiseModulusCurve;

    private final double youngModulus;
    private final double transitionIndentation;
    private final double transitionForce;

    public LivePreviewPack(IndentationCurve indentationCurve, PointwiseModulusCurve pointwiseModulusCurve, double youngModulus, double transitionIndentation,double transitionForce)
    {        
        this.indentationCurve = indentationCurve;
        this.pointwiseModulusCurve = pointwiseModulusCurve; 
        this.youngModulus = youngModulus;
        this.transitionIndentation = transitionIndentation;
        this.transitionForce = transitionForce;
    }

    public IndentationCurve getIndentationCurve()
    {        
        return indentationCurve;
    }

    public PointwiseModulusCurve getPointwiseModulusCurve()
    {
        return pointwiseModulusCurve;
    }

    public double getYoungModulus()
    {
        return youngModulus;
    }

    public double getTransitionIndentation()
    {
        return transitionIndentation;
    }

    public double getTransitionForce()
    {
        return transitionForce;
    }
}
