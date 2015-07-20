package io.scalac.slack.bots.voting

import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.common.{MessageFormatter, Command, OutboundMessage}
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
    case Command("vote-open", words, message) if words.length > 1 =>
      val parts =  words.mkString(" ").split(";")
      val voting = VotingTopic(parts.head, parts.tail, DateTime.now())
      val sessionId = repo.createSession(voting)

      log.info(s"New session $sessionId started with ${parts.mkString(" ")}")
      publish( OutboundMessage(message.channel,
        s"${mention(message.user)}: Voting session $sessionId started. Q: ${parts.head} $EOL" +
        s"A: ${parts.tail.mkString(EOL)}") )

    case Command("vote", sessionId :: answerIdStr :: _, message) =>
      log.info(s"${message.user} is voting on $answerIdStr in session $sessionId")
      val answerId = answerIdStr.toInt

      val response = repo.findSession(sessionId.toLong) match {
        case Some(session) if answerId < session.topic.answers.length =>
          val vote = Vote(message.user, answerId, DateTime.now())
          repo.addVote(sessionId, vote, session)
          OutboundMessage(message.channel, s"${message.user}: Vote in $sessionId for '${session.topic.answers(answerId)}' has been taken")

        case Some(session) if answerIdStr.toLong > session.topic.answers.length =>
          OutboundMessage(message.channel, s"${message.user}: Possible answers are ${session.topic.answers.zipWithIndex}")

        case None =>
          OutboundMessage(message.channel, s"No session with this Id: $sessionId")
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

object VotingBot {
  case class VotingTopic(question: String, answers: Array[String], asked: DateTime, isOpened: Boolean = true)
  case class Vote(voter: String, answer: Int, voted: DateTime)
  case class Session(topic: VotingTopic, votes: List[Vote])
}
