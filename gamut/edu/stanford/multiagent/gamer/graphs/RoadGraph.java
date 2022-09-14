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
 * Generates a road graph with the specified number 
 * of nodes.
 */

public class RoadGraph extends ALGraph
{

    // ---------------------------------------------------
    // Parameters: The road graph is specified only by the
    // number of nodes.
    //

    private static Parameters.ParamInfo[] rgParam;

    static {

	rgParam = new Parameters.ParamInfo[] {Graph.pNumNodes};
	Global.registerParams(RoadGraph.class, rgParam);
    }

    // ----------------------------------------------


    /**
     * Constructor
     */
    public RoadGraph()
	throws Exception
    {
	super();
    }

    protected  String getGraphHelp()
    {
	return "RoadGraph: Generates a road graph: consists of a two sets of n nodes each connected in a line, with additional n edges connecting corresponding nodes in two sets.";
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
		Global.handleError(e, "Could not get parameter " +
				   "to initialize RoadGraph");
	    }

	    for(int i = 0; i < numNodes; i++)
		addNode();
	}
    }


    /**
     * Resets the range of the number of nodes and randomize.
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
     * Makes sure that the parameters are in the proper ranges.
     */
    protected void checkParameters() throws Exception 
    {
	if (getLongParameter(Graph.pNumNodes.name) <= 0)
	    throw new Exception("nodes <= 0");
    }


    /**
     * It is always the case in road graphs that for every edge
     * a to b there is also an edge b to a.
     */
    public boolean hasSymEdges() {
	return (true);
    }


    /**
     * It is never the case in road graphs that reflexive edges 
     * are allowed.
     */
    public boolean reflexEdgesOk() {
	return (false);
    }


    /**
     * Generates a road structured graph getting the number
     * of nodes from the parameters.
     */
    public void doGenerate() {

	long numNodes = getLongParameter(Graph.pNumNodes.name);
	
	// Now add edges in the graph in the appropriate places.
	// For all node i, add edges to i-2 and i+2.  For even
	// numbered nods, also add i+1 and for odd numbered
	// nodes also add i-1, unless of course some of these 
	// nodes do not exist.
	for (int i = 0; i < numNodes; i++) {

	    if (i-2 >= 0) {
		try {
		    addEdge(i, i-2);
		} catch (Exception e) {
		    Global.handleError(e, "Error adding edge");
		}
	    }
	    
	    if (i+2 < numNodes) {
		try {
		    addEdge(i, i+2);
		} catch (Exception e) {
		    Global.handleError(e, "Error adding edge");
		}
	    }

	    if (i%2 == 0) {

		// For nodes on the even side of the road
		if (i + 1 < numNodes) {
		    try {
			addEdge(i, i+1);
		    } catch (Exception e) {
			Global.handleError(e, "Error adding edge");
		    }
		}

	    } else {

		// For nodes on the odd side of the road
		try {
		    addEdge(i, i-1);
		} catch (Exception e) {
		    Global.handleError(e, "Error adding edge");
		}
	    }
	}
    }

}


