package raf.tabiin.tahajudcalculator.ui.calculator;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import raf.tabiin.tahajudcalculator.R;
import raf.tabiin.tahajudcalculator.databinding.FragmentCalculatorBinding;

public class CalculatorFragment extends Fragment {
    FragmentCalculatorBinding b;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        b = FragmentCalculatorBinding.inflate(getLayoutInflater());



        return b.getRoot();
    }
}