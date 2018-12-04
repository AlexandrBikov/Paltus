package com.dev.ornament.paltus;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.dev.ornament.paltus.Entity.DataSample;
import com.dev.ornament.paltus.Service.ImportPALTUSAcceleration;
import com.dev.ornament.paltus.Service.ServiceParser;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickOpen (View v) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri dataFileUri = null;
            if (resultData != null) {
                dataFileUri = resultData.getData();
            }
            drawGraphs(dataFileUri);
        }
    }

    private void drawGraphs(Uri dataFileUri){

        try {
            PushbackInputStream fStream = new PushbackInputStream(getContentResolver().openInputStream(dataFileUri));
            byte buf[] = new byte[fStream.available()];
            fStream.read(buf, 0, buf.length);
            ByteArrayInputStream bArray = new ByteArrayInputStream(buf);

            ServiceParser parser = new ServiceParser();
            ImportPALTUSAcceleration data = parser.parse(bArray);

            ArrayList<DataSample> measX = data.getMeasX();

//            ArrayList<DataSample> measY = data.getMeasY();
//            ArrayList<DataSample> measZ = data.getMeasZ();    //TODO

            GraphView graph = findViewById(R.id.graph);
            GridLabelRenderer gr = graph.getGridLabelRenderer();

            drawAccelGraph(measX, graph);
            drawVelGraph(measX, graph);

            fStream.close();
        } catch (FileNotFoundException ex) {
            System.out.println("File not found");
        } catch (IOException ex) {
            System.out.println("Stream closing error");
        }
    }

    private void drawAccelGraph(ArrayList<DataSample> meas, GraphView graph){
        ArrayList<DataPoint> points = new ArrayList<>();
        double dTimeX = meas.get(0).getTime();
        for (DataSample m: meas) {
            points.add(new DataPoint(m.getTime() - dTimeX, m.getValue()));
        }
        DataPoint[] p = new DataPoint[1];
        p = points.toArray(p);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(p);
        double[] maxMinAccel = accelBoundaries(meas);

        graph.getViewport().setYAxisBoundsManual(true);         //TODO
        graph.getViewport().setMinY(Math.ceil(maxMinAccel[0]*1.3));
        graph.getViewport().setMaxY(Math.floor(maxMinAccel[1]*1.3));

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(Math.ceil(meas.get(0).getTime() - dTimeX));
        graph.getViewport().setMaxX(Math.floor(meas.get(meas.size()-1).getTime() - dTimeX));

        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        ((TextView)findViewById(R.id.max_accel_text_view)).setText(Double.toString(maxMinAccel[1]));
        ((TextView)findViewById(R.id.min_accel_text_view)).setText(Double.toString(maxMinAccel[0]));

        graph.addSeries(series);
    }

    private void drawVelGraph(ArrayList<DataSample> meas, GraphView graph){
        ArrayList<DataPoint> points = new ArrayList<>();
        double dTimeX = meas.get(0).getTime();

        double vel = 0;
        double T = 0;
        double dt = 0;
        double maxVel = -9999;

        for (DataSample m: meas) {
            vel += m.getValue()*dt;
            if(vel > maxVel){
                maxVel = vel;
            }
            dt = m.getTime() - T - dTimeX;
            T += dt;
            if(T < 999999999) {
                points.add(new DataPoint(T, vel));
            } else {
                points.add(new DataPoint(0, vel));
            }
        }
        DataPoint[] p = new DataPoint[1];
        p = points.toArray(p);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(p);
        series.setColor(Color.RED);

        ((TextView)findViewById(R.id.max_vel_text_view)).setText(Double.toString(maxVel));

        graph.addSeries(series);
    }

    private double[] accelBoundaries(ArrayList<DataSample> meas){

        double[] maxMinAccel = new double[2];
        maxMinAccel[0] = maxMinAccel[1] = meas.get(0).getValue();

        for(DataSample dot : meas){
            if(maxMinAccel[0] > dot.getValue()){
                maxMinAccel[0] = dot.getValue();
            }
            if(maxMinAccel[1] < dot.getValue()){
                maxMinAccel[1] = dot.getValue();
            }
        }
        return maxMinAccel;
    }

}
