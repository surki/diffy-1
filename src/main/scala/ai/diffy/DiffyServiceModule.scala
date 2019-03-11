package ai.diffy

import com.google.inject.Provides
import com.twitter.inject.TwitterModule
import com.twitter.util.TimeConversions._
import java.net.InetSocketAddress
import javax.inject.Singleton

import ai.diffy.analysis.{InMemoryDifferenceCollector, InMemoryDifferenceCounter, NoiseDifferenceCounter, RawDifferenceCounter}
import ai.diffy.proxy.{Settings, Target}
import com.twitter.util.Duration

object DiffyServiceModule extends TwitterModule {
  val datacenter =
    flag("dc", "localhost", "the datacenter where this Diffy instance is deployed")

  val servicePort =
    flag("proxy.port", new InetSocketAddress(9992), "The port where the proxy service should listen")

  val candidatePath =
    flag[String]("candidate", "candidate serverset where code that needs testing is deployed")

  val primaryPath =
    flag[String]("master.primary", "primary master serverset where known good code is deployed")

  val secondaryPath =
    flag[String]("master.secondary", "secondary master serverset where known good code is deployed")

  val candidateHeaders =
    flag[String]("candidate.headers", "", "Headers to pass tocandidate server. Ex: key1:value1,key2:value2")

  val primaryHeaders =
    flag[String]("primary.headers", "", "Headers to pass to primary server. Ex: key1:value1,key2:value2")

  val secondaryHeaders =
    flag[String]("secondary.headers", "", "Headers to pass to secondary server. Ex: key1:value1,key2:value2")

  val protocol =
    flag[String]("service.protocol", "Service protocol: thrift, http or https")

  val clientId =
    flag[String]("proxy.clientId", "diffy.proxy", "The clientId to be used by the proxy service to talk to candidate, primary, and master")

  val pathToThriftJar =
    flag[String]("thrift.jar", "path/to/thrift.jar", "The path to a fat Thrift jar")

  val serviceClass =
    flag[String]("thrift.serviceClass", "UserService", "The service name within the thrift jar e.g. UserService")

  val serviceName =
    flag[String]("serviceName", "Gizmoduck", "The service title e.g. Gizmoduck")

  val apiRoot =
    flag[String]("apiRoot", "", "The API root the front end should ping, defaults to the current host")

  val enableThriftMux =
    flag[Boolean]("enableThriftMux", true, "use thrift mux server and clients")

  val relativeThreshold =
    flag[Double]("threshold.relative", 20.0, "minimum (inclusive) relative threshold that a field must have to be returned")

  val absoluteThreshold =
    flag[Double]("threshold.absolute", 0.03, "minimum (inclusive) absolute threshold that a field must have to be returned")

  val teamEmail =
    flag[String]("notifications.targetEmail", "diffy-team@twitter.com", "team email to which cron report should be sent")

  val emailDelay =
    flag[Duration]("notifications.delay", 4.hours, "duration to wait before sending report out. e.g. 30.minutes or 4.hours")

  val rootUrl =
    flag[String]("rootUrl", "", "Root url to access this service, e.g. diffy-staging-gizmoduck.service.smf1.twitter.com")

  val allowHttpSideEffects =
    flag[Boolean]("allowHttpSideEffects", false, "Ignore POST, PUT, and DELETE requests if set to false")

  val excludeHttpHeadersComparison =
    flag[Boolean]("excludeHttpHeadersComparison", false, "Exclude comparison on HTTP headers if set to false")

  val skipEmailsWhenNoErrors =
    flag[Boolean]("skipEmailsWhenNoErrors", false, "Do not send emails if there are no critical errors")

  val httpsPort =
    flag[String]("httpsPort", "443", "Port to be used when using HTTPS as a protocol")

  val thriftFramedTransport =
    flag[Boolean]("thriftFramedTransport", true, "Run in BufferedTransport mode when false")
  @Provides
  @Singleton
  def settings =
    Settings(
      datacenter(),
      servicePort(),
      Target(candidatePath(), candidateHeaders()),
      Target(primaryPath(), primaryHeaders()),
      Target(secondaryPath(), secondaryHeaders()),
      protocol(),
      clientId(),
      pathToThriftJar(),
      serviceClass(),
      serviceName(),
      apiRoot(),
      enableThriftMux(),
      relativeThreshold(),
      absoluteThreshold(),
      teamEmail(),
      emailDelay(),
      rootUrl(),
      allowHttpSideEffects(),
      excludeHttpHeadersComparison(),
      skipEmailsWhenNoErrors(),
      httpsPort(),
      thriftFramedTransport()
    )

  @Provides
  @Singleton
  def providesRawCounter = RawDifferenceCounter(new InMemoryDifferenceCounter)

  @Provides
  @Singleton
  def providesNoiseCounter = NoiseDifferenceCounter(new InMemoryDifferenceCounter)

  @Provides
  @Singleton
  def providesCollector = new InMemoryDifferenceCollector
}
