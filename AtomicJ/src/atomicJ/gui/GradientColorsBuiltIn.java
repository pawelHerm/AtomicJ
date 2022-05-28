
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

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

public class GradientColorsBuiltIn 
{
    private static final Map<String, ColorGradient> GRADIENTS = new LinkedHashMap<>();

    static
    {
        ColorGradient gradient = new ColorGradientInterpolation(new Color[] {new Color(0, 0, 0), new Color(79, 25, 7), new Color(119, 49, 0), new Color(158, 81, 0), new Color(178, 115, 0), new Color(211, 157, 0), new Color(233, 186, 16), new Color(254, 215, 26), new Color(255, 249, 112)},
                new float[] {0.0f, 0.1576087f, 0.32427537f, 0.44746378f, 0.5416666f, 0.66284406f, 0.767094f, 0.88247865f, 1.0f}, 1024);
        GRADIENTS.put("Golden", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {Color.black, new Color(153,51,0) , new Color(255,204,0), new Color(255,255,102)}, new float[] {0, 0.2f,0.5f, 1}, 1024);
        GRADIENTS.put("Light golden", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {Color.black, Color.white}, 1024);
        GRADIENTS.put("Black & white", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {Color.black , new Color(0, 0, 195), new Color(51, 153, 255) , new Color(153,255,255)}, 1024);
        GRADIENTS.put("Ocean", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(0, 0, 0), new Color(9, 38, 0), new Color(17, 76, 0), new Color(54, 106, 5), new Color(87, 143, 9), new Color(133, 162, 11), new Color(183, 189, 14), new Color(215, 210, 19), new Color(251, 240, 74)},
                new float[] {0.0f, 0.13990825f, 0.27564102f, 0.4059633f, 0.5206422f, 0.63461536f, 0.7545872f, 0.87376237f, 1.0f},1024);
        GRADIENTS.put("Jungle", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {Color.black , new Color(101, 34, 1), new Color(204, 102, 0) , new Color(255,204,0), new Color(255,255,102)}, 1024);
        GRADIENTS.put("Browns", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(0, 0, 0), new Color(38, 30, 2), new Color(57, 41, 5), new Color(72, 50, 17), new Color(77, 70, 50), new Color(84, 85, 86), new Color(46, 65, 74), new Color(18, 28, 35), new Color(2, 5, 6)},
                new float[] {0.0f, 0.14449541f, 0.25f, 0.3568376f, 0.4551282f, 0.6004273f, 0.72863245f, 0.8696581f, 1.0f}, 1024);
        GRADIENTS.put("Stones", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(0, 0, 0), new Color(51, 34, 0), new Color(82, 57, 19), new Color(121, 81, 0), new Color(147, 109, 0), new Color(180, 149, 18), new Color(212, 180, 16), new Color(253, 228, 55)},
                new float[] {0.0f, 0.14449541f, 0.3440171f, 0.5363248f, 0.69871795f, 0.8354701f, 0.92948717f, 1.0f}, 1024);
        GRADIENTS.put("Sands", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {Color.black,new Color(58,0,0) , new Color(219, 0, 0), new Color(255, 51, 0) , new Color(255,153,0), new Color(255,255,102)}, 1024);
        GRADIENTS.put("Sunset", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {Color.black, new Color(42,0,52), new Color(42,0,52) , new Color(102,0,51), new Color(204,0,0) , new Color(255,102,0),new Color(255,255,51)}, 1024);
        GRADIENTS.put("Plum", gradient);		

        gradient = new ColorGradientInterpolation(new Color[] {new Color(0, 0, 0), new Color(30, 0, 51), new Color(62, 3, 48), new Color(92, 1, 53), new Color(174, 0, 0), new Color(201, 90, 20), new Color(224, 144, 25), new Color(245, 223, 79)},
                new float[] {0.0f, 0.13967136f, 0.26173708f, 0.37676057f, 0.5903756f, 0.7664319f, 0.87441313f, 1.0f}, 1024);

        GRADIENTS.put("Plum 2", gradient);		

        gradient = new ColorGradientInterpolation(new Color[] {Color.white, new Color(19,5,19), new Color(42,0,52), new Color(42,0,52) , new Color(102,0,51), new Color(204,0,0) , new Color(255,102,0),new Color(255,255,51)}, 1024);
        GRADIENTS.put("Mouldy plum", gradient);		

        gradient = new ColorGradientInterpolation(new Color[] {Color.black,new Color(18,0,21), new Color(48,0,41) , new Color(75,1,75), new Color(174,0,0) , new Color(226,135,25), new Color(245,223,79)}, 1024);
        GRADIENTS.put("Deep plum", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {Color.black, new Color(51,0,51),new Color(53, 1, 78) , new Color(74, 1, 141), new Color(119,7,224) , new Color(180,109,250), new Color(255,204,255)}, 1024);
        GRADIENTS.put("Lilac", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {Color.black,  new Color(32,0,35) ,new Color(39,0,43), new Color(60, 4,85), new Color(252,79,190) , new Color(255, 153, 204)}, 1024);
        GRADIENTS.put("Carnation", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(0, 0, 0), new Color(51, 0, 31), new Color(153, 0, 51), new Color(188, 0, 54), new Color(219, 52, 77), new Color(255, 102, 102), new Color(255, 153, 102), new Color(255, 204, 102), new Color(255, 255, 102)},
                new float[] {0.0f, 0.14449541f, 0.31410256f, 0.41666666f, 0.5277778f, 0.6645299f, 0.75f, 0.8696581f, 1.0f}, 1024);
        GRADIENTS.put("Foxglove", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {Color.black, new Color(0,43,19), new Color(0,56,20) , new Color(0, 64,10), new Color(0,93,5) , new Color(120,162,3), new Color(204,204,0),new Color(255,204,0), new Color(255,255,51)}, 1024);
        GRADIENTS.put("Avocado", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {Color.black, new Color(76, 1, 1), new Color(128, 3, 3) , new Color(150, 2, 2), new Color(205,30,4) , new Color(255,102,0), new Color(255,204,0)}, 1024);
        GRADIENTS.put("Bloody", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(133,3,216) , new Color(7,76,245), new Color(3,148,148) , new Color(0,190,0),new Color(186,186,0),new Color(227,33,6),new Color(175,4,4)}, 1024);
        GRADIENTS.put("Rainbow", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {Color.black, new Color(51,0,51) , new Color(0,0,153), new Color(0,51,51) , new Color(0,51,0),new Color(102,102,0),new Color(153,19,0),new Color(102,0,0)}, 1024);
        GRADIENTS.put("Dark rainbow", gradient);

        gradient = new ColorGradientBands(new Color[] {new Color(0,10,0) , new Color(0,30,0), new Color(1,56,1) , new Color(46,119,7),new Color(51,153,0),new Color(102,204,0)
                ,new Color(153,255,0),new Color(204,255,0),new Color(255,255,0),new Color(255,197,0),new Color(255,153,0),new Color(255,102,0),
                new Color(255,51,0),new Color(204,0,0),new Color(153,0,0),new Color(102,0,0),new Color(9,0,0)});
        GRADIENTS.put("Hypsometric", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {Color.black, new Color(5,5,63), new Color(47, 4, 100), new Color(98, 20, 122),
                new Color(242, 127, 20), new Color(245,179,9), new Color(250,243,13)}, 1024);
        GRADIENTS.put("Violet", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(0,0,0),new Color(32,32,8),new Color(53,51,2),new Color(91,83,0),new Color(153,133,0),new Color(190,172,6),new Color(236,214,13)},
                new float[] {0f,0.116f,0.25f,0.427f,0.72f,0.844f,1.0f}, 1024);
        GRADIENTS.put("Olive", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {Color.black, new Color(36,37,15),new Color(64,60,25),new Color(104,105,71),new Color(187,176,130),new Color(215,200,107),new Color(255,255,96)}, 
                new float[] {0f,0.11f,0.25f,0.427f,0.67f,0.814f,1.0f}, 1024);
        GRADIENTS.put("Smoke", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(0,0,0),new Color(68,3,15),new Color(112,5,33),new Color(159,49,74),new Color(227,93,102),new Color(251,153,99),new Color(253,216,127)},
                new float[] {0f,0.11f,0.25f,0.427f,0.67f,0.815f,1.0f}, 1024);
        GRADIENTS.put("Bortsch", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(255,255,255),new Color(68,3,15),new Color(125,11,41),new Color(159,49,74),new Color(227,93,102),new Color(251,153,99),new Color(254,231,118)},
                new float[] {0f,0.11f,0.25f,0.427f,0.67f,0.814f,1.0f}, 1024);
        GRADIENTS.put("Creamy bortsch", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(4,4,4),new Color(34,22,24),new Color(79,52,59),new Color(128,89,98),new Color(165,99,105),new Color(216,122,122),new Color(253,161,151)},
                new float[] {0.0f,0.11f,0.25f,0.427f,0.67f,0.814f,1.0f}, 1024);
        GRADIENTS.put("Salmon", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(0, 0, 0), new Color(82, 0, 21), new Color(143, 13, 25), new Color(189, 37, 37), new Color(208, 101, 33), new Color(226, 185, 23), new Color(245, 221, 58), new Color(241, 255, 153)},
                new float[] {0.0f, 0.11752137f, 0.26430976f, 0.4023569f, 0.5673401f, 0.7828283f, 0.9040404f, 1.0f}, 1024);
        GRADIENTS.put("Maple", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(0, 0, 0), new Color(4, 27, 0), new Color(67, 90, 1), new Color(142, 142, 0), new Color(205, 172, 0), new Color(239, 185, 1), new Color(236, 146, 8), new Color(246, 120, 6), new Color(230, 62, 3), new Color(208, 18, 11), new Color(161, 6, 19), new Color(83, 29, 2), new Color(0,0,0)},
                new float[] {0.0f, 0.09228188f, 0.214f, 0.338f, 0.442f, 0.5265958f, 0.6258865f, 0.68971634f, 0.7606383f, 0.82894737f, 0.91541356f, 0.95f, 1.0f }, 1024);
        GRADIENTS.put("Autumn leaves", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(0, 0, 0), new Color(53, 1, 25), new Color(96, 5, 48), new Color(106, 8, 40), new Color(132, 9, 17), new Color(161, 6, 19), new Color(208, 18, 11), new Color(230, 62, 3), new Color(246, 120, 6), new Color(236, 146, 8), new Color(225, 189, 9), new Color(205, 172, 0), new Color(142, 142, 0), new Color(82, 110, 1), new Color(56, 91, 0), new Color(31, 81, 0), new Color(10, 70, 43), new Color(6, 49, 60), new Color(7, 34, 87), new Color(18, 15, 84), new Color(38, 2, 64), new Color(25, 1, 37), new Color(12, 0, 20)},
                new float[] {0.0f, 0.040186916f, 0.08691589f, 0.12429905f, 0.17102803f, 0.21775702f, 0.26074767f, 0.30373833f, 0.3542056f, 0.38971964f, 0.43831775f, 0.4813084f, 0.53364486f, 0.5859813f, 0.63084114f, 0.6906542f, 0.72803736f, 0.7766355f, 0.81588787f, 0.863472f, 0.9122966f, 0.9575045f, 1.0f}, 1024);
        GRADIENTS.put("Revealing", gradient);

        gradient = new ColorGradientBands(new Color[] {new Color(0, 0, 0), new Color(53, 1, 25),  new Color(106, 8, 40), new Color(132, 9, 17), new Color(161, 6, 19), new Color(208, 18, 11), new Color(230, 62, 3), new Color(246, 120, 6), new Color(236, 146, 8), new Color(225, 189, 9), new Color(205, 172, 0), new Color(142, 142, 0), new Color(82, 110, 1), new Color(56, 91, 0), 
                new Color(31, 81, 0), new Color(10, 70, 43), new Color(6, 49, 60), new Color(7, 34, 87), new Color(38, 2, 64), new Color(25, 1, 37), new Color(12, 0, 20)});
        GRADIENTS.put("Hyps-revealing", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(0, 13, 13), new Color(8, 68, 34), new Color(17, 87, 42), new Color(55, 78, 1), new Color(67, 90, 1), new Color(142, 142, 0), new Color(205, 172, 0), new Color(239, 185, 1), new Color(236, 146, 8), new Color(246, 120, 6), new Color(230, 62, 3), new Color(208, 18, 11), new Color(161, 6, 19), new Color(110, 21, 8), new Color(75, 0, 36), new Color(74, 30, 43), new Color(0, 0, 0)},
                new float[] {0.0f, 0.009683073f, 0.013204225f, 0.022007048f, 0.029049296f, 0.050176058f, 0.09242958f, 0.12235916f, 0.15052816f, 0.19278169f, 0.24735916f, 0.3125f, 0.340669f, 0.43045774f, 0.61187845f, 0.7417127f, 1.0f}, 1024);
        GRADIENTS.put("Low-revealing", gradient);
        gradient = new ColorGradientInterpolation(new Color[] {new Color(0, 0, 0), new Color(74, 30, 43), new Color(75, 0, 36), new Color(110, 21, 8), new Color(161, 6, 19), new Color(208, 18, 11), new Color(230, 62, 3), new Color(246, 120, 6), new Color(236, 146, 8), new Color(239, 185, 1), new Color(205, 172, 0), new Color(142, 142, 0), new Color(67, 90, 1), new Color(55, 78, 1), new Color(17, 87, 42), new Color(8, 68, 34), new Color(0, 13, 13)},
                new float[] {0.0f, 0.2582873f, 0.38812155f, 0.5695423f, 0.65933096f, 0.6875f, 0.75264084f, 0.8072183f, 0.8494718f, 0.87764084f, 0.9075704f, 0.9498239f, 0.9709507f, 0.97799295f, 0.9867958f, 0.9903169f, 1.0f}, 1024);
        GRADIENTS.put("High-revealing", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(0, 0, 0), new Color(255,0,0)},
                new float[] {0.0f, 1.0f}, 1024);
        GRADIENTS.put("Red", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(0, 0, 0), new Color(0,255,0)},
                new float[] {0.0f, 1.0f}, 1024);
        GRADIENTS.put("Green", gradient);

        gradient = new ColorGradientInterpolation(new Color[] {new Color(0, 0, 0), new Color(0, 0, 255)},
                new float[] {0.0f, 1.0f}, 1024);
        GRADIENTS.put("Blue", gradient);
    }

    public static Map<String, ColorGradient> getGradients()
    {
        return GRADIENTS;
    }
}
