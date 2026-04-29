package com.desafio.config;

import javax.annotation.PostConstruct;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSchemaMaintenance {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void ajustarSchemaLegado() {
        try {
            jdbcTemplate.execute("ALTER TABLE tarefa ALTER COLUMN id_pessoa DROP NOT NULL");
            jdbcTemplate.execute("ALTER TABLE tarefa ALTER COLUMN finalizado SET DEFAULT false");
            jdbcTemplate.execute("ALTER TABLE tarefa ALTER COLUMN em_andamento SET DEFAULT false");
            jdbcTemplate.execute("ALTER TABLE tarefa ALTER COLUMN notificacao_vencimento_enviada SET DEFAULT false");
        } catch (Exception ex) {
            log.warn("Nao foi possivel aplicar ajustes de schema legado automaticamente: {}", ex.getMessage());
        }
    }
}
