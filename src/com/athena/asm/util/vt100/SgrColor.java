/*
 * Copyright (c) 2009-2011 Graham Edgecombe.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.athena.asm.util.vt100;

import android.graphics.Color;


/**
 * Contains colors used by the SGR ANSI escape sequence.
 * @author Graham Edgecombe
 */
final class SgrColor {
    
    public static final int[] COLOR_NORMAL_DAY = new int[] {
    	Color.rgb(255, 255, 250),
    	Color.rgb(128, 0, 0),
    	Color.rgb(0, 128, 0),
    	Color.rgb(128, 128, 0),        
    	Color.rgb(0, 0, 128),
    	Color.rgb(128, 0, 128),
    	Color.rgb(0, 128, 128),    	
    	Color.rgb(0, 0, 0)
    };
    
    public static final int[] COLOR_NORMAL_NIGHT = new int[] {
    	Color.rgb(0, 0, 0),
    	Color.rgb(128, 0, 0),
    	Color.rgb(0, 128, 0),
    	Color.rgb(128, 128, 0),        
    	Color.rgb(0, 0, 128),
    	Color.rgb(128, 0, 128),
    	Color.rgb(0, 128, 128),
    	Color.rgb(192, 192, 192)
    };    
    
    public static int[] COLOR_NORMAL = COLOR_NORMAL_DAY;

    public static final int[] COLOR_BRIGHT_DAY = new int[] {
    	Color.rgb(255, 255, 255),    	
    	Color.rgb(255, 0, 0),
    	Color.rgb(0, 255, 0),
    	Color.rgb(255, 255, 0),        
    	Color.rgb(0, 0, 255),
    	Color.rgb(255, 0, 255),
    	Color.rgb(0, 255, 255),
    	Color.rgb(128, 128, 128)
    };    
    
    public static final int[] COLOR_BRIGHT_NIGHT = new int[] {
    	Color.rgb(128, 128, 128),
    	Color.rgb(255, 0, 0),
    	Color.rgb(0, 255, 0),
    	Color.rgb(255, 255, 0),        
    	Color.rgb(0, 0, 255),
    	Color.rgb(255, 0, 255),
    	Color.rgb(0, 255, 255),
    	Color.rgb(255, 255, 255)
    };        
    
    public static int[] COLOR_BRIGHT = COLOR_BRIGHT_DAY;    
    
    public static final int[] BACKGROUND_COLOR_BRIGHT_DAY = new int[] {
    	Color.rgb(255, 255, 250),
    	Color.rgb(255, 0, 0),
    	Color.rgb(0, 255, 0),
    	Color.rgb(255, 255, 0),        
    	Color.rgb(0, 0, 255),
    	Color.rgb(255, 0, 255),
    	Color.rgb(0, 255, 255),    	
    	Color.rgb(0, 0, 0)
    };
    
    public static final int[] BACKGROUND_COLOR_BRIGHT_NIGHT = new int[] {
    	Color.rgb(0, 0, 0),
    	Color.rgb(255, 0, 0),
    	Color.rgb(0, 255, 0),
    	Color.rgb(255, 255, 0),        
    	Color.rgb(0, 0, 255),
    	Color.rgb(255, 0, 255),
    	Color.rgb(0, 255, 255),
    	Color.rgb(192, 192, 192)
    };    
    
    public static int[] BACKGROUND_COLOR_BRIGHT = BACKGROUND_COLOR_BRIGHT_DAY;    
    
    /**
     * Default private constructor to prevent instantiation.
     */
    private SgrColor() {


	}

}

