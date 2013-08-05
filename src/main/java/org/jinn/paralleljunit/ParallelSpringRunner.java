package org.jinn.paralleljunit;

import java.util.ArrayList;
import java.util.List;

import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.jinn.paralleljunit.annotation.CheckMethod;
import org.jinn.paralleljunit.annotation.InitMethod;
import org.jinn.paralleljunit.annotation.ParallelConfig;
import org.jinn.paralleljunit.annotation.Threads;
import org.jinn.paralleljunit.annotation.ThreadsMethod;

/**
 * 
 */
public class ParallelSpringRunner extends SpringJUnit4ClassRunner {
	    private static int DEFAULT_POOL_SIZE=5;
        private List<FrameworkMethod> threadedTests;
        int[] threadNums;

        public ParallelSpringRunner(Class<?> klass) throws InitializationError {
                super(klass);
        }

        private int[] getParallelSetting(Class<?> klass) {
        	ParallelConfig setting = klass.getAnnotation(ParallelConfig.class);
                if (setting != null) {
                        return setting.threadNumber();
                }
                return null;
        }
        
        private int getParallelPoolSize(Class<?> klass) {
        	ParallelConfig setting = klass.getAnnotation(ParallelConfig.class);
            if (setting != null) {
                    return setting.threadPoolSize();
            }
            return DEFAULT_POOL_SIZE;
        }

        private java.util.List<org.junit.runners.model.FrameworkMethod> testMethods;

        protected java.util.List<org.junit.runners.model.FrameworkMethod> computeTestMethods() {
        		int threadPoolSize=DEFAULT_POOL_SIZE;
                if (testMethods != null)
                        return testMethods;
                java.util.List<org.junit.runners.model.FrameworkMethod> methods = super.computeTestMethods();
                threadedTests = getTestClass().getAnnotatedMethods(Threads.class);
                List<FrameworkMethod> tests = new ArrayList<FrameworkMethod>();
                if (threadNums == null){
                        threadNums = getParallelSetting(getTestClass().getJavaClass());
                        threadPoolSize=getParallelPoolSize(getTestClass().getJavaClass());
                }
                for (FrameworkMethod method : threadedTests) {
                        for (int thread : threadNums) {
                                tests.add(new ParallelFrameworkMethod(method.getMethod(),thread,threadPoolSize));
                        }
                }
                methods.addAll(tests);
                testMethods = methods;
                return methods;
        }

        @Override
        protected String testName(FrameworkMethod method) {
                if (method instanceof ParallelFrameworkMethod) {
                        ParallelFrameworkMethod pmethod = (ParallelFrameworkMethod) method;
                        return pmethod.getName() + ":" + pmethod.getThreadNum();
                }
                return super.testName(method);
        }

        @Override
        protected void runChild(FrameworkMethod method, RunNotifier notifier) {

        	if (method instanceof ParallelFrameworkMethod) {
                        ParallelFrameworkMethod pmethod = (ParallelFrameworkMethod) method;
                        Description description = describeChild(method);
                        EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);

                        eachNotifier.fireTestStarted();
                        try {
                                Statement methodBlock = methodBlock(pmethod);
                                methodBlock.evaluate();
                        } catch (AssumptionViolatedException e) {
                                eachNotifier.addFailedAssumption(e);
                        } catch (Throwable e) {
                                eachNotifier.addFailure(e);
                        } finally {
                                eachNotifier.fireTestFinished();
                        }
                } else {
                		runMethodByThreads(method, notifier);
                        super.runChild(method, notifier);
                }
        	
        }

        protected void runMethodByThreads(FrameworkMethod method, RunNotifier notifier){
        	ThreadsMethod tr=method.getAnnotation(ThreadsMethod.class);
        	if(null!=tr){
        		int[] threadsCount=tr.threadCount();
        		int poolsize=tr.threadPoolSize();
        		 for (int num : threadsCount) {
	        		 ParallelFrameworkMethod pmethod = new ParallelFrameworkMethod(method.getMethod(), num,poolsize);
	                 Description description = describeChild(method);
	                 EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
	
	                 eachNotifier.fireTestStarted();
	                 try {
	                         Statement methodBlock = methodBlock(pmethod);
	                         methodBlock.evaluate();
	                 } catch (AssumptionViolatedException e) {
	                         eachNotifier.addFailedAssumption(e);
	                 } catch (Throwable e) {
	                         eachNotifier.addFailure(e);
	                 } finally {
	                         eachNotifier.fireTestFinished();
	                 }
        		 }
        	}
        }
        
        
        protected Statement methodBlock(FrameworkMethod method) {
                if (method instanceof ParallelFrameworkMethod) {
                        ParallelFrameworkMethod pmethod = (ParallelFrameworkMethod) method;
                        Object test;
                        try {
                                test = new ReflectiveCallable() {
                                        @Override
                                        protected Object runReflectiveCall() throws Throwable {
                                                return createTest();
                                        }
                                }.run();
                        } catch (Throwable e) {
                                return new Fail(e);
                        }

                        Statement statement = methodInvoker(method, test);
                        statement = withInitMethod(pmethod, test, statement);
                        statement = withBefores(pmethod, test, statement);
                        statement = withCheckMethod(pmethod, test, statement);
                        statement = withAfters(pmethod, test, statement);
                        return statement;
                } else
                        return super.methodBlock(method);
        }

        private Statement withCheckMethod(ParallelFrameworkMethod method, Object target, Statement statement) {
                List<FrameworkMethod> initfors = getTestClass().getAnnotatedMethods(CheckMethod.class);
                String methodName = method.getName();
                List<FrameworkMethod> results = new ArrayList<FrameworkMethod>();
                for (FrameworkMethod fm : initfors) {
                	CheckMethod anno = fm.getAnnotation(CheckMethod.class);
                        if (anno.value().equals(methodName)) {
                                results.add(fm);
                        }
                }
                return results.isEmpty() ? statement : new RunCheckMethod(statement, results, target);
        }

        private Statement withInitMethod(ParallelFrameworkMethod method, Object target, Statement statement) {
                List<FrameworkMethod> checkFors = getTestClass().getAnnotatedMethods(InitMethod.class);
                String methodName = method.getName();
                List<FrameworkMethod> results = new ArrayList<FrameworkMethod>();
                for (FrameworkMethod fm : checkFors) {
                	InitMethod anno = fm.getAnnotation(InitMethod.class);
                        if (anno.value().equals(methodName)) {
                                results.add(fm);
                        }
                }
                return results.isEmpty() ? statement : new RunInitMethod(statement, results, target);
        }
}
