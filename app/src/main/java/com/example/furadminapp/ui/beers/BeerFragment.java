package com.example.furadminapp.ui.beers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.furadminapp.R;
import com.example.furadminapp.databinding.FragmentBeersBinding;
import com.example.furadminapp.ui.home.CreateNewsActivity;
import com.example.furadminapp.ui.home.HomeFragment;
import com.example.furadminapp.ui.home.HomeModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.io.InputStream;

public class BeerFragment extends Fragment {

    private BeerViewModel beerViewModel;
    private FragmentBeersBinding binding;
    private RecyclerView beersFirestoreList;
    private FirebaseFirestore db;
    FirestoreRecyclerAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        beerViewModel =
                new ViewModelProvider(this).get(BeerViewModel.class);

        binding = FragmentBeersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        db = FirebaseFirestore.getInstance();
        beersFirestoreList = root.findViewById(R.id.rv_beers);

        root.findViewById(R.id.createBeerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), CreateBeerActivity.class);
                getContext().startActivity(i);
            }
        });
        Query query = db.collection("beers").orderBy("name");
        //RecyclerOptions
        FirestoreRecyclerOptions<BeerModel> options = new FirestoreRecyclerOptions.Builder<BeerModel>().setQuery(query, BeerModel.class).build();
        adapter = new FirestoreRecyclerAdapter<BeerModel, BeerViewHolder>(options) {
            @NonNull
            @Override
            public BeerFragment.BeerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_single_beer, parent, false);
                return new BeerViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull BeerViewHolder holder, int position, @NonNull BeerModel model) {
                holder.list_name.setText(model.getName());
                holder.list_type.setText(model.getType());
                holder.list_proof.setText("" + model.getProof() + "%");
                new DownloadImageTask(holder.beer_image)
                        .execute(model.getImageLink());
                holder.list_button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Confirm Deletion");
                        builder.setMessage("Are you sure that you want to delete the beer?");
                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.collection("beers").document(model.getId()).delete();
                            }
                        });
                        builder.show();
                    }
                });
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.d("Firestore Error", e.getMessage());
            }


        };
        beersFirestoreList.setHasFixedSize(true);
        beersFirestoreList.setLayoutManager(new LinearLayoutManager(root.getContext()));
        beersFirestoreList.setAdapter(adapter);

        return root;
    }
    private class BeerViewHolder extends RecyclerView.ViewHolder{

        private TextView list_name;
        private TextView list_type;
        private TextView list_proof;
        private ImageView beer_image;
        private Button list_button;

        public BeerViewHolder(@NonNull View itemView) {
            super(itemView);
            list_name = itemView.findViewById(R.id.list_name);
            list_type = itemView.findViewById(R.id.list_type);
            list_proof = itemView.findViewById(R.id.list_proof);
            beer_image = itemView.findViewById(R.id.beer_image);
            list_button = itemView.findViewById(R.id.list_button);

        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}