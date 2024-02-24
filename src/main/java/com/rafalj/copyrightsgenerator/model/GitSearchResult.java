package com.rafalj.copyrightsgenerator.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class GitSearchResult {
    private String id;
    private String user;
    private String message;
    private Date date;
}
