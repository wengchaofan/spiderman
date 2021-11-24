package test.producer;

import com.lmax.disruptor.RingBuffer;
import test.entity.LogData;

import java.nio.ByteBuffer;

public class LogDataProducer {

    //环形缓冲区
    private final RingBuffer<LogData> ringBuffer;

    public LogDataProducer(RingBuffer<LogData> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    //将数据推入到缓冲区的方法：将数据装载到ringBuffer
    public void onData(ByteBuffer bb) {
        long sequence = ringBuffer.next(); // Grab the next sequence //获取下一个可用的序列号
        try {
            LogData event = ringBuffer.get(sequence); // Get the entry in the Disruptor //通过序列号获取空闲可用的LongEvent
            event.setValue(bb.getLong(0)); // Fill with data //设置数值
        } finally {
            ringBuffer.publish(sequence); //数据发布，只有发布后的数据才会真正被消费者看见
        }
    }

}
