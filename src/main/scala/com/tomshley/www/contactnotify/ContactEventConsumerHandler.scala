package com.tomshley.www.contactnotify

import com.google.protobuf.any.Any as ScalaPBAny
import com.tomshley.www.contact.proto
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.pekko.Done
import org.apache.pekko.actor.typed.ActorSystem
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class ContactEventConsumerHandler(system: ActorSystem[?], serviceName: String, kafkaTopic: String) {
  private val logger = LoggerFactory.getLogger(getClass)

  given ec: ExecutionContext = system.executionContext

  def process(record: ConsumerRecord[String, Array[Byte]]): Future[Done] = {
    val bytes = record.value()
    val x = ScalaPBAny.parseFrom(bytes)
    val typeUrl = x.typeUrl
    try {
      val inputBytes = x.value.newCodedInput()
      val event =
        typeUrl match {
          case "www-tomshley-com-contact-service/contact.CustomerContactReceived" =>
            proto.CustomerContactReceived.parseFrom(inputBytes)
          case _ =>
            throw new IllegalArgumentException(
              s"unknown record type [$typeUrl]")
        }

      event match {
        case proto.CustomerContactReceived(
        contactUUID,
        name,
        phone,
        email,
        message,
        inboundTime,
        _
        ) => {
          logger.info(s"CustomerContactReceived: ${contactUUID}")
        }
      }

      Future.successful(Done)
    } catch {
      case NonFatal(e) =>
        logger.error("Could not process event of type [{}]", typeUrl, e)
        Future.failed(e)
    }
  }
}