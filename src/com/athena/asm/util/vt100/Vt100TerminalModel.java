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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.widget.TextView;

import com.athena.asm.aSMApplication;



/**
 * A VT100/ANSI-compatible terminal model.
 * @author Graham Edgecombe
 */
public class Vt100TerminalModel {		
	
	public static final int CMD_BACKGROUND_COLOR = 1;
	public static final int CMD_FOREGROUND_COLOR = 2;
	public static final int CMD_UNDERLINE = 3;
//	public static final int CMD_STRIKEOUT = 4;
	public static final int CMD_INVERSE = 5;
	
	private static String Str_Control = "" + (char)(27) + "[";
	
	public List<Map<String, Integer>> liCommand = new ArrayList<Map<String, Integer>>(4);
	public boolean bSetCommand = true;

	/**
	 * A {@link AnsiControlSequenceListener} which modifies the
	 * {@link TerminalModel} appropriately when an event happens.
	 * @author Graham Edgecombe
	 */
	private class Vt100Listener implements AnsiControlSequenceListener {

		@Override
		public void parsedControlSequence(AnsiControlSequence seq) {
			char command = seq.getCommand();
			String[] parameters = seq.getParameters();

			switch (command) {
            case 'm':
                if (parameters.length == 0) {
                    parameters = new String[] { "0" };
                }
                for (String parameter : parameters) {
                    if (parameter.equals("0")) { // reset color configuration to default
                        foregroundColor = DEFAULT_FOREGROUND_COLOR;
                        backgroundColor = DEFAULT_BACKGROUND_COLOR;
                        backgroundBold = DEFAULT_BACKGROUND_BOLD;
                        foregroundBold = DEFAULT_FOREGROUND_BOLD;
                        underlineFlag = DEFAULT_UNDERLINE_FLAG;
                        inverseFlag = DEFAULT_INVERSE_FLAG;
//                        strikeoutFlag = DEFAULT_STRIKEOUT_FLAG;
                    } else if (parameter.equals("1") || parameter.equals("2")) {
                        backgroundBold = true;
                        foregroundBold = true;
                    } else if (parameter.equals("4")) {
                        underlineFlag = true;
                    } else if (parameter.equals("7")) {
                        inverseFlag = true;
                    } /*else if (parameter.equals("9")) {
                    	strikeoutFlag = true;
                    } */else if (parameter.equals("22")) {
                        backgroundBold = false;
                        foregroundBold = false;
                    } else if ((parameter.startsWith("3") || parameter.startsWith("4")) && parameter.length() == 2) {
                        int color = Integer.parseInt(parameter.substring(1));
                        if (parameter.startsWith("3")) {
                            foregroundColor = color;
                        } else if (parameter.startsWith("4")) {
                            backgroundColor = color;
                        }
                    }                    
                }
                break;
            case '#': // custom foreground color control
                if (1 != parameters.length) {
                    break;
                }                
                String param = parameters[0];
                foregroundColor = Color.rgb(
                		Integer.valueOf(param.substring(0, 3)), 
                		Integer.valueOf(param.substring(3, 6)), 
                		Integer.valueOf(param.substring(6, 9)));
                break;                
			}
		}

