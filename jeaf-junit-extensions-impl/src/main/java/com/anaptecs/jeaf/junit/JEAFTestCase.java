/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.junit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import com.anaptecs.jeaf.core.api.JEAF;
import com.anaptecs.jeaf.core.api.ServiceObjectID;
import com.anaptecs.jeaf.core.api.jaas.JAASConstants;
import com.anaptecs.jeaf.core.api.jaas.JEAFCallbackHandler;
import com.anaptecs.jeaf.core.servicechannel.JEAFCore;
import com.anaptecs.jeaf.core.servicechannel.api.SessionContextManager;
import com.anaptecs.jeaf.xfun.api.XFun;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.common.Identifiable;
import com.anaptecs.jeaf.xfun.api.config.Configuration;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Class is the base class of all JUnit Test cases that are executed within a JEAF environment. This class ensures that
 * the required environment to use JEAF has be set up completely.
 * 
 * @author JEAF Development Team
 * @version $LastChangedRevision: 1226 $
 * 
 */
public abstract class JEAFTestCase extends TestCase {
  /**
   * Login name of the user under whose context the tests are run.
   */
  private String loginName;

  /**
   * Password of the user which is used to run the tests.
   */
  private String password;

  /**
   * Attribute defines if the test case should perform an automatic login for each test case if a login module is
   * defined or not.
   */
  private final boolean automaticLogin;

  /**
   * Login context represents the logged in user of the test.
   */
  private static LoginContext loginContext;

  /**
   * Initialize test case. Therefore the name of the test that should be executed must be provided.
   * 
   * @param pName Name of the test method that should be executed by this test. The Parameter must not be null.
   */
  public JEAFTestCase( String pName ) {
    this(pName, true);
  }

  /**
   * Initialize test case. Therefore the name of the test that should be executed must be provided.
   * 
   * @param pName Name of the test method that should be executed by this test. The Parameter must not be null.
   * @param pAutomaticLogin Parameter defines if the test cases should perform an automatic login if a login module is
   * defined or not.
   */
  public JEAFTestCase( String pName, boolean pAutomaticLogin ) {
    super(pName);
    Configuration lConfiguration = XFun.getConfigurationProvider().getSystemPropertiesConfiguration();
    loginName = lConfiguration.getConfigurationValue("jeaf.user", null, String.class);
    password = lConfiguration.getConfigurationValue("jeaf.password", "", String.class);
    automaticLogin = pAutomaticLogin;
  }

  /**
   * Initialize test case. Therefore the name of the test that should be executed must be provided. The userId and the
   * password of a specific user can be given to this Constructor so this specific user can be loged in.
   * 
   * @param pName Name of the test method that should be executed by this test. The Parameter must not be null.
   * @param pUserId UserId of the User who should be logged in for this Test.
   * @param pPassword Password of the User who should be logged in for this Test.
   */
  public JEAFTestCase( String pName, String pUserId, String pPassword ) {
    this(pName, pUserId, pPassword, true);
  }

  /**
   * Initialize test case. Therefore the name of the test that should be executed must be provided. The userId and the
   * password of a specific user can be given to this Constructor so this specific user can be loged in.
   * 
   * @param pName Name of the test method that should be executed by this test. The Parameter must not be null.
   * @param pUserId UserId of the User who should be logged in for this Test.
   * @param pPassword Password of the User who should be logged in for this Test.
   * @param pAutomaticLogin Parameter defines if the test cases should perform an automatic login if a login module is
   * defined or not.
   */
  public JEAFTestCase( String pName, String pUserId, String pPassword, boolean pAutomaticLogin ) {
    super(pName);
    loginName = pUserId;
    password = pPassword;
    automaticLogin = pAutomaticLogin;
  }

  /**
   * Method performs the initialization of the test case. If JEAF has not yet been initialized the life cycle manager is
   * requested to do this. If a concrete implementation of a test cases requires special init actions the init method
   * has to be overridden.
   * 
   * @throws Exception if an error occurs during initialization of JEAF.
   * @see junit.framework.TestCase#setUp()
   * @see #init()
   */
  @Override
  protected final void setUp( ) throws Exception {
    // Call super class implementation of this method.
    super.setUp();

    XFun.getTrace().info("Classpath: " + System.getProperty("java.class.path"));

    // Ensure that JEAF is loaded before we proceed.
    JEAF.load();

    // Inject dependencies to all referenced services.
    JEAF.injectDependencies(this);

    // Use new session context.
    SessionContextManager lContextManager =
        JEAFCore.getInstance().getLifecycleManager().getContextManager().getSessionContextManager();
    if (lContextManager instanceof JUnitSessionContextManager) {
      ((JUnitSessionContextManager) lContextManager).newSessionContext();
    }

    // As soon as a login module is configured, JEAF will try to login the defined user.
    if (automaticLogin == true && this.isLoginModuleDefined()) {
      this.login(loginName, password);
    }

    // Call init method of test case in order to perform test cases specific initializations.
    this.init();
  }

  /**
   * Method performs a JAAS login with the passed user name and password.
   * 
   * @param pUserName Name of the user to login. The parameter must not be null.
   * @param pPassword Password of the user.
   * @throws LoginException if the login fails
   */
  protected void login( String pUserName, String pPassword ) throws LoginException {
    // Create JEAF specific callback handler and perform JASS login.
    CallbackHandler lCallbackHandler = new JEAFCallbackHandler(pUserName, pPassword);
    loginContext = new LoginContext("JEAFSecurity", lCallbackHandler);
    loginContext.login();
  }

