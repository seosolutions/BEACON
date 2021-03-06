package com.comp3004.beacon.GUI;

import android.app.SearchManager;

import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.comp3004.beacon.FirebaseServices.DatabaseManager;
import com.comp3004.beacon.NotificationHandlers.CurrentFriendRequestsHandler;
import com.comp3004.beacon.Networking.MessageSenderHandler;
import com.comp3004.beacon.R;
import com.comp3004.beacon.User.BeaconUser;
import com.comp3004.beacon.User.CurrentBeaconUser;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class UserSearchActivity extends GenericActivity {

    ArrayList<BeaconUser> users = new ArrayList<>();
    ListView friendsListView;
    String query = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        friendsListView = (ListView) findViewById(R.id.generic_listview);
        final FriendAdapter friendAdapter = new FriendAdapter(UserSearchActivity.this, users);
        friendsListView.setAdapter(friendAdapter);
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);


            DatabaseReference dbr = DatabaseManager.getInstance().databaseReference.child("beaconUsers");

            dbr.orderByChild("name").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    BeaconUser user = dataSnapshot.getValue(BeaconUser.class);

                    // We do not want our own name to show up in search, check that here
                    if (user.getDisplayName().toLowerCase().contains(query.toLowerCase()) &&
                            !user.getDisplayName().equals(CurrentBeaconUser.getInstance().getDisplayName())) {
                        users.add(user);
                        friendAdapter.notifyDataSetChanged();
                        showProgressBar(false);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        registerUserSearchDialog();
    }

    public void registerUserSearchDialog() {
        final ListView friendsListView = (ListView) findViewById(R.id.generic_listview);

        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BeaconUser selectedBeaconUser = users.get(position);
                if (CurrentBeaconUser.getInstance().getFriend(selectedBeaconUser.getDisplayName()) == null) {
                    showUserSearchDialog(selectedBeaconUser, position);
                }
            }
        });
    }

    public void showUserSearchDialog(BeaconUser beaconUser, final int userIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setTitle(beaconUser.getDisplayName())
                .setItems(new String[]{"Add Friend", "Cancel"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                BeaconUser selectedUser = users.get(userIndex);
                                MessageSenderHandler.getInstance().sendFriendRequest(users.get(userIndex).getUserId());
                                CurrentFriendRequestsHandler.getInstance().setPendingAprovalUser(users.get(userIndex));
                                break;
                            case 1:
                                break;
                        }
                    }
                });
        builder.create().show();
    }

}
