package com.hackaton.toncash.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@NonNull
public class PersonDealDTO {

    PersonDTO person;
    DealDTO deal;
}
