/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

@Mixin(StaticCompilationTestSupport)
class MixedModeStaticCompilationTest extends StaticTypeCheckingTestCase {
    @Override
    protected void setUp() {
        super.setUp()
        extraSetup()
    }

    void testDynamicMethodCall() {
        def customizers = config.compilationCustomizers
        customizers.removeAll { it instanceof ASTTransformationCustomizer}
        //config.optimizationOptions.indy = true
        try {
            assertScript '''import groovy.transform.CompileStatic
                @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
                int bar() {
                    foo() + baz()
                }
                int baz() {
                    456
                }
                this.metaClass.foo = { 123 }
                assert bar() == 579
            '''
        } finally {
            // println astTrees
        }
    }

    void testDynamicMethodCallWithStaticCallArgument() {
        def customizers = config.compilationCustomizers
        customizers.removeAll { it instanceof ASTTransformationCustomizer}
        //config.optimizationOptions.indy = true
        try {
            assertScript '''import groovy.transform.CompileStatic
                @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
                int bar() {
                    twice(baz())
                }
                int baz() {
                    456
                }
                this.metaClass.twice = { 2*it }
                assert bar() == 912
            '''
        } finally {
            // println astTrees
        }
    }

    void testDynamicMethodCallOnField() {
        def customizers = config.compilationCustomizers
        customizers.removeAll { it instanceof ASTTransformationCustomizer}
        //config.optimizationOptions.indy = true
        try {
            assertScript '''import groovy.transform.CompileStatic
                @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
                class Holder {
                    def delegate
                    int bar() {
                        2*delegate.baz()
                    }
                }
                class Baz {
                    int baz() { 456 }
                }
                def holder = new Holder(delegate: new Baz())
                assert holder.bar() == 912
            '''
        } finally {
            // println astTrees
        }
    }

    void testDynamicProperty() {
        def customizers = config.compilationCustomizers
        customizers.removeAll { it instanceof ASTTransformationCustomizer}
        //config.optimizationOptions.indy = true
        try {
            assertScript '''import groovy.transform.CompileStatic
                @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
                int value(String str) {
                    str.val
                }
                @Category(String)
                class StringCategory {
                    int getVal() { this.length() }
                }
                use (StringCategory) {
                    assert value('abc') == 3
                }
            '''
        } finally {
            // println astTrees
        }
    }

    void testDynamicPropertyMixedWithStatic() {
        def customizers = config.compilationCustomizers
        customizers.removeAll { it instanceof ASTTransformationCustomizer}
        //config.optimizationOptions.indy = true
        try {
            assertScript '''import groovy.transform.CompileStatic
                @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
                class Holder {
                    int offset() { 2 }
                    int value(String str) {
                        str.val + offset()
                    }
                }
                @Category(String)
                class StringCategory {
                    int getVal() { this.length() }
                }
                def holder = new Holder()
                use (StringCategory) {
                    assert holder.value('abc') == 5
                }
            '''
        } finally {
            // println astTrees
        }
    }

    void testDynamicPropertyAsStaticArgument() {
        def customizers = config.compilationCustomizers
        customizers.removeAll { it instanceof ASTTransformationCustomizer}
        //config.optimizationOptions.indy = true
        try {
            assertScript '''import groovy.transform.CompileStatic
                @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
                class Holder {
                    int twice(int v) { 2*v }
                    int value(String str) {
                        twice(str.val)
                    }
                }
                @Category(String)
                class StringCategory {
                    int getVal() { this.length() }
                }
                def holder = new Holder()
                use (StringCategory) {
                    assert holder.value('abc') == 6
                }
            '''
        } finally {
            // println astTrees
        }
    }

    void testDynamicVariable() {
        def customizers = config.compilationCustomizers
        customizers.removeAll { it instanceof ASTTransformationCustomizer}
        //config.optimizationOptions.indy = true
        try {
            shell.setVariable("myVariable", 123)
            assertScript '''import groovy.transform.CompileStatic
                @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
                int value() {
                    myVariable
                }
                assert value() == 123
            '''
        } finally {
            // println astTrees
        }
    }

    void testDynamicVariableMixedWithStaticCall() {
        def customizers = config.compilationCustomizers
        customizers.removeAll { it instanceof ASTTransformationCustomizer}
        //config.optimizationOptions.indy = true
        try {
            shell.setVariable("myVariable", 123)
            assertScript '''import groovy.transform.CompileStatic
                @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
                class Holder {
                    def binding
                    def propertyMissing(String name) { binding.getVariable(name) }
                    int value() {
                        myVariable + offset()
                    }
                    int offset() { 123 }
                }
                def h = new Holder(binding:binding)
                assert h.value() == 246
            '''
        } finally {
            // println astTrees
        }
    }

    void testDynamicVariableAsStaticCallParameter() {
        def customizers = config.compilationCustomizers
        customizers.removeAll { it instanceof ASTTransformationCustomizer}
        //config.optimizationOptions.indy = true
        try {
            shell.setVariable("myVariable", 123)
            assertScript '''import groovy.transform.CompileStatic
                @CompileStatic(extensions='groovy/transform/sc/MixedMode.groovy')
                class Holder {
                    def binding
                    def propertyMissing(String name) { binding.getVariable(name) }
                    int value() {
                        twice(myVariable)
                    }
                    int twice(int x) { 2*x }
                }
                def h = new Holder(binding:binding)
                assert h.value() == 246
            '''
        } finally {
            // println astTrees
        }
    }
}