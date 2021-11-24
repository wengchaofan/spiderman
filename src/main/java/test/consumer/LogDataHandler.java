package test.consumer;

import com.lmax.disruptor.EventHandler;
import test.entity.LogData;

public class LogDataHandler implements EventHandler<LogData> {

    @Override
    public void onEvent(LogData event, long sequence, boolean endOfBatch) {
        System.out.println("消费Event数据: " + event.getValue());
    }
}
