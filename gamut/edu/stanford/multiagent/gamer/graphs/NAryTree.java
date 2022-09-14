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
 * Generates an N-ary tree with a given N and a given depth.
 *
 */

public class NAryTree extends ALGraph
{

    // ---------------------------------------------------
    // Parameters: The N-ary tree is specified by N and the
    // depth of the tree.
    //

    private static Parameters.ParamInfo pN;
    private static Parameters.ParamInfo pDepth;
    private static Parameters.ParamInfo[] treeParam;

    static {

	// Allow up to 5 children or a depth of 8 when the parameters are set
	// manually, although it is not advisable to set both of these params
	// to large values.  Scale down limits when parameters are randomized.

	pN = new Parameters.ParamInfo("n", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(5), "Number of children of every non-leaf node in the tree.");

	pDepth = new Parameters.ParamInfo("depth", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(8), "Depth of the tree.  Please note that it is advisable to use a very small value for at least one of n and depth parameters to avoid creating graphs too large for the games.");

	treeParam = new Parameters.ParamInfo[] {pN, pDepth};
	Global.registerParams(NAryTree.class, treeParam);
    }

    // ----------------------------------------------


    protected  String getGraphHelp()
    {
	return "NAryTree: Generates an n-ary tree with a given branching factor and a given depth.";
    }

    
    /**
     * Constructor.
     */
    public NAryTree()
	throws Exception
    {
	super();
    }



    
    /**
     * Calls graph initialize and also set up the nodes Vector
     * if it has not yet been set up
     */
    public void initialize()
	throws Exception 
    { 
	super.initialize();

	if (nodes.isEmpty()) {

	    long n = 0, depth = 0;

	    try {
		n = getLongParameter(pN.name);
		depth = getLongParameter(pDepth.name);
	    } catch (Exception e) {
		Global.handleError(e, "Could not get parameters " +
				   "to initialize NAryTree");
	    }

	    int numNodes = (int) getNumNodes(n, depth);
	    for(int i = 0; i < numNodes; i++)
		addNode();
	}
    }


    /**
     *  Reset the range of the number of nodes and randomize
     */
    public void randomizeParameters()
    {
	// Do not want to allow very large graphs when we
	// are randomizing since these are not always appropriate.

	if(!parameters.setByUser(pN.name)) {
	    pN.high = new Long(3);	    
	    parameters.randomizeParameter(pN.name);
	}

	if(!parameters.setByUser(pDepth.name)) {
	    pDepth.high = new Long(4);
	    parameters.randomizeParameter(pDepth.name);
	}
    }


    /**
     * Makes sure that the parameters are in the proper range
     */
    protected void checkParameters() throws Exception 
    {
	if (getLongParameter(pN.name) <= 0)
	    throw new Exception("n <= 0");

	if (getLongParameter(pDepth.name) <= 0)
	    throw new Exception("depth <= 0");
    }


    /**
     * It is always the case in N-Ary tree that for every edge
     * a to b there is also an edge b to a.
     */
    public boolean hasSymEdges() {
	return (true);
    }



    /**
     * It is never the case in N-ary trees that reflexive edges 
     * are allowed
     */
    public boolean reflexEdgesOk() {
	return (false);
    }



    /**
     * Generates the N-ary tree of the given depth
     */
    public void doGenerate() {

	long n = getLongParameter(pN.name);
	long depth = getLongParameter(pDepth.name);
	int numNodes = (int) getNumNodes(n, depth);

	int child = 1;
	int parent = 0;

	while (child < numNodes - 1) {
	    for (int i = 0; i < n; i++) {

		try {
		    // Note that edges are being added in both 
		    // directions (parent to child and also child to
		    // parent) at the moment.
		    addEdge(parent, child);
		    addEdge(child, parent);
		} catch (Exception e) {
		    Global.handleError(e, "Error adding edge to tree");
		}

		child++;
	    }
	    parent++;
	}
    }



    /**
     * Determines how many nodes need to be created to hold an
     * N-ary tree of the given depth
     */
    private long getNumNodes(long n, long depth) {

	long numNodes = 0;

	for (int i = 0; i < depth; i++)
	    numNodes += Math.pow(n, i);

	return numNodes;
    }

}


