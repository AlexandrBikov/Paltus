package com.dev.ornament.paltus.Service;

import java.io.ByteArrayInputStream;

public class ServiceParser{
    public ImportPALTUSAcceleration parse(ByteArrayInputStream fStream){
        ImportPALTUSAcceleration data = null;
        try {
            ServiceBaseHeader header = new ServiceBaseHeader(fStream);
            data = new ImportPALTUSAcceleration(fStream, header);
        } catch (ServiceBaseHeaderException ex) {
            System.out.println("Файл невозможно прочитать (14 байт)");
        }
        return data;
    }
}
