package com.alura.pix.consumidor;

import com.alura.pix.avro.PixRecord;
import com.alura.pix.dto.PixDTO;
import com.alura.pix.dto.PixStatus;
import com.alura.pix.exception.KeyNotFoundException;
import com.alura.pix.model.Key;
import com.alura.pix.model.Pix;
import com.alura.pix.repository.KeyRepository;
import com.alura.pix.repository.PixRepository;
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

    @KafkaListener(topics = "pix-topic", groupId = "grupo")
    @RetryableTopic(
            backoff = @Backoff(value = 3000L),
            attempts = "4",
            autoCreateTopics = "true",
            include = KeyNotFoundException.class

    )
    public void processaPix(PixRecord pixDTO, Acknowledgment acknowledgment) {
        acknowledgment.acknowledge();
        System.out.println("Pix  recebido: " + pixDTO.getIdentificador());

        Pix pix = pixRepository.findByIdentifier(pixDTO.getIdentificador().toString());

        Key origem = keyRepository.findByChave(pixDTO.getChaveOrigem().toString());
        Key destino = keyRepository.findByChave(pixDTO.getChaveDestino().toString());

        if (origem == null || destino == null) {
            pix.setStatus(PixStatus.ERRO);
            throw new KeyNotFoundException();
        } else {
            pix.setStatus(PixStatus.PROCESSADO);
        }
        pixRepository.save(pix);
    }

}
