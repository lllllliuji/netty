package ch1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

public class PlainNio2EchoServer {
    public void serve(int port) throws IOException {
        System.out.println("Listening for connections on port " + port);
        final AsynchronousServerSocketChannel asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
        asynchronousServerSocketChannel.bind(inetSocketAddress);

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        asynchronousServerSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Object attachment) {
                asynchronousServerSocketChannel.accept(null, this);
                ByteBuffer byteBuffer = ByteBuffer.allocate(100);
                result.read(byteBuffer, byteBuffer, new EchoCompleteHandler(result));
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                try {
                    asynchronousServerSocketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private final class EchoCompleteHandler implements CompletionHandler<Integer, ByteBuffer> {
        private final AsynchronousSocketChannel asynchronousSocketChannel;

        public EchoCompleteHandler(AsynchronousSocketChannel channel) {
            this.asynchronousSocketChannel = channel;
        }

        @Override
        public void completed(Integer result, ByteBuffer byteBuffer) {
            byteBuffer.flip();
            asynchronousSocketChannel.read(byteBuffer, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer buffer) {
                    if (buffer.hasRemaining()) {
                        asynchronousSocketChannel.write(buffer, buffer, this);
                    } else {
                        buffer.compact();
                        asynchronousSocketChannel.read(buffer, buffer, EchoCompleteHandler.this);
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    try {
                        asynchronousSocketChannel.close();
                    } catch (IOException e) {
                        //do something
                    }
                }
            });
        }

        @Override
        public void failed(Throwable exc, ByteBuffer byteBuffer) {
            try {
                asynchronousSocketChannel.close();
            } catch (IOException e) {
                //do something
            }
        }
    }
}
