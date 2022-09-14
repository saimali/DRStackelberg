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
 * Generetes a power-law out-degree graph using PLOD algorithm by
 * Palmer and Steffan (Generating Network Topologies That Obey Power Laws).
 *
 * If symmetric flag is set to false, generates undirected version of this.
 */

public class PLODGraph extends ALGraph
{
    
    // ---------------------------------------------------
    // Parameters: The PLOD graph is parameterized on the 
    // numbers of nodes and edges as well as a flag that can
    // be set when it should be true that every time there is
    // an edge from a to b there is an edge from b to a.
    //

    private static Parameters.ParamInfo pNumEdges;
    private static Parameters.ParamInfo pAlpha;
    private static Parameters.ParamInfo pBeta;
    private static Parameters.ParamInfo[] plodParam;

    static {

	pNumEdges = new Parameters.ParamInfo("edges", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(10000), "The total number of directed/or undirected edges.");

	pAlpha = new Parameters.ParamInfo("alpha", Parameters.ParamInfo.DOUBLE_PARAM, new Double(1), new Double(5), "Alpha parameter (power) in the power law, defaults to 2.1.", false, new Double(2.1));

	pBeta = new Parameters.ParamInfo("beta", Parameters.ParamInfo.DOUBLE_PARAM, new Double(1), new Double(100000), "Beta parameter (multiplier) in the power law, defaults to 5.", false, new Double(5));

	plodParam = new Parameters.ParamInfo[] {Graph.pNumNodes, pNumEdges, pAlpha, pBeta,
						Graph.pSymEdges };

	Global.registerParams(PLODGraph.class, plodParam);
    }

    // ----------------------------------------------



    public PLODGraph()
	throws Exception
    {
	super();
    }


    protected  String getGraphHelp()
    {
	return "PLODGraph: Generates a power-law out-degree graph via PLOD algorithm of Palmer and Stefan.";
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
				   "initialize PLODGraph");
	    }
	    
	    for(int i = 0; i < numNodes; i++)
		addNode();
	}
    }


    /**
     * Resets the range of the number of nodes and randomizes.
     */
    public void randomizeParameters()
    {
	parameters.randomizeParameter(Graph.pSymEdges.name);

	if(!parameters.setByUser(Graph.pNumNodes.name)) {

	    if (parameters.setByUser(pNumEdges.name)) {
		long edges = getLongParameter(pNumEdges.name);
		if(getBooleanParameter(Graph.pSymEdges.name))
		    edges*=2;

		double x = Math.sqrt(1+4*edges);
		x=(1+x)/2;
		Graph.pNumNodes.low = new Long(Math.round(Math.ceil(x)));
	    }
	
	    Graph.pNumNodes.high = new Long(20);

	    if (((Long)Graph.pNumNodes.high).longValue() < 
		((Long)Graph.pNumNodes.low).longValue()) {
		Global.handleError("Cannot randomize parameter nodes because " +
				   "edges was set too high.  Try a smaller " +
				   "value of edges, or try randomizing both " +
				   "parameters together.");
	    }

	    parameters.randomizeParameter(Graph.pNumNodes.name);
	}

	if(!parameters.setByUser(pNumEdges.name)) {
	    long nodes=getLongParameter(Graph.pNumNodes.name);
	    long high = nodes*(nodes-1);
	
	    if(getBooleanParameter(Graph.pSymEdges.name))
		high = high/2;

	    pNumEdges.high=new Long(high);
	    parameters.randomizeParameter(pNumEdges.name);
	    return;
	}

    }


    /**
     * Makes sure that the parameters are in the proper range.
     */
    protected void checkParameters() throws Exception 
    {

	if (getLongParameter(Graph.pNumNodes.name) <= 0)
	    throw new Exception("nodes <= 0");

	if (getLongParameter(pNumEdges.name) <= 0)
	    throw new Exception("edges <= 0");

	if (getBooleanParameter(Graph.pSymEdges.name)) {

	    if (getLongParameter(pNumEdges.name) > 
		((getLongParameter(Graph.pNumNodes.name) - 1) *
		 getLongParameter(Graph.pNumNodes.name)) / 2)
		throw new Exception("edges is too large for nodes");
	    
	} else {

	    if (getLongParameter(pNumEdges.name) >
		((getLongParameter(Graph.pNumNodes.name) - 1) *
		 getLongParameter(Graph.pNumNodes.name)))
		throw new Exception("edges is too large for nodes");
	   
	} 
    }


    /**
     * Returns true if it must be the case that for every edge
     * a to b there is also an edge b to a.
     */
    public boolean hasSymEdges() {
	return (getBooleanParameter(Graph.pSymEdges.name));
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

      System.err.println("WARNING: Generating PLODGraph may take a very long time. Consider using BAGraph instead");

	int numNodes = (int)getLongParameter(pNumNodes.name);
	int numEdges = (int)getLongParameter(pNumEdges.name);
	double alpha = getDoubleParameter(pAlpha.name);
	double beta = getDoubleParameter(pBeta.name);
  
	boolean sym=hasSymEdges();


	// -- initialize degrees
	int[] degrees=new int[numNodes];
	int total=0;

	for(int i=0; i<degrees.length; i++) {
	    int x=Global.randomInt(1,numNodes);
	    degrees[i] = (int)Math.round(beta*Math.pow(x, -alpha));
	    total+=degrees[i];
	}
	
	// -- add edges
	int cond=(sym ? 2 : 1);

	for(int i=0; i<numEdges; i++) {
	    if(total<cond) {
		System.err.println("WARNING: Failed to generated requested number of edges in PLOD!");
		return;
	    }

	    while(true) {
		int s=Global.randomInt(0, numNodes-1);
		int t=Global.randomInt(0, numNodes-1);
	    
		boolean ok= (s!=t) && (degrees[s]>0) && !areNeighbours(s,t);
		if(sym)
		    ok = ok && degrees[t]>0;

		if ( ok ) {
		    addEdge(s,t); 
		    degrees[s]--;
		    if(sym) {
			addEdge(t,s);
			degrees[t]--; 
		    }
		    total-=cond;
		    break;
		}
	    }
	}

    }
  

}


