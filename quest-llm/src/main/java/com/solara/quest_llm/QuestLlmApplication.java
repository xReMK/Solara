package com.solara.quest_llm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
public class QuestLlmApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuestLlmApplication.class, args);
	}

}
