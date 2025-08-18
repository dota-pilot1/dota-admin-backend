package com.company.dotaadminbackend.application.event;

import org.springframework.context.ApplicationEvent;

public class MemberJoinEvent extends ApplicationEvent {
    
    private final String memberName;
    private final String email;

    public MemberJoinEvent(Object source, String memberName, String email) {
        super(source);
        this.memberName = memberName;
        this.email = email;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getEmail() {
        return email;
    }
}