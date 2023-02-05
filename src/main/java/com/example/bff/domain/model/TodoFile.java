package com.example.bff.domain.model;

import java.io.InputStream;

import lombok.Data;

@Data
public class TodoFile {
    private InputStream fileInputStream;
    private long size;
}
