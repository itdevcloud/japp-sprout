package com.itdevcloud.japp.se.common.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TreeNode<T extends TreeDataI<T>> {

	private T data;
	private TreeNode<T> root;
	private TreeNode<T> parent;

	private List<TreeNode<T>> children;

	public TreeNode(T data) {
		if (data == null) {
			throw new RuntimeException(
					"Data cannot be null when construct a TreeNode object!");
		}
		children = new ArrayList<TreeNode<T>>();
		setData(data);
	}

	public List<TreeNode<T>> getChildren() {
		if(this.children == null) {
			this.children = new ArrayList<TreeNode<T>>();
		}
		return this.children;
	}

	public int getNumberOfChildren() {
		return getChildren().size();
	}

	public boolean hasChildren() {
		return (getNumberOfChildren() > 0);
	}

	public void setChildren(List<TreeNode<T>> children) {
		if (children != null) {
			for (int i = 0; i <= children.size() - 1; i++) {
				children.get(i).setParent(this);
				children.get(i).setRoot(this.root);
			}
		}else {
			children = new ArrayList<TreeNode<T>>();
		}
		this.children = children;
	}

	public void addChild(TreeNode<T> child) {
		child.setParent(this);
		child.setRoot(this.root);
		this.children.add(child);
	}

	public void addChildAt(int index, TreeNode<T> child) {
		child.setParent(this);
		child.setRoot(this.root);
		if (index < 0) {
			this.children.add(0, child);
		} else if (index >= this.children.size()) {
			this.children.add(child);
		} else {
			this.children.add(index, child);
		}
	}

	public void removeChildren() {
		this.children = new ArrayList<TreeNode<T>>();
	}

	public void removeChildAt(int index) {
		if(!hasChildren()) {
			return;
		}
		if (index < 0) {
			this.children.remove(0);
		} else if (index >= this.children.size()) {
			this.children.remove(this.children.size() - 1);
		} else {
			this.children.remove(index);
		}

	}

	public TreeNode<T> getChildAt(int index) {
		if(!hasChildren()) {
			return null;
		}
		if (index < 0) {
			return this.children.get(0);
		} else if (index >= this.children.size()) {
			return this.children.get(this.children.size() - 1);
		} else {
			return this.children.get(index);
		}
	}

	
	public List<TreeNode<T>> getAllChildren(int traversalLevel) {
		if (traversalLevel < 0) {
			traversalLevel = Integer.MAX_VALUE;
		}
		if (traversalLevel == 0) {
			return new ArrayList<TreeNode<T>>();
		}
		List<TreeNode<T>> list = new ArrayList<TreeNode<T>>();
		if (this.children != null) {
			list.addAll(this.children);
		}
		traversalLevel--;
		if (traversalLevel > 0) {
			for (int i = 0; i < getNumberOfChildren(); i++) {

				list.addAll(getChildAt(i).getAllChildren(traversalLevel));
			}
		}
		return list;
	}
	
	public List<TreeNode<T>> getAllChildren() {
		return getAllChildren(-1);
	}
	
	public T getData() {
		return this.data;
	}

	public void setData(T data) {
		if (data == null) {
			throw new RuntimeException(
					"Data cannot be null when construct a TreeNode object!");
		}
		this.data = data;
	}

	public TreeNode<T> getRoot() {
		return root;
	}

	public void setRoot(TreeNode<T> root) {
		this.root = root;
		for (int i = 0; i <= this.children.size() - 1; i++) {
			getChildAt(i).setRoot(root);
		}
	}

	public TreeNode<T> getParent() {
		return parent;
	}

	public void setParent(TreeNode<T> parent) {
		this.parent = parent;
	}
	
	public List<TreeNode<T>> getAncestors() {
		ArrayList<TreeNode<T>> list = new ArrayList<TreeNode<T>>();
		if (this.parent != null) {
			list.add(this.parent);
			if(!this.parent.equals(this.root)) {
				List<TreeNode<T>> tmpList = this.parent.getAncestors();
				if(tmpList != null) {
					list.addAll(tmpList);
				}
			}
		}
		return list;
	}

	public String toString() {
		StringBuffer strBuf = new StringBuffer(getData().toSimpleString());
		strBuf.append("[");
		if (this.children.size() > 0) {
			for (int i = 0; i <= this.children.size() - 1; i++) {
				strBuf.append(getChildAt(i).getData().toSimpleString());
				if (i < this.children.size() - 1) {
					strBuf.append(", ");
				}
			}
		}
		strBuf.append("]");
		return strBuf.toString();
	}

	public String createTreeDetailString(TreeNode<T> node, int level, int indent) {
		if (node == null) {
			node = this;
		}
		StringBuffer strBuf = new StringBuffer(
				createSpaceString(level * indent) + node.getData().toString());
		if (node.getChildren().size() > 0) {
			strBuf.append("\n");
			int len = node.getChildren().size() - 1;
			for (int i = 0; i <= len; i++) {
				strBuf.append(createTreeDetailString(node.getChildAt(i), level + 1,
						indent));
				if (i < len) {
					strBuf.append("\n");
				}
			}
		}
		return strBuf.toString();
	}

	public String createTreeSimpleString(TreeNode<T> node, int level, int indent) {
		if (node == null) {
			node = this;
		}
		StringBuffer strBuf = new StringBuffer(
				createSpaceString(level * indent) + node.getData().toSimpleString());
		if (node.getChildren().size() > 0) {
			strBuf.append("\n");
			int len = node.getChildren().size() - 1;
			for (int i = 0; i <= len; i++) {
				strBuf.append(createTreeSimpleString(node.getChildAt(i), level + 1,
						indent));
				if (i < len) {
					strBuf.append("\n");
				}
			}
		}
		return strBuf.toString();
	}

	private String createSpaceString(int length) {
		StringBuffer strBuf = new StringBuffer();
		for (int i = 0; i <= length - 1; i++) {
			strBuf.append(" ");
		}
		return strBuf.toString();
	}

	public void validateTreeNode(TreeNode<T> node, Set<TreeNode<T>> nodeSet, Set<TreeNode<T>> dupNodeSet) {
		if (node == null) {
			node = this;
		}
		if (nodeSet == null || dupNodeSet == null) {
			throw new RuntimeException("validateTreeNode() - nodeSet and/or dupNodeSet can not be null, check code! ");
		}
		if (nodeSet.contains(node)) {
			dupNodeSet.add(node);
			//the node has been validated, no need to validate it
			return;
		} else {
			nodeSet.add(node);
		}
		if (node.getChildren().size() > 0) {
			for (int i = 0; i <= node.getChildren().size() - 1; i++) {
				validateTreeNode(node.getChildAt(i), nodeSet, dupNodeSet);
			}
		}
		return;
	}
	
	public boolean equals(TreeNode<T> node) {
		if (node == null) {
			return false;
		}
		if(data == null && node.getData() == null) {
			return true;
		}else if(data != null && node.getData() != null) {
			return data.getUID().equalsIgnoreCase(node.getData().getParentUID());
		}else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return getData().getUID().hashCode();
	}

}