/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Ivan Kniazkov
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
package org.cqfn.astranaut.core.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import org.cqfn.astranaut.core.Delete;
import org.cqfn.astranaut.core.DraftNode;
import org.cqfn.astranaut.core.EmptyTree;
import org.cqfn.astranaut.core.Node;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test for {@link JsonSerializer} class.
 *
 * @since 1.0.2
 */
class JsonSerializerTest {
    /**
     * The type IntegerLiteral.
     */
    private static final String INT_LITERAL = "IntegerLiteral";

    /**
     * The folder with test resources.
     */
    private static final String TESTS_PATH = "src/test/resources/json/";

    /**
     * Test for a tree serialization to a JSON string.
     */
    @Test
    void testSerializationToString() {
        final DraftNode.Constructor ctor = new DraftNode.Constructor();
        ctor.setName("TestNode");
        ctor.setData("value");
        final Node tree = ctor.createNode();
        final boolean result = this.serializeAndCompare(
            tree,
            "serialization_to_string_expected.txt"
        );
        Assertions.assertTrue(result);
    }

    /**
     * Test for a tree serialization to a JSON string.
     */
    @Test
    void testSerializationTreeWithActions() {
        final boolean result = this.serializeAndCompare(
            this.createSampleTreeWithActions(),
            "serialization_and_parsing_actions.txt"
        );
        Assertions.assertTrue(result);
    }

    /**
     * Test for a tree serialization to a JSON file.
     * @param temp A temporary directory
     */
    @Test
    void testSerializationToFile(@TempDir final Path temp) {
        final Node tree = this.createSampleTree();
        final JsonSerializer serializer = new JsonSerializer(tree);
        boolean oops = false;
        String expected = "";
        String result = "";
        final Path actual = temp.resolve("result.json");
        try {
            serializer.serializeToFile(actual.toString());
            expected =
                new FilesReader(
                    JsonSerializerTest.TESTS_PATH.concat("serialization_to_file_expected.json")
                ).readAsString();
            result = new FilesReader(actual.toString()).readAsString();
        } catch (final IOException exception) {
            oops = true;
        }
        Assertions.assertFalse(oops);
        Assertions.assertEquals(expected, result);
    }

    /**
     * Test for exception while writing to a JSON file.
     */
    @Test
    void testFilesWriterException() {
        final Node tree = EmptyTree.INSTANCE;
        final JsonSerializer serializer = new JsonSerializer(tree);
        final boolean result = serializer.serializeToFile("/");
        Assertions.assertFalse(result);
    }

    /**
     * Create a simple tree for testing.
     * @return Tree
     */
    private Node createSampleTree() {
        final DraftNode.Constructor addition = new DraftNode.Constructor();
        addition.setName("Addition");
        final DraftNode.Constructor left = new DraftNode.Constructor();
        left.setName(JsonSerializerTest.INT_LITERAL);
        left.setData("2");
        final DraftNode.Constructor right = new DraftNode.Constructor();
        right.setName(JsonSerializerTest.INT_LITERAL);
        right.setData("3");
        addition.setChildrenList(Arrays.asList(left.createNode(), right.createNode()));
        return addition.createNode();
    }

    /**
     * Create a simple tree containing actions for testing.
     * @return Tree
     */
    private Node createSampleTreeWithActions() {
        final DraftNode.Constructor statements = new DraftNode.Constructor();
        statements.setName("StatementList");
        final DraftNode.Constructor left = new DraftNode.Constructor();
        left.setName("FunctionCall");
        left.setData("print");
        final DraftNode.Constructor ret = new DraftNode.Constructor();
        ret.setName("Return");
        final DraftNode.Constructor literal = new DraftNode.Constructor();
        literal.setName(JsonSerializerTest.INT_LITERAL);
        literal.setData("0");
        ret.setChildrenList(Collections.singletonList(literal.createNode()));
        final Delete delete = new Delete(ret.createNode());
        statements.setChildrenList(Arrays.asList(left.createNode(), delete));
        return statements.createNode();
    }

    /**
     * Serializes syntax tree and compares the result to some sample file.
     * @param root Root node of the tree
     * @param filename File name
     * @return Checking result, {@code true} if the result is obtained and matches the sample
     */
    private boolean serializeAndCompare(final Node root, final String filename) {
        final JsonSerializer serializer = new JsonSerializer(root);
        final String result = serializer.serialize().replace("\r", "");
        boolean oops = false;
        String expected = "";
        try {
            expected =
                new FilesReader(
                    JsonSerializerTest.TESTS_PATH.concat(filename)
                ).readAsString().replace("\r", "");
        } catch (final IOException exception) {
            oops = true;
        }
        return !oops && expected.equals(result);
    }
}
