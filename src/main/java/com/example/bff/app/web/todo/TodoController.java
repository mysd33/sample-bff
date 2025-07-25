package com.example.bff.app.web.todo;

import java.util.Collection;

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
import com.example.bff.app.web.todo.TodoForm.TodoCreate;
import com.example.bff.app.web.todo.TodoForm.TodoDelete;
import com.example.bff.app.web.todo.TodoForm.TodoFinish;
import com.example.bff.domain.message.MessageIds;
import com.example.bff.domain.model.Todo;
import com.example.bff.domain.service.todo.TodoService;
import com.example.fw.common.exception.BusinessException;
import com.example.fw.common.message.ResultMessage;
import com.example.fw.common.message.ResultMessageType;

import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;

/**
 * 
 * Todo機能のコントローラクラス
 * 
 */
@XRayEnabled
@Controller
@RequestMapping("todo")
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;
    private final TodoMapper todoMapper;

    @ModelAttribute
    public TodoForm setUpForm() {
        return new TodoForm();
    }

    /**
     * Todoリストの表示
     */
    @GetMapping("list")
    public String list(Model model) {
        Collection<Todo> todos = todoService.findAll();
        model.addAttribute("todos", todos);
        return "todo/todoList";
    }

    /**
     * Todoの登録
     */
    @PostMapping("create")
    public String create(@Validated({ Default.class, TodoCreate.class }) TodoForm todoForm, BindingResult bindingResult,
            Model model, RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            return list(model);
        }
        Todo todo = todoMapper.formToModel(todoForm);
        try {
            todoService.create(todo);
        } catch (BusinessException e) {
            model.addAttribute(e.getResultMessage());
            return list(model);
        }
        attributes.addFlashAttribute(
                ResultMessage.builder().type(ResultMessageType.INFO).code(MessageIds.I_EX_0001).build());
        return "redirect:/todo/list";
    }

    /**
     * Todoの完了
     */
    @PostMapping("finish")
    public String finish(@Validated({ Default.class, TodoFinish.class }) TodoForm form, BindingResult bindingResult,
            Model model, RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            return list(model);
        }
        try {
            todoService.finish(form.getTodoId());
        } catch (BusinessException e) {
            model.addAttribute(e.getResultMessage());
            return list(model);
        }
        attributes.addFlashAttribute(
                ResultMessage.builder().type(ResultMessageType.INFO).code(MessageIds.I_EX_0002).build());
        return "redirect:/todo/list";
    }

    @PostMapping("delete")
    public String delete(@Validated({ Default.class, TodoDelete.class }) TodoForm form, BindingResult bindingResult,
            Model model, RedirectAttributes attributes) {

        if (bindingResult.hasErrors()) {
            return list(model);
        }

        try {
            todoService.delete(form.getTodoId());
        } catch (BusinessException e) {
            model.addAttribute(e.getResultMessage());
            return list(model);
        }
        attributes.addFlashAttribute(
                ResultMessage.builder().type(ResultMessageType.INFO).code(MessageIds.I_EX_0003).build());
        return "redirect:/todo/list";
    }
}
