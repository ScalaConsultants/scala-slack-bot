package io.scalac.slack.bots.voting

import io.scalac.slack.bots.voting.VotingBot.{VotingTopic, Vote, Session}

import scala.collection.mutable


trait VotingRepo {
  def findSession(sessionId: Long): Option[Session]
  def addVote(sessionId: String, vote: Vote, session: Session): Unit
  def closeSession(sessionId: Long, session: Session): Long
  def createSession(session: VotingTopic): Long
}

class InMemoryVotingRepo extends VotingRepo {
  var globalVotingId = 0L
  val sessionStorage = mutable.Map[Long, Session]()

  override def findSession(sessionId: Long): Option[Session] = {
    sessionStorage.get(sessionId)
  }

  override def addVote(sessionId: String, vote: Vote, session: Session): Unit = {
    sessionStorage += (sessionId.toLong -> Session(session.topic, vote :: session.votes))
  }

  override def closeSession(sessionId: Long, session: Session): Long = {
    sessionStorage += (sessionId -> Session(session.topic.copy(isOpened = false), session.votes))
    sessionId
  }

  override def createSession(session: VotingTopic) = {
    globalVotingId += 1
    sessionStorage += (globalVotingId -> Session(session, List.empty[Vote]))
    globalVotingId
  }
}
