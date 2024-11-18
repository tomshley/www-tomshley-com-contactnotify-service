package com.tomshley.www.contactnotify

import com.tomshley.hexagonal.lib.ManagedActorSystemMain
import org.apache.pekko.actor.typed.ActorSystem

@main def main(): Unit = {
  ManagedActorSystemMain("www-tomshley-com-contactnotify-service", (system: ActorSystem[?]) => {
    ContactEventConsumer.init(system)
  })
}