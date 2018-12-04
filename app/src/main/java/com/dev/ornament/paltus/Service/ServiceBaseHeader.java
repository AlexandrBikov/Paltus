package com.dev.ornament.paltus.Service;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ServiceBaseHeader {
    private Calendar startTime = new GregorianCalendar();
    private short dataType;
    private short serialNum;
    private short version;
    private boolean isExist = false;

    private final byte BUF_LENGTH = 14;

    public ServiceBaseHeader (ByteArrayInputStream fStream) throws ServiceBaseHeaderException{

        short[] buf = new short[BUF_LENGTH];

        //try {
            int streamLength = fStream.available();
            fStream.skip(streamLength-14);
            for(int i = 0; i<14; i++){
                buf[i] = (short)fStream.read();
                if(buf[i] == -1){
                    throw new ServiceBaseHeaderException();
                }
            }
            fStream.reset();
            if (buf[BUF_LENGTH - 1] == 0xAA && buf[BUF_LENGTH - 2] == 0x99 && buf[BUF_LENGTH - 3] == 0x88 && buf[BUF_LENGTH - 4] == 0x77) {
                isExist = true;
            }

            version = buf[BUF_LENGTH-5];

            if (version == 2|| version == 3) {
                dataType = buf[BUF_LENGTH - 7];
            } else {
                dataType = buf[BUF_LENGTH - 6];
            }

            serialNum = buf[BUF_LENGTH - 14];

            setStartTime(buf);

       /* } catch (IOException ex){
            System.out.println("File read error");
        }*/
    }

    private void setStartTime(short[] buf) {
        short y = buf[BUF_LENGTH - 8];
        short m = buf[BUF_LENGTH - 9];
        short d = buf[BUF_LENGTH - 10];
        short sec = buf[BUF_LENGTH - 11];
        short min = buf[BUF_LENGTH - 12];
        short h = buf[BUF_LENGTH - 13];

        startTime.set(y + 2000, m, d, h, min, sec);
    }

    public boolean isExist() {
        return isExist;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public short getDataType() {
        return dataType;
    }

    public short getSerialNum() {
        return serialNum;
    }

    public short getVersion() {
        return version;
    }
}
