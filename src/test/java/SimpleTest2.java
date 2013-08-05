

import java.util.concurrent.ConcurrentHashMap;

import org.jinn.paralleljunit.ParallelSpringRunner;
import org.jinn.paralleljunit.annotation.CheckMethod;
import org.jinn.paralleljunit.annotation.InitMethod;
import org.jinn.paralleljunit.annotation.ParallelConfig;
import org.jinn.paralleljunit.annotation.Threads;
import org.jinn.paralleljunit.annotation.ThreadsMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;


@RunWith(ParallelSpringRunner.class)
@ContextConfiguration(locations={"classpath:/application-context.xml"}) 
@ParallelConfig(threadNumber = { 2, 4, 8 },threadPoolSize=5)
public class SimpleTest2 {
    ConcurrentHashMap<Integer, String> MapIntStr;

    @Before
    public void setUp() {
        MapIntStr = new ConcurrentHashMap<Integer, String>();
    }

    @InitMethod("testThread")
    public void putSomeData() {
    }
    @Test
    @ThreadsMethod(threadCount={3},threadPoolSize=4,profileOn=true)
    public void testThread2() {
		System.out.println("spring runner testThread2");
    }
    
    @Test
    @Threads
    public void testThread() {
    	System.out.println("spring runner testThread");
    }
    

    @CheckMethod("testThread2")
    public void checkResult() {
    	System.out.println("check result");
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++)
            JUnitCore.runClasses(SimpleTest2.class);
    }
}
