package com.hackaton.toncash.tgbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;


@Service
public class TonBotService {
    private final TonCashBot bot;

    @Autowired
    public TonBotService(@Lazy TonCashBot bot) {
        this.bot = bot;
    }

    public void sendNotification(String chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendNotificationWithApplyButton(String chatId, String message, String orderId, long personId) {
        System.out.println(chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        InlineKeyboardMarkup keyboard = createInlineButtons(orderId, personId);
        sendMessage.setReplyMarkup(keyboard);


        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    private InlineKeyboardMarkup createInlineButtons(String orderId, long personId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        InlineKeyboardButton applyButton = new InlineKeyboardButton("Apply");
        applyButton.setCallbackData("apply_deal_orderId:" + orderId + ";personId:" + personId);
        InlineKeyboardButton denyButton = new InlineKeyboardButton("Deny");
        denyButton.setCallbackData("deny_deal_orderId:" + orderId + ";personId:" + personId);
        InlineKeyboardButton viewProfileButton = new InlineKeyboardButton("View profile");
        viewProfileButton.setCallbackData("view_profile_" + personId);
        InlineKeyboardButton viewOrder = new InlineKeyboardButton("View order");
        viewOrder.setCallbackData("view_order_" + orderId);

        List<InlineKeyboardButton> row1 = List.of(applyButton, denyButton);
        List<InlineKeyboardButton> row2 = List.of(viewProfileButton, viewOrder);

        List<List<InlineKeyboardButton>> rows = List.of(row1, row2);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

//    private String getCallbackDataString(String payload, Order order) {
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//        try {
//            String encodedObject = objectMapper.writeValueAsString(order);
//            return payload + encodedObject;
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }

    //    private ReplyKeyboardMarkup createButton() {
//        KeyboardButton button = new KeyboardButton();
//        button.setText("Click me!");
//        button.setRequestLocation(true);
//
//
//        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
//        List<KeyboardRow> rows = new ArrayList<>();
//        KeyboardRow row = new KeyboardRow();
//        row.add(button);
//        rows.add(row);
//        markup.setKeyboard(rows);
//        markup.setOneTimeKeyboard(true);
//        return markup;
//    }

}
