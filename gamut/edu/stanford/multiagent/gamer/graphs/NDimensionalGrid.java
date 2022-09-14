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
 * Generates a graph on an n-dimensional grid, i.e. 1D
 * is a line, 2D is a regular grid, and so on.  Currently
 * all dimensions are the same length to keep parameterization
 * from getting out of hand.
 */

public class NDimensionalGrid extends ALGraph
{

    // ---------------------------------------------------
    // Parameters: The N-dimensional grid is parameterized on
    // the number of dimensions and the size of a dimension.
    //    

    protected static Parameters.ParamInfo pN;
    protected static Parameters.ParamInfo pDimSize;
    protected static Parameters.ParamInfo[] ndgParam;

    static {

	pN = new Parameters.ParamInfo("num_dimensions", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(10), "Dimensions of the graph.  Must be > 0.  May be set up to 10, but when randomized will be no greater than 4 since graphs of higher dimensions will be too large for most games.");

	pDimSize = new Parameters.ParamInfo("dim_size", Parameters.ParamInfo.LONG_PARAM, new Long(1), new Long(20), "Size of a single dimension in the graph.  Must be > 0.  Can be set up to 20, but when randomized will be no greater than 4.");

	ndgParam = new Parameters.ParamInfo[] {pN, pDimSize};
	Global.registerParams(NDimensionalGrid.class, ndgParam);
    }

    // ----------------------------------------------

    protected  String getGraphHelp()
    {
	return "NDimensionalGrid: Generates an n-dimensional grid with a given number of points in each dimension. Each node is connected to its neighbors.";
    }


    public NDimensionalGrid()
	throws Exception
    {
	super();
    }




    /**
     * Calls graph initialize and also sets up the nodes Vector,
     */
    public void initialize()
	throws Exception 
    {
	super.initialize();

	if (nodes.isEmpty()) {

	    long dimensionSize = 0;
	    int dimensions = 0;

	    try {
		dimensionSize = getLongParameter(pDimSize.name);
		dimensions = (int) getLongParameter(pN.name);
	    } catch (Exception e) {
		Global.handleError(e, "Unable to get parameters to " +
				   "initialize NDimensionalGrid");
	    }
	    
	    int numNodes = (int) Math.pow(dimensionSize, dimensions);

	    for(int i = 0; i < numNodes; i++)
		addNode();
	}
    }


    /**
     * Resets the range of all parameters so that random graph 
     * will not be too large to work with most games, then randomize.
     */
    public void randomizeParameters()
    {
	if(!parameters.setByUser(pN.name)) {
	    pN.high = new Long(4);	    
	    parameters.randomizeParameter(pN.name);
	}

	if(!parameters.setByUser(pDimSize.name)) {
	    pDimSize.high = new Long(4);	    
	    parameters.randomizeParameter(pDimSize.name);
	}
    }


    /**
     * Makes sure that the parameters are in the proper range
     */
    protected void checkParameters() throws Exception 
    {
	if (getLongParameter(pN.name) <= 0)
	    throw new Exception("num_dimensions <= 0");

	if (getLongParameter(pN.name) > 10)
	    throw new Exception("num_dimensions > 10");

	if (getLongParameter(pDimSize.name) <= 0)
	    throw new Exception("dim_size <= 0");

	if (getLongParameter(pDimSize.name) > 20)
	    throw new Exception("dim_size > 20");
    }



    /**
     * Translate an array of indices into a single integer
     * index of a Vector.  Assumes that each index in the 
     * array is between 0 and dimensionSize.
     */
    protected int translateIndices(int[] indices, long dimSize,
				   int numDimensions) {

	int index = 0;

	try {
	    for (int i = 0; i + 1 < numDimensions; i++) {
		index += indices[i];
		index *= dimSize;
	    }
	    index += indices[numDimensions-1];
	} catch (Exception e) {
	    Global.handleError(e, "Error translating indices");
	}

	return index;
    }



    /**
     * It is always the case in N-dimensional grids  that for every 
     * edge a to b there is also an edge b to a
     */
    public boolean hasSymEdges() {
	return (true);
    }



    /**
     * It is never the case in N-dimensional grids that reflexive edges 
     * are allowed
     */
    public boolean reflexEdgesOk() {
	return (false);
    }



    /**
     * Generate an N-dimensional grid graph
     */
    public void doGenerate() {

	int n = (int) getLongParameter(pN.name);
	long dimSize = getLongParameter(pDimSize.name);

	boolean hasMore = true;
	int currNode[] = new int[n];

	// Set the index in each dimension to be 0 first
	for (int i = 0; i < n; i++)
	    currNode[i] = 0;

	// Systematically loop through all nodes indexed by their
	// position in the n-dimensional grid, adding edges
	// between adjacent nodes
	while (hasMore) {

	    // First find the next node
	    for (int i = 0; i < n; i++) {
		if (currNode[i] < dimSize - 1) {
		    currNode[i]++;
		    break;
		} else {
		    currNode[i] = 0;
		    if(i == n - 1)
			hasMore = false;
		}
	    }

	    int node1 = translateIndices(currNode, dimSize, n);

	    // Now add one to each dimension in the node and
	    // add edges back and forth
	    for (int i = 0; i < n; i++) {
		if (currNode[i] + 1 < dimSize) {
		    currNode[i]++;
		    int node2 = translateIndices(currNode, dimSize, n);

		    // Add the edges to the graph
		    addEdge(node1, node2);
		    addEdge(node2, node1);

		    // Reset currNode to the currentNode
		    currNode[i]--;
		}
	    }
	}
    }
}


