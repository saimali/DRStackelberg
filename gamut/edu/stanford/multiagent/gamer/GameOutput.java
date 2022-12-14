/*
 * Copyright (C) 2004 Jennifer Wortman, Eugene Nudelman, Kevin Leyton-Brown, Yoav Shoham.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.stanford.multiagent.gamer;

import java.io.*;

/**
 * Super class for all game outputters.
 */

public abstract class GameOutput
{
    protected final String GAMER_STRING = "Generated by " + Global.VERSION_STRING;
    

    /**
     * Writes the game to the specified PrintWriter.  Must be implemented
     * by every subclass.
     *
     * @param out the PrintWriter to which the game should be written
     * @param g the Game
     *
     * @throws Exception if unable to write the game
     */
    public abstract void writeGame(PrintWriter out, Game g) throws Exception;
    
    
    /**
     * Properly adds and formats comments to the output in the comment
     * format specified.
     *
     * @param str the string to add comments to
     * @param comment the string to use as a comment marker at the 
     * beginning of a line
     */ 
    public static String commentString(String str, String comment)
    {
	return comment + str.replaceAll("\n", "\n" + comment);
    }
    

    /**
     * Converts an array of Strings into one long String in which
     * each element of the array is separated from the next by
     * the specified seperator.
     *
     * @param args the array of Strings to convert
     * @param sep the separator
     */
    public static String arrayToString(String[] args, String sep)
    {
	StringBuffer buff=new StringBuffer();
	for(int i=0; i<args.length; i++) {
	    buff.append(args[i]);
	    if (i!=args.length-1)
		buff.append(sep);
	}
	
	return buff.toString();
    }
}
