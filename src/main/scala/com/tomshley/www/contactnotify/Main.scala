package com.tomshley.www.contactnotify

import com.tomshley.hexagonal.lib.ManagedActorSystemMain
import com.tomshley.hexagonal.lib.twilio.util.TwilioClient
import com.tomshley.hexagonal.lib.twilio.util.TwilioConfig
import org.apache.pekko.actor.typed.ActorSystem

@main def main(): Unit = {
  ManagedActorSystemMain("www-tomshley-com-contactnotify-service", (system: ActorSystem[?]) => {
    TwilioClient.init(TwilioConfig(
      accountSid=system.settings.config.getString("twilio.account_sid"),
      authToken=system.settings.config.getString("twilio.auth_token"),
      from=system.settings.config.getString("twilio.from_number")
    ))
    ContactEventConsumer.init(system)
  })
}