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
package org.cqfn.astranaut.core.example.javascript.rules;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.cqfn.astranaut.core.algorithms.conversion.Converter;
import org.cqfn.astranaut.core.base.Builder;
import org.cqfn.astranaut.core.base.DummyNode;
import org.cqfn.astranaut.core.base.Factory;
import org.cqfn.astranaut.core.base.Node;

/**
 * Converter describing DSL conversion rule.
 * @since 1.0
 */
public final class Rule0 implements Converter {
    /**
     * The instance.
     */
    public static final Converter INSTANCE = new Rule0();

    /**
     * The 'Variable' string.
     */
    private static final String VARIABLE = "Variable";

    /**
     * Constructor.
     */
    private Rule0() {
    }

    @Override
    public Node convert(final Node node, final Factory factory) {
        Node result = DummyNode.INSTANCE;
        final Map<Integer, List<Node>> children = new TreeMap<>();
        final Map<Integer, String> data = new TreeMap<>();
        final boolean matched = Matcher2.INSTANCE.match(node, children, data);
        if (matched) {
            final Builder builder = factory.createBuilder(Rule0.VARIABLE);
            builder.setData(data.get(1));
            result = builder.createNode();
        }
        return result;
    }
}
