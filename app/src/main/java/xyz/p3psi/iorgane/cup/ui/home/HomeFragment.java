package xyz.p3psi.iorgane.cup.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import xyz.p3psi.iorgane.cup.MainActivity;
import xyz.p3psi.iorgane.cup.R;
import xyz.p3psi.iorgane.cup.ui.SearchCup;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private Button searchCupBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        searchCupBtn = (Button)root.findViewById(R.id.search_cup_btn);
        searchCupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchCup.class);
                startActivity(intent);
            }
        });
        return root;
    }
}