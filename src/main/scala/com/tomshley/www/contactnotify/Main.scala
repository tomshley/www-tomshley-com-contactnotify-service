package com.tomshley.www.contactnotify

import com.tomshley.hexagonal.lib.ManagedPekkoClusterMain
import org.apache.pekko.actor.typed.ActorSystem

@main def main(): Unit = {
  ManagedPekkoClusterMain("www-tomshley-com-contactnotify-service", (system: ActorSystem[?]) => {
    ContactEventConsumer.init(system)
  })
}