package com.haein.shoppingmall.dto;

public class AdminAnswerForm {

    private String content;

    public AdminAnswerForm() {
    }

    public AdminAnswerForm(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
