package com.example.cse_4095_final_project.ui.Update;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UpdateFragment extends Fragment {

    private UpdateViewModel updateViewModel;

    private static final String TAG = "_UpdateFragment_";
    private Button updateStocksButton;
    private EditText nameEditText;
    private EditText priceEditText;
    private PortfolioViewModel portfolioViewModel;
    private LiveData<List<Stock>> allStocks;

    private Observable<Stock> observable;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        updateViewModel =
                new ViewModelProvider(this).get(UpdateViewModel.class);
        View root = inflater.inflate(R.layout.fragment_update, container, false);

        updateStocksButton = root.findViewById(R.id.update_stock_button);
        nameEditText = root.findViewById(R.id.editTextUpdateStockName);
        priceEditText = root.findViewById(R.id.editTextUpdatePrice);

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

        updateStocksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stockname = nameEditText.getText().toString();
                double newprice = Double.parseDouble(priceEditText.getText().toString());
                boolean stockfound = false;
                //Loop through all stocks, see if stock name is present (modified version of is stock in db)
                for (Stock stock : allStocks.getValue()){
                    //If stock is found, update
                    if (stockname.equals(stock.name)){
                        stockfound = true;
                        stock.price = newprice;
                        stock.databaseOperations = DatabaseOperations.UPDATE;
                        observable = io.reactivex.Observable.just(stock);
                        io.reactivex.Observer<Stock> observer = getStockObserver(stock);

                        ((io.reactivex.Observable) observable)
                                .observeOn(Schedulers.io())
                                .subscribe(observer);

                        break;
                    }
                }
                //If stock is not found, display error
                if (stockfound == false){
                    notinDataBaseAlert();
                }
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
    private void notinDataBaseAlert() {
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
                        //Check if stock is in database before
                        portfolioViewModel.getPortfolioDatabase().stockDao().update(stock);
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