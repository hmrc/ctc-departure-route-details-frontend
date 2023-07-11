
# ctc-departure-route-details-frontend

This service allows a user to complete the route details section of a transit movement departure.

Service manager port: 10129

To toggle between the Phase 5 transition and post-transition modes we have defined two separate modules, each with their own set of class bindings to handle the rules associated with these two periods.

To run the service in 'transition' mode:
```
sbt -Dplay.additional.module=config.TransitionModule run
```

To run the service in 'post-transition' mode:
```
sbt -Dplay.additional.module=config.PostTransitionModule run
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").