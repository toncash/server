package com.hackaton.toncash.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum PersonLevel {
    BASIC(100), ADVANCED(500), MASTER(2000), GRANDMASTER(5000), EXPERT(10000), UNREAL(Integer.MAX_VALUE);
    private int value;
}
