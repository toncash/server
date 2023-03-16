package com.hackaton.toncash.tgbot;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
@Data
public class BotConfig {
    @Value("${tg_bot_token}")
    private String botToken;
    @Value("${tg_bot_username}")
    private String botUsername;

}
