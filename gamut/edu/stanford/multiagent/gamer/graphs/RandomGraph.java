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
 * Generates a random graph with the specified number of
 * nodes and edges.
 *
 * If the sym flag is set, then every time there is an edge 
 * from a to b, there will also be an edge from b to a, and the 
 * number of edges will really be twice the number specified.
 */

public class RandomGraph extends ALGraph
{

    // ---------------------------------------------------
    // Parameters: The random graph is parameterized on the 
    // numbers of nodes and edges as well as a flag that can
    // be set when it should be true that every time there is
    // an edge from a to b there is an edge from b to a.
    //

    private static Parameters.ParamInfo pNumEdges;
    private static Parameters.ParamInfo[] rgParam;

    static {

	pNumEdges = new Parameters.ParamInfo("edges", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(10000000000L), "If symmetric_edges is not set, the total number of directed edges in the random graph.  If symmetric_edges is set, the number of pairs of directed edges.");

	rgParam = new Parameters.ParamInfo[] {Graph.pNumNodes, pNumEdges,
					      Graph.pSymEdges, 
					      Graph.pReflexEdges};

	Global.registerParams(RandomGraph.class, rgParam);
    }

    // ----------------------------------------------



    public RandomGraph()
	throws Exception
    {
	super();
    }
  

    protected  String getGraphHelp()
    {
	return "RandomGraph: Generates a (uniformly) random graph according to G(n,m) model.";
    }

    
    /**
     * Calls graph initialize and also sets up the nodes Vector.
     * if it has not yet been set up
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
				   "initialize RandomGraph");
	    }
	    
	    for(int i = 0; i < numNodes; i++)
		addNode();
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
	    
	    if (getBooleanParameter(Graph.pReflexEdges.name)) {

		if (getLongParameter(pNumEdges.name) > 
		    (((getLongParameter(Graph.pNumNodes.name) - 1) *
		      getLongParameter(Graph.pNumNodes.name)) / 2) + 
		    getLongParameter(Graph.pNumNodes.name))
		    throw new Exception("edges is too large for nodes");

	    } else { 

		if (getLongParameter(pNumEdges.name) > 
		    ((getLongParameter(Graph.pNumNodes.name) - 1) *
		     getLongParameter(Graph.pNumNodes.name)) / 2)
		    throw new Exception("edges is too large for nodes");
	    
	    }

	} else {

	    if (getBooleanParameter(Graph.pReflexEdges.name)) {

		if (getLongParameter(pNumEdges.name) >
		    ((getLongParameter(Graph.pNumNodes.name) - 1) *
		     getLongParameter(Graph.pNumNodes.name)) +
		    getLongParameter(Graph.pNumNodes.name))
		    throw new Exception("edges is too large for nodes");

	    } else {

		if (getLongParameter(pNumEdges.name) >
		    ((getLongParameter(Graph.pNumNodes.name) - 1) *
		     getLongParameter(Graph.pNumNodes.name)))
		    throw new Exception("edges is too large for nodes");
	   
	    }
	} 
    }


    /**
     * Returns true if it must be the case that for every edge
     * a to b there is also an edge b to a
     */
    public boolean hasSymEdges() {
	return (getBooleanParameter(Graph.pSymEdges.name));
    }



    /**
     * Returns true if reflexive edges are allowed.
     */
    public boolean reflexEdgesOk() {
	return (getBooleanParameter(Graph.pReflexEdges.name));
    }


    /**
     * Resets range of parameters and randomizes.
     */
    public void randomizeParameters()
    {
	parameters.randomizeParameter(Graph.pSymEdges.name);
	parameters.randomizeParameter(Graph.pReflexEdges.name);

	if(!parameters.setByUser(Graph.pNumNodes.name)) {

	    if (parameters.setByUser(pNumEdges.name)) {
		long edges = getLongParameter(pNumEdges.name);
		if(getBooleanParameter(Graph.pSymEdges.name))
		    edges*=2;
		if(getBooleanParameter(Graph.pReflexEdges.name))
		    Graph.pNumNodes.low = new Long(Math.round(Math.ceil(Math.sqrt(edges))));
		else {
		    double x = Math.sqrt(1+4*edges);
		    x=(1+x)/2;
		    Graph.pNumNodes.low = new Long(Math.round(Math.ceil(x)));
		}
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
	
	    if(getBooleanParameter(Graph.pReflexEdges.name))
		high = high + nodes;
	
	    pNumEdges.high=new Long(high);
	    parameters.randomizeParameter(pNumEdges.name);
	    return;
	}
    }


    /**
     * Generates a random graph with given parameters.
     */
    public void doGenerate() {

	long numNodes = getLongParameter(Graph.pNumNodes.name);
	long numEdges = getLongParameter(pNumEdges.name);

	long currEdges = 0;
	
	if (hasSymEdges()) {
	    
	    // Add numEdges pairs of edges in both directions
	    // between nodes randomly 
	    while (currEdges < numEdges) {
		int i = (int) Global.randomLong(0, numNodes-1);
		int j = (int) Global.randomLong(0, numNodes-1);

		if (!areNeighbours(i, j)) {
		    if ((i != j) || (reflexEdgesOk())) {
			addEdge(i, j);
			if (i != j) addEdge(j, i);
			currEdges++;
		    }
		}
	    }

	} else {

	    // Add numEdges undirected edges randomly
	    while (currEdges < numEdges) {
		int i = (int) Global.randomLong(0, numNodes-1);
		int j = (int) Global.randomLong(0, numNodes-1);
		
		if (!areNeighbours(i, j)) {
		    if ((i != j) || (reflexEdgesOk())) {
			addEdge(i, j);
			currEdges++;
		    }
		}
	    }   
	}
    } 


}


