package edu.sc.seis.sod.subsetter.eventArm;

import junit.framework.TestCase;
// JUnitDoclet end import

/**
* Generated by JUnitDoclet, a tool provided by
* ObjectFab GmbH under LGPL.
* Please see www.junitdoclet.org, www.gnu.org
* and www.objectfab.de for informations about
* the tool, the licence and the authors.
*/


public class PassOriginTest
// JUnitDoclet begin extends_implements
extends TestCase
// JUnitDoclet end extends_implements
{
  // JUnitDoclet begin class
  edu.sc.seis.sod.subsetter.origin.PassOrigin passorigin = null;
  // JUnitDoclet end class

  public PassOriginTest(String name) {
    // JUnitDoclet begin method PassOriginTest
    super(name);
    // JUnitDoclet end method PassOriginTest
  }

  public edu.sc.seis.sod.subsetter.origin.PassOrigin createInstance() throws Exception {
    // JUnitDoclet begin method testcase.createInstance
    return new edu.sc.seis.sod.subsetter.origin.PassOrigin();
    // JUnitDoclet end method testcase.createInstance
  }

  protected void setUp() throws Exception {
    // JUnitDoclet begin method testcase.setUp
    super.setUp();
    passorigin = createInstance();
    // JUnitDoclet end method testcase.setUp
  }

  protected void tearDown() throws Exception {
    // JUnitDoclet begin method testcase.tearDown
    passorigin = null;
    super.tearDown();
    // JUnitDoclet end method testcase.tearDown
  }

  public void testAccept() throws Exception {
    // JUnitDoclet begin method accept
      assertTrue(passorigin.accept(null, null, null));
    // JUnitDoclet end method accept
  }



  /**
  * JUnitDoclet moves marker to this method, if there is not match
  * for them in the regenerated code and if the marker is not empty.
  * This way, no test gets lost when regenerating after renaming.
  * Method testVault is supposed to be empty.
  */
  public void testVault() throws Exception {
    // JUnitDoclet begin method testcase.testVault
    // JUnitDoclet end method testcase.testVault
  }

  public static void main(String[] args) {
    // JUnitDoclet begin method testcase.main
    junit.textui.TestRunner.run(PassOriginTest.class);
    // JUnitDoclet end method testcase.main
  }
}
