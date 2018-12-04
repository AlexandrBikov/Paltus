package com.dev.ornament.paltus.Service;

import com.dev.ornament.paltus.Entity.DataSample;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ImportPALTUSAcceleration {

    private ByteArrayInputStream fStream;
    private ArrayList<DataSample> measX = new ArrayList<>();
    private ArrayList<DataSample> measY = new ArrayList<>();
    private ArrayList<DataSample> measZ = new ArrayList<>();

    private final int FIRST_BUF_LENGTH = 38;
    private final int SECOND_BUF_LENGTH = 128;

    public ImportPALTUSAcceleration(ByteArrayInputStream tStream, ServiceBaseHeader header) {

        this.fStream = tStream;

            int streamLength = fStream.available();
            double time = 0;
            Calendar measDate = new GregorianCalendar();
            measDate.set(Calendar.AM_PM, Calendar.AM);
            measDate.set(Calendar.HOUR, 0);
            measDate.set(Calendar.MINUTE, 0);
            measDate.set(Calendar.SECOND, 0);
            measDate.set(Calendar.MILLISECOND, 0);
            if (header.isExist()) {
                Calendar creationDate = header.getStartTime();
                time = (creationDate.getTimeInMillis() - measDate.getTimeInMillis())/1000;
            }

            long[] gains = new long[]{4278, 4278, 4278};
            long[] zeros = new long[]{28618, 28618, 28618};
            try {
                if (header.isExist()) {
                    short[] buf = new short[FIRST_BUF_LENGTH];
                    fStream.skip(streamLength - FIRST_BUF_LENGTH);
                    if (read(buf, 0, FIRST_BUF_LENGTH) == FIRST_BUF_LENGTH) {
                        for (int k = 0; k < gains.length; k++) {
                            gains[k] = toUInt32(buf, k * 8);
                            zeros[k] = toUInt32(buf, k * 8 + 4);
                        }
                    }
                }
            } catch (Exception e){}

            fStream.reset();

            short[] buf = new short[SECOND_BUF_LENGTH];

            Float firstT = null;

                while (read(buf, 0, SECOND_BUF_LENGTH) == SECOND_BUF_LENGTH) {
                    if (buf[0] != 0xF0 || buf[1] != 0xF0 || buf[SECOND_BUF_LENGTH - 1] != 0x0F)
                        break;

                    float Tcorr = -(7.654e-3f) / 0.875f;
                    int dscount = 20;
                    int startpos = 6;
                    ArrayList<DataSample> dslist = new ArrayList<>(dscount);
                    double dt = 1e-3;

                    double G = 9.815;

                    float T = (float) ((buf[2] >> 1) - 0.25 + (16 - buf[4]) / 16.0);
                    float dTemp = 0;
                    if (firstT != null && header.isExist())
                        dTemp = T - firstT;
                    else
                        firstT = T;

                    for (int k = 0; k < 3; k++) {
                        dslist.clear();
                        for (int i = 0; i < dscount; i++) {
                            int value = (buf[startpos + i * 6 + k * 2] << 8) + buf[startpos + i * 6 + k * 2 + 1];
                            double zero = zeros[k];
                            if (header.isExist() && header.getVersion() == 3)
                                zero /= 3200;
                            double a = (((double) value - zero) * G / gains[k]);

                            if (dTemp != 0)
                                a += Tcorr * dTemp;

                            dslist.add(new DataSample(time + dt * i, a));
                        }

                        switch (k) {
                            case 0:
                                measX.addAll(dslist);
                            break;
                            case 1:
                                measY.addAll(dslist);
                            break;
                            case 2:
                                measZ.addAll(dslist);
                            break;
                        }
                    }
                    time += dt * dscount;
                }
        }


        private static long toUInt32(short[] bytes, int offset) {
            long result = (int)bytes[offset]&0xff;
            result |= ((int)bytes[offset+1]&0xff) << 8;
            result |= ((int)bytes[offset+2]&0xff) << 16;
            result |= ((int)bytes[offset+3]&0xff) << 24;
            return result & 0xFFFFFFFFL;
        }

        private int read ( short[] b, int off, int len){
            int i = 1;
            //try {
                if (b == null) {
                    throw new NullPointerException();
                } else if (off < 0 || len < 0 || len > b.length - off) {
                    throw new IndexOutOfBoundsException();
                } else if (len == 0) {
                    return 0;
                }

                int c = fStream.read();
                if (c == -1) {
                    return -1;
                }
                b[off] = (short) c;

                for (; i < len; i++) {
                    c = fStream.read();
                    if (c == -1) {
                        break;
                    }
                    b[off + i] = (short) c;
                }
            /*} catch (IOException ee) {
            }*/
            return i;
        }

        public ArrayList<DataSample> getMeasX () {
            return measX;
        }

        public ArrayList<DataSample> getMeasY () {
            return measY;
        }

        public ArrayList<DataSample> getMeasZ () {
            return measZ;
        }
    }

