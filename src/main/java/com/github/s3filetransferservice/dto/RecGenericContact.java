package com.github.s3filetransferservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RecGenericContact {


    private String tipoContacto;


    private String nombreContacto;


    private String rutContacto;


    private String psesCdgIso;


    private String paisNmr;


    private String rgnsCdg;


    private String rgnsDsc;


    private String cmnsCdgIne;


    private String cmnsNmb;


    private String pstaCdg;


    private String pstaDsc;


    private String baseCdg;


    private String baseDsc;


    private String direccionCompleta;


    private String telefonoPrefijo;


    private String telefonoNum;


    private String telefonoAnexo;


    private String latLonDire;

}