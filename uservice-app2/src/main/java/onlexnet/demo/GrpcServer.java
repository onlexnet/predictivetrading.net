package onlexnet.demo;

import java.util.OptionalInt;

import org.springframework.stereotype.Component;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
class GrpcServer implements RpcFacade, AutoCloseable {
  private final AppGrpcProperties grpcProperties;
  private final BindableService[] services;

  private OptionalInt serverPort = OptionalInt.empty();
  private Server server;

  @jakarta.annotation.PostConstruct
  @SneakyThrows
  public void start() {
    var port = grpcProperties.getServerPort();
    var builder = ServerBuilder.forPort(port);
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
  public void close() throws Exception {
    server.shutdownNow();
    server.awaitTermination();
  }

  @Override
  public OptionalInt getServerPort() {
    return serverPort;
  }
}
