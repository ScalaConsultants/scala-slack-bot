package io.scalac.slack.exceptions

/**
 * Created on 21.01.15 16:23
 */
class SlackError extends Exception
class NotAuthenticated extends SlackError //no authentication token provided
class InvalidAuth extends SlackError //invalid auth token
class AccountInactive extends SlackError //token is for deleted user or team
class UnknownError(msg: String) extends Exception(msg)
