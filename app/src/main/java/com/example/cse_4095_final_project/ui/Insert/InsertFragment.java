package com.example.cse_4095_final_project.ui.Insert;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.cse_4095_final_project.AlreadyInDatabase;
import com.example.cse_4095_final_project.NotInDatabase;
import com.example.cse_4095_final_project.PortfolioViewModel;
import com.example.cse_4095_final_project.R;
import com.example.cse_4095_final_project.db.DatabaseOperations;
import com.example.cse_4095_final_project.db.Stock;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class InsertFragment extends Fragment {

    private InsertViewModel insertViewModel;

    private static final String TAG = "_MainActivity_";
    private Button addStockButton;
    private Button deleteStockButton;
    private EditText nameEditText;
    private EditText priceEditText;
    private PortfolioViewModel portfolioViewModel;
    private LiveData<List<Stock>> allStocks;

    private Observable<Stock> observable;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //Basic Fragment Setup
        insertViewModel =
                new ViewModelProvider(this).get(InsertViewModel.class);
        View root = inflater.inflate(R.layout.fragment_insert, container, false);

        addStockButton = root.findViewById(R.id.insert_stock_button);
        deleteStockButton = root.findViewById(R.id.delete_stock_button);
        nameEditText = root.findViewById(R.id.stock_name_entry);
        priceEditText = root.findViewById(R.id.stock_price_entry);

        portfolioViewModel = new ViewModelProvider(this).get(PortfolioViewModel.class);
        allStocks = portfolioViewModel.getAllStocks();

        portfolioViewModel.getAllStocks().observe((LifecycleOwner)this,
                new Observer<List<Stock>>() {
                    @Override
                    public void onChanged(List<Stock> stocks) {
                        for (Stock stock : stocks) {
                            if (!allStocks.getValue().contains(stock)){
                                allStocks.getValue().add(stock);
                            }
                        }
                    }
                });

        addStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                double price = Double.parseDouble(priceEditText.getText().toString());
                Stock stock = new Stock(name, price);


                if (isStockInDatabase_faster(stock.name)) {
                    inDataBaseAlert();
                    return;
                }

                stock.databaseOperations = DatabaseOperations.INSERT;
                observable = io.reactivex.Observable.just(stock);
                io.reactivex.Observer<Stock> observer = getStockObserver(stock);

                ((io.reactivex.Observable) observable)
                        .observeOn(Schedulers.io())
                        .subscribe(observer);

            }
        });

        deleteStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                double price = Double.parseDouble(priceEditText.getText().toString());
                Stock stock = new Stock(name, price);

                if (isStockInDatabase_faster(stock.name) == false){
                    NotinDataBaseAlert();
                    return;
                }

                stock.databaseOperations = DatabaseOperations.DELETE;
                observable = io.reactivex.Observable.just(stock);
                io.reactivex.Observer<Stock> observer = getStockObserver(stock);

                ((io.reactivex.Observable) observable)
                        .observeOn(Schedulers.io())
                        .subscribe(observer);
            }
        });

        return root;
    }


    /*
     * Try in UI thread...:-(
     */
    private boolean isStockInDatabase(String name) {
        Stock stock = portfolioViewModel.getPortfolioDatabase().stockDao().isStockInDatabase(name);
        if (null == stock) {
            return false;
        } else {
            return true;
        }
    }


    private boolean isStockInDatabase_faster(String name) {
        boolean inDB = false;
        for (Stock stock : allStocks.getValue()) {
            if (name.equals(stock.name)) {
                inDB = true;
                break;
            }
        }

        return inDB;
    }

    /*
     * https://developer.android.com/guide/topics/ui/dialogs
     */
    private void inDataBaseAlert() {
        new AlreadyInDatabase().show(getParentFragmentManager(), TAG);
    }

    private void NotinDataBaseAlert() {
        new NotInDatabase().show(getParentFragmentManager(), TAG);
    }

    private void listAll() {
        Log.i(TAG, "allStocks size: " + allStocks.getValue().size());
        for (Stock stock : allStocks.getValue()) {
            Log.i(TAG, "Stock: " + stock.name);
            Log.i(TAG, "Stock: " + stock.price);
        }
    }

    private io.reactivex.Observer<Stock> getStockObserver(Stock stock) { // OBSERVER
        return new io.reactivex.Observer<Stock>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe");
            }

            @Override
            public void onNext(@NonNull Stock stock) {
                switch(stock.databaseOperations) {
                    case INSERT:
                        if (!isStockInDatabase(stock.name)) {
                            portfolioViewModel.getPortfolioDatabase().stockDao().insert(stock);
                        }
                        break;
                    case DELETE:
                        portfolioViewModel.getPortfolioDatabase().stockDao().delete(stock);
                        break;
                    case UPDATE:
                        Log.i(TAG, "Update");
                        break;
                    default:
                        Log.i(TAG, "Default");
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "All items are emitted!");
            }
        };
    }
}