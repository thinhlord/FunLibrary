package com.teamx.funlibrary;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * Created by ruler_000 on 10/04/2015.
 * Project: cham hoi
 */
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    Activity activity;
    ArrayList<Book> books;
    PopupMenuItemClickListener popupMenuItemClickListener;

    public BookAdapter(Activity activity) {
        this.activity = activity;
        this.books = new ArrayList<>();
    }

    public void updateAdapter(ArrayList<Book> books) {
        this.books = books;
        notifyDataSetChanged();
    }

    public void addBook(ArrayList<Book> books) {
        this.books.addAll(books);
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public BookAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_book_list, parent, false);
        // set the view's size, margins, padding and layout parameters

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        final Book book = books.get(position);
        holder.name.setText(book.name);
        holder.year.setText(book.year + "");
        holder.author.setText(book.author);
        holder.des.setText(book.description);
        holder.pub.setText(book.publisher);
        Picasso.with(activity).load(book.imageUrl).placeholder(R.drawable.ic_book).into(holder.thumb);
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, AddBookActivity.class);
                intent.putExtra(GlobalConst.EXTRA_BOOK, book);
                activity.startActivity(intent);
            }
        });
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(activity, holder.menu);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.menu_book_popup, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (popupMenuItemClickListener != null) {
                            popupMenuItemClickListener.onPopupMenuClick(item, book);
                        }
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        });
    }

    public void setPopupMenuItemClickListener(PopupMenuItemClickListener listener) {
        this.popupMenuItemClickListener = listener;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return books.size();
    }

    public void deleteBook(Book book) {
        books.remove(book);
        notifyDataSetChanged();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View container;
        public ImageView menu;
        public ImageView thumb;
        public TextView name;
        public TextView year;
        public TextView author;
        public TextView pub;
        public TextView des;

        public ViewHolder(View v) {
            super(v);
            container = v;
            name = (TextView) v.findViewById(R.id.name);
            menu = (ImageView) v.findViewById(R.id.menu);
            year = (TextView) v.findViewById(R.id.year);
            author = (TextView) v.findViewById(R.id.author);
            pub = (TextView) v.findViewById(R.id.publisher);
            des = (TextView) v.findViewById(R.id.description);
            thumb = (ImageView) v.findViewById(R.id.thumb);
        }
    }

    public interface PopupMenuItemClickListener {
        void onPopupMenuClick(MenuItem item, Book book);
    }
}
