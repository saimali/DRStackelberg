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

package edu.stanford.multiagent.gamer.graphs;

import java.util.*;
import edu.stanford.multiagent.gamer.*;

/**
 * Generetes a power-law out-degree graph using Barabasi-Albert model.
 * Gives exponent of -3.
 */

public class BAGraph extends ALGraph
{

    // ---------------------------------------------------
    // Parameters: 
    // m0 - starting number of vertices
    // m>=m0 - additional vertices
    // t number of time steps
    //

    private static Parameters.ParamInfo pStartNodes;
    private static Parameters.ParamInfo pAddEdges;
    private static Parameters.ParamInfo pTime;

    private static Parameters.ParamInfo[] baParam;

    static {

	pStartNodes = new Parameters.ParamInfo("m0", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(100), "Number of nodes to start with.  Defaults to 5.", false, new Long(5));

	pAddEdges = new Parameters.ParamInfo("m", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(100), "Number of edges to add to each new node <=m0.", false, new Long(1));

	pTime = new Parameters.ParamInfo("t", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(1000000), "The total number of time steps.");

	baParam = new Parameters.ParamInfo[] {pStartNodes, pAddEdges, pTime};

	Global.registerParams(BAGraph.class, baParam);
    }

    // ----------------------------------------------



    public BAGraph()
	throws Exception
    {
	super();
    }



    protected  String getGraphHelp()
    {
	return "BAGraph: Generates a power-law out-degree graph using Barabasi-Albert model. Resulting power-law exponent is around -3.";
    }

    
    /**
     * Calls graph initialize 
     */
    public void initialize()
	throws Exception 
    {
	super.initialize();
    }



    /**
     * Makes sure that the parameters are in the proper range.
     */
    protected void checkParameters() throws Exception 
    {

	if(getLongParameter(pAddEdges.name) > getLongParameter(pStartNodes.name))
	    throw new Exception("m must be <= m0");
    }


    /**
     * Returns true if it must be the case that for every edge
     * a to b there is also an edge b to a.
     */
    public boolean hasSymEdges() {
	return true;
    }



    /**
     * Returns true if reflexive edges are allowed.
     */
    public boolean reflexEdgesOk() {
	return false;
    }



    /**
     * Generates a random graph with given parameters.
     */
    public void doGenerate() 
    {
    
	int m0=(int)getLongParameter(pStartNodes.name);
	int m=(int)getLongParameter(pAddEdges.name);
	int t=(int)getLongParameter(pTime.name);

	// -- initialize (cumulative) probabilities
	int[] degrees=new int[m0+t];
	int[] cumul=new int[m0+t];

	for(int i=0; i<degrees.length; i++)
	    degrees[i]=1;

	// -- add initial nodes
	for(int i=0; i<m0; i++)
	    addNode();

	// -- Now start adding nodes
	for(int currNode=m0; currNode<degrees.length; currNode++)
	    {
		addNode();

		// -- generate cumul distribution
		cumul[0]=degrees[0];
		for(int i=1; i<currNode; i++)
		    cumul[i]=cumul[i-1]+degrees[i];

		// -- now add to it m edges
		for(int j=0; j<m; j++)
		    {
			int r=Global.randomInt(1, cumul[currNode-1]);
			int n;
			for(n=0; n<currNode; n++)
			    if(r<=cumul[n])
				break;
	    
			addEdge(currNode, n); addEdge(n, currNode);
			degrees[n]++; degrees[currNode]++;
		    }
	    }
    }
  

}


