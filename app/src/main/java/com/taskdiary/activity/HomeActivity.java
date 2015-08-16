package com.taskdiary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.taskdiary.adapter.PagerAdapter;
import com.taskdiary.database.DatabaseHelper;
import com.taskdiary.fragment.CompletedTaskFragment;
import com.taskdiary.fragment.CurrentTaskFragment;
import com.taskdiary.fragment.PendingTaskFragment;
import com.taskdiary.utils.Utils;

import java.util.ArrayList;

/**
 * Created by akshaymehta on 05/08/15.
 */
public class HomeActivity extends AppCompatActivity implements View.OnClickListener{
    private TabLayout tabLayout;
    private PagerAdapter pagerAdapter;
    private ArrayList<Fragment> mFragments;
    private ViewPager mViewPager;
    private String[] mTabTitles = {"Todays\'s", "Pending", "Completed"};
    private Toolbar mToolbar;
    private FloatingActionButton addTaskFloatingButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private DatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getWidgetReferences();
        setWidgetEvents();
        initialization();

        setupToolbar();
        setupTablayout();
        setupNavigationView();

        setupViewPager();


    }

    private void setupNavigationView() {
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.create:
                Intent newTaskIntent = new Intent(this,CreateTaskActivity.class);
                startActivity(newTaskIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void getWidgetReferences() {
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        addTaskFloatingButton = (FloatingActionButton) findViewById(R.id.addTaskFloatingButton);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation);

    }

    private void setWidgetEvents() {
        addTaskFloatingButton.setOnClickListener(this);
    }

    private void initialization() {
        mFragments = new ArrayList<Fragment>();
        mFragments.add(new CurrentTaskFragment());
        mFragments.add(new PendingTaskFragment());
        mFragments.add(new CompletedTaskFragment());
        db=new DatabaseHelper(this);
    }

    private void setupViewPager() {
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(),mFragments, mTabTitles);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void setupToolbar(){
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setupTablayout(){
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.addTab(tabLayout.newTab().setText("Current"));
        tabLayout.addTab(tabLayout.newTab().setText("Pending"));
        tabLayout.addTab(tabLayout.newTab().setText("Completed"));
    }

    @Override
    public void onClick(View v) {
        if(v==addTaskFloatingButton)
        {
            Intent newTaskIntent = new Intent(this,CreateTaskActivity.class);
            startActivity(newTaskIntent);
        }
    }


    /**
     * Setup menus action
     *
     * @param navigationView
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                switch (menuItem.getItemId()) {
                    case R.id.create_task:
                        Intent newTaskIntent = new Intent(HomeActivity.this,CreateTaskActivity.class);
                        startActivity(newTaskIntent);
                        break;
                    case R.id.view_category:
                        viewAllDialog();
                        break;
                    case R.id.view_diary:
                        Intent allTaskIntent = new Intent(HomeActivity.this,ViewAllTaskActivity.class);
                        startActivity(allTaskIntent);
                    default:
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });

    }

    private void viewAllDialog() {
        String list[] = db.getAllCategories();
        new MaterialDialog.Builder(this)
                .title(R.string.view_all)
                .items(list)
                .theme(Theme.LIGHT)
                .neutralText(R.string.add)
                .autoDismiss(false)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        dialog.dismiss();
                        editCategoryDialog(text);
                    }
                })
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        dialog.dismiss();
                        Utils.addNewCategory(HomeActivity.this);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void editCategoryDialog(final CharSequence text) {
        final String[] inputCategory = {""};
        new MaterialDialog.Builder(this)
                .title(R.string.edit_category)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .neutralText(R.string.delete)
                .positiveText(R.string.update)
                .negativeText(R.string.cancel)
                .theme(Theme.LIGHT)
                .autoDismiss(false)
                .input("", text, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        inputCategory[0] = input.toString();
                        if (!inputCategory[0].toString().isEmpty()) {
                            db.updateCategory(text, inputCategory[0].toString());
                            Utils.showToast(HomeActivity.this, getString(R.string.update_success));
                            dialog.dismiss();
                        }

                    }
                })
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        db.deleteCategory(text);
                        Utils.showToast(HomeActivity.this, getString(R.string.delete_success));
                        dialog.dismiss();
                    }
                }).show();
    }


}
