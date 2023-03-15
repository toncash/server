package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.OrderDTO;
import com.hackaton.toncash.dto.PersonDTO;
import com.hackaton.toncash.model.Person;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

public class CommonMethods {
    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
    public static PersonDTO mapToPersonDTO(Person person) {
        return PersonDTO.builder()
                .id(person.getId())
                .username(person.getUsername())
                .avatarURL(person.getAvatarURL())
                .chatId(person.getChatId())
                .currentOrders(person.getCurrentOrders())
                .finishedOrders(person.getFinishedOrders().size())
                .badOrders(person.getBadOrders().size())
                .currentDeals(person.getCurrentDeals())
                .community(person.getCommunity())
                .rank(person.getRank())
                .build();
    }

}
