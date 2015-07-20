package io.scalac.slack.bots.voting

import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.common.{MessageFormatter, Command, OutboundMessage}
import org.joda.time.DateTime

import scala.collection.mutable

/**
 * Maintainer: Patryk
 */
class VotingBot(override val bus: MessageEventBus) extends AbstractBot with MessageFormatter {
  import io.scalac.slack.bots.voting.VotingBot._
  
  var globalVotingId = 0L
  val sessionStorage = mutable.Map[Long, Session]()
  
  override def help(channel: String): OutboundMessage = OutboundMessage(channel, 
    s"*$name* provides a voting mechanism, so everyone case express their opinion. \\n " +
    s"`vote-open {question} {semicolon ; separated list of possible answers}` - starts a voting session and returns it's id\\n" +
    s"`vote {voting session id} {voting option, zero based number}` - send vote for given session\\n" +
    s"`vote-close {voting session id} - closes the given session, presents outcomes`")

  ///TODO: add commands for displaying current session details (votes, question, options)
  
  override def act: Receive = {
    case aaa @ Command("vote-open", words, message) if words.length > 1 =>
      val parts =  words.mkString(" ").split(";")
      val voting = VotingTopic(parts.head, parts.tail, DateTime.now())
      println(s" === $voting")
      createSession(voting)

      log.info(s"New session $globalVotingId started with ${parts.mkString(" ")}")
      publish( OutboundMessage(message.channel,
        s"${mention(message.user)}: Voting session $globalVotingId started. Q: ${parts.head} $EOL" +
        s"A: ${parts.tail.mkString(EOL)}") )

    case Command("vote", sessionId :: answerIdStr :: _, message) =>
      log.info(s"${message.user} is voting on $answerIdStr in session $sessionId")
      val answerId = answerIdStr.toInt

      val response = sessionStorage.get(sessionId.toLong) match {
        case Some(session) if answerIdStr.toLong < session._1.answers.length =>
          val vote = Vote(message.user, answerId, DateTime.now())
          addVote(sessionId, vote, session)
          OutboundMessage(message.channel, s"${message.user}: Vote in $sessionId for '${session._1.answers(answerId)}' has been taken")

        case Some(session) if answerIdStr.toLong > session._1.answers.length =>
          OutboundMessage(message.channel, s"${message.user}: Possible answers are ${session._1.answers.zipWithIndex}")

        case None =>
          OutboundMessage(message.channel, s"No session with this Id: $sessionId")
      }
      publish(response)

    case Command("vote-close", sessionId :: _, message) =>
      log.info(s"Session $sessionId is going to be closed")
      val response = sessionStorage.get(sessionId.toLong) match {
        case Some(session) =>
          sessionStorage += (sessionId.toLong -> (session._1.copy(isOpened = false), session._2))
          
          val votingResults = session._2.groupBy(_.answer).map{ case (answerId, votes) =>
            val answerText = session._1.answers(answerId)
            val voters = votes.foldLeft("")((acc, v) =>  acc + mention(v.voter))
            (s"$answerText VOTES #${votes.length} by $voters", votes.length)
          }.toList.sortBy(_._2).map(_._1)
          
          OutboundMessage(message.channel, s"Closed session $sessionId. Results $EOL" +
            votingResults.mkString(EOL))
          
        case None =>
          OutboundMessage(message.channel, s"No session with this Id: $sessionId")
      }
      publish(response)
  }

  private def addVote(sessionId: String, vote: Vote, session: (VotingTopic, List[Vote])): Unit = {
    sessionStorage += (sessionId.toLong -> (session._1, vote :: session._2))
  }

  private def createSession(session: VotingTopic) = {
    globalVotingId += 1
    sessionStorage += (globalVotingId ->(session, List.empty[Vote]))
  }
}

object VotingBot {
  case class VotingTopic(question: String, answers: Array[String], asked: DateTime, isOpened: Boolean = true)
  case class Vote(voter: String, answer: Int, voted: DateTime)
  type Session = (VotingTopic, List[Vote])
}
