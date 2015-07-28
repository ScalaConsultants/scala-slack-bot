package io.scalac.slack.bots.voting

import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.common.{BaseMessage, MessageFormatter, Command, OutboundMessage}
import org.joda.time.DateTime

/**
 * Maintainer: Patryk
 */
class VotingBot(repo: VotingRepo, override val bus: MessageEventBus) extends AbstractBot with MessageFormatter {
  import io.scalac.slack.bots.voting.VotingBot._

  override def help(channel: String): OutboundMessage = OutboundMessage(channel, 
    s"*$name* provides a voting mechanism, so everyone case express their opinion. $EOL" +
    s"`vote-open {semicolon ; separated list of question and possible answers}` - starts a voting session and returns it's id $EOL" +
    s"`vote {voting session id} {voting option, zero based number}` - send vote for given session $EOL" +
    s"`vote-close {voting session id} - closes the given session, presents outcomes`")

  ///TODO: add commands for displaying current session details (votes, question, options)
  
  override def act: Receive = {
    case Command("vote-open", words, message) if words.length >= 1 =>
      val parts =  words.mkString(" ").split(";")
      val sessionId = repo.createSession(parts.head, parts.tail)

      log.info(s"New session $sessionId started with ${parts.mkString(" ")}")
      publish( OutboundMessage(message.channel,
        formatOpenMessage(sessionId, message.user, parts)) )

    case Command("vote", sessionIdStr :: answerIdStr :: _, message) =>
      log.info(s"${message.user} is voting on $answerIdStr in session $sessionIdStr")
      val answerId = answerIdStr.toInt
      val sessionId = sessionIdStr.toLong

      val vote = Vote(message.user, answerId)
      val response = repo.addVote(sessionId, vote) match {
        case VoteResult.Voted =>
          OutboundMessage(message.channel, formatVoteMessage(sessionId, message.user, answerId))
        case VoteResult.NoAnswer =>
          OutboundMessage(message.channel, formatNoAnswerMessage(sessionId, message.user, answerId))
        case VoteResult.SessionClosed =>
          OutboundMessage(message.channel, formatSessionClosedMessage(sessionId, message.user))
        case VoteResult.NoSession =>
          OutboundMessage(message.channel, formatNoSessionMessage(sessionId, message.user))
      }
      publish(response)

    case Command("vote-close", sessionIdStr :: _, message) =>
      log.info(s"Session $sessionIdStr is going to be closed")
      val sessionId = sessionIdStr.toLong

      val response = repo.findSession(sessionId) match {
        case Some(session) =>
          repo.closeSession(sessionId, session)
          
          val votingResults = session.votes.groupBy(_.answer).map{ case (answerId, votes) =>
            val answerText = session.topic.answers(answerId)
            val voters = votes.foldLeft("")((acc, v) =>  acc + mention(v.voter))
            (s"$answerText VOTES #${votes.length} by $voters", votes.length)
          }.toList.sortBy(_._2).map(_._1)
          
          OutboundMessage(message.channel, s"Closed session $sessionIdStr. Results $EOL" +
            votingResults.mkString(EOL))
          
        case None =>
          OutboundMessage(message.channel, s"No session with this Id: $sessionIdStr")
      }
      publish(response)
  }
}

object VotingBot extends MessageFormatter {
  case class VotingTopic(question: String, answers: Array[String], asked: DateTime, isOpened: Boolean = true)
  case class Vote(voter: String, answer: Int)
  case class Session(topic: VotingTopic, votes: List[Vote])

  def formatOpenMessage(sessionId: Long, user: String, parts: Array[String]): String = {
    s"${mention(user)}: Voting session $sessionId started. Q: ${parts.head} $EOL" +
      s"A: ${parts.tail.mkString(EOL)}"
  }

  def formatVoteMessage(sessionId: Long, user: String, answerId: Int): String =
    s"$user: Vote in $sessionId for $answerId has been taken"

  def formatSessionClosedMessage(sessionId: Long, user: String): String =
    s"$user: Session $sessionId has been closed"

  def formatNoAnswerMessage(sessionId: Long, user: String, answerId: Int): String =
    s"$user: Session $sessionId has no answer $answerId"

  def formatNoSessionMessage(sessionId: Long, user: String): String =
    s"$user: No session with this Id: $sessionId"
}
