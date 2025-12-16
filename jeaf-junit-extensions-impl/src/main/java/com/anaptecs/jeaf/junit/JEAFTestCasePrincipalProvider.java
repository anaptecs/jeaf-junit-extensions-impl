/**
 * Copyright 2004 - 2019 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.junit;

import java.security.Principal;

import javax.security.auth.Subject;

import com.anaptecs.jeaf.core.api.jaas.SubjectPrincipalProvider;

/**
 * Class implements a principal provider that is used together with JEAF Test Cases ({@link JEAFTestCase}). The
 * resolution of the current principal is based on the JAAS subject of the tests.
 * 
 * @author JEAF Development Team
 */
public class JEAFTestCasePrincipalProvider extends SubjectPrincipalProvider {
  /**
   * Method returns the current principal object. Therefore the method uses the JAAS subject of the JEAF application. If
   * no subject is available then the method returns ANONYMOUS.
   * 
   * @return {@link Principal} Principal of the user based on the current JAAS subject. The method never returns null.
   */
  @Override
  public Principal getCurrentPrincipal( ) {
    // Resolve JAAS subject from JEAF Application.
    Subject lSubject = JEAFTestCase.getCurrentUser();

    // Select right principal from subject and return it.
    return this.selectPrincipal(lSubject);
  }
}
