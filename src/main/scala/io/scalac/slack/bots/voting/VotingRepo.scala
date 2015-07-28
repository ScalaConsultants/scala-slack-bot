package io.scalac.slack.bots.voting

import io.scalac.slack.bots.voting.VotingBot.{VotingTopic, Vote, Session}
import org.joda.time.DateTime

import scala.collection.mutable

object VoteResult extends Enumeration {
  type VoteResult = Value
  val Voted, NoSession, SessionClosed, NoAnswer = Value
}

trait VotingRepo {
  def findSession(sessionId: Long): Option[Session]
  def addVote(sessionId: Long, vote: Vote): VoteResult.Value
  def closeSession(sessionId: Long, session: Session): Long
  def createSession(question: String, answers: Array[String]): Long
}

class InMemoryVotingRepo extends VotingRepo {

  import VoteResult._

  var globalVotingId = 0L
  val sessionStorage = mutable.Map[Long, Session]()

  override def findSession(sessionId: Long): Option[Session] =
    sessionStorage.get(sessionId)

  override def addVote(sessionId: Long, vote: Vote): VoteResult.Value =
    findSession(sessionId) match {
      case Some(session) if !session.topic.isOpened =>
        SessionClosed
      case Some(session) if !legalVote(vote, session) =>
        NoAnswer
      case Some(session) if legalVote(vote, session)=>
        sessionStorage += (sessionId.toLong -> Session(session.topic, vote :: session.votes))
        Voted
      case _ =>
        NoSession
    }

  def legalVote(vote: Vote, session: Session): Boolean =
    session.topic.answers.length > vote.answer

  override def closeSession(sessionId: Long, session: Session): Long = {
    sessionStorage += (sessionId -> Session(session.topic.copy(isOpened = false), session.votes))
    sessionId
  }

  override def createSession(question: String, answers: Array[String]) = {
    val voting = VotingTopic(question, answers, DateTime.now())
    globalVotingId += 1
    sessionStorage += (globalVotingId -> Session(voting, List.empty[Vote]))
    globalVotingId
  }
}
