/**
 * Copyright 2004 - 2013 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * All rights reserved.
 */
package com.anaptecs.jeaf.junit;

import com.anaptecs.jeaf.core.api.SessionContext;
import com.anaptecs.jeaf.core.servicechannel.api.SessionContextManager;

/**
 * Class implements a session context manager that can be used inside JUnit tests.
 * 
 * @author JEAF Development Team
 * @version JEAF Release 1.3
 */
public class JUnitSessionContextManager implements SessionContextManager {
  /**
   * Single user session context.
   */
  private SessionContext sessionContext = new SessionContext();

  @Override
  public synchronized SessionContext getSessionContext( ) {
    return sessionContext;
  }

  /**
   * Method overrides the currently used session context by a completely new one.
   */
  synchronized void newSessionContext( ) {
    sessionContext = new SessionContext();
  }

  @Override
  public boolean isSessionContextAvailable( ) {
    return true;
  }
}
