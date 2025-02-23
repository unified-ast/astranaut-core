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
package org.cqfn.astranaut.core.algorithms.conversion;

import java.util.ArrayList;
import java.util.List;
import org.cqfn.astranaut.core.base.DummyNode;
import org.cqfn.astranaut.core.base.Factory;
import org.cqfn.astranaut.core.base.MutableNode;
import org.cqfn.astranaut.core.base.Node;

/**
 * Tree converter built on a set of rules described in DSL.
 * @since 1.0
 */
public class Adapter {
    /**
     * The list of node converters.
     */
    private final List<Converter> converters;

    /**
     * The node factory.
     */
    private final Factory factory;

    /**
     * Constructor.
     * @param converters The list of node converters
     * @param factory The node factory
     */
    public Adapter(final List<Converter> converters, final Factory factory) {
        this.converters = converters;
        this.factory = factory;
    }

    /**
     * Converts the [sub]tree to another, based on DSL rules.
     * @param root The root node of the subtree
     * @return A converted tree or empty tree if the conversion is impossible
     */
    public Node convert(final Node root) {
        final MutableNode mutable = new MutableNode(root);
        Node result = mutable;
        final List<MutableNode> nodes = new ArrayList<>(0);
        NodeListBuilder.buildNodeList(mutable, nodes);
        for (final MutableNode original : nodes) {
            boolean converted = false;
            for (final Converter converter : this.converters) {
                final Node transformed = converter.convert(original, this.factory);
                if (!(transformed instanceof DummyNode)) {
                    result = Adapter.replace(original, result, transformed);
                    converted = true;
                    break;
                }
            }
            if (!converted) {
                result = Adapter.replace(original, result, original.rebuild());
            }
        }
        return result;
    }

    /**
     * Converts the [sub]tree to another applying the specified variant
     *  of conversion.
     * @param variant The variant index
     * @param root The root node of the subtree
     * @return A converted tree or empty tree if the conversion is impossible
     */
    public Node partialConvert(final int variant, final Node root) {
        int conversions = 0;
        final MutableNode mutable = new MutableNode(root);
        Node result = mutable;
        final List<MutableNode> nodes = new ArrayList<>(0);
        NodeListBuilder.buildNodeList(mutable, nodes);
        for (final MutableNode original : nodes) {
            final Converter converter = this.converters.get(0);
            final Node transformed = converter.convert(original, this.factory);
            if (!(transformed instanceof DummyNode)) {
                if (variant == conversions) {
                    result = Adapter.replace(original, result, transformed);
                    break;
                }
                conversions += 1;
            }
        }
        return result;
    }

    /**
     * Calculate an amount of possible conversions that one rule may conduct
     *  within the tree.
     * @param root The root node of the subtree
     * @return Amount of conversions
     */
    public int calculateConversions(final Node root) {
        int conversions = 0;
        final MutableNode mutable = new MutableNode(root);
        final List<MutableNode> nodes = new ArrayList<>(0);
        NodeListBuilder.buildNodeList(mutable, nodes);
        for (final MutableNode original : nodes) {
            final Converter converter = this.converters.get(0);
            final Node transformed = converter.convert(original, this.factory);
            if (!(transformed instanceof DummyNode)) {
                conversions += 1;
            }
        }
        return conversions;
    }

    /**
     * Replaces the [sub]tree to another.
     * @param original The initial node to be converted
     * @param target The result node
     * @param transformed The new node
     * @return Modified [sub]tree
     */
    private static Node replace(final MutableNode original,
        final Node target, final Node transformed) {
        Node result = target;
        final MutableNode parent = original.getParent();
        if (parent == null) {
            result = transformed;
        } else {
            parent.replaceChild(original, transformed);
        }
        return result;
    }

    /**
     * Creates a list from nodes.
     *  The list is sorted in descending order of nodes depth in the tree.
     *  Leaf nodes are at the beginning of the list, and the last element is the root.
     * @since 0.2.2
     */
    private static class NodeListBuilder {
        /**
         * Expands the tree to the node list.
         * @param root Root node
         * @param nodes Resulting list of nodes
         */
        private static void buildNodeList(final MutableNode root,
            final List<MutableNode> nodes) {
            final int count = root.getChildCount();
            for (int index = 0; index < count; index = index + 1) {
                NodeListBuilder.buildNodeList(root.getMutableChild(index), nodes);
            }
            nodes.add(root);
        }
    }
}
