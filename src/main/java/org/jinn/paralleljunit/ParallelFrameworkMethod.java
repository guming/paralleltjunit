package org.jinn.paralleljunit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.junit.runners.model.FrameworkMethod;

/**
 *
 */
public class ParallelFrameworkMethod extends FrameworkMethod {
	private static final Logger log = Logger.getLogger(ParallelFrameworkMethod.class);
	private int fThreadNum;
	private int threadPoolSize;
	private Future<Object>[] futures;

	@SuppressWarnings("unchecked")
	public ParallelFrameworkMethod(Method method, int threadNum,int threadPoolSize) {
		super(method);
		this.fThreadNum = threadNum;
		futures = new Future[fThreadNum];
		this.threadPoolSize=threadPoolSize;
	}
	
	public int getThreadNum(){
		return fThreadNum;
	}

	@Override
	public Object invokeExplosively(final Object target, final Object... params)
			throws Throwable {
		
		long startTime = System.currentTimeMillis();
		
		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		for (int i = 0; i < fThreadNum; i++) {
			final int rank = i;
			futures[i] = executor.submit(new Callable<Object>() {
				public Object call() throws Exception {
					Method method = getMethod();
					List<Object> parameters = new ArrayList<Object>();
					parameters.addAll(Arrays.asList(params));
					parameters.add(rank);
					parameters.add(fThreadNum);
					return method.invoke(target);
				}
			});
		}

		List<Object> results = new ArrayList<Object>(fThreadNum);
		for (Future<Object> future : futures) {
			results.add(future.get());
		}

		long endTime = System.currentTimeMillis() - startTime;
		log.info("All Threads excute time>>>>>>>>>>>>>>" + endTime);
		return results;
	}

}
