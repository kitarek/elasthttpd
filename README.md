# ElastHttpD

...tries to be elastic HTTP server based on Apache HttpComponents. Allows to
setup HTTP server that listens for requests in a few lines:

        ElastHttpD
            .startBuilding()
            .customRequestConsumer(customRequestConsumer)
            .networkConfiguration(
                newConfiguration().setListeningPort(8080))
            .run() //blocks current thread

The `customRequestConsumer` instance needs only to implement the following
method:


        void consumeRequest(HttpRequest request, HttpResponse response);
        

The `ElastHttpD` builder is pretty flexible. Most of the aspects are 
preconfigured so you don't need to setup them. Even listening port is not
required as the below code:

        ElastHttpD
            .startBuilding()
            .customRequestConsumer(customRequestConsumer)
            .runAsync()

will start HTTP server in the background on: 18181 port defined by 
`NetworkConfigurationBuilder.DEFAULT_LISTEN_PORT`.

## The main purpose

The goal of that server is to easily bootstrap fully-fledged HTTP server
that can mock many external services responding to a different test related 
requests.

It can be also used to check behavior of HTTP client under test to simulate
different broken responses or outages.

Quick learning curve for starting HTTP server in the foreground and in the 
background can make life easier when external dependencies are needed. They can
be just created on the spot by test itself on the same machine/the same JVM
the test is invoked.

## How to develop ?

Please run: `./gradlew clean test` for compilation and running all the unit 
and integration tests.

You should be able to see the high-level integration specifications to see
how it works.

For code test coverage use command: `./gradlew clean test jacocoTestReport`

For other tasks refer to: `./gradlew tasks`.