  /**
   * Method performs logout of the current user.
   * 
   * @throws LoginException if the logout fails.
   */
  protected void logout( ) throws LoginException {
    loginContext.logout();
  }

  /**
   * Method returns the subject of the current user.
   * 
   * @return {@link Subject} Subject representing the current user. If no user is logged in then the method returns
   * null.
   */
  static Subject getCurrentUser( ) {
    Subject lSubject;
    if (loginContext != null) {
      lSubject = loginContext.getSubject();
    }
    else {
      lSubject = null;
    }
    return lSubject;
  }

  /**
   * Method checks whether a JASS Login Module is defined or not.
   * 
   * @return boolean The method returns true if a JAAS login module is defined and false in all other cases.
   */
  private boolean isLoginModuleDefined( ) {
    return System.getProperty(JAASConstants.LOGIN_MODULE_PROPERTY) != null;
  }

  /**
   * Method performs cleanup routines for the test cases. If a concrete implementation of a test cases requires special
   * cleanup actions the cleanup method has to be overridden.
   * 
   * @throws Exception if an error occurs during cleanup.
   * @see junit.framework.TestCase#tearDown()
   * @see #cleanup()
   */
  @Override
  protected final void tearDown( ) throws Exception {
    // Call cleanup method in order to perform test case specific cleanup routines.
    this.cleanup();

    // Logout current user.
    if (automaticLogin == true && this.isLoginModuleDefined() == true) {
      this.logout();
    }

    // Call super class implementation of this method.
    super.tearDown();
  }

  /**
   * Method returns the login name of the user under which this test case is run.
   * 
   * @return {@link String} Login name of the current user. The method never returns null.
   */
  public String getLoginName( ) {
    return loginName;
  }

  /**
   * Method performs test case specific initialization routines. If a test case implementation requires some special
   * operations at startup this method can be overridden.
   * 
   * @throws Exception if an error occurs during initialization.
   */
  protected void init( ) throws Exception {
    // Nothing to do.
  }

  /**
   * Method performs test case specific cleanup routines. If a test case implementation requires some special operations
   * at cleanup this method can be overridden.
   * 
   * @throws Exception if an error occurs during initialization.
   */
  protected void cleanup( ) throws Exception {
    // Nothing to do.
  }

  /**
   * Method checks if the passed list contains the passed service object. Therefore the service objects inside the list
   * and the expected one are compared by their unversioned service object id.
   * 
   * @param pMessage Message that will be thrown if the assertion fails.
   * @param pServiceObjectList Collection with service objects. The parameter must not be null.
   * @param pExpectedServiceObject Service object that is expected to be part of the passed collection. The parameter
   * must not be null.
   */
  public static void assertContainsServiceObject( String pMessage,
      Collection<? extends Identifiable<ServiceObjectID>> pServiceObjectList,
      Identifiable<ServiceObjectID> pExpectedServiceObject ) {

    // Check if service object is not in list.
    JEAFTestCase.assertContainsServiceObject(pMessage, pServiceObjectList, pExpectedServiceObject, true);
  }

  /**
   * Method checks if the passed list does not contain the passed service object. Therefore the service objects inside
   * the list and the expected one are compared by their unversioned service object id.
   * 
   * @param pMessage Message that will be thrown if the assertion fails.
   * @param pServiceObjectList Collection with service objects. The parameter must not be null.
   * @param pExpectedServiceObject Service object that is expected to be part of the passed collection. The parameter
   * must not be null.
   */
  public static void assertNotContainsServiceObject( String pMessage,
      Collection<? extends Identifiable<ServiceObjectID>> pServiceObjectList,
      Identifiable<ServiceObjectID> pExpectedServiceObject ) {

    // Check if service object is not in list.
    JEAFTestCase.assertContainsServiceObject(pMessage, pServiceObjectList, pExpectedServiceObject, false);
  }

  /**
   * Method checks if the passed list does not contain the passed service object. Therefore the service objects inside
   * the list and the expected one are compared by their unversioned service object id.
   * 
   * @param pMessage Message that will be thrown if the assertion fails.
   * @param pServiceObjectList Collection with service objects. The parameter must not be null.
   * @param pExpectedServiceObject Service object that is expected to be part of the passed collection. The parameter
   * must not be null.
   * @param pExpected Parameter defines the expectation.
   */
  public static void assertContainsServiceObject( String pMessage,
      Collection<? extends Identifiable<ServiceObjectID>> pServiceObjectList,
      Identifiable<ServiceObjectID> pExpectedServiceObject, boolean pExpected ) {

    // Check parameters.
    Check.checkInvalidParameterNull(pServiceObjectList, "pServiceObjectList");
    Check.checkInvalidParameterNull(pExpectedServiceObject, "pExpectedServiceObject");

    // Add all service objects into a map with their id as key.
    Map<ServiceObjectID, Identifiable<ServiceObjectID>> lServiceObjectMap =
        new HashMap<ServiceObjectID, Identifiable<ServiceObjectID>>();
    for (Identifiable<ServiceObjectID> lNextServiceObject : pServiceObjectList) {
      lServiceObjectMap.put(lNextServiceObject.getUnversionedID(), lNextServiceObject);
    }

    // Check if service object id is known.
    if (lServiceObjectMap.containsKey(pExpectedServiceObject.getUnversionedID()) != pExpected) {
      if (pMessage != null) {
        throw new AssertionFailedError(pMessage);
      }
      else {
        throw new AssertionFailedError();
      }
    }
  }
}
