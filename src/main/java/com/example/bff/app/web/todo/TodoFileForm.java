package com.example.bff.app.web.todo;

import org.springframework.web.multipart.MultipartFile;

import com.example.fw.web.validation.UploadFileMaxSize;
import com.example.fw.web.validation.UploadFileNotEmpty;
import com.example.fw.web.validation.UploadFileRequired;

import lombok.Data;

@Data
public class TodoFileForm {
    
    @UploadFileRequired
    @UploadFileNotEmpty
    @UploadFileMaxSize
    private MultipartFile todoFile;
}
