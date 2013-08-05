import org.jinn.paralleljunit.ParallelSimpleRunner;
import org.jinn.paralleljunit.annotation.CheckMethod;
import org.jinn.paralleljunit.annotation.InitMethod;
import org.jinn.paralleljunit.annotation.ParallelConfig;
import org.jinn.paralleljunit.annotation.Threads;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;

@RunWith(ParallelSimpleRunner.class)
@ParallelConfig(threadNumber = { 2, 4, 6, 8 },threadPoolSize=5)
public class SimpleTest {

    @Before
    public void setUp() {
    }

    @Test
    public void doNothing() {
    	System.out.println("single thread doNothing");
    }

    @InitMethod("testThread")
    public void init() {
    	System.out.println("init data");
    }
  
    @Threads
    public void testThread() {
    	System.out.println("parallel threads testing start");
    }

    @CheckMethod("testThread")
    public void checkResult() {
    	System.out.println("checkresult");
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++)
            JUnitCore.runClasses(SimpleTest.class);
    }
}
