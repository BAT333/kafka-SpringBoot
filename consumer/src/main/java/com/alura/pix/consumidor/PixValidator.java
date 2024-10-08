package com.alura.pix.consumidor;

import com.alura.pix.avro.PixRecord;
import com.alura.pix.dto.PixDTO;
import com.alura.pix.dto.PixStatus;
import com.alura.pix.exception.KeyNotFoundException;
import com.alura.pix.model.Key;
import com.alura.pix.model.Pix;
import com.alura.pix.repository.KeyRepository;
import com.alura.pix.repository.PixRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.generic.GenericData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
public class PixValidator {

    @Autowired
    private KeyRepository keyRepository;

    @Autowired
    private PixRepository pixRepository;

    @KafkaListener(topics = "pix-service.public.pix", groupId = "grupo")
    @RetryableTopic(
            backoff = @Backoff(value = 3000L),
            attempts = "4",
            autoCreateTopics = "true",
            include = KeyNotFoundException.class

    )
    public void processaPix(GenericData .Record data, Acknowledgment acknowledgment) throws JsonProcessingException {
        acknowledgment.acknowledge();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        PixDTO pixDTO = objectMapper.readValue(data.get("after").toString(), PixDTO.class);
        if (pixDTO.getStatus().equals(PixStatus.EM_PROCESSAMENTO)){
            Pix pix = pixRepository.findByIdentifier(pixDTO.getIdentifier());

            Key origem = keyRepository.findByChave(pixDTO.getChaveOrigem());
            Key destino = keyRepository.findByChave(pixDTO.getChaveDestino());

            if (origem == null || destino == null) {
                pix.setStatus (PixStatus.ERRO);
            } else {
                pix.setStatus (PixStatus.PROCESSADO);
            }
            pixRepository.save(pix);
        }
    }

}
