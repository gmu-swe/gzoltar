/**
 * Copyright (C) 2019 GZoltar contributors.
 * 
 * This file is part of GZoltar.
 * 
 * GZoltar is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * GZoltar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with GZoltar. If
 * not, see <https://www.gnu.org/licenses/>.
 */
package com.gzoltar.core.model;

public final class NodeFactory {

  /**
   * Parse a {@link Node} object and create its ancestors.
   * 
   * @param tree
   * @param node
   */
  public static void createNode(final Tree tree, final Node node) {

    final String nodeName = node.getName();
    final NodeType nodeType = node.getNodeType();
    final int lineNumber = node.getLineNumber();

    // === Package ===

    final String packageName = nodeName.substring(0,
        nodeType.equals(NodeType.PACKAGE) ? nodeName.indexOf(NodeType.LINE.getSymbol())
            : nodeName.indexOf(NodeType.CLASS.getSymbol()));
    Node packageNode = tree.getNode(packageName);
    if (packageNode == null) {
      packageNode = new Node(packageName, lineNumber, NodeType.PACKAGE, tree.getRoot());
      tree.addNode(packageNode);
    }

    // === Class ===

    if (nodeType.equals(NodeType.CLASS)) {
      node.setParent(packageNode);
      tree.addNode(node);
      return;
    }

    final String className = nodeName.substring(0,
        nodeType.equals(NodeType.CLASS) ? nodeName.indexOf(NodeType.LINE.getSymbol())
            : nodeName.indexOf(NodeType.METHOD.getSymbol()));
    Node classNode = tree.getNode(className);
    if (classNode == null) {
      classNode = new Node(className, lineNumber, NodeType.CLASS, packageNode);
      tree.addNode(classNode);
    }

    // === Method ===

    if (nodeType.equals(NodeType.METHOD)) {
      node.setParent(classNode);
      tree.addNode(node);
      return;
    }

    final String methodName = nodeName.substring(0, nodeName.indexOf(NodeType.LINE.getSymbol()));
    Node methodNode = tree.getNode(methodName);
    if (methodNode == null) {
      methodNode = new Node(methodName, lineNumber, NodeType.METHOD, classNode);
      tree.addNode(methodNode);
    }

    // === Line ===

    node.setParent(methodNode);
    tree.addNode(node);
  }

  public static Node createNode(String className, String methodName, String methodDesc, int lineNumber, String instructionIdentifier, Node parentNode) {
    String packageName;
    if (className.indexOf('/') > 0)
      packageName = className.substring(0, className.lastIndexOf('/'));
    else
      packageName = "";

    packageName = packageName.replace('/','.');

    StringBuilder _className = new StringBuilder(packageName);
    _className.append(NodeType.CLASS.getSymbol());
    if (className.indexOf('/') > 0)
      _className.append(className.substring(className.lastIndexOf('/') + 1));
    else
      _className.append(className);

    StringBuilder _methodName = _className;
    _methodName.append(NodeType.METHOD.getSymbol());
    _methodName.append(methodName);
    _methodName.append(methodDesc);

    StringBuilder _lineName = _methodName;
    _lineName.append(NodeType.LINE.getSymbol());
    _lineName.append(String.valueOf(lineNumber));

    StringBuilder instructionProbeName = _lineName;
    instructionProbeName.append(NodeType.INSTRUCTION.getSymbol());
    instructionProbeName.append(instructionIdentifier);


    return new Node(instructionProbeName.toString(), lineNumber, instructionIdentifier, NodeType.INSTRUCTION, parentNode);
  }

  public static Node createNode(String className, String methodName, String methodDesc, int lineNumber) {
    String packageName;
    if (className.indexOf('/') > 0)
      packageName = className.substring(0, className.lastIndexOf('/'));
    else
      packageName = "";

    packageName = packageName.replace('/','.');

    StringBuilder _className = new StringBuilder(packageName);
    _className.append(NodeType.CLASS.getSymbol());
    if (className.indexOf('/') > 0)
      _className.append(className.substring(className.lastIndexOf('/') + 1));
    else
      _className.append(className);

    StringBuilder _methodName = _className;
    _methodName.append(NodeType.METHOD.getSymbol());
    _methodName.append(methodName);
    _methodName.append(methodDesc);

    StringBuilder _lineName = _methodName;
    _lineName.append(NodeType.LINE.getSymbol());
    _lineName.append(String.valueOf(lineNumber));

    return new Node(_lineName.toString(), lineNumber, NodeType.LINE);
  }

}
