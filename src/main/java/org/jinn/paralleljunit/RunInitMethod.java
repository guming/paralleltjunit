/**
 * 
 */
package org.jinn.paralleljunit;

import java.util.List;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class RunInitMethod extends Statement {
        private final Statement fNext;

        private final Object fTarget;

        private final List<FrameworkMethod> fInitFors;

        public RunInitMethod(Statement next, List<FrameworkMethod> initFors, Object target) {
                fNext= next;
                fInitFors= initFors;
                fTarget= target;
        }

        @Override
        public void evaluate() throws Throwable {
                for (FrameworkMethod before : fInitFors)
                        before.invokeExplosively(fTarget);
                fNext.evaluate();
        }
}