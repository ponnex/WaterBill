package com.ponnex.interfacing.waterbill;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.txusballesteros.widgets.FitChart;
import com.txusballesteros.widgets.FitChartValue;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Ramos on 2/20/2016.
 */
public class DummyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dummy, container, false);

        NestedScrollView nestedScrollView = (NestedScrollView)view.findViewById(R.id.nestedscrollview);
        nestedScrollView.setFillViewport(true);

        FitChart fitChart = (FitChart)view.findViewById(R.id.fitChart);

        Collection<FitChartValue> values = new ArrayList<>();
        values.add(new FitChartValue(30f, ContextCompat.getColor(getContext(), R.color.colorStroke1)));
        values.add(new FitChartValue(20f, ContextCompat.getColor(getContext(), R.color.colorStroke3)));
        values.add(new FitChartValue(15f, ContextCompat.getColor(getContext(), R.color.colorStroke5)));
        values.add(new FitChartValue(10f, ContextCompat.getColor(getContext(), R.color.colorStroke7)));

        fitChart.setMinValue(0f);
        fitChart.setMaxValue(100f);
        fitChart.setValues(values);

        return view;
    }
}