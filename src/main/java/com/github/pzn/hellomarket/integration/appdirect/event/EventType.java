package com.github.pzn.hellomarket.integration.appdirect.event;

/**
 * https://help.appdirect.com/appdistrib/Default.htm#Dev-DistributionGuide/en-subs-ev-notif-flow.html
 */
public enum EventType {

  // https://help.appdirect.com/appdistrib/Default.htm#Dev-DistributionGuide/en-create-subs.html
  SUBSCRIPTION_ORDER,

  // https://help.appdirect.com/appdistrib/Default.htm#Dev-DistributionGuide/en-change-subs.html
  SUBSCRIPTION_CHANGE,

  // https://help.appdirect.com/appdistrib/Default.htm#Dev-DistributionGuide/en-cancel-subs.html
  SUBSCRIPTION_CANCEL,

  // https://help.appdirect.com/appdistrib/Default.htm#Dev-DistributionGuide/en-subs-notice.html
  SUBSCRIPTION_NOTICE,

  // https://help.appdirect.com/appdistrib/Default.htm#Dev-DistributionGuide/en-ue-user-assign.html
  USER_ASSIGNMENT,

  // https://help.appdirect.com/appdistrib/Default.htm#Dev-DistributionGuide/en-ue-user-usassign.html
  USER_UNASSIGNMENT,
}
