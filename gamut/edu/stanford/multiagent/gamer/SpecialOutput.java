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
 * This outputter uses game own output function
 */

public class SpecialOutput extends GameOutput
{

    public void writeGame(PrintWriter out, Game g)
    {
	out.println(commentString(GAMER_STRING, "# "));

	out.println(commentString(g.getDescription(), "# "));

	out.println("# Players: " + g.getNumPlayers());
	
	out.print("# Actions: [ ");
	for(int i=0; i<g.getNumPlayers(); i++)
	    out.print(g.getNumActions(i) + " ");
	out.println("]");

	try {
	    g.writeGame(out);
	} catch (Exception e) {
	    System.err.println("FATAL: Failed to write game!");
	    System.err.println(e.getMessage());
	    System.exit(1);
	}
    }
}
