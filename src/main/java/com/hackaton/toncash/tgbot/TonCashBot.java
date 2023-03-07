package com.hackaton.toncash.tgbot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

//@Component
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TonCashBot extends TelegramLongPollingBot {
    private String botToken;
    private String botUsername;
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().isCommand() && update.getMessage().getText().equals("/start")) {
            SendMessage message = new SendMessage();
                    message.setChatId(update.getMessage().getChatId());
                    String name = update.getMessage().getFrom().getUserName();
                    message.setText("Hi "+ name +", my master! " + update.getMessage().getChatId()+ " " + update.getMessage().getFrom().getId());
                    message.setReplyMarkup(createButton());
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }else if (update.getMessage() != null && update.getMessage().hasLocation()) {
            Message message = update.getMessage();
            SendLocation location = new SendLocation();
            location.setChatId(message.getChatId());
            location.setLatitude(message.getLocation().getLatitude());
            location.setLongitude(message.getLocation().getLongitude());
            try {
                execute(location);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private ReplyKeyboardMarkup createButton() {
        KeyboardButton button = new KeyboardButton();
        button.setText("Click me!");
        button.setRequestLocation(true);



        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(button);
        rows.add(row);
        markup.setKeyboard(rows);
        markup.setOneTimeKeyboard(true);
        return markup;
    }
    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }
}
