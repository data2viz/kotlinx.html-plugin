
package com.intellij.testFramework;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ThrowableRunnable;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.model.Statement;

/**
 * @author gregsh
 */
@RunWith(JUnit4.class)
public abstract class LightPlatform4TestCase extends LightPlatformTestCase {
  @Rule
  public TestRule rule = (base, description) -> new Statement() {
    @Override
    public void evaluate() {
      TestRunnerUtil.replaceIdeEventQueueSafely();
      String name = description.getMethodName();
      setName(name.startsWith("test") ? name : "test" + StringUtil.capitalize(name));
      EdtTestUtil.runInEdtAndWait((ThrowableRunnable<Throwable>) () -> {
        setUp();
        try {
          base.evaluate();
        }
        finally {
          tearDown();
        }
      });
    }
  };
}