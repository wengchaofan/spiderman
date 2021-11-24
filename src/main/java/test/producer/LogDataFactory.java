package test.producer;

import com.lmax.disruptor.EventFactory;
import test.entity.LogData;

public class LogDataFactory implements EventFactory<LogData> {

    @Override
    public LogData newInstance() {
        return new LogData();
    }
}
