/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Ivan Kniazkov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.cqfn.astranaut.core.base;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Node containing child nodes, as well as actions on these nodes.
 * @since 1.1.0
 */
public final class DiffNode extends NodeAndType implements DiffTreeItem, PrototypeBasedNode {
    /**
     * The parent node with action.
     */
    private final DiffNode parent;

    /**
     * The prototype node, i.e. 'ordinary', non-difference original node before the changes.
     */
    private final Node prototype;

    /**
     * The list of children with actions.
      */
    private final List<DiffTreeItem> children;

    /**
     * Constructor.
     * @param parent The parent convertible node
     * @param prototype The prototype node
     */
    private DiffNode(final DiffNode parent, final Node prototype) {
        this.parent = parent;
        this.prototype = prototype;
        this.children = this.initChildrenList();
    }

    /**
     * Constructor.
     * @param prototype The prototype node.
     */
    public DiffNode(final Node prototype) {
        this(null, prototype);
    }

    /**
     * Returns the parent node.
     * @return The parent node
     */
    public DiffNode getParent() {
        return this.parent;
    }

    @Override
    public Node getPrototype() {
        return this.prototype;
    }

    @Override
    public Fragment getFragment() {
        return this.prototype.getFragment();
    }

    @Override
    public String getData() {
        return this.prototype.getData();
    }

    @Override
    public int getChildCount() {
        return this.children.size();
    }

    @Override
    public Node getChild(final int index) {
        return this.children.get(index);
    }

    @Override
    public String getName() {
        return this.prototype.getTypeName();
    }

    @Override
    public Map<String, String> getProperties() {
        return this.prototype.getProperties();
    }

    @Override
    public Builder createBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getBefore() {
        return this.getBranch(DiffTreeItem::getBefore);
    }

    @Override
    public Node getAfter() {
        return this.getBranch(DiffTreeItem::getAfter);
    }

    @Override
    public String toString() {
        return Node.toString(this);
    }

    /**
     * Adds an action that inserts the node after another node.
     *  If no other node is specified, inserts at the beginning of the children's list.
     * @param node Node to be inserted
     * @param after Node after which to insert
     * @return Result of operation, @return {@code true} if action was added
     */
    public boolean insertNodeAfter(final Node node, final Node after) {
        boolean result = false;
        if (after == null) {
            this.children.add(0, new Insert(node));
            result = true;
        } else {
            final ListIterator<DiffTreeItem> iterator = this.children.listIterator();
            while (iterator.hasNext()) {
                final Node child = iterator.next();
                if (child instanceof DiffNode && ((DiffNode) child).getPrototype() == after
                    || child instanceof Insert && ((Insert) child).getAfter() == after) {
                    iterator.add(new Insert(node));
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Adds an action that replaces a node.
     *  The position of the node is specified by the index.
     * @param index Node index
     * @param replacement Child node to be replaced by
     * @return Result of operation, @return {@code true} if action was added
     */
    public boolean replaceNode(final int index, final Node replacement) {
        boolean result = false;
        if (index >= 0 && index < this.children.size()) {
            final DiffTreeItem child = this.children.get(index);
            if (child instanceof DiffNode) {
                this.children.set(
                    index,
                    new Replace(
                        ((DiffNode) child).getPrototype(),
                        replacement
                    )
                );
                result = true;
            }
        }
        return result;
    }

    /**
     * Adds an action that replaces a node.
     * @param node A node
     * @param replacement Child node to be replaced by
     * @return Result of operation, @return {@code true} if action was added
     */
    public boolean replaceNode(final Node node, final Node replacement) {
        boolean result = false;
        final int index = this.findChildIndex(node);
        if (index >= 0) {
            result = this.replaceNode(index, replacement);
        }
        return result;
    }

    /**
     * Adds an action that removes a node by index.
     * @param index Node index
     * @return Result of operation, @return {@code true} if action was added
     */
    public boolean deleteNode(final int index) {
        boolean result = false;
        if (index >= 0 && index < this.children.size()) {
            final DiffTreeItem child = this.children.get(index);
            if (child instanceof DiffNode) {
                this.children.set(index, new Delete(((DiffNode) child).getPrototype()));
                result = true;
            }
        }
        return result;
    }

    /**
     * Adds an action that removes a node.
     * @param node A node
     * @return Result of operation, @return {@code true} if action was added
     */
    public boolean deleteNode(final Node node) {
        boolean result = false;
        final int index = this.findChildIndex(node);
        if (index >= 0) {
            result = this.deleteNode(index);
        }
        return result;
    }

    /**
     * Transforms children nodes to difference ones.
     * @return List of difference nodes
     */
    private List<DiffTreeItem> initChildrenList() {
        final int count = this.prototype.getChildCount();
        final List<DiffTreeItem> result = new LinkedList<>();
        for (int index = 0; index < count; index = index + 1) {
            result.add(
                new DiffNode(this, this.prototype.getChild(index))
            );
        }
        return result;
    }

    /**
     * Searches the index of a child element by its prototype.
     * @param node Prototype of the node whose index is to be found
     * @return Index or -1 if there is no such node or it has already been deleted or replaced
     */
    private int findChildIndex(final Node node) {
        int result = -1;
        final int count = this.children.size();
        for (int index = 0; result < 0 && index < count; index = index + 1) {
            final DiffTreeItem child = this.children.get(index);
            if (child instanceof DiffNode) {
                Node proto = ((DiffNode) child).getPrototype();
                while (true) {
                    if (node.equals(proto)) {
                        result = index;
                        break;
                    } else if (proto instanceof PrototypeBasedNode) {
                        proto = ((PrototypeBasedNode) proto).getPrototype();
                    } else {
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns a branch: before or after the changes.
     * @param selector Branch selector
     * @return This node, before of after changes
     */
    private Node getBranch(final BranchSelector selector) {
        Node result = DummyNode.INSTANCE;
        final Builder builder = this.prototype.getType().createBuilder();
        do {
            if (builder == null) {
                break;
            }
            builder.setFragment(this.getFragment());
            if (!builder.setData(this.getData())) {
                break;
            }
            final List<Node> list = new ArrayList<>(this.children.size());
            for (final DiffTreeItem child : this.children) {
                final Node branch = selector.select(child);
                if (branch != null) {
                    list.add(branch);
                }
            }
            if (!builder.setChildrenList(list)) {
                break;
            }
            if (!builder.isValid()) {
                break;
            }
            result = builder.createNode();
        } while (false);
        return result;
    }

    /**
     * Selector that selects some branch from a difference tree item: before or after the changes.
     * @since 1.1.0
     */
    private interface BranchSelector {
        /**
         * Selects a branch from difference tree item.
         * @param item An item
         * @return Branch (node before or after changes)
         */
        Node select(DiffTreeItem item);
    }
}
