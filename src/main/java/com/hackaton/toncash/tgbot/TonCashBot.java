package com.hackaton.toncash.tgbot;

import com.hackaton.toncash.service.OrderServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


@Component
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TonCashBot extends TelegramLongPollingBot {
    private BotConfig config;
    private String botToken;
    private String botUsername;
    private OrderServiceImpl orderService;

    @Autowired
    public TonCashBot(BotConfig config, @Lazy OrderServiceImpl orderService) {
        this.orderService = orderService;
        this.config = config;
        this.botToken = config.getBotToken();
        this.botUsername = config.getBotUsername();
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().isCommand() && update.getMessage().getText().equals("/start")) {
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId());
            String name = update.getMessage().getFrom().getUserName();
            message.setText("Hi " + name + ", my master! ");
//                    message.setReplyMarkup(createButton());
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();

            if (callbackData.startsWith("accept_deal_")) {
                System.out.println("apply_deal_button");

                int dealIdStartIndex = callbackData.indexOf("dealId:") + "dealId:".length();
                int dealIdEndIndex = callbackData.indexOf(";", dealIdStartIndex);
                String dealId = callbackData.substring(dealIdStartIndex, dealIdEndIndex);
                int personIdStartIndex = callbackData.indexOf("personId:") + "personId:".length();
                String personId = callbackData.substring(personIdStartIndex);
                Long ownerOrderId = update.getCallbackQuery().getFrom().getId();

                Long chatId = update.getCallbackQuery().getFrom().getId();
                Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
//                orderService.acceptDeal(Long.parseLong(personId), dealId, ownerOrderId, chatId, messageId);
//                Long.parseLong(personId), dealId, ownerOrderId, chatId, messageId
            } else if (callbackData.startsWith("deny_deal_")) {
                System.out.println("diny_deal_");

                int dealIdStartIndex = callbackData.indexOf("dealId:") + "dealId:".length();
                int dealIdEndIndex = callbackData.indexOf(";", dealIdStartIndex);
                String dealId = callbackData.substring(dealIdStartIndex, dealIdEndIndex);
                int personIdStartIndex = callbackData.indexOf("personId:") + "personId:".length();
                String personId = callbackData.substring(personIdStartIndex);

//                EditMessageText message = chooseAnswer(update, "Y deny");

//                orderService.denyOrder(Long.parseLong(personId), orderId);
            }


        }

    }



//    public InlineKeyboardMarkup createInlineButton(String text, String url) {
//        InlineKeyboardButton btn = InlineKeyboardButton.builder()
//                .text(text)
//                .webApp(new WebAppInfo(url))
//                .build();
//        return InlineKeyboardMarkup.builder().keyboardRow(Collections.singletonList(btn)).build();
//    }
//
//    public void sendInlineButton(Long who, String txt, InlineKeyboardMarkup kb) {
//        SendMessage sm = SendMessage.builder().chatId(who.toString())
//                .parseMode("HTML").text(txt)
//                .replyMarkup(kb).build();
//
//        try {
//            execute(sm);
//        } catch (TelegramApiException e) {
//            throw new RuntimeException(e);
//        }
//    }

}
