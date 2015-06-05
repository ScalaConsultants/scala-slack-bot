package io.scalac.slack.bots.linkedin

import java.io.{InputStreamReader, BufferedReader}

import com.google.code.linkedinapi.client.LinkedInApiClientFactory
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory
import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.common.{AbstractRepository, Command, OutboundMessage}
import org.joda.time.{DateTimeZone, DateTime}

/**
 * Maintainer: Patryk
 */
class LinkedInBot(
    messenger: LinkedInMessenger,
    repo: LinkedInRepository,
    override val bus: MessageEventBus) extends AbstractBot {

  override def act = {
    case Command("li-post", params, message) =>
      val formattedTweet = params.mkString(" ").replaceAll("\\\\@", "@").replaceAll("\\\\#", "#")
      log.debug(s"Got x= twitter-post $formattedTweet from Slack")
      messenger.post(formattedTweet)
      repo.create(formattedTweet, message.user)
      publish(OutboundMessage(message.channel, s"LinkedIn message '$formattedTweet' has been posted it's our ${repo.count()} update"))
  }

  override def help(channel: String): OutboundMessage = OutboundMessage(channel,
    s"Thanks to *${name}* one can post to LinkedIn as Scalac. \\n " +
      s"`li-post {message}` - posts the given message to LinkedIn from company account")
}

class LinkedInMessenger(
    consumerKey: String,
    consumerKeySecret: String,
    accessTokenN: String,
    accessTokenSecret: String) {

//  private val factory = LinkedInApiClientFactory.newInstance(consumerKey, consumerKeySecret)
//  private val client = factory.createLinkedInApiClient(accessToken, accessTokenSecret)

  ////
  val consumerKeyValue = "778dr971zag7yd" //line.getOptionValue(CONSUMER_KEY_OPTION);
  val consumerSecretValue = "JZV096ETHtvf2I9t" //line.getOptionValue(CONSUMER_SECRET_OPTION);

  val oauthService = LinkedInOAuthServiceFactory.getInstance().createLinkedInOAuthService(consumerKeyValue, consumerSecretValue);

  System.out.println("Fetching request token from LinkedIn...");

  val requestToken = oauthService.getOAuthRequestToken();

  val authUrl = requestToken.getAuthorizationUrl();

  System.out.println("Request token: " + requestToken.getToken());
  System.out.println("Token secret: " + requestToken.getTokenSecret());
  System.out.println("Expiration time: " + requestToken.getExpirationTime());

  System.out.println("Now visit:\n" + authUrl
    + "\n... and grant this app authorization");
  System.out.println("Enter the PIN code and hit ENTER when you're done:");

  val br = new BufferedReader(new InputStreamReader(System.in));
  val pin = br.readLine();

  System.out.println("Fetching access token from LinkedIn...");

  val accessToken = oauthService.getOAuthAccessToken(requestToken, pin);

  System.out.println("Access token: " + accessToken.getToken());
  System.out.println("Token secret: " + accessToken.getTokenSecret());

  val factory = LinkedInApiClientFactory.newInstance(consumerKeyValue, consumerSecretValue);
  val client = factory.createLinkedInApiClient(accessToken);

  System.out.println("Fetching profile for current user.");
  val profile = client.getProfileForCurrentUser();
  println(" ==== Profile is " + profile);
  ///////

  ////
  /* val linkedinKey = "778dr971zag7yd";    //add your LinkedIn key
  val linkedinSecret = "JZV096ETHtvf2I9t"; //add your LinkedIn Secret

  System.out.println("Fetching request token from LinkedIn...");

  val oauthService= LinkedInOAuthServiceFactory.getInstance().createLinkedInOAuthService(linkedinKey,linkedinSecret);
  val requestToken= oauthService.getOAuthRequestToken();
  val authToken= requestToken.getToken();
  val authTokenSecret = requestToken.getTokenSecret();

  System.out.println("Request token " +requestToken);
  System.out.println("Auth token" +authToken);
  System.out.println("Auth token secret" +authTokenSecret);

  val authUrl = requestToken.getAuthorizationUrl();

  System.out.println("Copy below link in web browser to authorize. Copy the PIN obtained\n" + authUrl);
  System.out.println("Enter the PIN code:");

  try
  {

    val s = new java.util.Scanner(System.in);
    val pin = s.next();
    System.out.println("Fetching access token from LinkedIn...");

    val accessToken =  oauthService.getOAuthAccessToken(requestToken, pin);
    System.out.println("Access token : " +  accessToken.getToken());
    System.out.println("Token secret : " +  accessToken.getTokenSecret());
    val factory =  LinkedInApiClientFactory.newInstance(linkedinKey,linkedinSecret);
    val client =  factory.createLinkedInApiClient(accessToken);

  //posting status to profile
  client.updateCurrentStatus("LinkedIN API is cool!");

  }

  finally
  {
    System.out.println("Updated status!");
  }*/
  ////

  def post(message: String) = () //client.updateCurrentStatus(message)
}

class LinkedInRepository() extends AbstractRepository {

  import scala.slick.driver.H2Driver.simple._
  import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

  /// definitions
  override val bucket = "LinkedInBot"

  private class PublishedUpdate(tag: Tag) extends Table[(Long, String, String, Long)](tag, s"${bucket}_PublishedUpdate") {
    def id = column[Long]("PublishedUpdateId", O.PrimaryKey, O.AutoInc)
    def text = column[String]("Text")
    def author = column[String]("Author")
    def added = column[Long]("Added")
    def * = (id, text, author, added)
  }
  private val published = TableQuery[PublishedUpdate]

  db.withDynSession {
    if(migrationNeeded())
      published.ddl.create
  }

  //public methods

  def create(msg: String, user: String) = {
    db.withDynSession{
      val when = new DateTime(DateTimeZone.UTC).getMillis
      published.insert((-1L, msg, user, when))
    }
  }
  def count() = db.withDynSession{
    published.list.length //TODO: not efficient
  }
}