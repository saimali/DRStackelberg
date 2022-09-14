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
 * Generates a random graph using the Watts-Strogatz model
 * Nature 1998.
 *
 * Taken from Albert and Barabasi Tutorial.
 */

public class SmallWorldGraph extends ALGraph
{
  
    // ---------------------------------------------------
    // Parameters: The random graph is parameterized on the 
    // numbers of nodes, paremer K indicating connectedness of the 
    // original ring lattice, and probability p of rewiring
    //
  
    private static Parameters.ParamInfo pKParam;
    private static Parameters.ParamInfo pRewireParam;
    private static Parameters.ParamInfo[] wsParam;

    static {

	pKParam = new Parameters.ParamInfo("K", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(10), "Each node will have 2K neighbours in the original ring lattice. Defaults to 2 or can be randomized.", false, new Long(2));

	pRewireParam = new Parameters.ParamInfo("p", Parameters.ParamInfo.DOUBLE_PARAM, new Double(0), new Double(1), "Probability of rewiring each edge", false, new Double(0.5));

	wsParam = new Parameters.ParamInfo[] {Graph.pNumNodes, pKParam, pRewireParam};

	Global.registerParams(SmallWorldGraph.class, wsParam);
    }

    // ----------------------------------------------



    public SmallWorldGraph()
	throws Exception
    {
	super();
    }

    protected  String getGraphHelp()
    {
	return "SmallWorldGraph: Generates a small-world graph according to the Watts-Strogatz model. Starts with a ring lattice of degree 2k, and then randomly rewires each edge with some probability.";
    }

    
    /**
     * Calls graph initialize and also sets up the nodes Vector.
     */
    public void initialize()
	throws Exception 
    {
	super.initialize();

	if (nodes.isEmpty()) {
	    int numNodes = 0;

	    try {
		numNodes = (int) getLongParameter(Graph.pNumNodes.name);
	    } catch (Exception e) {
		Global.handleError(e, "Could not get parameter to " +
				   "initialize SmallWorldGraph");
	    }
	    
	    for(int i = 0; i < numNodes; i++)
		addNode();
	}
    }



    /**
     * Resets parameter ranges and randomize.
     */
    public void randomizeParameters()
    {
	parameters.randomizeParameter(pRewireParam.name);
	
	if(!parameters.setByUser(Graph.pNumNodes.name)) {
	    // Do not want to allow very large graphs when we
	    // are randomizing since these are not always appropriate.
	    Graph.pNumNodes.high = new Long(20);	    
	    parameters.randomizeParameter(Graph.pNumNodes.name);
	}

	if(!parameters.setByUser(pKParam.name)) {
	    pKParam.high = new Long(Math.max(getLongParameter(Graph.pNumNodes.name)/2 - 1, 1));
	    parameters.randomizeParameter(pKParam.name);
	}
    }
    

    /**
     * Makes sure that the parameters are in the proper range.
     */
    protected void checkParameters() throws Exception 
    {

	if (getLongParameter(Graph.pNumNodes.name) <= 0)
	    Global.handleError("nodes <= 0");

	if (getLongParameter(pKParam.name) >= Math.max(getLongParameter(Graph.pNumNodes.name)/2, 2) )
	    Global.handleError("K must be < nodes /2");
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
    public void doGenerate() {

	int numNodes = (int)getLongParameter(Graph.pNumNodes.name);
	int K = (int)getLongParameter(pKParam.name);
	double p = getDoubleParameter(pRewireParam.name);

	// -- First set-up original ring
	// -- need to do that because rewiring prohibits duplication

	for(int i=1; i<=K; i++)
	    for(int n=0; n<numNodes; n++) {
		int t= (n + i) % numNodes;
		addEdge(n,t);
		addEdge(t,n);
	    }

	// -- now do rewiring
	for(int i=1; i<=K; i++)
	    for(int n=0; n<numNodes; n++) {
		double d = Global.randomDouble();

		if(getNumNeighbours(n)>=numNodes-1)
		  continue; // -- cannot rewire

		if(d<=p) {
		    // -- rewire
		  
		    int t;
		    do {
			t=Global.randomInt(1, numNodes - 1);
			t= (n+t) % numNodes;
		    }
		    while(areNeighbours(n, t));
		  
		    int s = (n+i) % numNodes;

		    removeEdge(n, s);
		    removeEdge(s, n);
		    addEdge(n, t);
		    addEdge(t, n);
		}
	    }
    } 


}


