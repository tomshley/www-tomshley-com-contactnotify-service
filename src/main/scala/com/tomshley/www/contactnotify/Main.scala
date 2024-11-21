package com.tomshley.www.contactnotify

import com.tomshley.hexagonal.lib.ManagedPekkoClusterMain
import com.tomshley.hexagonal.lib.bootstrap.LoadStatusStartup
import org.apache.pekko.actor.typed.ActorSystem

import scala.concurrent.ExecutionContext

@main def main(): Unit = {
  ManagedPekkoClusterMain("www-tomshley-com-contactnotify-service", (system: ActorSystem[?]) => {
    LoadStatusStartup.start {
      given ec:ExecutionContext = system.executionContext
      
      ContactEventConsumer.init(system).map(_ => false)
    }
  })
}