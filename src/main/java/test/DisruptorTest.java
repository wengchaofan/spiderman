package test;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import test.consumer.LogDataHandler;
import test.entity.LogData;
import test.producer.LogDataFactory;
import test.producer.LogDataProducer;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DisruptorTest {


    private static final Executor executor = Executors.newCachedThreadPool();

    public static void main(String[] args) throws InterruptedException {

        int bufferSize = 1024;
        LogDataFactory logDataFactory = new LogDataFactory();
        // ProducerType.MULTI 多生产者模式
        Disruptor<LogData> disruptor = new Disruptor<>(logDataFactory, bufferSize, executor, ProducerType.MULTI, new BlockingWaitStrategy());
        //指定一个消费者
        disruptor.handleEventsWith(new LogDataHandler());

        //多个消费者间形成依赖关系，每个依赖节点的消费者为单线程。
      //  disruptor.handleEventsWith(new OrderHandler1("1")).then(new OrderHandler1("2"), new OrderHandler1("3")).then(new OrderHandler1("4"));
        /*
         * 该方法传入的消费者需要实现WorkHandler接口，方法的内部实现是：先创建WorkPool，然后封装WorkPool为EventHandlerPool返回。
         * 消费者1、2对于消息的消费有时有竞争，保证同一消息只能有一个消费者消费
         */
       // disruptor.handleEventsWithWorkerPool(new OrderHandler1("1"), new OrderHandler1("2"));

        // Start the Disruptor, starts all threads running
        //启动并初始化disruptor
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        //获取已经初始化好的ringBuffer
        RingBuffer<LogData> ringBuffer = disruptor.getRingBuffer();

        //获取已经初始化好的ringBuffer
        LogDataProducer producer = new LogDataProducer(ringBuffer);

        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; l < 1000L ; l++) {
            bb.putLong(0, l);
            producer.onData(bb);
        }
    }


}
