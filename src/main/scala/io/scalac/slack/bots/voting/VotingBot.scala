package io.scalac.slack.bots.voting

import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.common.{Command, OutboundMessage}
import org.joda.time.DateTime

import scala.collection.mutable

/**
 * Maintainer: Mario
 */
class VotingBot(override val bus: MessageEventBus) extends AbstractBot {
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
      globalVotingId += 1
      val parts =  words.mkString(" ").split(";")
      val session = VotingTopic(parts.head, words.tail, DateTime.now())
      sessionStorage += (globalVotingId -> (session, List.empty[Vote]))

      log.info(s"New session $globalVotingId started with ${parts.mkString(" ")}")
      publish( OutboundMessage(message.channel,
        s"<${message.user}>: Voting session $globalVotingId started. Q: ${parts.head} \\n" +
        s"A: ${parts.tail.mkString("\\n")}") )

    case Command("vote", sessionId :: answer :: _, message) =>
      log.info(s"${message.user} is voting on $answer in session $sessionId")
      val response = sessionStorage.get(sessionId.toLong) match {
        case Some(session) if answer.toLong < session._1.answers.length =>
          val vote = Vote(message.user, answer.toInt, DateTime.now())
          sessionStorage += (sessionId.toLong -> (session._1, vote :: session._2))

          OutboundMessage(message.channel, s"${message.user}: Vote in $sessionId has been taken")
        case Some(session) if answer.toLong > session._1.answers.length =>
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
          
          val votingResults = session._2.groupBy(_.answer).map{
            case (answerId, votes) => 
              val answerText = session._1.answers(answerId)
              (s"$answerText VOTES #${votes.length} by ${votes.foldLeft("")(_ + " <" + _.voter + ">")}", votes.length)
          }.toList.sortBy(_._2).map(_._1)
          
          OutboundMessage(message.channel, s"Closed session $sessionId. Results \\n" +
            votingResults.mkString("\\n"))
          
        case None =>
          OutboundMessage(message.channel, s"No session with this Id: $sessionId")
      }
      publish(response)
  }
}

object VotingBot {
  case class VotingTopic(question: String, answers: List[String], asked: DateTime, isOpened: Boolean = true)
  case class Vote(voter: String, answer: Int, voted: DateTime)
  type Session = (VotingTopic, List[Vote])
}
