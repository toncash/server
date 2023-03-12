package com.hackaton.toncash.tgbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class TonBotService {

    public static void sendNotification(TonCashBot bot, String chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendNotificationWithApplyButton(TonCashBot bot, String chatId, String message, String orderId, long personId) {
        System.out.println(chatId);
        InlineKeyboardMarkup keyboard = createInlineKeyboard(orderId, personId);

        SendMessage sm = SendMessage.builder().chatId(chatId)
                .parseMode("HTML").text(message)
                .replyMarkup(keyboard).build();

        try {
            bot.execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    private static InlineKeyboardMarkup createInlineKeyboard(String orderId, long personId) {

        InlineKeyboardButton applyButton = createButton("Apply", "apply_deal_orderId:" + orderId + ";personId:" + personId, "");
        InlineKeyboardButton denyButton = createButton("Deny", "deny_deal_orderId:" + orderId + ";personId:" + personId, "");
        InlineKeyboardButton viewOrder = createButton("View order", "", "https://toncash.github.io/ui/");

        return InlineKeyboardMarkup.builder()
                .keyboard(Arrays.asList(Collections.singletonList(viewOrder), Arrays.asList(applyButton, denyButton)))
                .build();
    }

    public static InlineKeyboardButton createButton(String text, String callback, String url) {
        InlineKeyboardButton btn = new InlineKeyboardButton(text);
        if (!callback.isEmpty()){
            btn.setCallbackData(callback);
        } else {
            btn.setWebApp(new WebAppInfo(url));
        }
        return btn;
    }


}
