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

## How to see it in action ? 

Remember that this HTTP server is pretty flexible and it tends to be more 
programmable than complete so please don't expect file based HTTP server
or any special built-in behavior for serving virtual hosts.

To see that it actually do the job just run:

        ./gradlew run

in the README file directory to be able to see dummy pre-programmed response
for default configuration -- use any browser and enter the following URL
on which the server listens by default.

        http://localhost:18181/

You should see dummy response from so called "dummy" request consumer.

For file server functionality you need to use *built-in plugin* called fileServer.
There is a default runner for Gradle that by default serves the project
directory (this is the root webserver directory).

To browse project files via server just run:

        ./gradlew clean test jacocoTestReport runFileServer

and try to access:

        http://localhost:18181/build/reports/jacoco/test/html/index.html

You should see this project *unit test coverage report*. You should also
be able to click on different links.

## The idea behind...

The goal of that server is to easily bootstrap fully-fledged HTTP server
that can mock many external services responding to a different test related 
requests.

It can be also used to check behavior of HTTP client under test to simulate
different broken responses or outages.

Quick learning curve for starting HTTP server in the foreground and in the 
background can make life easier when external dependencies are needed. They can
be just created on the spot by test itself on the same machine/the same JVM
the test is invoked.

## Pluggable behavior of server. Infinite possibilities...

The server supports pluggable architecture of request consumers so you can
do with them whatever you like - in example you can server file from server
using built-in file server functionality.

Instead of using `customRequestConsumer` passing handler class you need
to decalre correct plugin builder.

See real life example that works:

        ElastHttpD
            .startBuilding()
            .consumeRequestsWithPlugin(
                 fileServer().withRootServerDirectory(currentDirectory())
            )
            .runAsync()

The fileServer() method comes from:

 `io.github.kitarek.elasthttpd.plugins.consumers.file.FileServerPluginBuilder`

...and this plugin it's built into server core functionality.

You can write your own extensions and plugins (see fileServer plugin for
reference how to write them) and compose or nest them in each other to
achieve what you like.

Imagine virtual host plugin that can be used for each domain differently
composing fileServer plugins in a different way.

## The idea under the hood... How to develop ?

Please run: `./gradlew clean test` for compilation and running all the unit 
and integration tests.

You should be able to find the high-level integration specifications and tests 
to discover how it works and how it can be used.

For the code test coverage use the command:
`./gradlew clean test jacocoTestReport`

For other tasks refer to: `./gradlew tasks`.

## The next step

Operating directly on `HttpRequest` and `HttpResponse` seems to be a bit too 
low-level for easy and clean mocking.

Especially for mocking HTTP server that tries to be a simple implementation
of complicated 3rd party service it would be the best to use facade and 
builders to react only to some interesting HTTP request in terms of 
`HttpMethod`, URI path or HTTP headers. 

Also the most important in terms of responding to requests is letting to easily
build JSON, XML or binary stream to be able to respond to typical REST or other
API structure.

What can be also interesting is a possibility to pass results of HTTP server 
expectation so whether HTTP client requested properly for a series of response.

In general the final solution is too let you quickly build significant number
of scenarios for which some expectations will be defined and the report
for such server session could be built.

This can be now achieved writing dedicated plugin that will handle such
expectation as a part of builder pattern.

## Why not just normal HttpServer ?

The core is so flexible that there is a still possibility to add new plugins
that for some URLs/methods and virtual hosts (like `Host`) could offer web
file-related server by activating supplied plugin.

The main purpose is to offer the base for HTTP server and the rest should be
swappable (serving files, microservices, proxies etc).

Obviously the central point of this webserver is the ElastHttpD builder where
you can compose and build/nest differnet plugins.
