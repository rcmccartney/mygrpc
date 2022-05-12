package kvprog.client;

import com.google.common.collect.Multiset;
import io.grpc.*;
import io.grpc.Metadata.Key;

import javax.inject.Inject;

public class RpcInterceptorOutput implements ClientInterceptor {

  private final Multiset<String> calls;

  @Inject
  RpcInterceptorOutput(@ClientModule.CallMetadata Multiset<String> calls) {
    this.calls = calls;
  }

  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(final MethodDescriptor<ReqT, RespT> methodDescriptor, final CallOptions callOptions, final Channel channel) {
    return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
      @Override
      public void start(Listener<RespT> responseListener, Metadata requestHeader) {
        super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
          @Override
          public void onHeaders(Metadata responseHeader) {
            calls.add(responseHeader.get(Key.of("elapsed_time", Metadata.ASCII_STRING_MARSHALLER)));
            super.onHeaders(responseHeader);
          }
        }, requestHeader);
      }
    };
  }
}