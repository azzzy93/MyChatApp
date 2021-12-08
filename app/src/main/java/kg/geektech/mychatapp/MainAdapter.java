package kg.geektech.mychatapp;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import kg.geektech.mychatapp.databinding.ListMyBinding;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {
    private ListMyBinding binding;
    private List<MainModel> list;

    public MainAdapter() {
        list = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ListMyBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addItems(List<MainModel> list) {
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearList() {
        list.clear();
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private ListMyBinding binding;

        public ViewHolder(@NonNull ListMyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void onBind(MainModel mainModel) {
            if (FirebaseAuth.getInstance().getUid().equals(mainModel.getUserId())) {
                binding.tvMy.setText(mainModel.getText());
                binding.tvFriend.setVisibility(View.GONE);
            } else {
                binding.tvFriend.setText(mainModel.getText());
                binding.tvMy.setVisibility(View.GONE);
            }
        }
    }
}
