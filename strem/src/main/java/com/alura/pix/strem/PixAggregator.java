package com.alura.pix.strem;

import com.alura.pix.dto.PixDTO;
import com.alura.pix.serdes.PixSerdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.kafka.common.serialization.Serdes;


@Service
public class PixAggregator {
    @Autowired
    public void aggregator(StreamsBuilder builder){

        KTable<String, Double> stream = builder
                .stream("pix-topic", Consumed.with(Serdes.String(), PixSerdes.serdes()))
                .peek((key,value)-> System.out.println("PIX RECEBIDO "+ value.getChaveOrigem()))
                .groupBy((key,value)->value.getChaveOrigem())
                .aggregate(
                        ()->0.0,(key,velue,aggregate)->(aggregate + velue.getValor()),
                        Materialized.with(Serdes.String(),Serdes.Double())

                )
                ;

        stream.toStream().print(Printed.toSysOut());
        stream.toStream().to("pix-topic-validação", Produced.with(Serdes.String(), Serdes.Double()));

    }
}
