package com.example.cse_4095_final_project.ui.Display;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.cse_4095_final_project.PortfolioViewModel;
import com.example.cse_4095_final_project.R;
import com.example.cse_4095_final_project.db.Stock;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class DisplayFragment extends Fragment {

    private DisplayViewModel displayViewModel;

    private static final String TAG = "_DisplayFragment_";
    private PortfolioViewModel portfolioViewModel;
    private Button displaystocks;
    private LiveData<List<Stock>> allStocks;
    private ListView listView;
    private ArrayList<String> stockstrings = new ArrayList<String>();

    private Observable<Stock> observable;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        displayViewModel =
                new ViewModelProvider(this).get(DisplayViewModel.class);
        View root = inflater.inflate(R.layout.fragment_display, container, false);

        portfolioViewModel = new ViewModelProvider(this).get(PortfolioViewModel.class);
        allStocks = portfolioViewModel.getAllStocks();

        portfolioViewModel.getAllStocks().observe((LifecycleOwner) this,
                new Observer<List<Stock>>() {
                    @Override
                    public void onChanged(List<Stock> stocks) {
                        for (Stock stock : stocks) {
                            if (!allStocks.getValue().contains(stock)) {
                                allStocks.getValue().add(stock);

                            }
                        }
                    }
                });

        //Set up listview to display all stocks
        listView = root.findViewById(R.id.listView);

        displaystocks = root.findViewById(R.id.button_display);
        displaystocks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stockstrings.clear();
                putStocksinArray();

                final ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, stockstrings);
                listView.setAdapter(adapter);
            }
        });
        return root;
    }


    private void putStocksinArray() {
        String stockentry = "";
        for (Stock stock : allStocks.getValue()){
            stockentry = stock.name + " $" + stock.price;
            stockstrings.add(stockentry);
        }
    }

}
