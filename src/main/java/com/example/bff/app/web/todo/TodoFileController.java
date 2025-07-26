package com.example.bff.app.web.todo;

import java.io.IOException;
import java.util.HashMap;

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
import com.example.bff.domain.service.async.AsyncService;
import com.example.bff.domain.service.todo.TodoFileService;
import com.example.fw.common.async.model.JobRequest;
import com.example.fw.common.message.ResultMessage;
import com.example.fw.common.message.ResultMessageType;

import lombok.RequiredArgsConstructor;

@XRayEnabled
@Controller
@RequestMapping("todoFile")
@RequiredArgsConstructor
public class TodoFileController {
    private static final String JOB003 = "job003";
    private final TodoFileService todoFileService;
    private final AsyncService asyncService;

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
        // 本処理のサービス実行
        TodoFile todoFile = new TodoFile();
        todoFile.setFileInputStream(form.getTodoFile().getInputStream());
        todoFile.setSize(form.getTodoFile().getSize());
        todoFileService.save(todoFile);
        redirectAttributes.addFlashAttribute(
                ResultMessage.builder().type(ResultMessageType.INFO).code(MessageIds.I_EX_0004).build());
        // 非同期実行依頼サービス実行
        HashMap<String, String> params = new HashMap<>();
        params.put("filePath", todoFile.getTargetFilePath());
        // @formatter:off 
        JobRequest jobRequest = JobRequest.builder()
                .jobId(JOB003)
                .parameters(params)
                .build();
        // @formatter:on        
        try {
            asyncService.invokeAsync(jobRequest);
        } catch (Exception e) {
            // 非同期実行依頼時にエラーが発生した場合は、業務エラーとしてメッセージを設定
            model.addAttribute(ResultMessage.builder().type(ResultMessageType.WARN).code(MessageIds.W_EX_8007).build());
            return getUpload(form);
        }

        return "redirect:upload";
    }

}
