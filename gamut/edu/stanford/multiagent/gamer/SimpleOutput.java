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
 * A very simple outputter, mostly for development purposes.
 */

public class SimpleOutput extends GameOutput
{

    public void writeGame(PrintWriter out, Game g)
	throws Exception
    {
	out.println(commentString(GAMER_STRING, "# "));

	out.println(commentString(g.getDescription(), "# "));

	Outcome o = new Outcome(g.getNumPlayers(), g.getNumActions());

	for(o.reset(); o.hasMoreOutcomes(); o.nextOutcome())
	    {
		out.print(o + " :\t[ ");

		for(int i=0; i<g.getNumPlayers(); i++)
		    out.print(g.getOutputPayoff(o.getOutcome(), i) + " ");
		out.println("]");
	    }

    }
}
