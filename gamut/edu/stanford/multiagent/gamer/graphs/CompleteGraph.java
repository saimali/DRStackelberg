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
 * Generates a complete graph with the specified number of
 * nodes.
 */

public class CompleteGraph extends ALGraph
{

    // ---------------------------------------------------
    // Parameters: The random graph is parameterized on the 
    // numbers of nodes and a flag telling whether or not
    // reflexive edges should be included.
    //    

    private static Parameters.ParamInfo[] cgParam;

    static {

	cgParam = new Parameters.ParamInfo[] {Graph.pNumNodes, Graph.pReflexEdges};
	Global.registerParams(CompleteGraph.class, cgParam);
    }

    // ----------------------------------------------

    protected  String getGraphHelp()
    {
	return "CompleteGraph: Generates a complete graph with a specified number of nodes.";
    }

    /**
     * Constructor to be used when parameters are coming from
     * the command line.  The nodes vector will then be set up in
     * initialize rather than here.
     */
    public CompleteGraph()
	throws Exception
    {
	super();
    }


    /**
     * Constructor to be used when parameters are being set
     * by the game creating the graph
     */
    public CompleteGraph(long numNodes, boolean reflexive)
	throws Exception
    {
	super((int) numNodes);

	try {

	    Long numNodesLong = new Long(numNodes);
	    parameters.setParameter(Graph.pNumNodes.name, numNodesLong);
	    
	    Boolean reflexBool = new Boolean(reflexive);
	    parameters.setParameter(Graph.pReflexEdges.name, reflexBool);

	} catch (Exception e) {
	    System.out.println("Unable to set graph parameters\n");
	}
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

	    int numNodes = 0;

	    try {
		numNodes = (int) getLongParameter(Graph.pNumNodes.name);
	    } catch (Exception e) {
		Global.handleError(e, "Could not get parameters " +
				   "to initialize CompleteGraph");
	    }

	    for(int i = 0; i < numNodes; i++)
		addNode();
	}
    }


    /**
     * Resets the range of the number of nodes and randomize
     */
    public void randomizeParameters()
    {
	if(!parameters.setByUser(pReflexEdges.name))
	    parameters.randomizeParameter(pReflexEdges.name);

	if(!parameters.setByUser(Graph.pNumNodes.name)) {
	    // Do not want to allow very large graphs when we
	    // are randomizing since these are not always appropriate.
	    Graph.pNumNodes.high = new Long(20);	    
	    parameters.randomizeParameter(pNumNodes.name);
	}
    }


    /**
     * Makes sure that the parameters are in the proper range
     */
    protected void checkParameters() throws Exception 
    {
	if (getLongParameter(Graph.pNumNodes.name) <= 0)
	    throw new Exception("nodes <= 0");
    }


    /**
     * It is always the case in complete graphs that for every edge
     * a to b there is also an edge b to a
     */
    public boolean hasSymEdges() {
	return (true);
    }


    /**
     * Return true if reflexive edges are allowed
     */
    public boolean reflexEdgesOk() {
	return (getBooleanParameter(Graph.pReflexEdges.name));
    }



    /**
     * Generate a random graph with given parameters.
     */
    public void doGenerate() {

	long numNodes = getLongParameter(pNumNodes.name);
	long currEdges = 0;
	
	// Add edges between every pair of points in both directions.
	// Only add the egde from a node to itself if the reflexive
	// flag is set.
	for (int i = 0; i < numNodes; i++)
	    for (int j = 0; j < numNodes; j++)
		if ((reflexEdgesOk()) || (i != j))
		    addEdge(i, j);
    }

}


