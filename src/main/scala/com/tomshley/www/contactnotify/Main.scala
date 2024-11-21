package com.tomshley.www.contactnotify

import com.tomshley.hexagonal.lib.twilio.util.{TwilioClient, TwilioConfig}
import com.tomshley.hexagonal.lib.{ManagedActorSystemMain, ManagedPekkoClusterMain}
import org.apache.pekko.actor.typed.ActorSystem

@main def main(): Unit = {
  ManagedPekkoClusterMain("www-tomshley-com-contactnotify-service", (system: ActorSystem[?]) => {
    TwilioClient.init(TwilioConfig(
      accountSid=system.settings.config.getString("twilio.account_sid"),
      authToken=system.settings.config.getString("twilio.auth_token"),
      from=system.settings.config.getString("twilio.from_number")
    ))
    ContactEventConsumer.init(system)
  })
}