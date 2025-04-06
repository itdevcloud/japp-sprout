package com.itdevcloud.japp.se.common.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.itdevcloud.japp.se.common.util.CommonUtil;

public class Tree<T extends TreeDataI<T>> {

	private static final Logger logger = Logger.getLogger(Tree.class.getName());
	
	private HashMap<String, TreeNode<T>> tempNodeMap = new HashMap<String, TreeNode<T>>();
	
	private TreeNode<T> root;

	public Tree(TreeNode<T> root) {
		if (root == null) {
			throw new RuntimeException(
					"root cannot be null when construct a Tree object!");
		}
		this.root = root;
	}

	public TreeNode<T> getRoot() {
		return this.root;
	}

	public int getNumberOfNodes() {
		int numberOfNodes = getNumberOfNodes(root) + 1; // 1 for the root!
		return numberOfNodes;
	}

	private int getNumberOfNodes(TreeNode<T> node) {
		int numberOfNodes = node.getNumberOfChildren();
		for (TreeNode<T> child : node.getChildren()) {
			numberOfNodes += getNumberOfNodes(child);
		}
		return numberOfNodes;
	}
	
	public List<TreeNode<T>> getAllChildren(int traversalLevel) {
		return getAllChildren(root, traversalLevel) ;
	}
	public List<TreeNode<T>> getAllChildren(TreeNode<T> node, int traversalLevel) {
		if(node == null) {
			node = root;
		}
		//node may only have UID info
		TreeNode<T> realNode = find(node);
		if(realNode == null) {
			return new ArrayList<TreeNode<T>>();
		}
		List<TreeNode<T>> list = realNode.getAllChildren(traversalLevel);
		return list;
	}

	public TreeNode<T> find(TreeNode<T> nodeToFind) {
		TreeNode<T> returnNode = null;
		returnNode = find(root, nodeToFind);
		return returnNode;
	}

	public TreeNode<T> find(TreeNode<T> currentNode, TreeNode<T> nodeToFind) {
		if(currentNode == null) {
			currentNode = root;
		}
		TreeNode<T> returnNode = null;
		int i = 0;
		if (currentNode.getData().getUID().equalsIgnoreCase(nodeToFind.getData().getUID())) {
			returnNode = currentNode;
		} else if (currentNode.hasChildren()) {
			i = 0;
			while (returnNode == null && i < currentNode.getNumberOfChildren()) {
				returnNode = find(currentNode.getChildAt(i), nodeToFind);
				i++;
			}
		}

		return returnNode;
	}
	
	
	public TreeNode<T> addTreeNode(TreeNode<T> treeNode) {

		if (treeNode == null) {
			throw new RuntimeException(
					"treeNode parameter can't be null when add to a Tree, check code!");
		}

		// Check existing one
		TreeNode<T> existingNode = this.find(treeNode);
		if (existingNode != null) {
			//find the node in the tree, no need to add it
			return existingNode;
		}

		// Lookup parent in tree
		T tmpData = treeNode.getData().createNewInstance();
		tmpData.setUID(treeNode.getData().getParentUID());
		TreeNode<T> tempParentTreeNode = new TreeNode<T>(tmpData);
		
		TreeNode<T> parentTreeNode = this.find(tempParentTreeNode);
		
		TreeNode<T> tmpTreeNode = null;
		
		if (parentTreeNode == null) {
			//parent not found in the tree, so can not add the node into tree at this time.
			//add it into temp tree map
			// is the node saved in temp tree map?
			tmpTreeNode = tempNodeMap.get(treeNode.getData().getUID());
			if (tmpTreeNode != null) {
				//find the node in temp tree map, reset/update tree data 
				tmpTreeNode.setData(treeNode.getData());
			} else {
				//not find the node, try to find its parent in temp tree map
				tmpTreeNode = tempNodeMap.get(treeNode.getData().getParentUID());
				if (tmpTreeNode != null) {
					tmpTreeNode.addChild(treeNode);
				} else {
					//parent not in the temp tree map as well, just save the node in the temp tree map for future process
					tempNodeMap.put(treeNode.getData().getUID(), treeNode);
				}
			}
		} else {
			//parent found in the tree
			// is the node saved in temp parent map?
			tmpTreeNode = tempNodeMap.get(treeNode.getData().getUID());
			if (tmpTreeNode != null) {
				parentTreeNode.addChild(treeNode);
				tempNodeMap.remove(treeNode.getData().getUID());
			} else {
				parentTreeNode.addChild(treeNode);
			}
		}
		return treeNode;
	}

	
	
	@Override
	public String toString() {
		StringBuffer strBuf = new StringBuffer(root.toString());
		return strBuf.toString();
	}

	public String createTreeSimpleString() {
		return root.createTreeSimpleString(null, 0, 4);
	}
	
	public String createTreeDetailString() {
		return root.createTreeDetailString(null, 0, 4);
	}
	
	public boolean validateTree() {
		// Continue to process the tempNodeMap to build the tree
		boolean foundChild = true;
		while (tempNodeMap!= null && !tempNodeMap.isEmpty() && foundChild) {
			foundChild = false;
			for (Iterator<Map.Entry<String, TreeNode<T>>> it = tempNodeMap
					.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, TreeNode<T>> entry = it.next();

				T myTreeData = entry.getValue().getData();
				T tmpData = myTreeData.createNewInstance();
				tmpData.setUID(myTreeData.getParentUID());
				TreeNode<T> tmpParentTreeNode =  new TreeNode<T>(tmpData);

				TreeNode<T> trueParentTreeNode = this.find(tmpParentTreeNode);
				
				if (trueParentTreeNode != null) {
					trueParentTreeNode.addChild(entry.getValue());
					it.remove();
					foundChild = true;
				}
			}
		}//end while

		boolean result = true;
		Set<TreeNode<T>> nodeSet = new HashSet<TreeNode<T>>();
		Set<TreeNode<T>> dupNodeSet = new HashSet<TreeNode<T>>();
		
		this.root.validateTreeNode(this.root, nodeSet, dupNodeSet);

		int totalNodeNumber = getNumberOfNodes();
		int nodeSetSize = nodeSet.size();
		logger.info("Validate Tree: Total Node Number = "
				+ totalNodeNumber);
		logger.info("Validate Tree: nodeSet Number = " + nodeSet.size());
	
    	logger.fine("Validate Tree: Simple String = \n" + createTreeSimpleString());

		if (dupNodeSet.size() > 0 || totalNodeNumber != nodeSetSize) {
			logger.warning("Validate Tree: Contains Duplication, dupNodeSet Number = " + dupNodeSet.size());
			logger.info("Validate Tree: DupDataSet = \n" + CommonUtil.setToString(dupNodeSet, 0));
			result = false;
		}else {
			logger.info("Validate Tree: No Duplication Detected");
		}

		if (tempNodeMap != null && tempNodeMap.size() > 0) {
			logger.warning("Validate Tree: Orphan Tree Node Count  = "
					+ tempNodeMap.size());
			int i = 1;
			for (Map.Entry<String, TreeNode<T>> entry : tempNodeMap
					.entrySet()) {
				logger.info("\n Orphan (" + i++ + ")\n "
						+ entry.getValue().createTreeSimpleString(null, 0, 4));
			}
			result = false;
		}
		return result;
	}
}