		@Override
		public String parsedString(String str, int nStart, List<Map<String, Integer>> liCommand,
				boolean bSetCommand) {
		    StringBuilder sb = new StringBuilder("");
			for (char ch : str.toCharArray()) {
				switch (ch) {
				case 127:
					continue;
				case 7:
					continue;
				}				
				sb.append((char)ch);
			}
			String strText = sb.toString();
			int nLen = strText.length();
			if(0 == nLen) {
				return strText;
			}
			
			if(!bSetCommand) {
				return strText;
			}
			
            int back = backgroundBold ? SgrColor.BACKGROUND_COLOR_BRIGHT[backgroundColor] : SgrColor.COLOR_NORMAL[backgroundColor];
            int fore = 10 > Math.abs(foregroundColor) ? (foregroundBold ? SgrColor.COLOR_BRIGHT[foregroundColor] : SgrColor.COLOR_NORMAL[foregroundColor])
            		: foregroundColor;
            if(backgroundColor == foregroundColor && foregroundBold) {
            	back = SgrColor.COLOR_NORMAL[backgroundColor];
            }
            
            int start = nStart - nLen + 1;
            if(0 > start) {
            	start = 0;
            }
            int end = nStart + 1;
            if(backgroundBold || backgroundColor != DEFAULT_BACKGROUND_COLOR) {
            	Map<String, Integer> mapCmd = new HashMap<String, Integer>(4);
            	mapCmd.put("c", CMD_BACKGROUND_COLOR);
            	mapCmd.put("s", start);
            	mapCmd.put("e", end);
            	mapCmd.put("v", back);
            	liCommand.add(mapCmd);
            }
            
            if(foregroundBold || foregroundColor != DEFAULT_FOREGROUND_COLOR) {
            	Map<String, Integer> mapCmd = new HashMap<String, Integer>(4);
            	mapCmd.put("c", CMD_FOREGROUND_COLOR);
            	mapCmd.put("s", start);
            	mapCmd.put("e", end);
            	mapCmd.put("v", fore);
            	liCommand.add(mapCmd);
            }
            
            if(underlineFlag) {
            	Map<String, Integer> mapCmd = new HashMap<String, Integer>(3);
            	mapCmd.put("c", CMD_UNDERLINE);
            	mapCmd.put("s", start);
            	mapCmd.put("e", end);
            	liCommand.add(mapCmd);
            }
            
            if(inverseFlag) {
            	Map<String, Integer> mapCmd = new HashMap<String, Integer>(5);
            	mapCmd.put("c", CMD_INVERSE);
            	mapCmd.put("s", start);
            	mapCmd.put("e", end);
            	mapCmd.put("v1", back);
            	mapCmd.put("v2", fore);
            	liCommand.add(mapCmd);
            }            
            
            /*if(strikeoutFlag) {
            	Map<String, Integer> mapCmd = new HashMap<String, Integer>(3);
            	mapCmd.put("c", CMD_STRIKEOUT);
            	mapCmd.put("s", start);
            	mapCmd.put("e", end);
            	liCommand.add(mapCmd);
            }*/
            
            return strText;
		}

	}

	/**
	 * The default foreground bold flag.
	 */
	private static final boolean DEFAULT_FOREGROUND_BOLD = false;

	/**
	 * The default background bold flag.
	 */
	private static final boolean DEFAULT_BACKGROUND_BOLD = false;
	
	/**
	 * The default underline flag.
	 */
	private static final boolean DEFAULT_UNDERLINE_FLAG = false;
	
	/**
	 * The default inverse flag.
	 */
	private static final boolean DEFAULT_INVERSE_FLAG = false;	
	
	/**
	 * The default strikeout flag.
	 */
//	private static final boolean DEFAULT_STRIKEOUT_FLAG = false;	

	/**
	 * The default foreground color.
	 */
	private static final int DEFAULT_FOREGROUND_COLOR = 7;

	/**
	 * The default background color.
	 */
	private static final int DEFAULT_BACKGROUND_COLOR = 0;

	/**
	 * The ANSI control sequence listener.
	 */
	private final AnsiControlSequenceListener listener = this.new Vt100Listener();

	/**
	 * The ANSI control sequence parser.
	 */
	private final AnsiControlSequenceParser parser = new AnsiControlSequenceParser(listener);

	/**
	 * The current foreground bold flag.
	 */
	private boolean foregroundBold = DEFAULT_FOREGROUND_BOLD;

	/**
	 * The current background bold flag.
	 */
	private boolean backgroundBold = DEFAULT_BACKGROUND_BOLD;	

	/**
	 * The current foreground color.
	 */
	private int foregroundColor = DEFAULT_FOREGROUND_COLOR;

	/**
	 * The current background color.
	 */
	private int backgroundColor = DEFAULT_BACKGROUND_COLOR;
	
	/**
	 * The current underline flag.
	 */
	private boolean underlineFlag = DEFAULT_UNDERLINE_FLAG;
	
	/**
	 * The current inverse flag.
	 */
	private boolean inverseFlag = DEFAULT_INVERSE_FLAG;	
	
