package com.tomshley.www.contactnotify

import com.tomshley.hexagonal.lib.kafka.util.ProducerProtoBoilerplate
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.apache.pekko.Done
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.kafka.scaladsl.{Committer, Consumer}
import org.apache.pekko.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import org.apache.pekko.stream.RestartSettings
import org.apache.pekko.stream.scaladsl.RestartSource

import scala.annotation.tailrec
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future, Promise}

object ContactEventConsumer {
  private final val serviceName = ConfigFactory.load().getString("service-name")
  private val publicationRecordsEventsInstance: Promise[Future[Done]] = Promise()

  private def consumerInstance(system: ActorSystem[?], serviceName:String, kafkaTopic:String, handler:ContactEventConsumerHandler) = {
    given sys: ActorSystem[?] = system
    given ec: ExecutionContext = system.executionContext

    val consumerSettings =
      ConsumerSettings(
        system,
        new StringDeserializer,
        new ByteArrayDeserializer).withGroupId(serviceName)
    val committerSettings = CommitterSettings(system)

    RestartSource
      .onFailuresWithBackoff(
        RestartSettings(
          minBackoff = 1.second,
          maxBackoff = 30.seconds,
          randomFactor = 0.1)) { () =>
        Consumer
          .committableSource(
            consumerSettings,
            Subscriptions.topics(kafkaTopic)
          )
          .mapAsync(1) { msg =>
            handler.process(msg.record).map(_ => msg.committableOffset)
          }
          .via(Committer.flow(committerSettings))
      }
      .run()
  }
  @tailrec
  def init(system: ActorSystem[?]): Future[Done] = {
    publicationRecordsEventsMaybe match
      case Some(value) => value
      case None =>
        val kafkaTopic = system.settings.config.getString("www-tomshley-com-contactnotify-service.kafka.topic")

        publicationRecordsEventsInstance.trySuccess{
          consumerInstance(system, serviceName, kafkaTopic, new ContactEventConsumerHandler(system, serviceName, kafkaTopic))
        }

        init(system)
  }

  private def publicationRecordsEventsMaybe = publicationRecordsEventsInstance.future.value.flatMap(_.toOption)
}

