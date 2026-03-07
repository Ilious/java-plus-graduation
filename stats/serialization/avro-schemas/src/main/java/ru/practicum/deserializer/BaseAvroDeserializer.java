package ru.practicum.deserializer;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    private final DatumReader<T> reader;

    private final DecoderFactory decoderFactory;

    public BaseAvroDeserializer(Schema schema) {
        this(schema, DecoderFactory.get());
    }

    public BaseAvroDeserializer(Schema schema, DecoderFactory decoderFactory) {
        this.reader = new SpecificDatumReader<>(schema);
        this.decoderFactory = decoderFactory;
    }

    @Override
    public T deserialize(String topic, byte[] bytes) {
        try {
            if (bytes == null)
                return null;

            BinaryDecoder decoder = decoderFactory.binaryDecoder(bytes, null);

            return this.reader.read(null, decoder);
        } catch (Exception e) {
            throw new SerializationException("Error deserializing data from topic [" + topic + "]", e);
        }
    }
}
