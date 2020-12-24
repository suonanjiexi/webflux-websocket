package com.snjx.netty.websocket.server;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.netty.http.server.HttpServer;

/**
 * @author by ernest
 * @version 1.0
 * @apiNote TODO
 * @date 12/22/20 5:35 下午
 */
@Component
public class NettyWebServerFactoryPortCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    @Override
    public void customize(NettyReactiveWebServerFactory serverFactory) {
        serverFactory.setPort(8088);
    }

    private static class PortCustomizer implements NettyServerCustomizer {
        private final int port;

        private PortCustomizer(int port) {
            this.port = port;
        }
        @Override
        public HttpServer apply(HttpServer httpServer) {
            return httpServer.port(port);
        }
    }

    private static class EventLoopNettyCustomizer implements NettyServerCustomizer {
        @Override
        public HttpServer apply(HttpServer httpServer) {
            EventLoopGroup parentGroup = new NioEventLoopGroup();
            EventLoopGroup childGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
            return httpServer.tcpConfiguration(tcpServer -> tcpServer
                    .bootstrap(serverBootstrap -> serverBootstrap
                            .group(parentGroup, childGroup)
                            .channel(NioServerSocketChannel.class)));
        }
    }

    @Bean
    public NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {
        NettyReactiveWebServerFactory webServerFactory = new NettyReactiveWebServerFactory();
        webServerFactory.addServerCustomizers(new EventLoopNettyCustomizer());
        return webServerFactory;
    }
}
