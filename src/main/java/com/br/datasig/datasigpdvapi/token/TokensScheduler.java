package com.br.datasig.datasigpdvapi.token;

import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Calendar;
import java.util.List;

@Configuration
@EnableScheduling
@NoArgsConstructor
public class TokensScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TokensScheduler.class);
    private static final int EXECUTION_RATE = 300000; // 5 minutes
    private static final int EXPIRATION_TIME = 14400000; // 4 hours

    @Scheduled(fixedRate = EXECUTION_RATE)
    public static void checkIfAnyTokenShouldBeDeleted() {
        List<Token> validTokens = TokensManager.getInstance().getValidTokens();
        long currentDate = Calendar.getInstance().getTimeInMillis();
        validTokens.forEach(token -> {
            if((currentDate - token.getCreatedAt()) >= EXPIRATION_TIME) {
                logger.info("Token %s do usuário %s não é mais válido e será removido.".formatted(token.getValue(), token.getUserName()));
                token.invalidateToken();
            }
        });
        TokensManager.getInstance().removeInvalidTokens();
    }
}
