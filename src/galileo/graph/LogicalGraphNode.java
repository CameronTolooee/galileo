/*
Copyright (c) 2013, Colorado State University
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are
disclaimed. In no event shall the copyright holder or contributors be liable for
any direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused and on
any theory of liability, whether in contract, strict liability, or tort
(including negligence or otherwise) arising in any way out of the use of this
software, even if advised of the possibility of such damage.
*/

package galileo.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class LogicalGraphNode {
    private HashMap<String, LogicalGraphNode> edges
        = new HashMap<String, LogicalGraphNode>();

    private ArrayList<String> blockPaths
        = new ArrayList<String>();

    public void addNodePath(String nodePath, String blockPath) {
        String[] nodes = nodePath.split("/");
        Iterator<String> nodeIter = Arrays.asList(nodes).iterator();

        if (nodeIter.hasNext()) {
            linkNodes(nodeIter, blockPath);
        } else {
            //TODO This should NOT silently die here.  Fix.
        }
    }

    private void linkNodes(Iterator<String> nodes, String blockPath) {
        String nodeName = nodes.next();

        LogicalGraphNode nextNode = edges.get(nodeName);

        if (nextNode == null) {
            nextNode = new LogicalGraphNode();
            edges.put(nodeName, nextNode);
        }

        if (nodes.hasNext()) {
            nextNode.linkNodes(nodes, blockPath);
        } else {
            nextNode.addFilePath(blockPath);
        }
    }

    public void addFilePath(String blockPath) {
        blockPaths.add(blockPath);
    }

    /**
     * Query this node and any of its children.
     *
     * @param query
     *     Query string
     *
     * @param resultList
     *     ArrayList of logical graph nodes that results should be added to, if
     *     any.
     */
    public void subQuery(Iterator<String> query,
                         ArrayList<LogicalGraphNode> resultList) {
        if (!query.hasNext()) {
            resultList.add(this);
            for (LogicalGraphNode n : edges.values()) {
                n.subQuery(query, resultList);
            }
            return;
        }

        String nextNode = query.next();
        if (query.equals("*")) {
            for (LogicalGraphNode n : edges.values()) {
                n.subQuery(query, resultList);
            }
        }

        LogicalGraphNode node = edges.get(nextNode);
        if (node != null) {
            node.subQuery(query, resultList);
        }
    }

    public ArrayList<String> getBlockPaths() {
        return blockPaths;
    }
}
