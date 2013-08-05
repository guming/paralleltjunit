/**
 * 
 */
package org.jinn.paralleljunit;

import java.util.ArrayList;
import java.util.List;

import org.junit.internal.runners.model.MultipleFailureException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class RunCheckMethod extends Statement {
        private final Statement fNext;

        private final Object fTarget;

        private final List<FrameworkMethod> fCheckFors;

        public RunCheckMethod(Statement next, List<FrameworkMethod> checkFors, Object target) {
                fNext= next;
                fCheckFors= checkFors;
                fTarget= target;
        }

        @Override
        public void evaluate() throws Throwable {
                List<Throwable> errors = new ArrayList<Throwable>();
                errors.clear();
                try {
                        fNext.evaluate();
                } catch (Throwable e) {
                        errors.add(e);
                } finally {
                        for (FrameworkMethod each : fCheckFors)
                                try {
                                        each.invokeExplosively(fTarget);
                                } catch (Throwable e) {
                                        errors.add(e);
                                }
                }
                MultipleFailureException.assertEmpty(errors);
        }
}