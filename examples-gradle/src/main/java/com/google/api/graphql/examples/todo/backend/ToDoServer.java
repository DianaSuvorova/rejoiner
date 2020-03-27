package com.google.api.graphql.examples.todo.backend;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.todo.ToDo;
import io.grpc.examples.todo.TodoGrpc;
import io.grpc.examples.todo.AddToDoResponse;
import io.grpc.examples.todo.AddToDoRequest;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.logging.Logger;

public class ToDoServer {
    private static final Logger logger = Logger.getLogger(ToDoServer.class.getName());

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = ServerBuilder.forPort(port).addService(new TodoImpl()).build().start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread() {
                            @Override
                            public void run() {
                                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                                ToDoServer.this.stop();
                                System.err.println("*** server shut down");
                            }
                        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /** Await termination on the main thread since the grpc library uses daemon threads. */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /** Main launches the server from the command line. */
    public static void main(String[] args) throws IOException, InterruptedException {
        final ToDoServer server = new ToDoServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class TodoImpl extends TodoGrpc.TodoImplBase {

        @Override
        public void addToDo(AddToDoRequest req, StreamObserver<AddToDoResponse> responseObserver) {
            ToDo todo = ToDo.newBuilder().setText("Task: " + req.getText()).setId("1").build();
            AddToDoResponse todoResponse = AddToDoResponse.newBuilder().setToDo(todo).build();
            responseObserver.onNext(todoResponse);
            responseObserver.onCompleted();
        }
    }
}
