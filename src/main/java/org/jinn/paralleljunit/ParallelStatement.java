package org.jinn.paralleljunit;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class ParallelStatement extends Statement {

	private final FrameworkMethod fTestMethod;
	private Object fTarget;
	
	public ParallelStatement(FrameworkMethod testMethod, Object target) {
		fTestMethod= testMethod;
		fTarget= target;
	}
	
	@Override
	public void evaluate() throws Throwable {
		fTestMethod.invokeExplosively(fTarget);
	}
}