	/**
	 * The current strikeout flag.
	 */
//	private boolean strikeoutFlag = DEFAULT_STRIKEOUT_FLAG;	

	/**
	 * Creates the terminal model with the default number of columns and rows,
	 * and the default buffer size.
	 */
	public Vt100TerminalModel() {
		if (aSMApplication.getCurrentApplication().isNightTheme()) {
			SgrColor.COLOR_NORMAL = SgrColor.COLOR_NORMAL_NIGHT;
			SgrColor.COLOR_BRIGHT = SgrColor.COLOR_BRIGHT_NIGHT;
			SgrColor.BACKGROUND_COLOR_BRIGHT = SgrColor.BACKGROUND_COLOR_BRIGHT_NIGHT;
		} else {
			SgrColor.COLOR_NORMAL = SgrColor.COLOR_NORMAL_DAY;
			SgrColor.COLOR_BRIGHT = SgrColor.COLOR_BRIGHT_DAY;
			SgrColor.BACKGROUND_COLOR_BRIGHT = SgrColor.BACKGROUND_COLOR_BRIGHT_DAY;
		}		
	}

	public String print(String str) {
		if (str == null) {
			return null;
		}
		return parser.parse(str, liCommand, bSetCommand);
	}
	
	private static void handleCommands(TextView textView, String source, List<Map<String, Integer>> liCommand) {						
		SpannableString strSpan = new SpannableString(source);        
		for(Map<String, Integer> mapCmd : liCommand) {
			int cmd = mapCmd.get("c");
			int start = mapCmd.get("s");
			int end = mapCmd.get("e");
			if(CMD_BACKGROUND_COLOR == cmd) {
				strSpan.setSpan(new BackgroundColorSpan(mapCmd.get("v")), start, end,
		                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			} else if(CMD_FOREGROUND_COLOR == cmd) {
				strSpan.setSpan(new ForegroundColorSpan(mapCmd.get("v")), start, end,
		                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);        				
			} else if(CMD_UNDERLINE == cmd) {
				strSpan.setSpan(new UnderlineSpan(), start, end,
		                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);        				
			} else if(CMD_INVERSE == cmd) {
				strSpan.setSpan(new ForegroundColorSpan(mapCmd.get("v1")), start, end,
		                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				strSpan.setSpan(new BackgroundColorSpan(mapCmd.get("v2")), start, end,
		                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			} /*else if(CMD_STRIKEOUT == cmd) {
				strSpan.setSpan(new StrikethroughSpan(), start, end,
		                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);        				
			} */
		}
		textView.setText(strSpan);        
	}	
	
	public static void handleContent(String strRawContentOri, TextView textView) {
		
		if(null == strRawContentOri) {
			textView.setText("");
			return;
		}
		
//		Log.d("tag", "11111111");
		boolean bHasControl = true;
        Vt100TerminalModel vt100 = new Vt100TerminalModel();
        
		String strRawContentNew = strRawContentOri.replace("\\r[", Str_Control);
        if(strRawContentOri.length() == strRawContentNew.length()) {
        	bHasControl = false;
        }		
        String strRawContent = strRawContentNew.replace("\\r", "\r");
//        Log.d("tag", "22222222222");
        
		strRawContent = vt100.print(strRawContent);
//		Log.d("tag", "333333333");		
		
        if(bHasControl) {        	        	    		        	        	    		    	
    		handleCommands(textView, strRawContent, vt100.liCommand);
//    		Log.d("tag", "44444444444");    		
        } else {
        	textView.setText(strRawContent);                    
        }		
	}

	public int getDefaultBackgroundColor() {
		final int bg = DEFAULT_BACKGROUND_COLOR;
		return DEFAULT_BACKGROUND_BOLD ? SgrColor.COLOR_BRIGHT[bg] : SgrColor.COLOR_NORMAL[bg];
	}

	public int getDefaultForegroundColor() {
		final int fg = DEFAULT_FOREGROUND_COLOR;
		return DEFAULT_FOREGROUND_BOLD ? SgrColor.COLOR_BRIGHT[fg] : SgrColor.COLOR_NORMAL[fg];
	}
			
}

