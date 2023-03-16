package com.hackaton.toncash.service;

import com.hackaton.toncash.dto.OrderDTO;
import com.hackaton.toncash.dto.PersonDTO;
import com.hackaton.toncash.model.Deal;
import com.hackaton.toncash.model.Person;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommonMethods {

    public static <T, D> T updateEntity(T entity, D dto) {
        try {
            // Get all fields from both entity and DTO classes
            Field[] entityFields = entity.getClass().getDeclaredFields();
            Field[] dtoFields = dto.getClass().getDeclaredFields();

            // Iterate through all fields in entity and check if they exist in DTO
            for (Field entityField : entityFields) {
                try {
                    // Set field as accessible to read its value
                    entityField.setAccessible(true);

                    // Check if field exists in DTO
                    Field dtoField = Arrays.stream(dtoFields)
                            .filter(field -> field.getName().equals(entityField.getName()))
                            .findFirst()
                            .orElse(null);

                    // If field exists in DTO, update its value in entity
                    if (dtoField != null) {
                        // Set field as accessible to write its value
                        dtoField.setAccessible(true);
                        Object value = dtoField.get(dto);
                        if (value != null) {
                            entityField.set(entity, value);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        // Return the updated entity
        return entity;
    }



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
