package com.atguigu.cdc.model;

import lombok.Data;

import javax.persistence.Column;

@Data
public class AlbumCdcEntity {
    @Column(name = "id")
    private Long id;

    @Column(name = "album_title")
    private String albumTitle;
}