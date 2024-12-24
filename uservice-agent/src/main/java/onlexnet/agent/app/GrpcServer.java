package onlexnet.agent.app;

import java.util.OptionalInt;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
class GrpcServer implements RpcFacade, AutoCloseable {
  private final AppGrpcProperties grpcProperties;
  private final BindableService[] services;

  private OptionalInt serverPort = OptionalInt.empty();
  private Server server;

  @PostConstruct
  @SneakyThrows
  public void start() {
    var port = grpcProperties.getServerPort();
    var builder = ServerBuilder
        .forPort(port)
        .executor(Executors.newVirtualThreadPerTaskExecutor());
    for (var bindableService : services) {
      builder.addService(bindableService);
    }
    server = builder
        .intercept(new ExceptionHandler())
        .build();
    server.start();
    serverPort = OptionalInt.of(server.getPort());
  }

  @Override
  public void close() throws InterruptedException {
    server.shutdownNow();
    server.awaitTermination();
  }

  @Override
  public OptionalInt getServerPort() {
    return serverPort;
  }
}
