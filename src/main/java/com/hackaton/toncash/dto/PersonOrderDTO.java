package com.hackaton.toncash.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@NonNull
public class PersonOrderDTO {
    PersonDTO person;
    OrderDTO order;
}
