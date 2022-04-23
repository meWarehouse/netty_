package com.at._08_netty._09_demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Optional;

/**
 * @create 2022-04-21
 */
public class TestNettyServer {

    public static void main(String[] args) {

        EventLoopGroup boosGroup = null;
        EventLoopGroup workGroup = null;

        try {

            /*

                boosGroup,workGroup 含有的子线程（NioEventLoop）的个数默认为机器CPU核数的2倍 使用EventExecutor线程组管理

                boosGroup将数据循环的发送给workGroup的各个线程

             */

            //创建 BoosGroup 工作线程
            //bossGroup 一直循环 只是处理连接请求 , 真正的和客户端业务处理，会交给 workerGroup完成
            boosGroup = new NioEventLoopGroup(2);

            //创建 WorkGroup 工作线程组
            workGroup = new NioEventLoopGroup(4);


            //配置服务端参数
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(boosGroup,workGroup)  // 配置boosGroup workGroup
                    .channel(NioServerSocketChannel.class) //使用NioSocketChannel 作为服务器的通道实现
                    .option(ChannelOption.SO_BACKLOG,64) //设置线程队列得到连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE,true) //设置保持活动连接状态
                    .childHandler(new ChannelInitializer<SocketChannel>() {  //创建一个通道初始化对象
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            System.out.println("初始化server端channel对象...");
                            socketChannel.pipeline().addLast(new TestNettyServerHandler());
                        }
                    });


            System.out.println("server端 is ready ...");


            //绑定端口 生成一个ChannelFuture 对象
            ChannelFuture channelFuture = serverBootstrap.bind(9089).sync();

            //给 ChannelFuture 注册监听器
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        System.out.println("server 端监听端口成功...");
                    }else {
                        System.out.println("server 端监听端口失败...");
                    }
                }
            });

            //关闭通道
            channelFuture.channel().closeFuture().sync();


        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

            Optional.ofNullable(boosGroup).ifPresent(bg -> bg.shutdownGracefully());
            Optional.ofNullable(workGroup).ifPresent(wg -> wg.shutdownGracefully());

        }


    }

}