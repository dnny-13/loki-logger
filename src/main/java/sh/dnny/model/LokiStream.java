package sh.dnny.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import sh.dnny.serializer.LokiLogValueSerializer;

import java.util.ArrayList;
import java.util.List;

public class LokiStream {
    private LokiStreamLabel stream;
    private List<LokiLogValue> values = new ArrayList<>();

    public List<LokiLogValue> getValues() {
        return values;
    }

    public void setValues(List<LokiLogValue> values) {
        this.values = values;
    }

    public void addValue(LokiLogValue value) {
        this.values.add(value);
    }

    public LokiStreamLabel getStream() {
        return stream;
    }

    public void setStream(LokiStreamLabel stream) {
        this.stream = stream;
    }

    public static class LokiStreamLabel {
        private String label;

        public LokiStreamLabel(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
    public static class LokiLogValue {
        public String ts;
        public String line;


        public String getTs() {
            return ts;
        }

        public void setTs(String ts) {
            this.ts = ts;
        }

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
        }
    }
}
