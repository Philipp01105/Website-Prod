package com.example.demo.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Wikis")
public class Wiki {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String wikiname;
    private String picPath;
    private String content;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setWikiname(String wikiname) {
        this.wikiname = wikiname;
    }

    public String getWikiname() {
        return wikiname;
    }

    public void setPicPath(String PicPath) {
        this.picPath = PicPath;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setContent(String Content) {
        this.content = Content;
    }

    public String getContent() {
        return content;
    }
}
