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
 * Generates a star graph with n nodes total.
 */

public class StarGraph extends ALGraph
{

    // ---------------------------------------------------
    // Parameters: the StarGraph is parameterized only by the
    // total number of nodes.
    //

    private static Parameters.ParamInfo[] starParam;

    static {

	starParam = new Parameters.ParamInfo[] {Graph.pNumNodes};
	Global.registerParams(StarGraph.class, starParam);
    }

    // ----------------------------------------------


    public StarGraph()
	throws Exception
    {
	super();
    }


    protected  String getGraphHelp()
    {
	return "StarGraph: Generates a star graph: a single center node connected to all other nodes.";
    }


    /**
     * Initializes the graph with the correct number of nodes.
     */
    public void initialize()
	throws Exception 
    {
	super.initialize();

	int numNodes = 0;

	if (nodes.isEmpty()) {

	    try {
		numNodes = (int) getLongParameter(Graph.pNumNodes.name);
	    } catch (Exception e) {
		Global.handleError(e, "Could not get number of nodes.");
	    }

	    for(int i = 0; i < numNodes; i++)
		addNode();
	}
    }



    /**
     *  Resets the range of the number of nodes and randomize.
     */
    public void randomizeParameters()
    {
	if(!parameters.setByUser(Graph.pNumNodes.name)) {
	    // Do not want to allow very large graphs when we
	    // are randomizing since these are not always appropriate.
	    Graph.pNumNodes.high = new Long(20);	    
	    parameters.randomizeParameter(Graph.pNumNodes.name);
	}
    }


    /**
     * Makes sure that the parameters are in the proper range.
     */
    protected void checkParameters() throws Exception 
    {
	if (getLongParameter(Graph.pNumNodes.name) <= 0)
	    throw new Exception("nodes <= 0");
    }



    /**
     * It is always the case in star graphs that for every edge
     * a to b there is also an edge b to a.
     */
    public boolean hasSymEdges() {
	return (true);
    }



    /**
     * It is never the case in star graphs that reflexive edges 
     * are allowed
     */
    public boolean reflexEdgesOk() {
	return (false);
    }



    /**
     * Generate the Star graph
     */
    public void doGenerate() {

	int numNodes = (int) getLongParameter(Graph.pNumNodes.name);

	// Set node 0 to be the root.  Add edges from the root to
	// all other nodes and from all other nodes to the root.
	for (int i = 1; i < numNodes; i++) {
	    try {
		addEdge(0, i);
		addEdge(i, 0);
	    } catch (Exception e) {
		Global.handleError(e, "Error adding edge to tree.");
	    }
	}
    }
}


