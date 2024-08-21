package com.alura.pix.strem;

import com.alura.pix.dto.PixDTO;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PixAggregator {
    @Autowired
    public void aggregator(StreamsBuilder builder){
        KStream<String, PixDTO>
    }
}
