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
 * A very simple outputter, for two player games
 */

public class TwoPlayerOutput extends GameOutput
{

    public void writeGame(PrintWriter out, Game g)
	throws Exception
    {
	if(g.getNumPlayers()!=2)
	    throw new Exception("Unsupported Game Type for Output!");

	out.println("// " + GAMER_STRING + "\n");

	out.println(commentString(g.getDescription(), "// "));

	int n=g.getNumActions(0);
	int m=g.getNumActions(1);

	int[] o=new int[2];

	for(int i=1; i<=n; i++)
	    {
		o[0]=i;
		for(int j=1; j<=m; j++)
		    {
			o[1]=j;
			out.print("(" + g.getOutputPayoff(o,0) + ", " + 
				    g.getOutputPayoff(o,1) + ")");
			if(j!=m)
			    out.print("\t");
		    }
		out.println("");
	    }
    }
}
