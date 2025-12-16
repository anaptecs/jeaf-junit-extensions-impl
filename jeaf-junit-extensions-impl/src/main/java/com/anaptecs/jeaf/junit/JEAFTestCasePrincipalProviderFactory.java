/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.junit;

import com.anaptecs.jeaf.xfun.api.principal.PrincipalProvider;
import com.anaptecs.jeaf.xfun.api.principal.PrincipalProviderFactory;

/**
 * Class implements a factory to create principal provider for JEAF JUnit environments.
 * 
 * @author JEAF Development Team
 */
public class JEAFTestCasePrincipalProviderFactory implements PrincipalProviderFactory {

  /**
   * Method returns {@link JEAFTestCasePrincipalProvider}.
   * 
   * @return {@link PrincipalProvider} Principal provider for JEAF JUnit environments. The method never returns null.
   */
  @Override
  public PrincipalProvider getPrincipalProvider( ) {
    return new JEAFTestCasePrincipalProvider();
  }
}
