package com.example.bff.app.web.todo;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.example.bff.domain.message.MessageIds;
import com.example.bff.domain.model.TodoFile;
import com.example.bff.domain.service.todo.TodoFileService;
import com.example.fw.common.message.ResultMessage;
import com.example.fw.common.message.ResultMessageType;

import lombok.RequiredArgsConstructor;

@XRayEnabled
@Controller
@RequestMapping("todoFile")
@RequiredArgsConstructor
public class TodoFileController {
    private final TodoFileService todoFileService;

    @GetMapping("upload")
    public String getUpload(@ModelAttribute TodoFileForm form) {
        return "todo/upload";
    }

    @PostMapping("upload")
    public String postUpload(@Validated TodoFileForm form, BindingResult result, RedirectAttributes redirectAttributes,
            Model model) throws IOException {
        if (result.hasErrors()) {
            return "todo/upload";
        }
        TodoFile todoFile = new TodoFile();
        todoFile.setFileInputStream(form.getTodoFile().getInputStream());
        todoFileService.save(todoFile);
        redirectAttributes.addFlashAttribute(ResultMessage.builder().type(ResultMessageType.INFO)
                .code(MessageIds.I_EX_0004).build());
                
        //TODO: 非同期実行依頼の実装
        
        return "redirect:upload";
    }

}
