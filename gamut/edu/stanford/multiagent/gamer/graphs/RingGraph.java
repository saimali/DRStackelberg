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
 * Generates a ring structured graph with the specified number 
 * of nodes.
 */

public class RingGraph extends ALGraph
{

    // ---------------------------------------------------
    // Parameters: The ring structured graph is parameterized by
    // the number of inner nodes and the number of outernodes.
    //

    private static Parameters.ParamInfo pInnerNodes;
    private static Parameters.ParamInfo pOuterNodes;
    private static Parameters.ParamInfo[] rgParam;

    static {
	// arbitrary choice for the max number of nodes in inner and
	// outer circles.. just don't want these to be too big or
	// the graph will blow up in size

	pInnerNodes = new Parameters.ParamInfo("inner_nodes", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(50), "Number of nodes in the inner circle of the ring graph.  May be set up to 50 by hand, but when randomized will be set to something no larger than 6 since many games cannot handle large graphs.");

	pOuterNodes = new Parameters.ParamInfo("outer_nodes", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(50), "Number of nodes in each of the outer circles of the ring graph.  May be set up to 50 by hand, but when randomized will be set to something no larger than 6 since many games cannot handle large graphs.");

	rgParam = new Parameters.ParamInfo[] {pInnerNodes, pOuterNodes};
	Global.registerParams(RingGraph.class, rgParam);
    }

    // ----------------------------------------------


    /**
     * Constructor
     */
    public RingGraph()
	throws Exception
    {
	super();
    }


    protected  String getGraphHelp()
    {
	return "RingGraph: Generates a ring-of-ring graphs. Consists of a central ring of nodes, each of which participates in a separate outer ring of nodes.";
    }


    /**
     * Calls graph initialize and also sets up the nodes Vector.
     */
    public void initialize()
	throws Exception 
    {
	super.initialize();

	if (nodes.isEmpty()) {

	    long innerNodes = 0, outerNodes = 0;

	    try {
		innerNodes = getLongParameter(pInnerNodes.name);
		outerNodes = getLongParameter(pOuterNodes.name);
	    } catch (Exception e) {
		Global.handleError(e, "Could not get parameters to " +
				   "initialize RingGraph");
	    }
	    
	    int numNodes = (int) (innerNodes * outerNodes);

	    for(int i = 0; i < numNodes; i++)
		addNode();
	}
    }


    /**
     * Makes sure that the parameters are in the proper range
     */
    protected void checkParameters() throws Exception 
    {
	if (getLongParameter(pInnerNodes.name) <= 0)
	    throw new Exception("inner_nodes <= 0");
	
	if (getLongParameter(pOuterNodes.name) <= 0)
	    throw new Exception("outer_nodes <= 0");
    }



    /**
     * Resets the range of the number of nodes and randomize
     */
    public void randomizeParameters()
    {
	if(!parameters.setByUser(pInnerNodes.name)) {
	    pInnerNodes.high = new Long(6);	    
	    parameters.randomizeParameter(pInnerNodes.name);
	}

	if(!parameters.setByUser(pOuterNodes.name)) {
	    pOuterNodes.high = new Long(6);
	    parameters.randomizeParameter(pOuterNodes.name);
	}
    }



    /**
     * It is always the case in ring graphs that for every edge
     * a to b there is also an edge b to a.
     */
    public boolean hasSymEdges() {
	return (true);
    }



    /**
     * It is never the case in ring graphs that reflexive edges 
     * are allowed.
     */
    public boolean reflexEdgesOk() {
	return (false);
    }



    /**
     * Generates a ring structured graph getting the number
     * of nodes from the parameters
     */
    public void doGenerate() {

	int innerNodes = (int) getLongParameter(pInnerNodes.name);
	int outerNodes = (int) getLongParameter(pOuterNodes.name);

	for (int i = 0; i < innerNodes; i++) {
	    
	    // Add two edges on the graph between the current 
	    // inner node and the next inner node
	    if (i < innerNodes - 1) {
		try {
		    addEdge(i * outerNodes, (i+1) * outerNodes);
		    addEdge((i+1) * outerNodes, i * outerNodes);
		} catch (Exception e) {
		    Global.handleError(e, "Error adding edge");
		}
	    } else {
		try {
		    addEdge(i * outerNodes, 0);
		    addEdge(0, i * outerNodes);
		} catch (Exception e) {
		    Global.handleError(e, "Error adding edge");
		}
	    }

	    // Also add a loop of size outerNodes starting with
	    // the node (i * outerNodes)

	    for (int j = (i * outerNodes);
		 j < ((i + 1) * outerNodes); j++) {

		if (j < ((i + 1) * outerNodes) - 1) {
		    try {
			addEdge(j, j + 1);
			addEdge(j + 1, j);
		    } catch (Exception e) {
			Global.handleError(e, "Error adding edge");
		    }
		} else {
		    try {
			addEdge(j, i * outerNodes);
			addEdge(i * outerNodes, j);
		    } catch (Exception e) {
			Global.handleError(e, "Error adding edge");
		    }
		}
	    }
	}
    }

}


