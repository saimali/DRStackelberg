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
 * Outputs a game in the Gambit .nfg file format.
 *
 * The format works with Gambit version 0.97.0.3 which is the
 * current version.  Could eventually need to be updated to work 
 * with later versions of gambit if the file format is modified.
 */

public class GambitOutput extends GameOutput
{

    public void writeGame(PrintWriter out, Game g)
      throws Exception
    {
	
	// The first line of every .nfg file starts with NFG 1
	out.print("NFG 1 ");

	// R for rational (I think?).  If we allow games to have
	// decimal payoffs, we will need to change this to D.
	boolean intFlag = Global.params.getBooleanParameter(Game.intPayoffs.name);

	if(intFlag)
	  out.print("R ");
	else
	  out.print("D ");

	// Next comes the name of the game, which will be the
	// name of the class used to generate plus a generated
	// by blurb, for now
	
	//String gName = g.getName() + ", " + GAMER_STRING;

	String gName = GAMER_STRING + "\n" + 
	  g.getDescription();
	
	out.print("\"" + gName + "\" ");


	// Next print the players in a format like
	//       { "Player1", "Player2", "Player3" }
	out.print("{ ");
	for (int i = 1; i <= g.getNumPlayers(); i++)
	    out.print("\"Player" + i + "\" ");
	out.print("} ");

	// Still on the first line, print out the number of 
	// actions for each player in a format like
	//       {2, 3, 4}
	out.print("{ ");
	for(int i=0; i<g.getNumPlayers(); i++)
	    out.print(g.getNumActions(i) + " "); 
	out.print("} ");

	// That's it for the first line..
	out.println();
	out.println();

	// Now print the payoffs in a row
	Outcome o = new Outcome(g.getNumPlayers(), g.getNumActions());
	
	for(o.reset(); o.hasMoreOutcomes(); o.nextOutcome())
	    for(int i=0; i<g.getNumPlayers(); i++)
		out.print(g.getOutputPayoff(o.getOutcome(), i) + " ");

	out.println();
	
    }
}
