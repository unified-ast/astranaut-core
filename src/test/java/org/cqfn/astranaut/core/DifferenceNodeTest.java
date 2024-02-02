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
package org.cqfn.astranaut.core;

import org.cqfn.astranaut.core.example.green.GreenFactory;
import org.cqfn.astranaut.core.exceptions.BaseException;
import org.cqfn.astranaut.core.utils.FilesReader;
import org.cqfn.astranaut.core.utils.JsonDeserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests covering difference node, i.e. {@link DifferenceNode} class.
 *
 * @since 1.1.0
 */
class DifferenceNodeTest {
    /**
     * The folder with test resources.
     */
    private static final String TESTS_PATH = "src/test/resources/json/";

    /**
     * File name with tree containing 'Delete' action.
     */
    private static final String TREE_WITH_DELETE = "tree_containing_delete_action.json";

    /**
     * Testing {@link  DifferenceNode#getBefore()} method.
     */
    @Test
    void testDeleteGetBefore() {
        final Node root = this.loadTree(DifferenceNodeTest.TREE_WITH_DELETE);
        Assertions.assertTrue(root instanceof DifferenceNode);
        final DifferenceNode diff = (DifferenceNode) root;
        final Node actual = diff.getBefore();
        Assertions.assertNotEquals(EmptyTree.INSTANCE, actual);
        final Node expected = this.loadTree("before_delete_action.json");
        Assertions.assertTrue(expected.deepCompare(actual));
    }

    /**
     * Testing {@link  DifferenceNode#getAfter()} method.
     */
    @Test
    void testDeleteGetAfter() {
        final Node root = this.loadTree(DifferenceNodeTest.TREE_WITH_DELETE);
        Assertions.assertTrue(root instanceof DifferenceNode);
        final DifferenceNode diff = (DifferenceNode) root;
        final Node actual = diff.getAfter();
        Assertions.assertNotEquals(EmptyTree.INSTANCE, actual);
        final Node expected = this.loadTree("after_delete_action.json");
        Assertions.assertTrue(expected.deepCompare(actual));
    }

    /**
     * Returns content of the specified file.
     * @param name The name of the file
     * @return The file content
     */
    private String getFileContent(final String name) {
        final String file = DifferenceNodeTest.TESTS_PATH.concat(name);
        boolean oops = false;
        String source = "";
        try {
            source = new FilesReader(file).readAsString(
                (FilesReader.CustomExceptionCreator<BaseException>) ()
                    -> new BaseException() {
                        private static final long serialVersionUID = -8921455132545245481L;

                        @Override
                        public String getInitiator() {
                            return "DifferenceNodeTest";
                        }

                        @Override
                        public String getErrorMessage() {
                            return String.format(
                                "Could not read the file that contains source tree: %s",
                                file
                            );
                        }
                    }
            );
        } catch (final BaseException exception) {
            oops = true;
        }
        Assertions.assertFalse(oops);
        return source;
    }

    /**
     * Loads a tree from a JSON file.
     * @param name The name of the file
     * @return Root node
     */
    private Node loadTree(final String name) {
        final String source = this.getFileContent(name);
        final JsonDeserializer deserializer = new JsonDeserializer(
            source,
            language -> GreenFactory.INSTANCE
        );
        final Node root = deserializer.convert();
        Assertions.assertNotEquals(EmptyTree.INSTANCE, root);
        return root;
    }
}
