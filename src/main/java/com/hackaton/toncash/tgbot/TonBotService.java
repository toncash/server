package com.hackaton.toncash.tgbot;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.Collections;


public class TonBotService {

    public static void sendNotification(TonCashBot bot, String chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendOfferDealNotification(TonCashBot bot, String chatId, String message, String dealId, long personId) {
        System.out.println(chatId);

        InlineKeyboardMarkup keyboard = createInlineKeyboard(dealId, personId);


        SendMessage sm = SendMessage.builder().chatId(chatId)
                .parseMode("HTML").text(message)
                .replyMarkup(keyboard).build();

        try {
            Message mes = bot.execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    private static InlineKeyboardMarkup createInlineKeyboard(String dealId, long personId) {

//        InlineKeyboardButton applyButton = createButton("Apply", "accept_deal_dealId:" + dealId + ";personId:" + personId, "");
//        InlineKeyboardButton denyButton = createButton("Deny", "deny_deal_dealId:" + dealId + ";personId:" + personId, "");
        InlineKeyboardButton viewOrder = createButton("View deal", "", "https://toncash.github.io/ui/");

        return InlineKeyboardMarkup.builder()
                .keyboard(Collections.singletonList(Collections.singletonList(viewOrder)))
                .build();
    }

    public static InlineKeyboardButton createButton(String text, String callback, String url) {
        InlineKeyboardButton btn = new InlineKeyboardButton(text);
        if (!callback.isEmpty()) {
            btn.setCallbackData(callback);
        } else {
            btn.setWebApp(new WebAppInfo(url));
        }
        return btn;
    }

//    private static void getTelegramUser(TonCashBot bot, Long userId) {
//        GetChatMember request = new GetChatMember();
//        request.setChatId(userId);
//        request.setUserId(userId);
//        ChatMember chatMember = null;
//        try {
//            chatMember = bot.execute(request);
//        } catch (TelegramApiException e) {
//            throw new RuntimeException(e);
//        }
//
//        // If the user has a username, print it to the console
//        if (chatMember.getStatus().equals("member") && chatMember.getUser().getUserName() != null) {
//            String username = chatMember.getUser().getUserName();
//            System.out.println("Username for user ID " + userId + " is @" + username);
//        } else {
//            System.out.println("User with ID " + userId + " is not a member of the chat or doesn't have a username");
//        }
//    }

    public static void sendEditMassage(TonCashBot bot, Long chatId, Integer messageId, String message, String orderId, String dealId, long clientId) {
        EditMessageText editMessage = chooseAnswer(chatId, messageId, message, orderId, dealId, clientId);
        try {
            bot.execute(editMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static EditMessageText chooseAnswer(Long chatId, Integer messageId, String message, String orderId, String dealId, long clientId) {
        return EditMessageText.builder()
                .chatId(chatId)
                .text(message)
                .messageId(messageId)
                .replyMarkup(createChangeInlineKeyboard(orderId, dealId, clientId))
                .parseMode(ParseMode.HTML)
                .build();
    }


    private static InlineKeyboardMarkup createChangeInlineKeyboard(String orderId, String dealId, long clientId) {

        InlineKeyboardButton viewOrder = InlineKeyboardButton.builder()
                .text("View deal")
                .webApp(new WebAppInfo("https://toncash.github.io/ui/"))
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(Collections.singletonList(viewOrder))
                .build();
    }

}
