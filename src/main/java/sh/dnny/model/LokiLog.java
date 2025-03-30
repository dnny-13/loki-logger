package sh.dnny.model;

import java.util.ArrayList;
import java.util.List;

public class LokiLog {
    private List<LokiStream> streams = new ArrayList<>();

    public void addStream(LokiStream stream) {
        streams.add(stream);
    }

    public List<LokiStream> getStreams() {
        return streams;
    }

    public void setStreams(List<LokiStream> streams) {
        this.streams = streams;
    }
}
