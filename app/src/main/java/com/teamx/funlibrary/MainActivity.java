package com.teamx.funlibrary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    BookAdapter mAdapter;
    SwipeRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FunHttpClient.initialize(this);
        getSupportActionBar().setIcon(R.drawable.ic_library_small);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new BookAdapter(this);
        mAdapter.setPopupMenuItemClickListener(new BookAdapter.PopupMenuItemClickListener() {
            @Override
            public void onPopupMenuClick(MenuItem item, final Book book) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        new MaterialDialog.Builder(MainActivity.this).title(R.string.action_delete)
                                .content("Delete book \"" + book.name + "\" ?")
                                .negativeText("No")
                                .positiveText("Yes")
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        deleteBook(book);
                                    }
                                })
                                .show();
                        break;
                    case R.id.action_edit:
                        editBook(book);
                        break;
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new EndlessScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                int offset = (currentPage - 1) * 20 + 1;
                FunHttpClient.getList(20, offset, new FunHttpClient.GetListCallback() {
                    @Override
                    public void onSuccess(ArrayList<Book> list) {
                        mAdapter.addBook(list);
                    }

                    @Override
                    public void onFailure(String error) {
                        toast(error);
                    }
                });
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(mRecyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddBookActivity.class));
            }
        });

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load();
            }
        });
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    @Override
    protected void onResume() {
        super.onResume();
        swipeLayout.setRefreshing(true);
        load();
    }

    private void load() {
        FunHttpClient.getList(20, 0, new FunHttpClient.GetListCallback() {
            @Override
            public void onSuccess(ArrayList<Book> list) {
                mAdapter.updateAdapter(list);
                swipeLayout.setRefreshing(false);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String error) {
                toast(error);
                swipeLayout.setRefreshing(false);
            }
        });
    }

    private void editBook(Book book) {
        Intent intent = new Intent(this, AddBookActivity.class);
        intent.putExtra(GlobalConst.EXTRA_BOOK, book);
        startActivity(intent);
    }

    private void deleteBook(final Book book) {
        FunHttpClient.delete(book.id, new FunHttpClient.CommonCallback() {
            @Override
            public void onSuccess() {
                mAdapter.deleteBook(book);
            }

            @Override
            public void onFailure(String error) {
                toast(error);
            }
        });
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

}
